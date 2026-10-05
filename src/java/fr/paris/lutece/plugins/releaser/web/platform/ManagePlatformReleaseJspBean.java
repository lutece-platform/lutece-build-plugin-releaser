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
package fr.paris.lutece.plugins.releaser.web.platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.paris.lutece.portal.service.i18n.I18nService;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformRelease;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseHome;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleasePlan;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStatus;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStep;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseType;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepCode;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepDefinition;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepDefinitionHome;
import fr.paris.lutece.plugins.releaser.service.ReleasePreparationService;
import fr.paris.lutece.plugins.releaser.service.SiteService;
import fr.paris.lutece.plugins.releaser.service.platform.PlatformReleaseService;
import fr.paris.lutece.plugins.releaser.service.platform.PlatformStepTask;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.json.AbstractJsonResponse;
import fr.paris.lutece.util.json.ErrorJsonResponse;
import fr.paris.lutece.util.json.JsonResponse;
import fr.paris.lutece.util.json.JsonUtil;
import fr.paris.lutece.util.url.UrlItem;

/**
 * Release of the Lutece platform : campaigns, their 5 steps, preparation of a step (components and versions, like a site), launch on Jenkins,
 * follow-up and CSV export. The transient site of the step being prepared is kept in session, like the site of the site screen.
 */
@Controller( controllerJsp = "ManagePlatformRelease.jsp", controllerPath = "jsp/admin/plugins/releaser/", right = "RELEASER_PLATFORM" )
public class ManagePlatformReleaseJspBean extends MVCAdminJspBean
{
    private static final long serialVersionUID = 1L;

    // Parameters
    private static final String PARAMETER_ID = "id";
    private static final String PARAMETER_STEP = "step";
    private static final String PARAMETER_ARTIFACT_ID = "artifact_id";
    private static final String PARAMETER_NAME = "name";
    private static final String PARAMETER_RELEASE_TYPE = "release_type";
    private static final String PARAMETER_CORE_MAJOR = "core_major";
    private static final String PARAMETER_DRY_RUN = "dry_run";
    private static final String PARAMETER_RELOAD = "reload";
    private static final String PARAMETER_AUTH = "auth";
    private static final String PARAMETER_OPEN_AGGREGATE_VERSION = "open_aggregate_version";
    private static final String PARAMETER_RELEASE_TARGET = "release_target";
    private static final String PARAMETER_RELEASE_BRANCH = "release_branch";
    private static final String PARAMETER_CORE_VERSION = "core_version";
    private static final String PARAMETER_PARENT_VERSION = "parent_version";
    private static final String PARAMETER_ALLOW_SNAPSHOT_DEPENDENCIES = "allow_snapshot_dependencies";

    // Views
    private static final String VIEW_MANAGE_PLATFORM_RELEASES = "managePlatformReleases";
    private static final String VIEW_CREATE_PLATFORM_RELEASE = "createPlatformRelease";
    private static final String VIEW_PLATFORM_RELEASE = "platformRelease";
    private static final String VIEW_PREPARE_STEP = "prepareStep";
    private static final String VIEW_STEP_STATUS_JSON = "stepStatusJson";
    private static final String VIEW_EXPORT_CSV = "exportCsv";
    private static final String VIEW_STEP_DEFINITIONS = "stepDefinitions";

    // Actions
    private static final String ACTION_CREATE_PLATFORM_RELEASE = "createPlatformRelease";
    private static final String ACTION_CONFIRM_REMOVE_PLATFORM_RELEASE = "confirmRemovePlatformRelease";
    private static final String ACTION_REMOVE_PLATFORM_RELEASE = "removePlatformRelease";
    private static final String ACTION_AUTHENTICATE = "authenticate";
    private static final String ACTION_SKIP_STEP = "skipStep";
    private static final String ACTION_REARM_STEP = "rearmStep";
    private static final String ACTION_VERIFY_EXPORT = "verifyExport";
    private static final String ACTION_TOGGLE_COMPONENT = "toggleComponent";
    private static final String ACTION_CHANGE_COMPONENT_VERSION = "changeComponentVersion";
    private static final String ACTION_CHANGE_AGGREGATE_VERSION = "changeAggregateVersion";
    private static final String ACTION_UPGRADE_COMPONENT = "upgradeComponent";
    private static final String ACTION_CANCEL_UPGRADE_COMPONENT = "cancelUpgradeComponent";
    private static final String ACTION_DOWNGRADE_COMPONENT = "downgradeComponent";
    private static final String ACTION_CANCEL_DOWNGRADE_COMPONENT = "cancelDowngradeComponent";
    private static final String ACTION_USE_LATEST_SNAPSHOT = "useLatestSnapshot";
    private static final String ACTION_CANCEL_LATEST_SNAPSHOT = "cancelLatestSnapshot";
    private static final String ACTION_UPGRADE_PARENT = "upgradeParent";
    private static final String ACTION_USE_CORE_VERSION = "useCoreVersion";
    private static final String ACTION_CANCEL_CORE_VERSION = "cancelCoreVersion";
    private static final String ACTION_USE_PARENT_VERSION = "useParentVersion";
    private static final String ACTION_CANCEL_PARENT_VERSION = "cancelParentVersion";
    private static final String ACTION_CANCEL_PARENT_UPGRADE = "cancelParentUpgrade";
    private static final String ACTION_UPGRADE_ALL_PARENTS = "upgradeAllParents";
    private static final String ACTION_LAUNCH_STEP = "launchStep";
    private static final String ACTION_SAVE_STEP_DEFINITIONS = "saveStepDefinitions";
    private static final String ACTION_CHANGE_STEP_BRANCH = "changeStepBranch";
    private static final String ACTION_SELECT_SNAPSHOT_COMPONENTS = "selectSnapshotComponents";
    private static final String ACTION_DESELECT_SNAPSHOT_COMPONENTS = "deselectSnapshotComponents";

    // Templates
    private static final String TEMPLATE_MANAGE_PLATFORM_RELEASES = "/admin/plugins/releaser/platform/manage_platform_releases.html";
    private static final String TEMPLATE_CREATE_PLATFORM_RELEASE = "/admin/plugins/releaser/platform/create_platform_release.html";
    private static final String TEMPLATE_PLATFORM_RELEASE = "/admin/plugins/releaser/platform/platform_release.html";
    private static final String TEMPLATE_PREPARE_STEP = "/admin/plugins/releaser/platform/prepare_platform_step.html";
    private static final String TEMPLATE_STEP_DEFINITIONS = "/admin/plugins/releaser/platform/manage_step_definitions.html";

