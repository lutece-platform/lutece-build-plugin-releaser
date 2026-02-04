package fr.paris.lutece.plugins.releaser.service;

import java.util.HashMap;

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

public class JenkinsService implements IJenkinsService {

    /** The jenkins url server. */
    private static String URL_JENKINS_SERVER;

    /** The jenkins user. */
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
		URL_JENKINS_SERVER = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_URL_JENKINS_SERVICE );
		JENKINS_USER_LOGIN = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_JENKINS_RELEASE_ACCOUNT_LOGIN );
		JENKINS_USER_PWD = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_JENKINS_RELEASE_ACCOUNT_PASSWORD );

	}

	@Override
	public String TriggerPipeline( String strRepositoryUrl, String strBranchToRelease ) 
	{
        String strJsonResult = null;
        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        CommandResult commandResult = context.getCommandResult( );

        try
        {
            HttpAccess httpaccess = new HttpAccess( );

            HashMap<String, String> params = new HashMap<String, String>();
            params.put( "push_url", strRepositoryUrl );
        	//params.put( "branch", strBranchToRelease );
    
        	RequestAuthenticator requestAuthenticator = new BasicAuthorizationAuthenticator( JENKINS_USER_LOGIN, JENKINS_USER_PWD );
          
            String response = httpaccess.doPost(URL_JENKINS_SERVER, params, requestAuthenticator, null);
            
        }
        catch( HttpAccessException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, "Erreur lors du déclenchement de la pipeline Jenkins  : "  + e.getMessage( ), e );
            
        }

        return strJsonResult;    
	}
}

