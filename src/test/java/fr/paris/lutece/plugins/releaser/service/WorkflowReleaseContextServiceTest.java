package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.util.Locale;

import org.junit.Test;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.service.WorkflowReleaseContextService;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.test.LuteceTestCase;

public class WorkflowReleaseContextServiceTest extends LuteceTestCase
{

    
    
   @Test
   public void testReleaseComponentGit() throws IOException
   {
       
      WorkflowReleaseContext context=initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ));
      
      
      ReleaserUtils.startCommandResult( context ) ;
      
      WorkflowReleaseContextService.getService( ).gitCloneRepository( context, Locale.FRENCH );
      WorkflowReleaseContextService.getService( ).gitMerge( context, Locale.FRENCH );
      WorkflowReleaseContextService.getService( ).realeasePrepareGit( context, Locale.FRENCH );
      WorkflowReleaseContextService.getService( ).realeasePerformGit( context, Locale.FRENCH );
        
      ReleaserUtils.stopCommandResult( context ) ;
       
   }
   
   
    
   @Test
    private void testGitCloneRepository() throws IOException
    {
     
      WorkflowReleaseContext context=initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ));
       
       ReleaserUtils.startCommandResult( context ) ;   
      
       WorkflowReleaseContextService.getService( ).gitCloneRepository( context, Locale.FRENCH );
      
       ReleaserUtils.stopCommandResult( context ) ;   
        
    }
    
  
    
    
   public static WorkflowReleaseContext initContextServiceTest(String strRessourceDir,String strClassName) throws IOException
    {
        
        LuteceTestFileUtils.injectTestProperties( strRessourceDir, strClassName);
        String strArtifactId=AppPropertiesService.getProperty( "releaser.componentTest.artifactId" );
        String strScmDevelopperConnection=AppPropertiesService.getProperty( "releaser.componentTest.scmDeveloperConnection" );
        String strGitHubUserLogin=AppPropertiesService.getProperty(ConstanteUtils.PROPERTY_GITHUB_RELEASE_COMPONET_ACCOUNT_LOGIN );
        String strGitHubUserPassword=AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITHUB_RELEASE_COMPONET_ACCOUNT_PASSWORD);
        String strReleaserVersion=AppPropertiesService.getProperty( "releaser.componentTest.releaseVersion" );
        String strReleaserTagName=AppPropertiesService.getProperty( "releaser.componentTest.releaseTagName" );
        String strReleaserNewDeveloppmentVersion=AppPropertiesService.getProperty( "releaser.componentTest.releaseNewDeveloppmentVersion");
        
        
        
        
        
        WorkflowReleaseContext context=new WorkflowReleaseContext( );
        Component component=new Component( );
        component.setArtifactId( strArtifactId );
        component.setScmDeveloperConnection( strScmDevelopperConnection );
        component.setNextSnapshotVersion( strReleaserNewDeveloppmentVersion );
        component.setTargetVersion( strReleaserVersion );
    
        
        context.setGitHubUserLogin( strGitHubUserLogin );
        context.setGitHubUserPassord( strGitHubUserPassword );
        context.setComponent(component );
        return context;
        
    }
    
    
    
    
    

}
