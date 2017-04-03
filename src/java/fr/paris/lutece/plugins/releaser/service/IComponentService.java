package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

public interface IComponentService
{

   void setRemoteInformations( Component component,boolean bCache) throws HttpAccessException, IOException;
   
   void updateRemoteInformations( Component component );
   
   
   int release( Component component, Locale locale, AdminUser user, HttpServletRequest request );

    boolean isGitComponent( Component component );
    
    /**
     * Returns the LastAvailableVersion
     * 
     * @return The LastAvailableVersion
     */
    String getLastReleaseVersion( String strArtifactId);    
    /**
     * set  the LastAvailableVersion
     * 
     * set The LastAvailableVersion
     */
    void setLastReleaseVersion( String strArtifactId,String strVersion);
    
    /**
     * Returns the LastAvailableVersion
     * 
     * @return The LastAvailableVersion
     */
    String getLastReleaseNextSnapshotVersion( String strArtifactId);
    

    
    /**
     * set the LastAvailableVersion
     * 
     * set The LastAvailableVersion
     */
     void setLastReleaseNextSnapshotVersion( String strArtifactId,String strVersion);

    void init( );

}
