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
package fr.paris.lutece.plugins.releaser.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.service.ComponentService;
import fr.paris.lutece.plugins.releaser.service.SiteService;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.json.AbstractJsonResponse;
import fr.paris.lutece.util.json.JsonResponse;
import fr.paris.lutece.util.json.JsonUtil;

/**
 * ManageSiteRelease JSP Bean abstract class for JSP Bean
 */
@Controller( controllerJsp = "ManageComponent.jsp", controllerPath = "jsp/admin/plugins/releaser/", right = "RELEASER_MANAGEMENT" )
public class ManageComponentReleaseJspBean extends MVCAdminJspBean
{
    // Parameters

    private static final String PARAMETER_SEARCH = "search";

    // Views
    private static final String VIEW_MANAGE_COMPONENT = "manageComponent";
    // Actions

    private static final String TEMPLATE_MANAGE_COMPONENT = "/admin/plugins/releaser/manage_component.html";

    private static final String MARK_LIST_COMPONENT = "list_component";
    private static final String MARK_PAGINATOR = "paginator";

    private static final String MARK_SEARCH = "search";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";

    private static final String JSP_MANAGE_COMPONENT = "jsp/admin/plugins/releaser/ManageComponent.jsp";
    private static final String JSP_MANAGE_CLUSTERS = "ManageClusters.jsp";

    private static final String ACTION_CHANGE_COMPONENT_NEXT_RELEASE_VERSION = "versionComponent";
    private static final String ACTION_RELEASE_COMPONENT = "releaseComponent";

    private static final String PARAMETER_ARTIFACT_ID = "artifact_id";
    private static final String PARAMETER_TWEET_MESSAGE = "tweet_message";

    private String _strSearch;
    private String _strCurrentPageIndex;
    Paginator<Component> _paginatorComponents;

    @View( value = VIEW_MANAGE_COMPONENT, defaultView = true )
    public String getManageComponent( HttpServletRequest request )
    {
        ReleaserUser user=ReleaserUtils.getReleaserUser( request, getLocale( ) );
        if(user==null)
        {
            user=new ReleaserUser( );
           
        }
        ReleaserUtils.populateReleaserUser(request, user);
        ReleaserUtils.setReleaserUser( request, user );
        
        
        _strSearch = request.getParameter( PARAMETER_SEARCH )!=null?request.getParameter( PARAMETER_SEARCH ):_strSearch;
        String stCurrentPageIndexOld = _strCurrentPageIndex;
        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );

        

        if (  !StringUtils.isEmpty( request.getParameter( PARAMETER_SEARCH )) || (_strCurrentPageIndex!=null && !_strCurrentPageIndex.equals( stCurrentPageIndexOld )) )
        {

            _paginatorComponents = ComponentService.getService( ).getSearchComponent( _strSearch, request, getLocale( ), JSP_MANAGE_COMPONENT,
                    _strCurrentPageIndex );

        }

        Map<String, Object> model = getModel( );
        model.put( MARK_SEARCH, _strSearch );
        model.put( MARK_LIST_COMPONENT, _paginatorComponents!=null?_paginatorComponents.getPageItems( ):null );
        model.put( MARK_PAGINATOR, _paginatorComponents );
        model.put( MARK_NB_ITEMS_PER_PAGE,_paginatorComponents!=null?_paginatorComponents.getItemsPerPage( ):null );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_COMPONENT, getLocale( ), model );
        return template.getHtml( );
    }

    @Action( ACTION_CHANGE_COMPONENT_NEXT_RELEASE_VERSION )
    public String doChangeComponentNextReleaseVersion( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );

        if ( strArtifactId != null && _paginatorComponents != null )
        {

            for ( Component component : _paginatorComponents.getPageItems( ) )
            {

                if ( component.getArtifactId( ).equals( strArtifactId ) )
                {

                    ComponentService.getService( ).changeNextReleaseVersion( component );
                    break;
                }
            }

        }

        return redirectView( request, VIEW_MANAGE_COMPONENT );
    }

    @Action( ACTION_RELEASE_COMPONENT )
    public String doReleaseComponent( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        String strTweetMessage = request.getParameter( PARAMETER_TWEET_MESSAGE );
        
        AbstractJsonResponse jsonResponse = null;
        Integer nIdContext = null;

        if ( strArtifactId != null && _paginatorComponents != null )
        {

            for ( Component component : _paginatorComponents.getPageItems( ) )
            {
                if ( component.getArtifactId( ).equals( strArtifactId ) )
                {
                    component.setTweetMessage( strTweetMessage );
                    nIdContext = ComponentService.getService( ).release( component, getLocale( ), getUser( ), request,true );
                    break;
                }
            }
        }
        jsonResponse = new JsonResponse( nIdContext );

        return JsonUtil.buildJsonResponse( jsonResponse );
    }

}
