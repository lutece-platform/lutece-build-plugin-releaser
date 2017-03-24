package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.util.Locale;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.github.GitUtils;

public class GitMavenPrepareUpdateRemoteRepository implements IMavenPrepareUpdateRemoteRepository
{

  
    @Override
    public void updateDevelopBranch(String strLocalBasePath,WorkflowReleaseContext context, Locale locale, String strMessage )
    {
        
        
        FileRepository fLocalRepo = null;
        Git git = null;
        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );
     
       
        try
        {
     
            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
    
            git = new Git( fLocalRepo );
            git.checkout( ).setName( GitUtils.DEVELOP_BRANCH ).call( );
            git.add( ).addFilepattern( "." ).setUpdate( true ).call( );
            git.commit( ).setMessage( strMessage).call( );
            git.push( )
                    .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getReleaserUser( ).getGithubComponentAccountLogin( ), context.getReleaserUser( ).getGithubComponentAccountPassword( ) ) )
                    .call( );
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

    @Override
    public void updateReleaseBranch(String strLocalBasePath, WorkflowReleaseContext context, Locale locale)
    {
        FileRepository fLocalRepo = null;
        Git git = null;
        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
        String strComponentName = ReleaserUtils.getGitComponentName( component.getScmDeveloperConnection( ) );
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );
     
       
        try
        {
     
            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
    
            git = new Git( fLocalRepo );
            git.checkout( ).setName( GitUtils.DEVELOP_BRANCH ).call( );
            GitUtils.mergeBack( git, context.getReleaserUser( ).getGithubComponentAccountLogin( ), context.getReleaserUser( ).getGithubComponentAccountPassword( ), commandResult );
            
    
        
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
