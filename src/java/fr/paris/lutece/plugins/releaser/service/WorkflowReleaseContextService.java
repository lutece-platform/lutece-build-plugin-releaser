package fr.paris.lutece.plugins.releaser.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.MapperJsonUtil;
import fr.paris.lutece.plugins.releaser.util.PluginUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.file.FileUtils;
import fr.paris.lutece.plugins.releaser.util.github.GitUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;

public class WorkflowReleaseContextService implements IWorkflowReleaseContextService
{

    private static IWorkflowReleaseContextService _singleton;
    private HashMap<Integer, WorkflowReleaseContext> _mapWorkflowReleaseContext = new HashMap<Integer, WorkflowReleaseContext>( );
    private ExecutorService _executor;

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.plugins.releaser.service.IWorkflowReleaseContextService#addWorkflowDeploySiteContext(fr.paris.lutece.plugins.releaser.business.
     * WorkflowReleaseContext)
     */
    @Override
    public synchronized int addWorkflowReleaseContext( WorkflowReleaseContext context )
    {
        int nIdKey = Integer.parseInt( DatastoreService.getDataValue( ConstanteUtils.CONSTANTE_MAX_RELEASE_CONTEXT_KEY, "0" ) ) + 1;
        // stored key in database
        DatastoreService.setDataValue( ConstanteUtils.CONSTANTE_MAX_RELEASE_CONTEXT_KEY, Integer.toString( nIdKey ) );
        context.setId( nIdKey );
        _mapWorkflowReleaseContext.put( nIdKey, context );

        return nIdKey;
    }

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

    public synchronized void saveWorkflowReleaseContext( WorkflowReleaseContext context )
    {
        try
        {
            String strJsonContext = MapperJsonUtil.getJson( context );
            DatastoreService
                    .setDataValue( ReleaserUtils.getWorklowContextDataKey( context.getComponent( )!=null?context.getComponent( ).getArtifactId( ):context.getSite( ).getArtifactId( ), context.getId( ) ), strJsonContext );

        }
        catch( IOException e )
        {
            AppLogService.error( "error during save context json", e );
        }
    }

    public WorkflowReleaseContext getWorkflowReleaseContextHistory( int nIdContext, String strArtifactId )
    {
        WorkflowReleaseContext context = null;
        try
        {

            String strJsonContext = DatastoreService.getDataValue( ReleaserUtils.getWorklowContextDataKey( strArtifactId, nIdContext ), null );
            context = MapperJsonUtil.parse( strJsonContext, WorkflowReleaseContext.class );

        }
        catch( IOException e )
        {
            AppLogService.error( "error during get context in the datastore", e );
        }
        return context;

    }

    public List<WorkflowReleaseContext> getListWorkflowReleaseContextHistory( String strArtifactId )
    {
        WorkflowReleaseContext context = null;
        List<WorkflowReleaseContext> listContext = new ArrayList<WorkflowReleaseContext>( );

        try
        {

            ReferenceList refListContextHistory = DatastoreService.getDataByPrefix( ConstanteUtils.CONSTANTE_RELEASE_CONTEXT_PREFIX + strArtifactId );
            if ( !CollectionUtils.isEmpty( refListContextHistory ) )
            {
                for ( Iterator iterator = refListContextHistory.iterator( ); iterator.hasNext( ); )
                {
                    ReferenceItem referenceItem = (ReferenceItem) iterator.next( );
                    context = MapperJsonUtil.parse( referenceItem.getName( ), WorkflowReleaseContext.class );
                    if ( context != null )
                    {
                        listContext.add( context );
                    }
                }
            }
        }
        catch( IOException e )
        {
            AppLogService.error( "error during get context in the datastore", e );
        }
        return listContext;

    }

    public int getIdWorkflow( WorkflowReleaseContext context )
    {
        int nIdWorkflow = ConstanteUtils.CONSTANTE_ID_NULL;

        if ( context.isLuteceSite( ) )
        {
            nIdWorkflow = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_ID_WORKFLOW_LUTECE_SITE, ConstanteUtils.CONSTANTE_ID_NULL );

        }
        else
        {

            if ( ComponentService.getService( ).isGitComponent( context.getComponent( ) ) )
            {
                nIdWorkflow = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_ID_WORKFLOW_GIT_COMPONENT, ConstanteUtils.CONSTANTE_ID_NULL );
            }
            else
            {
                nIdWorkflow = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_ID_WORKFLOW_SVN_COMPONENT, ConstanteUtils.CONSTANTE_ID_NULL );
            }
        }

        return nIdWorkflow;
    }

    public static IWorkflowReleaseContextService getService( )
    {
        if ( _singleton == null )
        {

            _singleton = SpringContextService.getBean( ConstanteUtils.BEAN_WORKFLOW_RELEASE_CONTEXT_SERVICE );
            _singleton.init( );
        }

        return _singleton;
    }

    public void gitCloneRepository( WorkflowReleaseContext context, Locale locale )
    {
        Git git = null;
        // FileRepository fLocalRepo = null;
        CommandResult commandResult = context.getCommandResult( );
        ReleaserUtils.logStartAction( context, " Clone Repository" );
        Component component = context.getComponent( );
        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );

        File file = new File( strLocalComponentPath );

        if ( file.exists( ) )
        {

            commandResult.getLog( ).append( "Local repository " + strComponentName + " exist\nCleaning Local folder...\n" );
            if ( !FileUtils.delete( file, commandResult.getLog( ) ) )
            {
                commandResult.setError( commandResult.getLog( ).toString( ) );

            }
            commandResult.getLog( ).append( "Local repository has been cleaned\n" );
        }

        commandResult.getLog( ).append( "Cloning repository ...\n" );
        try
        {

            // PROGRESS 5%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );
            git = GitUtils.cloneRepo( strLocalComponentPath, component.getScmDeveloperConnection( ), commandResult, context.getGitHubUserLogin( ) );
            // fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
            // git = new Git( fLocalRepo );
            GitUtils.createLocalBranch( git, GitUtils.DEVELOP_BRANCH, commandResult );
            GitUtils.createLocalBranch( git, GitUtils.MASTER_BRANCH, commandResult );
            commandResult.getLog( ).append( "the repository has been successfully cloned.\n" );
            commandResult.getLog( ).append( "Checkout branch \"" + GitUtils.DEVELOP_BRANCH + "\" ...\n" );
            GitUtils.checkoutRepoBranch( git, GitUtils.DEVELOP_BRANCH, commandResult );
            // PROGRESS 10%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );

        }
        finally
        {
            if ( git != null )
            {

                git.close( );

            }
        }

        commandResult.getLog( ).append( "Checkout branch develop successfull\n" );

        ReleaserUtils.logEndAction( context, " Clone Repository" );

    }

    public void gitMerge( WorkflowReleaseContext context, Locale locale )
    {

        FileRepository fLocalRepo = null;
        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
        ReleaserUtils.logStartAction( context, " Merge DEVELOP/MASTER" );
        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );
        Git git = null;
        try
        {

            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
            git = new Git( fLocalRepo );
            commandResult.getLog( ).append( "Checking if local repository " + strComponentName + " exist\n" );
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

                commandResult.getLog( ).append( "Going to merge '" + GitUtils.DEVELOP_BRANCH + "' branch on 'master' branch...\n" );
                MergeResult mergeResult = GitUtils.mergeRepoBranch( git, GitUtils.DEVELOP_BRANCH );
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
                    git.push( )
                            .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getGitHubUserLogin( ), context.getGitHubUserPassord( ) ) )
                            .call( );
                    commandResult.getLog( ).append( mergeResult.getMergeStatus( ) );
                }
                ReleaserUtils.logEndAction( context, " Merge DEVELOP/MASTER" );
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

    public void realeasePrepareGit( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );
        String strLocalComponentPomPath = ReleaserUtils.getLocalComponentPomPath( strComponentName );

        String strComponentReleaseVersion = component.getTargetVersion( );
        String strComponentReleaseTagName = component.getArtifactId( ) + "-" + component.getTargetVersion( );
        String strComponentReleaseNewDeveloppmentVersion = component.getNextSnapshotVersion( );

        // Switch to develop branch
        FileRepository fLocalRepo = null;
        Git git = null;
        ReleaserUtils.logStartAction( context, " Release Prepare" );

        try
        {
            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );

            git = new Git( fLocalRepo );
            git.checkout( ).setName( GitUtils.DEVELOP_BRANCH ).call( );

            if ( PluginUtils.isCore( strComponentName ) )
            {
                // update core xml
                String strCoreXMLPath = PluginUtils.getCoreXMLFile( strLocalComponentPath );

                if ( StringUtils.isNotBlank( strCoreXMLPath ) )
                {
                    commandResult.getLog( ).append( "Updating Core XML " + strComponentName + " to " + strComponentReleaseVersion + "\n" );

                    PluginUtils.updatePluginXMLVersion( strLocalComponentPath, strComponentReleaseVersion, commandResult );
                    // Commit Plugin xml modification version
                    git.add( ).addFilepattern( "." ).setUpdate( true ).call( );
                    git.commit( ).setMessage( "[site-release] Update core version to " + strComponentReleaseVersion ).call( );
                    git.push( )
                            .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getGitHubUserLogin( ), context.getGitHubUserPassord( ) ) )
                            .call( );
                    commandResult.getLog( ).append( "Core XML updated to " + strComponentReleaseVersion + "\n" );
                    // PROGRESS 30%
                    commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );

                }

                // update appinfo.java
                String strAppInfoFilePath = PluginUtils.getAppInfoFile( strLocalComponentPath, PluginUtils.CORE_PLUGIN_NAME1 );
                if ( StringUtils.isBlank( strAppInfoFilePath ) )
                {
                    strAppInfoFilePath = PluginUtils.getAppInfoFile( strLocalComponentPath, PluginUtils.CORE_PLUGIN_NAME2 );
                }
                if ( StringUtils.isNotBlank( strAppInfoFilePath ) )
                {
                    PluginUtils.updateAppInfoFile( strAppInfoFilePath, strComponentReleaseVersion, commandResult );
                    git.add( ).addFilepattern( "." ).setUpdate( true ).call( );
                    git.commit( ).setMessage( "[site-release] Update AppInfo.java version" ).call( );
                    git.push( )
                            .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getGitHubUserLogin( ), context.getGitHubUserPassord( ) ) )
                            .call( );
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
                    // Commit Plugin xml modification version
                    git.add( ).addFilepattern( "." ).setUpdate( true ).call( );
                    git.commit( ).setMessage( "[site-release] Update plugin version to " + strComponentReleaseVersion + " for " + strComponentName ).call( );
                    git.push( )
                            .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getGitHubUserLogin( ), context.getGitHubUserPassord( ) ) )
                            .call( );

                    commandResult.getLog( ).append( "Plugin XML updated to " + strComponentReleaseVersion + "\n" );
                    // PROGRESS 30%
                    commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );

                }
            }

            MavenService.getService( ).mvnReleasePrepare( strLocalComponentPomPath, strComponentReleaseVersion, strComponentReleaseTagName,
                    strComponentReleaseNewDeveloppmentVersion, context.getGitHubUserLogin( ), context.getGitHubUserPassord( ), commandResult );
            // Merge Master
            GitUtils.mergeBack( git, context.getGitHubUserLogin( ), context.getGitHubUserPassord( ), commandResult );
            // PROGRESS 50%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 20 );

            // Modify plugin version on develop
            git.checkout( ).setName( GitUtils.DEVELOP_BRANCH ).call( );
            if ( PluginUtils.isCore( strComponentName ) )
            {
                // update core xml
                String strCoreXMLPath = PluginUtils.getCoreXMLFile( strLocalComponentPath );

                if ( StringUtils.isNotBlank( strCoreXMLPath ) )
                {
                    commandResult.getLog( ).append( "Updating Core XML " + strComponentName + " to " + strComponentReleaseNewDeveloppmentVersion + "\n" );

                    PluginUtils.updatePluginXMLVersion( strLocalComponentPath, strComponentReleaseNewDeveloppmentVersion, commandResult );
                    // Commit Plugin xml modification version
                    git.add( ).addFilepattern( "." ).setUpdate( true ).call( );
                    git.commit( ).setMessage( "[site-release] Update core version to " + strComponentReleaseNewDeveloppmentVersion ).call( );
                    git.push( )
                            .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getGitHubUserLogin( ), context.getGitHubUserPassord( ) ) )
                            .call( );
                    commandResult.getLog( ).append( "Core XML updated to " + strComponentReleaseNewDeveloppmentVersion + "\n" );
                }

                // update appinfo.java
                String strAppInfoFilePath = PluginUtils.getAppInfoFile( strLocalComponentPath, PluginUtils.CORE_PLUGIN_NAME1 );
                if ( StringUtils.isBlank( strAppInfoFilePath ) )
                {
                    strAppInfoFilePath = PluginUtils.getAppInfoFile( strLocalComponentPath, PluginUtils.CORE_PLUGIN_NAME2 );
                }
                if ( StringUtils.isNotBlank( strAppInfoFilePath ) )
                {
                    PluginUtils.updateAppInfoFile( strAppInfoFilePath, strComponentReleaseNewDeveloppmentVersion, commandResult );
                    git.add( ).addFilepattern( "." ).setUpdate( true ).call( );
                    git.commit( ).setMessage( "[site-release] Update AppInfo.java version" ).call( );
                    git.push( )
                            .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getGitHubUserLogin( ), context.getGitHubUserPassord( ) ) )
                            .call( );
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
                    git.add( ).addFilepattern( "." ).setUpdate( true ).call( );
                    git.commit( )
                            .setMessage( "[site-release] Update plugin version to " + strComponentReleaseNewDeveloppmentVersion + " for " + strComponentName )
                            .call( );
                    git.push( )
                            .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getGitHubUserLogin( ), context.getGitHubUserPassord( ) ) )
                            .call( );

                    commandResult.getLog( ).append( "Plugin XML updated to " + strComponentReleaseNewDeveloppmentVersion + "\n" );
                }
            }
            // PROGRESS 65%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 15 );

            ReleaserUtils.logEndAction( context, " Release Prepare" );

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

    public void realeasePerformGit( WorkflowReleaseContext context, Locale locale )
    {

        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );

        ReleaserUtils.logStartAction( context, " Release Perform" );

        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strLocalComponentPomPath = ReleaserUtils.getLocalComponentPomPath( strComponentName );

        // PROGRESS 75%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 10 );

        MavenService.getService( ).mvnReleasePerform( strLocalComponentPomPath, context.getGitHubUserLogin( ), context.getGitHubUserPassord( ), commandResult );

        ReleaserUtils.logEndAction( context, " Release Perform" );
    }

    public void checkoutSite( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );

        ReleaserUtils.logStartAction( context, " checkout Site" );

        SvnService.getService( ).doSvnCheckoutSite( context.getSite( ), context.getSvnUserLogin( ), context.getSvnUserPassword( ), context.getCommandResult( ) );
        // PROGRESS 30%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 30 );

        ReleaserUtils.logEndAction( context, " checkout Site" );

    }
    
    
    public void checkoutComponent( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );
       
        ReleaserUtils.logStartAction( context, " Checkout Svn Component " );
        String strComponentName =  context.getComponent( ).getArtifactId( );
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );

        File file = new File( strLocalComponentPath );

        if ( file.exists( ) )
        {

            commandResult.getLog( ).append( "Local SVN Component " + strComponentName + " exist\nCleaning Local folder...\n" );
            if ( !FileUtils.delete( file, commandResult.getLog( ) ) )
            {
                commandResult.setError( commandResult.getLog( ).toString( ) );

            }
            commandResult.getLog( ).append( "Local SVN Component has been cleaned\n" );
        }
        // PROGRESS 5%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );
     

        commandResult.getLog( ).append( "Checkout SVN Component ...\n" );
        SvnService.getService( ).doSvnCheckoutComponent( context.getComponent( ), context.getSvnUserLogin( ), context.getSvnUserPassword( ), context.getCommandResult( ) );
        // PROGRESS 10%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );

        ReleaserUtils.logEndAction( context, "Checkout Svn Component " );

    }

    public void tagSite( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );

        ReleaserUtils.logStartAction( context, " tag Site" );

       
        context.getCommandResult( ).getLog( ).append( "Starting Action Tag  Site...\n" );
        SvnService.getService( ).doSvnTagSite( context.getSite( ), context.getSvnUserLogin( ), context.getSvnUserPassword( ), context.getCommandResult( ) );

        ReleaserUtils.logEndAction( context, " tag SIte" );

    }

    public static void realeasePrepareSvn( WorkflowReleaseContext context, Locale locale )
    {

    }

    public static void realeasePerformSvn( WorkflowReleaseContext context, Locale locale )
    {

    }

    public void sendTweet( WorkflowReleaseContext context, Locale locale )
    {

        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );

        ReleaserUtils.logStartAction( context, " send Tweet" );

        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strComponentReleaseVersion = component.getTargetVersion( );

        Object [ ] messageAgrs = {
                strComponentName, strComponentReleaseVersion
        };

        String strTweetMessage = I18nService.getLocalizedString( ConstanteUtils.I18_TWITTER_MESSAGE, messageAgrs, locale );

        TwitterService.getService( ).sendTweet( strTweetMessage, commandResult );

        ReleaserUtils.logEndAction( context, " send Tweet" );

    }

    public void startWorkflowReleaseContext( WorkflowReleaseContext context, int nIdWorkflow, Locale locale, HttpServletRequest request, AdminUser user )
    {
        _executor.execute( new ReleaseComponentTask( nIdWorkflow, context, request, user, locale ) );
    }

    public void init( )
    {

        _executor = Executors.newFixedThreadPool( AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_THREAD_RELEASE_POOL_MAX_SIZE, 10 ) );

    }

}
