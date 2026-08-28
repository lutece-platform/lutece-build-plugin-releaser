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

import static junit.framework.TestCase.*;
import org.junit.Test;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;

/**
 * RedmineComponentServiceTest : URL normalization (projects index matching), repository type derivation and bugtracker scope guard.
 */
public class RedmineComponentServiceTest
{
    /**
     * Test of cleanRepoUrl : scm:git: prefix, .git suffix and trailing slash are stripped, case is preserved.
     */
    @Test
    public void testCleanRepoUrl( )
    {
        assertEquals( "https://github.com/lutece-platform/lutece-core",
                RedmineComponentService.cleanRepoUrl( "scm:git:https://github.com/lutece-platform/lutece-core.git" ) );

        assertEquals( "https://github.com/lutece-platform/lutece-core",
                RedmineComponentService.cleanRepoUrl( "https://github.com/lutece-platform/lutece-core/" ) );

        assertEquals( "https://github.com/lutece-platform/lutece-core",
                RedmineComponentService.cleanRepoUrl( "scm:git:https://github.com/lutece-platform/lutece-core.git/" ) );

        assertEquals( "https://dev.lutece.paris.fr/gitlab/bild/u06/site-releaser",
                RedmineComponentService.cleanRepoUrl( "scm:git:https://dev.lutece.paris.fr/gitlab/bild/u06/site-releaser.git" ) );

        // Case preserved (normalizeRepoUrl handles the lowercase)
        assertEquals( "https://GitHub.com/Lutece-Platform/Plugin-X",
                RedmineComponentService.cleanRepoUrl( "https://GitHub.com/Lutece-Platform/Plugin-X" ) );

        // Surrounding whitespace stripped
        assertEquals( "https://github.com/lutece-platform/lutece-core",
                RedmineComponentService.cleanRepoUrl( "  https://github.com/lutece-platform/lutece-core  " ) );

        assertNull( RedmineComponentService.cleanRepoUrl( null ) );
        assertNull( RedmineComponentService.cleanRepoUrl( "" ) );
        assertNull( RedmineComponentService.cleanRepoUrl( "   " ) );
    }

    /**
     * Test of normalizeRepoUrl : same cleaning plus lowercase, so index lookups are case-insensitive.
     */
    @Test
    public void testNormalizeRepoUrl( )
    {
        assertEquals( "https://github.com/lutece-platform/plugin-workflow",
                RedmineComponentService.normalizeRepoUrl( "scm:git:https://GitHub.com/Lutece-Platform/Plugin-Workflow.git" ) );

        // A POM scm URL and a Redmine "Sources" URL differing only by prefix/suffix/case must match
        assertEquals( RedmineComponentService.normalizeRepoUrl( "scm:git:https://github.com/lutece-platform/lutece-core.git" ),
                RedmineComponentService.normalizeRepoUrl( "https://github.com/lutece-platform/lutece-core/" ) );

        assertNull( RedmineComponentService.normalizeRepoUrl( null ) );
        assertNull( RedmineComponentService.normalizeRepoUrl( "  " ) );
    }

    /**
     * Test of Component.getRepoType : the repository type is derived from the scm developer connection.
     */
    @Test
    public void testGetRepoType( )
    {
        assertEquals( RepositoryType.GITHUB, componentWithScm( "scm:git:https://github.com/lutece-platform/plugin-workflow.git" ).getRepoType( ) );

        assertEquals( RepositoryType.GITLAB, componentWithScm( "scm:git:https://dev.lutece.paris.fr/gitlab/bild/u06/site-releaser.git" ).getRepoType( ) );

        // Unsupported repository (legacy SVN) : no repo type.
        assertNull( componentWithScm( "scm:svn:https://dev.lutece.paris.fr/svn/lutece/portal/trunk" ).getRepoType( ) );

        assertNull( componentWithScm( null ).getRepoType( ) );
        assertNull( componentWithScm( "" ).getRepoType( ) );
    }

    /**
     * Test of the prepareReleaseInBugtracker scope guard : unsupported-repo or scm-less components are out of the
     * bugtracker scope and must be ignored without any Redmine access (the service is deliberately NOT initialized here).
     */
    @Test
    public void testPrepareReleaseInBugtrackerOutOfScope( )
    {
        RedmineComponentService service = new RedmineComponentService( );

        Component componentSvn = componentWithScm( "scm:svn:https://dev.lutece.paris.fr/svn/lutece/portal/trunk" );
        assertNull( service.prepareReleaseInBugtracker( componentSvn, false, false, false, null, false ) );

        Component componentNoScm = componentWithScm( null );
        assertNull( service.prepareReleaseInBugtracker( componentNoScm, false, false, false, null, false ) );
    }

    private Component componentWithScm( String strScmDeveloperConnection )
    {
        Component component = new Component( );
        component.setArtifactId( "plugin-test" );
        component.setScmDeveloperConnection( strScmDeveloperConnection );

        return component;
    }
}
