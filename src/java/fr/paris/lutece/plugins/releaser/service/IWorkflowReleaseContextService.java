package fr.paris.lutece.plugins.releaser.service;

import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;

public interface IWorkflowReleaseContextService
{

    int addWorkflowDeploySiteContext( WorkflowReleaseContext context );

    WorkflowReleaseContext getWorkflowDeploySiteContext( int nIdContext );
    
    int getIdWorkflow(WorkflowReleaseContext context);
    
    
    void gitCloneRepository( WorkflowReleaseContext context, Locale locale );
    
    void gitMerge( WorkflowReleaseContext context, Locale locale );
    
    
    void realeasePrepareGit( WorkflowReleaseContext context, Locale locale );
    
    void realeasePerformGit( WorkflowReleaseContext context, Locale locale );

}
