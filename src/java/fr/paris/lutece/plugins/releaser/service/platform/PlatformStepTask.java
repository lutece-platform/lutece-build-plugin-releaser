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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformPlanResource;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformRelease;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseHome;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleasePlan;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStep;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseType;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepCode;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepResult;
import fr.paris.lutece.plugins.releaser.service.IJenkinsService;
import fr.paris.lutece.plugins.releaser.service.JenkinsBuild;
import fr.paris.lutece.plugins.releaser.service.JenkinsService;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.MapperJsonUtil;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

/**
 * Background execution of a platform step : triggers the Jenkins job with the plan, follows the build and records its result on the step.
 */
public class PlatformStepTask implements Runnable
{
    /** Parameter of the generic step pipeline : the plan as JSON */
    static final String PARAM_RELEASE_PLAN = "RELEASE_PLAN";

    /** Parameters of the existing lutece-platform release pipeline */
    public static final String PARAM_RELEASE_TARGET = "RELEASE_TARGET";
    static final String PARAM_RELEASE_VERSION = "RELEASE_VERSION";
    static final String PARAM_NEXT_SNAPSHOT_VERSION = "NEXT_SNAPSHOT_VERSION";
    static final String PARAM_DRY_RUN = "DRY_RUN";
    static final String PARAM_PARENT_VERSION = "PARENT_VERSION";
    static final String PARAM_CORE_VERSION = "CORE_VERSION";
    static final String PARAM_PRERELEASE_TYPE = "PRERELEASE_TYPE";
    static final String PARAM_PRERELEASE_NUMBER = "PRERELEASE_NUMBER";
    public static final String PARAM_LUTECE_MAJOR = "LUTECE_MAJOR";
    static final String PARAM_MONOREPO_BRANCH = "MONOREPO_BRANCH";
    static final String PARAM_MASTER_BRANCH = "MASTER_BRANCH";
    public static final String PARAM_ALLOW_SNAPSHOT_DEPENDENCIES = "ALLOW_SNAPSHOT_DEPENDENCIES";
    static final String PARAM_GIT_USER_NAME = "GIT_USER_NAME";
    static final String PARAM_GIT_USER_EMAIL = "GIT_USER_EMAIL";

    /** Parameters of the lutece-platform pipeline that can be changed on the screen of the last step ; the others come from the campaign */
    public static final List<String> EDITABLE_PARAMETERS = Collections.unmodifiableList( Arrays.asList( PARAM_RELEASE_TARGET, PARAM_ALLOW_SNAPSHOT_DEPENDENCIES ) );

    public static final String RELEASE_TARGET_ALL = "all";
    static final String PRERELEASE_STABLE = "stable";
    static final String PRERELEASE_BETA = "beta";
    static final String PRERELEASE_RC = "rc";

    /** Identity of the commits made by the pipelines, the same as the step pipeline defaults */
    static final String GIT_USER_NAME = "releaser";
    static final String GIT_USER_EMAIL = "releaser@paris.fr";

    /** Report archived by the step pipeline */
    static final String STEP_REPORT_ARTIFACT = "step-report.json";

    /** Report archived by the lutece-platform pipeline */
    static final String PLATFORM_REPORT_ARTIFACT = "release-report.txt";

    private static final String JOB_PATH_SEPARATOR = "/job/";

    private static final String RESULT_SUCCESS = "SUCCESS";
    private static final String RESULT_UNSTABLE = "UNSTABLE";
    private static final String RESULT_ERROR_PREFIX = "ERROR : ";
    private static final String QUALIFIER_SEPARATOR = "-";
    private static final long BUILD_POLL_DELAY = 8000L;

    /** Credentials of the person releasing, sent to the pipelines as build parameters, never stored with the plan */
    static final String PARAM_GITHUB_LOGIN = "GITHUB_LOGIN";
    static final String PARAM_GITHUB_TOKEN = "GITHUB_TOKEN";
    static final String PARAM_GITLAB_LOGIN = "GITLAB_LOGIN";
    static final String PARAM_GITLAB_TOKEN = "GITLAB_TOKEN";