    // Page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE = "releaser.manage_platform_releases.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE = "releaser.create_platform_release.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_PLATFORM_RELEASE = "releaser.platform_release.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_PREPARE_STEP = "releaser.prepare_platform_step.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_STEP_DEFINITIONS = "releaser.manage_step_definitions.pageTitle";

    // Marks
    private static final String MARK_PLATFORM_RELEASE_LIST = "platform_release_list";
    private static final String MARK_PLATFORM_RELEASE = "platform_release";
    private static final String MARK_RELEASE_TYPES = "release_types";
    private static final String MARK_CORE_MAJORS = "core_majors";
    private static final String MARK_STEP_DEFINITIONS = "step_definitions";
    private static final String MARK_CAN_PREPARE = "can_prepare";
    private static final String MARK_CAN_SKIP = "can_skip";
    private static final String MARK_ORPHAN_STEPS = "orphan_steps";
    private static final String MARK_OPEN_AUTH_STEP = "open_auth_step";
    private static final String MARK_OPEN_AUTH_MISSING = "open_auth_missing";
    private static final String MARK_AUTH_MESSAGES = "auth_messages";
    private static final String MARK_STEP = "step";
    private static final String MARK_STEP_CODE = "step_code";
    private static final String MARK_STEP_DEFINITION = "step_definition";
    private static final String MARK_RELEASE_TYPE = "release_type";
    private static final String MARK_SITE = "site";
    private static final String MARK_OPEN_AGGREGATE_VERSION = "open_aggregate_version";
    private static final String MARK_PIPELINE_PARAMETERS = "pipeline_parameters";
    private static final String MARK_CORE_BRANCHES = "core_branches";
    private static final String MARK_STEP_REPORTS = "step_reports";
    private static final String MARK_MASTER_BRANCH = "master_branch";
    private static final String MARK_HAS_SELECTABLE_SNAPSHOTS = "has_selectable_snapshots";
    private static final String MARK_LATEST_SNAPSHOT_CANDIDATES = "latest_snapshot_candidates";
    private static final String MARK_LATEST_SNAPSHOT_COMPONENTS = "latest_snapshot_components";
    private static final String MARK_CREATE_AUTHORIZED = "create_authorized";

    // Messages
    private static final String MESSAGE_ACCESS_DENIED = "releaser.message.accesDenied";
    private static final String MESSAGE_CONFIRM_REMOVE = "releaser.message.confirmRemovePlatformRelease";
    private static final String MESSAGE_NOT_FOUND = "releaser.message.platformRelease.notFound";
    private static final String MESSAGE_STEP_LOCKED = "releaser.message.platformRelease.stepLocked";
    private static final String MESSAGE_REMOVE_RUNNING = "releaser.message.platformRelease.removeRunning";
    private static final String MESSAGE_UNKNOWN_BRANCH = "releaser.message.platformRelease.unknownBranch";
    private static final String MESSAGE_EXPORT_VERIFICATION_NOT_ALLOWED = "releaser.message.platformRelease.exportVerificationNotAllowed";
    private static final String MESSAGE_LOAD_ERROR = "releaser.message.platformRelease.loadError";
    private static final String MESSAGE_CORE_MAJOR_REQUIRED = "releaser.message.platformRelease.coreMajorRequired";
    private static final String MESSAGE_RELEASE_TYPE_REQUIRED = "releaser.message.platformRelease.releaseTypeRequired";
    private static final String MESSAGE_NAME_ALREADY_USED = "releaser.message.platformRelease.nameAlreadyUsed";
    private static final String MESSAGE_CREDENTIALS_REFUSED = "releaser.message.platform.credentialsRefused";
    private static final String MESSAGE_CREDENTIALS_MISSING_FOR = "releaser.message.platform.credentialsMissingFor";
    private static final String INFO_CREATED = "releaser.info.platformRelease.created";
    private static final String INFO_REMOVED = "releaser.info.platformRelease.removed";
    private static final String INFO_STEP_SKIPPED = "releaser.info.platformRelease.stepSkipped";
    private static final String INFO_STEP_REARMED = "releaser.info.platformRelease.stepRearmed";
    private static final String MESSAGE_STEP_NOT_REARMABLE = "releaser.message.platformRelease.stepNotRearmable";
    private static final String INFO_STEP_LAUNCHED = "releaser.info.platformRelease.stepLaunched";
    private static final String INFO_DRY_RUN_LAUNCHED = "releaser.info.platformRelease.dryRunLaunched";
    private static final String INFO_EXPORT_VERIFIED = "releaser.info.platformRelease.exportVerified";
    private static final String INFO_STEP_DEFINITIONS_SAVED = "releaser.info.platformRelease.stepDefinitionsSaved";

    private static final String VALIDATION_ATTRIBUTES_PREFIX = "releaser.model.entity.platformRelease.attribute.";
    private static final String VALIDATION_STEP_DEFINITION_PREFIX = "releaser.model.entity.platformStepDefinition.attribute.";
    private static final String PARAMETER_STEP_NAME_PREFIX = "name_";
    private static final String PARAMETER_STEP_SCM_URL_PREFIX = "scm_url_";
    private static final String CSV_FILE_PREFIX = "release-plateforme-";
    private static final String CSV_FILE_EXTENSION = ".csv";
    private static final String CSV_CONTENT_TYPE = "text/csv";
    private static final Integer [ ] CORE_MAJORS = {
            8, 7
    };

    private PlatformRelease _campaign;
    private Site _site;
    private int _nSiteCampaignId;
    private int _nSiteStepNumber;
    private String _strSiteStepStamp;
    private List<PlatformStepDefinition> _listPendingDefinitions;

