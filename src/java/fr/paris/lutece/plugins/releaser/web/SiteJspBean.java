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

import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.SiteHome;
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
 * This class provides the user interface to manage Site features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageSites.jsp", controllerPath = "jsp/admin/plugins/releaser/", right = "RELEASER_SITE_MANAGEMENT" )
public class SiteJspBean extends ManageSitesJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_SITES = "/admin/plugins/releaser/manage_sites.html";
    private static final String TEMPLATE_CREATE_SITE = "/admin/plugins/releaser/create_site.html";
    private static final String TEMPLATE_MODIFY_SITE = "/admin/plugins/releaser/modify_site.html";

    // Parameters
    private static final String PARAMETER_ID_SITE = "id";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_SITES = "releaser.manage_sites.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_SITE = "releaser.modify_site.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_SITE = "releaser.create_site.pageTitle";

    // Markers
    private static final String MARK_SITE_LIST = "site_list";
    private static final String MARK_SITE = "site";

    private static final String JSP_MANAGE_SITES = "jsp/admin/plugins/releaser/ManageSites.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_SITE = "releaser.message.confirmRemoveSite";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "releaser.model.entity.site.attribute.";

    // Views
    private static final String VIEW_MANAGE_SITES = "manageSites";
    private static final String VIEW_CREATE_SITE = "createSite";
    private static final String VIEW_MODIFY_SITE = "modifySite";

    // Actions
    private static final String ACTION_CREATE_SITE = "createSite";
    private static final String ACTION_MODIFY_SITE = "modifySite";
    private static final String ACTION_REMOVE_SITE = "removeSite";
    private static final String ACTION_CONFIRM_REMOVE_SITE = "confirmRemoveSite";

    // Infos
    private static final String INFO_SITE_CREATED = "releaser.info.site.created";
    private static final String INFO_SITE_UPDATED = "releaser.info.site.updated";
    private static final String INFO_SITE_REMOVED = "releaser.info.site.removed";

    // Session variable to store working values
    private Site _site;

    /**
     * Build the Manage View
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_SITES, defaultView = true )
    public String getManageSites( HttpServletRequest request )
    {
        _site = null;
        List<Site> listSites = SiteHome.getSitesList( );
        Map<String, Object> model = getPaginatedListModel( request, MARK_SITE_LIST, listSites, JSP_MANAGE_SITES );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_SITES, TEMPLATE_MANAGE_SITES, model );
    }

    /**
     * Returns the form to create a site
     *
     * @param request
     *            The Http request
     * @return the html code of the site form
     */
    @View( VIEW_CREATE_SITE )
    public String getCreateSite( HttpServletRequest request )
    {
        _site = ( _site != null ) ? _site : new Site( );

        Map<String, Object> model = getModel( );
        model.put( MARK_SITE, _site );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_SITE, TEMPLATE_CREATE_SITE, model );
    }

    /**
     * Process the data capture form of a new site
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CREATE_SITE )
    public String doCreateSite( HttpServletRequest request )
    {
        populate( _site, request );

        // Check constraints
        if ( !validateBean( _site, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_SITE );
        }

        SiteHome.create( _site );
        addInfo( INFO_SITE_CREATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_SITES );
    }

    /**
     * Manages the removal form of a site whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_SITE )
    public String getConfirmRemoveSite( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_SITE ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_SITE ) );
        url.addParameter( PARAMETER_ID_SITE, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SITE, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a site
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage sites
     */
    @Action( ACTION_REMOVE_SITE )
    public String doRemoveSite( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_SITE ) );
        SiteHome.remove( nId );
        addInfo( INFO_SITE_REMOVED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_SITES );
    }

    /**
     * Returns the form to update info about a site
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_SITE )
    public String getModifySite( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_SITE ) );

        if ( _site == null || ( _site.getId( ) != nId ) )
        {
            _site = SiteHome.findByPrimaryKey( nId );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_SITE, _site );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_SITE, TEMPLATE_MODIFY_SITE, model );
    }

    /**
     * Process the change form of a site
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_SITE )
    public String doModifySite( HttpServletRequest request )
    {
        populate( _site, request );

        // Check constraints
        if ( !validateBean( _site, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_SITE, PARAMETER_ID_SITE, _site.getId( ) );
        }

        SiteHome.update( _site );
        addInfo( INFO_SITE_UPDATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_SITES );
    }
}
