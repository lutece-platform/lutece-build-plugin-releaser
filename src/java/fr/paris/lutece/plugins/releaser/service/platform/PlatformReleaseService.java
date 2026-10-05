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

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.releaser.business.AbstractReleaserResource;
import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser.Credential;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformPlanResource;
import fr.paris.lutece.plugins.releaser.business.platform.PomVersionDecision;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformRelease;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseHome;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleasePlan;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStatus;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStep;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStepHome;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseType;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepCode;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepDefinition;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepDefinitionHome;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepResult;
import fr.paris.lutece.plugins.releaser.service.MavenRepoComponentInfoProvider;
import fr.paris.lutece.plugins.releaser.service.ReleasePreparationService;
import fr.paris.lutece.plugins.releaser.service.BugtrackerService;
import fr.paris.lutece.plugins.releaser.service.WorkflowReleaseContextService;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.CVSFactoryService;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.IVCSResourceService;
import fr.paris.lutece.plugins.releaser.util.MapperJsonUtil;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.file.FileUtils;
import fr.paris.lutece.plugins.releaser.util.git.GitUtils;
import fr.paris.lutece.plugins.releaser.util.pom.PomParser;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import fr.paris.lutece.plugins.releaser.util.version.VersionParsingException;
import fr.paris.lutece.plugins.releaser.util.version.VersionUtils;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * Release of the Lutece platform, step by step : campaign lifecycle, preparation of a step in a transient {@link Site} (aggregate POM, components,
 * target versions by release type), propagation of the released versions to the next steps, plan sent to Jenkins, CSV export.
 */
public final class PlatformReleaseService
{
    private static final String MESSAGE_UNKNOWN_GROUP_ID = "releaser.message.platform.unknownGroupId";
    private static final String MESSAGE_ALREADY_RELEASED = "releaser.message.platform.alreadyReleased";
    private static final String MESSAGE_BOM_NOT_FOUND = "releaser.message.platform.bomNotFound";
    private static final String MESSAGE_BRANCH_CREDENTIALS_MISSING = "releaser.message.platform.branchCredentialsMissing";
    private static final String MESSAGE_BRANCH_POM_UNREADABLE = "releaser.message.platform.branchPomUnreadable";
    private static final String MESSAGE_BRANCH_VERSION_MISMATCH = "releaser.message.platform.branchVersionMismatch";
    private static final String MESSAGE_BRANCH_PARENT_MISMATCH = "releaser.message.platform.branchParentMismatch";
    private static final String MESSAGE_BLOCKED_COMPONENTS = "releaser.message.platform.blockedComponents";
    private static final String MESSAGE_SNAPSHOT_UNKNOWN_IN_NEXUS = "releaser.message.platform.snapshotUnknownInNexus";
    private static final String MESSAGE_CREDENTIALS_REQUIRED = "releaser.message.platform.credentialsRequired";
    private static final String MESSAGE_THEME_NOT_RELEASABLE = "releaser.message.themeNotReleasable";
    private static final String MESSAGE_AGGREGATE_CORE_MISMATCH = "releaser.message.platform.aggregateCoreMismatch";
    private static final String MESSAGE_AGGREGATE_BLOCKED = "releaser.message.platform.aggregateBlocked";
    private static final String MESSAGE_MASTER_BRANCH_MISSING = "releaser.message.platform.masterBranchMissing";
    private static final String MESSAGE_REMOTE_BRANCHES_UNAVAILABLE = "releaser.message.platform.remoteBranchesUnavailable";
    private static final String MESSAGE_POM_VERSION_MISMATCH = "releaser.message.platform.pomVersionMismatch";
    private static final String MESSAGE_POM_VERSION_WRONG_CORE = "releaser.message.platform.pomVersionWrongCore";
    private static final String MESSAGE_POM_VERSION_WILL_BE_SET = "releaser.message.platform.pomVersionWillBeSet";
    private static final String MESSAGE_POM_VERSION_LATEST_DIFFERS = "releaser.message.platform.pomVersionLatestDiffers";
    private static final String MESSAGE_POM_VERSION_KEPT = "releaser.message.platform.pomVersionKept";
    private static final String MESSAGE_POM_VERSION_UNKNOWN = "releaser.message.platform.pomVersionUnknown";

    private static final String POM_FILE = "pom.xml";
    private static final String LATEST_SNAPSHOT_KEY = "latest";
    private static final String PARENT_KEY = "parent";
    private static final String CORE_VERSION_KEY = "coreversion";
    private static final String PARENT_VERSION_KEY = "parentversion";
    private static final String POM_VERSION_KEEP = "keep";
    private static final String GROUP_ID_CORE = "fr.paris.lutece";
    private static final String GLOBAL_POM_ARTIFACT_ID = "lutece-global-pom";
    private static final String GROUP_ID_TOOLS = "fr.paris.lutece.tools";

    private static final String TYPE_POM = "pom";
    private static final int JENKINS_RESULT_MAX_LENGTH = 255;
    private static final String RESULT_INTERRUPTED_WITHOUT_BUILD = "ERROR : follow-up interrupted by a restart before the build was known, prepare the step again";
    private static final String BUGTRACKER_REPORT_HEADER = "\n=== Redmine ===\n";
    private static final String MESSAGE_BRANCH_OVERRIDE_RESET = "releaser.message.platform.branchOverrideReset";
    private static final String COORDINATES_SEPARATOR = ":";
    private static final String ALIASES_SEPARATOR = ",";
    private static final String DATA_KEY_SEPARATOR = "_";
    private static final String CORE_PROPERTY_KEY = "core";
    private static final String STEP_BRANCH_KEY = "branch";

    private static final String DRY_RUN_RESULT_PREFIX = "DRY RUN ";

    private static final String CSV_SEPARATOR = ";";
    private static final String CSV_LINE_SEPARATOR = "\n";
    private static final String CSV_HEADER = "étape;groupId;artifactId;versionAvant;versionCible;nextSnapshot;branche;parentAvant;parentCible;releaseStatus;statut";
    private static final String CSV_RELEASED = "RELEASED";
    private static final String CSV_NOT_RELEASED = "NOT_RELEASED";
    private static final String CSV_STATUS_PUBLISHED = "PUBLISHED";
    private static final String CSV_STATUS_POM_VERSIONS_UPDATED = "POM_VERSIONS_UPDATED";
    private static final String CSV_STATUS_POM_NOT_UPDATED = "POM_NOT_UPDATED";
    private static final String CSV_STATUS_SIMULATED = "SIMULATED";
    private static final String CSV_STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String CSV_STATUS_FAILED = "FAILED";
    private static final String CSV_STATUS_ROLLED_BACK = "ROLLED_BACK";
    private static final String CSV_STATUS_NOT_PROCESSED = "NOT_PROCESSED";
    private static final String CSV_STATUS_NOT_SENT = "NOT_SENT";

    private static final String PREFIX_LIBRARY = "library-";
    private static final String PREFIX_PLUGIN = "plugin-";
    private static final String PREFIX_MODULE = "module-";
    private static final int ORDER_LIBRARY = 0;
    private static final int ORDER_OTHER = 1;
    private static final int ORDER_PLUGIN = 2;
    private static final int ORDER_MODULE = 3;

    /**
     * Utility class.
     */
    private PlatformReleaseService( )
    {
    }

    /**
     * Creates a campaign with its 5 steps : the first one ready, the others to do.
     *
     * @param strName
     *            the campaign name
     * @param releaseType
     *            the release type (stable, beta, RC)
     * @param nCoreMajor
     *            the core version (7 or 8)
     * @param strUserName
     *            the creator
     * @return the created campaign, with its steps
     */
    public static PlatformRelease createPlatformRelease( String strName, PlatformReleaseType releaseType, int nCoreMajor, String strUserName )
    {
        Timestamp now = new Timestamp( System.currentTimeMillis( ) );

        PlatformRelease campaign = new PlatformRelease( );
        campaign.setName( strName );
        campaign.setReleaseType( releaseType );
        campaign.setCoreMajor( nCoreMajor );
        campaign.setUserName( strUserName );
        campaign.setCurrentStep( 1 );
        campaign.setStatus( PlatformReleaseStatus.READY );
        campaign.setDateCreation( now );
        campaign.setDateUpdate( now );
        PlatformReleaseHome.create( campaign );

        for ( PlatformStepCode code : PlatformStepCode.values( ) )
        {
            PlatformReleaseStep step = new PlatformReleaseStep( );
            step.setIdPlatformRelease( campaign.getId( ) );
            step.setStepNumber( code.getStepNumber( ) );
            step.setStatus( code.getStepNumber( ) == 1 ? PlatformReleaseStatus.READY : PlatformReleaseStatus.TODO );
            PlatformReleaseStepHome.create( step );
            campaign.getSteps( ).add( step );
        }

        return campaign;
    }

