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

package fr.paris.lutece.plugins.releaser.service.svn;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.service.ComponentService;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.IVCSSiteService;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.file.FileUtils;
import fr.paris.lutece.plugins.releaser.util.svn.ReleaseSvnCheckoutClient;
import fr.paris.lutece.plugins.releaser.util.svn.ReleaseSvnCommitClient;
import fr.paris.lutece.plugins.releaser.util.svn.ReleaseSvnCopyClient;
import fr.paris.lutece.plugins.releaser.util.svn.SvnUtils;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.signrequest.BasicAuthorizationAuthenticator;
import fr.paris.lutece.util.signrequest.RequestAuthenticator;

/**
 * SvnSiteService
 */
public class SvnResourceService implements IVCSSiteService
{

    /**
     * Fetch the pom.xml content from a repository
     * 
     * @param strPomUrl
     *            The POM URL
     * @return The POM content
     */
    public String fetchPom( Site site, String strLogin, String strPassword )
    {

        String strPomUrl = site.getScmUrl( ) + "/pom.xml";
        try
        {
            RequestAuthenticator authenticator = getSiteAuthenticator( strLogin, strPassword );
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
     * 
     * @param strSiteArtifactId
     *            The site artifact id
     * @param strTrunkUrl
     *            The trunk URL
     * @return The version if found otherwise null
     */
    public String getLastRelease( Site site, String strLogin, String strPassword )
    {

        String strSiteArtifactId = site.getArtifactId( );
        String strTrunkUrl = site.getScmUrl( );
        String strTagsUrl = strTrunkUrl.replace( "trunk", "tags" );

        List<String> list = new ArrayList<>( );

        try
        {
            RequestAuthenticator authenticator = getSiteAuthenticator( strLogin, strPassword );
            HttpAccess httpAccess = new HttpAccess( );
            String strHtml = httpAccess.doGet( strTagsUrl, authenticator, null );
            list = getAnchorsList( strHtml, strSiteArtifactId );
        }
        catch( HttpAccessException e )
        {
            AppLogService.error( "SvnSiteService : Error retrieving release version : " + e.getMessage( ), e );
        }

        String strLastRelease = ( !list.isEmpty( ) ) ? list.get( list.size( ) - 1 ) : null;

        if ( strLastRelease != null && strLastRelease.contains( "-" ) )
        {

            String [ ] tabRelease = strLastRelease.split( "-" );
            strLastRelease = tabRelease [tabRelease.length - 1];
        }
        else
        {
            strLastRelease = "";
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
    private static List<String> getAnchorsList( String strHtml, String strPrefix )
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
            if ( strTag.startsWith( strPrefix ) )
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
    private static RequestAuthenticator getSiteAuthenticator( String strLogin, String strPassword )
    {

        return new BasicAuthorizationAuthenticator( strLogin, strPassword );
    }




    
    @Override
    public String doCheckoutRepository( WorkflowReleaseContext context, String strLogin, String strPassword )
    {
        CommandResult commandResult = context.getCommandResult( );

        ReleaserUtils.logStartAction( context, " Checkout Svn" );
    
        String strLocalBasePath  = ReleaserUtils.getLocalPath( context );
        

        File file = new File( strLocalBasePath );

        if ( file.exists( ) )
        {

            commandResult.getLog( ).append( "Local SVN  " + strLocalBasePath + " exist\nCleaning Local folder...\n" );
            if ( !FileUtils.delete( file, commandResult.getLog( ) ) )
            {
                commandResult.setError( commandResult.getLog( ).toString( ) );

            }
            commandResult.getLog( ).append( "Local SVN  has been cleaned\n" );
        }
        
        
        
        
        // PROGRESS 5%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );

        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( strLogin,
                strPassword );
       
        
          ReleaseSvnCheckoutClient updateClient = new ReleaseSvnCheckoutClient( authManager,
                    SVNWCUtil.createDefaultOptions( false ) );
            
        
        commandResult.getLog( ).append( "Checkout SVN  ...\n" );
        Long nLastCommitId=null;
        
        try
        {
            nLastCommitId =SvnUtils.doSvnCheckout( SvnUtils.getRepoUrl( context.getReleaserResource( ).getScmUrl( )), strLocalBasePath, updateClient,
                    commandResult );
            
           SvnUtils.getLastRevision( strLocalBasePath, strLogin, strPassword );
        }
        catch ( Exception e )
        {
            ReleaserUtils.addTechnicalError(commandResult,"errreur lors du checkout du composant"+ e.getMessage(),e);
        }
        
        
        if(nLastCommitId!=null)
        {
            context.setRefBranchDev( nLastCommitId.toString( ) );
        }
        // PROGRESS 10%
        commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );
        
        if(context.getSite( )==null && ComponentService.getService( ).isErrorSnapshotComponentInformations( context.getComponent( ) ,ReleaserUtils.getLocalPomPath( context)))
        {
            ReleaserUtils.addTechnicalError( commandResult,"The checkout component does not match the release informations");
         }
        

        ReleaserUtils.logEndAction( context, "Checkout Svn Component " );
        return ConstanteUtils.CONSTANTE_EMPTY_STRING;
    }

    @Override
    public void updateDevelopBranch( WorkflowReleaseContext context, Locale locale, String strMessage )
    {
        
        String strLogin =context.getReleaserUser( ).getCredential(context.getReleaserResource( ).getRepoType( )).getLogin();
        String strPassword =context.getReleaserUser( ).getCredential(context.getReleaserResource( ).getRepoType( )).getPassword( );
        String strLocalPath = ReleaserUtils.getLocalPath( context );
        
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(strLogin,
                strPassword);

        ReleaseSvnCommitClient commitClient = new ReleaseSvnCommitClient( authManager,
                SVNWCUtil.createDefaultOptions( false ) );
        
        try
        {
            SvnUtils.doCommit( strLocalPath, strMessage, commitClient );
        }
        catch( Exception e )
        {
          
            AppLogService.error( e );
            ReleaserUtils.addTechnicalError( context.getCommandResult( ), e.getMessage( ), e );
        }
        
        
    
        
    }

    @Override
    public void updateMasterBranch( WorkflowReleaseContext context, Locale locale )
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rollbackRelease( WorkflowReleaseContext context, Locale locale )
    {
        
        String strLogin =context.getReleaserUser( ).getCredential(context.getReleaserResource( ).getRepoType( )).getLogin();
        String strPassword =context.getReleaserUser( ).getCredential(context.getReleaserResource( ).getRepoType( )).getPassword( );
        String strLocalPath = ReleaserUtils.getLocalPath( context );
       
        ReleaserUtils.logStartAction( context, " Rollback Release prepare" );
        
        SvnUtils.update( strLocalPath,strLogin, strPassword);
        Long lastRevision= SvnUtils.getLastRevision( strLocalPath,strLogin,
                 strPassword);
        
        Long lastCommitBeforeRelease=context.getRefBranchDev( )!=null?new Long( context.getRefBranchDev( ) ):null;
        
        if(lastRevision !=null && lastCommitBeforeRelease!=null && lastRevision!=lastCommitBeforeRelease )
        {
        
            SvnUtils.revert( strLocalPath, SvnUtils.getRepoUrl(context.getReleaserResource( ).getScmUrl( )),strLogin,
                 strPassword, lastRevision, lastCommitBeforeRelease);
       
            
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(strLogin,
                    strPassword);

            
            
            
            ReleaseSvnCommitClient commitClient = new ReleaseSvnCommitClient( authManager,
                    SVNWCUtil.createDefaultOptions( false ) );
            
            
            try
            {
                SvnUtils.doCommit( strLocalPath, "[site-release]-Revert after error during release", commitClient );
            }
            catch( Exception e )
            {
              
                AppLogService.error( e );
                ReleaserUtils.addTechnicalError( context.getCommandResult( ), e.getMessage( ), e );
            }
            
        }
        ReleaserUtils.logEndAction( context, " Rollback Release prepare" );
        
    }

    @Override
    public void checkoutDevelopBranch( WorkflowReleaseContext context, Locale locale )
    {
        // TODO Auto-generated method stub
        
    }

}
