/*
 * Copyright (c) 2002-2025, City of Paris
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
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

import fr.paris.lutece.portal.service.util.AppException;

/**
 * Tests of the scm check run before release:prepare
 */
public class PomScmCheckTest
{
    private static final String ORIGIN = "https://github.com/lutece-platform/lutece-core.git";

    /**
     * The check receives the pom.xml itself and accepts a developerConnection naming the cloned repository
     *
     * @throws IOException
     *             on temp file error
     */
    @Test
    public void testAcceptsPomNamingOrigin( ) throws IOException
    {
        File filePom = writePom( "scm:git:https://github.com/lutece-platform/lutece-core.git", "scm:git:https://github.com/lutece-platform/lutece-core.git" );
        WorkflowReleaseContextService.checkPomScmBeforePrepare( filePom.getPath( ), ORIGIN );
    }

    /**
     * Only the developerConnection is pushed by maven-release : a wrong connection is tolerated
     *
     * @throws IOException
     *             on temp file error
     */
    @Test
    public void testIgnoresConnectionWhenDeveloperConnectionIsRight( ) throws IOException
    {
        File filePom = writePom( "scm:git:https://github.com/lutece-platform/lutece-cor.git", "scm:git:https://github.com/lutece-platform/lutece-core.git" );
        WorkflowReleaseContextService.checkPomScmBeforePrepare( filePom.getPath( ), ORIGIN );
    }

    /**
     * A developerConnection pointing elsewhere, even on an allowed host, is refused
     *
     * @throws IOException
     *             on temp file error
     */
    @Test
    public void testRefusesOtherRepository( ) throws IOException
    {
        File filePom = writePom( "scm:git:https://github.com/lutece-platform/lutece-core.git", "scm:git:https://github.com/lutece-platform/other.git" );
        try
        {
            WorkflowReleaseContextService.checkPomScmBeforePrepare( filePom.getPath( ), ORIGIN );
            fail( "expected refusal" );
        }
        catch( AppException e )
        {
            assertTrue( e.getMessage( ).contains( "refused" ) );
        }
    }

    /**
     * A missing pom.xml is refused, not silently accepted
     */
    @Test
    public void testRefusesMissingPom( )
    {
        try
        {
            WorkflowReleaseContextService.checkPomScmBeforePrepare( new File( "/nonexistent/pom.xml" ).getPath( ), ORIGIN );
            fail( "expected refusal" );
        }
        catch( AppException e )
        {
            assertTrue( e.getMessage( ).contains( "Unreadable" ) );
        }
    }

    private static File writePom( String strConnection, String strDeveloperConnection ) throws IOException
    {
        File filePom = File.createTempFile( "pom", ".xml" );
        filePom.deleteOnExit( );
        String strPom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"><modelVersion>4.0.0</modelVersion><artifactId>lutece-core</artifactId>"
                + "<version>8.0.0-SNAPSHOT</version><scm><connection>" + strConnection + "</connection><developerConnection>" + strDeveloperConnection
                + "</developerConnection></scm></project>";
        Files.write( filePom.toPath( ), strPom.getBytes( StandardCharsets.UTF_8 ) );

        return filePom;
    }
}
