package fr.paris.lutece.plugins.releaser.service;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.business.Site;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/*
 * Copyright (c) 2002-2015, Mairie de Paris
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


/**
 * SiteService
 */
public class SiteService 
{
    
    public static Site getSite( int nSiteId )
    {
        // Site site = SiteHome.findByPrimaryKey( nSiteId );
        // String strPom = fetchPom( site.getScmUrl() + "/pom.xml" );
        // PomParser parser = new PomParser();
        // parser.parse( site, strPOM );
        //
        // return site;

        // FIXME
        Site site = getMockSite();
        initComponents( site );
        
        return site;
    }

    private static String fetchPom( String strPomUrl )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    private static void initComponents( Site site )
    {
        for( Dependency dependency : site.getCurrentDependencies() )
        {
            Component component = new Component();

            component.setIsProject( isProjectComponent( site , dependency.getArtifactId() ) );
            component.setArtifactId( dependency.getArtifactId() );
            component.setGroupId( dependency.getGroupId() );
            component.setCurrentVersion( dependency.getVersion() );
            defineTargetVersion( component );
            checkForNewVersion( component );
            buildReleaseComments( component );
            site.addComponent( component );
        }
    }

    private static void defineTargetVersion( Component component )
    {   // FIXME
        String strTargetVersion = component.getCurrentVersion();
        
        int nPos = strTargetVersion.indexOf( "-SNAPSHOT");
        if( nPos > 0 && component.isProject() )
        {
            strTargetVersion = strTargetVersion.substring( 0 , nPos);
        }
            
        component.setTargetVersion( strTargetVersion );
    }

    private static boolean isProjectComponent( Site site , String strArtifactId )
    {
        // FIXME
        return ( strArtifactId.contains( "gru" )  || strArtifactId.contains( "ticketing" ) || strArtifactId.contains( "identity" ) );
    }

    private static void buildReleaseComments( Component component )
    {
        if( ! component.isProject() && isSnapshot( component.getTargetVersion() ))
        {
            component.addReleaseComment( "Ne pas utiliser des snapshots pour les projets externes.");
        }
        
        if( component.getLastAvailableVersion() != null )
        {
            component.addReleaseComment( "Une version " + component.getLastAvailableVersion() + " plus récente est disponible");
        }

        if( component.isProject() && isSnapshot( component.getCurrentVersion() ))
        {
            component.addReleaseComment( "A releaser.");
        }
    }

    private static boolean isSnapshot( String strVersion )
    {
        return strVersion.contains( "SNAPSHOT" );
    }

    private static void checkForNewVersion( Component component )
    {
        if( ! component.isProject() )
        {
            String strLastestVersion = getLatestVersion( component );
            if( ! strLastestVersion.equals( component.getTargetVersion() ))
            {
                component.setLastAvailableVersion( strLastestVersion );
            }
        }
    }

    private static String getLatestVersion( Component component )
    {
        // FIXME
        if( component.getArtifactId().equals( "library-httpaccess"))
        {
            return "2.4.2";
        }
        if( component.getArtifactId().equals( "plugin-rest"))
        {
            return "3.1.1";
        }
        return component.getTargetVersion();
    }
    
    
    
    public String generateTargetPOM( Site site )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static Site getMockSite()
    {
        try
        {
            Site site = new Site();
            site.setName( "Ticketing");
            site.setDescription( "Site de gestion des sollicitations");
            String strPOM = loadFile( "/pom.xml" );
            PomParser parser = new PomParser();
            parser.parse( site, strPOM );
            return site;
        }
        catch( IOException ex )
        {
            Logger.getLogger( SiteService.class.getName() ).log( Level.SEVERE, null, ex );
            return null;
        }
    }
    
    private static String loadFile( String strFilePath ) throws IOException
    {
        return IOUtils.toString( SiteService.class.getResourceAsStream( strFilePath ),  "UTF-8" );
    }

}
