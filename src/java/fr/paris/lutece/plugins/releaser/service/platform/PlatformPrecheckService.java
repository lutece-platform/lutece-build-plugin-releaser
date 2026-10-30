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
package fr.paris.lutece.plugins.releaser.service.platform;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.paris.lutece.plugins.releaser.business.AbstractReleaserResource;
import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformPlanResource;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformRelease;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleasePlan;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStatus;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStep;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStepHome;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformResourceVerification;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepCode;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepMode;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepResult;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformVerificationCheck;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.MapperJsonUtil;
import fr.paris.lutece.plugins.releaser.util.git.GitUtils;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

/**
 * Verification of a campaign before it starts (step 0) : census of every SNAPSHOT the five steps will release, instant checks of the releaser
 * on each of them, then the plan the step pipeline verifies on the head of their branch, nothing being set nor pushed. The step 0 is stored
 * as a step row numbered 0, left out of the steps of the campaign.
 */
public final class PlatformPrecheckService
{
    /** Number of the step row holding the verification of the campaign. */
    public static final int STEP_NUMBER = 0;

    static final String CHECK_RELEASABLE = "releasable";
    static final String CHECK_CREDENTIALS = "credentials";
    static final String CHECK_RELEASE_COMMENTS = "release-comments";

    private static final String MESSAGE_CREDENTIALS_MISSING = "releaser.message.platform.precheck.credentialsMissing";
    private static final String MESSAGE_CREDENTIALS_REFUSED = "releaser.message.platform.precheck.credentialsRefused";
    private static final String MESSAGE_STEP_UNAVAILABLE = "releaser.message.platform.precheck.stepUnavailable";
    private static final String MESSAGE_CENSUS_SUMMARY = "releaser.message.platform.precheck.censusSummary";
    private static final String TYPE_POM = "pom";
    private static final String COORDINATES_SEPARATOR = ":";
    private static final String LINE_SEPARATOR = "\n";

    /**
     * Private constructor
     */
    private PlatformPrecheckService( )
    {
    }

    /**
     * Returns the step 0 of a campaign, created when missing (campaigns created before the step 0).
     *
     * @param campaign
     *            the campaign
     * @return the step 0
     */
    public static PlatformReleaseStep getPrecheckStep( PlatformRelease campaign )
    {
        PlatformReleaseStep step = PlatformReleaseStepHome.findByPlatformReleaseAndNumber( campaign.getId( ), STEP_NUMBER );
        if ( step == null )
        {
            step = new PlatformReleaseStep( );
            step.setIdPlatformRelease( campaign.getId( ) );
            step.setStepNumber( STEP_NUMBER );
            step.setStatus( PlatformReleaseStatus.READY );
            PlatformReleaseStepHome.create( step );
        }
        campaign.setPrecheckStep( step );

        return step;
    }

