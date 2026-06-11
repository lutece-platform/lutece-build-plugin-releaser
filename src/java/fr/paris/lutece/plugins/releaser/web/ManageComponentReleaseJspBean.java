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
package fr.paris.lutece.plugins.releaser.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.service.ComponentService;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.json.AbstractJsonResponse;
import fr.paris.lutece.util.json.JsonResponse;
import fr.paris.lutece.util.json.JsonUtil;

// TODO: Auto-generated Javadoc
/**
 * ManageSiteRelease JSP Bean abstract class for JSP Bean.
 */
@Controller( controllerJsp = "ManageComponent.jsp", controllerPath = "jsp/admin/plugins/releaser/", right = "RELEASER_MANAGEMENT" )
public class ManageComponentReleaseJspBean extends MVCAdminJspBean
{
    // Parameters

    /** The Constant PARAMETER_SEARCH. */
    private static final String PARAMETER_SEARCH = "search";

    /** The Constant PARAMETER_RELEASE_BRANCH_NAME */
    private static final String PARAMETER_RELEASE_BRANCH_NAME = "release_branch";

    /** The Constant VIEW_MANAGE_COMPONENT. */
    // Views
    private static final String VIEW_MANAGE_COMPONENT = "manageComponent";
    // Actions

    /** The Constant VIEW_CHANGE_BRANCH. */
    private static final String VIEW_CHANGE_BRANCH = "changeBranch";

    /** The Constant VIEW_RELEASE_FROM_TAG. */
    private static final String VIEW_RELEASE_FROM_TAG = "releaseFromTag";

    /** The Constant TEMPLATE_MANAGE_COMPONENT. */
    private static final String TEMPLATE_MANAGE_COMPONENT = "/admin/plugins/releaser/manage_component.html";

    /** The Constant TEMPLATE_RELEASE_FROM_TAG. */
    private static final String TEMPLATE_RELEASE_FROM_TAG = "/admin/plugins/releaser/release_from_tag.html";

    /** The Constant MARK_LIST_COMPONENT. */
    private static final String MARK_LIST_COMPONENT = "list_component";

    /** The Constant MARK_PAGINATOR. */
    private static final String MARK_PAGINATOR = "paginator";

    /** The Constant MARK_SEARCH. */
    private static final String MARK_SEARCH = "search";

    /** The Constant MARK_NB_ITEMS_PER_PAGE. */
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";

    /** The Constant JSP_MANAGE_COMPONENT. */
    private static final String JSP_MANAGE_COMPONENT = "jsp/admin/plugins/releaser/ManageComponent.jsp";

    /** The Constant ACTION_CHANGE_COMPONENT_NEXT_RELEASE_VERSION. */
    private static final String ACTION_CHANGE_COMPONENT_NEXT_RELEASE_VERSION = "versionComponent";

    /** The Constant ACTION_RELEASE_COMPONENT. */
    private static final String ACTION_RELEASE_COMPONENT = "releaseComponent";

    /** The Constant ACTION_CHANGE_BRANCH. */
    private static final String ACTION_CHANGE_BRANCH = "doChangeBranch";

    /** The Constant ACTION_RELEASE_FROM_TAG. */
    private static final String ACTION_RELEASE_FROM_TAG = "doReleaseFromTag";

    /** The Constant PARAMETER_ARTIFACT_ID. */
    private static final String PARAMETER_ARTIFACT_ID = "artifact_id";

    /** The Constant PARAMETER_TWEET_MESSAGE. */
    private static final String PARAMETER_TWEET_MESSAGE = "tweet_message";

    /** The Constant PARAMETER_SOURCE_TAG. */
    private static final String PARAMETER_SOURCE_TAG = "source_tag";

    /** The Constant MARK_COMPONENT. */
    private static final String MARK_COMPONENT = "component";

    // Messages
    private static final String MESSAGE_ACCESS_DENIED = "releaser.message.accesDenied";

    /** The str search. */
    private String _strSearch;

    /** The str current page index. */
    private String _strCurrentPageIndex;

