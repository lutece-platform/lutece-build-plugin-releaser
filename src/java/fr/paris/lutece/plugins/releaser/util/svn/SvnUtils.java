/*
 * Copyright (c) 2002-2013, Mairie de Paris
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitPacket;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNRevisionRange;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.file.FileUtils;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceList;

// TODO: Auto-generated Javadoc
/**
 * The Class SvnUtils.
 */
public final class SvnUtils
{
    
    /** The Constant MESSAGE_ERROR_SVN. */
    private static final String MESSAGE_ERROR_SVN = "Impossible de se connecter au SVN. Veuillez verifier vos identifiants";
    
    /** The Constant CONSTANTE_SLASH. */
    private static final String CONSTANTE_SLASH = "/";
    
    /** The Constant logger. */
    private static final Logger logger = Logger.getLogger( SvnUtils.class );

    /**
     * Constructeur vide.
     */
    private SvnUtils( )
    {
        // nothing
    }

    /**
     * Initialise les diff�rentes factory pour le svn.
     */
    public static void init( )
    {
        /*
         * For using over http:// and https:
         */
        DAVRepositoryFactory.setup( );
        /*
         * For using over svn:// and svn+xxx:
         */
        SVNRepositoryFactoryImpl.setup( );

        /*
         * For using over file:/
         */
        FSRepositoryFactory.setup( );
    }

    /**
     * Commit.
     *
     * @param strPathFile the str path file
     * @param strCommitMessage the str commit message
     * @param commitClient the commit client
     * @throws SVNException the SVN exception
     */
    public static void doCommit( String strPathFile, String strCommitMessage, ReleaseSvnCommitClient commitClient ) throws SVNException
    {
        SVNCommitPacket commitPacket = commitClient.doCollectCommitItems( new File [ ] {
                new File( strPathFile )
        }, false, false, true );

        if ( !SVNCommitPacket.EMPTY.equals( commitPacket ) )
        {
            commitClient.doCommit( commitPacket, false, strCommitMessage );
        }
    }

    /**
     * Tag un site.
     *
     * @param strSiteName            le nom du site
     * @param strTagName            le nom du tag
     * @param strSrcURL the str src URL
     * @param strDstURL the str dst URL
     * @param copyClient            le client svn permettant la copie
     * @return string
     * @throws SVNException the SVN exception
     */
    public static String doTagSite( String strSiteName, String strTagName, String strSrcURL, String strDstURL, SVNCopyClient copyClient ) throws SVNException
    {
        // COPY from trunk to tags/tagName
        SVNURL srcURL = SVNURL.parseURIEncoded( strSrcURL );
        SVNURL dstURL = SVNURL.parseURIEncoded( strDstURL );
        SVNCopySource svnCopySource = new SVNCopySource( SVNRevision.HEAD, SVNRevision.HEAD, srcURL );
        SVNCopySource [ ] tabSVNCopy = new SVNCopySource [ 1];
        tabSVNCopy [0] = svnCopySource;

        SVNCommitInfo info = copyClient.doCopy( tabSVNCopy, dstURL, false, false, false, "[site-release] Tag site " + strSiteName + " to " + strTagName, null );

        if ( info.getErrorMessage( ) != null )
        {

            return info.getErrorMessage( ).getMessage( );
        }

        return null;
    }

