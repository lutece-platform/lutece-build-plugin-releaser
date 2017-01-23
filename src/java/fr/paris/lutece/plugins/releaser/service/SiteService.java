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

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.SiteHome;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.signrequest.BasicAuthorizationAuthenticator;
import fr.paris.lutece.util.signrequest.RequestAuthenticator;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SiteService
 */
public class SiteService
{
    private static final String PROPERTY_SITE_REPOSITORY_LOGIN = "releaser.site.repository.login";
    private static final String PROPERTY_SITE_REPOSITORY_PASSWORD = "releaser.site.repository.password";

    public static Site getSite( int nSiteId )
    {
        Site site = SiteHome.findByPrimaryKey( nSiteId );
        String strPom = fetchPom( site.getScmUrl( ) + "/pom.xml" );
        if ( strPom != null )
        {
            PomParser parser = new PomParser( );
            parser.parse( site, strPom );
            initComponents( site );
        }
        return site;
    }

    private static String fetchPom( String strPomUrl )
    {
        try
        {
            RequestAuthenticator authenticator = getSiteAuthenticator( );
            HttpAccess httpAccess = new HttpAccess( );
            String strPom = httpAccess.doGet( strPomUrl, authenticator, null );
            return strPom;
        }
        catch( HttpAccessException ex )
        {
            Logger.getLogger( SiteService.class.getName( ) ).log( Level.SEVERE, null, ex );
        }
        return null;
    }

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
            checkForNewVersion( component );
            buildReleaseComments( component );
            site.addComponent( component );
        }
    }

    private static void defineTargetVersion( Component component )
    { // FIXME
        String strTargetVersion = component.getCurrentVersion( );

        int nPos = strTargetVersion.indexOf( "-SNAPSHOT" );
        if ( nPos > 0 && component.isProject( ) )
        {
            strTargetVersion = strTargetVersion.substring( 0, nPos );
        }

        component.setTargetVersion( strTargetVersion );
    }

    private static boolean isProjectComponent( Site site, String strArtifactId )
    {
        // FIXME
        return ( strArtifactId.contains( "gru" ) || strArtifactId.contains( "ticketing" ) || strArtifactId.contains( "identity" ) );
    }

    private static void buildReleaseComments( Component component )
    {
        if ( !component.isProject( ) && isSnapshot( component.getTargetVersion( ) ) )
        {
            component.addReleaseComment( "Ne pas utiliser des snapshots pour les projets externes." );
        }

        if ( component.getLastAvailableVersion( ) != null )
        {
            component.addReleaseComment( "Une version " + component.getLastAvailableVersion( ) + " plus récente est disponible" );
        }

        if ( component.isProject( ) && isSnapshot( component.getCurrentVersion( ) ) )
        {
            component.addReleaseComment( "A releaser." );
        }
    }

    private static boolean isSnapshot( String strVersion )
    {
        return strVersion.contains( "SNAPSHOT" );
    }

    private static void checkForNewVersion( Component component )
    {
        if ( !component.isProject( ) )
        {
            try
            {
                String strLastestVersion = ComponentService.getLatestVersion( component.getArtifactId( ) );
                System.out.println( component.getArtifactId( ) + " currentversion:" + component.getTargetVersion( ) + " latestvesion:" + strLastestVersion );

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

    public String generateTargetPOM( Site site )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); // To change body of generated methods, choose Tools | Templates.
    }

    private static RequestAuthenticator getSiteAuthenticator( )
    {
        String strLogin = AppPropertiesService.getProperty( PROPERTY_SITE_REPOSITORY_LOGIN );
        String strPassword = AppPropertiesService.getProperty( PROPERTY_SITE_REPOSITORY_PASSWORD );

        return new BasicAuthorizationAuthenticator( strLogin, strPassword );
    }

}
