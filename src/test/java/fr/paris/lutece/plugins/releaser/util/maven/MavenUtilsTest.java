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
package fr.paris.lutece.plugins.releaser.util.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.paris.lutece.plugins.releaser.business.RepositoryType;

/**
 * Tests of the reading of the Nexus repositories holding a component.
 */
public class MavenUtilsTest
{
    /**
     * The only repository is returned, none when the component is in no repository or in several ones.
     */
    @Test
    public void testSingleRepository( )
    {
        Map<String, String> mapUrls = new LinkedHashMap<>( );
        assertNull( MavenUtils.getSingleRepository( mapUrls ) );

        mapUrls.put( MavenUtils.REPOSITORY_PUBLIC, "https://nexus/public/c" );
        assertEquals( MavenUtils.REPOSITORY_PUBLIC, MavenUtils.getSingleRepository( mapUrls ) );

        mapUrls.put( MavenUtils.REPOSITORY_PRIVATE, "https://nexus/private/c" );
        assertNull( MavenUtils.getSingleRepository( mapUrls ) );
    }

    /**
     * The blocking message gives the first and last versions of each repository.
     */
    @Test
    public void testSeveralRepositoriesMessage( )
    {
        Map<String, List<String>> mapVersions = new LinkedHashMap<>( );
        mapVersions.put( MavenUtils.REPOSITORY_PRIVATE, Collections.singletonList( "1.0.7" ) );
        mapVersions.put( MavenUtils.REPOSITORY_PUBLIC, Arrays.asList( "1.0.0", "1.0.8", "1.0.9-beta-01" ) );

        assertEquals( "Composant présent dans plusieurs dépôts Nexus de releases (private : 1.0.7 ; public : 1.0.0 … 1.0.9-beta-01) : "
                + "à corriger dans Nexus avant toute release.",
                MavenUtils.getSeveralRepositoriesMessage( "releases", mapVersions ) );
    }

    /**
     * Releases outside the repository of the platform are reported, the expected ones and the unknown cases are not.
     */
    @Test
    public void testMisplacedReleasesMessage( )
    {
        assertNull( MavenUtils.getMisplacedReleasesMessage( MavenUtils.REPOSITORY_PRIVATE, RepositoryType.GITLAB ) );
        assertNull( MavenUtils.getMisplacedReleasesMessage( MavenUtils.REPOSITORY_PUBLIC, RepositoryType.GITHUB ) );
        assertNull( MavenUtils.getMisplacedReleasesMessage( null, RepositoryType.GITLAB ) );
        assertNull( MavenUtils.getMisplacedReleasesMessage( MavenUtils.REPOSITORY_PUBLIC, null ) );

        assertEquals( "Les releases du composant sont dans le dépôt Nexus public alors qu'un composant GITLAB les publie dans le dépôt private.",
                MavenUtils.getMisplacedReleasesMessage( MavenUtils.REPOSITORY_PUBLIC, RepositoryType.GITLAB ) );
        assertEquals( "Les releases du composant sont dans le dépôt Nexus private alors qu'un composant GITHUB les publie dans le dépôt public.",
                MavenUtils.getMisplacedReleasesMessage( MavenUtils.REPOSITORY_PRIVATE, RepositoryType.GITHUB ) );
    }

    /**
     * A GitLab component is blocked without private repository, never with one, and a GitHub component never.
     */
    @Test
    public void testPrivateRepositoryMissingMessage( )
    {
        assertEquals( "Dépôt Nexus privé non configuré (releaser.mavenPrivateReleaseDeploymentRepository) : release impossible d'un composant GitLab.",
                MavenUtils.getPrivateRepositoryMissingMessage( RepositoryType.GITLAB, null ) );
        assertEquals( MavenUtils.getPrivateRepositoryMissingMessage( RepositoryType.GITLAB, null ),
                MavenUtils.getPrivateRepositoryMissingMessage( RepositoryType.GITLAB, " " ) );
        assertNull( MavenUtils.getPrivateRepositoryMissingMessage( RepositoryType.GITLAB, "https://nexus/private" ) );
        assertNull( MavenUtils.getPrivateRepositoryMissingMessage( RepositoryType.GITHUB, null ) );
        assertNull( MavenUtils.getPrivateRepositoryMissingMessage( null, null ) );
    }
}
