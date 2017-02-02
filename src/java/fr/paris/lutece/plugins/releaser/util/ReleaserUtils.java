package fr.paris.lutece.plugins.releaser.util;

import java.io.File;

import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class ReleaserUtils
{

   
    public static String getPathCheckoutSite( String strSiteName )
    {
        String strCheckoutBasePath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_SITE_BASE_PAH);

        return strCheckoutBasePath + File.separator + strSiteName;
    }
    
    public static String getLocalComponentPath( String strComponentName)
    {
        String strLocaleComponentBasePath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_COMPONENT_BASE_PAH );

        return strLocaleComponentBasePath + File.separator + strComponentName;
    }
    
    
   
    public static String getLocalComponentPomPath( String strComponentName)
    {
        return getLocalComponentPath( strComponentName )+ File.separator + ConstanteUtils.CONSTANTE_POM_XML;
    }
   
    public static String getGitComponentName( String strScmDeveloperConnection)
    {
        
        if(strScmDeveloperConnection.contains( "/" ) && strScmDeveloperConnection.contains( ".git" ))
        {
            String[] tabDevConnection=strScmDeveloperConnection.split("/");
            return tabDevConnection[tabDevConnection.length-1].replace( ".git","" );
        }
        return null;
    }
    
    public static void addTechnicalError(CommandResult commandResult,String strError, Exception e  )
    {
       
        if(e!=null)
        {
            AppLogService.error(strError,e);
        }else
        {
            AppLogService.error(strError);
        }
        
        if(commandResult!=null)
        {
            commandResult.setError(strError);
            commandResult.setStatus(CommandResult.STATUS_ERROR);
            commandResult.setRunning(false);
            commandResult.setErrorType(CommandResult.ERROR_TYPE_STOP);
        }
      }
    
    public static void addTechnicalError(CommandResult commandResult,String strError  )
    {
            addTechnicalError( commandResult, strError ,null);
     }
    
    
    public static void startCommandResult( WorkflowReleaseContext context )
    {
        CommandResult commandResult = new CommandResult(  );
        commandResult.setLog( new StringBuffer(  ) );
        commandResult.setRunning( true );
        commandResult.setStatus(CommandResult.STATUS_OK);
  
        context.setCommandResult( commandResult );
        
    }
    
   

    public static void stopCommandResult( WorkflowReleaseContext context )
    {
        context.getCommandResult( ).setRunning( false );
    }
    


}
