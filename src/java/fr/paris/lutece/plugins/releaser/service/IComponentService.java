package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;



import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
// TODO: Auto-generated Javadoc

/**
 * IComponentService .
 */
public interface IComponentService
{

   /**
    * Sets the remote informations.
    *
    * @param component the component
    * @param bCache the b cache
    * @throws HttpAccessException the http access exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
void setRemoteInformations( Component component,boolean bCache) throws HttpAccessException, IOException;
   
   /**
    * Update remote informations.
    *
    * @param component the component
    */
void updateRemoteInformations( Component component );
   
   
   /**
    * Release.
    *
    * @param component the component
    * @param locale the locale
    * @param user the user
    * @param request the request
    * @param forceRelease the force release
    * @return context id
    */
int release( Component component, Locale locale, AdminUser user, HttpServletRequest request,boolean forceRelease );

   /**
    * Release.
    *
    * @param component the component
    * @param locale the locale
    * @param user the user
    * @param request the request
    * @return context id
    */
int release( Component component, Locale locale, AdminUser user, HttpServletRequest request );

    /**
     * Checks if is git component.
     *
     * @param component the component
     * @return boolean
     */
    boolean isGitComponent( Component component );
    
    /**
     * Returns the LastAvailableVersion.
     *
     * @param strArtifactId the str artifact id
     * @return The LastAvailableVersion
     */
    /**
     * @param strArtifactId
     * @return lastRelease version
     */
    String getLastReleaseVersion( String strArtifactId);    
    
    /**
     * set  the LastAvailableVersion
     * 
     * set The LastAvailableVersion.
     *
     * @param strArtifactId the str artifact id
     * @param strVersion the str version
     */
    /**
     * @param strArtifactId
     * @param strVersion
     */
    void setLastReleaseVersion( String strArtifactId,String strVersion);
    
    /**
     * Returns the LastAvailableVersion.
     *
     * @param strArtifactId the str artifact id
     * @return The LastAvailableVersion
     */
    /**
     * @param strArtifactId
     * @return LastReleaseNextSnapshotVersion
     */
    String getLastReleaseNextSnapshotVersion( String strArtifactId);
    

    
    /**
     * setLastReleaseNextSnapshotVersion.
     *
     * @param strArtifactId the str artifact id
     * @param strVersion the str version
     */
    void setLastReleaseNextSnapshotVersion( String strArtifactId,String strVersion);
     
     
     /**
      * Gets the search component.
      *
      * @param strSearch the str search
      * @param request the request
      * @param locale the locale
      * @param strPaginateUrl the str paginate url
      * @param strCurrentPageIndex the str current page index
      * @return local paginator
      */
    LocalizedPaginator<Component> getSearchComponent( String strSearch, HttpServletRequest request, Locale locale,String strPaginateUrl,String strCurrentPageIndex);
    
     /**
      * Load component.
      *
      * @param component the component
      * @param strPom the str pom
      * @param stUser the st user
      * @param strPassword the str password
      * @return component
      */
    Component loadComponent(Component component,String strPom, String stUser,String strPassword);
     
     /**
      * Change next release version.
      *
      * @param component component
      */
    void changeNextReleaseVersion(Component component );
     
     /**
      * Checks if is error snapshot component informations.
      *
      * @param component the component
      * @param strComponentPomPath the str component pom path
      * @return boolean
      */
    boolean isErrorSnapshotComponentInformations( Component component ,String strComponentPomPath);

     /**
      * Inits the.
      */
    void init( );

}
