package fr.paris.lutece.plugins.releaser.service;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.portal.business.user.AdminUser;

// TODO: Auto-generated Javadoc
/**
 * The Interface IWorkflowReleaseContextService.
 */
public interface IWorkflowReleaseContextService
{

    /**
     * Start workflow release context.
     *
     * @param context the context
     * @param nIdWorkflow the n id workflow
     * @param locale the locale
     * @param request the request
     * @param user the user
     */
    void startWorkflowReleaseContext( WorkflowReleaseContext context, int nIdWorkflow, Locale locale, HttpServletRequest request, AdminUser user );

    /**
     * Adds the workflow release context.
     *
     * @param context the context
     * @return the int
     */
    int addWorkflowReleaseContext( WorkflowReleaseContext context );

    /**
     * Save workflow release context.
     *
     * @param context the context
     */
    void saveWorkflowReleaseContext( WorkflowReleaseContext context );

    /**
     * Gets the workflow release context.
     *
     * @param nIdContext the n id context
     * @return the workflow release context
     */
    WorkflowReleaseContext getWorkflowReleaseContext( int nIdContext );

    

    /**
     * Gets the list workflow release context history.
     *
     * @param strArtifactId the str artifact id
     * @return the list workflow release context history
     */
    List<WorkflowReleaseContext> getListWorkflowReleaseContextHistory( String strArtifactId );

    /**
     * Gets the id workflow.
     *
     * @param context the context
     * @return the id workflow
     */
    int getIdWorkflow( WorkflowReleaseContext context );

    /**
     * Checkout repository.
     *
     * @param context the context
     * @param locale the locale
     */
    void checkoutRepository( WorkflowReleaseContext context, Locale locale );

    /**
     * Merge develop master.
     *
     * @param context the context
     * @param locale the locale
     */
    void mergeDevelopMaster( WorkflowReleaseContext context, Locale locale );

    /**
     * Release prepare component.
     *
     * @param context the context
     * @param locale the locale
     */
    void releasePrepareComponent( WorkflowReleaseContext context, Locale locale );

    /**
     * Release prepare site.
     *
     * @param context the context
     * @param locale the locale
     */
    void releasePrepareSite( WorkflowReleaseContext context, Locale locale );

    /**
     * Release perform component.
     *
     * @param context the context
     * @param locale the locale
     */
    void releasePerformComponent( WorkflowReleaseContext context, Locale locale );

    /**
     * Release perform site.
     *
     * @param context the context
     * @param locale the locale
     */
    void releasePerformSite( WorkflowReleaseContext context, Locale locale );

    /**
     * Send tweet.
     *
     * @param context the context
     * @param locale the locale
     */
    void sendTweet( WorkflowReleaseContext context, Locale locale );

    /**
     * Update jira versions.
     *
     * @param context the context
     * @param locale the locale
     */
    void updateJiraVersions( WorkflowReleaseContext context, Locale locale );

    /**
     * Start release in progress.
     *
     * @param strArtifactId the str artifact id
     */
    void startReleaseInProgress( String strArtifactId );

    /**
     * Stop release in progress.
     *
     * @param strArtifactId the str artifact id
     */
    void stopReleaseInProgress( String strArtifactId );

    /**
     * Checks if is release in progress.
     *
     * @param strArtifactId the str artifact id
     * @return true, if is release in progress
     */
    boolean isReleaseInProgress( String strArtifactId );

    /**
     * Inits the.
     */
    void init( );

}
