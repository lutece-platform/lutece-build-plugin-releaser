package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.util.Locale;

import org.junit.Test;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.test.LuteceTestCase;

public class WorkflowReleaseContextServiceTest extends LuteceTestCase
{

    
   @Test
   public void testReleaseComponentGit() throws IOException
   {
       
      WorkflowReleaseContext context=initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ),false);
      
      
      ReleaserUtils.startCommandResult( context ) ;
      
      WorkflowReleaseContextService.getService( ).gitCloneRepository( context, Locale.FRENCH );
      WorkflowReleaseContextService.getService( ).gitMerge( context, Locale.FRENCH );
      WorkflowReleaseContextService.getService( ).releasePrepareGit( context, Locale.FRENCH );
      WorkflowReleaseContextService.getService( ).releasePerformGit( context, Locale.FRENCH );
        
      ReleaserUtils.stopCommandResult( context ) ;
       
   }
   
   
    
   @Test
    public void testGitCloneRepository() throws IOException
    {
     
      WorkflowReleaseContext context=initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ),false);
       
       ReleaserUtils.startCommandResult( context ) ;   
      
       WorkflowReleaseContextService.getService( ).gitCloneRepository( context, Locale.FRENCH );
      
       ReleaserUtils.stopCommandResult( context ) ;   
        
    }
   
   @Test
   public void testPrepareSvn() throws IOException
   {
    
      WorkflowReleaseContext context=initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ),true);
      
      ReleaserUtils.startCommandResult( context ) ;   
     
      WorkflowReleaseContextService.getService( ).checkoutComponent( context, Locale.FRENCH );
      WorkflowReleaseContextService.getService( ).releasePrepareSvn( context, Locale.FRENCH );
      
      ReleaserUtils.stopCommandResult( context ) ;   
       
   }
    
  
    
    
   public static WorkflowReleaseContext initContextServiceTest(String strRessourceDir,String strClassName,boolean bSvnComponent) throws IOException
    {
        
        LuteceTestFileUtils.injectTestProperties( strRessourceDir, strClassName);
        String strArtifactId=!bSvnComponent?AppPropertiesService.getProperty( "releaser.componentTest.artifactId" ):AppPropertiesService.getProperty( "releaser.componentTestSvn.artifactId" );
        String strScmDevelopperConnection=!bSvnComponent?AppPropertiesService.getProperty( "releaser.componentTest.scmDeveloperConnection"):AppPropertiesService.getProperty( "releaser.componentTestSvn.scmDeveloperConnection");
        String strUserLogin=!bSvnComponent?AppPropertiesService.getProperty("releaser.componentTest.releaseAccountLogin"):AppPropertiesService.getProperty("releaser.componentTestSvn.releaseAccountLogin");
        String strUserPassword=!bSvnComponent?AppPropertiesService.getProperty( "releaser.componentTest.releaseAccountPassword"):AppPropertiesService.getProperty( "releaser.componentTestSvn.releaseAccountPassword");
        String strReleaserVersion=!bSvnComponent?AppPropertiesService.getProperty( "releaser.componentTest.releaseVersion" ):AppPropertiesService.getProperty( "releaser.componentTestSvn.releaseVersion" );
        String strReleaserTagName=!bSvnComponent?AppPropertiesService.getProperty( "releaser.componentTest.releaseTagName" ):AppPropertiesService.getProperty( "releaser.componentTestSvn.releaseTagName" );
        String strReleaserNewDeveloppmentVersion=!bSvnComponent?AppPropertiesService.getProperty( "releaser.componentTest.releaseNewDeveloppmentVersion"):AppPropertiesService.getProperty( "releaser.componentTestSvn.releaseNewDeveloppmentVersion");
        String strReleaserCurrentVersion=!bSvnComponent?AppPropertiesService.getProperty( "releaser.componentTest.currentVersion"):AppPropertiesService.getProperty( "releaser.componentTestSvn.currentVersion");
        
      
        
        
        
        WorkflowReleaseContext context=new WorkflowReleaseContext( );
        Component component=new Component( );
        component.setArtifactId( strArtifactId );
        component.setScmDeveloperConnection( strScmDevelopperConnection );
        component.setNextSnapshotVersion( strReleaserNewDeveloppmentVersion );
        component.setTargetVersion( strReleaserVersion );
        component.setLastAvailableSnapshotVersion( strReleaserCurrentVersion );
        component.setCurrentVersion( strReleaserCurrentVersion );
        
        if(bSvnComponent)
        {
            component.setName( strArtifactId );
        }
        else
        {
            
            component.setName( ReleaserUtils.getComponentName( strScmDevelopperConnection, strArtifactId ));
        }
        
        
        ReleaserUser user=new ReleaserUser( );
        user.setGithubComponentAccountLogin( strUserLogin );
        user.setGithubComponentAccountPassword(strUserPassword );
        user.setSvnComponentAccountLogin( strUserLogin );
        user.setSvnComponentAccountPassword(strUserPassword );
        context.setReleaserUser( user );
        context.setComponent(component );
        return context;
        
    }
    
    
    
    
    

}