    /**
     * Removes a campaign, its steps and the "to be released" flags of its components.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     */
    public static void removePlatformRelease( int nIdPlatformRelease )
    {
        DatastoreService.removeDataByPrefix( getProjectDataKeyPrefix( nIdPlatformRelease ) );
        PlatformReleaseHome.remove( nIdPlatformRelease );
        String strBasePath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_SITE_BASE_PAH );
        for ( PlatformStepCode code : PlatformStepCode.values( ) )
        {
            org.apache.commons.io.FileUtils.deleteQuietly( new File( strBasePath, getLocalDirectoryName( nIdPlatformRelease, code ) ) );
        }
    }

    /**
     * Name of the local directory holding the clone of the aggregate of a step, one per campaign : two campaigns prepared at the same time
     * (core 7 and core 8, two people) must not erase each other's clone.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param code
     *            the step code
     * @return the directory name
     */
    private static String getLocalDirectoryName( int nIdPlatformRelease, PlatformStepCode code )
    {
        return ConstanteUtils.CONSTANTE_PLATFORM_LOCAL_PATH_PREFIX + nIdPlatformRelease + DATA_KEY_SEPARATOR + code.name( ).toLowerCase( );
    }

    /**
     * Returns a step of a campaign.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @return the step, or null if not found
     */
    public static PlatformReleaseStep getStep( PlatformRelease campaign, int nStepNumber )
    {
        for ( PlatformReleaseStep step : campaign.getSteps( ) )
        {
            if ( step.getStepNumber( ) == nStepNumber )
            {
                return step;
            }
        }

        return null;
    }

    /**
     * Whether a step can be prepared : the first step always, the others once the previous step is completed (success or skipped) and the step
     * itself is not already running or done ; the last step also needs the export of the campaign to have been checked.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @return true if the step can be prepared
     */
    public static boolean canPrepareStep( PlatformRelease campaign, int nStepNumber )
    {
        PlatformReleaseStep step = getStep( campaign, nStepNumber );
        if ( step == null || step.getStatus( ) == PlatformReleaseStatus.RUNNING || step.getStatus( ).isCompleted( ) )
        {
            return false;
        }

        if ( nStepNumber == 1 )
        {
            return true;
        }

        if ( nStepNumber == PlatformStepCode.PLATFORM_STARTERS.getStepNumber( ) && !campaign.isExportVerified( ) )
        {
            return false;
        }

        PlatformReleaseStep previous = getStep( campaign, nStepNumber - 1 );

        return previous != null && previous.getStatus( ).isCompleted( );
    }

    /**
     * Whether a step can be skipped : global-pom, site-pom and core may have nothing to release ; the platform plugins and starters steps
     * are the purpose of the campaign and can never be skipped.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @return true if the step can be skipped
     */
    public static boolean canSkipStep( PlatformRelease campaign, int nStepNumber )
    {
        return nStepNumber <= PlatformStepCode.CORE.getStepNumber( ) && canPrepareStep( campaign, nStepNumber );
    }

    /**
     * Skips a step and unlocks the next one.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     */
    public static void skipStep( PlatformRelease campaign, int nStepNumber )
    {
        if ( !canSkipStep( campaign, nStepNumber ) )
        {
            throw new AppException( "Platform step " + nStepNumber + " cannot be skipped" );
        }

        closeStep( campaign, getStep( campaign, nStepNumber ), PlatformReleaseStatus.SKIPPED );
    }

    /**
     * Launches a step : records the plan, marks the step running and hands the Jenkins dialog over to a background task of the release thread
     * pool.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @param plan
     *            the plan to send
     * @param user
     *            the releaser user, whose credentials are sent to the pipeline : it clones and pushes with them
     * @throws IOException
     *             if the plan cannot be serialized
     */
    public static void launchStep( PlatformRelease campaign, int nStepNumber, PlatformReleasePlan plan, ReleaserUser user ) throws IOException
    {
        if ( !canPrepareStep( campaign, nStepNumber ) )
        {
            throw new AppException( "Platform step " + nStepNumber + " cannot be launched" );
        }
        checkPlanCredentials( plan, user );

        startStep( campaign, getStep( campaign, nStepNumber ), plan );
        WorkflowReleaseContextService.getService( )
                .execute( new PlatformStepTask( campaign.getId( ), nStepNumber, plan, PlatformStepTask.buildCredentialParameters( user ) ) );
    }

    /**
     * Refuses a plan when the user gave no credentials for the host of one of its resources : the pipeline would fail at the first clone or
     * push of that host.
     *
     * @param plan
     *            the plan
     * @param user
     *            the releaser user, null when not authenticated
     */
    private static void checkPlanCredentials( PlatformReleasePlan plan, ReleaserUser user )
    {
        List<PlatformPlanResource> listResources = new ArrayList<>( plan.getComponents( ) );
        if ( plan.getAggregate( ) != null )
        {
            listResources.add( plan.getAggregate( ) );
        }
        for ( PlatformPlanResource resource : listResources )
        {
            Site host = new Site( );
            host.setScmUrl( resource.getScmUrl( ) );
            RepositoryType type = host.getRepoType( );
            if ( type != null && ( user == null || user.getCredential( type ) == null ) )
            {
                throw new AppException( I18nService.getLocalizedString( MESSAGE_CREDENTIALS_REQUIRED, new String [ ] {
                        type.name( ), resource.getArtifactId( )
                }, Locale.getDefault( ) ) );
            }
        }
    }

    /**
     * Checks, before a real launch, that the master branch each released resource will be merged into exists on its repository : the
     * pipeline merges after the deploy, a missing branch would leave the artifacts published and the build failed without rollback. The
     * branches of a repository are listed once.
     *
     * @param plan
     *            the plan about to be sent
     * @param user
     *            the releaser user, for the repository credentials
     * @param locale
     *            the locale of the message
     * @return the message refusing the launch, null when every master branch exists
     */
    public static String checkMasterBranches( PlatformReleasePlan plan, ReleaserUser user, Locale locale )
    {
        List<PlatformPlanResource> listResources = new ArrayList<>( plan.getComponents( ) );
        if ( plan.getAggregate( ) != null )
        {
            listResources.add( plan.getAggregate( ) );
        }
        Map<String, List<String>> mapBranches = new HashMap<>( );
        for ( PlatformPlanResource resource : listResources )
        {
            if ( StringUtils.isBlank( resource.getMasterBranch( ) ) || StringUtils.isBlank( resource.getScmUrl( ) ) )
            {
                continue;
            }
            StringBuilder sbError = new StringBuilder( );
            List<String> listBranches = mapBranches.computeIfAbsent( resource.getScmUrl( ), url -> listRemoteBranches( url, user, sbError ) );
            if ( sbError.length( ) > 0 )
            {
                return I18nService.getLocalizedString( MESSAGE_REMOTE_BRANCHES_UNAVAILABLE, new String [ ] {
                        resource.getArtifactId( ), sbError.toString( )
                }, locale );
            }
            if ( !listBranches.contains( resource.getMasterBranch( ) ) )
            {
                return I18nService.getLocalizedString( MESSAGE_MASTER_BRANCH_MISSING, new String [ ] {
                        resource.getMasterBranch( ), resource.getArtifactId( )
                }, locale );
            }
        }

        return null;
    }

    /**
     * Lists the branches of a repository with the credentials of the user for its host.
     *
     * @param strScmUrl
     *            the repository URL
     * @param user
     *            the releaser user
     * @param sbError
     *            receives the error message when the listing fails
     * @return the branch names, empty on failure
     */
    private static List<String> listRemoteBranches( String strScmUrl, ReleaserUser user, StringBuilder sbError )
    {
        Site host = new Site( );
        host.setScmUrl( strScmUrl );
        RepositoryType type = host.getRepoType( );
        Credential credential = ( user != null && type != null ) ? user.getCredential( type ) : null;

        return GitUtils.lsRemoteBranches( strScmUrl, credential != null ? credential.getLogin( ) : null, credential != null ? credential.getPassword( ) : null,
                sbError );
    }

    /**
     * Checks, before a real launch, that the user may push on the GitHub repositories the step will touch : the aggregate, always committed,
     * and every component to release. The pipeline pushes with the credentials of the user, this check saves a failed build and its rollback.
     *
     * @param site
     *            the transient site of the step
     * @param user
     *            the releaser user
     * @return the message refusing the launch, null when every push is allowed
     */
    public static String checkLaunchPermissions( Site site, ReleaserUser user )
    {
        String strError = ReleaserUtils.checkGithubWritePermission( site, user );
        for ( Component component : site.getComponents( ) )
        {
            if ( strError == null && component.shouldBeReleased( ) )
            {
                strError = ReleaserUtils.checkGithubWritePermission( component, user );
            }
        }

        return strError;
    }

    /**
     * Records the start of a step : the plan sent to Jenkins, the step and the campaign running.
     *
     * @param campaign
     *            the campaign
     * @param step
     *            the step
     * @param plan
     *            the plan sent
     * @throws IOException
     *             if the plan cannot be serialized
     */
    public static void startStep( PlatformRelease campaign, PlatformReleaseStep step, PlatformReleasePlan plan ) throws IOException
    {
        step.setPlanJson( MapperJsonUtil.getJson( plan ) );
        step.setJenkinsBuildUrl( null );
        step.setJenkinsBuildNumber( 0 );
        step.setJenkinsResult( null );
        step.setStatus( PlatformReleaseStatus.RUNNING );
        step.setDateBegin( new Timestamp( System.currentTimeMillis( ) ) );
        step.setDateEnd( null );
        PlatformReleaseStepHome.update( step );

        campaign.setStatus( PlatformReleaseStatus.RUNNING );
        touch( campaign );
    }

    /**
     * Records the Jenkins build of a running step, once the queue has started it.
     *
     * @param step
     *            the step
     * @param strBuildUrl
     *            the Jenkins build URL
     * @param nBuildNumber
     *            the Jenkins build number
     */
    public static void attachBuild( PlatformReleaseStep step, String strBuildUrl, int nBuildNumber )
    {
        step.setJenkinsBuildUrl( strBuildUrl );
        step.setJenkinsBuildNumber( nBuildNumber );
        PlatformReleaseStepHome.update( step );
    }

    /**
     * Records the end of a dry run : nothing was released, the step is ready again and keeps the Jenkins result and the report of the build
     * for information.
     *
     * @param campaign
     *            the campaign
     * @param step
     *            the step
     * @param strJenkinsResult
     *            the Jenkins build result
     * @param strReport
     *            the report of the build, null when none could be read
     * @throws IOException
     *             if the report cannot be serialized
     */
    public static void finishDryRun( PlatformRelease campaign, PlatformReleaseStep step, String strJenkinsResult, String strReport )
            throws IOException
    {
        step.setJenkinsResult( truncateResult( DRY_RUN_RESULT_PREFIX + strJenkinsResult ) );
        step.setStatus( PlatformReleaseStatus.READY );
        if ( strReport != null )
        {
            PlatformStepResult report = new PlatformStepResult( );
            report.setReport( strReport );
            step.setResultJson( MapperJsonUtil.getJson( mergeResults( parseResult( step ), report ) ) );
        }
        step.setDateEnd( new Timestamp( System.currentTimeMillis( ) ) );
        PlatformReleaseStepHome.update( step );

        campaign.setStatus( PlatformReleaseStatus.READY );
        touch( campaign );
    }

    /**
     * Fits a Jenkins result or an error message into the result column : an oversized value would make the update fail inside the error
     * handling of the task and leave the step RUNNING for ever.
     *
     * @param strResult
     *            the result
     * @return the result, abbreviated to the column length
     */
    static String truncateResult( String strResult )
    {
        return StringUtils.abbreviate( strResult, JENKINS_RESULT_MAX_LENGTH );
    }

    /**
     * Updates the bugtracker for the resources published by a build, as the component workflow does after a classic release : the Redmine
     * version of each published component is closed and the next one created, a beta or RC leaves it untouched, a component without project
     * is reported. Idempotent : a version already closed or already created is left as is. Never fails the step : the outcome is appended to
     * the report of the step. Nothing is done for a simulation nor for the platform pipeline, whose aggregate is not tracked.
     *
     * @param step
     *            the step, its result stored
     * @param plan
     *            the plan sent with the build
     * @param result
     *            the result of this build, null when none could be read
     */
    public static void updateBugtrackerVersions( PlatformReleaseStep step, PlatformReleasePlan plan, PlatformStepResult result )
    {
        if ( plan.isDryRun( ) || result == null || plan.getStepCode( ) == null || plan.getStepCode( ).getExecutor( ) != PlatformStepCode.Executor.STEP_PIPELINE )
        {
            return;
        }
        try
        {
            CommandResult commandResult = new CommandResult( );
            commandResult.setLog( new StringBuffer( ) );
            for ( PlatformPlanResource resource : plan.getComponents( ) )
            {
                String strReleased = result.getReleasedVersions( ).get( resource.getGroupId( ) + COORDINATES_SEPARATOR + resource.getArtifactId( ) );
                if ( strReleased != null )
                {
                    updateBugtrackerVersions( resource, strReleased, commandResult );
                }
            }
            if ( plan.getAggregate( ) != null && result.getAggregateVersion( ) != null )
            {
                updateBugtrackerVersions( plan.getAggregate( ), result.getAggregateVersion( ), commandResult );
            }
            if ( commandResult.getLog( ).length( ) > 0 )
            {
                PlatformStepResult stored = parseResult( step );
                if ( stored == null )
                {
                    stored = new PlatformStepResult( );
                }
                stored.setReport( StringUtils.defaultString( stored.getReport( ) ) + BUGTRACKER_REPORT_HEADER + commandResult.getLog( ) );
                step.setResultJson( MapperJsonUtil.getJson( stored ) );
                PlatformReleaseStepHome.update( step );
            }
        }
        catch( IOException | RuntimeException e )
        {
            AppLogService.error( "Releaser : bugtracker update failed after platform step " + step.getStepNumber( ) + " : " + e.getMessage( ), e );
        }
    }

    /**
     * Updates the bugtracker for one published resource, the outcome going to the log.
     *
     * @param resource
     *            the resource of the plan
     * @param strReleasedVersion
     *            the version published
     * @param commandResult
     *            the log
     */
    private static void updateBugtrackerVersions( PlatformPlanResource resource, String strReleasedVersion, CommandResult commandResult )
    {
        Component component = new Component( );
        component.setGroupId( resource.getGroupId( ) );
        component.setArtifactId( resource.getArtifactId( ) );
        component.setScmDeveloperConnection( resource.getScmUrl( ) );
        component.setCurrentVersion( resource.getCurrentVersion( ) );
        component.setTargetVersion( strReleasedVersion );
        component.setNextSnapshotVersion( resource.getNextSnapshotVersion( ) );
        commandResult.getLog( ).append( resource.getArtifactId( ) ).append( ' ' ).append( strReleasedVersion ).append( " :\n" );
        BugtrackerService.getService( ).updateComponentVersions( component, commandResult );
    }

    /**
     * Whether the build of a running step is followed by a task of this webapp. A running step that is not followed was interrupted by a
     * restart or gave up on a lost connection : it has to be rearmed.
     *
     * @param step
     *            the step
     * @return true when followed
     */
    public static boolean isStepFollowed( PlatformReleaseStep step )
    {
        return PlatformStepTask.isFollowed( step.getId( ) );
    }

    /**
     * Rearms a running step that nobody follows : its known Jenkins build is followed again until its result is recorded ; without a known
     * build the step is failed, so that it can be prepared and launched again.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @return true when something was done, false when the step is not running or is still followed
     */
    public static boolean rearmStep( PlatformRelease campaign, int nStepNumber )
    {
        PlatformReleaseStep step = getStep( campaign, nStepNumber );
        if ( step == null || step.getStatus( ) != PlatformReleaseStatus.RUNNING || isStepFollowed( step ) )
        {
            return false;
        }
        if ( StringUtils.isBlank( step.getJenkinsBuildUrl( ) ) || StringUtils.isBlank( step.getPlanJson( ) ) )
        {
            failStep( campaign, step, RESULT_INTERRUPTED_WITHOUT_BUILD );
            return true;
        }
        try
        {
            PlatformReleasePlan plan = MapperJsonUtil.parse( step.getPlanJson( ), PlatformReleasePlan.class );
            WorkflowReleaseContextService.getService( )
                    .execute( new PlatformStepTask( campaign.getId( ), nStepNumber, plan, step.getJenkinsBuildUrl( ), step.getJenkinsBuildNumber( ) ) );
        }
        catch( IOException e )
        {
            AppLogService.error( "Releaser : unreadable plan of platform step " + nStepNumber + " of campaign " + campaign.getId( ), e );
            failStep( campaign, step, RESULT_INTERRUPTED_WITHOUT_BUILD );
        }

        return true;
    }

    /**
     * Rearms every step left RUNNING by a previous run of the webapp.
     *
     * @return the number of steps rearmed
     */
    public static int rearmRunningSteps( )
    {
        int nRearmed = 0;
        for ( PlatformRelease summary : PlatformReleaseHome.getPlatformReleasesList( ) )
        {
            if ( summary.getStatus( ) != PlatformReleaseStatus.RUNNING )
            {
                continue;
            }
            PlatformRelease campaign = PlatformReleaseHome.findByPrimaryKeyWithSteps( summary.getId( ) );
            for ( PlatformReleaseStep step : campaign.getSteps( ) )
            {
                if ( rearmStep( campaign, step.getStepNumber( ) ) )
                {
                    nRearmed++;
                }
            }
        }

        return nRearmed;
    }

    /**
     * Records the success of a step and unlocks the next one. The components released by the failed attempts of the same step are kept in
     * the stored result, so that the next steps know every version released.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param step
     *            the step
     * @param result
     *            the result reported by the pipeline
     * @param strJenkinsResult
     *            the Jenkins build result
     * @throws IOException
     *             if the result cannot be serialized
     */
    public static void completeStep( PlatformRelease campaign, PlatformReleaseStep step, PlatformStepResult result, String strJenkinsResult )
            throws IOException
    {
        step.setResultJson( MapperJsonUtil.getJson( mergeResults( parseResult( step ), result ) ) );
        step.setJenkinsResult( truncateResult( strJenkinsResult ) );
        closeStep( campaign, step, PlatformReleaseStatus.SUCCESS );
    }

    /**
     * Records the failure of a step : the campaign stays on this step, which can be prepared and sent again.
     *
     * @param campaign
     *            the campaign
     * @param step
     *            the step
     * @param strJenkinsResult
     *            the Jenkins build result
     */
    public static void failStep( PlatformRelease campaign, PlatformReleaseStep step, String strJenkinsResult )
    {
        failStep( campaign, step, strJenkinsResult, null );
    }

    /**
     * Records the failure of a step with the partial report of the pipeline : the components released before the failure are stored, so that
     * the next attempt of the step does not release them again.
     *
     * @param campaign
     *            the campaign
     * @param step
     *            the step
     * @param strJenkinsResult
     *            the Jenkins build result
     * @param partialResult
     *            the components released before the failure, null when the report could not be read
     */
    public static void failStep( PlatformRelease campaign, PlatformReleaseStep step, String strJenkinsResult, PlatformStepResult partialResult )
    {
        if ( partialResult != null )
        {
            try
            {
                step.setResultJson( MapperJsonUtil.getJson( mergeResults( parseResult( step ), partialResult ) ) );
            }
            catch( IOException e )
            {
                AppLogService.error( "Releaser : unable to store the partial result of platform step " + step.getStepNumber( ), e );
            }
        }
        step.setJenkinsResult( truncateResult( strJenkinsResult ) );
        step.setStatus( PlatformReleaseStatus.FAILED );
        step.setDateEnd( new Timestamp( System.currentTimeMillis( ) ) );
        PlatformReleaseStepHome.update( step );

        campaign.setStatus( PlatformReleaseStatus.FAILED );
        touch( campaign );
    }

    /**
     * Merges the result of an attempt of a step into the result of its previous attempts : the released versions add up (the newest wins on a
     * same component), the aggregate version and the report are those of the newest attempt when it has them.
     *
     * @param previous
     *            the result of the previous attempts, null when none
     * @param next
     *            the result of the newest attempt
     * @return the merged result
     */
    static PlatformStepResult mergeResults( PlatformStepResult previous, PlatformStepResult next )
    {
        if ( previous == null )
        {
            return next;
        }

        PlatformStepResult merged = new PlatformStepResult( );
        merged.getReleasedVersions( ).putAll( previous.getReleasedVersions( ) );
        merged.getReleasedVersions( ).putAll( next.getReleasedVersions( ) );
        merged.setAggregateVersion( next.getAggregateVersion( ) != null ? next.getAggregateVersion( ) : previous.getAggregateVersion( ) );
        merged.setReport( next.getReport( ) != null ? next.getReport( ) : previous.getReport( ) );
        merged.getFailedVersions( ).putAll( next.getFailedVersions( ).isEmpty( ) ? previous.getFailedVersions( ) : next.getFailedVersions( ) );
        merged.getFailedVersions( ).keySet( ).removeAll( merged.getReleasedVersions( ).keySet( ) );
        merged.getNotProcessed( ).addAll( next.getNotProcessed( ).isEmpty( ) ? previous.getNotProcessed( ) : next.getNotProcessed( ) );
        merged.getNotProcessed( ).removeAll( merged.getReleasedVersions( ).keySet( ) );
        merged.setPomUpdated( previous.isPomUpdated( ) || next.isPomUpdated( ) );

        return merged;
    }

    /**
     * Closes a step with a completed status, moves the campaign to the next step or completes it after the last one.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param step
     *            the step
     * @param status
     *            SUCCESS or SKIPPED
     */
    private static void closeStep( PlatformRelease campaign, PlatformReleaseStep step, PlatformReleaseStatus status )
    {
        step.setStatus( status );
        step.setDateEnd( new Timestamp( System.currentTimeMillis( ) ) );
        PlatformReleaseStepHome.update( step );

        if ( step.getStepNumber( ) < PlatformStepCode.getStepCount( ) )
        {
            PlatformReleaseStep next = getStep( campaign, step.getStepNumber( ) + 1 );
            if ( next != null )
            {
                next.setStatus( PlatformReleaseStatus.READY );
                PlatformReleaseStepHome.update( next );
            }
            campaign.setCurrentStep( step.getStepNumber( ) + 1 );
            campaign.setStatus( PlatformReleaseStatus.READY );
        }
        else
        {
            campaign.setStatus( PlatformReleaseStatus.SUCCESS );
        }

        touch( campaign );
    }

    /**
     * Saves a campaign with its update date. The export verification flag is taken from the database : the copy held by a running task
     * must not overwrite a verification done meanwhile from the screen.
     *
     * @param campaign
     *            the campaign
     */
    private static void touch( PlatformRelease campaign )
    {
        PlatformRelease stored = PlatformReleaseHome.findByPrimaryKey( campaign.getId( ) );
        if ( stored != null )
        {
            campaign.setExportVerified( stored.isExportVerified( ) );
        }
        campaign.setDateUpdate( new Timestamp( System.currentTimeMillis( ) ) );
        PlatformReleaseHome.update( campaign );
    }

    /**
     * Identifies the state of a step as seen by a prepared screen : the screen must be rebuilt when the step was run meanwhile, otherwise
     * components published by a failed attempt would be sent again.
     *
     * @param step
     *            the step, null when unknown
     * @return a stamp made of the status and the end date
     */
    public static String getStepStamp( PlatformReleaseStep step )
    {
        return step == null ? "none" : step.getStatus( ) + "|" + step.getDateEnd( );
    }

    /**
     * Whether a step of the campaign is being run : a Jenkins build is in progress and the campaign must not be removed.
     *
     * @param campaign
     *            the campaign, with its steps when loaded
     * @return true when running
     */
    public static boolean isRunning( PlatformRelease campaign )
    {
        if ( campaign.getStatus( ) == PlatformReleaseStatus.RUNNING )
        {
            return true;
        }
        for ( PlatformReleaseStep step : campaign.getSteps( ) )
        {
            if ( step.getStatus( ) == PlatformReleaseStatus.RUNNING )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether the CSV export may be marked as verified : only once step 4 succeeded and while step 5 has not been run.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @return true when the verification is expected
     */
    public static boolean canVerifyExport( PlatformRelease campaign )
    {
        PlatformReleaseStep stepPlugins = getStep( campaign, PlatformStepCode.PLATFORM_PLUGINS.getStepNumber( ) );
        PlatformReleaseStep stepStarters = getStep( campaign, PlatformStepCode.PLATFORM_STARTERS.getStepNumber( ) );
        if ( stepPlugins == null || stepStarters == null || stepPlugins.getStatus( ) != PlatformReleaseStatus.SUCCESS )
        {
            return false;
        }

        return stepStarters.getStatus( ) != PlatformReleaseStatus.RUNNING && stepStarters.getStatus( ) != PlatformReleaseStatus.SUCCESS
                && stepStarters.getStatus( ) != PlatformReleaseStatus.SKIPPED;
    }

    /**
     * Returns the release type of a step : stable for the stable-only steps, the campaign type otherwise.
     *
     * @param campaign
     *            the campaign
     * @param code
     *            the step code
     * @return the release type
     */
    public static PlatformReleaseType getStepReleaseType( PlatformRelease campaign, PlatformStepCode code )
    {
        return code.isStableOnly( ) ? PlatformReleaseType.STABLE : campaign.getReleaseType( );
    }

    /**
     * Loads the aggregate of a step in a transient site : clone, POM, components according to the step source, remote informations, target
     * versions according to the release type, versions already released by the previous steps.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @param user
     *            the releaser user (credentials)
     * @param locale
     *            the locale of the comments
     * @return the transient site
     */
    public static Site loadStepAggregate( PlatformRelease campaign, int nStepNumber, ReleaserUser user, Locale locale )
    {
        PlatformStepCode code = PlatformStepCode.fromStepNumber( nStepNumber );
        PlatformStepDefinition definition = PlatformStepDefinitionHome.findByStepNumber( nStepNumber );
        if ( code == null || definition == null )
        {
            throw new AppException( "Unknown platform step " + nStepNumber );
        }

        Site site = new Site( );
        site.setArtifactId( getLocalDirectoryName( campaign.getId( ), code ) );
        int nCoreMajor = campaign.getCoreMajor( );
        site.setName( definition.getName( ) );
        site.setScmUrl( definition.getScmUrl( ) );
        site.setBranchReleaseFrom( StringUtils.defaultIfBlank( getStepBranchOverride( campaign.getId( ), nStepNumber ), getStepBranch( code, nCoreMajor ) ) );

        RepositoryType repositoryType = site.getRepoType( );
        if ( repositoryType == null )
        {
            throw new AppException( "Unsupported repository : " + definition.getScmUrl( ) );
        }
        Credential credential = user != null ? user.getCredential( repositoryType ) : null;
        if ( credential == null )
        {
            throw new AppException( ConstanteUtils.ERROR_TYPE_AUTHENTICATION_ERROR );
        }

        IVCSResourceService vcsService = CVSFactoryService.getService( repositoryType );
        List<String> listBranches = new ArrayList<>( GitUtils.lsRemoteBranches( GitUtils.getRepoUrl( site.getScmUrl( ) ), credential.getLogin( ),
                credential.getPassword( ) ) );
        listBranches.remove( GitUtils.MASTER_BRANCH );
        String strPom = vcsService.fetchPom( site, credential.getLogin( ), credential.getPassword( ) );
        if ( strPom == null && getStepBranchOverride( campaign.getId( ), nStepNumber ) != null )
        {
            String strOverride = site.getBranchReleaseFrom( );
            saveStepBranch( campaign.getId( ), nStepNumber, null );
            site.setBranchReleaseFrom( getStepBranch( code, nCoreMajor ) );
            strPom = vcsService.fetchPom( site, credential.getLogin( ), credential.getPassword( ) );
            site.addReleaseComment( I18nService.getLocalizedString( MESSAGE_BRANCH_OVERRIDE_RESET, new String [ ] {
                    strOverride, site.getBranchReleaseFrom( )
            }, locale ) );
        }
        if ( strPom == null )
        {
            throw new AppException( "Unable to read the POM of " + definition.getScmUrl( ) + " on branch " + site.getBranchReleaseFrom( ) );
        }
        listBranches.remove( site.getBranchReleaseFrom( ) );
        site.setBranches( listBranches );
        vcsService.getLastRelease( site, credential.getLogin( ), credential.getPassword( ) );
        String strLocalPath = ReleaserUtils.getLocalSitePath( site );

        PomParser parser = new PomParser( );
        parser.parse( site, strPom );
        setPomParent( site, parser.parseParent( strPom ) );
        site.setModules( parser.parseModules( strPom ) );
        site.setCoreVersion( parser.parseProperty( strPom, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PLATFORM_VERSION_PROPERTY_PREFIX )
                + CORE_PROPERTY_KEY + AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PLATFORM_VERSION_PROPERTY_SUFFIX ) ) );
        String strLastReleaseVersion = getLastReleaseOfLine( site );
        checkAggregateCore( site, nCoreMajor, locale );
        checkPomVersions( campaign, code, site, nCoreMajor, locale );

        List<Dependency> listUnknownGroupId = new ArrayList<>( );
        site.getCurrentDependencies( ).clear( );
        for ( Dependency dependency : getStepDependencies( parser, code, site, strPom, strLocalPath, locale ) )
        {
            if ( dependency.getGroupId( ) == null )
            {
                listUnknownGroupId.add( dependency );
            }
            else
            {
                site.addCurrentDependency( dependency );
            }
        }

        site.setParentVersion( nCoreMajor + ".0.0" );

        ReleasePreparationService.initComponents( site, user, component -> isProjectComponent( campaign.getId( ), nStepNumber, component.getArtifactId( ) ) );
        ReleasePreparationService.defineAggregateVersions( site, strLastReleaseVersion );

        PlatformReleaseType releaseType = getStepReleaseType( campaign, code );
        applyReleaseType( site, releaseType );
        for ( Component component : site.getComponents( ) )
        {
            applyReleaseType( component, releaseType );
        }

        addUnknownGroupIdComponents( site, listUnknownGroupId, locale );
        applyPreviousResults( campaign, nStepNumber, site, locale );
        for ( Component component : site.getComponents( ) )
        {
            applyLatestSnapshotChoice( campaign, nStepNumber, component );
            if ( checkReleasable( component, nCoreMajor, locale ) )
            {
                if ( component.isProject( ) )
                {
                    setProjectComponent( campaign, nStepNumber, site, component, false, null, null );
                }
                continue;
            }
            checkComponentBranch( component, nCoreMajor, user, locale );
        }
        loadParentVersions( campaign, nStepNumber, site );

        return site;
    }

    /**
     * Records the parent POM of a resource.
     *
     * @param resource
     *            the resource
     * @param parent
     *            the parent read in its POM, null when none
     */
    private static void setPomParent( AbstractReleaserResource resource, Dependency parent )
    {
        if ( parent != null )
        {
            resource.setPomParentGroupId( parent.getGroupId( ) );
            resource.setPomParentArtifactId( parent.getArtifactId( ) );
            resource.setPomParentVersion( parent.getVersion( ) );
        }
    }

    /**
     * Blocks the step when the branch of the aggregate is built for another core version than the campaign : the components are checked one
     * by one, the aggregate must be too, otherwise the parent proposals and the step 5 parameters would mix two core versions. The screen
     * stays usable so that another branch can be chosen.
     *
     * @param site
     *            the transient site, POM parsed
     * @param nCoreMajor
     *            the core version of the campaign
     * @param locale
     *            the locale of the messages
     */
    static void checkAggregateCore( Site site, int nCoreMajor, Locale locale )
    {
        Integer nCoreLine = getAggregateCoreLine( site );
        if ( nCoreLine == null || nCoreLine == nCoreMajor )
        {
            return;
        }
        boolean bParent = ReleasePreparationService.getParentCoreLine( site ) != null;
        site.setBlockingReleaseComment( I18nService.getLocalizedString( MESSAGE_AGGREGATE_CORE_MISMATCH, new String [ ] {
                site.getBranchReleaseFrom( ), Integer.toString( nCoreLine ), bParent ? site.getPomParentArtifactId( ) : site.getArtifactId( ),
                bParent ? site.getPomParentVersion( ) : site.getVersion( ), Integer.toString( nCoreMajor )
        }, locale ) );
    }

    /**
     * Returns the core version an aggregate is built for : told by its core line parent, otherwise by its own version when the aggregate is
     * itself a core line parent (lutece-global-pom, lutece-site-pom, numbered like the core from 8).
     *
     * @param site
     *            the transient site, POM parsed
     * @return 7 or 8, null when nothing tells it
     */
    static Integer getAggregateCoreLine( Site site )
    {
        Integer nCoreLine = ReleasePreparationService.getParentCoreLine( site );
        if ( nCoreLine != null || !ReleasePreparationService.CORE_LINE_PARENTS.contains( site.getArtifactId( ) ) || site.getVersion( ) == null )
        {
            return nCoreLine;
        }
        try
        {
            return ReleasePreparationService.toCoreLine( Version.parse( site.getVersion( ) ).getMajor( ) );
        }
        catch( VersionParsingException e )
        {
            return null;
        }
    }

    /**
     * Tells whether a version referenced by the platform POM is acceptable : equal to the expected version when one is known, otherwise of
     * the core of the campaign.
     *
     * @param strCurrent
     *            the version referenced by the POM, null when the POM has none
     * @param strExpected
     *            the expected version, null when none is known
     * @param nCoreMajor
     *            the core version of the campaign
     * @return the problem, null when the referenced version is acceptable
     */
    static PomVersionDecision.Problem getPomVersionProblem( String strCurrent, String strExpected, int nCoreMajor )
    {
        if ( strCurrent == null )
        {
            return null;
        }
        if ( strExpected != null )
        {
            return strExpected.equals( strCurrent ) ? null : PomVersionDecision.Problem.MISMATCH;
        }
        try
        {
            return ReleasePreparationService.toCoreLine( Version.parse( strCurrent ).getMajor( ) ) == nCoreMajor ? null : PomVersionDecision.Problem.WRONG_CORE;
        }
        catch( VersionParsingException e )
        {
            return null;
        }
    }

    /**
     * Decides whether a stored choice applies : "keep" only when the campaign released nothing and the POM version is of the right core ; a
     * version only when the campaign released nothing, or that very version.
     *
     * @param strChoice
     *            the stored choice, "keep" or a version, null when none
     * @param strReleased
     *            the version released by the campaign, null when none
     * @param problem
     *            the problem of the POM version
     * @return true when the choice lifts the block
     */
    static boolean isPomVersionChoiceApplicable( String strChoice, String strReleased, PomVersionDecision.Problem problem )
    {
        if ( strChoice == null )
        {
            return false;
        }
        if ( POM_VERSION_KEEP.equals( strChoice ) )
        {
            return strReleased == null && problem == PomVersionDecision.Problem.MISMATCH;
        }

        return strReleased == null || strReleased.equals( strChoice );
    }

    /**
     * Builds the decision about a version the platform POM references : the expected version is the one released by the campaign, or the
     * last one published for the core of the campaign ; a difference is a problem that only a choice can solve. Pure, for the tests.
     *
     * @param strCurrent
     *            the version referenced by the POM
     * @param strReleased
     *            the version released by the campaign, null when none
     * @param listPublished
     *            the versions published in Nexus
     * @param nCoreMajor
     *            the core version of the campaign
     * @param strChoice
     *            the stored choice, "keep" or a version, null when none
     * @return the decision
     */
    static PomVersionDecision decidePomVersion( String strCurrent, String strReleased, List<String> listPublished, int nCoreMajor, String strChoice )
    {
        PomVersionDecision decision = new PomVersionDecision( );
        decision.setCurrent( strCurrent );
        String strExpected = strReleased != null ? strReleased : VersionUtils.getLastVersionUsingMajor( listPublished, nCoreMajor );
        decision.setExpected( strExpected );
        decision.setExpectedReleased( strReleased != null );
        PomVersionDecision.Problem problem = getPomVersionProblem( strCurrent, strExpected, nCoreMajor );
        decision.setProblem( problem );
        if ( problem == null )
        {
            return decision;
        }
        decision.setKeepAllowed( strReleased == null && problem == PomVersionDecision.Problem.MISMATCH );
        if ( isPomVersionChoiceApplicable( strChoice, strReleased, problem ) )
        {
            if ( POM_VERSION_KEEP.equals( strChoice ) )
            {
                decision.setKept( true );
            }
            else
            {
                decision.setTarget( strChoice );
            }
            return decision;
        }
        decision.setBlocked( true );

        return decision;
    }

    /**
     * Returns the aggregate version released by a step of the campaign.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param code
     *            the step code
     * @return the released version, null when the step did not succeed
     */
    private static String getReleasedAggregateVersion( PlatformRelease campaign, PlatformStepCode code )
    {
        PlatformReleaseStep step = getStep( campaign, code.getStepNumber( ) );
        if ( step == null || step.getStatus( ) != PlatformReleaseStatus.SUCCESS )
        {
            return null;
        }
        PlatformStepResult result = parseResult( step );

        return result != null ? result.getAggregateVersion( ) : null;
    }

    /**
     * Returns the versions of an artifact published in Nexus.
     *
     * @param strGroupId
     *            the groupId
     * @param strArtifactId
     *            the artifactId
     * @return the released versions, empty when Nexus cannot be read
     */
    private static List<String> getPublishedVersions( String strGroupId, String strArtifactId )
    {
        Component artifact = new Component( );
        artifact.setGroupId( strGroupId );
        artifact.setArtifactId( strArtifactId );
        MavenRepoComponentInfoProvider.getInstance( ).setComponentRemoteInformations( artifact );

        return artifact.getReleaseVersions( ) != null ? artifact.getReleaseVersions( ) : Collections.<String> emptyList( );
    }

    /**
     * The step of the campaign that releases the parent POM of the platform.
     *
     * @param site
     *            the transient site
     * @return the step code, null when the parent is not a core line parent
     */
    private static PlatformStepCode getParentStepCode( Site site )
    {
        if ( GLOBAL_POM_ARTIFACT_ID.equals( site.getPomParentArtifactId( ) ) )
        {
            return PlatformStepCode.GLOBAL_POM;
        }

        return ReleasePreparationService.CORE_LINE_PARENTS.contains( site.getPomParentArtifactId( ) ) ? PlatformStepCode.SITE_POM : null;
    }

    /**
     * Checks, on the platform step, the lutece-core version and the parent POM version referenced by the POM of the aggregate against the
     * versions released by the campaign (core step, parent step), or, when those steps released nothing, against the last versions published
     * in Nexus. A difference blocks the step until the user chooses : the released version when there is one ; otherwise keep the POM, take
     * the last published version, or name another published version. The pipeline sets the chosen versions in the POM before the release.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param code
     *            the step code
     * @param site
     *            the transient site, POM parsed
     * @param nCoreMajor
     *            the core version of the campaign
     * @param locale
     *            the locale of the messages
     */
    static void checkPomVersions( PlatformRelease campaign, PlatformStepCode code, Site site, int nCoreMajor, Locale locale )
    {
        if ( code != PlatformStepCode.PLATFORM_STARTERS )
        {
            return;
        }
        if ( site.getCoreVersion( ) != null )
        {
            String strChoice = DatastoreService.getDataValue( getPomVersionDataKey( campaign.getId( ), code.getStepNumber( ), CORE_VERSION_KEY ), null );
            PomVersionDecision decision = decidePomVersion( site.getCoreVersion( ), getReleasedAggregateVersion( campaign, PlatformStepCode.CORE ),
                    getPublishedVersions( GROUP_ID_CORE, ConstanteUtils.TAG_LUTECE_CORE ), nCoreMajor, strChoice );
            site.setCoreVersionDecision( decision );
            commentPomVersion( site, decision, ConstanteUtils.TAG_LUTECE_CORE, PlatformStepCode.CORE.getStepNumber( ), nCoreMajor, locale );
        }
        PlatformStepCode parentCode = getParentStepCode( site );
        if ( parentCode != null && site.getPomParentVersion( ) != null )
        {
            String strChoice = DatastoreService.getDataValue( getPomVersionDataKey( campaign.getId( ), code.getStepNumber( ), PARENT_VERSION_KEY ), null );
            PomVersionDecision decision = decidePomVersion( site.getPomParentVersion( ), getReleasedAggregateVersion( campaign, parentCode ),
                    getPublishedVersions( StringUtils.defaultIfBlank( site.getPomParentGroupId( ), GROUP_ID_TOOLS ), site.getPomParentArtifactId( ) ), nCoreMajor,
                    strChoice );
            site.setParentVersionDecision( decision );
            site.setTargetPomParentVersion( decision.getTarget( ) );
            commentPomVersion( site, decision, site.getPomParentArtifactId( ), parentCode.getStepNumber( ), nCoreMajor, locale );
        }
    }

    /**
     * Writes the comments of a POM version decision on the aggregate : the choice made, or the blocking anomaly when none solves the problem.
     * The first anomaly keeps the blocking comment, the rows of the screen show each decision anyway.
     *
     * @param site
     *            the transient site
     * @param decision
     *            the decision
     * @param strArtifactId
     *            the artifact the version refers to
     * @param nReleaseStep
     *            the step of the campaign that releases that artifact
     * @param nCoreMajor
     *            the core version of the campaign
     * @param locale
     *            the locale of the messages
     */
    private static void commentPomVersion( Site site, PomVersionDecision decision, String strArtifactId, int nReleaseStep, int nCoreMajor, Locale locale )
    {
        if ( decision.getProblem( ) == null )
        {
            return;
        }
        String [ ] arguments = {
                strArtifactId, decision.getCurrent( ), Integer.toString( nReleaseStep ), StringUtils.defaultString( decision.getExpected( ) ),
                Integer.toString( nCoreMajor )
        };
        if ( decision.getTarget( ) != null )
        {
            site.addReleaseComment( I18nService.getLocalizedString( MESSAGE_POM_VERSION_WILL_BE_SET, new String [ ] {
                    strArtifactId, decision.getTarget( )
            }, locale ) );
            return;
        }
        if ( decision.isKept( ) )
        {
            site.addReleaseComment( I18nService.getLocalizedString( MESSAGE_POM_VERSION_KEPT, arguments, locale ) );
            return;
        }
        if ( site.getBlockingReleaseComment( ) != null )
        {
            return;
        }
        String strKey = decision.isExpectedReleased( ) ? MESSAGE_POM_VERSION_MISMATCH
                : ( decision.getProblem( ) == PomVersionDecision.Problem.MISMATCH ? MESSAGE_POM_VERSION_LATEST_DIFFERS : MESSAGE_POM_VERSION_WRONG_CORE );
        site.setBlockingReleaseComment( I18nService.getLocalizedString( strKey, arguments, locale ) );
    }

    /**
     * Records the choice made for a version of the platform POM : "keep" to leave the POM as it is, a version to let the pipeline set it
     * before the release, null to forget the choice. A version must be the one released by the campaign or a published release for the core
     * of the campaign. The step is loaded again to apply the choice.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @param bParent
     *            true for the parent POM version, false for the lutece-core version
     * @param strChoice
     *            "keep", a version, or null
     * @throws AppException
     *             when the version is not acceptable
     */
    public static void usePomVersion( PlatformRelease campaign, int nStepNumber, Site site, boolean bParent, String strChoice )
    {
        String strKey = getPomVersionDataKey( campaign.getId( ), nStepNumber, bParent ? PARENT_VERSION_KEY : CORE_VERSION_KEY );
        String strValue = StringUtils.trimToNull( strChoice );
        if ( strValue == null )
        {
            DatastoreService.removeData( strKey );
            return;
        }
        if ( !POM_VERSION_KEEP.equals( strValue ) && !isAcceptablePomVersion( campaign, site, bParent, strValue ) )
        {
            throw new AppException( I18nService.getLocalizedString( MESSAGE_POM_VERSION_UNKNOWN, new String [ ] {
                    bParent ? site.getPomParentArtifactId( ) : ConstanteUtils.TAG_LUTECE_CORE, strValue, Integer.toString( campaign.getCoreMajor( ) )
            }, Locale.getDefault( ) ) );
        }
        DatastoreService.setDataValue( strKey, strValue );
    }

    /**
     * Whether a version may be set in the platform POM : the one released by the campaign, or a published release for the core of the
     * campaign.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param site
     *            the transient site of the step
     * @param bParent
     *            true for the parent POM version, false for the lutece-core version
     * @param strVersion
     *            the version
     * @return true when acceptable
     */
    private static boolean isAcceptablePomVersion( PlatformRelease campaign, Site site, boolean bParent, String strVersion )
    {
        PlatformStepCode releaseCode = bParent ? getParentStepCode( site ) : PlatformStepCode.CORE;
        if ( releaseCode != null && strVersion.equals( getReleasedAggregateVersion( campaign, releaseCode ) ) )
        {
            return true;
        }
        try
        {
            if ( ReleasePreparationService.toCoreLine( Version.parse( strVersion ).getMajor( ) ) != campaign.getCoreMajor( ) )
            {
                return false;
            }
        }
        catch( VersionParsingException e )
        {
            return false;
        }
        List<String> listPublished = bParent ? getPublishedVersions( StringUtils.defaultIfBlank( site.getPomParentGroupId( ), GROUP_ID_TOOLS ), site.getPomParentArtifactId( ) )
                : getPublishedVersions( GROUP_ID_CORE, ConstanteUtils.TAG_LUTECE_CORE );

        return listPublished.contains( strVersion );
    }

    /**
     * Builds the Datastore key of a POM version choice of a step.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param strKind
     *            the kind of version, core or parent
     * @return the key
     */
    private static String getPomVersionDataKey( int nIdPlatformRelease, int nStepNumber, String strKind )
    {
        return getProjectDataKeyPrefix( nIdPlatformRelease ) + nStepNumber + DATA_KEY_SEPARATOR + strKind;
    }

    /**
     * Proposes, for the aggregate and every component of a step whose parent is a POM of the core line (lutece-global-pom, lutece-site-pom),
     * the last version of that parent when it is newer than the declared one : the version released by the campaign at the step of that
     * parent, otherwise the last release published in Nexus for the core version of the campaign. The upgrades chosen earlier are re-applied.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     */
    public static void loadParentVersions( PlatformRelease campaign, int nStepNumber, Site site )
    {
        Map<String, String> mapLatest = new LinkedHashMap<>( );
        if ( nStepNumber != PlatformStepCode.PLATFORM_STARTERS.getStepNumber( ) )
        {
            applyParentVersion( campaign, nStepNumber, site, mapLatest );
        }
        for ( Component component : site.getComponents( ) )
        {
            applyParentVersion( campaign, nStepNumber, component, mapLatest );
        }
    }

    /**
     * Sets the proposed parent version of a resource and re-applies the upgrade chosen earlier, if still relevant.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param resource
     *            the resource
     * @param mapLatest
     *            the last version of each parent already resolved, filled on the way
     */
    private static void applyParentVersion( PlatformRelease campaign, int nStepNumber, AbstractReleaserResource resource, Map<String, String> mapLatest )
    {
        String strParent = resource.getPomParentArtifactId( );
        if ( resource.getBlockingReleaseComment( ) != null || strParent == null || !ReleasePreparationService.CORE_LINE_PARENTS.contains( strParent )
                || resource.getPomParentVersion( ) == null )
        {
            return;
        }
        String strLatest = mapLatest.computeIfAbsent( strParent, a -> findLatestParentVersion( campaign, resource.getPomParentGroupId( ), a ) );
        if ( strLatest == null || ReleaserUtils.compareVersion( resource.getPomParentVersion( ), strLatest ) >= 0 )
        {
            return;
        }
        resource.setLatestPomParentVersion( strLatest );
        if ( DatastoreService.getDataValue( getParentDataKey( campaign.getId( ), nStepNumber, resource.getArtifactId( ) ), null ) != null )
        {
            resource.setTargetPomParentVersion( strLatest );
        }
    }

    /**
     * Returns the last version of a parent POM of the core line : the version released by the campaign at the step of that parent, otherwise
     * the last release published in Nexus for the core version of the campaign.
     *
     * @param campaign
     *            the campaign
     * @param strGroupId
     *            the groupId of the parent
     * @param strArtifactId
     *            the artifactId of the parent
     * @return the last version, null when none is known
     */
    private static String findLatestParentVersion( PlatformRelease campaign, String strGroupId, String strArtifactId )
    {
        PlatformStepCode code = GLOBAL_POM_ARTIFACT_ID.equals( strArtifactId ) ? PlatformStepCode.GLOBAL_POM : PlatformStepCode.SITE_POM;
        String strReleased = getStepAggregateVersion( campaign, code.getStepNumber( ) );
        if ( strReleased != null )
        {
            return strReleased;
        }
        Component parent = new Component( );
        parent.setGroupId( StringUtils.defaultIfBlank( strGroupId, GROUP_ID_TOOLS ) );
        parent.setArtifactId( strArtifactId );
        MavenRepoComponentInfoProvider.getInstance( ).setComponentRemoteInformations( parent );

        return VersionUtils.getLastVersionUsingMajor( parent.getReleaseVersions( ), campaign.getCoreMajor( ) );
    }

    /**
     * Chooses, or gives up, the upgrade of the parent POM of a resource of a step to its last version. The choice is kept in the Datastore
     * with the other choices of the campaign and applied by the pipeline before the release commit.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @param strArtifactId
     *            the artifactId of the aggregate or of a component
     * @param bUpgrade
     *            true to release with the last parent version, false to keep the declared one
     */
    public static void upgradeParent( PlatformRelease campaign, int nStepNumber, Site site, String strArtifactId, boolean bUpgrade )
    {
        AbstractReleaserResource resource = Objects.equals( site.getArtifactId( ), strArtifactId ) ? site
                : site.getComponents( ).stream( ).filter( c -> Objects.equals( c.getArtifactId( ), strArtifactId ) ).findFirst( ).orElse( null );
        if ( resource != null )
        {
            setParentUpgrade( campaign, nStepNumber, resource, bUpgrade );
        }
    }

    /**
     * Chooses the upgrade of the parent POM of the aggregate and of every flagged component of a step whose parent has a newer version.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     */
    public static void upgradeAllParents( PlatformRelease campaign, int nStepNumber, Site site )
    {
        setParentUpgrade( campaign, nStepNumber, site, true );
        for ( Component component : site.getComponents( ) )
        {
            if ( component.isProject( ) )
            {
                setParentUpgrade( campaign, nStepNumber, component, true );
            }
        }
    }

    /**
     * Applies and stores the parent upgrade choice of a resource, when a newer parent version is proposed.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param resource
     *            the resource
     * @param bUpgrade
     *            true to upgrade, false to keep the declared version
     */
    private static void setParentUpgrade( PlatformRelease campaign, int nStepNumber, AbstractReleaserResource resource, boolean bUpgrade )
    {
        String strKey = getParentDataKey( campaign.getId( ), nStepNumber, resource.getArtifactId( ) );
        if ( bUpgrade && resource.getLatestPomParentVersion( ) != null )
        {
            resource.setTargetPomParentVersion( resource.getLatestPomParentVersion( ) );
            DatastoreService.setDataValue( strKey, Boolean.TRUE.toString( ) );
        }
        else
        {
            resource.setTargetPomParentVersion( null );
            DatastoreService.removeData( strKey );
        }
    }

    /**
     * Returns the Datastore key of the parent upgrade choice of a resource, under the prefix of the campaign so that it is purged with it.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param strArtifactId
     *            the resource artifactId
     * @return the key
     */
    private static String getParentDataKey( int nIdPlatformRelease, int nStepNumber, String strArtifactId )
    {
        return getProjectDataKeyPrefix( nIdPlatformRelease ) + nStepNumber + DATA_KEY_SEPARATOR + PARENT_KEY + DATA_KEY_SEPARATOR + strArtifactId;
    }

    /**
     * Turns into a blocking anomaly the cases where a component cannot be released by this campaign, whatever the user does : a theme, never
     * released from here ; a SNAPSHOT of the aggregate POM ahead of the last one published in Nexus, which the pipeline could not release ; a
     * default branch built for another core version than the campaign. The Nexus check comes first : the parent is read in the POM of the
     * current SNAPSHOT, which only exists when Nexus has it. Such a component is taken out of the components to release.
     *
     * @param component
     *            the component
     * @param nCoreMajor
     *            the core version of the campaign
     * @param locale
     *            the locale of the messages
     * @return true when the component is not releasable and now carries the blocking anomaly
     */
    public static boolean checkReleasable( Component component, int nCoreMajor, Locale locale )
    {
        ReleasePreparationService.NonReleasableReason reason = ReleasePreparationService.getNonReleasableReason( component, nCoreMajor,
                AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_BRANCH_DEFAULT ) );
        if ( reason == null )
        {
            return false;
        }
        switch( reason )
        {
            case THEME:
                component.setBlockingReleaseComment( I18nService.getLocalizedString( MESSAGE_THEME_NOT_RELEASABLE, locale ) );
                break;
            case SNAPSHOT_UNKNOWN_IN_NEXUS:
                component.setBlockingReleaseComment( I18nService.getLocalizedString( MESSAGE_SNAPSHOT_UNKNOWN_IN_NEXUS, new String [ ] {
                        component.getCurrentVersion( ), component.getLastAvailableSnapshotVersion( )
                }, locale ) );
                break;
            default:
                component.setBlockingReleaseComment( I18nService.getLocalizedString( ReleasePreparationService.MESSAGE_BRANCH_CORE_MISMATCH,
                        ReleasePreparationService.getCoreMismatchArguments( component, nCoreMajor ), locale ) );
        }

        return true;
    }

    /**
     * Releases a flagged component from the last SNAPSHOT published in Nexus instead of the older SNAPSHOT still referenced by the aggregate
     * POM, or goes back to the POM version. The choice is kept in the Datastore with the flag of the component and re-applied when the step is
     * loaded again.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @param strArtifactId
     *            the component artifactId
     * @param bUse
     *            true to release the last published SNAPSHOT, false to go back to the POM version
     * @param user
     *            the releaser user, for the branch check
     * @param locale
     *            the locale of the messages
     */
    public static void useLatestSnapshot( PlatformRelease campaign, int nStepNumber, Site site, String strArtifactId, boolean bUse, ReleaserUser user,
            Locale locale )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( !component.getArtifactId( ).equals( strArtifactId ) || !component.isProject( ) )
            {
                continue;
            }
            String strKey = getLatestSnapshotDataKey( campaign.getId( ), nStepNumber, strArtifactId );
            if ( bUse )
            {
                DatastoreService.setDataValue( strKey, Boolean.TRUE.toString( ) );
            }
            else
            {
                DatastoreService.removeData( strKey );
            }
            component.setBlockingReleaseComment( null );
            component.setCurrentVersion( bUse ? component.getLastAvailableSnapshotVersion( ) : getPomVersion( site, component ) );
            ReleasePreparationService.refreshProjectComponent( component );
            applyReleaseType( component, getStepReleaseType( campaign, PlatformStepCode.fromStepNumber( nStepNumber ) ) );
            if ( checkReleasable( component, campaign.getCoreMajor( ), locale ) )
            {
                setProjectComponent( campaign, nStepNumber, site, component, false, null, null );
                return;
            }
            checkComponentBranch( component, campaign.getCoreMajor( ), user, locale );
        }
    }

    /**
     * Re-applies the "release the last published SNAPSHOT" choice of a flagged component when the step is loaded again, with the same
     * refresh as the click : the Nexus information (parent included) must describe the chosen SNAPSHOT before any check relies on it.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param component
     *            the component
     */
    private static void applyLatestSnapshotChoice( PlatformRelease campaign, int nStepNumber, Component component )
    {
        if ( !component.isProject( ) || !hasLatestSnapshotChoice( campaign.getId( ), nStepNumber, component.getArtifactId( ) )
                || !isLatestSnapshotCandidate( component ) )
        {
            return;
        }
        component.setCurrentVersion( component.getLastAvailableSnapshotVersion( ) );
        ReleasePreparationService.refreshProjectComponent( component );
        applyReleaseType( component, getStepReleaseType( campaign, PlatformStepCode.fromStepNumber( nStepNumber ) ) );
    }

    /**
     * Forgets the "release the last published SNAPSHOT" choice of a component taken out of the release, and gives it its POM version back.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @param component
     *            the component
     */
    private static void clearLatestSnapshotChoice( PlatformRelease campaign, int nStepNumber, Site site, Component component )
    {
        if ( hasLatestSnapshotChoice( campaign.getId( ), nStepNumber, component.getArtifactId( ) ) )
        {
            DatastoreService.removeData( getLatestSnapshotDataKey( campaign.getId( ), nStepNumber, component.getArtifactId( ) ) );
            component.setCurrentVersion( getPomVersion( site, component ) );
        }
    }

    /**
     * Tells whether a flagged component may be released from the last SNAPSHOT published in Nexus : its POM SNAPSHOT is behind that one.
     *
     * @param component
     *            the component
     * @return true when the choice makes sense
     */
    static boolean isLatestSnapshotCandidate( Component component )
    {
        return component.isProject( ) && component.isSnapshotVersion( ) && !component.isDowngrade( ) && !component.isTheme( )
                && ReleasePreparationService.isParsableSnapshot( component.getLastAvailableSnapshotVersion( ) )
                && ReleaserUtils.compareVersion( component.getCurrentVersion( ), component.getLastAvailableSnapshotVersion( ) ) < 0;
    }

    /**
     * Returns the flagged components whose POM SNAPSHOT is behind the last one published in Nexus, offered the "release the last SNAPSHOT"
     * choice.
     *
     * @param site
     *            the transient site of the step
     * @return the artifactIds
     */
    public static List<String> getLatestSnapshotCandidates( Site site )
    {
        List<String> listCandidates = new ArrayList<>( );
        for ( Component component : site.getComponents( ) )
        {
            if ( isLatestSnapshotCandidate( component ) )
            {
                listCandidates.add( component.getArtifactId( ) );
            }
        }

        return listCandidates;
    }

    /**
     * Returns the components of a step for which the "release the last published SNAPSHOT" choice is active.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @return the artifactIds
     */
    public static List<String> getLatestSnapshotComponents( int nIdPlatformRelease, int nStepNumber, Site site )
    {
        List<String> listComponents = new ArrayList<>( );
        for ( Component component : site.getComponents( ) )
        {
            if ( component.isProject( ) && hasLatestSnapshotChoice( nIdPlatformRelease, nStepNumber, component.getArtifactId( ) ) )
            {
                listComponents.add( component.getArtifactId( ) );
            }
        }

        return listComponents;
    }

    /**
     * Returns the version of a component in the POM of the aggregate.
     *
     * @param site
     *            the transient site of the step
     * @param component
     *            the component
     * @return the POM version, the current version of the component when the POM does not reference it
     */
    private static String getPomVersion( Site site, Component component )
    {
        for ( Dependency dependency : site.getCurrentDependencies( ) )
        {
            if ( Objects.equals( dependency.getArtifactId( ), component.getArtifactId( ) )
                    && ( dependency.getGroupId( ) == null || component.getGroupId( ) == null || dependency.getGroupId( ).equals( component.getGroupId( ) ) )
                    && dependency.getVersion( ) != null )
            {
                return dependency.getVersion( );
            }
        }

        return component.getCurrentVersion( );
    }

    /**
     * Whether the "release the last published SNAPSHOT" choice is stored for a component of a step.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param strArtifactId
     *            the component artifactId
     * @return true when the choice is stored
     */
    private static boolean hasLatestSnapshotChoice( int nIdPlatformRelease, int nStepNumber, String strArtifactId )
    {
        return DatastoreService.getDataValue( getLatestSnapshotDataKey( nIdPlatformRelease, nStepNumber, strArtifactId ), null ) != null;
    }

    /**
     * Returns the Datastore key of the "release the last published SNAPSHOT" choice of a component, under the prefix of the campaign so that
     * it is purged with it.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param strArtifactId
     *            the component artifactId
     * @return the key
     */
    private static String getLatestSnapshotDataKey( int nIdPlatformRelease, int nStepNumber, String strArtifactId )
    {
        return getProjectDataKeyPrefix( nIdPlatformRelease ) + nStepNumber + DATA_KEY_SEPARATOR + LATEST_SNAPSHOT_KEY + DATA_KEY_SEPARATOR + strArtifactId;
    }

    /**
     * Checks that the branch of a component to release carries what the plan expects, before anything is sent to the pipeline : the version of
     * its POM must be the SNAPSHOT referenced by the aggregate, and a parent of the core line (lutece-global-pom, lutece-site-pom) must be of
     * the core version of the campaign. An unreadable POM or a mismatch is a blocking anomaly. A component that is not to be released is not
     * checked.
     *
     * @param component
     *            the component
     * @param nCoreMajor
     *            the core version of the campaign
     * @param user
     *            the releaser user, for the repository credentials
     * @param locale
     *            the locale of the messages
     */
    public static void checkComponentBranch( Component component, int nCoreMajor, ReleaserUser user, Locale locale )
    {
        if ( !component.shouldBeReleased( ) || component.getBlockingReleaseComment( ) != null )
        {
            return;
        }
        RepositoryType repositoryType = component.getRepoType( );
        String strRepository = repositoryType != null ? repositoryType.name( ) : "";
        Credential credential = ( user != null && repositoryType != null ) ? user.getCredential( repositoryType ) : null;
        String strBranch = component.getBranchReleaseFrom( );
        if ( credential == null )
        {
            component.setBlockingReleaseComment( I18nService.getLocalizedString( MESSAGE_BRANCH_CREDENTIALS_MISSING, new String [ ] {
                    strRepository, strBranch
            }, locale ) );
            return;
        }
        String strPom = GitUtils.getRemoteFileContent( component.getScmUrl( ), strBranch, POM_FILE, credential.getLogin( ), credential.getPassword( ) );
        if ( strPom == null )
        {
            component.setBlockingReleaseComment( I18nService.getLocalizedString( MESSAGE_BRANCH_POM_UNREADABLE, new String [ ] {
                    strBranch, strRepository
            }, locale ) );
            return;
        }

        PomParser parser = new PomParser( );
        String strBranchVersion = parser.parseVersion( strPom );
        if ( !Objects.equals( strBranchVersion, component.getCurrentVersion( ) ) )
        {
            component.setBlockingReleaseComment( I18nService.getLocalizedString( MESSAGE_BRANCH_VERSION_MISMATCH, new String [ ] {
                    strBranch, StringUtils.defaultString( strBranchVersion, "?" ), component.getCurrentVersion( )
            }, locale ) );
            return;
        }

        Dependency parent = parser.parseParent( strPom );
        setPomParent( component, parent );
        if ( parent == null || !ReleasePreparationService.CORE_LINE_PARENTS.contains( parent.getArtifactId( ) ) )
        {
            return;
        }
        try
        {
            if ( ReleasePreparationService.toCoreLine( Version.parse( parent.getVersion( ) ).getMajor( ) ) != nCoreMajor )
            {
                component.setBlockingReleaseComment( I18nService.getLocalizedString( MESSAGE_BRANCH_PARENT_MISMATCH, new String [ ] {
                        parent.getArtifactId( ), parent.getVersion( ), strBranch, Integer.toString( nCoreMajor )
                }, locale ) );
            }
        }
        catch( VersionParsingException e )
        {
            AppLogService.error( "Releaser : unreadable parent version " + parent.getVersion( ) + " of " + component.getArtifactId( ) + " on branch " + strBranch );
        }
    }

    /**
     * Returns the last released version of the aggregate for its own core version : the tags of the other core version (8.x tags of a 7.x
     * aggregate and the other way round) are ignored, otherwise a newer RC of the other core version would drive the target version.
     *
     * @param site
     *            the transient site, tags loaded and POM parsed
     * @return the last release for this core version, null when none
     */
    private static String getLastReleaseOfLine( Site site )
    {
        try
        {
            int nMajor = Version.parse( site.getVersion( ) ).getMajor( );

            return VersionUtils.getLastVersionUsingMajor( site.getTags( ), nMajor );
        }
        catch( VersionParsingException e )
        {
            AppLogService.error( "Releaser : unreadable version of platform aggregate " + site.getArtifactId( ) + " : " + site.getVersion( ), e );

            return null;
        }
    }

    /**
     * Returns the repository type of the aggregate of a step, the credentials asked before the step can be prepared.
     *
     * @param nStepNumber
     *            the step number
     * @return the repository type, null for an unknown step or an unsupported repository
     */
    public static RepositoryType getStepRepositoryType( int nStepNumber )
    {
        PlatformStepDefinition definition = PlatformStepDefinitionHome.findByStepNumber( nStepNumber );
        if ( definition == null )
        {
            return null;
        }
        Site site = new Site( );
        site.setScmUrl( definition.getScmUrl( ) );

        return site.getRepoType( );
    }

    /**
     * Returns the repository types a step needs credentials for : the host of its aggregate, then the hosts of its components, in order.
     *
     * @param site
     *            the transient site of the step
     * @return the repository types
     */
    public static List<RepositoryType> getNeededRepositoryTypes( Site site )
    {
        List<RepositoryType> listTypes = new ArrayList<>( );
        if ( site.getRepoType( ) != null )
        {
            listTypes.add( site.getRepoType( ) );
        }
        for ( Component component : site.getComponents( ) )
        {
            RepositoryType type = component.getRepoType( );
            if ( type != null && !listTypes.contains( type ) )
            {
                listTypes.add( type );
            }
        }

        return listTypes;
    }

    /**
     * What is wrong with the credentials of the user for a Git platform.
     */
    public enum CredentialProblem
    {
        /** No credential given for the platform */
        MISSING,
        /** Every component of the platform had its branch listing rejected for authentication */
        REFUSED
    }

    /**
     * Diagnoses the credentials of the user for the hosts of the components of a step : missing when none was given, refused when every
     * component of the host had its branch listing rejected for authentication and none succeeded. Without valid credentials the branches
     * cannot be listed, the POM of the branch cannot be checked and nothing can be released.
     *
     * @param site
     *            the transient site of the step
     * @param user
     *            the releaser user, null when not authenticated
     * @return the problem of each repository type in trouble, in the order of the components ; empty when the credentials are fine
     */
    public static Map<RepositoryType, CredentialProblem> getCredentialProblems( Site site, ReleaserUser user )
    {
        Map<RepositoryType, CredentialProblem> mapProblems = new LinkedHashMap<>( );
        Map<RepositoryType, Boolean> mapListed = new LinkedHashMap<>( );
        Map<RepositoryType, Boolean> mapRefused = new LinkedHashMap<>( );
        for ( Component component : site.getComponents( ) )
        {
            RepositoryType type = component.getRepoType( );
            if ( type == null )
            {
                continue;
            }
            if ( user == null || user.getCredential( type ) == null )
            {
                mapProblems.put( type, CredentialProblem.MISSING );
                continue;
            }
            boolean bListed = CollectionUtils.isNotEmpty( component.getBranches( ) );
            mapListed.put( type, Boolean.TRUE.equals( mapListed.get( type ) ) || bListed );
            mapRefused.put( type, Boolean.TRUE.equals( mapRefused.get( type ) ) || GitUtils.isAuthenticationError( component.getRemoteError( ) ) );
        }
        for ( Map.Entry<RepositoryType, Boolean> entry : mapRefused.entrySet( ) )
        {
            if ( entry.getValue( ) && !Boolean.TRUE.equals( mapListed.get( entry.getKey( ) ) ) && !mapProblems.containsKey( entry.getKey( ) ) )
            {
                mapProblems.put( entry.getKey( ), CredentialProblem.REFUSED );
            }
        }

        return mapProblems;
    }

    /**
     * Toggles the "to be released" flag of a component of a step, like a site component. A component already released by a previous step
     * cannot be toggled. A component taken out of the release gets its current version back as target.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @param strArtifactId
     *            the component artifactId
     * @param user
     *            the releaser user, for the branch check of a component to release
     * @param locale
     *            the locale of the messages
     */
    public static void toggleProjectComponent( PlatformRelease campaign, int nStepNumber, Site site, String strArtifactId, ReleaserUser user, Locale locale )
    {
        Map<String, String> mapReleasedVersions = getReleasedVersions( campaign, nStepNumber );

        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) && !mapReleasedVersions.containsKey( getCoordinates( component ) ) )
            {
                setProjectComponent( campaign, nStepNumber, site, component, !component.isProject( ), user, locale );
            }
        }
    }

    /**
     * Flags every component of a step that can be released as to be released : still in SNAPSHOT, not flagged yet, not released by a
     * previous attempt, without blocking anomaly and without pending upgrade, the same rule as the "add" button of a component line.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @param user
     *            the releaser user, for the branch check of the flagged components
     * @param locale
     *            the locale of the messages
     * @return the number of components flagged
     */
    public static int selectSnapshotComponents( PlatformRelease campaign, int nStepNumber, Site site, ReleaserUser user, Locale locale )
    {
        Map<String, String> mapReleasedVersions = getReleasedVersions( campaign, nStepNumber );
        int nSelected = 0;

        for ( Component component : site.getComponents( ) )
        {
            if ( isSelectableSnapshot( component, mapReleasedVersions ) )
            {
                setProjectComponent( campaign, nStepNumber, site, component, true, user, locale );
                nSelected++;
            }
        }

        return nSelected;
    }

    /**
     * Unflags every component flagged as to be released in a step and not released by a previous attempt, the reverse of
     * {@link #selectSnapshotComponents(PlatformRelease, int, Site, ReleaserUser, Locale)}.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @return the number of components unflagged
     */
    public static int deselectSnapshotComponents( PlatformRelease campaign, int nStepNumber, Site site )
    {
        Map<String, String> mapReleasedVersions = getReleasedVersions( campaign, nStepNumber );
        int nDeselected = 0;

        for ( Component component : site.getComponents( ) )
        {
            if ( component.isProject( ) && !mapReleasedVersions.containsKey( getCoordinates( component ) ) )
            {
                setProjectComponent( campaign, nStepNumber, site, component, false, null, null );
                nDeselected++;
            }
        }

        return nDeselected;
    }

    /**
     * Tells whether a step still has components that {@link #selectSnapshotComponents(PlatformRelease, int, Site, ReleaserUser, Locale)} would flag.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @return true if at least one component can still be flagged
     */
    public static boolean hasSelectableSnapshotComponents( PlatformRelease campaign, int nStepNumber, Site site )
    {
        Map<String, String> mapReleasedVersions = getReleasedVersions( campaign, nStepNumber );

        return site.getComponents( ).stream( ).anyMatch( component -> isSelectableSnapshot( component, mapReleasedVersions ) );
    }

    /**
     * Tells whether a component can be flagged as to be released : still in SNAPSHOT, not flagged yet, not released by a previous attempt,
     * without blocking anomaly and without pending upgrade, the same rule as the "add" button of a component line.
     *
     * @param component
     *            the component
     * @param mapReleasedVersions
     *            the versions released by a previous attempt, by coordinates
     * @return true if the component can be flagged
     */
    private static boolean isSelectableSnapshot( Component component, Map<String, String> mapReleasedVersions )
    {
        return !component.isProject( ) && component.isSnapshotVersion( ) && component.getBlockingReleaseComment( ) == null
                && Objects.equals( component.getCurrentVersion( ), component.getTargetVersion( ) )
                && !mapReleasedVersions.containsKey( getCoordinates( component ) );
    }

    /**
     * Flags a component as to be released in a step, or not, keeps the choice in the Datastore and recomputes its target versions.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @param component
     *            the component
     * @param bProject
     *            true to release the component in the step
     * @param user
     *            the releaser user, for the branch check of a component to release
     * @param locale
     *            the locale of the messages
     */
    private static void setProjectComponent( PlatformRelease campaign, int nStepNumber, Site site, Component component, boolean bProject, ReleaserUser user,
            Locale locale )
    {
        component.setIsProject( bProject );
        DatastoreService.setDataValue( getProjectDataKey( campaign.getId( ), nStepNumber, component.getArtifactId( ) ), Boolean.toString( bProject ) );

        if ( bProject )
        {
            ReleasePreparationService.refreshProjectComponent( component );
            applyReleaseType( component, getStepReleaseType( campaign, PlatformStepCode.fromStepNumber( nStepNumber ) ) );
            applyLatestSnapshotChoice( campaign, nStepNumber, component );
            if ( checkReleasable( component, campaign.getCoreMajor( ), locale ) )
            {
                setProjectComponent( campaign, nStepNumber, site, component, false, null, null );
                return;
            }
            checkComponentBranch( component, campaign.getCoreMajor( ), user, locale );
        }
        else
        {
            clearLatestSnapshotChoice( campaign, nStepNumber, site, component );
            component.setDowngrade( false );
            ReleasePreparationService.defineTargetVersion( component );
            ReleasePreparationService.defineNextSnapshotVersion( component );
        }
    }

    /**
     * Selects, among the next versions of a component to release, the one matching the release type.
     *
     * @param component
     *            the component
     * @param releaseType
     *            the release type
     */
    public static void applyReleaseType( Component component, PlatformReleaseType releaseType )
    {
        if ( !component.isProject( ) || !component.isSnapshotVersion( ) || CollectionUtils.isEmpty( component.getTargetVersions( ) ) )
        {
            return;
        }

        String strTargetVersion = selectTargetVersion( component.getTargetVersions( ), component.getTargetVersion( ), releaseType );
        component.setTargetVersion( strTargetVersion );
        component.setTargetVersionIndex( Math.max( 0, component.getTargetVersions( ).indexOf( strTargetVersion ) ) );
        component.setNextSnapshotVersion( Version.getNextSnapshotVersion( strTargetVersion ) );
    }

    /**
     * Selects, among the next versions of an aggregate, the one matching the release type.
     *
     * @param site
     *            the aggregate
     * @param releaseType
     *            the release type
     */
    public static void applyReleaseType( Site site, PlatformReleaseType releaseType )
    {
        if ( CollectionUtils.isEmpty( site.getTargetVersions( ) ) )
        {
            return;
        }

        String strTargetVersion = selectTargetVersion( site.getTargetVersions( ), site.getNextReleaseVersion( ), releaseType );
        site.setNextReleaseVersion( strTargetVersion );
        site.setTargetVersionIndex( Math.max( 0, site.getTargetVersions( ).indexOf( strTargetVersion ) ) );
        site.setNextSnapshotVersion( Version.getNextSnapshotVersion( strTargetVersion ) );
    }

    /**
     * Selects the version matching a release type in a list of candidate versions : the first RC for RC, the first beta for beta, the first
     * plain version otherwise. Falls back on the default when the list has no such entry.
     *
     * @param listTargetVersions
     *            the candidate versions (see {@link Version#getNextReleaseVersions(String, String)})
     * @param strDefaultVersion
     *            the version to keep when no candidate matches
     * @param releaseType
     *            the release type
     * @return the selected version
     */
    public static String selectTargetVersion( List<String> listTargetVersions, String strDefaultVersion, PlatformReleaseType releaseType )
    {
        for ( String strVersion : listTargetVersions )
        {
            boolean bMatch;
            if ( releaseType == PlatformReleaseType.RC )
            {
                bMatch = Version.isCandidate( strVersion );
            }
            else
                if ( releaseType == PlatformReleaseType.BETA )
                {
                    bMatch = Version.isBeta( strVersion );
                }
                else
                {
                    bMatch = !Version.isCandidate( strVersion ) && !Version.isBeta( strVersion ) && !Version.isSnapshot( strVersion );
                }

            if ( bMatch )
            {
                return strVersion;
            }
        }

        return strDefaultVersion;
    }

    /**
     * Builds the plan of a step : the flagged components that must be released, with the same rule as a site release
     * ({@link Component#shouldBeReleased()} : a component whose snapshot is not the last one available, a downgrade or a theme is not released),
     * in release order, then the aggregate with the version updates to apply to its POM.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @param bDryRun
     *            true to ask the pipeline for a simulation
     * @return the plan
     */
    public static PlatformReleasePlan buildPlan( PlatformRelease campaign, int nStepNumber, Site site, boolean bDryRun )
    {
        PlatformStepCode code = PlatformStepCode.fromStepNumber( nStepNumber );
        PlatformReleaseType releaseType = getStepReleaseType( campaign, code );

        PlatformReleasePlan plan = new PlatformReleasePlan( );
        plan.setCampaignId( campaign.getId( ) );
        plan.setCampaignName( campaign.getName( ) );
        plan.setStep( nStepNumber );
        plan.setStepCode( code );
        plan.setReleaseType( releaseType );
        plan.setCoreMajor( campaign.getCoreMajor( ) );
        plan.setDryRun( bDryRun );

        if ( site.getBlockingReleaseComment( ) != null )
        {
            throw new AppException( I18nService.getLocalizedString( MESSAGE_AGGREGATE_BLOCKED, new String [ ] {
                    site.getBlockingReleaseComment( )
            }, Locale.getDefault( ) ) );
        }
        List<Component> listToRelease = new ArrayList<>( );
        List<String> listBlocked = new ArrayList<>( );
        for ( Component component : site.getComponents( ) )
        {
            if ( component.isProject( ) && component.getBlockingReleaseComment( ) != null )
            {
                listBlocked.add( component.getArtifactId( ) );
            }
            else
                if ( component.shouldBeReleased( ) )
                {
                    listToRelease.add( component );
                }
        }
        if ( !listBlocked.isEmpty( ) )
        {
            throw new AppException( I18nService.getLocalizedString( MESSAGE_BLOCKED_COMPONENTS, new String [ ] {
                    String.join( ", ", listBlocked )
            }, Locale.getDefault( ) ) );
        }
        Map<String, String> mapVersionProperties = getVersionProperties( site );
        for ( Component component : sortComponents( listToRelease ) )
        {
            PlatformPlanResource resource = toResource( component );
            resource.setVersionProperty( mapVersionProperties.get( getCoordinates( component ) ) );
            plan.getComponents( ).add( resource );
        }

        plan.setAggregate( buildAggregateResource( campaign, nStepNumber, code, site ) );

        return plan;
    }

    /**
     * Orders the components of a step for a sequential release : libraries, then the other artifacts, then plugins, then modules. The POM order
     * is kept inside each group.
     *
     * @param listComponents
     *            the components
     * @return the ordered components
     */
    public static List<Component> sortComponents( List<Component> listComponents )
    {
        List<Component> listSorted = new ArrayList<>( listComponents );
        listSorted.sort( Comparator.comparingInt( PlatformReleaseService::getReleaseOrder ) );

        return listSorted;
    }

    /**
     * Returns the release group of a component from its artifactId prefix.
     *
     * @param component
     *            the component
     * @return the group order
     */
    private static int getReleaseOrder( Component component )
    {
        String strArtifactId = StringUtils.defaultString( component.getArtifactId( ) );
        if ( strArtifactId.startsWith( PREFIX_LIBRARY ) )
        {
            return ORDER_LIBRARY;
        }
        if ( strArtifactId.startsWith( PREFIX_PLUGIN ) )
        {
            return ORDER_PLUGIN;
        }
        if ( strArtifactId.startsWith( PREFIX_MODULE ) )
        {
            return ORDER_MODULE;
        }

        return ORDER_OTHER;
    }

    /**
     * Returns the master branch to merge a released resource into : none for a beta or RC target version, whatever the type of the campaign
     * (a stable component may be released in a beta campaign and a beta component in a stable one), the master counterpart of the release
     * branch otherwise.
     *
     * @param strBranchReleaseFrom
     *            the release branch
     * @param strTargetVersion
     *            the version released
     * @return the master branch, or null when no merge applies
     */
    public static String getMasterBranch( String strBranchReleaseFrom, String strTargetVersion )
    {
        return isMergedIntoMaster( strTargetVersion ) ? GitUtils.getTargetMasterBranch( strBranchReleaseFrom ) : null;
    }

    /**
     * Whether a released version is merged into the master branch : only a stable version is, never a beta or RC one.
     *
     * @param strTargetVersion
     *            the version released
     * @return true for a stable version
     */
    public static boolean isMergedIntoMaster( String strTargetVersion )
    {
        return strTargetVersion != null && !Version.isBeta( strTargetVersion ) && !Version.isCandidate( strTargetVersion );
    }

    /**
     * Converts a component to a plan resource.
     *
     * @param component
     *            the component
     * @return the resource
     */
    private static PlatformPlanResource toResource( Component component )
    {
        PlatformPlanResource resource = new PlatformPlanResource( );
        resource.setGroupId( component.getGroupId( ) );
        resource.setArtifactId( component.getArtifactId( ) );
        resource.setType( component.getType( ) );
        resource.setScmUrl( GitUtils.getRepoUrl( component.getScmUrl( ) ) );
        resource.setBranch( component.getBranchReleaseFrom( ) );
        resource.setMasterBranch( getMasterBranch( component.getBranchReleaseFrom( ), component.getTargetVersion( ) ) );
        resource.setCurrentVersion( component.getCurrentVersion( ) );
        resource.setTargetVersion( component.getTargetVersion( ) );
        resource.setNextSnapshotVersion( component.getNextSnapshotVersion( ) );
        resource.setCurrentParentVersion( component.getPomParentVersion( ) );
        resource.setParentVersion( component.getTargetPomParentVersion( ) );

        return resource;
    }

    /**
     * Builds the aggregate resource of a plan : its versions, the parent version released at the first step and the version updates of every
     * component whose target differs from the current version (released in this step or by a previous one). A version held by a POM property
     * is updated through the property, the other ones through the dependency declaration.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @param code
     *            the step code
     * @param site
     *            the transient site of the step
     * @return the aggregate resource
     */
    private static PlatformPlanResource buildAggregateResource( PlatformRelease campaign, int nStepNumber, PlatformStepCode code, Site site )
    {
        PlatformPlanResource aggregate = new PlatformPlanResource( );
        aggregate.setGroupId( site.getGroupId( ) );
        aggregate.setArtifactId( site.getArtifactId( ) );
        aggregate.setType( code == PlatformStepCode.CORE ? ConstanteUtils.DEPENDENCY_TYPE_LUTECE_CORE : TYPE_POM );
        aggregate.setCoreVersion( site.getCoreVersionDecision( ) != null ? site.getCoreVersionDecision( ).getTarget( ) : null );
        aggregate.setScmUrl( site.getScmUrl( ) );
        aggregate.setBranch( site.getBranchReleaseFrom( ) );
        aggregate.setMasterBranch( getMasterBranch( site.getBranchReleaseFrom( ), site.getNextReleaseVersion( ) ) );
        aggregate.setCurrentVersion( site.getVersion( ) );
        aggregate.setTargetVersion( site.getNextReleaseVersion( ) );
        aggregate.setNextSnapshotVersion( site.getNextSnapshotVersion( ) );
        aggregate.setCurrentParentVersion( site.getPomParentVersion( ) );
        aggregate.setParentVersion( site.getTargetPomParentVersion( ) );

        Map<String, String> mapVersionProperties = getVersionProperties( site );
        for ( Component component : site.getComponents( ) )
        {
            String strTargetVersion = component.getTargetVersion( );
            if ( strTargetVersion == null || ConstanteUtils.NO_VERSION_DEFINED_IN_POM.equals( strTargetVersion )
                    || strTargetVersion.equals( component.getCurrentVersion( ) ) )
            {
                continue;
            }

            String strCoordinates = getCoordinates( component );
            String strVersionProperty = mapVersionProperties.get( strCoordinates );
            if ( strVersionProperty != null )
            {
                aggregate.getVersionUpdates( ).getProperties( ).put( strVersionProperty, strTargetVersion );
            }
            else
            {
                aggregate.getVersionUpdates( ).getDependencies( ).put( strCoordinates, strTargetVersion );
            }
        }

        return aggregate;
    }

    /**
     * Returns, for the dependencies of the aggregate whose version is held by a POM property, the property name keyed by "groupId:artifactId".
     *
     * @param site
     *            the transient site of the step
     * @return the version properties
     */
    private static Map<String, String> getVersionProperties( Site site )
    {
        Map<String, String> mapVersionProperties = new LinkedHashMap<>( );
        for ( Dependency dependency : site.getCurrentDependencies( ) )
        {
            if ( dependency.getVersionProperty( ) != null )
            {
                mapVersionProperties.put( dependency.getGroupId( ) + COORDINATES_SEPARATOR + dependency.getArtifactId( ), dependency.getVersionProperty( ) );
            }
        }

        return mapVersionProperties;
    }

    /**
     * Applies the results of the previous steps and of the failed attempts of this step : a component already released is no longer to
     * release, its target is the released version. The information is carried as the blocking comment : it survives the rebuild of the
     * comments at display time, and no later check or action can put the component back into the release.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @param site
     *            the transient site of the step
     * @param locale
     *            the locale of the comments
     */
    private static void applyPreviousResults( PlatformRelease campaign, int nStepNumber, Site site, Locale locale )
    {
        for ( int nPrevious = 1; nPrevious <= nStepNumber; nPrevious++ )
        {
            PlatformStepResult result = getStepResult( campaign, nPrevious );
            if ( result == null )
            {
                continue;
            }

            for ( Component component : site.getComponents( ) )
            {
                String strReleasedVersion = result.getReleasedVersions( ).get( getCoordinates( component ) );
                if ( strReleasedVersion != null && component.isSnapshotVersion( ) )
                {
                    component.setIsProject( false );
                    component.setTargetVersion( strReleasedVersion );
                    component.setNextSnapshotVersion( Version.NOT_AVAILABLE );
                    String [ ] arguments = {
                            strReleasedVersion, String.valueOf( nPrevious )
                    };
                    component.setBlockingReleaseComment( I18nService.getLocalizedString( MESSAGE_ALREADY_RELEASED, arguments, locale ) );
                }
            }
        }
    }

    /**
     * Returns the versions released by the steps before a given one and by the failed attempts of this one, keyed by "groupId:artifactId".
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @return the released versions
     */
    static Map<String, String> getReleasedVersions( PlatformRelease campaign, int nStepNumber )
    {
        Map<String, String> mapReleasedVersions = new LinkedHashMap<>( );
        for ( int nPrevious = 1; nPrevious <= nStepNumber; nPrevious++ )
        {
            PlatformStepResult result = getStepResult( campaign, nPrevious );
            if ( result != null )
            {
                mapReleasedVersions.putAll( result.getReleasedVersions( ) );
            }
        }

        return mapReleasedVersions;
    }

    /**
     * Returns the version of the aggregate released by a step, null if the step is not successfully completed.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @return the aggregate version
     */
    private static String getStepAggregateVersion( PlatformRelease campaign, int nStepNumber )
    {
        PlatformStepResult result = getStepResult( campaign, nStepNumber );

        return result != null ? result.getAggregateVersion( ) : null;
    }

    /**
     * Returns the stored result of a step, complete after a success or partial after a failure, null when the step has none.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @return the result
     */
    private static PlatformStepResult getStepResult( PlatformRelease campaign, int nStepNumber )
    {
        return parseResult( getStep( campaign, nStepNumber ) );
    }

    /**
     * Returns the report of the last build of a step, simulation included.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @param nStepNumber
     *            the step number
     * @return the report, null when the step has none
     */
    public static String getStepReport( PlatformRelease campaign, int nStepNumber )
    {
        PlatformStepResult result = getStepResult( campaign, nStepNumber );

        return result != null ? StringUtils.trimToNull( result.getReport( ) ) : null;
    }

    /**
     * Reads the stored result of a step.
     *
     * @param step
     *            the step, may be null
     * @return the result, null when the step has none or it is unreadable
     */
    private static PlatformStepResult parseResult( PlatformReleaseStep step )
    {
        if ( step == null || StringUtils.isBlank( step.getResultJson( ) ) )
        {
            return null;
        }

        try
        {
            return MapperJsonUtil.parse( step.getResultJson( ), PlatformStepResult.class );
        }
        catch( IOException e )
        {
            AppLogService.error( "Unreadable result of platform step " + step.getStepNumber( ) + " of campaign " + step.getIdPlatformRelease( ), e );
            return null;
        }
    }

    /**
     * Exports the plans of every step of a campaign, components then aggregate, one line per resource.
     *
     * @param campaign
     *            the campaign, loaded with its steps
     * @return the CSV content
     */
    public static String exportCsv( PlatformRelease campaign )
    {
        StringBuilder sb = new StringBuilder( CSV_HEADER ).append( CSV_LINE_SEPARATOR );

        for ( PlatformReleaseStep step : campaign.getSteps( ) )
        {
            if ( StringUtils.isBlank( step.getPlanJson( ) ) )
            {
                continue;
            }

            try
            {
                PlatformReleasePlan plan = MapperJsonUtil.parse( step.getPlanJson( ), PlatformReleasePlan.class );
                PlatformStepResult result = parseResult( step );
                for ( PlatformPlanResource resource : plan.getComponents( ) )
                {
                    appendCsvLine( sb, step, plan, result, resource, false );
                }
                if ( plan.getAggregate( ) != null )
                {
                    appendCsvLine( sb, step, plan, result, plan.getAggregate( ), true );
                }
            }
            catch( IOException e )
            {
                AppLogService.error( "Unreadable plan of platform step " + step.getStepNumber( ) + " of campaign " + campaign.getId( ), e );
            }
        }

        return sb.toString( );
    }

    /**
     * Appends the CSV line of a resource of a step : whether it was released, read from the report of the step, then what happened to it. The
     * aggregate of a step that does not release it (the lutece-platform POM of the plugins step) carries no target version.
     *
     * @param sb
     *            the CSV content
     * @param step
     *            the step
     * @param plan
     *            the plan of the step
     * @param result
     *            the result of the step, null when none
     * @param resource
     *            the resource
     * @param bAggregate
     *            true for the aggregate of the step
     */
    private static void appendCsvLine( StringBuilder sb, PlatformReleaseStep step, PlatformReleasePlan plan, PlatformStepResult result,
            PlatformPlanResource resource, boolean bAggregate )
    {
        boolean bPomOnly = bAggregate && plan.getStepCode( ) != null && !plan.getStepCode( ).isAggregateReleased( );
        boolean bReleased = isReleased( step, plan, result, resource, bAggregate, bPomOnly );
        String strTargetVersion = bPomOnly ? null : resource.getTargetVersion( );
        String strNextSnapshotVersion = bPomOnly ? null : resource.getNextSnapshotVersion( );

        sb.append( step.getStepNumber( ) ).append( CSV_SEPARATOR ).append( StringUtils.defaultString( resource.getGroupId( ) ) ).append( CSV_SEPARATOR )
                .append( StringUtils.defaultString( resource.getArtifactId( ) ) ).append( CSV_SEPARATOR )
                .append( StringUtils.defaultString( resource.getCurrentVersion( ) ) ).append( CSV_SEPARATOR )
                .append( StringUtils.defaultString( strTargetVersion ) ).append( CSV_SEPARATOR ).append( StringUtils.defaultString( strNextSnapshotVersion ) )
                .append( CSV_SEPARATOR ).append( StringUtils.defaultString( resource.getBranch( ) ) ).append( CSV_SEPARATOR )
                .append( StringUtils.defaultString( resource.getCurrentParentVersion( ) ) ).append( CSV_SEPARATOR )
                .append( StringUtils.defaultString( resource.getParentVersion( ) ) ).append( CSV_SEPARATOR )
                .append( bReleased ? CSV_RELEASED : CSV_NOT_RELEASED ).append( CSV_SEPARATOR )
                .append( getCsvStatus( step, plan, result, resource, bAggregate, bReleased, bPomOnly ) )
                .append( CSV_LINE_SEPARATOR );
    }

    /**
     * Whether a resource of a step was released : it is listed in the report of the step, or the step succeeded without a readable report.
     *
     * @param step
     *            the step
     * @param plan
     *            the plan of the step
     * @param result
     *            the result of the step, null when none
     * @param resource
     *            the resource
     * @param bAggregate
     *            true for the aggregate of the step
     * @param bPomOnly
     *            true when the step only updates the POM of its aggregate
     * @return true when the resource was released
     */
    private static boolean isReleased( PlatformReleaseStep step, PlatformReleasePlan plan, PlatformStepResult result, PlatformPlanResource resource,
            boolean bAggregate, boolean bPomOnly )
    {
        if ( bPomOnly )
        {
            return false;
        }
        if ( result != null )
        {
            boolean bInReport = bAggregate ? result.getAggregateVersion( ) != null
                    : result.getReleasedVersions( ) != null
                            && result.getReleasedVersions( ).containsKey( resource.getGroupId( ) + COORDINATES_SEPARATOR + resource.getArtifactId( ) );
            if ( bInReport )
            {
                return true;
            }
        }

        return step.getStatus( ) == PlatformReleaseStatus.SUCCESS && !plan.isDryRun( );
    }

    /**
     * Returns what happened to a resource of a step : published, POM of the aggregate updated or not, simulated, in progress, rolled back
     * after a failure, not processed by a failed step, failed (platform pipeline), not sent.
     *
     * @param step
     *            the step
     * @param plan
     *            the plan of the step
     * @param result
     *            the result of the step, null when none
     * @param resource
     *            the resource
     * @param bAggregate
     *            true for the aggregate of the step
     * @param bReleased
     *            true when the resource was released
     * @param bPomOnly
     *            true when the step only updates the POM of its aggregate
     * @return the detailed status
     */
    private static String getCsvStatus( PlatformReleaseStep step, PlatformReleasePlan plan, PlatformStepResult result, PlatformPlanResource resource,
            boolean bAggregate, boolean bReleased, boolean bPomOnly )
    {
        if ( bReleased )
        {
            return CSV_STATUS_PUBLISHED;
        }
        if ( plan.isDryRun( ) )
        {
            return CSV_STATUS_SIMULATED;
        }
        if ( step.getStatus( ) == PlatformReleaseStatus.RUNNING )
        {
            return CSV_STATUS_IN_PROGRESS;
        }
        if ( step.getStatus( ) == PlatformReleaseStatus.SUCCESS )
        {
            return bPomOnly ? CSV_STATUS_POM_VERSIONS_UPDATED : CSV_STATUS_PUBLISHED;
        }
        if ( step.getStatus( ) != PlatformReleaseStatus.FAILED )
        {
            return CSV_STATUS_NOT_SENT;
        }
        if ( bAggregate && plan.getStepCode( ) != null && plan.getStepCode( ).getExecutor( ) == PlatformStepCode.Executor.STEP_PIPELINE )
        {
            return result != null && result.isPomUpdated( ) ? CSV_STATUS_POM_VERSIONS_UPDATED : CSV_STATUS_POM_NOT_UPDATED;
        }
        if ( bAggregate )
        {
            return CSV_STATUS_FAILED;
        }

        return result != null && result.getFailedVersions( ).containsKey( resource.getGroupId( ) + COORDINATES_SEPARATOR + resource.getArtifactId( ) )
                ? CSV_STATUS_ROLLED_BACK
                : CSV_STATUS_NOT_PROCESSED;
    }

    /**
     * Appends a CSV line.
     *
     * @param sb
     *            the CSV content
     * @param step
     *            the step
     * @param resource
     *            the resource
     * @param strTargetVersion
     *            the target version column
     * @param strNextSnapshotVersion
     *            the next snapshot column
     * @param strStatus
     *            the status column
     */
    private static void appendCsvLine( StringBuilder sb, PlatformReleaseStep step, PlatformPlanResource resource, String strTargetVersion,
            String strNextSnapshotVersion, String strStatus )
    {
        sb.append( step.getStepNumber( ) ).append( CSV_SEPARATOR ).append( StringUtils.defaultString( resource.getGroupId( ) ) ).append( CSV_SEPARATOR )
                .append( StringUtils.defaultString( resource.getArtifactId( ) ) ).append( CSV_SEPARATOR )
                .append( StringUtils.defaultString( resource.getCurrentVersion( ) ) ).append( CSV_SEPARATOR )
                .append( StringUtils.defaultString( strTargetVersion ) ).append( CSV_SEPARATOR ).append( StringUtils.defaultString( strNextSnapshotVersion ) )
                .append( CSV_SEPARATOR ).append( StringUtils.defaultString( resource.getBranch( ) ) ).append( CSV_SEPARATOR ).append( strStatus )
                .append( CSV_LINE_SEPARATOR );
    }

    /**
     * Returns the components of a step according to its source : Lutece artifacts referenced by the POM, version properties resolved with the
     * BOM (the own modules of the aggregate are left out, a missing BOM is reported on the aggregate), or none.
     *
     * @param parser
     *            the POM parser
     * @param code
     *            the step code
     * @param site
     *            the transient site of the step
     * @param strPom
     *            the aggregate POM
     * @param strLocalPath
     *            the local clone path
     * @param locale
     *            the locale of the comments
     * @return the dependencies
     */
    private static List<Dependency> getStepDependencies( PomParser parser, PlatformStepCode code, Site site, String strPom, String strLocalPath,
            Locale locale )
    {
        switch( code.getComponentSource( ) )
        {
            case REFERENCED_ARTIFACTS:
                return parser.parseReferencedArtifacts( strPom, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PLATFORM_GROUP_ID_PREFIX ) );

            case VERSION_PROPERTIES:
                String strBomPath = strLocalPath + File.separator + AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PLATFORM_BOM_PATH );
                String strBom = null;
                if ( new File( strBomPath ).exists( ) )
                {
                    strBom = FileUtils.readFile( strBomPath );
                }
                else
                {
                    String [ ] arguments = {
                            strBomPath
                    };
                    site.addReleaseComment( I18nService.getLocalizedString( MESSAGE_BOM_NOT_FOUND, arguments, locale ) );
                    AppLogService.error( "Releaser : BOM not found at " + strBomPath + " for platform step " + code );
                }
                List<Dependency> listDependencies = parser.parseVersionProperties( strPom,
                        AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PLATFORM_VERSION_PROPERTY_PREFIX ),
                        AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PLATFORM_VERSION_PROPERTY_SUFFIX ), strBom );
                List<String> listModules = parser.parseModules( strPom );
                Map<String, String> mapAliases = getArtifactIdAliases( );
                List<Dependency> listComponents = new ArrayList<>( );
                for ( Dependency dependency : listDependencies )
                {
                    if ( listModules.contains( dependency.getArtifactId( ) ) )
                    {
                        continue;
                    }
                    String strAlias = mapAliases.get( dependency.getArtifactId( ) );
                    if ( strAlias != null )
                    {
                        dependency.setArtifactId( strAlias );
                    }
                    listComponents.add( dependency );
                }
                return listComponents;

            default:
                return Collections.emptyList( );
        }
    }

    /**
     * Adds the components whose groupId is unknown (not in the BOM) with a blocking comment, without remote informations.
     *
     * @param site
     *            the transient site
     * @param listDependencies
     *            the dependencies without groupId
     * @param locale
     *            the locale of the comment
     */
    private static void addUnknownGroupIdComponents( Site site, List<Dependency> listDependencies, Locale locale )
    {
        for ( Dependency dependency : listDependencies )
        {
            Component component = new Component( );
            component.setArtifactId( dependency.getArtifactId( ) );
            component.setType( dependency.getType( ) );
            component.setCurrentVersion( StringUtils.defaultString( dependency.getVersion( ), ConstanteUtils.NO_VERSION_DEFINED_IN_POM ) );
            component.setTargetVersion( component.getCurrentVersion( ) );
            component.setName( dependency.getArtifactId( ) );
            component.setBlockingReleaseComment( I18nService.getLocalizedString( MESSAGE_UNKNOWN_GROUP_ID, locale ) );
            site.addComponent( component );
        }
    }

    /**
     * Returns the development branch of an aggregate for a core version : the default branch for the current core, the core 7 branch or the
     * plugins 7 branch for the legacy core 7.
     *
     * @param code
     *            the step code
     * @param nCoreMajor
     *            the core version (7 or 8)
     * @return the branch
     */
    private static String getStepBranch( PlatformStepCode code, int nCoreMajor )
    {
        int nParentMajorForDefault = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_BRANCH_PARENT_MAJOR_FOR_DEFAULT, 8 );
        if ( nCoreMajor >= nParentMajorForDefault )
        {
            return AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_BRANCH_DEFAULT );
        }

        return AppPropertiesService.getProperty( code.isCoreLineBranch( ) ? ConstanteUtils.PROPERTY_BRANCH_DEVELOPMENT_FOR_CORE7
                : ConstanteUtils.PROPERTY_BRANCH_DEVELOPMENT_FOR_LUTECE7 );
    }

    /**
     * Returns the artifactId aliases : name used in the version property to real artifactId.
     *
     * @return the aliases
     */
    private static Map<String, String> getArtifactIdAliases( )
    {
        Map<String, String> mapAliases = new LinkedHashMap<>( );
        String strAliases = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PLATFORM_ARTIFACT_ID_ALIASES );
        if ( StringUtils.isBlank( strAliases ) )
        {
            return mapAliases;
        }

        for ( String strAlias : strAliases.split( ALIASES_SEPARATOR ) )
        {
            String [ ] pair = strAlias.trim( ).split( COORDINATES_SEPARATOR );
            if ( pair.length == 2 )
            {
                mapAliases.put( pair [0].trim( ), pair [1].trim( ) );
            }
        }

        return mapAliases;
    }

    /**
     * Returns the Maven coordinates "groupId:artifactId" of a component.
     *
     * @param component
     *            the component
     * @return the coordinates
     */
    private static String getCoordinates( Component component )
    {
        return component.getGroupId( ) + COORDINATES_SEPARATOR + component.getArtifactId( );
    }

    /**
     * Whether a component of a step is flagged "to be released" in the Datastore.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param strArtifactId
     *            the component artifactId
     * @return true if flagged
     */
    private static boolean isProjectComponent( int nIdPlatformRelease, int nStepNumber, String strArtifactId )
    {
        return Boolean.parseBoolean( DatastoreService.getDataValue( getProjectDataKey( nIdPlatformRelease, nStepNumber, strArtifactId ), Boolean.FALSE.toString( ) ) );
    }

    /**
     * Returns the Datastore key of the "to be released" flag of a component of a step.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param strArtifactId
     *            the component artifactId
     * @return the key
     */
    private static String getProjectDataKey( int nIdPlatformRelease, int nStepNumber, String strArtifactId )
    {
        return getProjectDataKeyPrefix( nIdPlatformRelease ) + nStepNumber + DATA_KEY_SEPARATOR + strArtifactId;
    }

    /**
     * Returns the Datastore key prefix of the "to be released" flags of a campaign.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @return the prefix
     */
    private static String getProjectDataKeyPrefix( int nIdPlatformRelease )
    {
        return ConstanteUtils.CONSTANTE_PLATFORM_COMPONENT_PROJECT_PREFIX + nIdPlatformRelease + DATA_KEY_SEPARATOR;
    }

    /**
     * Returns the release branch of every aggregate for a core version, as deduced by the campaign (shown when a campaign is created).
     *
     * @param nCoreMajor
     *            the core major version
     * @return the branch by step code, in step order
     */
    public static Map<PlatformStepCode, String> getStepBranches( int nCoreMajor )
    {
        Map<PlatformStepCode, String> mapBranches = new LinkedHashMap<>( );
        for ( PlatformStepCode code : PlatformStepCode.values( ) )
        {
            mapBranches.put( code, getStepBranch( code, nCoreMajor ) );
        }

        return mapBranches;
    }

    /**
     * Returns the pipeline parameters chosen on the screen of a step, by Jenkins parameter name (only the ones set).
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @return the parameters
     */
    public static Map<String, String> getPipelineParameters( int nIdPlatformRelease, int nStepNumber )
    {
        Map<String, String> mapParameters = new LinkedHashMap<>( );
        for ( String strName : PlatformStepTask.EDITABLE_PARAMETERS )
        {
            String strValue = DatastoreService.getDataValue( getPipelineParameterKey( nIdPlatformRelease, nStepNumber, strName ), null );
            if ( strValue != null )
            {
                mapParameters.put( strName, strValue );
            }
        }

        return mapParameters;
    }

    /**
     * Saves the pipeline parameters chosen on the screen of a step ; a null value removes the parameter.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param mapParameters
     *            the parameters, by Jenkins parameter name
     */
    public static void savePipelineParameters( int nIdPlatformRelease, int nStepNumber, Map<String, String> mapParameters )
    {
        for ( Map.Entry<String, String> entry : mapParameters.entrySet( ) )
        {
            String strKey = getPipelineParameterKey( nIdPlatformRelease, nStepNumber, entry.getKey( ) );
            if ( entry.getValue( ) == null )
            {
                DatastoreService.removeData( strKey );
            }
            else
            {
                DatastoreService.setDataValue( strKey, entry.getValue( ) );
            }
        }
    }

    /**
     * Returns the pipeline parameters shown on the screen of a step : the chosen ones (target, SNAPSHOT tolerance), completed with the values
     * computed from the campaign and the prepared aggregate (core, next snapshot), shown for information only.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param site
     *            the prepared aggregate
     * @return the parameters, by Jenkins parameter name
     */
    public static Map<String, String> getEffectivePipelineParameters( PlatformRelease campaign, int nStepNumber, Site site )
    {
        Map<String, String> mapParameters = new LinkedHashMap<>( );
        mapParameters.put( PlatformStepTask.PARAM_LUTECE_MAJOR, Integer.toString( campaign.getCoreMajor( ) ) );
        mapParameters.put( PlatformStepTask.PARAM_RELEASE_TARGET, PlatformStepTask.RELEASE_TARGET_ALL );
        mapParameters.put( PlatformStepTask.PARAM_NEXT_SNAPSHOT_VERSION, StringUtils.defaultString( site.getNextSnapshotVersion( ) ) );
        mapParameters.put( PlatformStepTask.PARAM_ALLOW_SNAPSHOT_DEPENDENCIES, Boolean.FALSE.toString( ) );
        mapParameters.putAll( getPipelineParameters( campaign.getId( ), nStepNumber ) );

        return mapParameters;
    }

    /**
     * Copies the pipeline parameters chosen on the screen into the plan of a step before it is sent.
     *
     * @param campaign
     *            the campaign
     * @param nStepNumber
     *            the step number
     * @param plan
     *            the plan
     */
    public static void applyPipelineParameters( PlatformRelease campaign, int nStepNumber, PlatformReleasePlan plan )
    {
        plan.setPipelineParameters( getPipelineParameters( campaign.getId( ), nStepNumber ) );
    }

    /**
     * Returns the branch chosen on the screen of a step for its aggregate, in place of the one deduced from the core.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @return the branch, null when the deduced one applies
     */
    public static String getStepBranchOverride( int nIdPlatformRelease, int nStepNumber )
    {
        return DatastoreService.getDataValue( getStepBranchKey( nIdPlatformRelease, nStepNumber ), null );
    }

    /**
     * Saves the branch chosen on the screen of a step for its aggregate ; null restores the branch deduced from the core.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param strBranch
     *            the branch
     */
    public static void saveStepBranch( int nIdPlatformRelease, int nStepNumber, String strBranch )
    {
        String strKey = getStepBranchKey( nIdPlatformRelease, nStepNumber );
        if ( strBranch == null )
        {
            DatastoreService.removeData( strKey );
        }
        else
        {
            DatastoreService.setDataValue( strKey, strBranch );
        }
    }

    /**
     * Datastore key of the branch chosen for the aggregate of a step ; shares the prefix of the campaign so that removing the campaign
     * removes it.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @return the key
     */
    private static String getStepBranchKey( int nIdPlatformRelease, int nStepNumber )
    {
        return getProjectDataKeyPrefix( nIdPlatformRelease ) + nStepNumber + DATA_KEY_SEPARATOR + STEP_BRANCH_KEY;
    }

    /**
     * Datastore key of a pipeline parameter of a step ; shares the prefix of the campaign so that removing the campaign removes it.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param strName
     *            the Jenkins parameter name
     * @return the key
     */
    private static String getPipelineParameterKey( int nIdPlatformRelease, int nStepNumber, String strName )
    {
        return getProjectDataKeyPrefix( nIdPlatformRelease ) + nStepNumber + DATA_KEY_SEPARATOR + ConstanteUtils.CONSTANTE_PLATFORM_PIPELINE_PARAMETER_PREFIX
                + strName;
    }
}
