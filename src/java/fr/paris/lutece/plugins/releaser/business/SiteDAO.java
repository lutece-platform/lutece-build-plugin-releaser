/*
 * Copyright (c) 2002-2016, Mairie de Paris
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
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Data Access methods for Site objects
 */
public final class SiteDAO implements ISiteDAO
{
    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_site ) FROM releaser_site";
    private static final String SQL_QUERY_SELECT = "SELECT id_site, artifact_id, id_cluster, scm_url, name, description, jira_key FROM releaser_site WHERE id_site = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO releaser_site ( id_site, artifact_id, id_cluster, scm_url, name, description, jira_key ) VALUES ( ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM releaser_site WHERE id_site = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE releaser_site SET id_site = ?, artifact_id = ?, id_cluster = ?, scm_url = ?, name = ?, description = ?, jira_key = ? WHERE id_site = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_site, artifact_id, id_cluster, scm_url, name, description, jira_key FROM releaser_site";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_site FROM releaser_site";

    /**
     * Generates a new primary key
     * 
     * @param plugin
     *            The Plugin
     * @return The new primary key
     */
    public int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin );
        daoUtil.executeQuery( );
        int nKey = 1;

        if ( daoUtil.next( ) )
        {
            nKey = daoUtil.getInt( 1 ) + 1;
        }

        daoUtil.free( );
        return nKey;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( Site site, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        site.setId( newPrimaryKey( plugin ) );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, site.getId( ) );
        daoUtil.setString( nIndex++, site.getArtifactId( ) );
        daoUtil.setInt( nIndex++, site.getIdCluster( ) );
        daoUtil.setString( nIndex++, site.getScmUrl( ) );
        daoUtil.setString( nIndex++, site.getName( ) );
        daoUtil.setString( nIndex++, site.getDescription( ) );
        daoUtil.setString( nIndex++, site.getJiraKey( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Site load( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1, nKey );
        daoUtil.executeQuery( );
        Site site = null;

        if ( daoUtil.next( ) )
        {
            site = new Site( );
            int nIndex = 1;

            site.setId( daoUtil.getInt( nIndex++ ) );
            site.setArtifactId( daoUtil.getString( nIndex++ ) );
            site.setIdCluster( daoUtil.getInt( nIndex++ ) );
            site.setScmUrl( daoUtil.getString( nIndex++ ) );
            site.setName( daoUtil.getString( nIndex++ ) );
            site.setDescription( daoUtil.getString( nIndex++ ) );
            site.setJiraKey( daoUtil.getString( nIndex++ ) );
        }

        daoUtil.free( );
        return site;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nKey );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( Site site, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, site.getId( ) );
        daoUtil.setString( nIndex++, site.getArtifactId( ) );
        daoUtil.setInt( nIndex++, site.getIdCluster( ) );
        daoUtil.setString( nIndex++, site.getScmUrl( ) );
        daoUtil.setString( nIndex++, site.getName( ) );
        daoUtil.setString( nIndex++, site.getDescription( ) );
        daoUtil.setString( nIndex++, site.getJiraKey( ) );
        daoUtil.setInt( nIndex, site.getId( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Site> selectSitesList( Plugin plugin )
    {
        List<Site> siteList = new ArrayList<Site>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            Site site = new Site( );
            int nIndex = 1;

            site.setId( daoUtil.getInt( nIndex++ ) );
            site.setArtifactId( daoUtil.getString( nIndex++ ) );
            site.setIdCluster( daoUtil.getInt( nIndex++ ) );
            site.setScmUrl( daoUtil.getString( nIndex++ ) );
            site.setName( daoUtil.getString( nIndex++ ) );
            site.setDescription( daoUtil.getString( nIndex++ ) );
            site.setJiraKey( daoUtil.getString( nIndex++ ) );

            siteList.add( site );
        }

        daoUtil.free( );
        return siteList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdSitesList( Plugin plugin )
    {
        List<Integer> siteList = new ArrayList<Integer>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            siteList.add( daoUtil.getInt( 1 ) );
        }

        daoUtil.free( );
        return siteList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectSitesReferenceList( Plugin plugin )
    {
        ReferenceList siteList = new ReferenceList( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            siteList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
        }

        daoUtil.free( );
        return siteList;
    }
}
