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

import fr.paris.lutece.plugins.releaser.util.pom.PomParser;
import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.SiteHome;
import fr.paris.lutece.plugins.releaser.util.pom.PomFetcher;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import fr.paris.lutece.plugins.releaser.util.version.VersionParsingException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import java.io.IOException;
import java.util.Locale;

/**
 * SiteService
 */
public class SiteService
{
    private static final String MESSAGE_AVOID_SNAPSHOT = "releaser.message.avoidSnapshot";
    private static final String MESSAGE_UPGRADE_SELECTED = "releaser.message.upgradeSelected";
    private static final String MESSAGE_TO_BE_RELEASED = "releaser.message.toBeReleased";
    private static final String MESSAGE_MORE_RECENT_VERSION_AVAILABLE = "releaser.message.moreRecentVersionAvailable";

    private static final String NOT_AVAILABLE = "Not available";

    /**
     * Load a site from its id
     * 
     * @param nSiteId
     *            The site id
     * @return A site object
     */
    public static Site getSite( int nSiteId )
    {
        Site site = SiteHome.findByPrimaryKey( nSiteId );
        String strPom = PomFetcher.fetchPom( site.getScmUrl( ) + "/pom.xml" );
        if ( strPom != null )
        {
            PomParser parser = new PomParser( );
            parser.parse( site, strPom );
            initComponents( site );
        }
        return site;
    }

    /**
     * Initialize the component list for a given site
     * 
     * @param site
     *            The site
     */
    private static void initComponents( Site site )
    {
        for ( Dependency dependency : site.getCurrentDependencies( ) )
        {
            Component component = new Component( );

            component.setIsProject( isProjectComponent( site, dependency.getArtifactId( ) ) );
            component.setArtifactId( dependency.getArtifactId( ) );
            component.setGroupId( dependency.getGroupId( ) );
            component.setCurrentVersion( dependency.getVersion( ) );
            defineTargetVersion( component );
            defineNextSnapshotVersion( component );
            checkForNewVersion( component );
            site.addComponent( component );
        }
    }

    /**
     * Define the target version for a given component : <br>
     * - current version for non project component <br>
     * - nex release for project component
     * 
     * @param component
     *            The component
     */
    private static void defineTargetVersion( Component component )
    {
        if ( component.isProject( ) )
        {
            String strTargetVersion = NOT_AVAILABLE;
            try
            {
                strTargetVersion = Version.parse( component.getCurrentVersion( ) ).nextRelease( ).getVersion( );
            }
            catch( VersionParsingException ex )
            {
                AppLogService.error( "Error parsing version for component " + component.getArtifactId( ) + " : " + ex.getMessage( ), ex );
            }
            component.setTargetVersion( strTargetVersion );
        }
        else
        {
            component.setTargetVersion( component.getCurrentVersion( ) );
        }
    }

    /**
     * Define the next snapshot version for a given component
     * 
     * @param component
     *            The component
     */
    private static void defineNextSnapshotVersion( Component component )
    {
        String strNextSnapshotVersion = NOT_AVAILABLE;
        try
        {
            Version version = Version.parse( component.getTargetVersion( ) );
            boolean bSnapshot = true;
            strNextSnapshotVersion = version.nextPatch( bSnapshot ).toString( );
        }
        catch( VersionParsingException ex )
        {
            AppLogService.error( "Error parsing version for component " + component.getArtifactId( ) + " : " + ex.getMessage( ), ex );

        }
        component.setNextSnapshotVersion( strNextSnapshotVersion );

    }

    private static boolean isProjectComponent( Site site, String strArtifactId )
    {
        // FIXME
        return ( strArtifactId.contains( "gru" ) || strArtifactId.contains( "ticketing" ) || strArtifactId.contains( "identity" ) );
    }

    /**
     * Build release comments for a given site
     * 
     * @param site
     *            The site
     * @param locale
     *            The locale to use for comments
     */
    public static void buildComments( Site site, Locale locale )
    {
        for ( Component component : site.getComponents( ) )
        {
            component.resetComments( );
            buildReleaseComments( component, locale );
        }
    }

    /**
     * Build release comments for a given component
     * 
     * @param component
     *            The component
     * @param locale
     *            The locale to use for comments
     */
    private static void buildReleaseComments( Component component, Locale locale )
    {
        if ( !component.isProject( ) )
        {
            if ( Version.isSnapshot( component.getTargetVersion( ) ) )
            {
                String strComment = I18nService.getLocalizedString( MESSAGE_AVOID_SNAPSHOT, locale );
                component.addReleaseComment( strComment );
            }
            if ( !component.getTargetVersion( ).equals( component.getCurrentVersion( ) ) )
            {
                String strComment = I18nService.getLocalizedString( MESSAGE_UPGRADE_SELECTED, locale );
                component.addReleaseComment( strComment );
            }
        }

        if ( component.getLastAvailableVersion( ) != null && !component.getLastAvailableVersion( ).equals( component.getTargetVersion( ) ) )
        {
            String [ ] arguments = {
                component.getLastAvailableVersion( )
            };
            String strComment = I18nService.getLocalizedString( MESSAGE_MORE_RECENT_VERSION_AVAILABLE, arguments, locale );
            component.addReleaseComment( strComment );
        }

        if ( component.isProject( ) && Version.isSnapshot( component.getCurrentVersion( ) ) )
        {
            String strComment = I18nService.getLocalizedString( MESSAGE_TO_BE_RELEASED, locale );
            component.addReleaseComment( strComment );
        }
    }

    private static void checkForNewVersion( Component component )
    {
        if ( !component.isProject( ) )
        {
            try
            {
                String strLastestVersion = ComponentService.getLatestVersion( component.getArtifactId( ) );

                if ( !strLastestVersion.equals( component.getTargetVersion( ) ) )
                {
                    component.setLastAvailableVersion( strLastestVersion );
                }
            }
            catch( HttpAccessException | IOException ex )
            {
                AppLogService.error(
                        "Releaser unable to get the latest version for component : " + component.getArtifactId( ) + " error : " + ex.getMessage( ), ex );
            }
        }
    }

    public static void upgradeComponent( Site site, String strArtifactId )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) )
            {
                component.setTargetVersion( component.getLastAvailableVersion( ) );
            }
        }
    }

    /**
     * Add or Remove a component from the project's components list
     * 
     * @param site
     *            The site
     * @param strArtifactId
     *            The component artifact id
     */
    public static void toggleProjectComponent( Site site, String strArtifactId )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) )
            {
                component.setIsProject( !component.isProject( ) );
            }
        }
    }

    /**
     * Generate the pom.xml file for a given site
     * 
     * @param site
     *            The site
     * @return The pom.xml content
     */
    public String generateTargetPOM( Site site )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); // To change body of generated methods, choose Tools | Templates.
    }

}
