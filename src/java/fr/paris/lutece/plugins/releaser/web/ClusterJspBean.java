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
package fr.paris.lutece.plugins.releaser.web;

import fr.paris.lutece.plugins.releaser.business.Cluster;
import fr.paris.lutece.plugins.releaser.business.ClusterHome;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the user interface to manage Cluster features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageClusters.jsp", controllerPath = "jsp/admin/plugins/releaser/", right = "RELEASER_SITE_MANAGEMENT" )
public class ClusterJspBean extends ManageSitesJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_CLUSTERS = "/admin/plugins/releaser/manage_clusters.html";
    private static final String TEMPLATE_CREATE_CLUSTER = "/admin/plugins/releaser/create_cluster.html";
    private static final String TEMPLATE_MODIFY_CLUSTER = "/admin/plugins/releaser/modify_cluster.html";

    // Parameters
    private static final String PARAMETER_ID_CLUSTER = "id";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_CLUSTERS = "releaser.manage_clusters.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_CLUSTER = "releaser.modify_cluster.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_CLUSTER = "releaser.create_cluster.pageTitle";

    // Markers
    private static final String MARK_CLUSTER_LIST = "cluster_list";
    private static final String MARK_CLUSTER = "cluster";

    private static final String JSP_MANAGE_CLUSTERS = "jsp/admin/plugins/releaser/ManageClusters.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_CLUSTER = "releaser.message.confirmRemoveCluster";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "releaser.model.entity.cluster.attribute.";

    // Views
    private static final String VIEW_MANAGE_CLUSTERS = "manageClusters";
    private static final String VIEW_CREATE_CLUSTER = "createCluster";
    private static final String VIEW_MODIFY_CLUSTER = "modifyCluster";

    // Actions
    private static final String ACTION_CREATE_CLUSTER = "createCluster";
    private static final String ACTION_MODIFY_CLUSTER = "modifyCluster";
    private static final String ACTION_REMOVE_CLUSTER = "removeCluster";
    private static final String ACTION_CONFIRM_REMOVE_CLUSTER = "confirmRemoveCluster";

    // Infos
    private static final String INFO_CLUSTER_CREATED = "releaser.info.cluster.created";
    private static final String INFO_CLUSTER_UPDATED = "releaser.info.cluster.updated";
    private static final String INFO_CLUSTER_REMOVED = "releaser.info.cluster.removed";
    
    // Session variable to store working values
    private Cluster _cluster;
    
    /**
     * Build the Manage View
     * @param request The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_CLUSTERS, defaultView = true )
    public String getManageClusters( HttpServletRequest request )
    {
        _cluster = null;
        List<Cluster> listClusters = ClusterHome.getClustersList(  );
        Map<String, Object> model = getPaginatedListModel( request, MARK_CLUSTER_LIST, listClusters, JSP_MANAGE_CLUSTERS );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_CLUSTERS, TEMPLATE_MANAGE_CLUSTERS, model );
    }

    /**
     * Returns the form to create a cluster
     *
     * @param request The Http request
     * @return the html code of the cluster form
     */
    @View( VIEW_CREATE_CLUSTER )
    public String getCreateCluster( HttpServletRequest request )
    {
        _cluster = ( _cluster != null ) ? _cluster : new Cluster(  );

        Map<String, Object> model = getModel(  );
        model.put( MARK_CLUSTER, _cluster );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_CLUSTER, TEMPLATE_CREATE_CLUSTER, model );
    }

    /**
     * Process the data capture form of a new cluster
     *
     * @param request The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CREATE_CLUSTER )
    public String doCreateCluster( HttpServletRequest request )
    {
        populate( _cluster, request );

        // Check constraints
        if ( !validateBean( _cluster, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_CLUSTER );
        }

        ClusterHome.create( _cluster );
        addInfo( INFO_CLUSTER_CREATED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_CLUSTERS );
    }

    /**
     * Manages the removal form of a cluster whose identifier is in the http
     * request
     *
     * @param request The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_CLUSTER )
    public String getConfirmRemoveCluster( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_CLUSTER ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_CLUSTER ) );
        url.addParameter( PARAMETER_ID_CLUSTER, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_CLUSTER, url.getUrl(  ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a cluster
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage clusters
     */
    @Action( ACTION_REMOVE_CLUSTER )
    public String doRemoveCluster( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_CLUSTER ) );
        ClusterHome.remove( nId );
        addInfo( INFO_CLUSTER_REMOVED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_CLUSTERS );
    }

    /**
     * Returns the form to update info about a cluster
     *
     * @param request The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_CLUSTER )
    public String getModifyCluster( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_CLUSTER ) );

        if ( _cluster == null || ( _cluster.getId(  ) != nId ))
        {
            _cluster = ClusterHome.findByPrimaryKey( nId );
        }

        Map<String, Object> model = getModel(  );
        model.put( MARK_CLUSTER, _cluster );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_CLUSTER, TEMPLATE_MODIFY_CLUSTER, model );
    }

    /**
     * Process the change form of a cluster
     *
     * @param request The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_CLUSTER )
    public String doModifyCluster( HttpServletRequest request )
    {
        populate( _cluster, request );

        // Check constraints
        if ( !validateBean( _cluster, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_CLUSTER, PARAMETER_ID_CLUSTER, _cluster.getId( ) );
        }

        ClusterHome.update( _cluster );
        addInfo( INFO_CLUSTER_UPDATED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_CLUSTERS );
    }
}