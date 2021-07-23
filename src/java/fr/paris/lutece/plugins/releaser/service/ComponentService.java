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

package fr.paris.lutece.plugins.releaser.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.github.GitUtils;
import fr.paris.lutece.plugins.releaser.util.github.GithubSearchRepoItem;
import fr.paris.lutece.plugins.releaser.util.github.GithubSearchResult;
import fr.paris.lutece.plugins.releaser.util.pom.PomParser;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import fr.paris.lutece.plugins.releaser.util.version.VersionParsingException;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

/**
 * ComponentService
 */
public class ComponentService implements IComponentService
{
    private ExecutorService _executor;
    private ObjectMapper _mapper;
    private static final String PROPERTY_COMPONENT_WEBSERVICE = "releaser.component.webservice.url";
    private static final String URL_COMPONENT_WEBSERVICE = AppPropertiesService.getProperty( PROPERTY_COMPONENT_WEBSERVICE );
    private static final String FIELD_COMPONENT = "component";
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_SNAPSHOT_VERSION = "snapshotVersion";
    private static final String FIELD_ATTRIBUTES = "attributes";

    private static final String FIELD_JIRA_CODE = "jiraKey";
    private static final String FIELD_ROADMAP_URL = "jiraRoadmapUrl";
    private static final String FIELD_CLOSED_ISSUES = "jiraFixedIssuesCount";
    private static final String FIELD_OPENED_ISSUES = "jiraUnresolvedIssuesCount";
    private static final String FIELD_SCM_DEVELOPER_CONNECTION = "scmDeveloperConnection";
    private static final String RELEASE_NOT_FOUND = "Release not found";

    private static IComponentService _instance;

    /**
     * @return get service
     */
    public static IComponentService getService( )
    {
        if ( _instance == null )
        {
            _instance = SpringContextService.getBean( ConstanteUtils.BEAN_COMPONENT_SERVICE );
            _instance.init( );
        }

        return _instance;

    }

    public void setRemoteInformations( Component component, boolean bCache ) throws HttpAccessException, IOException
    {
        try
        {
            HttpAccess httpAccess = new HttpAccess( );
            String strInfosJSON;
            String strUrl = MessageFormat.format( URL_COMPONENT_WEBSERVICE, component.getArtifactId( ), bCache, component.getType( ) );
            if ( component.getType( ) == null )
            {
                strUrl = strUrl.replace( "&type=null", "" );
            }
            strInfosJSON = httpAccess.doGet( strUrl );
            JsonNode nodeRoot = _mapper.readTree( strInfosJSON );
            if ( nodeRoot != null )
            {
                JsonNode nodeComponent = nodeRoot.path( FIELD_COMPONENT );
                if ( nodeComponent != null )
                {
                    String strVersion = nodeComponent.get( FIELD_VERSION ).asText( );
                    if ( !RELEASE_NOT_FOUND.equals( strVersion ) )
                    {
                        component.setLastAvailableVersion( nodeComponent.get( FIELD_VERSION ).asText( ) );
                    }

                    JsonNode jnSnapshoteVersion = nodeComponent.get( FIELD_ATTRIBUTES ).get( FIELD_SNAPSHOT_VERSION );
                    JsonNode jnJiraCode = nodeComponent.get( FIELD_ATTRIBUTES ).get( FIELD_JIRA_CODE );
                    JsonNode jnJiraRoadMap = nodeComponent.get( FIELD_ATTRIBUTES ).get( FIELD_ROADMAP_URL );
                    JsonNode jnJiraCurrentVersionOpenedIssues = nodeComponent.get( FIELD_ATTRIBUTES ).get( FIELD_OPENED_ISSUES );
                    JsonNode jnJiraCurrentVersionClosedIssues = nodeComponent.get( FIELD_ATTRIBUTES ).get( FIELD_CLOSED_ISSUES );
                    JsonNode jnScmDeveloperConnection = nodeComponent.get( FIELD_ATTRIBUTES ).get( FIELD_SCM_DEVELOPER_CONNECTION );

                    component.setLastAvailableSnapshotVersion( jnSnapshoteVersion != null ? jnSnapshoteVersion.asText( ) : null );
                    component.setJiraCode( jnJiraCode != null ? jnJiraCode.asText( ) : null );
                    component.setJiraRoadmapUrl( jnJiraRoadMap != null ? jnJiraRoadMap.asText( ) : null );
                    component.setJiraCurrentVersionOpenedIssues( jnJiraCurrentVersionOpenedIssues != null ? jnJiraCurrentVersionOpenedIssues.asInt( ) : 0 );
                    component.setJiraCurrentVersionClosedIssues( jnJiraCurrentVersionClosedIssues != null ? jnJiraCurrentVersionClosedIssues.asInt( ) : 0 );

                    if ( jnScmDeveloperConnection != null && !StringUtils.isEmpty( jnScmDeveloperConnection.asText( ) )
                            && !jnScmDeveloperConnection.asText( ).equals( "null" ) )
                    {
                        component.setScmDeveloperConnection( jnScmDeveloperConnection.asText( ) );
                    }
                }
            }
        }
        catch( HttpAccessException | IOException ex )
        {
            AppLogService.error( "Error getting Remote informations : " + ex.getMessage( ), ex );
        }

    }

