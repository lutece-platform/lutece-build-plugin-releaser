package fr.paris.lutece.plugins.releaser.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.jgit.api.ResetCommand.ResetType;
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
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;

public class WorkflowReleaseContextService implements IWorkflowReleaseContextService
{

    private static IWorkflowReleaseContextService _singleton;
    private HashMap<Integer, WorkflowReleaseContext> _mapWorkflowReleaseContext = new HashMap<Integer, WorkflowReleaseContext>( );
    private ExecutorService _executor;
    private  IMavenPrepareUpdateRemoteRepository  _svnMavenPrepareUpadteRepo ;
    private  IMavenPrepareUpdateRemoteRepository  _gitMavenPrepareUpadteRepo;
    private HashSet<String> _releaseInProgress = new HashSet<String>( );

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
            DatastoreService.setDataValue( ReleaserUtils.getWorklowContextDataKey( context.getComponent( ) != null ? context.getComponent( ).getArtifactId( )
                    : context.getSite( ).getArtifactId( ), context.getId( ) ), strJsonContext );

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
                    
                    //return only the reference item associated to the artifact id
                    if(referenceItem.getCode( ).startsWith( ConstanteUtils.CONSTANTE_RELEASE_CONTEXT_PREFIX + strArtifactId+"_"  ))
                    {
                        context = MapperJsonUtil.parse( referenceItem.getName( ), WorkflowReleaseContext.class );
                        if ( context != null )
                        {
                            listContext.add( context );
                        }
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
        String strComponentName = component.getName( );
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
            git = GitUtils.cloneRepo( strLocalComponentPath, component.getScmDeveloperConnection( ), commandResult, context.getReleaserUser( ).getGithubComponentAccountLogin( ), context.getReleaserUser( ).getGithubComponentAccountLogin( ), context.getReleaserUser( ).getGithubComponentAccountPassword( ) );
            // fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
            // git = new Git( fLocalRepo );
            GitUtils.createLocalBranch( git, GitUtils.DEVELOP_BRANCH, commandResult );
            GitUtils.createLocalBranch( git, GitUtils.MASTER_BRANCH, commandResult );
            context.setRefBranchDev( GitUtils.getRefBranch( git, GitUtils.DEVELOP_BRANCH, commandResult ) );
            context.setRefBranchRelease( GitUtils.getRefBranch( git, GitUtils.MASTER_BRANCH, commandResult ) );
            
            
            //String ref = git.getRepository( ).findRef( GitUtils.MASTER_BRANCH ).getName( ); 
//            git.reset( ).setRef( ref  ).setMode( ResetType.HARD ).call( );
//            git.push( )
//            .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getReleaserUser( ).getGithubComponentAccountLogin( ), context.getReleaserUser( ).getGithubComponentAccountPassword( ) ) ).setRe
//            .call( );
            commandResult.getLog( ).append( "the repository has been successfully cloned.\n" );
            commandResult.getLog( ).append( "Checkout branch \"" + GitUtils.DEVELOP_BRANCH + "\" ...\n" );
            GitUtils.checkoutRepoBranch( git, GitUtils.DEVELOP_BRANCH, commandResult );
            // PROGRESS 10%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );
            
        
            if(ComponentService.getService( ).isErrorSnapshotComponentInformations( component,ReleaserUtils.getLocalComponentPomPath( strComponentName ) ))
            {
                ReleaserUtils.addTechnicalError( commandResult,"The cloned component does not match the release informations");
                
            }

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
        String strComponentName = component.getName( );
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
                            .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getReleaserUser( ).getGithubComponentAccountLogin( ), context.getReleaserUser( ).getGithubComponentAccountPassword( ) ) )
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


    
    public void releasePrepareGit( WorkflowReleaseContext context, Locale locale )
    {
        String strComponentName = context.getComponent().getName( );
        
        try
        {
        realeasePrepare(strComponentName, context.getReleaserUser( ).getGithubComponentAccountLogin( ),context.getReleaserUser( ).getGithubComponentAccountPassword( ),_gitMavenPrepareUpadteRepo,
                context, locale );
        
        }catch(AppException ex)
        {
            
            _gitMavenPrepareUpadteRepo.rollbackRelease( ReleaserUtils.getLocalComponentPath( strComponentName) ,context.getComponent().getScmDeveloperConnection( ), context, locale );
            throw ex;
        }

    }

    public void releasePerformGit( WorkflowReleaseContext context, Locale locale )
    {

        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );

        ReleaserUtils.logStartAction( context, " Release Perform" );

        String strComponentName =context.getComponent( ).getName( );
        String strLocalComponentPomPath = ReleaserUtils.getLocalComponentPomPath( strComponentName );

        // PROGRESS 75%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 10 );

       
        try
        {
         
            MavenService.getService( ).mvnReleasePerform( strLocalComponentPomPath, context.getReleaserUser( ).getGithubComponentAccountLogin( ), context.getReleaserUser( ).getGithubComponentAccountPassword( ), commandResult );

        }catch(AppException ex)
        {
            
            _gitMavenPrepareUpadteRepo.rollbackRelease( ReleaserUtils.getLocalComponentPath( strComponentName) ,context.getComponent().getScmDeveloperConnection( ), context, locale );
            throw ex;
        }
        ReleaserUtils.logEndAction( context, " Release Perform" );
    }

    public void checkoutSite( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );

        ReleaserUtils.logStartAction( context, " checkout Site" );

        SvnService.getService( ).doSvnCheckoutSite( context.getSite( ), context.getReleaserUser( ).getSvnSiteAccountLogin( ), context.getReleaserUser( ).getSvnSiteAccountPassword( ), context.getCommandResult( ) );
        // PROGRESS 30%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 30 );

        ReleaserUtils.logEndAction( context, " checkout Site" );

    }

    public void checkoutComponent( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );

        ReleaserUtils.logStartAction( context, " Checkout Svn Component " );
        String strComponentName = context.getComponent( ).getName( );
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
        Long nLastCommitId=SvnService.getService( ).doSvnCheckoutComponent( context.getComponent( ), context.getReleaserUser( ).getSvnComponentAccountLogin( ), context.getReleaserUser( ).getSvnComponentAccountPassword( ),
                context.getCommandResult( ) );
        
        if(nLastCommitId!=null)
        {
            context.setRefBranchDev( nLastCommitId.toString( ) );
        }
        // PROGRESS 10%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );
        
        if(ComponentService.getService( ).isErrorSnapshotComponentInformations( context.getComponent( ) ,ReleaserUtils.getLocalComponentPomPath( strComponentName )))
        {
            ReleaserUtils.addTechnicalError( commandResult,"The checkout component does not match the release informations");
         }
        

        ReleaserUtils.logEndAction( context, "Checkout Svn Component " );

    }

    public void releaseSite( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );

        ReleaserUtils.logStartAction( context, " Release Site" );

        context.getCommandResult( ).getLog( ).append( "Starting Action Release Site...\n" );
        SvnService.getService( ).doReleaseSite( context.getSite( ), context.getReleaserUser( ).getSvnSiteAccountLogin( ), context.getReleaserUser( ).getSvnSiteAccountPassword( ), context.getCommandResult( ) );

        ReleaserUtils.logEndAction( context, " Release Site" );

    }

    public void releasePrepareSvn( WorkflowReleaseContext context, Locale locale )
    {
        String strComponentName = context.getComponent( ).getName();
        try
        {
            realeasePrepare(strComponentName,context.getReleaserUser( ).getSvnComponentAccountLogin( ),context.getReleaserUser( ).getSvnComponentAccountPassword( ), _svnMavenPrepareUpadteRepo,
                    context, locale );
        
        }catch(AppException ex)
        {
            _svnMavenPrepareUpadteRepo.rollbackRelease( ReleaserUtils.getLocalComponentPath( strComponentName) , context.getComponent().getScmDeveloperConnection( ),context, locale );
            throw ex;
        }
        
    

    }
    

  

    public void releasePerformSvn( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );
       
        ReleaserUtils.logStartAction( context, " Release Perform" );

        String strComponentName = context.getComponent( ).getName();
        String strLocalComponentPomPath = ReleaserUtils.getLocalComponentPomPath( strComponentName );


        // PROGRESS 75%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 10 );

      try
        {
            MavenService.getService( ).mvnReleasePerform( strLocalComponentPomPath, context.getReleaserUser( ).getSvnComponentAccountLogin( ), context.getReleaserUser( ).getSvnComponentAccountPassword( ), commandResult );

        
        }catch(AppException ex)
        {
            _svnMavenPrepareUpadteRepo.rollbackRelease( ReleaserUtils.getLocalComponentPath( strComponentName) , context.getComponent().getScmDeveloperConnection( ),context, locale );
            throw ex;
        }
        
        ReleaserUtils.logEndAction( context, " Release Perform" );
    }

    public void sendTweet( WorkflowReleaseContext context, Locale locale )
    {

        CommandResult commandResult = context.getCommandResult( );
       
        
        ReleaserUtils.logStartAction( context, " send Tweet" );

        TwitterService.getService( ).sendTweet( context.getComponent( ).getTweetMessage( ), commandResult );

        ReleaserUtils.logEndAction( context, " send Tweet" );

    }
    
    public void updateJiraVersions( WorkflowReleaseContext context, Locale locale )
    {

        CommandResult commandResult = context.getCommandResult( );
       
        ReleaserUtils.logStartAction( context, " Update Jira Versions" );

        JiraComponentService.getService( ).updateComponentVersions( context.getComponent( ), commandResult );

        ReleaserUtils.logEndAction( context, " Update Jira Versions" );

    }

    public void startWorkflowReleaseContext( WorkflowReleaseContext context, int nIdWorkflow, Locale locale, HttpServletRequest request, AdminUser user )
    {
        _executor.execute( new ReleaseComponentTask( nIdWorkflow, context, request, user, locale ) );
    }

    public void init( )
    {

        _executor = Executors.newFixedThreadPool( AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_THREAD_RELEASE_POOL_MAX_SIZE, 5 ) );
        _svnMavenPrepareUpadteRepo =SpringContextService.getBean( ConstanteUtils.BEAN_SVN_MAVEN_PREPARE_UPDATE_REMOTE_REPOSITORY );
         _gitMavenPrepareUpadteRepo =SpringContextService.getBean( ConstanteUtils.BEAN_GIT_MAVEN_PREPARE_UPDATE_REMOTE_REPOSITORY );
        

    }

    private void realeasePrepare( String strComponentName,String strUserLogin,String strUserPassword,IMavenPrepareUpdateRemoteRepository mavenPrepareUpdateRepo, WorkflowReleaseContext context, Locale locale )
    {

        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );
        String strLocalComponentPomPath = ReleaserUtils.getLocalComponentPomPath( strComponentName );

        String strComponentReleaseVersion = component.getTargetVersion( );
        String strComponentReleaseTagName = component.getArtifactId( ) + "-" + component.getTargetVersion( );
        String strComponentReleaseNewDeveloppmentVersion = component.getNextSnapshotVersion( );

        ReleaserUtils.logStartAction( context, " Release Prepare" );

        if ( PluginUtils.isCore( strComponentName ) )
        {
            // update core xml
            String strCoreXMLPath = PluginUtils.getCoreXMLFile( strLocalComponentPath );

            if ( StringUtils.isNotBlank( strCoreXMLPath ) )
            {
                commandResult.getLog( ).append( "Updating Core XML " + strComponentName + " to " + strComponentReleaseVersion + "\n" );

                
                PluginUtils.updatePluginXMLVersion( strLocalComponentPath, strComponentReleaseVersion, commandResult );
                // Commit Plugin xml modification version
                mavenPrepareUpdateRepo.updateDevelopBranch( strLocalComponentPath,context, locale, "[site-release] Update core version to " + strComponentReleaseVersion );
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
                mavenPrepareUpdateRepo.updateDevelopBranch( strAppInfoFilePath,context, locale, "[site-release] Update AppInfo.java version" );

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
                mavenPrepareUpdateRepo.updateDevelopBranch(pluginXMLPath, context, locale, "[site-release] Update plugin version to " + strComponentReleaseVersion + " for "
                        + strComponentName );

                commandResult.getLog( ).append( "Plugin XML updated to " + strComponentReleaseVersion + "\n" );
                // PROGRESS 30%
                commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );

            }
        }

       
       
        // Checkout Developp
        mavenPrepareUpdateRepo.checkoutDevelopBranchBeforePrepare(  context, locale );
       
        MavenService.getService( ).mvnReleasePrepare( strLocalComponentPomPath, strComponentReleaseVersion, strComponentReleaseTagName,
                strComponentReleaseNewDeveloppmentVersion, strUserLogin, strUserPassword, commandResult );
        // Merge Master
        mavenPrepareUpdateRepo.updateReleaseBranch( strLocalComponentPomPath,context, locale );
        // PROGRESS 50%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 20 );

        // Modify plugin version on develop
        if ( PluginUtils.isCore( strComponentName ) )
        {
            // update core xml
            String strCoreXMLPath = PluginUtils.getCoreXMLFile( strLocalComponentPath );

            if ( StringUtils.isNotBlank( strCoreXMLPath ) )
            {
                commandResult.getLog( ).append( "Updating Core XML " + strComponentName + " to " + strComponentReleaseNewDeveloppmentVersion + "\n" );

                PluginUtils.updatePluginXMLVersion( strLocalComponentPath, strComponentReleaseNewDeveloppmentVersion, commandResult );
                // Commit Plugin xml modification version
                mavenPrepareUpdateRepo.updateDevelopBranch(strLocalComponentPath,  context, locale, "[site-release] Update core version to "
                        + strComponentReleaseNewDeveloppmentVersion );
            }

            // update appinfo.java
            String strAppInfoFilePath = PluginUtils.getAppInfoFile( strLocalComponentPath, PluginUtils.CORE_PLUGIN_NAME1 );
            if ( StringUtils.isBlank( strAppInfoFilePath ) )
            {
                strAppInfoFilePath = PluginUtils.getAppInfoFile( strLocalComponentPath, PluginUtils.CORE_PLUGIN_NAME2 );
            }
            if ( StringUtils.isNotBlank( strAppInfoFilePath ) )
            {
                mavenPrepareUpdateRepo.updateDevelopBranch(strAppInfoFilePath, context, locale, "[site-release] Update AppInfo.java version" );

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
                mavenPrepareUpdateRepo.updateDevelopBranch( pluginXMLPath,context, locale, "[site-release] Update plugin version to "
                        + strComponentReleaseNewDeveloppmentVersion + " for " + strComponentName );

                commandResult.getLog( ).append( "Plugin XML updated to " + strComponentReleaseNewDeveloppmentVersion + "\n" );
            }
        }
        // PROGRESS 65%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 15 );
        
        ReleaserUtils.logEndAction( context, " Release Prepare" );

    }
    
    public void startReleaseInProgress(String strArtifactId)
    {
        
        _releaseInProgress.add( strArtifactId );
    }
    
    public void stopReleaseInProgress(String strArtifactId)
    {
        
        _releaseInProgress.remove( strArtifactId );
    }
    
    public boolean isReleaseInProgress(String strArtifactId)
    {
        
        return _releaseInProgress.contains( strArtifactId );
    }
    
    

}