    /**
     * Do svn checkout.
     *
     * @param strUrl the str url
     * @param strCheckoutBaseSitePath the str checkout base site path
     * @param updateClient the update client
     * @param result the result
     * @return the long
     * @throws SVNException the SVN exception
     */
    public static Long doSvnCheckout( String strUrl, String strCheckoutBaseSitePath, ReleaseSvnCheckoutClient updateClient, CommandResult result )
            throws SVNException
    {
        Long nLastCommitId = null;
        SVNURL url = SVNURL.parseURIEncoded( strUrl );
        File file = new File( strCheckoutBaseSitePath );

        if ( file.exists( ) )
        {
            if ( !FileUtils.delete( file, result.getLog( ) ) )
            {
                result.setError( result.getLog( ).toString( ) );
                ReleaserUtils.addTechnicalError( result, "Fail to delete file" );

            }
        }

        SVNRepository repository = SVNRepositoryFactory.create( url, null );
        final StringBuffer logBuffer = result.getLog( );

        try
        {
            updateClient.setEventHandler( new ISVNEventHandler( )
            {
                public void checkCancelled( ) throws SVNCancelException
                {
                    // Do nothing
                }

                public void handleEvent( SVNEvent event, double progress ) throws SVNException
                {
                    logBuffer.append( ( ( event.getAction( ) == SVNEventAction.UPDATE_ADD ) ? "ADDED " : event.getAction( ) ) + " " + event.getFile( ) + "\n" );
                }
            } );

            // SVNDepth.INFINITY + dernier param�tre � FALSE pour la version 1.3.2
            nLastCommitId = updateClient.doCheckout( repository.getLocation( ), file, SVNRevision.HEAD, SVNRevision.HEAD, true );

        }
        catch( SVNAuthenticationException e )
        {
            // _result.getLog( ).append( CONSTANTE_NO_LOGIN_PASSWORD );
            // _result.setStatus( ICommandThread.STATUS_EXCEPTION );
            // _result.setRunning( false );

            ReleaserUtils.addTechnicalError( result, "Une erreur est survenue lors de la tentative d'authentification avec le svn" + e, e );

            StringWriter sw = new StringWriter( );
            PrintWriter pw = new PrintWriter( sw );
            e.printStackTrace( pw );

            String errorLog = sw.toString( );
            pw.flush( );
            pw.close( );

            try
            {
                sw.flush( );
                sw.close( );
            }
            catch( IOException e1 )
            {
                // do nothing
                // _logger.error( e1 );
            }

            // _result.setLog( _result.getLog( ).append( errorLog ) );
            // _logger.error( e );

            // _result.setIdError( ReleaseLogger.logError( _result.getLog( ).toString( ), e ) );
        }
        catch( Exception e )
        {
            // _result.setStatus( ICommandThread.STATUS_EXCEPTION );
            // _result.setRunning( false );
            StringWriter sw = new StringWriter( );
            PrintWriter pw = new PrintWriter( sw );
            e.printStackTrace( pw );

            String errorLog = sw.toString( );
            pw.flush( );
            pw.close( );

            try
            {
                sw.flush( );
                sw.close( );
            }
            catch( IOException e1 )
            {
                // do nothing
                // _logger.error( e1 );
            }

            ReleaserUtils.addTechnicalError( result, "Une erreur svn est survenue:" + e, e );

        }

        return nLastCommitId;
    }

    /**
     * Gets the svn sites.
     *
     * @param strUrlSite the str url site
     * @param clientManager the client manager
     * @return the svn sites
     * @throws SVNException the SVN exception
     */
    public static ReferenceList getSvnSites( String strUrlSite, SVNClientManager clientManager ) throws SVNException
    {
        final ReferenceList listSites = new ReferenceList( );
        final SVNURL url;

        url = SVNURL.parseURIEncoded( strUrlSite );

        SVNRepository repository = SVNRepositoryFactory.create( url, null );

        clientManager.getLogClient( ).doList( repository.getLocation( ), SVNRevision.HEAD, SVNRevision.HEAD, false, false, new ISVNDirEntryHandler( )
        {
            public void handleDirEntry( SVNDirEntry entry ) throws SVNException
            {

                if ( !url.equals( entry.getURL( ) ) )
                {
                    if ( entry.getKind( ) == SVNNodeKind.DIR )
                    {
                        listSites.addItem( entry.getName( ), entry.getName( ) );
                    }
                }
            }
        } );

        return listSites;
    }

