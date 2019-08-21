package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser.CREDENTIAL_TYPE;
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
        String strComponentName = component.getName( ) ;
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );
     
       
        try
        {
     
            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
    
            git = new Git( fLocalRepo );
            git.checkout( ).setName( GitUtils.DEVELOP_BRANCH ).call( );
            git.add( ).addFilepattern( "." ).setUpdate( true ).call( );
            git.commit( ).setCommitter(context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getLogin(), context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getLogin()).setMessage( strMessage).call( );
            git.push( )
                    .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getLogin(), context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getPassword() ) )
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
        String strComponentName = component.getName( ) ;
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );
     
       
        try
        {
     
            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
    
            git = new Git( fLocalRepo );
            git.checkout( ).setName( GitUtils.DEVELOP_BRANCH ).call( );
            GitUtils.mergeBack( git, context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getLogin(), context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getPassword(), commandResult );
            
    
        
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
    public void rollbackRelease(String strLocalBasePath,String strScmUrl, WorkflowReleaseContext context, Locale locale)
    {
        
        ReleaserUtils.logStartAction( context, " Rollback Release prepare" );
        FileRepository fLocalRepo = null;
        Git git = null;
        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
        String strComponentName = component.getName( ) ;
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );
        
        try
        {
     
            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
    
            git = new Git( fLocalRepo );
            
            
            
            //RESET commit on develop
            if(!StringUtils.isEmpty( context.getRefBranchDev( )))
            {
                git.checkout().setName( GitUtils.DEVELOP_BRANCH ).call();
                git.reset().setRef( context.getRefBranchDev( ) ).setMode( ResetType.HARD ).call( );
                git.push( ).setForce( true )
                .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getLogin(), context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getPassword() ) )
                .call( );
                
           }
            //Reset Commit on Master
            if(!StringUtils.isEmpty( context.getRefBranchRelease( )))
            {

                git.checkout().setName( GitUtils.MASTER_BRANCH).call();
                git.reset().setRef( context.getRefBranchRelease( ) ).setMode( ResetType.HARD ).call( );
                git.push( ).setForce( true )
                .setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getLogin(), context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getPassword() ) )
                .call( );       
             }
            //Delete Tag if exist
            List<Ref> call = git.tagList().call();
            String strTagName=component.getArtifactId( )+"-"+component.getTargetVersion( ) ;
            for (Ref refTag : call) {
                
                if(refTag.getName( ).contains(strTagName))
                {
                
                    LogCommand log = git.log().setMaxCount(1);
    
                    Ref peeledRef = git.getRepository( ).peel(refTag);
                    if(peeledRef.getPeeledObjectId() != null) {
                        log.add(peeledRef.getPeeledObjectId());
                    } else {
                        log.add(refTag.getObjectId());
                    }
        
                    Iterable<RevCommit> logs = log.call();
                    for (RevCommit rev : logs) {
                        //Test if the tag was created by the release
                        if(!rev.getName( ).equals( context.getRefBranchRelease( ) ))
                        {
                            
                            git.branchDelete().setBranchNames(refTag.getName( )).setForce(true).call();
                            RefSpec refSpec = new RefSpec()
                                    .setSource(null)
                                    .setDestination(refTag.getName( ));
                            git.push().setRefSpecs(refSpec).setCredentialsProvider( new UsernamePasswordCredentialsProvider( context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getLogin(), context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.GITHUB).getPassword() ) ).
                            setRemote("origin").call();
                        }
                        
                        
                    }
                
            
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
        ReleaserUtils.logEndAction( context, " Rollback Release prepare" );
        
    }

    @Override
    public void checkoutDevelopBranchBeforePrepare( WorkflowReleaseContext context, Locale locale)
    {
        FileRepository fLocalRepo = null;
        Git git = null;
        CommandResult commandResult = context.getCommandResult( );
        Component component = context.getComponent( );
        String strComponentName = component.getName( ) ;
        String strLocalComponentPath = ReleaserUtils.getLocalComponentPath( strComponentName );
     
       
        try
        {
     
            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );
    
            git = new Git( fLocalRepo );
            git.checkout( ).setName( GitUtils.DEVELOP_BRANCH ).call( );
        
        
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
