package fr.paris.lutece.plugins.releaser.service;

import java.util.Locale;

import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import fr.paris.lutece.plugins.releaser.business.ReleaserUser.CREDENTIAL_TYPE;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.svn.ReleaseSvnCommitClient;
import fr.paris.lutece.plugins.releaser.util.svn.SvnUtils;
import fr.paris.lutece.portal.service.util.AppLogService;

public class SvnMavenPrepareUpdateRemoteRepository implements IMavenPrepareUpdateRemoteRepository
{

  
    @Override
    public void updateDevelopBranch(String strLocalBasePath,WorkflowReleaseContext context, Locale locale, String strMessage )
    {
        
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.SVN).getLogin(),
        		context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.SVN).getPassword());

        ReleaseSvnCommitClient commitClient = new ReleaseSvnCommitClient( authManager,
                SVNWCUtil.createDefaultOptions( false ) );
        
        try
        {
            SvnUtils.doCommit( strLocalBasePath, strMessage, commitClient );
        }
        catch( Exception e )
        {
          
            AppLogService.error( e );
            ReleaserUtils.addTechnicalError( context.getCommandResult( ), e.getMessage( ), e );
        }
        
        
    
        

    }

    @Override
    public void updateReleaseBranch(String strLocalBasePath, WorkflowReleaseContext context, Locale locale)
    {
        
    }
    
    @Override
    public void rollbackRelease(String strLocalBasePath,String strScmUrl, WorkflowReleaseContext context, Locale locale)
    {
    
        
       ReleaserUtils.logStartAction( context, " Rollback Release prepare" );
       
       SvnUtils.update( strLocalBasePath, context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.SVN).getLogin(), context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.SVN).getPassword() );
       Long lastRevision= SvnUtils.getLastRevision( strLocalBasePath,context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.SVN).getLogin(),
                context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.SVN).getPassword());
       
       Long lastCommitBeforeRelease=context.getRefBranchDev( )!=null?new Long( context.getRefBranchDev( ) ):null;
       
       if(lastRevision !=null && lastCommitBeforeRelease!=null && lastRevision!=lastCommitBeforeRelease )
       {
       
           SvnUtils.revert( strLocalBasePath, SvnUtils.getRepoUrl(strScmUrl),context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.SVN).getLogin(),
                context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.SVN).getPassword(), lastRevision, lastCommitBeforeRelease);
      
           
           ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.SVN).getLogin(),
                   context.getReleaserUser( ).getCredential(CREDENTIAL_TYPE.SVN).getPassword());

           
           
           
           ReleaseSvnCommitClient commitClient = new ReleaseSvnCommitClient( authManager,
                   SVNWCUtil.createDefaultOptions( false ) );
           
           
           try
           {
               SvnUtils.doCommit( strLocalBasePath, "[site-release]-Revert after error during release", commitClient );
           }
           catch( Exception e )
           {
             
               AppLogService.error( e );
               ReleaserUtils.addTechnicalError( context.getCommandResult( ), e.getMessage( ), e );
           }
           
       }
       ReleaserUtils.logEndAction( context, " Rollback Release prepare" );

    
       
    }

    @Override
    public void checkoutDevelopBranchBeforePrepare( WorkflowReleaseContext context, Locale locale )
    {
        // TODO Auto-generated method stub
        
    }

  

}