    /**
     * Lists the campaigns the user may manage.
     *
     * @param request
     *            the request
     * @return the page
     */
    @View( value = VIEW_MANAGE_PLATFORM_RELEASES, defaultView = true )
    public String getManagePlatformReleases( HttpServletRequest request )
    {
        AdminUser user = AdminUserService.getAdminUser( request );
        List<PlatformRelease> listCampaigns = new ArrayList<>( );
        for ( PlatformRelease campaign : PlatformReleaseHome.getPlatformReleasesList( ) )
        {
            if ( isAuthorized( user, campaign ) )
            {
                listCampaigns.add( campaign );
            }
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_PLATFORM_RELEASE_LIST, listCampaigns );
        model.put( MARK_CREATE_AUTHORIZED, isAuthorized( user, new PlatformRelease( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE, TEMPLATE_MANAGE_PLATFORM_RELEASES, model );
    }

    /**
     * Displays the campaign creation form.
     *
     * @param request
     *            the request
     * @return the page
     * @throws AccessDeniedException
     *             if the user may not create a campaign
     */
    @View( VIEW_CREATE_PLATFORM_RELEASE )
    public String getCreatePlatformRelease( HttpServletRequest request ) throws AccessDeniedException
    {
        checkAuthorized( request, new PlatformRelease( ) );
        _campaign = ( _campaign != null ) ? _campaign : new PlatformRelease( );

        Map<String, Object> model = getModel( );
        model.put( MARK_PLATFORM_RELEASE, _campaign );
        model.put( MARK_RELEASE_TYPES, PlatformReleaseType.values( ) );
        model.put( MARK_CORE_MAJORS, CORE_MAJORS );
        Map<String, Map<String, String>> mapCoreBranches = new LinkedHashMap<>( );
        for ( Integer nCoreMajor : CORE_MAJORS )
        {
            Map<String, String> mapBranches = new LinkedHashMap<>( );
            for ( Map.Entry<PlatformStepCode, String> entry : PlatformReleaseService.getStepBranches( nCoreMajor ).entrySet( ) )
            {
                mapBranches.put( entry.getKey( ).name( ), entry.getValue( ) );
            }
            mapCoreBranches.put( Integer.toString( nCoreMajor ), mapBranches );
        }
        model.put( MARK_CORE_BRANCHES, mapCoreBranches );

        return getPage( PROPERTY_PAGE_TITLE_CREATE, TEMPLATE_CREATE_PLATFORM_RELEASE, model );
    }

    /**
     * Creates a campaign with its 5 steps.
     *
     * @param request
     *            the request
     * @return the campaign page
     * @throws AccessDeniedException
     *             if the user may not create a campaign
     */
    @Action( ACTION_CREATE_PLATFORM_RELEASE )
    public String doCreatePlatformRelease( HttpServletRequest request ) throws AccessDeniedException
    {
        checkAuthorized( request, new PlatformRelease( ) );
        _campaign = ( _campaign != null ) ? _campaign : new PlatformRelease( );
        _campaign.setName( StringUtils.trim( request.getParameter( PARAMETER_NAME ) ) );
        _campaign.setReleaseType( parseReleaseType( request.getParameter( PARAMETER_RELEASE_TYPE ) ) );
        _campaign.setCoreMajor( ReleaserUtils.convertStringToInt( request.getParameter( PARAMETER_CORE_MAJOR ) ) );

        if ( !validateBean( _campaign, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_PLATFORM_RELEASE );
        }
        if ( PlatformReleaseHome.findByName( _campaign.getName( ) ) != null )
        {
            addError( MESSAGE_NAME_ALREADY_USED, getLocale( ) );
            return redirectView( request, VIEW_CREATE_PLATFORM_RELEASE );
        }
        if ( _campaign.getReleaseType( ) == null )
        {
            addError( MESSAGE_RELEASE_TYPE_REQUIRED, getLocale( ) );
            return redirectView( request, VIEW_CREATE_PLATFORM_RELEASE );
        }
        if ( _campaign.getCoreMajor( ) <= 0 )
        {
            addError( MESSAGE_CORE_MAJOR_REQUIRED, getLocale( ) );
            return redirectView( request, VIEW_CREATE_PLATFORM_RELEASE );
        }

        PlatformRelease created = PlatformReleaseService.createPlatformRelease( _campaign.getName( ), _campaign.getReleaseType( ), _campaign.getCoreMajor( ),
                AdminUserService.getAdminUser( request ).getAccessCode( ) );
        _campaign = null;
        addInfo( INFO_CREATED, getLocale( ) );

        return redirect( request, VIEW_PLATFORM_RELEASE, PARAMETER_ID, created.getId( ) );
    }

    /**
     * Asks for the confirmation of a campaign removal.
     *
     * @param request
     *            the request
     * @return the confirmation page
     * @throws AccessDeniedException
     *             if the user may not manage the campaign
     */
    @Action( ACTION_CONFIRM_REMOVE_PLATFORM_RELEASE )
    public String getConfirmRemovePlatformRelease( HttpServletRequest request ) throws AccessDeniedException
    {
        PlatformRelease campaign = getCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        checkAuthorized( request, campaign );

        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_PLATFORM_RELEASE ) );
        url.addParameter( PARAMETER_ID, campaign.getId( ) );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Removes a campaign, its steps and its component flags.
     *
     * @param request
     *            the request
     * @return the campaign list
     * @throws AccessDeniedException
     *             if the user may not manage the campaign
     */
    @Action( ACTION_REMOVE_PLATFORM_RELEASE )
    public String doRemovePlatformRelease( HttpServletRequest request ) throws AccessDeniedException
    {
        PlatformRelease campaign = getCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        checkAuthorized( request, campaign );
        if ( PlatformReleaseService.isRunning( PlatformReleaseHome.findByPrimaryKeyWithSteps( campaign.getId( ) ) ) )
        {
            addError( MESSAGE_REMOVE_RUNNING, getLocale( ) );
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }

        PlatformReleaseService.removePlatformRelease( campaign.getId( ) );
        if ( _nSiteCampaignId == campaign.getId( ) )
        {
            _site = null;
        }
        addInfo( INFO_REMOVED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
    }

    /**
     * Displays a campaign : its 5 steps with their status, build and available actions.
     *
     * @param request
     *            the request
     * @return the page
     * @throws AccessDeniedException
     *             if the user may not manage the campaign
     */
    @View( VIEW_PLATFORM_RELEASE )
    public String getPlatformRelease( HttpServletRequest request ) throws AccessDeniedException
    {
        PlatformRelease campaign = getCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        checkAuthorized( request, campaign );

        List<Boolean> listCanPrepare = new ArrayList<>( );
        List<Boolean> listCanSkip = new ArrayList<>( );
        List<Boolean> listOrphan = new ArrayList<>( );
        Map<String, String> mapStepReports = new LinkedHashMap<>( );
        for ( PlatformReleaseStep step : campaign.getSteps( ) )
        {
            listCanPrepare.add( PlatformReleaseService.canPrepareStep( campaign, step.getStepNumber( ) ) );
            listCanSkip.add( PlatformReleaseService.canSkipStep( campaign, step.getStepNumber( ) ) );
            listOrphan.add( step.getStatus( ) == PlatformReleaseStatus.RUNNING && !PlatformReleaseService.isStepFollowed( step ) );
            String strReport = PlatformReleaseService.getStepReport( campaign, step.getStepNumber( ) );
            if ( strReport != null )
            {
                mapStepReports.put( Integer.toString( step.getStepNumber( ) ), strReport );
            }
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_PLATFORM_RELEASE, campaign );
        model.put( MARK_STEP_DEFINITIONS, PlatformStepDefinitionHome.findAll( ) );
        model.put( MARK_CAN_PREPARE, listCanPrepare );
        model.put( MARK_CAN_SKIP, listCanSkip );
        model.put( MARK_STEP_REPORTS, mapStepReports );
        model.put( MARK_ORPHAN_STEPS, listOrphan );
        model.put( MARK_OPEN_AUTH_STEP, request.getParameter( PARAMETER_AUTH ) );
        RepositoryType stepRepositoryType = PlatformReleaseService.getStepRepositoryType( ReleaserUtils.convertStringToInt( request.getParameter( PARAMETER_AUTH ) ) );
        putRepositoryTypeMarks( model, stepRepositoryType != null ? Arrays.asList( stepRepositoryType ) : Arrays.asList( RepositoryType.values( ) ) );
        model.put( ConstanteUtils.MARK_USER, ReleaserUtils.getReleaserUser( request, getLocale( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_PLATFORM_RELEASE, TEMPLATE_PLATFORM_RELEASE, model );
    }

    /**
     * Stores the Git credentials given in the authentication modal, then opens the step preparation.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_AUTHENTICATE )
    public String doAuthenticate( HttpServletRequest request )
    {
        ReleaserUser user = ReleaserUtils.getReleaserUser( request, getLocale( ) );
        if ( user == null )
        {
            user = new ReleaserUser( );
        }
        ReleaserUtils.populateReleaserUser( request, user );
        ReleaserUtils.setReleaserUser( request, user );

        return redirectToStep( request, VIEW_PREPARE_STEP, getCampaignId( request ), getStepNumber( request ), PARAMETER_RELOAD, "1" );
    }

    /**
     * Exposes to the authentication modal the repository types whose credentials are asked.
     *
     * @param model
     *            the model
     * @param listTypes
     *            the repository types
     */
    private static void putRepositoryTypeMarks( Map<String, Object> model, List<RepositoryType> listTypes )
    {
        if ( listTypes.contains( RepositoryType.GITHUB ) )
        {
            model.put( ConstanteUtils.MARK_REPO_TYPE_GITHUB, RepositoryType.GITHUB );
        }
        if ( listTypes.contains( RepositoryType.GITLAB ) )
        {
            model.put( ConstanteUtils.MARK_REPO_TYPE_GITLAB, RepositoryType.GITLAB );
        }
    }

    /**
     * Prepares a step : loads its aggregate and components in a transient site kept in session, then displays them like a site.
     *
     * @param request
     *            the request
     * @return the page
     * @throws AccessDeniedException
     *             if the user may not manage the campaign
     */
    @View( VIEW_PREPARE_STEP )
    public String getPrepareStep( HttpServletRequest request ) throws AccessDeniedException
    {
        PlatformRelease campaign = getCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        checkAuthorized( request, campaign );

        int nStepNumber = getStepNumber( request );
        if ( !PlatformReleaseService.canPrepareStep( campaign, nStepNumber ) )
        {
            addError( MESSAGE_STEP_LOCKED, getLocale( ) );
            return redirect( request, VIEW_PLATFORM_RELEASE, PARAMETER_ID, campaign.getId( ) );
        }

        ReleaserUser user = ReleaserUtils.getReleaserUser( request, getLocale( ) );
        if ( user == null )
        {
            return redirectToStep( request, VIEW_PLATFORM_RELEASE, campaign.getId( ), nStepNumber, PARAMETER_AUTH, Integer.toString( nStepNumber ) );
        }

        String strStepStamp = PlatformReleaseService.getStepStamp( PlatformReleaseService.getStep( campaign, nStepNumber ) );
        boolean bReload = request.getParameter( PARAMETER_RELOAD ) != null || !strStepStamp.equals( _strSiteStepStamp );
        if ( _site == null || _nSiteCampaignId != campaign.getId( ) || _nSiteStepNumber != nStepNumber || bReload )
        {
            try
            {
                _site = PlatformReleaseService.loadStepAggregate( campaign, nStepNumber, user, getLocale( ) );
                _nSiteCampaignId = campaign.getId( );
                _nSiteStepNumber = nStepNumber;
                _strSiteStepStamp = strStepStamp;
            }
            catch( AppException e )
            {
                _site = null;
                if ( ConstanteUtils.ERROR_TYPE_AUTHENTICATION_ERROR.equals( e.getMessage( ) ) )
                {
                    RepositoryType stepRepositoryType = PlatformReleaseService.getStepRepositoryType( nStepNumber );
                    if ( stepRepositoryType != null && user.getCredential( stepRepositoryType ) != null )
                    {
                        addError( I18nService.getLocalizedString( MESSAGE_CREDENTIALS_REFUSED, new String [ ] {
                                stepRepositoryType.name( )
                        }, getLocale( ) ) );
                    }
                    return redirectToStep( request, VIEW_PLATFORM_RELEASE, campaign.getId( ), nStepNumber, PARAMETER_AUTH, Integer.toString( nStepNumber ) );
                }
                AppLogService.error( "Releaser : unable to prepare platform step " + nStepNumber + " of campaign " + campaign.getId( ), e );
                addError( MESSAGE_LOAD_ERROR, getLocale( ) );
                addError( e.getMessage( ) );
                return redirect( request, VIEW_PLATFORM_RELEASE, PARAMETER_ID, campaign.getId( ) );
            }
        }

        ReleasePreparationService.buildComponentsComments( _site, getLocale( ) );
        PlatformStepCode code = PlatformStepCode.fromStepNumber( nStepNumber );

        Map<String, Object> model = getModel( );
        model.put( MARK_PLATFORM_RELEASE, campaign );
        model.put( MARK_STEP, PlatformReleaseService.getStep( campaign, nStepNumber ) );
        model.put( MARK_STEP_CODE, code );
        model.put( MARK_STEP_DEFINITION, PlatformStepDefinitionHome.findByStepNumber( nStepNumber ) );
        model.put( MARK_RELEASE_TYPE, PlatformReleaseService.getStepReleaseType( campaign, code ) );
        model.put( MARK_SITE, _site );
        Map<RepositoryType, PlatformReleaseService.CredentialProblem> mapCredentialProblems = PlatformReleaseService.getCredentialProblems( _site, user );
        List<String> listAuthMessages = new ArrayList<>( );
        for ( Map.Entry<RepositoryType, PlatformReleaseService.CredentialProblem> entry : mapCredentialProblems.entrySet( ) )
        {
            String strKey = entry.getValue( ) == PlatformReleaseService.CredentialProblem.REFUSED ? MESSAGE_CREDENTIALS_REFUSED : MESSAGE_CREDENTIALS_MISSING_FOR;
            listAuthMessages.add( I18nService.getLocalizedString( strKey, new String [ ] {
                    entry.getKey( ).name( )
            }, getLocale( ) ) );
        }
        putRepositoryTypeMarks( model, PlatformReleaseService.getNeededRepositoryTypes( _site ) );
        model.put( MARK_OPEN_AUTH_MISSING, !mapCredentialProblems.isEmpty( ) );
        model.put( MARK_AUTH_MESSAGES, listAuthMessages );
        model.put( ConstanteUtils.MARK_USER, user );
        model.put( MARK_HAS_SELECTABLE_SNAPSHOTS, PlatformReleaseService.hasSelectableSnapshotComponents( campaign, nStepNumber, _site ) );
        model.put( MARK_LATEST_SNAPSHOT_CANDIDATES, PlatformReleaseService.getLatestSnapshotCandidates( _site ) );
        model.put( MARK_LATEST_SNAPSHOT_COMPONENTS, PlatformReleaseService.getLatestSnapshotComponents( campaign.getId( ), nStepNumber, _site ) );
        model.put( MARK_OPEN_AGGREGATE_VERSION, request.getParameter( PARAMETER_OPEN_AGGREGATE_VERSION ) );
        if ( code.getExecutor( ) == PlatformStepCode.Executor.PLATFORM_PIPELINE )
        {
            model.put( MARK_PIPELINE_PARAMETERS, PlatformReleaseService.getEffectivePipelineParameters( campaign, nStepNumber, _site ) );
            model.put( MARK_MASTER_BRANCH, PlatformReleaseService.getMasterBranch( _site.getBranchReleaseFrom( ), _site.getNextReleaseVersion( ) ) );
        }
        model.put( ConstanteUtils.MARK_USER, user );

        return getPage( PROPERTY_PAGE_TITLE_PREPARE_STEP, TEMPLATE_PREPARE_STEP, model );
    }

    /**
     * Returns the status of a step as JSON, for the follow-up refresh.
     *
     * @param request
     *            the request
     * @return the JSON
     */
    @View( VIEW_STEP_STATUS_JSON )
    public String getStepStatusJson( HttpServletRequest request )
    {
        PlatformRelease campaign = getCampaign( request );
        PlatformReleaseStep step = campaign != null ? PlatformReleaseService.getStep( campaign, getStepNumber( request ) ) : null;

        AbstractJsonResponse jsonResponse;
        if ( step == null || !isAuthorized( AdminUserService.getAdminUser( request ), campaign ) )
        {
            jsonResponse = new ErrorJsonResponse( MESSAGE_NOT_FOUND );
        }
        else
        {
            jsonResponse = new JsonResponse( step );
        }

        return JsonUtil.buildJsonResponse( jsonResponse );
    }

    /**
     * Downloads the CSV export of a campaign : every resource of every step plan, with the step status.
     *
     * @param request
     *            the request
     * @return nothing, the file is written in the response
     * @throws AccessDeniedException
     *             if the user may not manage the campaign
     */
    @View( VIEW_EXPORT_CSV )
    public String getExportCsv( HttpServletRequest request ) throws AccessDeniedException
    {
        PlatformRelease campaign = getCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        checkAuthorized( request, campaign );

        byte [ ] content = PlatformReleaseService.exportCsv( campaign ).getBytes( StandardCharsets.UTF_8 );
        download( content, CSV_FILE_PREFIX + campaign.getId( ) + CSV_FILE_EXTENSION, CSV_CONTENT_TYPE );

        return null;
    }

    /**
     * Rearms a running step whose build follow-up was lost (restart of the webapp, Jenkins unreachable for too long).
     *
     * @param request
     *            the request
     * @return the campaign page
     * @throws AccessDeniedException
     *             if the user may not manage the campaign
     */
    @Action( ACTION_REARM_STEP )
    public String doRearmStep( HttpServletRequest request ) throws AccessDeniedException
    {
        PlatformRelease campaign = getCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        checkAuthorized( request, campaign );

        if ( PlatformReleaseService.rearmStep( PlatformReleaseHome.findByPrimaryKeyWithSteps( campaign.getId( ) ), getStepNumber( request ) ) )
        {
            addInfo( INFO_STEP_REARMED, getLocale( ) );
        }
        else
        {
            addError( MESSAGE_STEP_NOT_REARMABLE, getLocale( ) );
        }

        return redirect( request, VIEW_PLATFORM_RELEASE, PARAMETER_ID, campaign.getId( ) );
    }

    /**
     * Skips a stable-only step of a pre-release campaign.
     *
     * @param request
     *            the request
     * @return the campaign page
     * @throws AccessDeniedException
     *             if the user may not manage the campaign
     */
    @Action( ACTION_SKIP_STEP )
    public String doSkipStep( HttpServletRequest request ) throws AccessDeniedException
    {
        PlatformRelease campaign = getCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        checkAuthorized( request, campaign );

        int nStepNumber = getStepNumber( request );
        if ( !PlatformReleaseService.canSkipStep( campaign, nStepNumber ) )
        {
            addError( MESSAGE_STEP_LOCKED, getLocale( ) );
        }
        else
        {
            PlatformReleaseService.skipStep( campaign, nStepNumber );
            addInfo( INFO_STEP_SKIPPED, getLocale( ) );
        }

        return redirect( request, VIEW_PLATFORM_RELEASE, PARAMETER_ID, campaign.getId( ) );
    }

    /**
     * Records that the export of the plugins step has been checked by a human, which unlocks the last step.
     *
     * @param request
     *            the request
     * @return the campaign page
     * @throws AccessDeniedException
     *             if the user may not manage the campaign
     */
    @Action( ACTION_VERIFY_EXPORT )
    public String doVerifyExport( HttpServletRequest request ) throws AccessDeniedException
    {
        PlatformRelease campaign = getCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        checkAuthorized( request, campaign );
        if ( !PlatformReleaseService.canVerifyExport( PlatformReleaseHome.findByPrimaryKeyWithSteps( campaign.getId( ) ) ) )
        {
            addError( MESSAGE_EXPORT_VERIFICATION_NOT_ALLOWED, getLocale( ) );
            return redirect( request, VIEW_PLATFORM_RELEASE, PARAMETER_ID, campaign.getId( ) );
        }

        campaign.setExportVerified( true );
        PlatformReleaseHome.update( campaign );
        addInfo( INFO_EXPORT_VERIFIED, getLocale( ) );

        return redirect( request, VIEW_PLATFORM_RELEASE, PARAMETER_ID, campaign.getId( ) );
    }

    /**
     * Toggles the "to be released" flag of a component of the step being prepared.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_TOGGLE_COMPONENT )
    public String doToggleComponent( HttpServletRequest request )
    {
        PlatformRelease campaign = getPreparedCampaign( request );
        if ( campaign != null )
        {
            PlatformReleaseService.toggleProjectComponent( campaign, _nSiteStepNumber, _site, request.getParameter( PARAMETER_ARTIFACT_ID ),
                    ReleaserUtils.getReleaserUser( request, getLocale( ) ), getLocale( ) );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Flags every component of the prepared step still in SNAPSHOT as to be released.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_SELECT_SNAPSHOT_COMPONENTS )
    public String doSelectSnapshotComponents( HttpServletRequest request )
    {
        PlatformRelease campaign = getPreparedCampaign( request );
        if ( campaign != null )
        {
            PlatformReleaseService.selectSnapshotComponents( campaign, _nSiteStepNumber, _site, ReleaserUtils.getReleaserUser( request, getLocale( ) ), getLocale( ) );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Unflags every component of the prepared step flagged as to be released, the reverse of the previous action.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_DESELECT_SNAPSHOT_COMPONENTS )
    public String doDeselectSnapshotComponents( HttpServletRequest request )
    {
        PlatformRelease campaign = getPreparedCampaign( request );
        if ( campaign != null )
        {
            PlatformReleaseService.deselectSnapshotComponents( campaign, _nSiteStepNumber, _site );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Cycles the target version of a component of the step being prepared.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_CHANGE_COMPONENT_VERSION )
    public String doChangeComponentVersion( HttpServletRequest request )
    {
        if ( getPreparedCampaign( request ) != null )
        {
            SiteService.changeNextReleaseVersion( _site, request.getParameter( PARAMETER_ARTIFACT_ID ) );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Cycles the target version of the aggregate of the step being prepared.
     *
     * @param request
     *            the request
     * @return the step preparation, with the aggregate versions opened
     */
    @Action( ACTION_CHANGE_AGGREGATE_VERSION )
    public String doChangeAggregateVersion( HttpServletRequest request )
    {
        if ( getPreparedCampaign( request ) == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        SiteService.changeNextReleaseVersion( _site );

        return redirectToStep( request, VIEW_PREPARE_STEP, _nSiteCampaignId, _nSiteStepNumber, PARAMETER_OPEN_AGGREGATE_VERSION, "1" );
    }

    /**
     * Releases the aggregate of the prepared step from another branch of its repository : the choice is kept for the campaign and the
     * aggregate is reloaded from that branch.
     *
     * @param request
     *            the request
     * @return the step preparation
     * @throws AccessDeniedException
     *             if the user is not authorized
     */
    @Action( ACTION_CHANGE_STEP_BRANCH )
    public String doChangeStepBranch( HttpServletRequest request ) throws AccessDeniedException
    {
        PlatformRelease campaign = getPreparedCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        checkAuthorized( request, campaign );
        String strBranch = StringUtils.trimToNull( request.getParameter( PARAMETER_RELEASE_BRANCH ) );
        if ( strBranch != null && !strBranch.equals( _site.getBranchReleaseFrom( ) ) && ( _site.getBranches( ) == null || !_site.getBranches( ).contains( strBranch ) ) )
        {
            addError( I18nService.getLocalizedString( MESSAGE_UNKNOWN_BRANCH, new String [ ] {
                    strBranch
            }, getLocale( ) ) );
            return redirectToPreparedStep( request );
        }
        PlatformReleaseService.saveStepBranch( campaign.getId( ), _nSiteStepNumber, strBranch );

        return redirectToStep( request, VIEW_PREPARE_STEP, campaign.getId( ), _nSiteStepNumber, PARAMETER_RELOAD, "1" );
    }

    /**
     * Saves the parameters of the lutece-platform pipeline sent with the launch of the last step : the release target and the SNAPSHOT
     * tolerance ; the other parameters come from the campaign and the prepared aggregate. Nothing is saved for the other steps.
     *
     * @param request
     *            the request
     * @param campaign
     *            the campaign
     */
    private void savePipelineParameters( HttpServletRequest request, PlatformRelease campaign )
    {
        String strReleaseTarget = StringUtils.trimToNull( request.getParameter( PARAMETER_RELEASE_TARGET ) );
        if ( strReleaseTarget == null )
        {
            return;
        }
        Map<String, String> mapParameters = new LinkedHashMap<>( );
        mapParameters.put( PlatformStepTask.PARAM_RELEASE_TARGET, strReleaseTarget );
        mapParameters.put( PlatformStepTask.PARAM_ALLOW_SNAPSHOT_DEPENDENCIES,
                Boolean.toString( Boolean.parseBoolean( request.getParameter( PARAMETER_ALLOW_SNAPSHOT_DEPENDENCIES ) ) ) );
        PlatformReleaseService.savePipelineParameters( campaign.getId( ), _nSiteStepNumber, mapParameters );
    }

    /**
     * Parses the release type chosen on the creation screen.
     *
     * @param strReleaseType
     *            the submitted value
     * @return the release type, null when none was chosen
     */
    private static PlatformReleaseType parseReleaseType( String strReleaseType )
    {
        return StringUtils.isBlank( strReleaseType ) ? null : PlatformReleaseType.valueOf( strReleaseType );
    }

    /**
     * Uses the last released version of a non snapshot component.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_UPGRADE_COMPONENT )
    public String doUpgradeComponent( HttpServletRequest request )
    {
        if ( getPreparedCampaign( request ) != null )
        {
            SiteService.upgradeComponent( _site, request.getParameter( PARAMETER_ARTIFACT_ID ) );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Cancels the upgrade of a component.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_CANCEL_UPGRADE_COMPONENT )
    public String doCancelUpgradeComponent( HttpServletRequest request )
    {
        if ( getPreparedCampaign( request ) != null )
        {
            SiteService.cancelUpgradeComponent( _site, request.getParameter( PARAMETER_ARTIFACT_ID ) );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Releases a component of the prepared step from the last SNAPSHOT published in Nexus instead of the older SNAPSHOT of the aggregate POM.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_USE_LATEST_SNAPSHOT )
    public String doUseLatestSnapshot( HttpServletRequest request )
    {
        return doLatestSnapshot( request, true );
    }

    /**
     * Goes back to the SNAPSHOT of the aggregate POM for a component that was to be released from the last SNAPSHOT published in Nexus.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_CANCEL_LATEST_SNAPSHOT )
    public String doCancelLatestSnapshot( HttpServletRequest request )
    {
        return doLatestSnapshot( request, false );
    }

    /**
     * Releases the aggregate or a component of the prepared step with the last version of its parent POM.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_UPGRADE_PARENT )
    public String doUpgradeParent( HttpServletRequest request )
    {
        return doParentUpgrade( request, true );
    }

    /**
     * Keeps the declared parent POM version of the aggregate or of a component of the prepared step.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_CANCEL_PARENT_UPGRADE )
    public String doCancelParentUpgrade( HttpServletRequest request )
    {
        return doParentUpgrade( request, false );
    }

    /**
     * Releases the aggregate and every flagged component of the prepared step with the last version of their parent POM.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_UPGRADE_ALL_PARENTS )
    public String doUpgradeAllParents( HttpServletRequest request )
    {
        PlatformRelease campaign = getPreparedCampaign( request );
        if ( campaign != null )
        {
            PlatformReleaseService.upgradeAllParents( campaign, _nSiteStepNumber, _site );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Applies or cancels the parent POM upgrade of a resource of the prepared step.
     *
     * @param request
     *            the request
     * @param bUpgrade
     *            true to upgrade, false to keep the declared version
     * @return the step preparation
     */
    private String doParentUpgrade( HttpServletRequest request, boolean bUpgrade )
    {
        PlatformRelease campaign = getPreparedCampaign( request );
        if ( campaign != null )
        {
            PlatformReleaseService.upgradeParent( campaign, _nSiteStepNumber, _site, request.getParameter( PARAMETER_ARTIFACT_ID ), bUpgrade );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Records the choice made for the lutece-core version of the platform POM : keep it, or let the pipeline set the given version.
     *
     * @param request
     *            the request, with the choice ("keep" or a version)
     * @return the step preparation, reloaded
     */
    @Action( ACTION_USE_CORE_VERSION )
    public String doUseCoreVersion( HttpServletRequest request )
    {
        return doPomVersion( request, false, request.getParameter( PARAMETER_CORE_VERSION ) );
    }

    /**
     * Forgets the choice made for the lutece-core version of the platform POM.
     *
     * @param request
     *            the request
     * @return the step preparation, reloaded
     */
    @Action( ACTION_CANCEL_CORE_VERSION )
    public String doCancelCoreVersion( HttpServletRequest request )
    {
        return doPomVersion( request, false, null );
    }

    /**
     * Records the choice made for the parent POM version of the platform POM : keep it, or let the pipeline set the given version.
     *
     * @param request
     *            the request, with the choice ("keep" or a version)
     * @return the step preparation, reloaded
     */
    @Action( ACTION_USE_PARENT_VERSION )
    public String doUseParentVersion( HttpServletRequest request )
    {
        return doPomVersion( request, true, request.getParameter( PARAMETER_PARENT_VERSION ) );
    }

    /**
     * Forgets the choice made for the parent POM version of the platform POM.
     *
     * @param request
     *            the request
     * @return the step preparation, reloaded
     */
    @Action( ACTION_CANCEL_PARENT_VERSION )
    public String doCancelParentVersion( HttpServletRequest request )
    {
        return doPomVersion( request, true, null );
    }

    /**
     * Records or forgets a POM version choice of the prepared step, then reloads the step so that the choice is reflected.
     *
     * @param request
     *            the request
     * @param bParent
     *            true for the parent POM version, false for the lutece-core version
     * @param strChoice
     *            "keep", a version, or null to forget
     * @return the step preparation, reloaded
     */
    private String doPomVersion( HttpServletRequest request, boolean bParent, String strChoice )
    {
        PlatformRelease campaign = getPreparedCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        try
        {
            PlatformReleaseService.usePomVersion( campaign, _nSiteStepNumber, _site, bParent, strChoice );
        }
        catch( AppException e )
        {
            addError( e.getMessage( ) );
            return redirectToPreparedStep( request );
        }

        return redirectToStep( request, VIEW_PREPARE_STEP, campaign.getId( ), _nSiteStepNumber, PARAMETER_RELOAD, "1" );
    }

    /**
     * Applies or cancels the "release the last published SNAPSHOT" choice of a component of the prepared step.
     *
     * @param request
     *            the request
     * @param bUse
     *            true to apply the choice, false to cancel it
     * @return the step preparation
     */
    private String doLatestSnapshot( HttpServletRequest request, boolean bUse )
    {
        PlatformRelease campaign = getPreparedCampaign( request );
        if ( campaign != null )
        {
            PlatformReleaseService.useLatestSnapshot( campaign, _nSiteStepNumber, _site, request.getParameter( PARAMETER_ARTIFACT_ID ), bUse,
                    ReleaserUtils.getReleaserUser( request, getLocale( ) ), getLocale( ) );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Uses the last released version of a flagged component instead of releasing it.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_DOWNGRADE_COMPONENT )
    public String doDowngradeComponent( HttpServletRequest request )
    {
        if ( getPreparedCampaign( request ) != null )
        {
            SiteService.downgradeComponent( _site, request.getParameter( PARAMETER_ARTIFACT_ID ) );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Cancels the downgrade of a component.
     *
     * @param request
     *            the request
     * @return the step preparation
     */
    @Action( ACTION_CANCEL_DOWNGRADE_COMPONENT )
    public String doCancelDowngradeComponent( HttpServletRequest request )
    {
        if ( getPreparedCampaign( request ) != null )
        {
            SiteService.cancelDowngradeComponent( _site, request.getParameter( PARAMETER_ARTIFACT_ID ) );
        }

        return redirectToPreparedStep( request );
    }

    /**
     * Sends the plan of the step being prepared to Jenkins, as a dry run or a real release.
     *
     * @param request
     *            the request
     * @return the campaign page
     * @throws AccessDeniedException
     *             if the user may not manage the campaign
     */
    @Action( ACTION_LAUNCH_STEP )
    public String doLaunchStep( HttpServletRequest request ) throws AccessDeniedException
    {
        PlatformRelease campaign = getPreparedCampaign( request );
        if ( campaign == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }
        checkAuthorized( request, campaign );

        boolean bDryRun = Boolean.parseBoolean( request.getParameter( PARAMETER_DRY_RUN ) );
        ReleaserUser user = ReleaserUtils.getReleaserUser( request, getLocale( ) );
        String strPermissionError = bDryRun ? null : PlatformReleaseService.checkLaunchPermissions( _site, user );
        if ( strPermissionError != null )
        {
            addError( strPermissionError );
            return redirectToPreparedStep( request );
        }
        try
        {
            savePipelineParameters( request, campaign );
            PlatformReleasePlan plan = PlatformReleaseService.buildPlan( campaign, _nSiteStepNumber, _site, bDryRun );
            PlatformReleaseService.applyPipelineParameters( campaign, _nSiteStepNumber, plan );
            String strMasterError = bDryRun ? null : PlatformReleaseService.checkMasterBranches( plan, user, getLocale( ) );
            if ( strMasterError != null )
            {
                addError( strMasterError );
                return redirectToPreparedStep( request );
            }
            PlatformReleaseService.launchStep( campaign, _nSiteStepNumber, plan, user );
            addInfo( bDryRun ? INFO_DRY_RUN_LAUNCHED : INFO_STEP_LAUNCHED, getLocale( ) );
        }
        catch( IOException | AppException e )
        {
            AppLogService.error( "Releaser : unable to launch platform step " + _nSiteStepNumber + " of campaign " + campaign.getId( ), e );
            addError( e.getMessage( ) );
        }

        return redirect( request, VIEW_PLATFORM_RELEASE, PARAMETER_ID, campaign.getId( ) );
    }

    /**
     * Displays the configuration of the 5 steps : name and repository URL of each aggregate. After a failed save, the values typed by the user
     * are shown again with the validation errors.
     *
     * @param request
     *            the request
     * @return the page
     * @throws AccessDeniedException
     *             if the user may not manage the campaigns
     */
    @View( VIEW_STEP_DEFINITIONS )
    public String getStepDefinitions( HttpServletRequest request ) throws AccessDeniedException
    {
        checkAuthorized( request, new PlatformRelease( ) );

        List<PlatformStepDefinition> listDefinitions = ( _listPendingDefinitions != null ) ? _listPendingDefinitions : PlatformStepDefinitionHome.findAll( );
        _listPendingDefinitions = null;

        Map<String, Object> model = getModel( );
        model.put( MARK_STEP_DEFINITIONS, listDefinitions );

        return getPage( PROPERTY_PAGE_TITLE_STEP_DEFINITIONS, TEMPLATE_STEP_DEFINITIONS, model );
    }

    /**
     * Saves the configuration of the 5 steps. Every step is validated before anything is stored.
     *
     * @param request
     *            the request
     * @return the configuration page
     * @throws AccessDeniedException
     *             if the user may not manage the campaigns
     */
    @Action( ACTION_SAVE_STEP_DEFINITIONS )
    public String doSaveStepDefinitions( HttpServletRequest request ) throws AccessDeniedException
    {
        checkAuthorized( request, new PlatformRelease( ) );

        List<PlatformStepDefinition> listDefinitions = PlatformStepDefinitionHome.findAll( );
        boolean bValid = true;
        for ( PlatformStepDefinition definition : listDefinitions )
        {
            definition.setName( StringUtils.trim( request.getParameter( PARAMETER_STEP_NAME_PREFIX + definition.getStepNumber( ) ) ) );
            definition.setScmUrl( StringUtils.trim( request.getParameter( PARAMETER_STEP_SCM_URL_PREFIX + definition.getStepNumber( ) ) ) );
            bValid = validateBean( definition, VALIDATION_STEP_DEFINITION_PREFIX ) && bValid;
        }

        if ( !bValid )
        {
            _listPendingDefinitions = listDefinitions;
            return redirectView( request, VIEW_STEP_DEFINITIONS );
        }

        for ( PlatformStepDefinition definition : listDefinitions )
        {
            PlatformStepDefinitionHome.update( definition );
        }
        addInfo( INFO_STEP_DEFINITIONS_SAVED, getLocale( ) );

        return redirectView( request, VIEW_STEP_DEFINITIONS );
    }

    /**
     * Returns the campaign of the step being prepared, null when no step is loaded.
     *
     * @param request
     *            the request
     * @return the campaign, with its steps
     */
    private PlatformRelease getPreparedCampaign( HttpServletRequest request )
    {
        if ( _site == null )
        {
            return null;
        }

        PlatformRelease campaign = PlatformReleaseHome.findByPrimaryKeyWithSteps( _nSiteCampaignId );
        if ( campaign == null || !isAuthorized( AdminUserService.getAdminUser( request ), campaign ) )
        {
            _site = null;
            return null;
        }

        return campaign;
    }

    /**
     * Redirects to the preparation of the step in session, or to the campaign list when none is loaded.
     *
     * @param request
     *            the request
     * @return the redirection
     */
    private String redirectToPreparedStep( HttpServletRequest request )
    {
        if ( _site == null )
        {
            return redirectView( request, VIEW_MANAGE_PLATFORM_RELEASES );
        }

        return redirectToStep( request, VIEW_PREPARE_STEP, _nSiteCampaignId, _nSiteStepNumber );
    }

    /**
     * Redirects to a view of a step of a campaign.
     *
     * @param request
     *            the request
     * @param strView
     *            the view
     * @param nIdCampaign
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param strExtraParameters
     *            optional name and value pairs of extra parameters
     * @return the redirection
     */
    private String redirectToStep( HttpServletRequest request, String strView, int nIdCampaign, int nStepNumber, String... strExtraParameters )
    {
        UrlItem url = new UrlItem( getViewUrl( strView ) );
        url.addParameter( PARAMETER_ID, nIdCampaign );
        url.addParameter( PARAMETER_STEP, nStepNumber );
        for ( int i = 0; i + 1 < strExtraParameters.length; i += 2 )
        {
            url.addParameter( strExtraParameters [i], strExtraParameters [i + 1] );
        }

        return redirect( request, url.getUrl( ) );
    }

    /**
     * Loads the campaign of the request, with its steps.
     *
     * @param request
     *            the request
     * @return the campaign, or null
     */
    private static PlatformRelease getCampaign( HttpServletRequest request )
    {
        int nId = getCampaignId( request );

        return nId > 0 ? PlatformReleaseHome.findByPrimaryKeyWithSteps( nId ) : null;
    }

    /**
     * Returns the campaign id of the request.
     *
     * @param request
     *            the request
     * @return the id, -1 if absent
     */
    private static int getCampaignId( HttpServletRequest request )
    {
        return ReleaserUtils.convertStringToInt( request.getParameter( PARAMETER_ID ) );
    }

    /**
     * Returns the step number of the request.
     *
     * @param request
     *            the request
     * @return the step number, -1 if absent
     */
    private static int getStepNumber( HttpServletRequest request )
    {
        return ReleaserUtils.convertStringToInt( request.getParameter( PARAMETER_STEP ) );
    }

    /**
     * Whether a user may manage a campaign.
     *
     * @param user
     *            the admin user
     * @param campaign
     *            the campaign, a new one to check the creation right
     * @return true if authorized
     */
    private static boolean isAuthorized( AdminUser user, PlatformRelease campaign )
    {
        return RBACService.isAuthorized( campaign, PlatformRelease.PERMISSION_MANAGE, user );
    }

    /**
     * Denies the access when the user may not manage a campaign.
     *
     * @param request
     *            the request
     * @param campaign
     *            the campaign
     * @throws AccessDeniedException
     *             if not authorized
     */
    private static void checkAuthorized( HttpServletRequest request, PlatformRelease campaign ) throws AccessDeniedException
    {
        if ( !isAuthorized( AdminUserService.getAdminUser( request ), campaign ) )
        {
            throw new AccessDeniedException( MESSAGE_ACCESS_DENIED );
        }
    }
}
