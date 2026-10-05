package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.signrequest.BasicAuthorizationAuthenticator;
import fr.paris.lutece.util.signrequest.RequestAuthenticator;

public class JenkinsService implements IJenkinsService
{
	/** The Constant api/json */
    private static final String CST_API_JSON = "api/json";

	/** The Constant json.location */
    private static final String CST_JSON_LOCATION = "Location";

	/** The Constant json.executable */
    private static final String CST_JSON_EXECUTABLE = "executable";

	/** The Constant json.result */
    private static final String CST_JSON_RESULT = "result";

	/** The Constant json.result */
    private static final String CST_JSON_BUILDING = "building";

	/** The Constant json.number */
    private static final String CST_JSON_NUMBER = "number";

    /** The Constant json.url */
    private static final String CST_JSON_URL = "url";

    /** The Constant artifact/ : path of the archived artifacts of a build */
    private static final String CST_ARTIFACT_PATH = "artifact/";

    /** The Constant push_url : parameter of the docker image pipeline */
    private static final String PARAM_PUSH_URL = "push_url";

    /** Delay between two polls of the queue, in milliseconds */
    private static final long QUEUE_POLL_DELAY = 2000L;

    /** Delay between two polls of a running build, in milliseconds */
    private static final long BUILD_POLL_DELAY = 8000L;

    /** The jenkins server base url. */
    private static String JENKINS_BASE_URL;

    /** The jenkins pipeline name */
    private static String JENKINS_PIPELINE_NAME;

    /** The jenkins build type */
    private static String JENKINS_BUILD_TYPE;

    /** The jenkins user login. */
    private static String JENKINS_USER_LOGIN;

    /** The jenkins user pwd. */
    private static String JENKINS_USER_PWD;

    /** The instance. */
    private static IJenkinsService _instance = null;

    /** The JSON mapper */
    private static final ObjectMapper _mapper = new ObjectMapper( );

    /**
     * Gets the service.
     *
     * @return the service
     */
    public static IJenkinsService getService( )
    {
        if ( _instance == null )
        {
            _instance = SpringContextService.getBean( ConstanteUtils.BEAN_JENKINS_SERVICE );
            _instance.init( );
        }

        return _instance;
    }

