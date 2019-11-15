package fr.paris.lutece.plugins.releaser.util;

import java.io.File;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.service.WorkflowReleaseContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class ReleaserUtils
{

    public static final String REGEX_ID = "^[\\d]+$";

    public static String getWorklowContextDataKey( String strArtifactId, int nContextId )
    {
        return ConstanteUtils.CONSTANTE_RELEASE_CONTEXT_PREFIX + strArtifactId + "_" + nContextId;
    }
    
    public static String getLastReleaseVersionDataKey( String strArtifactId )
    {
        return ConstanteUtils.CONSTANTE_LAST_RELEASE_VERSION_PREFIX + strArtifactId ;
    }
    
    public static String getLastReleaseNextSnapshotVersionDataKey( String strArtifactId )
    {
        return ConstanteUtils.CONSTANTE_LAST_RELEASE_NEXT_SNPASHOT_VERSION_PREFIX + strArtifactId ;
    }

    
   public static String getLocalPath(WorkflowReleaseContext context)
   {
       String strPath=null;
       if(context.getSite( )!=null)
       {
           strPath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_SITE_BASE_PAH )+ File.separator + context.getSite( ).getArtifactId( );

       }
       else
       {
           
            strPath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_COMPONENT_BASE_PAH )+File.separator + context.getComponent( ).getName( );

       }
       
       return strPath;
           
    }
    
   public static String getLocalPomPath(WorkflowReleaseContext context)
   {
     
       return getLocalPath( context ) + File.separator + ConstanteUtils.CONSTANTE_POM_XML;
   
   }

    public static String getLocalSitePath( Site site )
    {
        String strCheckoutBasePath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_SITE_BASE_PAH );

        return strCheckoutBasePath + File.separator + site.getArtifactId( );
    }

    public static String getLocalSitePomPath( Site site )
    {
        return getLocalSitePath( site ) + File.separator + ConstanteUtils.CONSTANTE_POM_XML;
    }