    /**
     * Takes the census of the SNAPSHOT of the campaign : loads the aggregate of every step, runs the instant checks, then stores the result and
     * the plan of the step pipeline in the step 0. A step that cannot be loaded is reported and skipped.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param user
     *            the releaser user (credentials)
     * @param locale
     *            the locale of the messages
     * @return the result stored in the step 0
     * @throws IOException
     *             if the result or the plan cannot be serialized
     */
    public static PlatformStepResult runCensus( PlatformRelease campaign, ReleaserUser user, Locale locale ) throws IOException
    {
        Map<String, PlatformResourceVerification> mapItems = new LinkedHashMap<>( );
        Map<String, PlatformPlanResource> mapResources = new LinkedHashMap<>( );
        List<String> listProblems = new ArrayList<>( );
        for ( PlatformStepCode code : PlatformStepCode.values( ) )
        {
            Site site;
            try
            {
                site = PlatformReleaseService.loadStepAggregate( campaign, code.getStepNumber( ), user, locale );
            }
            catch( AppException e )
            {
                listProblems.add( I18nService.getLocalizedString( MESSAGE_STEP_UNAVAILABLE, new String [ ] {
                        Integer.toString( code.getStepNumber( ) ), e.getMessage( )
                }, locale ) );
                continue;
            }
            String strPermissionError = PlatformReleaseService.checkLaunchPermissions( site, user );
            if ( strPermissionError != null )
            {
                listProblems.add( I18nService.getLocalizedString( MESSAGE_STEP_UNAVAILABLE, new String [ ] {
                        Integer.toString( code.getStepNumber( ) ), strPermissionError
                }, locale ) );
            }
            addStep( mapItems, mapResources, code, site, getCredentialMessages( site, user, locale ) );
        }

        Timestamp now = new Timestamp( System.currentTimeMillis( ) );
        PlatformStepResult result = new PlatformStepResult( );
        result.setVerifications( mapItems );
        result.setVerificationDate( now.toString( ) );
        Map<String, Integer> mapCounts = PlatformReleaseService.countVerificationStatuses( getStatuses( mapItems ) );
        List<String> listReport = new ArrayList<>( );
        listReport.add( I18nService.getLocalizedString( MESSAGE_CENSUS_SUMMARY, new String [ ] {
                Integer.toString( mapItems.size( ) ), String.valueOf( mapCounts.get( PlatformVerificationCheck.STATUS_TO_FIX ) ),
                String.valueOf( mapCounts.get( PlatformVerificationCheck.STATUS_WARNING ) )
        }, locale ) );
        listReport.addAll( listProblems );
        result.setReport( String.join( LINE_SEPARATOR, listReport ) );

        PlatformReleaseStep step = getPrecheckStep( campaign );
        step.setPlanJson( MapperJsonUtil.getJson( buildPlan( campaign, mapItems, mapResources ) ) );
        step.setResultJson( MapperJsonUtil.getJson( result ) );
        step.setStatus( PlatformReleaseStatus.READY );
        step.setJenkinsBuildUrl( null );
        step.setJenkinsBuildNumber( 0 );
        step.setJenkinsResult( null );
        step.setDateBegin( now );
        step.setDateEnd( now );
        PlatformReleaseStepHome.update( step );

        return result;
    }

    /**
     * Adds the SNAPSHOT of a loaded step to the census : the aggregate when the step releases it, then every SNAPSHOT component. A resource
     * already met in a previous step is kept with its first step.
     *
     * @param mapItems
     *            the census, by "groupId:artifactId"
     * @param mapResources
     *            the plan resources, by "groupId:artifactId"
     * @param code
     *            the step
     * @param site
     *            the transient site of the step
     * @param mapCredentialMessages
     *            the credential problem of each repository type of the step
     */
    static void addStep( Map<String, PlatformResourceVerification> mapItems, Map<String, PlatformPlanResource> mapResources, PlatformStepCode code,
            Site site, Map<RepositoryType, String> mapCredentialMessages )
    {
        if ( code.isAggregateReleased( ) && Version.isSnapshot( site.getVersion( ) ) )
        {
            addItem( mapItems, mapResources, code.getStepNumber( ), site.getGroupId( ) + COORDINATES_SEPARATOR + site.getArtifactId( ), site.getVersion( ),
                    site, toAggregateResource( code, site ), mapCredentialMessages.get( site.getRepoType( ) ) );
        }
        for ( Component component : site.getComponents( ) )
        {
            if ( isCandidate( component ) )
            {
                addItem( mapItems, mapResources, code.getStepNumber( ), PlatformReleaseService.getCoordinates( component ), component.getCurrentVersion( ),
                        component, PlatformReleaseService.toResource( component ), mapCredentialMessages.get( component.getRepoType( ) ) );
            }
        }
    }

    /**
     * Whether a component of a step belongs to the census : a SNAPSHOT that is not a theme, released or not by the person, blocked or not.
     *
     * @param component
     *            the component
     * @return true for a SNAPSHOT to verify
     */
    static boolean isCandidate( Component component )
    {
        return component.isSnapshotVersion( ) && !component.isTheme( );
    }

