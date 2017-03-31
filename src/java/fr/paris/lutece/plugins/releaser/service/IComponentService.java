package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

public interface IComponentService
{

    String getLatestVersion( String strArtifactId ,boolean bCache,String strComponentType) throws HttpAccessException, IOException;

    void getJiraInfos( Component component,boolean bCache,String strComponentType );

    void getScmInfos( Component component,boolean bCache,String strComponentType );

    int release( Component component, Locale locale, AdminUser user, HttpServletRequest request );

    boolean isGitComponent( Component component );

    void init( );

}
