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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser.Credential;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.file.FileUtils;
import fr.paris.lutece.plugins.releaser.util.github.GitUtils;
import fr.paris.lutece.plugins.releaser.util.github.GithubSearchRepoItem;
import fr.paris.lutece.plugins.releaser.util.github.GithubSearchResult;
import fr.paris.lutece.plugins.releaser.util.pom.PomParser;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import fr.paris.lutece.plugins.releaser.util.version.VersionParsingException;
import fr.paris.lutece.plugins.releaser.util.version.VersionUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

/**
 * ComponentService
 */
public class ComponentService implements IComponentService
{
    private ExecutorService _executor;
    private ObjectMapper _mapper;

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
        MavenRepoComponentInfoProvider.getInstance( ).setComponentRemoteInformations( component );        
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

    public Component updateComponentForReleaseBranchFrom ( Component component, String strPom )
    {
    	if (strPom != null)
    	{
	    	PomParser parser = new PomParser( );
	        parser.parse( component, strPom );
    	}

        try
        {
			int nMajor = Version.parse( component.getCurrentVersion() ).getMajor();

			String strLastRelease = VersionUtils.getLastVersionUsingMajor( component.getReleaseVersions(), nMajor );
			if ( strLastRelease != null )
			{
				component.setLastAvailableVersion( strLastRelease );
			}

			String strLastSnapshot = VersionUtils.getLastVersionUsingMajor( component.getSnapshotVersions(), nMajor );
			if ( strLastSnapshot != null )
			{
				component.setLastAvailableSnapshotVersion( strLastSnapshot );
			}
		}
        catch (VersionParsingException e)
        {
			AppLogService.error( "Error parsing version, excluded from sorted list : " + component.getCurrentVersion() );
		}

        return component;
    }
   
    public LocalizedPaginator<Component> getSearchComponent( String strSearch, HttpServletRequest request, Locale locale, String strPaginateUrl,
            String strCurrentPageIndex )
    {

        int nItemsPerPageLoad = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_NB_SEARCH_ITEM_PER_PAGE_LOAD, 10 );
        ReleaserUser user = ReleaserUtils.getReleaserUser( request, locale );
        String strUserLogin = user.getCredential( RepositoryType.GITHUB ).getLogin( );
        String strUserPassword = user.getCredential( RepositoryType.GITHUB ).getPassword( );
        List<Component> listResultAll = getListComponent(
                GitUtils.searchRepo( strSearch, ConstanteUtils.CONSTANTE_GITHUB_ORG_LUTECE_PLATFORM, strUserLogin, strUserPassword ), strUserLogin,
                strUserPassword );
        listResultAll.addAll(
                getListComponent( GitUtils.searchRepo( strSearch, ConstanteUtils.CONSTANTE_GITHUB_ORG_LUTECE_SECTEUR_PUBLIC, strUserLogin, strUserPassword ),
                        strUserLogin, strUserPassword ) );

        List<Component> listResult = new ArrayList<Component>();        
        for ( Component component : listResultAll )
        {
            // Load only information on the current page
            loadComponent( component, false,
                    GitUtils.getFileContent( component.getFullName( ), "pom.xml", component.getBranchReleaseFrom( ), strUserLogin, strUserPassword ),
                    strUserLogin, strUserPassword );
            
            if (component.getArtifactId() != null && !component.getArtifactId().isEmpty())
            {
            	listResult.add(component);
            }
        }
        
