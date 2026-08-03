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
package fr.paris.lutece.plugins.releaser.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.Params;
import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.UserManager;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Version;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.json.ErrorJsonResponse;

/**
 * Redmine bugtracker integration : on a component release, creates the next open version, moves the
 * still-open issues of the released version onto it, then closes the released version.
 *
 * Connection pattern (createWithApiKey, no proxy) mirrors the read-only RedmineService of the
 * lutecetools plugin ; the write operations below are specific to the release workflow.
 */
public class RedmineComponentService implements IBugtrackerService
{

    /** The Constant CONSTANTE_SNAPSHOT_VERSION. */
    private static final String CONSTANTE_SNAPSHOT_VERSION = "-SNAPSHOT";

    /** Maximum page size as per the Redmine REST API. */
    private static final int LIMIT = 100;

    /** Default id of the "Sources" project custom field (holds the repository URL). */
    private static final int DEFAULT_SOURCES_CF_ID = 14;

    /** Default TTL of the projects index, in minutes. */
    private static final int DEFAULT_INDEX_TTL_MINUTES = 120;

    /** Minimum delay between two index build attempts after a failure, in ms. */
    private static final long INDEX_RETRY_DELAY_MS = 60000L;

    /** Default TTL of the issue counters cache, in minutes. */
    private static final int DEFAULT_COUNTERS_TTL_MINUTES = 10;

    private IssueManager _issueManager;
    private ProjectManager _projectManager;
    private UserManager _userManager;

    /** Projects index : normalized repository URL -> project reference. Null until first successful build. */
    private volatile Map<String, ProjectRef> _mapProjectsByRepoUrl;
    private volatile long _lIndexTime;
    private volatile long _lLastBuildAttempt;
    private final Object _indexLock = new Object( );

    /** Indexed project reference : identifier (string key) and numeric id. */
    private static final class ProjectRef
    {
        private final String _strIdentifier;
        private final int _nId;

        ProjectRef( String strIdentifier, int nId )
        {
            _strIdentifier = strIdentifier;
            _nId = nId;
        }
    }

    /** Issue counters cache : "projectId/versionName" -> counts. Entries expire on the counters TTL. */
    private final Map<String, IssueCounts> _mapIssueCounts = new ConcurrentHashMap<>( );

    /** Cached issue counters of a project version (-1 = unknown, e.g. version not found). */
    private static final class IssueCounts
    {
        private final int _nClosed;
        private final int _nOpened;
        private final long _lTime = System.currentTimeMillis( );

        IssueCounts( int nClosed, int nOpened )
        {
            _nClosed = nClosed;
            _nOpened = nOpened;
        }
    }

    /**
     * Constructor.
     */
    public RedmineComponentService( )
    {

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void init( )
    {
        String strUrl = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_URL_BUGTRACKER_SERVICE );
        String strApiKey = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_BUGTRACKER_API_KEY );

