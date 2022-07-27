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

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
// TODO: Auto-generated Javadoc

/**
 * IComponentService .
 */
public interface IComponentService
{

    /**
     * Sets the remote informations.
     *
     * @param component
     *            the component
     * @param bCache
     *            the b cache
     * @throws HttpAccessException
     *             the http access exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void setRemoteInformations( Component component, boolean bCache ) throws HttpAccessException, IOException;

    /**
     * Update remote informations.
     *
     * @param component
     *            the component
     */
    void updateRemoteInformations( Component component );

    /**
     * Release.
     *
     * @param component
     *            the component
     * @param locale
     *            the locale
     * @param user
     *            the user
     * @param request
     *            the request
     * @param forceRelease
     *            the force release
     * @return context id
     */
    int release( Component component, Locale locale, AdminUser user, HttpServletRequest request, boolean forceRelease );

    /**
     * Release.
     *
     * @param component
     *            the component
     * @param locale
     *            the locale
     * @param user
     *            the user
     * @param request
     *            the request
     * @return context id
     */
    int release( Component component, Locale locale, AdminUser user, HttpServletRequest request );

    /**
     * Checks if is git component.
     *
     * @param component
     *            the component
     * @return boolean
     */
    boolean isGitComponent( Component component );

    /**
     * Returns the LastAvailableVersion.
     *
     * @param strArtifactId
     *            the str artifact id
     * @return The LastAvailableVersion
     */

    String getLastReleaseVersion( String strArtifactId );

    /**
     * set the LastAvailableVersion
     * 
     * set The LastAvailableVersion.
     *
     * @param strArtifactId
     *            the str artifact id
     * @param strVersion
     *            the str version
     */

    void setLastReleaseVersion( String strArtifactId, String strVersion );

    /**
     * Returns the LastAvailableVersion.
     *
     * @param strArtifactId
     *            the str artifact id
     * @return The LastAvailableVersion
     */
    String getLastReleaseNextSnapshotVersion( String strArtifactId );

    /**
     * setLastReleaseNextSnapshotVersion.
     *
     * @param strArtifactId
     *            the str artifact id
     * @param strVersion
     *            the str version
     */
    void setLastReleaseNextSnapshotVersion( String strArtifactId, String strVersion );

    /**
     * Gets the search component.
     *
     * @param strSearch
     *            the str search
     * @param request
     *            the request
     * @param locale
     *            the locale
     * @param strPaginateUrl
     *            the str paginate url
     * @param strCurrentPageIndex
     *            the str current page index
     * @return local paginator
     */
    LocalizedPaginator<Component> getSearchComponent( String strSearch, HttpServletRequest request, Locale locale, String strPaginateUrl,
            String strCurrentPageIndex );

    /**
     * Load component.
     *
     * @param component
     *            the component
     * @param strPom
     *            the str pom
     * @param stUser
     *            the st user
     * @param strPassword
     *            the str password
     * @return component
     */
    Component loadComponent( Component component, String strPom, String stUser, String strPassword );

    /**
     * Change next release version.
     *
     * @param component
     *            component
     */
    void changeNextReleaseVersion( Component component );

    /**
     * Checks if is error snapshot component informations.
     *
     * @param component
     *            the component
     * @param strComponentPomPath
     *            the str component pom path
     * @return boolean
     */
    boolean isErrorSnapshotComponentInformations( Component component, String strComponentPomPath );

    /**
     * Inits the.
     */
    void init( );

    /**
     * Set list of all component branches
     * 
     * @param site
     *            The site
     * @param artifactId
     *            The component artifactId
     * @param user
     *            The user
     * @return
     */
    Component getComponentBranchList( Site site, String artifactId, ReleaserUser user );

    Component getLastBranchVersion( Component component, String branchName, ReleaserUser user );
}
