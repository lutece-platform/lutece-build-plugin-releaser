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

import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.service.SiteService;
import fr.paris.lutece.plugins.releaser.service.WorkflowReleaseContextService;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.json.AbstractJsonResponse;
import fr.paris.lutece.util.json.ErrorJsonResponse;
import fr.paris.lutece.util.json.JsonResponse;
import fr.paris.lutece.util.json.JsonUtil;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;



/**
 * ManageSiteRelease JSP Bean abstract class for JSP Bean
 */
@Controller( controllerJsp = "ManageSiteRelease.jsp", controllerPath = "jsp/admin/plugins/releaser/", right = "RELEASER_MANAGEMENT" )
public class ManageSiteReleaseJspBean extends MVCAdminJspBean
{
    // Parameters
    private static final String PARAMETER_SITE_ID = "id_site";
    private static final String PARAMETER_ARTIFACT_ID = "artifact_id";
    private static final String PARAMETER_ID_CONTEXT = "id_context";
    

    // Views
    private static final String VIEW_MANAGE_SITE_RELEASE = "siteRelease";
    private static final String VIEW_RELEASE_INFO_JSON = "releaseInfoJson";
    private static final String VIEW_RELEASE_COMPONENT_HISTORY = "releaseComponentHistory";
    
    

    // Actions
    private static final String ACTION_RELEASE_SITE = "releaseSite";
    private static final String ACTION_RELEASE_COMPONENT = "releaseComponent";
    private static final String ACTION_UPGRADE_COMPONENT = "upgradeComponent";
    private static final String ACTION_PROJECT_COMPONENT = "projectComponent";
    private static final String ACTION_CHANGE_COMPONENT_NEXT_RELEASE_VERSION = "versionComponent";
    private static final String ACTION_CHANGE_SITE_NEXT_RELEASE_VERSION = "versionSite";
    

    private static final String TEMPLATE_PREPARE_SITE_RELEASE = "/admin/plugins/releaser/prepare_site_release.html";
    private static final String TEMPLATE_RELEASE_COMPONENT_HISTORY = "/admin/plugins/releaser/release_component_history.html";
    
    private static final String MARK_SITE = "site";
    private static final String MARK_RELEASE_COMPONENT_HISTORY_LIST= "release_component_history_list";
    
    
    private static final String JSP_MANAGE_CLUSTERS = "ManageClusters.jsp";
    private static final String JSON_ERROR_RELEASE_CONTEXT_NOT_EXIST= "RELEASE_CONTEXT_NOT_EXIST";
    
    private Site _site;

    @View( value = VIEW_MANAGE_SITE_RELEASE, defaultView = true )
    public String getPrepareSiteRelease( HttpServletRequest request )
    {
        String strSiteId = request.getParameter( PARAMETER_SITE_ID );
        if ( ( _site == null ) || ( strSiteId != null ) )
        {
            try
            {
                int nSiteId = 0;
                nSiteId = Integer.parseInt( strSiteId );
                _site = SiteService.getSite( nSiteId );
            }
            catch( NumberFormatException e )
            {
                return redirect( request, JSP_MANAGE_CLUSTERS );
            }
        }
        SiteService.buildComments( _site, getLocale( ) );
        Map<String, Object> model = getModel( );
        model.put( MARK_SITE, _site );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_PREPARE_SITE_RELEASE, getLocale( ), model );
        return template.getHtml( );
    }
    
    @View( value = VIEW_RELEASE_COMPONENT_HISTORY)
    public String getReleaseComponentHistory( HttpServletRequest request )
    {
        
        
        String strArtifactId= request.getParameter( PARAMETER_ARTIFACT_ID );
        List<WorkflowReleaseContext> listReleaseComponentHistory=null;
        if ( !StringUtils.isEmpty( strArtifactId ))
        {
            
            listReleaseComponentHistory=WorkflowReleaseContextService.getService( ).getListWorkflowReleaseContextHistory( strArtifactId );
            
         }
        
        Map<String, Object> model = getModel( );
        model.put( MARK_RELEASE_COMPONENT_HISTORY_LIST, listReleaseComponentHistory );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_RELEASE_COMPONENT_HISTORY, getLocale( ), model );
        return template.getHtml( );

        
     }
    
    @View( value = VIEW_RELEASE_INFO_JSON)
    public String getReleaseInfoJson( HttpServletRequest request )
    {
        
        AbstractJsonResponse jsonResponse = null;

        String strIdReleaseContext = request.getParameter( PARAMETER_ID_CONTEXT );
        
        if ( !StringUtils.isEmpty( strIdReleaseContext ))
        {
            WorkflowReleaseContext context=WorkflowReleaseContextService.getService( ).getWorkflowReleaseContext( ReleaserUtils.convertStringToInt( strIdReleaseContext ) );
            if(context!=null)
            {
             jsonResponse=new JsonResponse( context );
               
            }
            else
            {
                jsonResponse=new ErrorJsonResponse( JSON_ERROR_RELEASE_CONTEXT_NOT_EXIST );
                
            }
        }
        else
        {
            jsonResponse=new ErrorJsonResponse( JSON_ERROR_RELEASE_CONTEXT_NOT_EXIST );
            
        }
        return JsonUtil.buildJsonResponse( jsonResponse );
        
     }
    
    
  
    @Action( ACTION_UPGRADE_COMPONENT )
    public String doUpgradeComponent( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        SiteService.upgradeComponent( _site, strArtifactId );

        return redirectView( request, VIEW_MANAGE_SITE_RELEASE );
    }
    @Action( ACTION_RELEASE_COMPONENT )
    public String doReleaseComponent( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        AbstractJsonResponse jsonResponse = null;
        Integer nidContext= SiteService.releaseComponent( _site, strArtifactId ,getLocale( ),getUser( ),request);
        jsonResponse=new JsonResponse( nidContext );
        
        return JsonUtil.buildJsonResponse( jsonResponse );
    }
    
    @Action( ACTION_RELEASE_SITE )
    public String doReleaseSite( HttpServletRequest request )
    {
       
        
      SiteService.releaseSite( _site, getLocale( ), getUser( ), request );

        return redirectView( request, VIEW_MANAGE_SITE_RELEASE );
    }

    @Action( ACTION_PROJECT_COMPONENT )
    public String doProjectComponent( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        SiteService.toggleProjectComponent( _site, strArtifactId );

        return redirectView( request, VIEW_MANAGE_SITE_RELEASE );
    }

    @Action( ACTION_CHANGE_COMPONENT_NEXT_RELEASE_VERSION )
    public String doChangeComponentNextReleaseVersion( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        SiteService.changeNextReleaseVersion( _site, strArtifactId );

        return redirectView( request, VIEW_MANAGE_SITE_RELEASE );
    }

    @Action( ACTION_CHANGE_SITE_NEXT_RELEASE_VERSION )
    public String doChangeSiteNextReleaseVersion( HttpServletRequest request )
    {
        SiteService.changeNextReleaseVersion( _site );

        return redirectView( request, VIEW_MANAGE_SITE_RELEASE );
    }

    
}