    /** The paginator components. */
    Paginator<Component> _paginatorComponents;

    /**
     * Gets the manage component.
     *
     * @param request
     *            the request
     * @return the manage component
     * @throws AccessDeniedException
     */
    @View( value = VIEW_MANAGE_COMPONENT, defaultView = true )
    public String getManageComponent( HttpServletRequest request ) throws AccessDeniedException
    {

        if ( !ComponentService.IsSearchComponentAuthorized( AdminUserService.getAdminUser( request ) ) )
        {
            throw new AccessDeniedException( MESSAGE_ACCESS_DENIED );
        }
        
        ReleaserUser user = setReleaserUser ( request );        
        
        if (request.getParameter( PARAMETER_SEARCH ) != null && !request.getParameter( PARAMETER_SEARCH ).isEmpty())
        {
        	if ( _strSearch == null || !_strSearch.equals(request.getParameter( PARAMETER_SEARCH )))
        	{
        		_strSearch = request.getParameter( PARAMETER_SEARCH );
        		
        		String stCurrentPageIndexOld = _strCurrentPageIndex;
                _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );

                if ( !StringUtils.isEmpty( request.getParameter( PARAMETER_SEARCH ) )
                        || ( _strCurrentPageIndex != null && !_strCurrentPageIndex.equals( stCurrentPageIndexOld ) ) )
                {

                    _paginatorComponents = ComponentService.getService( ).getSearchComponent( _strSearch, request, getLocale( ), JSP_MANAGE_COMPONENT,
                            _strCurrentPageIndex );

                }
        	}
        }
        else 
    	{
        	_strSearch = null;
    	}
     