    private static final int MAX_POLL_FAILURES = 40;
    private static final long MAX_QUEUE_WAIT = 60L * 60L * 1000L;
    private static final long MAX_BUILD_DURATION = 6L * 60L * 60L * 1000L;
    private static final String RESULT_JENKINS_UNREACHABLE = "ERROR : Jenkins unreachable, the build may still run : rearm the step once Jenkins is back";
    private static final String RESULT_BUILD_TOO_LONG = "ERROR : build still running after 6 hours : rearm the step to follow it again";
    private static final String RESULT_QUEUE_TOO_LONG = "ERROR : build not started by Jenkins within 1 hour";

    private static final ConcurrentMap<Integer, PlatformStepTask> LIVE_TASKS = new ConcurrentHashMap<>( );

    private final int _nIdPlatformRelease;
    private final int _nStepNumber;
    private final PlatformReleasePlan _plan;
    private final Map<String, String> _mapCredentialParameters;
    private final String _strBuildUrl;
    private final int _nBuildNumber;

    /**
     * Constructor.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param plan
     *            the plan to send
     * @param mapCredentialParameters
     *            the credentials of the person releasing, as build parameters (see {@link #buildCredentialParameters(ReleaserUser)})
     */
    public PlatformStepTask( int nIdPlatformRelease, int nStepNumber, PlatformReleasePlan plan, Map<String, String> mapCredentialParameters )
    {
        this( nIdPlatformRelease, nStepNumber, plan, mapCredentialParameters, null, 0 );
    }

    /**
     * Constructor of a task that follows an already triggered build again, after a restart of the webapp or a lost connection : nothing is
     * triggered, the result of the known build is awaited and recorded as usual.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param plan
     *            the plan sent with the build, read back from the step
     * @param strBuildUrl
     *            the URL of the build to follow
     * @param nBuildNumber
     *            its number
     */
    public PlatformStepTask( int nIdPlatformRelease, int nStepNumber, PlatformReleasePlan plan, String strBuildUrl, int nBuildNumber )
    {
        this( nIdPlatformRelease, nStepNumber, plan, Collections.<String, String> emptyMap( ), strBuildUrl, nBuildNumber );
    }

    /**
     * Constructor.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @param plan
     *            the plan
     * @param mapCredentialParameters
     *            the credential parameters, empty when nothing is triggered
     * @param strBuildUrl
     *            the build to follow again, null to trigger a new one
     * @param nBuildNumber
     *            the number of that build
     */
    private PlatformStepTask( int nIdPlatformRelease, int nStepNumber, PlatformReleasePlan plan, Map<String, String> mapCredentialParameters,
            String strBuildUrl, int nBuildNumber )
    {
        _nIdPlatformRelease = nIdPlatformRelease;
        _nStepNumber = nStepNumber;
        _plan = plan;
        _mapCredentialParameters = mapCredentialParameters;
        _strBuildUrl = strBuildUrl;
        _nBuildNumber = nBuildNumber;
    }

    /**
     * Whether a task of this webapp is following the build of a step right now. False after a restart : the step must be rearmed.
     *
     * @param nIdStep
     *            the step id
     * @return true when followed
     */
    public static boolean isFollowed( int nIdStep )
    {
        return LIVE_TASKS.containsKey( nIdStep );
    }

    /**
     * Builds the build parameters carrying the credentials of the person releasing : login and token of GitHub and GitLab when the user
     * gave them. The pipelines clone and push with them, so that nothing is pushed beyond the rights of that person. They are sent with the
     * plan but never stored with it nor logged.
     *
     * @param user
     *            the releaser user, null when not authenticated
     * @return the parameters, empty when the user gave no credentials
     */
    public static Map<String, String> buildCredentialParameters( ReleaserUser user )
    {
        Map<String, String> mapParams = new LinkedHashMap<>( );
        if ( user == null )
        {
            return mapParams;
        }
        putCredential( mapParams, user.getCredential( RepositoryType.GITHUB ), PARAM_GITHUB_LOGIN, PARAM_GITHUB_TOKEN );
        putCredential( mapParams, user.getCredential( RepositoryType.GITLAB ), PARAM_GITLAB_LOGIN, PARAM_GITLAB_TOKEN );

        return mapParams;
    }

