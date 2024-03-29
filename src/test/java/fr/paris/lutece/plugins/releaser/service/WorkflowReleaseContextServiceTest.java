/*
 * Copyright (c) 2002-2021, City of Paris
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
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser.Credential;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.test.LuteceTestCase;

public class WorkflowReleaseContextServiceTest extends LuteceTestCase
{

    @Ignore
    @Test
    public void testReleaseSiteGitlab( ) throws IOException
    {

        WorkflowReleaseContext context = initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ), RepositoryType.GITLAB, true );

        ReleaserUtils.startCommandResult( context );

        WorkflowReleaseContextService.getService( ).checkoutRepository( context, Locale.FRENCH );
        WorkflowReleaseContextService.getService( ).mergeDevelopMaster( context, Locale.FRENCH );

        WorkflowReleaseContextService.getService( ).releasePrepareSite( context, Locale.FRENCH );

        System.out.println( context.getCommandResult( ).getLog( ).toString( ) );

        ReleaserUtils.stopCommandResult( context );

    }

    @Ignore
    @Test
    public void testReleaseSiteSvn( ) throws IOException
    {

        WorkflowReleaseContext context = initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ), RepositoryType.SVN, true );

        try
        {

            ReleaserUtils.startCommandResult( context );

            WorkflowReleaseContextService.getService( ).checkoutRepository( context, Locale.FRENCH );

            WorkflowReleaseContextService.getService( ).releasePrepareSite( context, Locale.FRENCH );

            System.out.println( context.getCommandResult( ).getLog( ).toString( ) );

            ReleaserUtils.stopCommandResult( context );
        }
        catch( AppException e )
        {
            System.out.println( context.getCommandResult( ).toString( ) );
        }
    }

    @Ignore
    @Test
    public void testReleaseComponentSvn( ) throws IOException
    {

        WorkflowReleaseContext context = initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ), RepositoryType.SVN, false );
        try
        {

            ReleaserUtils.startCommandResult( context );

            WorkflowReleaseContextService.getService( ).checkoutRepository( context, Locale.FRENCH );
            WorkflowReleaseContextService.getService( ).releasePrepareComponent( context, Locale.FRENCH );
            WorkflowReleaseContextService.getService( ).releasePerformComponent( context, Locale.FRENCH );

            System.out.println( context.getCommandResult( ).getLog( ).toString( ) );

            ReleaserUtils.stopCommandResult( context );
        }
        catch( AppException e )
        {
            System.out.println( context.getCommandResult( ).toString( ) );
        }

    }

    @Ignore
    @Test
    public void testReleaseComponentGithub( ) throws IOException
    {

        WorkflowReleaseContext context = initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ), RepositoryType.GITHUB, false );

        ReleaserUtils.startCommandResult( context );

        WorkflowReleaseContextService.getService( ).checkoutRepository( context, Locale.FRENCH );
        WorkflowReleaseContextService.getService( ).mergeDevelopMaster( context, Locale.FRENCH );
        WorkflowReleaseContextService.getService( ).releasePrepareComponent( context, Locale.FRENCH );
        WorkflowReleaseContextService.getService( ).releasePerformComponent( context, Locale.FRENCH );

        ReleaserUtils.stopCommandResult( context );

    }

    @Ignore
    @Test
    public void testGitCloneRepositoryGithub( ) throws IOException
    {

        WorkflowReleaseContext context = initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ), RepositoryType.GITHUB, false );

        ReleaserUtils.startCommandResult( context );

        WorkflowReleaseContextService.getService( ).checkoutRepository( context, Locale.FRENCH );

        ReleaserUtils.stopCommandResult( context );

    }

    @Ignore
    @Test
    public void testPrepareComponentSvn( ) throws IOException
    {

        WorkflowReleaseContext context = initContextServiceTest( this.getResourcesDir( ), this.getClass( ).getCanonicalName( ), RepositoryType.SVN, false );

        ReleaserUtils.startCommandResult( context );

        WorkflowReleaseContextService.getService( ).checkoutRepository( context, Locale.FRENCH );
        WorkflowReleaseContextService.getService( ).releasePrepareComponent( context, Locale.FRENCH );

        ReleaserUtils.stopCommandResult( context );

    }

    public static WorkflowReleaseContext initContextServiceTest( String strRessourceDir, String strClassName, RepositoryType repotype, boolean bSite )
            throws IOException
    {

        LuteceTestFileUtils.injectTestProperties( strRessourceDir, strClassName );
        String strArtifactId = LuteceTestFileUtils.getProperty( "component.artifactId", repotype );
        String strScmDevelopperConnection = LuteceTestFileUtils.getProperty( "component.scmDeveloperConnection", repotype );
        String strUserLogin = LuteceTestFileUtils.getProperty( "component.releaseAccountLogin", repotype );
        String strUserPassword = LuteceTestFileUtils.getProperty( "component.releaseAccountPassword", repotype );
        String strReleaserVersion = LuteceTestFileUtils.getProperty( "component.releaseVersion", repotype );
        String strReleaserTagName = LuteceTestFileUtils.getProperty( "component.releaseTagName", repotype );
        String strReleaserNewDeveloppmentVersion = LuteceTestFileUtils.getProperty( "component.releaseNewDeveloppmentVersion", repotype );
        String strReleaserCurrentVersion = LuteceTestFileUtils.getProperty( "component.currentVersion", repotype );

        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        Component component = new Component( );
        component.setArtifactId( strArtifactId );
        component.setScmDeveloperConnection( strScmDevelopperConnection );
        component.setNextSnapshotVersion( strReleaserNewDeveloppmentVersion );
        component.setTargetVersion( strReleaserVersion );
        component.setLastAvailableSnapshotVersion( strReleaserCurrentVersion );
        component.setCurrentVersion( strReleaserCurrentVersion );

        if ( repotype.equals( RepositoryType.SVN ) )
        {
            component.setName( strArtifactId );
        }
        else
        {

            component.setName( ReleaserUtils.getComponentName( strScmDevelopperConnection, strArtifactId ) );
        }

        ReleaserUser user = new ReleaserUser( );

        user.addCredential( repotype, user.new Credential( strUserLogin, strUserPassword ) );

        context.setReleaserUser( user );
        context.setComponent( component );

        String strArtifactIdSite = LuteceTestFileUtils.getProperty( "site.artifactId", repotype );
        String strSiteName = LuteceTestFileUtils.getProperty( "site.name", repotype );

        String strScmDevelopperConnectionSite = LuteceTestFileUtils.getProperty( "site.scmDeveloperConnection", repotype );
        String strSiteLastReleaseVersion = LuteceTestFileUtils.getProperty( "site.lastReleaseVersion", repotype );
        String strSiteNextReleaseVersion = LuteceTestFileUtils.getProperty( "site.nextReleaseVersion", repotype );
        String strSiteNextSnapshotVersionVersion = LuteceTestFileUtils.getProperty( "site.nextSnapshotVersion", repotype );
        String strSiteCurrentVersion = LuteceTestFileUtils.getProperty( "site.currentVersion", repotype );
        String bolleanTheme = LuteceTestFileUtils.getProperty( "site.isTheme", repotype );
        String strSiteReleaseDescription = LuteceTestFileUtils.getProperty( "site.description", repotype );
        if ( bSite )
        {

            Site site = new Site( );
            site.setArtifactId( strArtifactIdSite );
            site.setLastReleaseVersion( strSiteLastReleaseVersion );
            site.setNextReleaseVersion( strSiteNextReleaseVersion );
            site.setNextSnapshotVersion( strSiteNextSnapshotVersionVersion );
            site.setTheme( new Boolean( bolleanTheme ) );
            site.setScmUrl( strScmDevelopperConnectionSite );
            site.setVersion( strSiteCurrentVersion );
            site.setName( strSiteName );
            site.setTagInformation( strSiteReleaseDescription );

            context.setSite( site );
        }
        return context;

    }

}