        Map<String, Object> model = getModel( );
        model.put( MARK_SEARCH, _strSearch );
        model.put( MARK_LIST_COMPONENT, _paginatorComponents != null ? _paginatorComponents.getPageItems( ) : null );
        model.put( MARK_PAGINATOR, _paginatorComponents );
        model.put( MARK_NB_ITEMS_PER_PAGE, _paginatorComponents != null ? _paginatorComponents.getItemsPerPage( ) : null );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_COMPONENT, getLocale( ), model );
        return template.getHtml( );
    }

    /**
     * Do change component next release version.
     *
     * @param request
     *            the request
     * @return the string
     */
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

    /**
     * Do release component.
     *
     * @param request
     *            the request
     * @return the string
     */
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
                if ( component.getArtifactId( ) != null && component.getArtifactId( ).equals( strArtifactId ) )
                {
                    component.setTweetMessage( strTweetMessage );
                    nIdContext = ComponentService.getService( ).release( component, getLocale( ), getUser( ), request, true );
                    break;
                }
            }
        }
        jsonResponse = new JsonResponse( nIdContext );

        return JsonUtil.buildJsonResponse( jsonResponse );
    }
    
    /**
     * Do get Branch list.
     *
     * @param request
     *            the request
     * @return the string
     */
    @View( value = VIEW_CHANGE_BRANCH )
    public String getChangeBranch( HttpServletRequest request )
    {
    	
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );        
                        
        ReleaserUser user = setReleaserUser ( request );

        Component component = ComponentService.getService( ).getComponentBranchList( getCurrentComponent( strArtifactId ), RepositoryType.GITHUB, user );

        return redirectView( request, VIEW_MANAGE_COMPONENT );
        
    }

    /**
     * Do change Branch.
     *
     * @param request
     *            the request
     * @return the string
     */
    @Action( ACTION_CHANGE_BRANCH )
    public String doChangeBranch( HttpServletRequest request )
    {

        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        String strReleaseBranchName = request.getParameter( PARAMETER_RELEASE_BRANCH_NAME );

        ReleaserUser user = setReleaserUser ( request );

        Component component = ComponentService.getService( ).getLastBranchVersion( getCurrentComponent( strArtifactId ), strReleaseBranchName, user );

        return redirectView( request, VIEW_MANAGE_COMPONENT );
    }

    /**
     * Show the "release from tag" selection page (tag dropdown + branch dropdown).
     * Clones the repo if needed and populates both lists on the component.
     *
     * @param request
     *            the request
     * @return the page html
     */
    @View( value = VIEW_RELEASE_FROM_TAG )
    public String getReleaseFromTag( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        ReleaserUser user = setReleaserUser( request );

        Component component = getCurrentComponent( strArtifactId );
        if ( component != null )
        {
            // Branches first (clones the repo), tags reuse the same clone.
            ComponentService.getService( ).getComponentBranchList( component, RepositoryType.GITHUB, user );
            ComponentService.getService( ).getComponentTagList( component, RepositoryType.GITHUB, user );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_COMPONENT, component );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_RELEASE_FROM_TAG, getLocale( ), model );
        return template.getHtml( );
    }

    /**
     * Submit the "release from tag" form : starts the dedicated workflow with the chosen
     * source tag and develop* branch.
     *
     * @param request
     *            the request
     * @return redirect to the components view
     */
    @Action( ACTION_RELEASE_FROM_TAG )
    public String doReleaseFromTag( HttpServletRequest request )
    {
        String strArtifactId = request.getParameter( PARAMETER_ARTIFACT_ID );
        String strSourceTag = request.getParameter( PARAMETER_SOURCE_TAG );
        String strReleaseBranchName = request.getParameter( PARAMETER_RELEASE_BRANCH_NAME );

        Integer nIdContext = null;

        if ( StringUtils.isBlank( strSourceTag ) || StringUtils.isBlank( strReleaseBranchName ) )
        {
            return JsonUtil.buildJsonResponse( new JsonResponse( nIdContext ) );
        }

        ReleaserUser user = setReleaserUser( request );
        Component component = getCurrentComponent( strArtifactId );
        if ( component != null )
        {
            // Realign lastAvailableSnapshotVersion on the chosen branch's major (via
            // updateComponentForReleaseBranchFrom). Without this, the catalog's default-branch value
            // mismatches the cloned branch pom and TaskCheckoutRepository would abort with
            // "The cloned component does not match the release informations".
            component = ComponentService.getService( ).getLastBranchVersion( component, strReleaseBranchName, user );

            component.setBranchReleaseFrom( strReleaseBranchName );

            fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext context =
                    new fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext( );
            context.setComponent( component );
            context.setReleaserUser( user );
            context.setSourceTag( strSourceTag );

            CommandResult commandResult = new CommandResult( );
            commandResult.setLog( new StringBuffer( ) );
            context.setCommandResult( commandResult );

            fr.paris.lutece.plugins.releaser.service.WorkflowReleaseContextService.getService( ).addWorkflowReleaseContext( context );
            nIdContext = context.getId( );

            int nIdWorkflow = AppPropertiesService.getPropertyInt(
                    ConstanteUtils.PROPERTY_ID_WORKFLOW_RELEASE_FROM_TAG, ConstanteUtils.CONSTANTE_ID_NULL );

            fr.paris.lutece.plugins.releaser.service.WorkflowReleaseContextService.getService( ).startWorkflowReleaseContext(
                    context, nIdWorkflow, request.getLocale( ), request, getUser( ) );
        }

        return JsonUtil.buildJsonResponse( new JsonResponse( nIdContext ) );
    }

    private ReleaserUser setReleaserUser ( HttpServletRequest request )
    {

        ReleaserUser user = ReleaserUtils.getReleaserUser( request, request.getLocale( ) );
        if ( user == null )
        {
            user = new ReleaserUser( );

        }
        ReleaserUtils.populateReleaserUser( request, user );
        ReleaserUtils.setReleaserUser( request, user );

        return user;
    }
    
    private Component getCurrentComponent( String artifactId )
    {
    	Component component = null;
    	
    	if ( artifactId != null && _paginatorComponents != null )
        {
        	for ( Component comp : _paginatorComponents.getPageItems() )
            {
                if ( comp.getArtifactId( ).equals( artifactId ) )
                {
                    component = comp;
                    break;
                }
            }
        }
    	
    	return component;
    }

}
