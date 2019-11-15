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

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.service.SiteService;
import fr.paris.lutece.plugins.releaser.service.WorkflowReleaseContextService;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.json.AbstractJsonResponse;
import fr.paris.lutece.util.json.ErrorJsonResponse;
import fr.paris.lutece.util.json.JsonResponse;
import fr.paris.lutece.util.json.JsonUtil;

import java.util.HashMap;
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
    private static final String PARAMETER_SITE_ID = "id";
    private static final String PARAMETER_ARTIFACT_ID = "id";
    private static final String PARAMETER_ID_CONTEXT = "id_context";
    private static final String PARAMETER_TAG_INFORMATION = "tag_information";
    private static final String PARAMETER_OPEN_SITE_VERSION = "open_site_version";
    private static final String PARAMETER_TWEET_MESSAGE = "tweet_message_";
    private static final String PARAMETER_VALID_RELEASE_MODIF = "valid_release_modif_";

    // Views
    private static final String VIEW_MANAGE_SITE_RELEASE = "siteRelease";
    private static final String VIEW_CONFIRM_RELEASE_SITE = "confirmReleaseSite";

    private static final String VIEW_RELEASE_SITE_RESULT = "releaseSiteResult";

    private static final String VIEW_RELEASE_INFO_JSON = "releaseInfoJson";
    private static final String VIEW_RELEASE_COMPONENT_HISTORY = "releaseComponentHistory";

    // Actions
    private static final String ACTION_RELEASE_SITE = "releaseSite";
    private static final String ACTION_DO_CONFIRM_RELEASE_SITE = "doConfirmReleaseSite";

    
    private static final String ACTION_RELEASE_COMPONENT = "releaseComponent";
    private static final String ACTION_UPGRADE_COMPONENT = "upgradeComponent";
    private static final String ACTION_DOWNGRADE_COMPONENT = "downgradeComponent";
    private static final String ACTION_CANCEL_DOWNGRADE_COMPONENT = "cancelDowngradeComponent";
    private static final String ACTION_CANCEL_UPGRADE_COMPONENT = "cancelUpgradeComponent";
    

    private static final String ACTION_PROJECT_COMPONENT = "projectComponent";
    private static final String ACTION_CHANGE_COMPONENT_NEXT_RELEASE_VERSION = "versionComponent";
    private static final String ACTION_CHANGE_SITE_NEXT_RELEASE_VERSION = "versionSite";

    private static final String TEMPLATE_PREPARE_SITE_RELEASE = "/admin/plugins/releaser/prepare_site_release.html";
    private static final String TEMPLATE_CONFIRM_RELEASE_SITE = "/admin/plugins/releaser/confirm_release_site.html";

    private static final String TEMPLATE_RELEASE_SITE_RESULT = "/admin/plugins/releaser/release_site_result.html";

    private static final String TEMPLATE_RELEASE_COMPONENT_HISTORY = "/admin/plugins/releaser/release_component_history.html";

    private static final String MARK_SITE = "site";
    private static final String MARK_MODIF_VALIDATED = "modif_validated";
    
    private static final String MARK_RELEASE_CTX_RESULT = "release_ctx_result";
    private static final String MARK_OPEN_SITE_VERSION = "open_site_version";

    private static final String MARK_RELEASE_COMPONENT_HISTORY_LIST = "release_component_history_list";

    private static final String JSP_MANAGE_CLUSTERS = "ManageClusters.jsp";
    private static final String JSP_MANAGE_RELEASE_SITE = "ManageSiteRelease.jsp";

    private static final String JSON_ERROR_RELEASE_CONTEXT_NOT_EXIST = "RELEASE_CONTEXT_NOT_EXIST";

    private static final String MESSAGE_ERROR_INFORMATION = "releaser.message.errorInfomationReleaseNotChecked";

    private Site _site;
    private Map<String, Integer> _mapReleaseSiteContext;
    private Map<String, Boolean> _modifValidated;
    

    @View( value = VIEW_MANAGE_SITE_RELEASE, defaultView = true )
    public String getPrepareSiteRelease( HttpServletRequest request )
    {
        _modifValidated=null;
        String strSiteId = request.getParameter( PARAMETER_SITE_ID );
       
        if ( ( _site == null ) || ( strSiteId != null ) )
        {
            try
            {
                int nSiteId = 0;
                nSiteId = Integer.parseInt( strSiteId );
                _site = SiteService.getSite( nSiteId, request, getLocale( ) );
                SiteService.buildComments( _site, getLocale( ) );
            }
            catch( NumberFormatException e )
            {
                return redirect( request, JSP_MANAGE_CLUSTERS );
            }
            catch (AppException e) {
              
                  return redirect( request, JSP_MANAGE_CLUSTERS+"?action=releaseSite&error="+e.getMessage( )+"&"+PARAMETER_SITE_ID+"="+ strSiteId );
               
            }
        }
      
        
        
        
       
        Map<String, Object> model = getModel( );
        model.put( MARK_SITE, _site );
        model.put( MARK_OPEN_SITE_VERSION, request.getParameter( PARAMETER_OPEN_SITE_VERSION ) );
        model.put( ConstanteUtils.MARK_REPO_TYPE_GITHUB,RepositoryType.GITHUB );
        model.put( ConstanteUtils.MARK_REPO_TYPE_GITLAB,RepositoryType.GITLAB );
        model.put( ConstanteUtils.MARK_REPO_TYPE_SVN,RepositoryType.SVN );
        model.put( ConstanteUtils.MARK_USER, ReleaserUtils.getReleaserUser( request, getLocale( ) ));
        
        
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_PREPARE_SITE_RELEASE, getLocale( ), model );
        return template.getHtml( );
    }

    @View( value = VIEW_CONFIRM_RELEASE_SITE )
    public String getConfirmReleaseSite( HttpServletRequest request )
    {

        
        if(_modifValidated == null)
        {
            _modifValidated=new HashMap<String, Boolean>();
            
        }
      
        if ( _site == null )
        {
            return redirect( request, JSP_MANAGE_CLUSTERS );

        }
        
        if ( ReleaserUtils.getReleaserUser( request, getLocale( ) )==null || ReleaserUtils.getReleaserUser( request, getLocale( ) ) .getCredential( _site.getRepoType( ) )==null )
        {
            return redirect( request, JSP_MANAGE_CLUSTERS );
        }

        Map<String, Object> model = getModel( );
        
        if( _site.getRepoType( ).equals( RepositoryType.GITHUB  ) ||  _site.getComponents( ).stream( ).anyMatch( x -> x.shouldBeReleased( ) && x.getRepoType( ).equals( RepositoryType.GITHUB ) ))
        {  
             model.put( ConstanteUtils.MARK_REPO_TYPE_GITHUB,RepositoryType.GITHUB );
        }
        if(_site.getRepoType( ).equals( RepositoryType.GITLAB  ) ||_site.getComponents( ).stream( ).anyMatch( x -> x.shouldBeReleased( ) && x.getRepoType( ).equals( RepositoryType.GITLAB ) ))
        {  
             model.put( ConstanteUtils.MARK_REPO_TYPE_GITLAB,RepositoryType.GITLAB );
        }
        if( _site.getRepoType( ).equals( RepositoryType.SVN  ) || _site.getComponents( ).stream( ).anyMatch( x -> x.shouldBeReleased( ) && x.getRepoType( ).equals( RepositoryType.SVN ) ))
        {  
             model.put( ConstanteUtils.MARK_REPO_TYPE_SVN,RepositoryType.SVN );
        }
        
       
        model.put( MARK_SITE, _site );
        model.put( MARK_MODIF_VALIDATED, _modifValidated );
        model.put( ConstanteUtils.MARK_USER, ReleaserUtils.getReleaserUser( request, getLocale( ) ));
      
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CONFIRM_RELEASE_SITE, getLocale( ), model );

        return template.getHtml( );
    }

    @View( value = VIEW_RELEASE_COMPONENT_HISTORY )
    public String getReleaseComponentHistory( HttpServletRequest request )
    {

        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        List<WorkflowReleaseContext> listReleaseComponentHistory = null;
        if ( !StringUtils.isEmpty( strArtifactId ) )
        {

            listReleaseComponentHistory = WorkflowReleaseContextService.getService( ).getListWorkflowReleaseContextHistory( strArtifactId );

        }

        Map<String, Object> model = getModel( );
        model.put( MARK_RELEASE_COMPONENT_HISTORY_LIST, listReleaseComponentHistory );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_RELEASE_COMPONENT_HISTORY, getLocale( ), model );
        return template.getHtml( );

    }

    @View( value = VIEW_RELEASE_INFO_JSON )
    public String getReleaseInfoJson( HttpServletRequest request )
    {

        AbstractJsonResponse jsonResponse = null;

        String strIdReleaseContext = request.getParameter( PARAMETER_ID_CONTEXT );

        if ( !StringUtils.isEmpty( strIdReleaseContext ) )
        {
            WorkflowReleaseContext context = WorkflowReleaseContextService.getService( ).getWorkflowReleaseContext(
                    ReleaserUtils.convertStringToInt( strIdReleaseContext ) );
            if ( context != null )
            {
                jsonResponse = new JsonResponse( context );

            }
            else
            {
                jsonResponse = new ErrorJsonResponse( JSON_ERROR_RELEASE_CONTEXT_NOT_EXIST );

            }
        }
        else
        {
            jsonResponse = new ErrorJsonResponse( JSON_ERROR_RELEASE_CONTEXT_NOT_EXIST );

        }
        return JsonUtil.buildJsonResponse( jsonResponse );

    }

    @Action( ACTION_DOWNGRADE_COMPONENT )
    public String doDowngradeComponent( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        SiteService.downgradeComponent( _site, strArtifactId );

        return redirectView( request, VIEW_MANAGE_SITE_RELEASE );
    }

    @Action( ACTION_CANCEL_DOWNGRADE_COMPONENT )
    public String doCancelDowngradeComponent( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        SiteService.cancelDowngradeComponent( _site, strArtifactId );

        return redirectView( request, VIEW_MANAGE_SITE_RELEASE );
    }

    @Action( ACTION_UPGRADE_COMPONENT )
    public String doUpgradeComponent( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        SiteService.upgradeComponent( _site, strArtifactId );

        return redirectView( request, VIEW_MANAGE_SITE_RELEASE );
    }
    
    @Action( ACTION_CANCEL_UPGRADE_COMPONENT )
    public String doCancelUpgradeComponent( HttpServletRequest request )
    {
        
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        SiteService.cancelUpgradeComponent( _site, strArtifactId );

        return redirectView( request, VIEW_MANAGE_SITE_RELEASE );
    }

    @Action( ACTION_RELEASE_COMPONENT )
    public String doReleaseComponent( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        AbstractJsonResponse jsonResponse = null;
        ReleaserUser user=ReleaserUtils.getReleaserUser( request, getLocale( ) );
        if(user==null)
        {
            user=new ReleaserUser( );
           
        }
        ReleaserUtils.populateReleaserUser(request, user);
        ReleaserUtils.setReleaserUser( request, user );
        Integer nidContext = SiteService.releaseComponent( _site, strArtifactId, getLocale( ), getUser( ), request );
        jsonResponse = new JsonResponse( nidContext );
        if ( ReleaserUtils.getReleaserUser( request, getLocale( ) ) == null )
        {
            return redirect( request, JSP_MANAGE_CLUSTERS );
        }

        return JsonUtil.buildJsonResponse( jsonResponse );
    }

    @Action( ACTION_DO_CONFIRM_RELEASE_SITE )
    public String doConfirmReleaseSite( HttpServletRequest request )
    {

        ReleaserUser user=ReleaserUtils.getReleaserUser( request, getLocale( ) );
        if(user==null)
        {
            user=new ReleaserUser( );
           
        }
        ReleaserUtils.populateReleaserUser(request, user);
        ReleaserUtils.setReleaserUser( request, user );

        String strCheckedReleaseInfo = null;
        String strTweetMessage = null;
        
        if ( _site != null && _site.getComponents( ) != null )
        {
            
            if(_modifValidated==null)
            {
                _modifValidated=new HashMap<String, Boolean>();

            }
                for ( Component component : _site.getComponents( ) )
                {

                    strCheckedReleaseInfo = request.getParameter( PARAMETER_VALID_RELEASE_MODIF + component.getArtifactId( ) );
                    strTweetMessage= request.getParameter( PARAMETER_TWEET_MESSAGE + component.getArtifactId( ) );
                    component.setTweetMessage( strTweetMessage );
                    _modifValidated.put( component.getArtifactId( ), strCheckedReleaseInfo != null && strCheckedReleaseInfo.equals( Boolean.TRUE.toString( ) )  );
                    
                }
            
            
            for ( Component component : _site.getComponents( ) )
            {

                if (Boolean.TRUE!=_modifValidated.get( component.getArtifactId( ) ) )
                {
                    addError( MESSAGE_ERROR_INFORMATION, getLocale( ) );
                    return redirectView( request, VIEW_CONFIRM_RELEASE_SITE );
                }
            }

        }
        _mapReleaseSiteContext = SiteService.releaseSite( _site, getLocale( ), getUser( ), request );

        return redirectView( request, VIEW_RELEASE_SITE_RESULT );
    }

    @Action( ACTION_RELEASE_SITE )
    public String doReleaseSite( HttpServletRequest request )
    {

        if ( ReleaserUtils.getReleaserUser( request, getLocale( ) ) == null )
        {
            return redirect( request, JSP_MANAGE_CLUSTERS );
        }

        String strTagInformation = request.getParameter( PARAMETER_TAG_INFORMATION );
        _site.setTagInformation( strTagInformation );
        return redirectView( request, VIEW_CONFIRM_RELEASE_SITE );
    }

    @View( value = VIEW_RELEASE_SITE_RESULT )
    public String getReleaseSiteResult( HttpServletRequest request )
    {
        if ( _mapReleaseSiteContext == null || _site == null )
        {
            return redirectView( request, VIEW_MANAGE_SITE_RELEASE );
        }
        Map<String, Object> model = getModel( );
        model.put( MARK_SITE, _site );
        model.put( MARK_RELEASE_CTX_RESULT, _mapReleaseSiteContext );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_RELEASE_SITE_RESULT, getLocale( ), model );
        String strTemplate = template.getHtml( );

        return strTemplate;

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

        return redirect( request, JSP_MANAGE_RELEASE_SITE, PARAMETER_OPEN_SITE_VERSION, 1 );
    }

}
