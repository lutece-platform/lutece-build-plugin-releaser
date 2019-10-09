package fr.paris.lutece.plugins.releaser.service;

import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;

public interface IMavenPrepareUpdateRemoteRepository
{

    
    
   void updateDevelopBranch(String strLocalBasePath,WorkflowReleaseContext context, Locale locale,String strMessage);
   
   void checkoutDevelopBranchBeforePrepare(WorkflowReleaseContext context, Locale locale);
    
   void updateMasterBranch(String strLocalBasePath,WorkflowReleaseContext context, Locale locale);
    
   void rollbackRelease(String strLocalBasePath, String strScmUrl,WorkflowReleaseContext context, Locale locale);
    
}
