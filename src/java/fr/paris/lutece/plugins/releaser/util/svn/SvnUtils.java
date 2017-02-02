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

import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNRevision;

import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.file.FileUtils;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;



public final class SvnUtils
{
    private static final String MESSAGE_ERROR_SVN = "Impossible de se connecter au SVN. Veuillez verifier vos identifiants";
    private static final String CONSTANTE_SLASH = "/";
    private static final Logger logger = Logger.getLogger( SvnUtils.class );

    /**
     * Constructeur vide
     */
    private SvnUtils(  )
    {
        // nothing
    }

    /**
     * Initialise les diff�rentes factory pour le svn
     */
    public static void init(  )
    {
        /*
        * For using over http:// and https:
        */
        DAVRepositoryFactory.setup(  );
        /*
         * For using over svn:// and svn+xxx:
         */
        SVNRepositoryFactoryImpl.setup(  );

        /*
         * For using over file:/
         */
        FSRepositoryFactory.setup(  );
    }

    /**
     * Tag un site
     * @param strSiteName le nom du site
     * @param strTagName le nom du tag
     * @param copyClient le client svn permettant la copie
     * @throws SVNException
     */
    public static String doTagSite( String strSiteName, String strTagName, String strSrcURL, String strDstURL,
        SVNCopyClient copyClient ) throws SVNException
    {
        // COPY from trunk to tags/tagName
        SVNURL srcURL = SVNURL.parseURIEncoded( strSrcURL );
        SVNURL dstURL = SVNURL.parseURIEncoded( strDstURL );
        SVNCopySource svnCopySource = new SVNCopySource( SVNRevision.HEAD, SVNRevision.HEAD, srcURL );
        SVNCopySource[] tabSVNCopy = new SVNCopySource[1];
        tabSVNCopy[0] = svnCopySource;

        SVNCommitInfo info = copyClient.doCopy( tabSVNCopy, dstURL, false, false, false,
                "[site-release] Tag site " + strSiteName + " to " + strTagName, null );

        if ( info.getErrorMessage(  ) != null )
        {
        	
            return info.getErrorMessage(  ).getMessage(  );
        }

        return null;
    }

    public static String doSvnCheckoutSite( String strSiteName, String strUrl, String strCheckoutBaseSitePath,
        ReleaseSvnCheckoutClient updateClient, CommandResult result )
        throws SVNException
    {
        SVNURL url = SVNURL.parseURIEncoded( strUrl );
        File file = new File( strCheckoutBaseSitePath );

        if ( file.exists(  ) )
        {
            if ( !FileUtils.delete( file, result.getLog(  ) ) )
            {
                result.setError( result.getLog(  ).toString(  ) );

                return result.getLog(  ).toString(  );
            }
        }

        SVNRepository repository = SVNRepositoryFactory.create( url, null );
        final StringBuffer logBuffer = result.getLog(  );

        try
        {
            updateClient.setEventHandler( new ISVNEventHandler(  )
                {
                    public void checkCancelled(  ) throws SVNCancelException
                    {
                        // Do nothing
                    }

                    public void handleEvent( SVNEvent event, double progress )
                        throws SVNException
                    {
                        logBuffer.append( ( ( event.getAction(  ) == SVNEventAction.UPDATE_ADD ) ? "ADDED "
                                                                                                 : event.getAction(  ) ) +
                            " " + event.getFile(  ) + "\n" );
                    }
                } );

            // SVNDepth.INFINITY + dernier param�tre � FALSE pour la version 1.3.2
            updateClient.doCheckout( repository.getLocation(  ), file, SVNRevision.HEAD, SVNRevision.HEAD, true );
        }
        catch ( SVNAuthenticationException e )
        {
            //            _result.getLog(  ).append( CONSTANTE_NO_LOGIN_PASSWORD );
            //            _result.setStatus( ICommandThread.STATUS_EXCEPTION );
            //            _result.setRunning( false );


        	ReleaserUtils.addTechnicalError(result,"Une erreur est survenue lors de la tentative d'authentification avec le svn"+e,e);
		      
            StringWriter sw = new StringWriter(  );
            PrintWriter pw = new PrintWriter( sw );
            e.printStackTrace( pw );

            String errorLog = sw.toString(  );
            pw.flush(  );
            pw.close(  );

            try
            {
                sw.flush(  );
                sw.close(  );
            }
            catch ( IOException e1 )
            {
                // do nothing
                //  _logger.error( e1 );
            }

            //            _result.setLog( _result.getLog(  ).append( errorLog ) );
            //            _logger.error( e );

            //_result.setIdError( ReleaseLogger.logError( _result.getLog(  ).toString(  ), e ) );
        }
        catch ( Exception e )
        {
            //            _result.setStatus( ICommandThread.STATUS_EXCEPTION );
            //            _result.setRunning( false );
            StringWriter sw = new StringWriter(  );
            PrintWriter pw = new PrintWriter( sw );
            e.printStackTrace( pw );

            String errorLog = sw.toString(  );
            pw.flush(  );
            pw.close(  );

            try
            {
                sw.flush(  );
                sw.close(  );
            }
            catch ( IOException e1 )
            {
                // do nothing
                //                _logger.error( e1 );
            }

        	ReleaserUtils.addTechnicalError(result,"Une erreur svn est survenue:"+e,e);
   		 
        }

        return null;
    }

    public static ReferenceList getSvnSites( String strUrlSite, SVNClientManager clientManager )
        throws SVNException
    {
        final ReferenceList listSites = new ReferenceList(  );
        final SVNURL url;

        url = SVNURL.parseURIEncoded( strUrlSite );

        SVNRepository repository = SVNRepositoryFactory.create( url, null );

        clientManager.getLogClient(  ).doList( repository.getLocation(  ), SVNRevision.HEAD, SVNRevision.HEAD, false,
            false,
            new ISVNDirEntryHandler(  )
            {
                public void handleDirEntry( SVNDirEntry entry )
                    throws SVNException
                {
                  
                    if ( !url.equals( entry.getURL(  ) ) )
                    {
                        if ( entry.getKind(  ) == SVNNodeKind.DIR )
                        {
                            listSites.addItem( entry.getName(  ), entry.getName(  ) );
                        }
                    }
                }
            } );

        return listSites;
    }

    public static String getSvnUrlTrunkSite( String strUrlSite )
    {
        return strUrlSite + ConstanteUtils.CONSTANTE_SEPARATOR_SLASH + ConstanteUtils.CONSTANTE_TRUNK;
    }

    public static String getSvnUrlTagSite( String strUrlSite, String strTagName )
    {
        return strUrlSite + ConstanteUtils.CONSTANTE_SEPARATOR_SLASH + ConstanteUtils.CONSTANTE_TAGS +
        ConstanteUtils.CONSTANTE_SEPARATOR_SLASH + strTagName;
    }

   
}
