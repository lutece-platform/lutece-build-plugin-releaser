package fr.paris.lutece.plugins.releaser.service;

import java.util.Locale;

import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

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
        
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(context.getReleaserUser( ).getSvnComponentAccountLogin( ),
                context.getReleaserUser( ).getSvnComponentAccountPassword( ));

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
       Long lastRevision= SvnUtils.getLastRevision( strLocalBasePath,context.getReleaserUser( ).getSvnComponentAccountLogin( ),
                context.getReleaserUser( ).getSvnComponentAccountPassword( ) );
       
       Long lastCommitBeforeRelease=context.getRefBranchDev( )!=null?new Long( context.getRefBranchDev( ) ):null;
       
       if(lastRevision !=null && lastCommitBeforeRelease!=null && lastRevision!=lastCommitBeforeRelease )
       {
       
           SvnUtils.revert( strLocalBasePath, SvnUtils.getRepoUrl(strScmUrl),context.getReleaserUser( ).getSvnComponentAccountLogin( ),
                context.getReleaserUser( ).getSvnComponentAccountPassword( ), lastRevision, lastCommitBeforeRelease);
      
           ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(context.getReleaserUser( ).getSvnComponentAccountLogin( ),
                   context.getReleaserUser( ).getSvnComponentAccountPassword( ));

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
