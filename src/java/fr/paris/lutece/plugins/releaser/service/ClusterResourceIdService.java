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

import java.util.List;
import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.Cluster;
import fr.paris.lutece.plugins.releaser.business.ClusterHome;
import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceList;

public class ClusterResourceIdService extends ResourceIdService
{
    private static final String PROPERTY_LABEL_RESOURCE_TYPE = "releaser.rbac.cluster.resourceType";
    private static final String PROPERTY_LABEL_ADD = "releaser.rbac.cluster.permission.add";
    private static final String PROPERTY_LABEL_MODIFY = "releaser.rbac.cluster.permission.modify";
    private static final String PROPERTY_LABEL_DELETE = "releaser.rbac.cluster.permission.delete";
    private static final String PROPERTY_LABEL_ADD_SITE_TO_CLUSTER = "releaser.rbac.cluster.permission.addSite";

    private static final String PLUGIN_NAME = "releaser";

    /** Permission for creating Cluster */
    public static final String PERMISSION_ADD = "ADD";

    /** Permission for creating Cluster */
    public static final String PERMISSION_ADD_SITE_TO_CLUSTER = "ADD_SITE_TO_CLUSTER";

    /** Permission for deleting Cluster */
    public static final String PERMISSION_DELETE = "DELETE";

    /** Permission for modifying Cluster */
    public static final String PERMISSION_MODIFY = "MODIFY";

    /** Creates a new instance of SuggestTypeResourceIdService */
    public ClusterResourceIdService( )
    {
        setPluginName( PLUGIN_NAME );
    }

    @Override
    public void register( )
    {

        ResourceType rt = new ResourceType( );
        rt.setResourceIdServiceClass( ClusterResourceIdService.class.getName( ) );
        rt.setPluginName( PLUGIN_NAME );
        rt.setResourceTypeKey( Cluster.RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );

        Permission p;

        p = new Permission( );
        p.setPermissionKey( PERMISSION_ADD );
        p.setPermissionTitleKey( PROPERTY_LABEL_ADD );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_MODIFY );
        p.setPermissionTitleKey( PROPERTY_LABEL_MODIFY );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_DELETE );
        p.setPermissionTitleKey( PROPERTY_LABEL_DELETE );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_ADD_SITE_TO_CLUSTER );
        p.setPermissionTitleKey( PROPERTY_LABEL_ADD_SITE_TO_CLUSTER );
        rt.registerPermission( p );

        ResourceTypeManager.registerResourceType( rt );

    }

    @Override
    public ReferenceList getResourceIdList( Locale locale )
    {

        ReferenceList referenceListCluster = new ReferenceList( );
        List<Cluster> listClusters = ClusterHome.getClustersList( );

        for ( Cluster cluster : listClusters )
        {
            referenceListCluster.addItem( cluster.getId( ), cluster.getName( ) );
        }

        return referenceListCluster;
    }

    @Override
    public String getTitle( String strId, Locale locale )
    {

        int nIdCluster = -1;

        try
        {
            nIdCluster = Integer.parseInt( strId );
        }
        catch( NumberFormatException ne )
        {
            AppLogService.error( ne );
        }

        Cluster cluster = ClusterHome.findByPrimaryKey( nIdCluster );

        return cluster.getName( );
    }

}