//
//    public static String getLocalComponentPath( String strComponentName )
//    {
//        String strLocaleComponentBasePath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_COMPONENT_BASE_PAH );
//
//        return strLocaleComponentBasePath + File.separator + strComponentName;
//    }
//
//    public static String getLocalComponentPomPath( String strComponentName )
//    {
//        return getLocalComponentPath( strComponentName ) + File.separator + ConstanteUtils.CONSTANTE_POM_XML;
//    }

    public static String getComponentName( String strScmDeveloperConnection,String strArtifactId )
    {
        
        String strComponentName=strArtifactId;
        if(!StringUtils.isEmpty( strScmDeveloperConnection) && strScmDeveloperConnection.contains( "/" ) && strScmDeveloperConnection.contains( ".git" ) )
        {   
            String [ ] tabDevConnection = strScmDeveloperConnection.split( "/" );
            strComponentName= tabDevConnection [tabDevConnection.length - 1].replace( ".git", "" );
        
         }
        return strComponentName;
    }
    
    public static String getSiteTagName(Site site)
    {

        String strTagName="";
        
        if(site!=null)
        {
            strTagName= site.getArtifactId( )+"-"+site.getNextReleaseVersion( );
         }
        return strTagName;
    }
    
    
    
    public static void addInfoError( CommandResult commandResult, String strError, Exception e )
    {

        if ( e != null )
        {
            AppLogService.error( strError, e );
        }
        else
        {
            AppLogService.error( strError );
        }

        if ( commandResult != null )
        {
            commandResult.setError( strError );
            commandResult.setStatus( CommandResult.STATUS_ERROR );
            commandResult.setErrorType( CommandResult.ERROR_TYPE_INFO );
         }
        
    }

    public static void addTechnicalError( CommandResult commandResult, String strError, Exception e ) throws AppException
    {

        if ( e != null )
        {
            AppLogService.error( strError, e );
        }
        else
        {
            AppLogService.error( strError );
        }

        if ( commandResult != null )
        {
            commandResult.setError( strError );
            commandResult.setStatus( CommandResult.STATUS_ERROR );
            commandResult.setRunning( false );
            commandResult.setErrorType( CommandResult.ERROR_TYPE_STOP );
            commandResult.setDateEnd( new Date( ) );
        }
        if ( e != null )
        {
            throw new AppException( strError, e );
        }
        else
        {
            throw new AppException( strError );
        }
    }

    public static void addTechnicalError( CommandResult commandResult, String strError ) throws AppException
    {
        addTechnicalError( commandResult, strError, null );
    }

    public static void startCommandResult( WorkflowReleaseContext context )
    {
        CommandResult commandResult = new CommandResult( );
        commandResult.setDateBegin( new Date( ) );
        commandResult.setLog( new StringBuffer( ) );
        commandResult.setRunning( true );
        commandResult.setStatus( CommandResult.STATUS_OK );
        commandResult.setProgressValue( 0 );
        context.setCommandResult( commandResult );
        WorkflowReleaseContextService.getService( ).startReleaseInProgress( context.getComponent( )!=null ?context.getComponent( ).getArtifactId( ) :context.getSite().getArtifactId( ) );
        

    }

    public static void logStartAction( WorkflowReleaseContext context, String strActionName )
    {

        context.getCommandResult( ).getLog( ).append( "******************Start Action: \"" + strActionName + "\" *******************\n\r" );

    }

    public static void logEndAction( WorkflowReleaseContext context, String strActionName )
    {
        context.getCommandResult( ).getLog( ).append( "******************End Action:\"" + strActionName + "\" *******************\n\r" );

    }

    public static void stopCommandResult( WorkflowReleaseContext context )
    {
        context.getCommandResult( ).setRunning( false );
        context.getCommandResult( ).setDateEnd( new Date( ) );
        context.getCommandResult( ).setProgressValue( 100 );
        WorkflowReleaseContextService.getService( ).stopReleaseInProgress( context.getComponent( )!=null ?context.getComponent( ).getArtifactId( ) :context.getSite().getArtifactId( ) );
        
    }

    /**
     * convert a string to int
     * 
     * @param strParameter
     *            the string parameter to convert
     * @return the conversion
     */
    public static int convertStringToInt( String strParameter )
    {
        int nIdParameter = ConstanteUtils.CONSTANTE_ID_NULL;

        try
        {
            if ( ( strParameter != null ) && strParameter.matches( REGEX_ID ) )
            {
                nIdParameter = Integer.parseInt( strParameter );
            }
        }
        catch( NumberFormatException ne )
        {
            AppLogService.error( ne );
        }

        return nIdParameter;
    }

    public static ReleaserUser getReleaserUser( HttpServletRequest request, Locale locale )
    {

        ReleaserUser releaserUser = null;

        if ( isApplicationAccountEnable( ) )
        {

            releaserUser = new ReleaserUser( );
            releaserUser.addCredential(RepositoryType.GITHUB, releaserUser.new Credential(AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITHUB_RELEASE_ACCOUNT_LOGIN ), ConstanteUtils.PROPERTY_GITHUB_RELEASE_ACCOUNT_PASSWORD ));
            releaserUser.addCredential(RepositoryType.GITLAB, releaserUser.new Credential(AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITLAB_RELEASE_ACCOUNT_LOGIN ), ConstanteUtils.PROPERTY_GITLAB_RELEASE_ACCOUNT_PASSWORD ));
            releaserUser.addCredential(RepositoryType.SVN, releaserUser.new Credential(AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_SVN_RELEASE_ACCOUNT_LOGIN ), ConstanteUtils.PROPERTY_GITLAB_RELEASE_ACCOUNT_PASSWORD ));
               
         }
        else
        {

            HttpSession session = ( request != null ) ? request.getSession( true ) : null;

            if ( session != null )
            {
                return (ReleaserUser) session.getAttribute( ConstanteUtils.ATTRIBUTE_RELEASER_USER );
            }

        }

        return releaserUser;
    }
    
    public static void populateReleaserUser( HttpServletRequest request, ReleaserUser user )
    {

        RepositoryType[] tabCredentialType= RepositoryType.values();
    	for (int i = 0; i < tabCredentialType.length; i++) {
    	
		if(!StringUtils.isEmpty( request.getParameter(tabCredentialType[i]+"_account_login")))
            {
            	 user.addCredential(tabCredentialType[i], user.new Credential(request.getParameter(tabCredentialType[i]+"_account_login"), request.getParameter(tabCredentialType[i]+"_account_password")));
                  	
           }
               
    	}
    }


    public static void setReleaserUser( HttpServletRequest request, ReleaserUser releaserUser )
    {

        HttpSession session = ( request != null ) ? request.getSession( true ) : null;

        if ( session != null )
        {
            session.setAttribute( ConstanteUtils.ATTRIBUTE_RELEASER_USER, releaserUser );
        }

    }

    public static boolean isApplicationAccountEnable( )
    {

        return AppPropertiesService.getPropertyBoolean( ConstanteUtils.PROPERTY_APPLICATION_ACCOUNT_ENABLE, false );

    }
    
    public static int compareVersion(String strVersion1,String strVersion2)
    {
        
        
      ComparableVersion cVersion1= new ComparableVersion( strVersion1 );
      ComparableVersion cVersion2= new ComparableVersion( strVersion2 );
     
      return cVersion1.compareTo( cVersion2 );
        
     
    }
    
    
    

   
}
