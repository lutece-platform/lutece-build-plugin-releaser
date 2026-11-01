package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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

    /** The Constant json.cancelled : queue item cancelled before it started */
    private static final String CST_JSON_CANCELLED = "cancelled";

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

        // The queue item is polled with the Jenkins account : only on the configured server
        String strLocation = headersResponse.get( CST_JSON_LOCATION );
        if ( StringUtils.isBlank( strLocation ) )
        {
            throw new HttpAccessException( "Jenkins answered the trigger of " + strJobPath + " without a Location header : the queue item is unknown", null );
        }

        return rebaseOnJenkinsUrl( strLocation );
    }

    @Override
    public JenkinsBuild waitForBuild( String strQueueUrl ) throws HttpAccessException, IOException, InterruptedException
    {
        JenkinsBuild build = null;

        while ( build == null )
        {
            Thread.sleep( QUEUE_POLL_DELAY );
            build = getQueuedBuild( strQueueUrl );
        }

        return build;
    }

    @Override
    public JenkinsBuild getQueuedBuild( String strQueueUrl ) throws HttpAccessException, IOException
    {
        HttpAccess httpaccess = new HttpAccess( );
        String strJsonQueueItem = httpaccess.doGet( strQueueUrl + "/" + CST_API_JSON, getAuthenticator( ), null );

        return parseQueuedBuild( strJsonQueueItem );
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
        if ( queueItem.path( CST_JSON_CANCELLED ).asBoolean( ) )
        {
            throw new IJenkinsService.QueueItemCancelledException( "Jenkins queue item cancelled" );
        }
        String strUrl = queueItem.path( CST_JSON_EXECUTABLE ).path( CST_JSON_URL ).asText( null );
        if ( StringUtils.isBlank( strUrl ) )
        {
            return null;
        }

        return new JenkinsBuild( rebaseOnJenkinsUrl( strUrl ), queueItem.path( CST_JSON_EXECUTABLE ).path( CST_JSON_NUMBER ).asInt( ) );
    }

    /**
     * Puts a build URL given by Jenkins on the configured Jenkins server (scheme, host and port of releaser.JenkinsBaseUrl) : the root URL
     * set in Jenkins may name another host (internal name, http), and the credentials must only go to the configured one.
     *
     * @param strUrl
     *            the URL given by Jenkins
     * @return the URL on the configured server, unchanged when no base URL is configured
     */
    static String rebaseOnJenkinsUrl( String strUrl )
    {
        if ( StringUtils.isBlank( JENKINS_BASE_URL ) )
        {
            return strUrl;
        }
        if ( strUrl == null )
        {
            return null;
        }
        try
        {
            URI base = new URI( JENKINS_BASE_URL );
            URI url = new URI( strUrl );
            // Raw parts : an encoded "/" of a branch name in a multibranch job URL must stay encoded
            String strRebased = base.getScheme( ) + "://" + base.getRawAuthority( ) + StringUtils.defaultString( url.getRawPath( ) );

            return url.getRawQuery( ) != null ? strRebased + "?" + url.getRawQuery( ) : strRebased;
        }
        catch( URISyntaxException e )
        {
            return strUrl;
        }
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