        LocalizedPaginator<Component> paginator = new LocalizedPaginator<Component>( listResult, nItemsPerPageLoad, strPaginateUrl,
                LocalizedPaginator.PARAMETER_PAGE_INDEX, strCurrentPageIndex, locale );

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
                component.setBranchReleaseFrom(GitUtils.DEFAULT_RELEASE_BRANCH);
                listComponent.add( component );
            }

        }
        
        return listComponent;
    }

    public Component loadComponent( Component component, boolean bUpdateComponent, String strPom, String strUser, String strPassword )
    {
        PomParser parser = new PomParser( );
        parser.parse( component, strPom );

        try
        {
        	if ( !bUpdateComponent )
        	{
        		ComponentService.getService( ).setRemoteInformations( component, false );
        	}
        	else
        	{
        		if ( component.getBranchReleaseFrom( ) != null )
                {
        			if ( component.getReleaseVersions() == null)
        			{
        				ComponentService.getService( ).setRemoteInformations( component, false );
        			}
        			else
        			{
        				ComponentService.getService( ).updateComponentForReleaseBranchFrom ( component, strPom );
        			}
                }
        	}        	

            component = getNextVersions( component, component.getLastAvailableVersion( ), component.getCurrentVersion( ), component.getCurrentVersion() );     	            
        }
        catch( HttpAccessException | IOException e )
        {
            AppLogService.error( e );
        }

        return component;
    }

    private Component getNextVersions( Component component, String strLastAvailableVersion, String strCurrentVersion, String strTargetVersion )
    {
    	if ( component.getLastAvailableSnapshotVersion () != null )
    	{
    		if ( component.getLastAvailableSnapshotVersion().equals( strCurrentVersion ) )
    		{
    			component.setTargetVersions( Version.getNextReleaseVersions( component.getLastAvailableSnapshotVersion(), strLastAvailableVersion ) );
    		}
    		else
    		{
    			component.setTargetVersions( Version.getNextReleaseVersions( strCurrentVersion ) );
    			component.addReleaseComment( "La version SNAPSHOT dans Nexus est différente de celle du repo des sources" );
    		}
    	}
    	else
    	{
    		component.setTargetVersions( Version.getNextReleaseVersions( strCurrentVersion ) );
    		component.addReleaseComment( "Aucune SNAPSHOT n'a été trouvée dans Nexus" );
    	}
    	
        component.setTargetVersion( Version.getReleaseVersion( strCurrentVersion ) );

        String strNextSnapshotVersion = null;
        try
        {
            Version version = Version.parse( strTargetVersion );
            boolean bSnapshot = true;
            strNextSnapshotVersion = version.nextPatch( bSnapshot ).toString( );
            component.setNextSnapshotVersion( strNextSnapshotVersion );
        }
        catch( VersionParsingException ex )
        {
            AppLogService.error( "Error parsing version for component " + component.getArtifactId( ) + " : " + ex.getMessage( ), ex );

        }        
        
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

    public static boolean IsSearchComponentAuthorized( AdminUser adminUser )
    {

        if ( RBACService.isAuthorized( new Component( ), ComponentResourceIdService.PERMISSION_SEARCH, adminUser ) )
        {
            return true;
        }

        return false;
    }

    @Override
    public Component getComponentBranchList( Component component, RepositoryType repositoryType, ReleaserUser user )
    {
    	
        Credential credential = user.getCredential( repositoryType );
        String strLogin = credential.getLogin( );
        String strPwd = credential.getPassword( );
        
    	CommandResult commandResult = new CommandResult( );
        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        commandResult.setLog( new StringBuffer( ) );
        context.setCommandResult( commandResult );
        context.setComponent( component );

        ReleaserUtils.logStartAction( context, " Clone Component '" + component.getName( ) + "'" );

        String strLocalComponentPath = ReleaserUtils.getLocalPath( context );

        String strRepoUrl = GitUtils.getRepoUrl( context.getReleaserResource( ).getScmUrl( ) );

        if ( StringUtils.isBlank( strRepoUrl ) && StringUtils.isNotBlank( component.getFullName( ) ) )
        {
            String strGithubBaseUrl = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITHUB_REPOSITORY_BASE_URL );
            strRepoUrl = strGithubBaseUrl + component.getFullName( ) + ".git";
            commandResult.getLog( ).append( "No SCM URL found, using GitHub fullName fallback : " + strRepoUrl + "\n" );
        }

        File fLocalRepo = new File( strLocalComponentPath );
        if ( fLocalRepo.exists( ) )
        {
            commandResult.getLog( ).append( "Local repository " + strLocalComponentPath + " exist\nCleaning Local folder...\n" );
            if ( !FileUtils.delete( fLocalRepo, commandResult.getLog( ) ) )
            {
                commandResult.setError( commandResult.getLog( ).toString( ) );

            }
            commandResult.getLog( ).append( "Local repository has been cleaned\n" );
        }

        List<String> branchNameList = GitUtils.getBranchList( strRepoUrl, fLocalRepo, commandResult, strLogin, strPwd );
        branchNameList.removeIf( b -> b.startsWith( GitUtils.MASTER_BRANCH ) );

        component.setBranches( branchNameList );
        context.setComponent( component );

        return component;
    }

    /**
     * {@inheritDoc}
     * Lists tags whose name matches "&lt;artifactId&gt;-X.Y.Z-(beta|RC)-NN" so the
     * "release from tag" dropdown only proposes pre-release tags eligible for stabilization.
     */
    @Override
    public Component getComponentTagList( Component component, RepositoryType repositoryType, ReleaserUser user )
    {
        Credential credential = user.getCredential( repositoryType );
        String strLogin = credential.getLogin( );
        String strPwd = credential.getPassword( );

        CommandResult commandResult = new CommandResult( );
        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        commandResult.setLog( new StringBuffer( ) );
        context.setCommandResult( commandResult );
        context.setComponent( component );

        ReleaserUtils.logStartAction( context, " List pre-release tags '" + component.getName( ) + "'" );

        String strLocalComponentPath = ReleaserUtils.getLocalPath( context );
        File fLocalRepo = new File( strLocalComponentPath );

        Git git = null;
        try
        {
            if ( fLocalRepo.exists( ) && new File( strLocalComponentPath + "/.git" ).exists( ) )
            {
                // Reuse the existing clone (likely just made by getComponentBranchList)
                git = GitUtils.getGit( strLocalComponentPath );
            }
            else
            {
                String strRepoUrl = GitUtils.getRepoUrl( context.getReleaserResource( ).getScmUrl( ) );
                git = Git.cloneRepository( )
                        .setCredentialsProvider( new org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider( strLogin, strPwd ) )
                        .setURI( strRepoUrl ).setDirectory( fLocalRepo ).setCloneAllBranches( true ).call( );
            }

            List<String> allTags = GitUtils.getTagNameList( git );
            List<String> preReleaseTags = new ArrayList<>( );
            if ( allTags != null )
            {
                String regex = "^" + java.util.regex.Pattern.quote( component.getArtifactId( ) ) + "-\\d+\\.\\d+\\.\\d+-(beta|RC)-\\d+$";
                java.util.regex.Pattern p = java.util.regex.Pattern.compile( regex );
                for ( String tag : allTags )
                {
                    if ( p.matcher( tag ).matches( ) )
                    {
                        preReleaseTags.add( tag );
                    }
                }
            }

            final String strArtifactId = component.getArtifactId( );
            preReleaseTags.sort( ( t1, t2 ) -> {
                try
                {
                    Version v1 = Version.parse( t1.substring( strArtifactId.length( ) + 1 ) );
                    Version v2 = Version.parse( t2.substring( strArtifactId.length( ) + 1 ) );
                    return v2.compareTo( v1 );
                }
                catch( VersionParsingException e )
                {
                    return 0;
                }
            } );

            component.setTags( preReleaseTags );
        }
        catch( Exception e )
        {
            AppLogService.error( "Error listing tags for component " + component.getArtifactId( ) + " : " + e.getMessage( ), e );
            component.setTags( new ArrayList<String>( ) );
        }
        finally
        {
            if ( git != null )
            {
                git.close( );
            }
        }

        return component;
    }

    public Component getLastBranchVersion( Component component, String branchName, ReleaserUser user )
    {
        String strPom = null;

        // Set default release branch
        if ( !component.getBranches( ).contains( component.getBranchReleaseFrom( ) ) )
        {
            component.getBranches( ).add( component.getBranchReleaseFrom( ) );
        }
        component.setBranchReleaseFrom( branchName );
        int indx = component.getBranches( ).indexOf( branchName );
        component.getBranches( ).remove( indx );

        CommandResult commandResult = new CommandResult( );
        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        commandResult.setLog( new StringBuffer( ) );
        context.setCommandResult( commandResult );
        context.setComponent( component );

        String strLocalComponentPath = ReleaserUtils.getLocalPath( context );
        File fLocalRepo = new File( strLocalComponentPath );

        ReleaserUtils.logStartAction( context, " Get versions of branch '" + branchName + "'" );

        Git git;
        try
        {
            git = Git.open( fLocalRepo );

            // Checkout branch
            GitUtils.createLocalBranch( git, branchName, commandResult );

            commandResult.getLog( ).append( "Checkout branch \"" + branchName + "\" ...\n" );
            GitUtils.checkoutRepoBranch( git, branchName, commandResult );

            // Fetch pom and Get last and next versions
            strPom = FileUtils.readFile( ReleaserUtils.getLocalPomPath( context ) );
            component = loadComponent( component, true, strPom, null, null );

        }
        catch( IOException e )
        {

            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }

        return component;
    }

}