    /**
     * Adds the login and token parameters of a credential, when present.
     *
     * @param mapParams
     *            the parameters
     * @param credential
     *            the credential, null tolerated
     * @param strLoginParam
     *            the name of the login parameter
     * @param strTokenParam
     *            the name of the token parameter
     */
    private static void putCredential( Map<String, String> mapParams, ReleaserUser.Credential credential, String strLoginParam, String strTokenParam )
    {
        if ( credential != null && StringUtils.isNotBlank( credential.getLogin( ) ) && StringUtils.isNotBlank( credential.getPassword( ) ) )
        {
            mapParams.put( strLoginParam, credential.getLogin( ) );
            mapParams.put( strTokenParam, credential.getPassword( ) );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void run( )
    {
        PlatformRelease campaign = PlatformReleaseHome.findByPrimaryKeyWithSteps( _nIdPlatformRelease );
        PlatformReleaseStep step = campaign != null ? PlatformReleaseService.getStep( campaign, _nStepNumber ) : null;
        if ( step == null )
        {
            AppLogService.error( "Releaser : platform step " + _nStepNumber + " of campaign " + _nIdPlatformRelease + " not found, Jenkins job not triggered" );
            return;
        }

        LIVE_TASKS.put( step.getId( ), this );
        try
        {
            IJenkinsService jenkins = JenkinsService.getService( );
            JenkinsBuild build;
            if ( _strBuildUrl != null )
            {
                build = new JenkinsBuild( _strBuildUrl, _nBuildNumber );
            }
            else
            {
                Map<String, String> mapParams = buildJobParameters( _plan );
                mapParams.putAll( _mapCredentialParameters );
                String strQueueUrl = jenkins.triggerJob( getJobPath( _plan ), mapParams );
                build = waitForQueuedBuild( jenkins, strQueueUrl );
                PlatformReleaseService.attachBuild( step, build.getUrl( ), build.getNumber( ) );
            }

            String strResult = waitForResult( jenkins, build.getUrl( ) );

            if ( _plan.isDryRun( ) )
            {
                PlatformReleaseService.finishDryRun( campaign, step, strResult, readReportText( jenkins, build ) );
            }
            else
                if ( isPublished( strResult ) )
                {
                    PlatformStepResult result = readStepResult( jenkins, build );
                    PlatformReleaseService.completeStep( campaign, step, result, strResult );
                    PlatformReleaseService.updateBugtrackerVersions( step, _plan, result );
                }
                else
                {
                    PlatformStepResult partialResult = readPartialResult( jenkins, build );
                    PlatformReleaseService.failStep( campaign, step, strResult, partialResult );
                    PlatformReleaseService.updateBugtrackerVersions( step, _plan, partialResult );
                }
        }
        catch( Exception e )
        {
            AppLogService.error( "Releaser : platform step " + _nStepNumber + " of campaign " + _nIdPlatformRelease + " failed : " + e.getMessage( ), e );
            markFailed( campaign, step, e instanceof FollowUpException ? e.getMessage( ) : RESULT_ERROR_PREFIX + e.getMessage( ) );
        }
        finally
        {
            LIVE_TASKS.remove( step.getId( ) );
        }
    }

    /**
     * Waits for the queued build to start, tolerating Jenkins being unreachable for a while : a restart of Jenkins or a network cut must not
     * turn a build that will run into a failed step.
     *
     * @param jenkins
     *            the Jenkins service
     * @param strQueueUrl
     *            the queue item URL
     * @return the started build
     * @throws InterruptedException
     *             when the task is interrupted
     * @throws FollowUpException
     *             when Jenkins stays unreachable or the build does not start in time
     */
    private JenkinsBuild waitForQueuedBuild( IJenkinsService jenkins, String strQueueUrl ) throws InterruptedException
    {
        long lDeadline = System.currentTimeMillis( ) + MAX_QUEUE_WAIT;
        int nFailures = 0;
        while ( System.currentTimeMillis( ) < lDeadline )
        {
            try
            {
                return jenkins.waitForBuild( strQueueUrl );
            }
            catch( HttpAccessException | IOException e )
            {
                nFailures = logPollFailure( strQueueUrl, nFailures, e );
            }
            Thread.sleep( BUILD_POLL_DELAY );
        }
        throw new FollowUpException( RESULT_QUEUE_TOO_LONG );
    }

    /**
     * Waits for the result of a build, tolerating Jenkins being unreachable for a while. The build keeps running on Jenkins whatever happens
     * here : when the follow-up has to give up, the message tells to rearm the step, which follows the same build again.
     *
     * @param jenkins
     *            the Jenkins service
     * @param strBuildUrl
     *            the build URL
     * @return the build result
     * @throws InterruptedException
     *             when the task is interrupted
     * @throws FollowUpException
     *             when Jenkins stays unreachable or the build lasts too long
     */
    private String waitForResult( IJenkinsService jenkins, String strBuildUrl ) throws InterruptedException
    {
        long lDeadline = System.currentTimeMillis( ) + MAX_BUILD_DURATION;
        int nFailures = 0;
        while ( System.currentTimeMillis( ) < lDeadline )
        {
            Thread.sleep( BUILD_POLL_DELAY );
            try
            {
                String strResult = jenkins.getBuildResult( strBuildUrl );
                nFailures = 0;
                if ( strResult != null )
                {
                    return strResult;
                }
            }
            catch( HttpAccessException | IOException e )
            {
                nFailures = logPollFailure( strBuildUrl, nFailures, e );
            }
        }
        throw new FollowUpException( RESULT_BUILD_TOO_LONG );
    }

    /**
     * Counts a failed call to Jenkins and gives up after too many in a row.
     *
     * @param strUrl
     *            the URL called
     * @param nFailures
     *            the failures in a row so far
     * @param e
     *            the failure
     * @return the failures in a row
     * @throws FollowUpException
     *             after too many failures in a row
     */
    private static int logPollFailure( String strUrl, int nFailures, Exception e )
    {
        int nCount = nFailures + 1;
        AppLogService.error( "Releaser : Jenkins unreachable (" + nCount + "/" + MAX_POLL_FAILURES + ") on " + strUrl + " : " + e.getMessage( ) );
        if ( nCount >= MAX_POLL_FAILURES )
        {
            throw new FollowUpException( RESULT_JENKINS_UNREACHABLE );
        }

        return nCount;
    }

    /**
     * The follow-up of a build had to give up : the message is the result to store, already worded for the user.
     */
    private static final class FollowUpException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param strMessage
         *            the result to store
         */
        FollowUpException( String strMessage )
        {
            super( strMessage );
        }
    }

    /**
     * Records the failure of the step. Nothing may escape from here : an exception while storing the failure would leave the step RUNNING
     * with no way to rearm it.
     *
     * @param campaign
     *            the campaign
     * @param step
     *            the step
     * @param strResult
     *            the result to store
     */
    private void markFailed( PlatformRelease campaign, PlatformReleaseStep step, String strResult )
    {
        try
        {
            PlatformReleaseService.failStep( campaign, step, strResult );
        }
        catch( RuntimeException e )
        {
            AppLogService.error( "Releaser : unable to record the failure of platform step " + _nStepNumber + " of campaign " + _nIdPlatformRelease, e );
        }
    }

    /**
     * Whether a Jenkins result means the step published its release : SUCCESS, or UNSTABLE, the result the lutece-platform pipeline gives when
     * it publishes despite a warning (SNAPSHOT dependencies allowed, version mismatch) ; the warning stays in its report.
     *
     * @param strResult
     *            the Jenkins result
     * @return true when the release was published
     */
    static boolean isPublished( String strResult )
    {
        return RESULT_SUCCESS.equals( strResult ) || RESULT_UNSTABLE.equals( strResult );
    }

    /**
     * Returns the Jenkins job of a step : the generic step pipeline, or the existing lutece-platform pipeline for the last step, followed by
     * the release branch of the aggregate when that pipeline is a multibranch job.
     *
     * @param plan
     *            the plan
     * @return the job path
     */
    static String getJobPath( PlatformReleasePlan plan )
    {
        if ( plan.getStepCode( ).getExecutor( ) != PlatformStepCode.Executor.PLATFORM_PIPELINE )
        {
            return AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PLATFORM_JENKINS_STEP_JOB );
        }
        String strJobPath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PLATFORM_JENKINS_PLATFORM_JOB );
        if ( AppPropertiesService.getPropertyBoolean( ConstanteUtils.PROPERTY_PLATFORM_JENKINS_PLATFORM_JOB_BY_BRANCH, true ) && plan.getAggregate( ) != null
                && StringUtils.isNotBlank( plan.getAggregate( ).getBranch( ) ) )
        {
            return strJobPath + JOB_PATH_SEPARATOR + encode( plan.getAggregate( ).getBranch( ) );
        }

