package fr.paris.lutece.plugins.releaser.service;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.portal.business.user.AdminUser;

public interface IWorkflowReleaseContextService
{

    
    void startWorkflowReleaseContext(WorkflowReleaseContext context,int nIdWorkflow, Locale locale,HttpServletRequest request,AdminUser user );

    int addWorkflowReleaseContext( WorkflowReleaseContext context );
    
    void saveWorkflowReleaseContext( WorkflowReleaseContext context );

    WorkflowReleaseContext getWorkflowReleaseContext( int nIdContext );
    
    WorkflowReleaseContext getWorkflowReleaseContextHistory( int nIdContext, String strArtifactId );
    
    List<WorkflowReleaseContext> getListWorkflowReleaseContextHistory( String strArtifactId );
    
    int getIdWorkflow(WorkflowReleaseContext context);
    
    
    void gitCloneRepository( WorkflowReleaseContext context, Locale locale );
    
    void gitMerge( WorkflowReleaseContext context, Locale locale );
    
    
    void releasePrepareGit( WorkflowReleaseContext context, Locale locale );
    
    void rollBackReleasePrepareGit( WorkflowReleaseContext context, Locale locale );
    
    void releasePrepareSvn( WorkflowReleaseContext context, Locale locale );
    
    void releasePerformGit( WorkflowReleaseContext context, Locale locale );
    
    void releasePerformSvn( WorkflowReleaseContext context, Locale locale );
    
    void sendTweet( WorkflowReleaseContext context, Locale locale );
    
    
    void checkoutSite( WorkflowReleaseContext context, Locale locale );
    
    
    void releaseSite( WorkflowReleaseContext context, Locale locale );
    
    void checkoutComponent( WorkflowReleaseContext context, Locale locale );
    
    
    void init();

}
