package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.util.HashMap;
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
		String url;
        boolean bBuilding = true;
        String strStatus = null;
    	int nBuildNumber =  -1;

        CommandResult commandResult = context.getCommandResult( );
        ObjectMapper mapper = new ObjectMapper( );

        try
        {
            HttpAccess httpaccess = new HttpAccess( );

            HashMap<String, String> params = new HashMap<String, String>();
            HashMap<String, String> headersResponse = new HashMap<String, String>();
            params.put( "push_url", context.getReleaserResource().getScmUrl() );
        	//params.put( "branch", context.getSite().getBranchReleaseFrom() );
    
        	RequestAuthenticator requestAuthenticator = new BasicAuthorizationAuthenticator( JENKINS_USER_LOGIN, JENKINS_USER_PWD );
          
        	url = JENKINS_BASE_URL + "/" + JENKINS_PIPELINE_NAME + "/" + JENKINS_BUILD_TYPE;
        	httpaccess.doPost(url, params, requestAuthenticator, null, null, headersResponse);
        	String queueUrl = headersResponse.get(CST_JSON_LOCATION);
        	
        	commandResult.getLog( ).append( "	Jenkins - Job waiting in pipeline...\n" );
        	
            while (nBuildNumber == -1) 
            {
                Thread.sleep(2000);
                
                String jsonQueueItem = httpaccess.doGet(queueUrl + "/" + CST_API_JSON, requestAuthenticator, null);
                
                if ( jsonQueueItem != null && !jsonQueueItem.isEmpty() )
            	{
                	JsonNode objectNodeJson = mapper.readTree(jsonQueueItem);
                	
                	if (objectNodeJson.has(CST_JSON_EXECUTABLE)) {
                        nBuildNumber = objectNodeJson.get(CST_JSON_EXECUTABLE).get(CST_JSON_NUMBER).asInt();
                    }    		
            	}
            }
       
            commandResult.getLog( ).append( "	Jenkins - Job #" + nBuildNumber + " launched --> Build in progress... (may take fiew minutes)\n" );
            
            while (bBuilding) 
            {
                Thread.sleep(8000);
                
                url = JENKINS_BASE_URL + "/" + JENKINS_PIPELINE_NAME + "/" + nBuildNumber + "/" + CST_API_JSON;
                String jsonStatus = httpaccess.doGet(url, requestAuthenticator, null);

                JsonNode ObjectBuildJson = mapper.readTree(jsonStatus);

                bBuilding = ObjectBuildJson.get(CST_JSON_BUILDING).asBoolean();

                if (!bBuilding) {
                    strStatus = ObjectBuildJson.get(CST_JSON_RESULT).asText();
                }
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
}