    /**
     * Adds a resource to the census with the result of the instant checks : the blocking comment and the credential problem are to fix, the
     * other comments are warnings.
     *
     * @param mapItems
     *            the census
     * @param mapResources
     *            the plan resources
     * @param nStep
     *            the step that releases the resource
     * @param strCoordinates
     *            the "groupId:artifactId" of the resource
     * @param strVersion
     *            the SNAPSHOT version of the resource
     * @param resource
     *            the resource and its comments
     * @param planResource
     *            the resource sent to the pipeline
     * @param strCredentialProblem
     *            the credential problem of its repository type, null when none
     */
    private static void addItem( Map<String, PlatformResourceVerification> mapItems, Map<String, PlatformPlanResource> mapResources, int nStep,
            String strCoordinates, String strVersion, AbstractReleaserResource resource, PlatformPlanResource planResource, String strCredentialProblem )
    {
        if ( mapItems.containsKey( strCoordinates ) )
        {
            return;
        }
        PlatformResourceVerification item = new PlatformResourceVerification( );
        item.setStep( nStep );
        item.setVersion( strVersion );
        String strBlocking = resource.getBlockingReleaseComment( );
        if ( strBlocking != null )
        {
            item.getChecks( ).add( newCheck( CHECK_RELEASABLE, PlatformVerificationCheck.STATUS_TO_FIX, strBlocking ) );
        }
        if ( strCredentialProblem != null )
        {
            item.getChecks( ).add( newCheck( CHECK_CREDENTIALS, PlatformVerificationCheck.STATUS_TO_FIX, strCredentialProblem ) );
        }
        List<String> listComments = new ArrayList<>( resource.getReleaseComments( ) );
        listComments.remove( strBlocking );
        if ( !listComments.isEmpty( ) )
        {
            item.getChecks( ).add( newCheck( CHECK_RELEASE_COMMENTS, PlatformVerificationCheck.STATUS_WARNING, String.join( LINE_SEPARATOR, listComments ) ) );
        }
        item.setStatus( getStatus( item.getChecks( ) ) );
        mapItems.put( strCoordinates, item );
        mapResources.put( strCoordinates, planResource );
    }

    /**
     * Builds a check of the census.
     *
     * @param strCheck
     *            the check name
     * @param strStatus
     *            the status
     * @param strDetails
     *            the details
     * @return the check
     */
    private static PlatformVerificationCheck newCheck( String strCheck, String strStatus, String strDetails )
    {
        PlatformVerificationCheck check = new PlatformVerificationCheck( );
        check.setCheck( strCheck );
        check.setStatus( strStatus );
        check.setDetails( strDetails );

        return check;
    }

    /**
     * Returns the status of a resource of the census : to fix or warning after the instant checks, otherwise not verified until the pipeline
     * builds it.
     *
     * @param listChecks
     *            the instant checks
     * @return the status
     */
    static String getStatus( List<PlatformVerificationCheck> listChecks )
    {
        String strStatus = PlatformReleaseService.VERIFICATION_NOT_VERIFIED;
        for ( PlatformVerificationCheck check : listChecks )
        {
            if ( PlatformVerificationCheck.STATUS_TO_FIX.equals( check.getStatus( ) ) )
            {
                return PlatformVerificationCheck.STATUS_TO_FIX;
            }
            if ( PlatformVerificationCheck.STATUS_WARNING.equals( check.getStatus( ) ) )
            {
                strStatus = PlatformVerificationCheck.STATUS_WARNING;
            }
        }

        return strStatus;
    }