    /**
     * Returns the LastAvailableVersion
     * 
     * @return The LastAvailableVersion
     */
    public String getLastReleaseVersion( String strArtifactId )
    {

        return DatastoreService.getDataValue( ReleaserUtils.getLastReleaseVersionDataKey( strArtifactId ), null );

    }

    /**
     * set the LastAvailableVersion
     * 
     * set The LastAvailableVersion
     */
    public void setLastReleaseVersion( String strArtifactId, String strVersion )
    {

        DatastoreService.setDataValue( ReleaserUtils.getLastReleaseVersionDataKey( strArtifactId ), strVersion );

    }

    /**
     * Returns the LastAvailableVersion
     * 
     * @return The LastAvailableVersion
     */
    public String getLastReleaseNextSnapshotVersion( String strArtifactId )
    {

        return DatastoreService.getDataValue( ReleaserUtils.getLastReleaseNextSnapshotVersionDataKey( strArtifactId ), null );

    }

    /**
     * set the LastAvailableVersion
     * 
     * set The LastAvailableVersion
     */
    public void setLastReleaseNextSnapshotVersion( String strArtifactId, String strVersion )
    {

        DatastoreService.setDataValue( ReleaserUtils.getLastReleaseNextSnapshotVersionDataKey( strArtifactId ), strVersion );
    }

    @Override
    public int release( Component component, Locale locale, AdminUser user, HttpServletRequest request, boolean forceRelease )
    {

        // Test if version in progression before release
        if ( WorkflowReleaseContextService.getService( ).isReleaseInProgress( component.getArtifactId( ) )
                || ( !forceRelease && ( !component.isProject( ) || !component.shouldBeReleased( ) ) ) )
        {
            return -1;
        }

        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        context.setComponent( component );
        context.setReleaserUser( ReleaserUtils.getReleaserUser( request, locale ) );

        int nIdWorkflow = WorkflowReleaseContextService.getService( ).getIdWorkflow( context );
        WorkflowReleaseContextService.getService( ).addWorkflowReleaseContext( context );

        // Compare Latest vesion of component before rekease
        WorkflowReleaseContextService.getService( ).startWorkflowReleaseContext( context, nIdWorkflow, locale, request, user );

        return context.getId( );
    }

    public int release( Component component, Locale locale, AdminUser user, HttpServletRequest request )
    {
        return release( component, locale, user, request, false );
    }

    public boolean isGitComponent( Component component )
    {
        return !StringUtils.isEmpty( component.getScmDeveloperConnection( ) )
                && component.getScmDeveloperConnection( ).trim( ).startsWith( ConstanteUtils.CONSTANTE_SUFFIX_GIT );
    }