	@Override
	public void init()
	{
		JENKINS_BASE_URL = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_JENKINS_BASE_URL );
	    JENKINS_PIPELINE_NAME = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_JENKINS_PIPELINE_NAME );
	    JENKINS_BUILD_TYPE = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_JENKINS_BUILD_TYPE );
		JENKINS_USER_LOGIN = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_JENKINS_RELEASE_ACCOUNT_LOGIN );
		JENKINS_USER_PWD = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_JENKINS_RELEASE_ACCOUNT_PASSWORD );
	}

	@Override
	public String TriggerPipeline( WorkflowReleaseContext context )
	{
        String strStatus = null;
        CommandResult commandResult = context.getCommandResult( );

        try
        {
            Map<String, String> params = new HashMap<String, String>();
            params.put( PARAM_PUSH_URL, context.getReleaserResource().getScmUrl() );

            String queueUrl = triggerJob( JENKINS_PIPELINE_NAME, params );

        	commandResult.getLog( ).append( "	Jenkins - Job waiting in pipeline...\n" );

            JenkinsBuild build = waitForBuild( queueUrl );

            commandResult.getLog( ).append( "	Jenkins - Job #" + build.getNumber( ) + " launched --> Build in progress... (may take fiew minutes)\n" );

            while ( strStatus == null )
            {
                Thread.sleep( BUILD_POLL_DELAY );
                strStatus = getBuildResult( build.getUrl( ) );
            }

            commandResult.getLog( ).append( "	Jenkins - Build Completed : " + strStatus + "\n\n" );

        }
        catch( HttpAccessException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, "Erreur lors du déclenchement de la pipeline Jenkins  : "  + e.getMessage( ), e );
        }
        catch (IOException e)
        {
        	ReleaserUtils.addTechnicalError( commandResult, "Erreur lors du déclenchement de la pipeline Jenkins  : "  + e.getMessage( ), e );
		}
        catch (InterruptedException e)
        {
        	ReleaserUtils.addTechnicalError( commandResult, "Erreur lors du déclenchement de la pipeline Jenkins  : "  + e.getMessage( ), e );
		}

        return strStatus;
	}

    @Override
    public String triggerJob( String strJobPath, Map<String, String> mapParams ) throws HttpAccessException
    {
        HttpAccess httpaccess = new HttpAccess( );
        Map<String, String> headersResponse = new HashMap<String, String>( );

        String strUrl = JENKINS_BASE_URL + "/" + strJobPath + "/" + JENKINS_BUILD_TYPE;
        httpaccess.doPost( strUrl, mapParams, getAuthenticator( ), null, null, headersResponse );

        return headersResponse.get( CST_JSON_LOCATION );
    }

    @Override
    public JenkinsBuild waitForBuild( String strQueueUrl ) throws HttpAccessException, IOException, InterruptedException
    {
        HttpAccess httpaccess = new HttpAccess( );
        JenkinsBuild build = null;

        while ( build == null )
        {
            Thread.sleep( QUEUE_POLL_DELAY );
            String strJsonQueueItem = httpaccess.doGet( strQueueUrl + "/" + CST_API_JSON, getAuthenticator( ), null );
            build = parseQueuedBuild( strJsonQueueItem );
        }

        return build;
    }

    @Override
    public String getBuildResult( String strBuildUrl ) throws HttpAccessException, IOException
    {
        HttpAccess httpaccess = new HttpAccess( );
        String strJsonStatus = httpaccess.doGet( withTrailingSlash( strBuildUrl ) + CST_API_JSON, getAuthenticator( ), null );

        return parseBuildResult( strJsonStatus );
    }

    @Override
    public String readArtifact( String strBuildUrl, String strArtifactPath ) throws HttpAccessException
    {
        HttpAccess httpaccess = new HttpAccess( );

        return httpaccess.doGet( withTrailingSlash( strBuildUrl ) + CST_ARTIFACT_PATH + strArtifactPath, getAuthenticator( ), null );
    }

    /**
     * Reads the build of a queue item answer, once Jenkins has started it.
     *
     * @param strJsonQueueItem
     *            the queue item JSON
     * @return the build, or null while the item is still queued
     * @throws IOException
     *             if the JSON cannot be read
     */
    static JenkinsBuild parseQueuedBuild( String strJsonQueueItem ) throws IOException
    {
        if ( strJsonQueueItem == null || strJsonQueueItem.isEmpty( ) )
        {
            return null;
        }

        JsonNode queueItem = _mapper.readTree( strJsonQueueItem );
        if ( !queueItem.has( CST_JSON_EXECUTABLE ) )
        {
            return null;
        }

        JsonNode executable = queueItem.get( CST_JSON_EXECUTABLE );

        return new JenkinsBuild( executable.path( CST_JSON_URL ).asText( null ), executable.path( CST_JSON_NUMBER ).asInt( ) );
    }

    /**
     * Reads the result of a build answer.
     *
     * @param strJsonBuild
     *            the build JSON
     * @return the result, or null while the build is running
     * @throws IOException
     *             if the JSON cannot be read
     */
    static String parseBuildResult( String strJsonBuild ) throws IOException
    {
        JsonNode build = _mapper.readTree( strJsonBuild );
        if ( build.path( CST_JSON_BUILDING ).asBoolean( ) )
        {
            return null;
        }

        return build.path( CST_JSON_RESULT ).asText( null );
    }

    /**
     * Ensures a URL ends with a slash before appending a path.
     *
     * @param strUrl
     *            the URL
     * @return the URL with a trailing slash
     */
    private static String withTrailingSlash( String strUrl )
    {
        return strUrl.endsWith( "/" ) ? strUrl : strUrl + "/";
    }

    /**
     * Returns the authenticator of the Jenkins release account.
     *
     * @return the authenticator
     */
    private static RequestAuthenticator getAuthenticator( )
    {
        return new BasicAuthorizationAuthenticator( JENKINS_USER_LOGIN, JENKINS_USER_PWD );
    }
}
