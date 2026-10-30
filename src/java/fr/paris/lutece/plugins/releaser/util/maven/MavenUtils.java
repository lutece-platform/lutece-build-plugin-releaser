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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;

/**
 * 
 * MavenUtils
 *
 */
public final class MavenUtils
{
    private static final String TOKEN_RELEASE_VERSION = "$1";
    private static final String TOKEN_TAG = "$2";
    private static final String TOKEN_DEVELOPMENT_VERSION = "$3";
    private static final String TOKEN_USERNAME = "$4";
    private static final String TOKEN_PASSWORD = "$5";
    private static final String RELEASE_PREPARE_BASE = "release:prepare";
    private static final String RELEASE_PREPARE_ARGS = "-DignoreSnapshots=true" + " -DreleaseVersion=" + TOKEN_RELEASE_VERSION + " -Dtag=" + TOKEN_TAG
            + " -DdevelopmentVersion=" + TOKEN_DEVELOPMENT_VERSION + " -DforkMode=never" + " -Dusername=" + TOKEN_USERNAME + " -Dpassword=" + TOKEN_PASSWORD
            + " -Darguments=\"-Dmaven.test.skip=true\" --batch-mode";

    /** Name of the private Nexus repositories (suffix of their properties). */
    public static final String REPOSITORY_PRIVATE = "private";

    /** Name of the public Nexus repositories (suffix of their properties). */
    public static final String REPOSITORY_PUBLIC = "public";

    private MavenUtils( )
    {
        // nothing
    }

    /**
     * Builds maven arguments for release:prepare (with release:prepare)
     * 
     * @param strReleaseVersion
     *            release version
     * @param strTag
     *            tag
     * @param strDevelopmentVersion
     *            development version
     * @param strUsername
     *            strUsername
     * @param strPassword
     *            strPassword
     * @return the list
     */
    public static List<String> createReleasePrepare( String strReleaseVersion, String strTag, String strDevelopmentVersion, String strUsername,
            String strPassword )
    {
        String strArguments = RELEASE_PREPARE_ARGS.replace( TOKEN_DEVELOPMENT_VERSION, strDevelopmentVersion );
        strArguments = strArguments.replace( TOKEN_RELEASE_VERSION, strReleaseVersion );
        strArguments = strArguments.replace( TOKEN_TAG, strTag );
        strArguments = strArguments.replace( TOKEN_USERNAME, strUsername );
        strArguments = strArguments.replace( TOKEN_PASSWORD, strPassword );

        List<String> listPrepare = new ArrayList<String>( 2 );
        listPrepare.add( RELEASE_PREPARE_BASE );
        listPrepare.add( strArguments );

        return listPrepare;
    }

    /**
     * Returns the name of the only Nexus repository holding a component.
     *
     * @param mapUrls
     *            the URLs of the component by repository name
     * @return the repository name, null when no repository or several ones hold the component
     */
    public static String getSingleRepository( Map<String, String> mapUrls )
    {
        return mapUrls.size( ) == 1 ? mapUrls.keySet( ).iterator( ).next( ) : null;
    }

    /**
     * Builds the blocking message of a component found in several Nexus repositories : the first and last versions of each repository.
     *
     * @param strKind
     *            the kind of repositories (releases or snapshots)
     * @param mapVersions
     *            the sorted versions of the component by repository name
     * @return the message
     */
    public static String getSeveralRepositoriesMessage( String strKind, Map<String, List<String>> mapVersions )
    {
        List<String> listRepositories = new ArrayList<>( );
        for ( Map.Entry<String, List<String>> entry : mapVersions.entrySet( ) )
        {
            List<String> listVersions = entry.getValue( );
            String strVersions;
            if ( listVersions == null || listVersions.isEmpty( ) )
            {
                strVersions = "aucune version";
            }
            else
                if ( listVersions.size( ) == 1 )
                {
                    strVersions = listVersions.get( 0 );
                }
                else
                {
                    strVersions = listVersions.get( 0 ) + " … " + listVersions.get( listVersions.size( ) - 1 );
                }
            listRepositories.add( entry.getKey( ) + " : " + strVersions );
        }

        return "Composant présent dans plusieurs dépôts Nexus de " + strKind + " (" + String.join( " ; ", listRepositories )
                + ") : à corriger dans Nexus avant toute release.";
    }

    /**
     * Builds the informative message of a component whose releases are not in the Nexus repository of its platform : private for GitLab, public
     * for GitHub.
     *
     * @param strReleaseRepository
     *            the only repository holding the releases, null when none or several
     * @param repositoryType
     *            the platform of the component
     * @return the message, null when the releases are in the expected repository or cannot be checked
     */
    public static String getMisplacedReleasesMessage( String strReleaseRepository, RepositoryType repositoryType )
    {
        if ( strReleaseRepository == null || repositoryType == null )
        {
            return null;
        }

        String strExpected = repositoryType == RepositoryType.GITLAB ? REPOSITORY_PRIVATE : REPOSITORY_PUBLIC;
        if ( strExpected.equals( strReleaseRepository ) )
        {
            return null;
        }

        return "Les releases du composant sont dans le dépôt Nexus " + strReleaseRepository + " alors qu'un composant " + repositoryType
                + " les publie dans le dépôt " + strExpected + ".";
    }

    /**
     * Builds the blocking message of a GitLab component when no private Nexus repository is configured : it would be released to the public one.
     *
     * @param repositoryType
     *            the platform of the component
     * @param strPrivateRepository
     *            the value of releaser.mavenPrivateReleaseDeploymentRepository
     * @return the message, null when the component can be released
     */
    public static String getPrivateRepositoryMissingMessage( RepositoryType repositoryType, String strPrivateRepository )
    {
        if ( repositoryType != RepositoryType.GITLAB || StringUtils.isNotBlank( strPrivateRepository ) )
        {
            return null;
        }

        return "Dépôt Nexus privé non configuré (" + ConstanteUtils.PROPERTY_MAVEN_PRIVATE_RELEASE_DEPLOYMENT_REPOSITORY
                + ") : release impossible d'un composant GitLab.";
    }

}