    /**
     * Builds the plan of the step 0 : every resource of the census not already to fix, verified on the head of its branch, and the coordinates
     * of the whole census for the SNAPSHOT outside the campaign.
     *
     * @param campaign
     *            the campaign
     * @param mapItems
     *            the census
     * @param mapResources
     *            the plan resources
     * @return the plan
     */
    static PlatformReleasePlan buildPlan( PlatformRelease campaign, Map<String, PlatformResourceVerification> mapItems,
            Map<String, PlatformPlanResource> mapResources )
    {
        PlatformReleasePlan plan = new PlatformReleasePlan( );
        plan.setCampaignId( campaign.getId( ) );
        plan.setCampaignName( campaign.getName( ) );
        plan.setStep( STEP_NUMBER );
        plan.setReleaseType( campaign.getReleaseType( ) );
        plan.setCoreMajor( campaign.getCoreMajor( ) );
        plan.setMode( PlatformStepMode.PRECHECK );
        for ( Map.Entry<String, PlatformResourceVerification> entry : mapItems.entrySet( ) )
        {
            if ( !PlatformVerificationCheck.STATUS_TO_FIX.equals( entry.getValue( ).getStatus( ) ) )
            {
                plan.getComponents( ).add( mapResources.get( entry.getKey( ) ) );
            }
        }
        plan.setCampaignCoordinates( new ArrayList<>( mapItems.keySet( ) ) );

        return plan;
    }

    /**
     * Converts the aggregate of a step to a plan resource, verified on the head of its branch.
     *
     * @param code
     *            the step
     * @param site
     *            the transient site of the step
     * @return the resource
     */
    private static PlatformPlanResource toAggregateResource( PlatformStepCode code, Site site )
    {
        PlatformPlanResource resource = new PlatformPlanResource( );
        resource.setGroupId( site.getGroupId( ) );
        resource.setArtifactId( site.getArtifactId( ) );
        resource.setType( code == PlatformStepCode.CORE ? ConstanteUtils.DEPENDENCY_TYPE_LUTECE_CORE : TYPE_POM );
        resource.setScmUrl( GitUtils.getRepoUrl( site.getScmUrl( ) ) );
        resource.setBranch( site.getBranchReleaseFrom( ) );
        resource.setCurrentVersion( site.getVersion( ) );

        return resource;
    }

    /**
     * Returns the message of each credential problem of a step, by repository type.
     *
     * @param site
     *            the transient site of the step
     * @param user
     *            the releaser user
     * @param locale
     *            the locale of the messages
     * @return the messages
     */
    private static Map<RepositoryType, String> getCredentialMessages( Site site, ReleaserUser user, Locale locale )
    {
        Map<RepositoryType, String> mapMessages = new LinkedHashMap<>( );
        for ( Map.Entry<RepositoryType, PlatformReleaseService.CredentialProblem> entry : PlatformReleaseService.getCredentialProblems( site, user )
                .entrySet( ) )
        {
            String strKey = entry.getValue( ) == PlatformReleaseService.CredentialProblem.MISSING ? MESSAGE_CREDENTIALS_MISSING : MESSAGE_CREDENTIALS_REFUSED;
            mapMessages.put( entry.getKey( ), I18nService.getLocalizedString( strKey, new String [ ] {
                    entry.getKey( ).name( )
            }, locale ) );
        }

        return mapMessages;
    }

    /**
     * Whether the step 0 of a campaign can be launched : it is not running.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @return true when it can be launched
     */
    public static boolean canLaunch( PlatformRelease campaign )
    {
        PlatformReleaseStep step = campaign.getPrecheckStep( );

        return step == null || step.getStatus( ) != PlatformReleaseStatus.RUNNING;
    }