    public void init( )
    {

        _mapper = new ObjectMapper( );
        _executor = Executors.newFixedThreadPool( AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_THREAD_RELEASE_POOL_MAX_SIZE, 10 ) );

    }

    @Override
    public void updateRemoteInformations( Component component )
    {
        String strLastReleaseVersion = ComponentService.getService( ).getLastReleaseVersion( component.getArtifactId( ) );
        String strLastReleaseNextSnapshotVersion = ComponentService.getService( ).getLastReleaseNextSnapshotVersion( component.getArtifactId( ) );
        if ( component.getLastAvailableVersion( ) == null )
        {
            component.setLastAvailableVersion( strLastReleaseVersion );
        }

        if ( component.getLastAvailableSnapshotVersion( ) == null )
        {
            component.setLastAvailableSnapshotVersion( strLastReleaseNextSnapshotVersion );
        }

        if ( component.getLastAvailableVersion( ) != null && strLastReleaseVersion != null )
        {
            try
            {
                Version vLastReleaseVersion = Version.parse( strLastReleaseVersion );
                Version vLastAvailableVersion = Version.parse( component.getLastAvailableVersion( ) );
                if ( vLastReleaseVersion.compareTo( vLastAvailableVersion ) > 0 )
                {
                    component.setLastAvailableVersion( strLastReleaseVersion );
                }

            }
            catch( VersionParsingException e )
            {
                AppLogService.error( e );
            }
        }
        if ( component.getLastAvailableSnapshotVersion( ) != null && strLastReleaseNextSnapshotVersion != null )
        {
            try
            {
                Version vLastReleaseNextSnapshotVersionVersion = Version.parse( strLastReleaseNextSnapshotVersion );
                Version vLastAvailableSnapshotVersion = Version.parse( component.getLastAvailableSnapshotVersion( ) );
                if ( vLastReleaseNextSnapshotVersionVersion.compareTo( vLastAvailableSnapshotVersion ) > 0 )
                {
                    component.setLastAvailableSnapshotVersion( strLastReleaseNextSnapshotVersion );
                }

            }
            catch( VersionParsingException e )
            {
                AppLogService.error( e );
            }
        }
    }

    public LocalizedPaginator<Component> getSearchComponent( String strSearch, HttpServletRequest request, Locale locale, String strPaginateUrl,
            String strCurrentPageIndex )
    {

        int nItemsPerPageLoad = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_NB_SEARCH_ITEM_PER_PAGE_LOAD, 10 );
        ReleaserUser user = ReleaserUtils.getReleaserUser( request, locale );
        String strUserLogin = user.getCredential( RepositoryType.GITHUB ).getLogin( );
        String strUserPassword = user.getCredential( RepositoryType.GITHUB ).getPassword( );
        List<Component> listResult = getListComponent(
                GitUtils.searchRepo( strSearch, ConstanteUtils.CONSTANTE_GITHUB_ORG_LUTECE_PLATFORM, strUserLogin, strUserPassword ), strUserLogin,
                strUserPassword );
        listResult.addAll(
                getListComponent( GitUtils.searchRepo( strSearch, ConstanteUtils.CONSTANTE_GITHUB_ORG_LUTECE_SECTEUR_PUBLIC, strUserLogin, strUserPassword ),
                        strUserLogin, strUserPassword ) );

        LocalizedPaginator<Component> paginator = new LocalizedPaginator<Component>( listResult, nItemsPerPageLoad, strPaginateUrl,
                LocalizedPaginator.PARAMETER_PAGE_INDEX, strCurrentPageIndex, locale );

        for ( Component component : paginator.getPageItems( ) )
        {
            // Load only information on the current page
            loadComponent( component, GitUtils.getFileContent( component.getFullName( ), "pom.xml", GitUtils.DEVELOP_BRANCH, strUserLogin, strUserPassword ),
                    strUserLogin, strUserPassword );

        }

        return paginator;
    }

    private List<Component> getListComponent( GithubSearchResult searchResult, String strUser, String strPassword )
    {

        List<Component> listComponent = new ArrayList<>( );
        Component component = null;

        if ( searchResult != null && searchResult.getListRepoItem( ) != null )
        {
            for ( GithubSearchRepoItem item : searchResult.getListRepoItem( ) )
            {

                component = new Component( );
                component.setFullName( item.getFullName( ) );
                component.setName( item.getName( ) );
                listComponent.add( component );
            }

        }
        return listComponent;

    }

    public Component loadComponent( Component component, String strPom, String stUser, String strPassword )
    {

        PomParser parser = new PomParser( );
        parser.parse( component, strPom );

        try
        {
            ComponentService.getService( ).setRemoteInformations( component, false );

        }
        catch( HttpAccessException | IOException e )
        {
            AppLogService.error( e );
        }
        ComponentService.getService( ).updateRemoteInformations( component );
        component.setTargetVersions( Version.getNextReleaseVersions( component.getLastAvailableVersion() ) );
        component.setTargetVersion( Version.getReleaseVersion( component.getCurrentVersion( ) ) );

        String strNextSnapshotVersion = null;
        try
        {
            Version version = Version.parse( component.getTargetVersion( ) );
            boolean bSnapshot = true;
            strNextSnapshotVersion = version.nextPatch( bSnapshot ).toString( );
        }
        catch( VersionParsingException ex )
        {
            AppLogService.error( "Error parsing version for component " + component.getArtifactId( ) + " : " + ex.getMessage( ), ex );

        }
        component.setNextSnapshotVersion( strNextSnapshotVersion );
        component.setLastAvailableSnapshotVersion( component.getCurrentVersion( ) );

        return component;

    }

    public boolean isErrorSnapshotComponentInformations( Component component, String strComponentPomPath )
    {

        boolean bError = true;

        PomParser parser = new PomParser( );
        Component componentPom = new Component( );

        FileInputStream inputStream;
        try
        {
            inputStream = new FileInputStream( strComponentPomPath );
            parser.parse( componentPom, inputStream );

            if ( component != null && componentPom != null && component.getArtifactId( ).equals( componentPom.getArtifactId( ) )
                    && component.getLastAvailableSnapshotVersion( ).equals( componentPom.getCurrentVersion( ) ) )
            {

                bError = false;

            }

        }
        catch( FileNotFoundException e )
        {
            AppLogService.error( e );

        }

        return bError;
    }

    /**
     * Change the next release version
     * 
     * @param component
     *            The component
     */
    public void changeNextReleaseVersion( Component component )
    {

        List<String> listTargetVersions = component.getTargetVersions( );
        if ( !CollectionUtils.isEmpty( listTargetVersions ) )
        {
            int nNewIndex = ( component.getTargetVersionIndex( ) + 1 ) % listTargetVersions.size( );
            String strTargetVersion = listTargetVersions.get( nNewIndex );
            component.setTargetVersion( strTargetVersion );
            component.setTargetVersionIndex( nNewIndex );
            component.setNextSnapshotVersion( Version.getNextSnapshotVersion( strTargetVersion ) );
        }
    }
    
    public static boolean IsSearchComponentAuthorized (AdminUser adminUser)
    {
    	
        if ( RBACService.isAuthorized( new Component(), ComponentResourceIdService.PERMISSION_SEARCH, adminUser ) )
        {
        	return true;
        }	        
    	
    	return false;
    }

}
