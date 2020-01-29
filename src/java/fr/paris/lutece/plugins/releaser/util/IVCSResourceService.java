package fr.paris.lutece.plugins.releaser.util;

import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;

// TODO: Auto-generated Javadoc
/**
 * The Interface IVCSResourceService.
 */
public interface IVCSResourceService
{

    /**
     * Fetch pom.
     *
     * @param site the site
     * @param strLogin the str login
     * @param strPassword the str password
     * @return the string
     */
    String fetchPom( Site site, String strLogin, String strPassword );

    /**
     * Gets the last release.
     *
     * @param site the site
     * @param strLogin the str login
     * @param strPassword the str password
     * @return the last release
     */
    String getLastRelease( Site site, String strLogin, String strPassword );

    /**
     * Do checkout repository.
     *
     * @param context the context
     * @param strLogin the str login
     * @param strPassword the str password
     * @return the string
     */
    String doCheckoutRepository( WorkflowReleaseContext context, String strLogin, String strPassword );

    /**
     * Update develop branch.
     *
     * @param context the context
     * @param locale the locale
     * @param strMessage the str message
     */
    void updateDevelopBranch( WorkflowReleaseContext context, Locale locale, String strMessage );

    /**
     * Update master branch.
     *
     * @param context the context
     * @param locale the locale
     */
    void updateMasterBranch( WorkflowReleaseContext context, Locale locale );

    /**
     * Rollback release.
     *
     * @param context the context
     * @param locale the locale
     */
    void rollbackRelease( WorkflowReleaseContext context, Locale locale );

    /**
     * Checkout develop branch.
     *
     * @param context the context
     * @param locale the locale
     */
    void checkoutDevelopBranch( WorkflowReleaseContext context, Locale locale );

}