    /**
     * Launches the step 0 : takes the census, then sends the resources not already to fix to the step pipeline.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param user
     *            the releaser user
     * @param locale
     *            the locale of the messages
     * @return true when the pipeline was launched, false when the census left nothing to send
     * @throws IOException
     *             if the result or the plan cannot be serialized
     */
    public static boolean launch( PlatformRelease campaign, ReleaserUser user, Locale locale ) throws IOException
    {
        if ( !canLaunch( campaign ) )
        {
            throw new AppException( "Platform step " + STEP_NUMBER + " of campaign " + campaign.getId( ) + " is already running" );
        }
        runCensus( campaign, user, locale );
        PlatformReleasePlan plan = MapperJsonUtil.parse( campaign.getPrecheckStep( ).getPlanJson( ), PlatformReleasePlan.class );
        if ( plan.getComponents( ).isEmpty( ) )
        {
            return false;
        }
        PlatformReleaseService.launchStep( campaign, STEP_NUMBER, plan, user, locale );

        return true;
    }

    /**
     * Adds the checks of the pipeline to the instant checks of the census, resource by resource : the step of the census is kept and the
     * status is the worst of both.
     *
     * @param mapItems
     *            the census, updated
     * @param mapPipeline
     *            the verifications of the pipeline, by "groupId:artifactId"
     */
    static void mergeVerifications( Map<String, PlatformResourceVerification> mapItems, Map<String, PlatformResourceVerification> mapPipeline )
    {
        for ( Map.Entry<String, PlatformResourceVerification> entry : mapPipeline.entrySet( ) )
        {
            PlatformResourceVerification item = mapItems.get( entry.getKey( ) );
            PlatformResourceVerification verification = entry.getValue( );
            if ( item != null )
            {
                List<PlatformVerificationCheck> listChecks = new ArrayList<>( item.getChecks( ) );
                listChecks.addAll( verification.getChecks( ) );
                verification.setChecks( listChecks );
                verification.setStep( item.getStep( ) );
                String strStatus = getStatus( listChecks );
                verification.setStatus( PlatformReleaseService.VERIFICATION_NOT_VERIFIED.equals( strStatus ) ? verification.getStatus( ) : strStatus );
            }
            mapItems.put( entry.getKey( ), verification );
        }
    }

    /**
     * Puts the report of the pipeline after the report of the census.
     *
     * @param strCensusReport
     *            the report of the census, may be null
     * @param strPipelineReport
     *            the report of the pipeline
     * @return the report of the step 0
     */
    static String mergeReports( String strCensusReport, String strPipelineReport )
    {
        return StringUtils.isBlank( strCensusReport ) ? strPipelineReport : strCensusReport + LINE_SEPARATOR + LINE_SEPARATOR + strPipelineReport;
    }

    /**
     * Returns the resources of the plan of the step 0, by "groupId:artifactId" : their branch is shown with the result.
     *
     * @param step
     *            the step 0, may be null
     * @return the resources, empty when none
     */
    public static Map<String, PlatformPlanResource> getPlanResources( PlatformReleaseStep step )
    {
        Map<String, PlatformPlanResource> mapResources = new LinkedHashMap<>( );
        if ( step == null || StringUtils.isBlank( step.getPlanJson( ) ) )
        {
            return mapResources;
        }
        try
        {
            for ( PlatformPlanResource resource : MapperJsonUtil.parse( step.getPlanJson( ), PlatformReleasePlan.class ).getComponents( ) )
            {
                mapResources.put( resource.getCoordinates( ), resource );
            }
        }
        catch( IOException e )
        {
            AppLogService.error( "Releaser : unreadable plan of the step 0 of campaign " + step.getIdPlatformRelease( ), e );
        }

        return mapResources;
    }

    /**
     * Returns the status of every resource of the census.
     *
     * @param mapItems
     *            the census
     * @return the status by "groupId:artifactId"
     */
    static Map<String, String> getStatuses( Map<String, PlatformResourceVerification> mapItems )
    {
        Map<String, String> mapStatuses = new LinkedHashMap<>( );
        for ( Map.Entry<String, PlatformResourceVerification> entry : mapItems.entrySet( ) )
        {
            mapStatuses.put( entry.getKey( ), entry.getValue( ).getStatus( ) );
        }

        return mapStatuses;
    }
}
