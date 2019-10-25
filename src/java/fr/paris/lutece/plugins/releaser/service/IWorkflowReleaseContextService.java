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
    
    
    void checkoutRepository( WorkflowReleaseContext context, Locale locale );
    
    void mergeDevelopMaster( WorkflowReleaseContext context, Locale locale );
    
    
    void releasePrepareComponent( WorkflowReleaseContext context, Locale locale );
         
    void releasePrepareSite( WorkflowReleaseContext context, Locale locale );
    
    void releasePerformComponent( WorkflowReleaseContext context, Locale locale );
    
    void releasePerformSite( WorkflowReleaseContext context, Locale locale );
    
    void sendTweet( WorkflowReleaseContext context, Locale locale );
    
   
    void updateJiraVersions( WorkflowReleaseContext context, Locale locale );
    
    void startReleaseInProgress(String strArtifactId);
   
    void stopReleaseInProgress(String strArtifactId);
    
    boolean isReleaseInProgress(String strArtifactId);
    

    
    void init();

}
