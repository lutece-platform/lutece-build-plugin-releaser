package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

public interface IComponentService
{

    String getLatestVersion( String strArtifactId ,boolean bCache) throws HttpAccessException, IOException;

    void getJiraInfos( Component component,boolean bCache );

    void getScmInfos( Component component,boolean bCache );

    int release( Component component, Locale locale, AdminUser user, HttpServletRequest request );

    boolean isGitComponent( Component component );

    void init( );

}
