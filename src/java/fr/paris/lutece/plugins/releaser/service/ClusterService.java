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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.paris.lutece.plugins.releaser.business.Cluster;
import fr.paris.lutece.plugins.releaser.business.ClusterHome;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.SiteHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.rbac.RBACService;

/**
 * ClusterService.
 */
public class ClusterService
{

    /**
     * Load the list of sites into each cluster object and returns the list of clusters
     * 
     * @return the list which contains the data of all the cluster objects
     */
    public static List<Cluster> getClustersListWithSites( AdminUser adminUser )
    {
        List<Cluster> listCluster = ClusterHome.getClustersList( );

        for ( Cluster cluster : listCluster )
        {
            List<Site> listSite = SiteHome.findByCluster( cluster.getId( ) );
            for ( Site site : listSite )
            {
                cluster.getSites( ).add( site );
            }
        }

        return listCluster;
    }

    /**
     * Load the list of sites into each cluster object and returns the list of clusters
     * 
     * @return the list which contains the data of all the cluster objects
     */
    public static List<Cluster> getUserClusters( AdminUser adminUser )
    {
        List<Cluster> listCluster = ClusterHome.getClustersList( );
        List<Cluster> listAuthorizedClusters = new ArrayList<Cluster>( );

        for ( Cluster cluster : listCluster )
        {
            HashMap<String, Boolean> clusterPermissions = new HashMap<String, Boolean>( );
            boolean bAuthoriseViewCluster = false;

            // Add site to the cluster permission
            if ( RBACService.isAuthorized( Cluster.RESOURCE_TYPE, cluster.getResourceId( ), ClusterResourceIdService.PERMISSION_ADD_SITE_TO_CLUSTER,
                    adminUser ) )
            {
                clusterPermissions.put( Cluster.PERMISSION_ADD_SITES_TO_CLUSTER, true );
                bAuthoriseViewCluster = true;
            }
            else
            {
                clusterPermissions.put( Cluster.PERMISSION_ADD_SITES_TO_CLUSTER, false );
            }

            // Modify cluster permission
            if ( RBACService.isAuthorized( Cluster.RESOURCE_TYPE, cluster.getResourceId( ), ClusterResourceIdService.PERMISSION_MODIFY, adminUser ) )
            {
                clusterPermissions.put( Cluster.PERMISSION_MODIFY_CLUSTER, true );
                bAuthoriseViewCluster = true;
            }
            else
            {
                clusterPermissions.put( Cluster.PERMISSION_MODIFY_CLUSTER, false );
            }

            // Delete cluster permission
            if ( RBACService.isAuthorized( Cluster.RESOURCE_TYPE, cluster.getResourceId( ), ClusterResourceIdService.PERMISSION_DELETE, adminUser ) )
            {
                clusterPermissions.put( Cluster.PERMISSION_DELETE_CLUSTER, true );
                bAuthoriseViewCluster = true;
            }
            else
            {
                clusterPermissions.put( Cluster.PERMISSION_DELETE_CLUSTER, false );
            }

            // Add permissions to the cluster
            cluster.setPermissions( clusterPermissions );

            // Add autorized sites
            List<Site> listAuthorizedSites = SiteService.getAuthorizedSites( cluster.getId( ), adminUser );

            if ( listAuthorizedSites != null )
            {
                cluster.setSites( listAuthorizedSites );

                if ( !listAuthorizedSites.isEmpty( ) )
                {
                    bAuthoriseViewCluster = true;
                }
            }

            if ( bAuthoriseViewCluster )
                listAuthorizedClusters.add( cluster );
        }

        return listAuthorizedClusters;
    }

    public static boolean IsAddClusterAuthorized( AdminUser adminUser )
    {

        if ( RBACService.isAuthorized( new Cluster( ), ClusterResourceIdService.PERMISSION_ADD, adminUser ) )
        {
            return true;
        }

        return false;
    }

    public static boolean IsUserAuthorized( AdminUser adminUser, String clusterId, String permission )
    {

        boolean bAuthorized = false;

        if ( RBACService.isAuthorized( Cluster.RESOURCE_TYPE, clusterId, permission, adminUser ) )
        {
            bAuthorized = true;
        }

        return bAuthorized;
    }
}
