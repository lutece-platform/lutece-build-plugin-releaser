/*
 * Copyright (c) 2002-2015, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */


package fr.paris.lutece.plugins.releaser.util.svn;

import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser.CREDENTIAL_TYPE;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.signrequest.BasicAuthorizationAuthenticator;
import fr.paris.lutece.util.signrequest.RequestAuthenticator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * SvnSiteService
 */
public class SvnSiteService 
{
    private static final String PROPERTY_SITE_REPOSITORY_LOGIN = "releaser.site.repository.login";
    private static final String PROPERTY_SITE_REPOSITORY_PASSWORD = "releaser.site.repository.password";

    /**
     * Fetch the pom.xml content from a repository
     * 
     * @param strPomUrl
     *            The POM URL
     * @return The POM content
     */
    public static String fetchPom( String strPomUrl,HttpServletRequest request,Locale locale )
    {
        try
        {
            RequestAuthenticator authenticator = getSiteAuthenticator( request,locale);
            HttpAccess httpAccess = new HttpAccess( );
            return httpAccess.doGet( strPomUrl, authenticator, null );
        }
        catch( HttpAccessException ex )
        {
            AppLogService.error( "Error fecthing pom.xml content : " + ex.getMessage( ), ex );
        }
        return null;
    }

    /**
     * Gets the last release found in the SVN repository
     * @param strSiteArtifactId The site artifact id
     * @param strTrunkUrl The trunk URL
     * @return The version if found otherwise null
     */
    public static String getLastRelease( String strSiteArtifactId , String strTrunkUrl,HttpServletRequest request,Locale locale )
    {
        String strTagsUrl = strTrunkUrl.replace( "trunk", "tags" );

        List<String> list = new ArrayList<>( );

        try
        {
            RequestAuthenticator authenticator = getSiteAuthenticator(request,locale );
            HttpAccess httpAccess = new HttpAccess( );
            String strHtml = httpAccess.doGet( strTagsUrl , authenticator , null );
            list = getAnchorsList( strHtml , strSiteArtifactId );
        }
        catch( HttpAccessException e )
        {
            AppLogService.error( "SvnSiteService : Error retrieving release version : " + e.getMessage( ), e );
        }

          String strLastRelease= ( !list.isEmpty() ) ? list.get( list.size() - 1 ) : null;
          
          if(strLastRelease!=null && strLastRelease.contains( "-" ))
          {
             
              String[] tabRelease=strLastRelease.split( "-" );
               strLastRelease=tabRelease[tabRelease.length-1];
          }
          else
          {
              strLastRelease="";
          }
          return strLastRelease;
        
    }
    
    /**
     * Gets anchor list using more optimized method
     *
     * @param strHtml
     *            The HTML code
     * @return The list
     */
    private static List<String> getAnchorsList( String strHtml , String strPrefix )
    {
        List<String> list = new ArrayList<String>( );
        String strCurrent = strHtml;

        int nPos = strCurrent.indexOf( "<a " );

        while ( nPos > 0 )
        {
            strCurrent = strCurrent.substring( nPos );

            int nEndTag = strCurrent.indexOf( '>' );
            int nTagEnd = strCurrent.indexOf( "</a>" );
            String strTag = strCurrent.substring( nEndTag + 1, nTagEnd ).replaceAll( "\\/", "" );
            if( strTag.startsWith( strPrefix ))
            {
                list.add( strTag );
            }
            strCurrent = strCurrent.substring( nTagEnd + 4 );
            nPos = strCurrent.indexOf( "<a " );
        }

        return list;
    }

    /**
     * Build an authenticathor to access the site repository
     * 
     * @return The authenticator
     */
    private static RequestAuthenticator getSiteAuthenticator( HttpServletRequest request,Locale locale)
    {
        ReleaserUser user=ReleaserUtils.getReleaserUser( request, locale );
        String strLogin=null;
        String strPassword=null;
        
        if(user!=null)
        {
            strLogin=user.getCredential(CREDENTIAL_TYPE.SVN).getLogin();
            strPassword=user.getCredential(CREDENTIAL_TYPE.SVN).getPassword();
        }
        return new BasicAuthorizationAuthenticator( strLogin, strPassword );
    }
    
}
