package fr.paris.lutece.plugins.releaser.service;

import java.util.Locale;

import org.tmatesoft.svn.core.SVNException;
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
    public void rollbackRelease(String strLocalBasePath, WorkflowReleaseContext context, Locale locale)
    {
    
    }

  

}
