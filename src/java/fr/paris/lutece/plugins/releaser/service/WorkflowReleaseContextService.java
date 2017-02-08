package fr.paris.lutece.plugins.releaser.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

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
import fr.paris.lutece.plugins.releaser.util.PluginUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.file.FileUtils;
import fr.paris.lutece.plugins.releaser.util.github.GitUtils;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class WorkflowReleaseContextService implements IWorkflowReleaseContextService
{

    private static IWorkflowReleaseContextService _singleton;
    private HashMap<Integer, WorkflowReleaseContext> _mapWorkflowReleaseContext = new HashMap<Integer, WorkflowReleaseContext>( );

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.plugins.releaser.service.IWorkflowReleaseContextService#addWorkflowDeploySiteContext(fr.paris.lutece.plugins.releaser.business.
     * WorkflowReleaseContext)
     */
    @Override
    public synchronized int addWorkflowDeploySiteContext( WorkflowReleaseContext context )
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
    public WorkflowReleaseContext getWorkflowDeploySiteContext( int nIdContext )
    {
        return _mapWorkflowReleaseContext.get( nIdContext );
    }

    public int getIdWorkflow( WorkflowReleaseContext context )
    {
        int nIdWorkflow = ConstanteUtils.CONSTANTE_ID_NULL;

        if ( ComponentService.isGitComponent( context.getComponent( ) ) )
        {
            nIdWorkflow = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_ID_WORKFLOW_GIT_COMPONENT, ConstanteUtils.CONSTANTE_ID_NULL );
        }
        else
        {
            nIdWorkflow = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_ID_WORKFLOW_SVN_COMPONENT, ConstanteUtils.CONSTANTE_ID_NULL );
        }

        return nIdWorkflow;
    }

    public static IWorkflowReleaseContextService getService( )
    {
        if ( _singleton == null )
        {

            _singleton = SpringContextService.getBean( ConstanteUtils.BEAN_WORKFLOW_RELEASE_CONTEXT_SERVICE);
        }

        return _singleton;
    }

    public  void gitCloneRepository( WorkflowReleaseContext context, Locale locale )
    {

        FileRepository fLocalRepo = null;
        CommandResult commandResult = context.getCommandResult( );
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

        try
        {
            commandResult.getLog( ).append( "Cloning repository ...\n" );
            GitUtils.cloneRepo( strLocalComponentPath, component.getScmDeveloperConnection( ) );
            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
            Git git = new Git( fLocalRepo );
            GitUtils.createLocalBranch( git, GitUtils.DEVELOP_BRANCH );
            GitUtils.createLocalBranch( git, GitUtils.MASTER_BRANCH);
            fLocalRepo.getConfig( ).setString( "user", null, "name", context.getGitHubUserLogin( ) );
            fLocalRepo.getConfig( ).setString( "user", null, "email", context.getGitHubUserLogin( ) + "@users.noreply.github.com" );
            fLocalRepo.getConfig( ).save( );
            commandResult.getLog( ).append( "the repository has been successfully cloned.\n" );
            commandResult.getLog( ).append( "Checkout branch \"" + GitUtils.DEVELOP_BRANCH + "\" ...\n" );
            GitUtils.checkoutRepoBranch( git,  GitUtils.DEVELOP_BRANCH);
            commandResult.getLog( ).append( "Checkout bracnh develop successfull\n" );

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
    }

    public  void gitMerge( WorkflowReleaseContext context, Locale locale )
    {

        FileRepository fLocalRepo = null;
        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );

        try
        {

            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
            Git git = new Git( fLocalRepo );
            commandResult.getLog( ).append( "Checking if local repository " + strComponentName + " exist\n" );
            if ( !fLocalRepo.getDirectory( ).exists( ) )
            {

                ReleaserUtils.addTechnicalError( commandResult, "the local repository does not exist" );

            }
            else
            {
                commandResult.getLog( ).append( "Checkout branch \"" + GitUtils.MASTER_BRANCH + "\" ...\n" );
                GitUtils.checkoutRepoBranch( git, GitUtils.MASTER_BRANCH );
                commandResult.getLog( ).append( "Checkout successfull\n" );
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

    }

    public void realeasePrepareGit( WorkflowReleaseContext context, Locale locale )
    {
        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );
        String strLocalComponentPomPath = ReleaserUtils.getLocalComponentPomPath( strComponentName );

        String strComponentReleaseVersion = component.getTargetVersion( );
        String strComponentReleaseTagName = component.getArtifactId( )+"-"+component.getTargetVersion( ) ;
        String strComponentReleaseNewDeveloppmentVersion = component.getNextSnapshotVersion( );

        // Switch to develop branch
        FileRepository fLocalRepo;
        try
        {
            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );

            Git git = new Git( fLocalRepo );
            git.checkout( ).setName( GitUtils.DEVELOP_BRANCH ).call( );

            if ( PluginUtils.isCore( strComponentName ) )
            {
                // update core xml
                String strCoreXMLPath = PluginUtils.getCoreXMLFile( strLocalComponentPath);
              
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
                }
            }

            MavenService.getService( ).mvnReleasePrepare( strLocalComponentPomPath, strComponentReleaseVersion, strComponentReleaseTagName,
                    strComponentReleaseNewDeveloppmentVersion, context.getGitHubUserLogin( ), context.getGitHubUserPassord( ), commandResult );
            // Merge Master
            GitUtils.mergeBack( git, context.getGitHubUserLogin( ), context.getGitHubUserPassord( ), commandResult );
            // Modify plugin version on develop
            git.checkout( ).setName( GitUtils.DEVELOP_BRANCH ).call( );
            if ( PluginUtils.isCore( strComponentName ) )
            {
                // update core xml
                String strCoreXMLPath = PluginUtils.getCoreXMLFile( strLocalComponentPath);
             
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
                    git.commit( ).setMessage( "[site-release] Update plugin version to " + strComponentReleaseNewDeveloppmentVersion + " for " + strComponentName )
                            .call( );
                    git.push( )
                            .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getGitHubUserLogin( ), context.getGitHubUserPassord( ) ) )
                            .call( );

                    commandResult.getLog( ).append( "Plugin XML updated to " + strComponentReleaseNewDeveloppmentVersion + "\n" );
                }
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

    }

    public  void realeasePerformGit( WorkflowReleaseContext context, Locale locale )
    {
       
        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
       
        
        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strLocalComponentPomPath = ReleaserUtils.getLocalComponentPomPath( strComponentName );

        MavenService.getService( ).mvnReleasePerform( strLocalComponentPomPath, context.getGitHubUserLogin( ), context.getGitHubUserPassord( ), commandResult );
    }

    public static void realeasePrepareSvn( WorkflowReleaseContext context, Locale locale )
    {

    }

    public static void realeasePerformSvn( WorkflowReleaseContext context, Locale locale )
    {

    }
    
    public  void sendTweet( WorkflowReleaseContext context, Locale locale )
    {
       
        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
       
        
        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strLocalComponentPomPath = ReleaserUtils.getLocalComponentPomPath( strComponentName );

     String strTweet="";
        
       TwitterService.getService( ).sendTweet( strTweet, commandResult );
        
    }
    

}