        // Redmine is internal (same host as the Lutece Maven repository) : direct access, no proxy.
        RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey( strUrl, strApiKey );
        _issueManager = redmineManager.getIssueManager( );
        _projectManager = redmineManager.getProjectManager( );
        _userManager = redmineManager.getUserManager( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isReachable( )
    {
        try
        {
            _userManager.getCurrentUser( );
            return true;
        }
        catch( Exception ex )
        {
            AppLogService.error( "Releaser : Redmine unreachable : " + ex.getMessage( ) );
            return false;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean hasProjectIndex( )
    {
        ensureProjectIndex( );
        return _mapProjectsByRepoUrl != null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String findProjectIdentifier( String strScmUrl )
    {
        ProjectRef ref = findProjectRef( strScmUrl );

        return ( ref != null ) ? ref._strIdentifier : null;
    }

    /**
     * Finds the indexed project reference matching a repository URL.
     *
     * @param strScmUrl
     *            the component SCM URL (scm:git:... accepted)
     * @return the project reference, or null if none matches or no index is available
     */
    private ProjectRef findProjectRef( String strScmUrl )
    {
        ensureProjectIndex( );
        Map<String, ProjectRef> mapIndex = _mapProjectsByRepoUrl;
        String strKey = normalizeRepoUrl( strScmUrl );

        return ( mapIndex != null && strKey != null ) ? mapIndex.get( strKey ) : null;
    }

    /**
     * Builds the projects index if missing or older than the TTL. On failure the previous index (possibly stale) is kept.
     */
    private void ensureProjectIndex( )
    {
        long lTtl = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_BUGTRACKER_PROJECTS_CACHE_TTL, DEFAULT_INDEX_TTL_MINUTES ) * 60000L;

        if ( _mapProjectsByRepoUrl != null && ( System.currentTimeMillis( ) - _lIndexTime ) < lTtl )
        {
            return;
        }

        synchronized( _indexLock )
        {
            if ( ( _mapProjectsByRepoUrl != null && ( System.currentTimeMillis( ) - _lIndexTime ) < lTtl )
                    || ( System.currentTimeMillis( ) - _lLastBuildAttempt ) < INDEX_RETRY_DELAY_MS )
            {
                return;
            }
            _lLastBuildAttempt = System.currentTimeMillis( );

            try
            {
                _mapProjectsByRepoUrl = buildProjectIndex( );
                _lIndexTime = System.currentTimeMillis( );
            }
            catch( Exception ex )
            {
                AppLogService.error( "Releaser : unable to build the Redmine projects index : " + ex.getMessage( ), ex );
            }
        }
    }

    /**
     * Builds the index normalized repository URL -> project reference from the "Sources" custom field and the homepage.
     *
     * @return the index
     * @throws RedmineException
     *             if the Redmine call fails
     */
    private Map<String, ProjectRef> buildProjectIndex( ) throws RedmineException
    {
        int nSourcesCfId = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_BUGTRACKER_SOURCES_CF_ID, DEFAULT_SOURCES_CF_ID );
        Map<String, ProjectRef> mapIndex = new ConcurrentHashMap<>( );
        Set<String> setAmbiguous = new HashSet<>( );

        for ( Project project : _projectManager.getProjects( ) )
        {
            CustomField cfSources = project.getCustomFieldById( nSourcesCfId );
            String [ ] tabUrls = {
                    ( cfSources != null ) ? cfSources.getValue( ) : null, project.getHomepage( )
            };

            for ( String strRawUrl : tabUrls )
            {
                String strKey = normalizeRepoUrl( strRawUrl );
                if ( strKey == null || setAmbiguous.contains( strKey ) )
                {
                    continue;
                }
                ProjectRef existing = mapIndex.putIfAbsent( strKey, new ProjectRef( project.getIdentifier( ), project.getId( ) ) );
                if ( existing != null && !existing._strIdentifier.equals( project.getIdentifier( ) ) )
                {
                    // Same repository URL on several projects : unreliable, ignore it.
                    mapIndex.remove( strKey );
                    setAmbiguous.add( strKey );
                    AppLogService.info( "Releaser : ambiguous Redmine repository URL " + strKey + " (projects " + existing._strIdentifier + ", "
                            + project.getIdentifier( ) + ") : ignored." );
                }
            }
        }

        return mapIndex;
    }

    /**
     * Normalizes a repository URL for index lookups.
     *
     * @param strUrl
     *            the raw URL (scm:git:... accepted)
     * @return the normalized URL, or null if blank
     */
    static String normalizeRepoUrl( String strUrl )
    {
        String strCleaned = cleanRepoUrl( strUrl );

        return ( strCleaned != null ) ? strCleaned.toLowerCase( ) : null;
    }

    /**
     * Strips the scm:git: prefix, the .git suffix and the trailing slash, preserving the case.
     *
     * @param strUrl
     *            the raw URL
     * @return the cleaned URL, or null if blank
     */
    static String cleanRepoUrl( String strUrl )
    {
        if ( StringUtils.isBlank( strUrl ) )
        {
            return null;
        }
        String strCleaned = strUrl.trim( );
        strCleaned = StringUtils.removeStart( strCleaned, "scm:git:" );
        strCleaned = StringUtils.removeEnd( strCleaned, "/" );
        strCleaned = StringUtils.removeEnd( strCleaned, ".git" );

        return strCleaned;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void populateBugtrackerInfo( Component component )
    {
        RepositoryType repoType = component.getRepoType( );
        if ( ( !RepositoryType.GITHUB.equals( repoType ) && !RepositoryType.GITLAB.equals( repoType ) ) || !hasProjectIndex( ) )
        {
            return;
        }

        ProjectRef ref = findProjectRef( component.getScmUrl( ) );
        String strBaseUrl = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_URL_BUGTRACKER_SERVICE );

        if ( ref != null && StringUtils.isNotBlank( strBaseUrl ) )
        {
            component.setBugtrackerRoadmapUrl( StringUtils.removeEnd( strBaseUrl, "/" ) + "/projects/" + ref._strIdentifier + "/roadmap" );

            if ( AppPropertiesService.getPropertyBoolean( ConstanteUtils.PROPERTY_BUGTRACKER_ISSUE_COUNTERS_ENABLED, false )
                    && component.getCurrentVersion( ) != null )
            {
                IssueCounts counts = fetchIssueCounts( ref._nId, component.getCurrentVersion( ).replace( CONSTANTE_SNAPSHOT_VERSION, "" ) );
                if ( counts != null )
                {
                    component.setBugtrackerCurrentVersionClosedIssues( counts._nClosed );
                    component.setBugtrackerCurrentVersionOpenedIssues( counts._nOpened );
                }
            }
        }
        else if ( ref == null && RepositoryType.GITHUB.equals( repoType ) )
        {
            component.addReleaseComment( "Aucun projet Redmine ne correspond au repo du composant " + component.getArtifactId( )
                    + " : vous devez créer le projet Redmine manuellement ou releaser ce composant via le bouton release pour une création lors de la release." );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ErrorJsonResponse prepareReleaseInBugtracker( Component component, boolean bForce, boolean bCreateProject, boolean bSkipCreate,
            String strDescription, boolean bPublic )
    {
        // Out of the bugtracker scope (SVN, no scm) : nothing to check.
        RepositoryType repoType = component.getRepoType( );
        if ( !RepositoryType.GITHUB.equals( repoType ) && !RepositoryType.GITLAB.equals( repoType ) )
        {
            return null;
        }

        // Redmine down (live check) : the release may proceed but the tracker will not be updated.
        if ( !bForce && !isReachable( ) )
        {
            return new ErrorJsonResponse( "BUGTRACKER_DOWN",
                    "Le bugtracker Redmine est injoignable. Si vous continuez, la release sera faite mais Redmine ne sera PAS mis à jour"
                            + " (version et tickets à reprendre manuellement plus tard). Continuer quand même ?" );
        }

        // Project creation is only proposed for GitHub components ; a GitLab component without project is simply not tracked.
        if ( !bSkipCreate && RepositoryType.GITHUB.equals( repoType ) && findProjectIdentifier( component.getScmUrl( ) ) == null )
        {
            // No matching Redmine project : ask the user to confirm its creation.
            if ( !bCreateProject )
            {
                return new ErrorJsonResponse( "NO_BUGTRACKER_PROJECT", "Aucun projet Redmine ne correspond au repo du composant "
                        + component.getArtifactId( ) + ". Le projet va être créé dans Redmine avant la release." );
            }
            try
            {
                createProject( component, strDescription, bPublic );
            }
            catch( Exception ex )
            {
                // Creation failed : warn the user with the reason, the release continues without tracker update.
                AppLogService.error( "Releaser : Redmine project creation failed for " + component.getArtifactId( ) + " : " + ex.getMessage( ), ex );
                return new ErrorJsonResponse( "BUGTRACKER_CREATE_FAILED", "Le projet Redmine n'a pas pu être créé (" + ex.getMessage( )
                        + "). La release va continuer mais le bugtracker ne sera PAS mis à jour." );
            }
        }

        return null;
    }

    /**
     * Creates the Redmine project of a component (identifier = artifactId, "Sources" = repository URL) and registers it in the index.
     *
     * @param component
     *            the component
     * @param strDescription
     *            the project description entered by the user
     * @param bPublic
     *            whether the project is public
     * @throws RedmineException
     *             if the creation fails
     */
    private void createProject( Component component, String strDescription, boolean bPublic ) throws RedmineException
    {
        String strIdentifier = component.getArtifactId( ).toLowerCase( );
        String strRepoUrl = cleanRepoUrl( component.getScmUrl( ) );
        int nSourcesCfId = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_BUGTRACKER_SOURCES_CF_ID, DEFAULT_SOURCES_CF_ID );

        Project project = new Project( null, component.getArtifactId( ), strIdentifier );
        project.setHomepage( strRepoUrl );
        project.setProjectPublic( bPublic );
        if ( StringUtils.isNotBlank( strDescription ) )
        {
            project.setDescription( strDescription );
        }
        project.addCustomFields( Collections.singletonList( new CustomField( ).setId( nSourcesCfId ).setValue( strRepoUrl ) ) );

        Project createdProject = _projectManager.createProject( project );

        // Register in the live index so the release finds it right away.
        Map<String, ProjectRef> mapIndex = _mapProjectsByRepoUrl;
        String strKey = normalizeRepoUrl( strRepoUrl );
        if ( mapIndex != null && strKey != null )
        {
            mapIndex.put( strKey, new ProjectRef( createdProject.getIdentifier( ), createdProject.getId( ) ) );
        }

        AppLogService.info( "Releaser : Redmine project '" + strIdentifier + "' created for repo " + strRepoUrl );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void updateComponentVersions( Component component, CommandResult commandResult )
    {
        try
        {
            // GitHub and GitLab components may be tracked in Redmine ; the others are out of the bugtracker scope.
            RepositoryType repoType = component.getRepoType( );
            if ( !RepositoryType.GITHUB.equals( repoType ) && !RepositoryType.GITLAB.equals( repoType ) )
            {
                AppLogService.info( "Releaser : component " + component.getArtifactId( ) + " out of the bugtracker scope : no Redmine update." );
                return;
            }

            String strProjectKey = findProjectIdentifier( component.getScmUrl( ) );

            if ( strProjectKey == null && !hasProjectIndex( ) )
            {
                // Redmine down (the user chose to continue) : skip the tracker update.
                String strMessage = "AVERTISSEMENT : Redmine injoignable, le bugtracker n'a PAS été mis à jour (reprise manuelle nécessaire).";
                commandResult.getLog( ).append( strMessage + "\n" );
                AppLogService.error( "Releaser : Redmine unreachable, tracker update skipped for " + component.getArtifactId( ) );
                return;
            }

            if ( strProjectKey == null )
            {
                // GitLab component not tracked in Redmine : expected case, silent skip.
                if ( RepositoryType.GITLAB.equals( repoType ) )
                {
                    AppLogService.info( "Releaser : no Redmine project for GitLab component " + component.getArtifactId( ) + " : tracker update skipped." );
                    return;
                }
                // Missing GitHub project (creation refused or failed at launch) : skip the tracker update.
                String strMessage = "AVERTISSEMENT : aucun projet Redmine pour ce composant, le bugtracker n'a PAS été mis à jour.";
                commandResult.getLog( ).append( strMessage + "\n" );
                AppLogService.info( "Releaser : no Redmine project for " + component.getArtifactId( ) + " : tracker update skipped." );
                return;
            }

            String strCurrentVersion = component.getCurrentVersion( ).replace( CONSTANTE_SNAPSHOT_VERSION, "" );
            String strReleaseVersionName = component.getTargetVersion( );
            String strNewVersion = component.getNextSnapshotVersion( ).replace( CONSTANTE_SNAPSHOT_VERSION, "" );

            Project project = _projectManager.getProjectByKey( strProjectKey );
            int nProjectId = project.getId( );

            // 1. Create the next open version (N+1) if it does not exist yet.
            Version newVersion = null;
            if ( !StringUtils.isEmpty( strNewVersion ) )
            {
                newVersion = findVersionByName( nProjectId, strNewVersion );
                if ( newVersion == null )
                {
                    newVersion = _projectManager.createVersion( new Version( ).setProjectId( nProjectId ).setName( strNewVersion ) );
                }
            }

            // 2. Find the version currently in development, the one being released.
            Version currentVersion = findVersionByName( nProjectId, strCurrentVersion );

            if ( currentVersion != null && !Version.STATUS_CLOSED.equals( currentVersion.getStatus( ) ) )
            {
                // 3. Move the still-open issues of the released version onto the new one.
                if ( newVersion != null )
                {
                    for ( Issue issue : fetchOpenIssuesForVersion( nProjectId, currentVersion.getId( ) ) )
                    {
                        issue.setTargetVersion( newVersion );
                        _issueManager.update( issue );
                    }
                }

                // 4. Close the released version (rename to the release name if needed).
                if ( !StringUtils.isEmpty( strReleaseVersionName ) && !strReleaseVersionName.equals( currentVersion.getName( ) ) )
                {
                    currentVersion.setName( strReleaseVersionName );
                }
                currentVersion.setStatus( Version.STATUS_CLOSED );
                currentVersion.setDueDate( new Date( ) );
                _projectManager.update( currentVersion );
            }
        }
        catch( RedmineException ex )
        {
            ReleaserUtils.addInfoError( commandResult, "Error updating Redmine version : " + ex.getMessage( ), ex );
        }
        catch( Exception ex )
        {
            ReleaserUtils.addInfoError( commandResult, "Error using Redmine API : " + ex.getMessage( ), ex );
        }
    }

    /**
     * Counts the closed and opened issues of a project version, through the TTL cache.
     * The "version not found" case is cached too (as unknown counts) to avoid re-querying on every display.
     *
     * @param nProjectId
     *            the numeric project id
     * @param strVersionName
     *            the version name
     * @return the counts (possibly unknown), or null if the count failed
     */
    private IssueCounts fetchIssueCounts( int nProjectId, String strVersionName )
    {
        long lTtl = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_BUGTRACKER_COUNTERS_CACHE_TTL, DEFAULT_COUNTERS_TTL_MINUTES ) * 60000L;
        String strCacheKey = nProjectId + "/" + strVersionName;

        IssueCounts counts = _mapIssueCounts.get( strCacheKey );
        if ( counts != null && ( System.currentTimeMillis( ) - counts._lTime ) < lTtl )
        {
            return counts;
        }

        try
        {
            Version version = findVersionByName( nProjectId, strVersionName );
            if ( version != null )
            {
                counts = new IssueCounts( countIssues( nProjectId, version.getId( ), "closed" ), countIssues( nProjectId, version.getId( ), "open" ) );
            }
            else
            {
                counts = new IssueCounts( -1, -1 );
            }
            _mapIssueCounts.put( strCacheKey, counts );

            return counts;
        }
        catch( Exception ex )
        {
            AppLogService.error( "Releaser : unable to count issues of project " + nProjectId + " version " + strVersionName + " : " + ex.getMessage( ) );

            return null;
        }
    }

    /**
     * Counts the issues of a project version for a given status, using the total count of a 1-item page.
     *
     * @param nProjectId
     *            the numeric project id
     * @param nVersionId
     *            the version id
     * @param strStatus
     *            the status filter (open or closed)
     * @return the count, or -1 if unavailable
     * @throws RedmineException
     *             if the Redmine call fails
     */
    private int countIssues( int nProjectId, int nVersionId, String strStatus ) throws RedmineException
    {
        Params params = new Params( ).add( "project_id", String.valueOf( nProjectId ) ).add( "status_id", strStatus )
                .add( "fixed_version_id", String.valueOf( nVersionId ) ).add( "limit", "1" );
        Integer nTotal = _issueManager.getIssues( params ).getTotalFoundOnServer( );

        return ( nTotal != null ) ? nTotal : -1;
    }

    /**
     * Finds a project version by its name.
     *
     * @param nProjectId
     *            the project id
     * @param strName
     *            the version name
     * @return the matching version, or {@code null} if none
     * @throws RedmineException
     *             if the Redmine call fails
     */
    private Version findVersionByName( int nProjectId, String strName ) throws RedmineException
    {
        if ( StringUtils.isEmpty( strName ) )
        {
            return null;
        }

        for ( Version version : _projectManager.getVersions( nProjectId ) )
        {
            if ( strName.equals( version.getName( ) ) )
            {
                return version;
            }
        }

        return null;
    }

    /**
     * Fetches all the open issues targeting a given version (paginated).
     *
     * @param nProjectId
     *            the project id
     * @param nVersionId
     *            the target version id
     * @return the list of open issues for that version
     * @throws RedmineException
     *             if the Redmine call fails
     */
    private List<Issue> fetchOpenIssuesForVersion( int nProjectId, int nVersionId ) throws RedmineException
    {
        List<Issue> listIssues = new ArrayList<>( );
        int nOffset = 0;

        while ( true )
        {
            Params params = new Params( ).add( "project_id", String.valueOf( nProjectId ) ).add( "status_id", "open" )
                    .add( "fixed_version_id", String.valueOf( nVersionId ) ).add( "limit", String.valueOf( LIMIT ) )
                    .add( "offset", String.valueOf( nOffset ) );

            List<Issue> listPage = _issueManager.getIssues( params ).getResults( );

            if ( listPage.isEmpty( ) )
            {
                break;
            }

            listIssues.addAll( listPage );
            nOffset += LIMIT;
        }

        return listIssues;
    }

}
