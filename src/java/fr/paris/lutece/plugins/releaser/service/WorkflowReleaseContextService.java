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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.WorkflowContextHistory;
import fr.paris.lutece.plugins.releaser.business.WorkflowContextHistoryHome;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.CVSFactoryService;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.IVCSResourceService;
import fr.paris.lutece.plugins.releaser.util.MapperJsonUtil;
import fr.paris.lutece.plugins.releaser.util.PluginUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.github.GitUtils;
import fr.paris.lutece.plugins.releaser.util.pom.PomUpdater;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

// TODO: Auto-generated Javadoc
/**
 * The Class WorkflowReleaseContextService.
 */
public class WorkflowReleaseContextService implements IWorkflowReleaseContextService
{

    /** The singleton. */
    private static IWorkflowReleaseContextService _singleton;

    /** The map workflow release context. */
    private HashMap<Integer, WorkflowReleaseContext> _mapWorkflowReleaseContext = new HashMap<Integer, WorkflowReleaseContext>( );

    /** The executor. */
    private ExecutorService _executor;

    /** The release in progress. */
    private HashSet<String> _releaseInProgress = new HashSet<String>( );

    /**
     * Adds the workflow release context.
     *
     * @param context
     *            the context
     * @return the int
     */
    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.plugins.releaser.service.IWorkflowReleaseContextService#addWorkflowDeploySiteContext(fr.paris.lutece.plugins.releaser.business.
     * WorkflowReleaseContext)
     */
    @Override
    public synchronized int addWorkflowReleaseContext( WorkflowReleaseContext context )
    {

        try
        {
            String strJsonContext = MapperJsonUtil.getJson( context );
            // clean PWD in log before save in history
            String strJsonContextClean = ReleaserUtils.cleanPWDInLog( strJsonContext );

            WorkflowContextHistory wfHistory = new WorkflowContextHistory( );
            wfHistory.setArtifactId( context.getComponent( ) != null ? context.getComponent( ).getArtifactId( ) : context.getSite( ).getArtifactId( ) );
            wfHistory.setData( strJsonContextClean );
            WorkflowContextHistoryHome.create( wfHistory );
            context.setId( wfHistory.getId( ) );
            _mapWorkflowReleaseContext.put( context.getId( ), context );
        }
        catch( IOException e )
        {
            AppLogService.error( "error during add workkflow context context json", e );
        }

        return context.getId( );
    }

    /**
     * Gets the workflow release context.
     *
     * @param nIdContext
     *            the n id context
     * @return the workflow release context
     */
    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.plugins.releaser.service.IWorkflowReleaseContextService#getWorkflowDeploySiteContext(int)
     */
    @Override
    public WorkflowReleaseContext getWorkflowReleaseContext( int nIdContext )
    {
        return _mapWorkflowReleaseContext.get( nIdContext );
    }

    /**
     * Save workflow release context.
     *
     * @param context
     *            the context
     */
    public synchronized void saveWorkflowReleaseContext( WorkflowReleaseContext context )
    {
        try
        {
            String strJsonContext = MapperJsonUtil.getJson( context );
            // clean PWD in log before save in history
            String strJsonContextClean = ReleaserUtils.cleanPWDInLog( strJsonContext );

            WorkflowContextHistory wfHistory = WorkflowContextHistoryHome.findByPrimaryKey( context.getId( ) );

            if ( wfHistory != null )
            {
                if ( context.getCommandResult( ) != null && context.getCommandResult( ).getDateBegin( ) != null )
                {
                    wfHistory.setDateBegin( new Timestamp( context.getCommandResult( ).getDateBegin( ).getTime( ) ) );
                }
                if ( context.getCommandResult( ) != null && context.getCommandResult( ).getDateEnd( ) != null )
                {
                    wfHistory.setDateEnd( new Timestamp( context.getCommandResult( ).getDateEnd( ).getTime( ) ) );
                }
                wfHistory.setData( strJsonContextClean );
                WorkflowContextHistoryHome.update( wfHistory );
            }
            else
            {
                wfHistory = new WorkflowContextHistory( );

                wfHistory.setArtifactId( context.getComponent( ) != null ? context.getComponent( ).getArtifactId( ) : context.getSite( ).getArtifactId( ) );
                wfHistory.setData( strJsonContextClean );
                WorkflowContextHistoryHome.create( wfHistory );
                context.setId( wfHistory.getId( ) );
            }

        }
        catch( IOException e )
        {
            AppLogService.error( "error during save context json", e );
        }
    }

    /**
     * Gets the workflow release context history.
     *
     * @param nIdContext
     *            the n id context
     * @return the workflow release context history
     */
    public WorkflowReleaseContext getWorkflowReleaseContextHistory( int nIdContext )
    {
        WorkflowReleaseContext context = null;
        try
        {

            WorkflowContextHistory wfHistory = WorkflowContextHistoryHome.findByPrimaryKey( nIdContext );
            context = MapperJsonUtil.parse( wfHistory.getData( ), WorkflowReleaseContext.class );

        }
        catch( IOException e )
        {
            AppLogService.error( "error during get context in the datastore", e );
        }
        return context;

    }

    /**
     * Gets the list workflow release context history.
     *
     * @param strArtifactId
     *            the str artifact id
     * @return the list workflow release context history
     */
    public List<WorkflowReleaseContext> getListWorkflowReleaseContextHistory( String strArtifactId )
    {
        WorkflowReleaseContext context = null;
        List<WorkflowReleaseContext> listContext = new ArrayList<WorkflowReleaseContext>( );

        try
        {

            List<WorkflowContextHistory> listWfContextHistory = WorkflowContextHistoryHome.getWorkflowDeployContextsListByArtifactId( strArtifactId );
            if ( !CollectionUtils.isEmpty( listWfContextHistory ) )
            {

                for ( WorkflowContextHistory wfHistory : listWfContextHistory )
                {
                    context = MapperJsonUtil.parse( wfHistory.getData( ), WorkflowReleaseContext.class );
                    if ( context != null )
                    {
                        listContext.add( context );
                    }
                }

            }
        }
        catch( IOException e )
        {
            AppLogService.error( "error for parsing json workflow context", e );
        }
        return listContext;

    }

    /**
     * Gets the id workflow.
     *
     * @param context
     *            the context
     * @return the id workflow
     */
    public int getIdWorkflow( WorkflowReleaseContext context )
    {
        int nIdWorkflow = ConstanteUtils.CONSTANTE_ID_NULL;

        if ( context.isLuteceSite( ) )
        {
            nIdWorkflow = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_ID_WORKFLOW_LUTECE_SITE, ConstanteUtils.CONSTANTE_ID_NULL );

        }
        else
        {

            nIdWorkflow = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_ID_WORKFLOW_COMPONENT, ConstanteUtils.CONSTANTE_ID_NULL );
        }

        return nIdWorkflow;
    }

    /**
     * Gets the service.
     *
     * @return the service
     */
    public static IWorkflowReleaseContextService getService( )
    {
        if ( _singleton == null )
        {

            _singleton = SpringContextService.getBean( ConstanteUtils.BEAN_WORKFLOW_RELEASE_CONTEXT_SERVICE );
            _singleton.init( );
        }

        return _singleton;
    }

    /**
     * Merge develop master.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    public void mergeDevelopMaster( WorkflowReleaseContext context, Locale locale )
    {

        if ( !context.getReleaserResource( ).getRepoType( ).equals( RepositoryType.SVN )
                && ReleaserUtils.getBranchReleaseFrom( context ).equals( GitUtils.DEFAULT_RELEASE_BRANCH ) )
        {

            FileRepository fLocalRepo = null;
            CommandResult commandResult = context.getCommandResult( );
            String strLogin = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getLogin( );
            String strPassword = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getPassword( );

            ReleaserUtils.logStartAction( context, " Merge DEVELOP/MASTER" );

            String strLocalComponentPath = ReleaserUtils.getLocalPath( context );
            Git git = null;
            try
            {

                fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
                git = new Git( fLocalRepo );
                commandResult.getLog( ).append( "Checking if local repository " + strLocalComponentPath + " exist\n" );
                if ( !fLocalRepo.getDirectory( ).exists( ) )
                {

                    ReleaserUtils.addTechnicalError( commandResult, "the local repository does not exist" );

                }
                else
                {
                    commandResult.getLog( ).append( "Checkout branch \"" + GitUtils.MASTER_BRANCH + "\" ...\n" );
                    GitUtils.checkoutRepoBranch( git, GitUtils.MASTER_BRANCH, commandResult );
                    commandResult.getLog( ).append( "Checkout successfull\n" );
                    // PROGRESS 15%
                    commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );

                    commandResult.getLog( ).append( "Going to merge '" + GitUtils.DEFAULT_RELEASE_BRANCH + "' branch on 'master' branch...\n" );
                    MergeResult mergeResult = GitUtils.mergeRepoBranch( git, GitUtils.DEFAULT_RELEASE_BRANCH );
                    if ( mergeResult.getMergeStatus( ).equals( MergeResult.MergeStatus.CHECKOUT_CONFLICT )
                            || mergeResult.getMergeStatus( ).equals( MergeResult.MergeStatus.CONFLICTING )
                            || mergeResult.getMergeStatus( ).equals( MergeResult.MergeStatus.FAILED )
                            || mergeResult.getMergeStatus( ).equals( MergeResult.MergeStatus.NOT_SUPPORTED ) )
                    {
                        ReleaserUtils.addTechnicalError( commandResult, "An error appear during merge operation, the status of merge result is: "
                                + mergeResult.getMergeStatus( ).toString( ) + "\nPlease merge manually before releasing." );

                    }
                    else
                    {
                        git.push( ).setCredentialsProvider( new UsernamePasswordCredentialsProvider( strLogin, strPassword ) ).call( );
                        commandResult.getLog( ).append( mergeResult.getMergeStatus( ) );
                    }
                    ReleaserUtils.logEndAction( context, " Merge DEVELOP/MASTER" );
                    // BACK to Branch DEVELOP after merge
                    GitUtils.checkoutRepoBranch( git, GitUtils.DEFAULT_RELEASE_BRANCH, commandResult );
                    // PROGRESS 25%
                    commandResult.setProgressValue( commandResult.getProgressValue( ) + 10 );

                }

            }
            catch( InvalidRemoteException e )
            {

                ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

            }
            catch( TransportException e )
            {
                ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

            }
            catch( IOException e )
            {
                ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
            }
            catch( GitAPIException e )
            {
                ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
            }
            finally
            {

                if ( fLocalRepo != null )
                {

                    fLocalRepo.close( );

                }
                if ( git != null )
                {

                    git.close( );

                }

            }
        }
    }

    /**
     * Release prepare component.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    public void releasePrepareComponent( WorkflowReleaseContext context, Locale locale )
    {
        String strComponentName = context.getComponent( ).getName( );
        String strLogin = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getLogin( );
        String strPassword = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getPassword( );
        IVCSResourceService cvsService = CVSFactoryService.getService( context.getComponent( ).getRepoType( ) );

        try
        {
            CommandResult commandResult = context.getCommandResult( );
            Component component = context.getComponent( );

            // Checkout branch release from before prepare
            cvsService.checkoutBranch( context, component.getBranchReleaseFrom( ), locale );

            String strLocalComponentPath = ReleaserUtils.getLocalPath( context );
            String strLocalComponentPomPath = ReleaserUtils.getLocalPomPath( context );

            String strComponentReleaseVersion = component.getTargetVersion( );

            String strComponentReleaseTagName = null;
            if ( component.getBranchReleaseFrom( ).equals( GitUtils.DEFAULT_RELEASE_BRANCH ) )
            {
                strComponentReleaseTagName = component.getArtifactId( ) + "-" + component.getTargetVersion( );
            }
            else
            {
                strComponentReleaseTagName = component.getArtifactId( ) + "-" + component.getTargetVersion( ) + "-" + component.getBranchReleaseFrom( );
            }

            String strComponentReleaseNewDeveloppmentVersion = component.getNextSnapshotVersion( );

            ReleaserUtils.logStartAction( context, " Release Prepare" );

            if ( PluginUtils.isCore( strComponentName ) )
            {
                // update core xml
                String strCoreXMLPath = PluginUtils.getCoreXMLFile( strLocalComponentPath );

                if ( StringUtils.isNotBlank( strCoreXMLPath ) )
                {
                    commandResult.getLog( ).append( "Updating Core XML " + strComponentName + " to " + strComponentReleaseVersion + "\n" );

                    PluginUtils.updatePluginXMLVersion( strCoreXMLPath, strComponentReleaseVersion, commandResult );
                    // Commit Plugin xml modification version
                    cvsService.updateBranch( context, component.getBranchReleaseFrom( ), locale,
                            "[site-release] Update core version to " + strComponentReleaseVersion );
                    commandResult.getLog( ).append( "Core XML updated to " + strComponentReleaseVersion + "\n" );
                    // PROGRESS 30%
                    commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );

                }

                // update appinfo.java
                String strAppInfoFilePath = PluginUtils.getAppInfoFile( strLocalComponentPath );

                if ( StringUtils.isNotBlank( strAppInfoFilePath ) )
                {
                    PluginUtils.updateAppInfoFile( strAppInfoFilePath, strComponentReleaseVersion, commandResult );
                    cvsService.updateBranch( context, component.getBranchReleaseFrom( ), locale, "[site-release] Update AppInfo.java version" );

                }
                else
                {
                    commandResult.getLog( ).append( "No AppInfo file found..." );
                }

            }
            else
            {
                String [ ] pluginNames = PluginUtils.getPluginXMLFile( strLocalComponentPath );
                for ( String pluginXMLPath : pluginNames )
                {
                    commandResult.getLog( ).append( "Updating plugin XML " + strComponentName + " to " + strComponentReleaseVersion + "\n" );
                    PluginUtils.updatePluginXMLVersion( pluginXMLPath, strComponentReleaseVersion, commandResult );
                    cvsService.updateBranch( context, component.getBranchReleaseFrom( ), locale,
                            "[site-release] Update plugin version to " + strComponentReleaseVersion + " for " + strComponentName );

                    commandResult.getLog( ).append( "Plugin XML updated to " + strComponentReleaseVersion + "\n" );
                    // PROGRESS 30%
                    commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );

                }
            }

            MavenService.getService( ).mvnReleasePrepare( strLocalComponentPomPath, strComponentReleaseVersion, strComponentReleaseTagName,
                    strComponentReleaseNewDeveloppmentVersion, strLogin, strPassword, commandResult );

            // Merge Master if release from develop branch
            if ( component.getBranchReleaseFrom( ).equals( GitUtils.DEFAULT_RELEASE_BRANCH ) )
            {
                cvsService.updateMasterBranch( context, locale );
            }

            // Checkout branch after prepare
            cvsService.checkoutBranch( context, component.getBranchReleaseFrom( ), locale );

            // PROGRESS 50%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 20 );

            // Modify plugin version on released branch
            if ( PluginUtils.isCore( strComponentName ) )
            {
                // update core xml
                String strCoreXMLPath = PluginUtils.getCoreXMLFile( strLocalComponentPath );

                if ( StringUtils.isNotBlank( strCoreXMLPath ) )
                {
                    commandResult.getLog( ).append( "Updating Core XML " + strComponentName + " to " + strComponentReleaseNewDeveloppmentVersion + "\n" );

                    PluginUtils.updatePluginXMLVersion( strCoreXMLPath, strComponentReleaseNewDeveloppmentVersion, commandResult );

                    // Commit Plugin xml modification version
                    cvsService.updateBranch( context, component.getBranchReleaseFrom( ), locale,
                            "[site-release] Update core version to " + strComponentReleaseNewDeveloppmentVersion );
                }

                // update appinfo.java
                String strAppInfoFilePath = PluginUtils.getAppInfoFile( strLocalComponentPath );

                if ( StringUtils.isNotBlank( strAppInfoFilePath ) )
                {
                    PluginUtils.updateAppInfoFile( strAppInfoFilePath, strComponentReleaseNewDeveloppmentVersion, commandResult );
                    cvsService.updateBranch( context, component.getBranchReleaseFrom( ), locale, "[site-release] Update AppInfo.java version" );
                }
                else
                {
                    commandResult.getLog( ).append( "No AppInfo file found..." );
                }

            }
            else
            {
                String [ ] pluginNames = PluginUtils.getPluginXMLFile( strLocalComponentPath );
                for ( String pluginXMLPath : pluginNames )
                {
                    commandResult.getLog( ).append( "Updating plugin XML " + strComponentName + " to " + strComponentReleaseNewDeveloppmentVersion + "\n" );
                    PluginUtils.updatePluginXMLVersion( pluginXMLPath, strComponentReleaseNewDeveloppmentVersion, commandResult );
                    // Commit Plugin xml modification version
                    cvsService.updateBranch( context, component.getBranchReleaseFrom( ), locale,
                            "[site-release] Update plugin version to " + strComponentReleaseNewDeveloppmentVersion + " for " + strComponentName );

                    commandResult.getLog( ).append( "Plugin XML updated to " + strComponentReleaseNewDeveloppmentVersion + "\n" );
                }
            }
            // PROGRESS 65%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 15 );

            ReleaserUtils.logEndAction( context, " Release Prepare" );

        }
        catch( AppException ex )
        {

            cvsService.rollbackRelease( context, locale );
            throw ex;
        }

    }

    /**
     * Release perform component.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    public void releasePerformComponent( WorkflowReleaseContext context, Locale locale )
    {

        CommandResult commandResult = context.getCommandResult( );
        String strLogin = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getLogin( );
        String strPassword = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getPassword( );
        IVCSResourceService cvsService = CVSFactoryService.getService( context.getComponent( ).getRepoType( ) );

        ReleaserUtils.logStartAction( context, " Release Perform" );

        String strLocalComponentPath = ReleaserUtils.getLocalPath( context );

        // PROGRESS 75%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 10 );

        try
        {

            MavenService.getService( ).mvnReleasePerform( strLocalComponentPath, strLogin, strPassword, commandResult );

        }
        catch( AppException ex )
        {
            cvsService.rollbackRelease( context, locale );
            throw ex;
        }
        ReleaserUtils.logEndAction( context, " Release Perform" );
    }

    /**
     * Checkout repository.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    public void checkoutRepository( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );
        String strLogin = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getLogin( );
        String strPassword = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getPassword( );

        ReleaserUtils.logStartAction( context, " checkout resource" );
        CVSFactoryService.getService( context.getReleaserResource( ).getRepoType( ) ).doCheckoutRepository( context, strLogin, strPassword );
        // PROGRESS 30%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 30 );

        ReleaserUtils.logEndAction( context, " checkout resource" );

    }

    /**
     * Release prepare site.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    public void releasePrepareSite( WorkflowReleaseContext context, Locale locale )
    {

        IVCSResourceService cvsService = CVSFactoryService.getService( context.getSite( ).getRepoType( ) );
        String strLogin = context.getReleaserUser( ).getCredential( context.getSite( ).getRepoType( ) ).getLogin( );
        String strPassword = context.getReleaserUser( ).getCredential( context.getSite( ).getRepoType( ) ).getPassword( );
        String strSitePomLocalBasePath = ReleaserUtils.getLocalPomPath( context );

        CommandResult commandResult = context.getCommandResult( );
        String componentTyeName = context.getSite( ).isTheme( ) ? "Theme " : "Site";

        ReleaserUtils.logStartAction( context, " Release perpare " + componentTyeName );

        commandResult.getLog( ).append( "Starting Action Release " + componentTyeName + "...\n" );

        commandResult.getLog( ).append( "Preparing release " + componentTyeName + "\n" );
        commandResult.getLog( ).append( "Updating pom version to " + context.getSite( ).getNextReleaseVersion( ) + "...\n" );
        commandResult.getLog( ).append( "Updating dependency version ...\n" );
        try
        {
            PomUpdater.updateSiteBeforeTag( context.getSite( ), ReleaserUtils.getLocalPomPath( context ) );
            cvsService.updateDevelopBranch( context, locale, "[site-release] update pom before tag" );

            // PROGRESS 30%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 30 );

            commandResult.getLog( ).append( "Pom updated\n" );
            commandResult.getLog( ).append( "Release prepare " + componentTyeName + " " + context.getSite( ).getNextReleaseVersion( ) + "...\n" );

            MavenService.getService( ).mvnReleasePrepare( strSitePomLocalBasePath, context.getSite( ).getNextReleaseVersion( ),
                    context.getSite( ).getArtifactId( ) + "-" + context.getSite( ).getNextReleaseVersion( ), context.getSite( ).getNextSnapshotVersion( ),
                    strLogin, strPassword, commandResult );

            commandResult.getLog( ).append( "End Release prepare " + componentTyeName + " " + context.getSite( ).getNextReleaseVersion( ) + "...\n" );

            // Merge Master
            cvsService.updateMasterBranch( context, locale );
            // checkout/pull develop branch after prepare
            cvsService.checkoutDevelopBranch( context, locale );

            // PROGRESS 20%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 20 );

            commandResult.getLog( ).append( "Updating pom after release prepare \n" );

            PomUpdater.updateSiteAfterTag( context.getSite( ), ReleaserUtils.getLocalPomPath( context ) );
            cvsService.updateDevelopBranch( context, locale, "[site-release] update Updating pom to next development" );
            commandResult.getLog( ).append( "Pom updated\n" );

        }

        catch( JAXBException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, "error during update pom", e );
        }
        catch( AppException ex )
        {

            cvsService.rollbackRelease( context, locale );
            throw ex;
        }

        ReleaserUtils.logEndAction( context, " Release prepare " + componentTyeName );

    }

    /**
     * Send tweet.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    public void sendTweet( WorkflowReleaseContext context, Locale locale )
    {

        CommandResult commandResult = context.getCommandResult( );

        ReleaserUtils.logStartAction( context, " send Tweet" );

        TwitterService.getService( ).sendTweet( context.getComponent( ).getTweetMessage( ), commandResult );

        ReleaserUtils.logEndAction( context, " send Tweet" );

    }

    /**
     * Update jira versions.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    public void updateJiraVersions( WorkflowReleaseContext context, Locale locale )
    {

        CommandResult commandResult = context.getCommandResult( );

        ReleaserUtils.logStartAction( context, " Update Jira Versions" );

        JiraComponentService.getService( ).updateComponentVersions( context.getComponent( ), commandResult );

        ReleaserUtils.logEndAction( context, " Update Jira Versions" );

    }

    /**
     * Start workflow release context.
     *
     * @param context
     *            the context
     * @param nIdWorkflow
     *            the n id workflow
     * @param locale
     *            the locale
     * @param request
     *            the request
     * @param user
     *            the user
     */
    public void startWorkflowReleaseContext( WorkflowReleaseContext context, int nIdWorkflow, Locale locale, HttpServletRequest request, AdminUser user )
    {
        _executor.execute( new ReleaseComponentTask( nIdWorkflow, context, request, user, locale ) );
    }

    /**
     * Inits the.
     */
    public void init( )
    {

        _executor = Executors.newFixedThreadPool( AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_THREAD_RELEASE_POOL_MAX_SIZE, 5 ) );

    }

    /**
     * Start release in progress.
     *
     * @param strArtifactId
     *            the str artifact id
     */
    public void startReleaseInProgress( String strArtifactId )
    {

        _releaseInProgress.add( strArtifactId );
    }

    /**
     * Stop release in progress.
     *
     * @param strArtifactId
     *            the str artifact id
     */
    public void stopReleaseInProgress( String strArtifactId )
    {

        _releaseInProgress.remove( strArtifactId );
    }

    /**
     * Checks if is release in progress.
     *
     * @param strArtifactId
     *            the str artifact id
     * @return true, if is release in progress
     */
    public boolean isReleaseInProgress( String strArtifactId )
    {

        return _releaseInProgress.contains( strArtifactId );
    }

    /**
     * Release perform site.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    @Override
    public void releasePerformSite( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );
        String strLogin = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getLogin( );
        String strPassword = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getPassword( );
        IVCSResourceService cvsService = CVSFactoryService.getService( context.getReleaserResource( ).getRepoType( ) );

        if ( context.getSite( ).isTheme( ) )
        {
            ReleaserUtils.logStartAction( context, " Release Perform" );

            String strLocalComponentPath = ReleaserUtils.getLocalPath( context );

            // PROGRESS 75%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 10 );

            try
            {

                MavenService.getService( ).mvnReleasePerform( strLocalComponentPath, strLogin, strPassword, commandResult );

            }
            catch( AppException ex )
            {
                cvsService.rollbackRelease( context, locale );
                throw ex;
            }
            ReleaserUtils.logEndAction( context, " Release Perform" );
        }
        else
        {
            commandResult.getLog( ).append( "No release Perform for Site" );
        }
    }

}
