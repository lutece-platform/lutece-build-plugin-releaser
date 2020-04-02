package fr.paris.lutece.plugins.releaser.service;

import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;

// TODO: Auto-generated Javadoc
/**
 * The Interface IMavenPrepareUpdateRemoteRepository.
 */
public interface IMavenPrepareUpdateRemoteRepository
{

    /**
     * Update develop branch.
     *
     * @param strLocalBasePath the str local base path
     * @param context the context
     * @param locale the locale
     * @param strMessage the str message
     */
    void updateDevelopBranch( String strLocalBasePath, WorkflowReleaseContext context, Locale locale, String strMessage );

    /**
     * Checkout develop branch before prepare.
     *
     * @param context the context
     * @param locale the locale
     */
    void checkoutDevelopBranchBeforePrepare( WorkflowReleaseContext context, Locale locale );

    /**
     * Update master branch.
     *
     * @param strLocalBasePath the str local base path
     * @param context the context
     * @param locale the locale
     */
    void updateMasterBranch( String strLocalBasePath, WorkflowReleaseContext context, Locale locale );

    /**
     * Rollback release.
     *
     * @param strLocalBasePath the str local base path
     * @param strScmUrl the str scm url
     * @param context the context
     * @param locale the locale
     */
    void rollbackRelease( String strLocalBasePath, String strScmUrl, WorkflowReleaseContext context, Locale locale );

}