        return strJobPath;
    }

    /**
     * Encodes a branch name for a Jenkins job URL.
     *
     * @param strBranch
     *            the branch
     * @return the encoded branch
     */
    private static String encode( String strBranch )
    {
        try
        {
            return URLEncoder.encode( strBranch, StandardCharsets.UTF_8.name( ) );
        }
        catch( UnsupportedEncodingException e )
        {
            return strBranch;
        }
    }

    /**
     * Builds the job parameters of a plan : the whole plan as JSON for the step pipeline, the parameters of the existing lutece-platform
     * pipeline for the last step.
     *
     * @param plan
     *            the plan
     * @return the parameters
     * @throws IOException
     *             if the plan cannot be serialized
     */
    static Map<String, String> buildJobParameters( PlatformReleasePlan plan ) throws IOException
    {
        Map<String, String> mapParams = new LinkedHashMap<>( );

        if ( plan.getStepCode( ).getExecutor( ) != PlatformStepCode.Executor.PLATFORM_PIPELINE )
        {
            mapParams.put( PARAM_RELEASE_PLAN, MapperJsonUtil.getJson( plan ) );
            return mapParams;
        }

        PlatformPlanResource aggregate = plan.getAggregate( );
        mapParams.put( PARAM_RELEASE_TARGET, RELEASE_TARGET_ALL );
        mapParams.put( PARAM_RELEASE_VERSION, aggregate.getTargetVersion( ) );
        mapParams.put( PARAM_NEXT_SNAPSHOT_VERSION, aggregate.getNextSnapshotVersion( ) );
        mapParams.put( PARAM_DRY_RUN, Boolean.toString( plan.isDryRun( ) ) );
        mapParams.put( PARAM_PRERELEASE_TYPE, getPrereleaseType( plan.getReleaseType( ) ) );
        mapParams.put( PARAM_PRERELEASE_NUMBER, getPrereleaseNumber( aggregate.getTargetVersion( ) ) );
        mapParams.put( PARAM_LUTECE_MAJOR, Integer.toString( plan.getCoreMajor( ) ) );
        mapParams.put( PARAM_MONOREPO_BRANCH, aggregate.getBranch( ) );
        mapParams.put( PARAM_MASTER_BRANCH, StringUtils.defaultString( aggregate.getMasterBranch( ) ) );
        mapParams.put( PARAM_PARENT_VERSION, StringUtils.defaultString( aggregate.getParentVersion( ) ) );
        mapParams.put( PARAM_CORE_VERSION, StringUtils.defaultString( aggregate.getCoreVersion( ) ) );
        mapParams.put( PARAM_ALLOW_SNAPSHOT_DEPENDENCIES, Boolean.FALSE.toString( ) );
        mapParams.put( PARAM_GIT_USER_NAME, GIT_USER_NAME );
        mapParams.put( PARAM_GIT_USER_EMAIL, GIT_USER_EMAIL );
        mapParams.putAll( plan.getPipelineParameters( ) );

        return mapParams;
    }

    /**
     * Returns the release type expected by the lutece-platform pipeline, which refuses a build without one.
     *
     * @param releaseType
     *            the release type
     * @return stable, beta or rc
     * @throws AppException
     *             if the campaign has no release type
     */
    static String getPrereleaseType( PlatformReleaseType releaseType )
    {
        if ( releaseType == null )
        {
            throw new AppException( "Releaser : the release type of the campaign is required to launch the lutece-platform pipeline" );
        }
        if ( releaseType == PlatformReleaseType.BETA )
        {
            return PRERELEASE_BETA;
        }
        if ( releaseType == PlatformReleaseType.RC )
        {
            return PRERELEASE_RC;
        }

        return PRERELEASE_STABLE;
    }

    /**
     * Returns the number of a pre-release version (02 for 8.0.0-beta-02), empty for a stable version.
     *
     * @param strVersion
     *            the version
     * @return the pre-release number
     */
    static String getPrereleaseNumber( String strVersion )
    {
        if ( strVersion == null || !( Version.isCandidate( strVersion ) || Version.isBeta( strVersion ) ) )
        {
            return "";
        }

        return strVersion.substring( strVersion.lastIndexOf( QUALIFIER_SEPARATOR ) + 1 );
    }

    /**
     * Reads the report archived by the step pipeline ; falls back on the plan (every resource released at its target version) when the report
     * cannot be read.
     *
     * @param jenkins
     *            the Jenkins service
     * @param build
     *            the finished build
     * @return the step result
     */
    private PlatformStepResult readStepResult( IJenkinsService jenkins, JenkinsBuild build )
    {
        if ( _plan.getStepCode( ).getExecutor( ) == PlatformStepCode.Executor.PLATFORM_PIPELINE )
        {
            PlatformStepResult result = buildResultFromPlan( _plan );
            String strReport = readPlatformReport( jenkins, build );
            if ( strReport != null )
            {
                result.setReport( strReport );
            }
            return result;
        }

        PlatformStepResult result = readPartialResult( jenkins, build );
        if ( result == null )
        {
            AppLogService.error( "Releaser : no readable " + STEP_REPORT_ARTIFACT + " for build " + build.getUrl( ) + ", result taken from the plan" );
            result = buildResultFromPlan( _plan );
        }

        return result;
    }

    /**
     * Reads the text of the report archived by the pipeline of the step, whatever the pipeline, to keep it after a simulation.
     *
     * @param jenkins
     *            the Jenkins service
     * @param build
     *            the finished build
     * @return the report text, null when it cannot be read
     */
    private String readReportText( IJenkinsService jenkins, JenkinsBuild build )
    {
        if ( _plan.getStepCode( ).getExecutor( ) == PlatformStepCode.Executor.PLATFORM_PIPELINE )
        {
            return readPlatformReport( jenkins, build );
        }
        PlatformStepResult result = readPartialResult( jenkins, build );

        return result != null ? result.getReport( ) : null;
    }

    /**
     * Reads the text report archived by the lutece-platform pipeline.
     *
     * @param jenkins
     *            the Jenkins service
     * @param build
     *            the finished build
     * @return the report, null when it cannot be read
     */
    private String readPlatformReport( IJenkinsService jenkins, JenkinsBuild build )
    {
        try
        {
            return jenkins.readArtifact( build.getUrl( ), PLATFORM_REPORT_ARTIFACT );
        }
        catch( Exception e )
        {
            AppLogService.error( "Releaser : unreadable " + PLATFORM_REPORT_ARTIFACT + " of build " + build.getUrl( ) + " : " + e.getMessage( ) );
            return null;
        }
    }

    /**
     * Reads the report archived by the step pipeline, written after each released component so that a failed build still lists what it
     * released. For the lutece-platform pipeline of the last step, only its text report is kept : nothing was released when it fails.
     *
     * @param jenkins
     *            the Jenkins service
     * @param build
     *            the finished build
     * @return the report, null when there is none or it cannot be read
     */
    private PlatformStepResult readPartialResult( IJenkinsService jenkins, JenkinsBuild build )
    {
        if ( _plan.getStepCode( ).getExecutor( ) == PlatformStepCode.Executor.PLATFORM_PIPELINE )
        {
            String strReport = readPlatformReport( jenkins, build );
            if ( strReport == null )
            {
                return null;
            }
            PlatformStepResult result = new PlatformStepResult( );
            result.setReport( strReport );
            return result;
        }

        try
        {
            return MapperJsonUtil.parse( jenkins.readArtifact( build.getUrl( ), STEP_REPORT_ARTIFACT ), PlatformStepResult.class );
        }
        catch( Exception e )
        {
            AppLogService.error( "Releaser : unreadable " + STEP_REPORT_ARTIFACT + " of build " + build.getUrl( ) + " : " + e.getMessage( ) );
            return null;
        }
    }

    /**
     * Builds the result of a successful step from its plan : every component released at its target version, the aggregate too.
     *
     * @param plan
     *            the plan
     * @return the result
     */
    static PlatformStepResult buildResultFromPlan( PlatformReleasePlan plan )
    {
        PlatformStepResult result = new PlatformStepResult( );
        for ( PlatformPlanResource component : plan.getComponents( ) )
        {
            result.getReleasedVersions( ).put( component.getCoordinates( ), component.getTargetVersion( ) );
        }
        if ( plan.getAggregate( ) != null )
        {
            result.setAggregateVersion( plan.getAggregate( ).getTargetVersion( ) );
        }
        result.setReport( "Result taken from the plan, no " + STEP_REPORT_ARTIFACT + " read from Jenkins." );

        return result;
    }
}
