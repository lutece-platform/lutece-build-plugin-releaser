/*
 * Copyright (c) 2002-2017, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;

import org.junit.Test;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * SiteServiceTest
 */
public class JiraServiceTest extends LuteceTestCase
{

    
     @Test
    public void testJiraService() throws IOException
    {
        
      
         WorkflowReleaseContext context=initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ));
       
       
       ReleaserUtils.startCommandResult( context ) ;
       
       JiraComponentService.getService( ).updateComponentVersions( context.getComponent( ), context.getCommandResult( ) );
         
       ReleaserUtils.stopCommandResult( context ) ;
        
    }
    
    
     
  
     
   
     
     
    public static WorkflowReleaseContext initContextServiceTest(String strRessourceDir,String strClassName) throws IOException
     {
         
         LuteceTestFileUtils.injectTestProperties( strRessourceDir, strClassName);
         String strArtifactId=AppPropertiesService.getProperty( "releaser.componentTest.artifactId" );
         String strScmDevelopperConnection=AppPropertiesService.getProperty( "releaser.componentTest.scmDeveloperConnection" );
         String strGitHubUserLogin=AppPropertiesService.getProperty(ConstanteUtils.PROPERTY_GITHUB_RELEASE_COMPONET_ACCOUNT_LOGIN );
         String strGitHubUserPassword=AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITHUB_RELEASE_COMPONET_ACCOUNT_PASSWORD);
         String strJiraKey=AppPropertiesService.getProperty( "releaser.componentTest.jiraKey" );
         String strCurrentVersion=AppPropertiesService.getProperty( "releaser.componentTest.currentVersion" );
         String strReleaserVersion=AppPropertiesService.getProperty( "releaser.componentTest.releaseVersion" );
         String strReleaserTagName=AppPropertiesService.getProperty( "releaser.componentTest.releaseTagName" );
         String strReleaserNewDeveloppmentVersion=AppPropertiesService.getProperty( "releaser.componentTest.releaseNewDeveloppmentVersion");
         
             
         
         
         
         WorkflowReleaseContext context=new WorkflowReleaseContext( );
         Component component=new Component( );
         component.setArtifactId( strArtifactId );
         component.setScmDeveloperConnection( strScmDevelopperConnection );
         component.setNextSnapshotVersion( strReleaserNewDeveloppmentVersion );
         component.setTargetVersion( strReleaserVersion );
         component.setCurrentVersion( strCurrentVersion );
         component.setJiraCode( strJiraKey );
         ReleaserUser user=new ReleaserUser( );
         user.setGithubComponentAccountLogin( strGitHubUserLogin );
         user.setGithubComponentAccountPassword( strGitHubUserPassword );
         context.setReleaserUser( user );
         context.setComponent(component );
         return context;
         
     }
}