    /**
     * Gets the last revision.
     *
     * @param strRepoPath the str repo path
     * @param strUserName the str user name
     * @param strPassword the str password
     * @return the last revision
     */
    public static Long getLastRevision( String strRepoPath, String strUserName, String strPassword )
    {
        Long lRevision = null;
        SVNClientManager clientManager = SVNClientManager.newInstance( new DefaultSVNOptions( ), strUserName, strPassword );
        SVNRevision revision;
        try
        {
            File fStrRepo = new File( strRepoPath );
            revision = clientManager.getStatusClient( ).doStatus( fStrRepo, true ).getCommittedRevision( );
            if ( revision != null )
            {
                return revision.getNumber( );
            }
        }
        catch( SVNException e )
        {
            AppLogService.error( e );
        }

        return lRevision;
    }

    /**
     * Update.
     *
     * @param strRepoPath the str repo path
     * @param strUserName the str user name
     * @param strPassword the str password
     */
    public static void update( String strRepoPath, String strUserName, String strPassword )
    {
        SVNClientManager clientManager = SVNClientManager.newInstance( new DefaultSVNOptions( ), strUserName, strPassword );

        try
        {
            File fStrRepo = new File( strRepoPath );
            clientManager.getUpdateClient( ).doUpdate( fStrRepo, SVNRevision.HEAD, true );

        }
        catch( SVNException e )
        {
            AppLogService.error( e );
        }

    }

    /**
     * Revert.
     *
     * @param strRepoPath the str repo path
     * @param strCmUrl the str cm url
     * @param strUserName the str user name
     * @param strPassword the str password
     * @param revCurrentCommit the rev current commit
     * @param lRevertCommit the l revert commit
     */
    public static void revert( String strRepoPath, String strCmUrl, String strUserName, String strPassword, Long revCurrentCommit, Long lRevertCommit )
    {

        SVNClientManager clientManager = SVNClientManager.newInstance( new DefaultSVNOptions( ), strUserName, strPassword );

        SVNDiffClient diffClient = clientManager.getDiffClient( );
        SVNRevision sRevertCommit = SVNRevision.create( lRevertCommit );
        SVNRevision sLastCommit = SVNRevision.create( revCurrentCommit );

        if ( revCurrentCommit > lRevertCommit )
        {

            SVNRevisionRange rangeToMerge = new SVNRevisionRange( sLastCommit, sRevertCommit );

            try
            {
                diffClient.doMerge( SVNURL.parseURIEncoded( strCmUrl ), sLastCommit, Collections.singleton( rangeToMerge ), new File( strRepoPath ),
                        SVNDepth.INFINITY, true, false, false, false );
            }
            catch( SVNException e )
            {
                AppLogService.error( e );
            }
        }

    }

    /**
     * Gets the svn url tag site.
     *
     * @param strScmUrl the str scm url
     * @param strTagName the str tag name
     * @return the svn url tag site
     */
    public static String getSvnUrlTagSite( String strScmUrl, String strTagName )
    {

        String strUrl = strScmUrl.contains( ConstanteUtils.CONSTANTE_TRUNK )
                ? strScmUrl.replace( ConstanteUtils.CONSTANTE_TRUNK, ConstanteUtils.CONSTANTE_TAGS )
                : strScmUrl;
        return strUrl + ConstanteUtils.CONSTANTE_SEPARATOR_SLASH + strTagName;
    }

    /**
     * Gets the repo url.
     *
     * @param strRepoUrl the str repo url
     * @return the repo url
     */
    public static String getRepoUrl( String strRepoUrl )
    {

        if ( strRepoUrl != null && strRepoUrl.startsWith( "scm:svn:" ) )
        {
            strRepoUrl = strRepoUrl.substring( 8 );

        }

        return strRepoUrl;

    }

    /**
     * Check authentication.
     *
     * @param strUrl the str url
     * @param strUserName the str user name
     * @param strPassword the str password
     * @return true, if successful
     */
    public static boolean checkAuthentication( String strUrl, String strUserName, String strPassword )
    {
        try
        {
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( strUserName, strPassword );

            SVNURL url = SVNURL.parseURIEncoded( strUrl );
            SVNRepository repository = SVNRepositoryFactory.create( url, null );
            repository.setAuthenticationManager( authManager );
            repository.testConnection( );
        }
        catch( SVNException e )
        {
            return false;
        }
        return true;

    }

}
