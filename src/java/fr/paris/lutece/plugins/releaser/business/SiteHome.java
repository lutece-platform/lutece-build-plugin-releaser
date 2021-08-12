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
package fr.paris.lutece.plugins.releaser.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for Site objects
 */
public final class SiteHome
{
    // Static variable pointed at the DAO instance
    private static ISiteDAO _dao = SpringContextService.getBean( "releaser.siteDAO" );
    private static Plugin _plugin = PluginService.getPlugin( "releaser" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private SiteHome( )
    {
    }

    /**
     * Create an instance of the site class
     * 
     * @param site
     *            The instance of the Site which contains the informations to store
     * @return The instance of site which has been created with its primary key.
     */
    public static Site create( Site site )
    {
        _dao.insert( site, _plugin );

        return site;
    }

    /**
     * Update of the site which is specified in parameter
     * 
     * @param site
     *            The instance of the Site which contains the data to store
     * @return The instance of the site which has been updated
     */
    public static Site update( Site site )
    {
        _dao.store( site, _plugin );

        return site;
    }

    /**
     * Remove the site whose identifier is specified in parameter
     * 
     * @param nKey
     *            The site Id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a site whose identifier is specified in parameter
     * 
     * @param nKey
     *            The site primary key
     * @return an instance of Site
     */
    public static Site findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Load the data of all the site objects and returns them as a list
     * 
     * @return the list which contains the data of all the site objects
     */
    public static List<Site> getSitesList( )
    {
        return _dao.selectSitesList( _plugin );
    }

    /**
     * Load the id of all the site objects and returns them as a list
     * 
     * @return the list which contains the id of all the site objects
     */
    public static List<Integer> getIdSitesList( )
    {
        return _dao.selectIdSitesList( _plugin );
    }

    /**
     * Load the data of all the site objects and returns them as a referenceList
     * 
     * @return the referenceList which contains the data of all the site objects
     */
    public static ReferenceList getSitesReferenceList( )
    {
        return _dao.selectSitesReferenceList( _plugin );
    }

    /**
     * Find all sites for a given cluster
     * 
     * @param nClusterId
     *            The cluster ID
     * @return the list which contains the data of all the site objects
     */
    public static List<Site> findByCluster( int nClusterId )
    {
        return _dao.selectByCluster( nClusterId, _plugin );
    }
    
    /**
     * Search if the site is already exist
     * 
     * @param siteName
     *            The site name
     *            
     * @param artifactId
     *            The site artifacteId
     *            
     * @param scmUrl
     *            The site sources url
     *            
     * @return the the cluster name if site exists or null otherwise
     */
    public static String findDuplicateSite( String siteName, String artifactId, String scmUrl )
    {
        return _dao.searchDuplicateSite( siteName, artifactId, scmUrl, _plugin );
    }
    

}
