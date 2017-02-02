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
package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.svn.ReleaseSvnCheckoutClient;
import fr.paris.lutece.plugins.releaser.util.svn.ReleaseSvnCommitClient;
import fr.paris.lutece.plugins.releaser.util.svn.ReleaseSvnCopyClient;
import fr.paris.lutece.plugins.releaser.util.svn.SvnUtils;
import fr.paris.lutece.plugins.releaser.util.svn.SvnUser;
import fr.paris.lutece.portal.service.util.AppLogService;


public class SvnService implements ISvnService
{
    // private static ISvnService _singleton;
  
    //    public static ISvnService getInstance(  )
    //    {
    //        if ( _singleton == null )
    //        {
    //            _singleton = new SvnService(  );
    //        }
    //
    //        return _singleton;
    //    }
    private SvnService(  )
    {
        init(  );
    }

    /* (non-Javadoc)
         * @see fr.paris.lutece.plugins.deployment.service.ISvnService#init()
         */
    public void init(  )
    {
        /*
             * For using over http:// and https:/
             */
        DAVRepositoryFactory.setup(  );
        /*
         * For using over svn:// and svn+xxx:/
         */
        SVNRepositoryFactoryImpl.setup(  );

        /*
         * For using over file://
         */
        FSRepositoryFactory.setup(  );
    }

  

    public String doSvnCheckoutSite( String strSiteName, String strSvnUrl, SvnUser user, CommandResult commandResult )
    {
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( user.getLogin(  ),
                user.getPasword(  ) );
        String strSiteLocalBasePath = ReleaserUtils.getPathCheckoutSite( strSiteName );

        if ( StringUtils.isNotBlank( strSiteName ) && ( user != null ) )
        {
            ReleaseSvnCheckoutClient updateClient = new ReleaseSvnCheckoutClient( authManager,
                    SVNWCUtil.createDefaultOptions( false ) );

            String strError = null;

            try
            {
                strError = SvnUtils.doSvnCheckoutSite( strSiteName, strSvnUrl, strSiteLocalBasePath, updateClient,
                        commandResult );
            }
            catch ( Exception e )
            {
            	ReleaserUtils.addTechnicalError(commandResult,"errreur lors du checkout du site "+ e.getMessage(),e);
            }
        }
        else
        {
            //        	 ErrorCommandThread thread;
            //         	if ( StringUtils.isBlank( strSiteName) )
            //         	{
            //             	thread = new ErrorCommandThread( AppPropertiesService
            //                        .getProperty( ConstanteUtils.PROPERTY_MESSAGE_CHECKOUT_ERROR_SITE_EMPTY ), ConstanteUtils.CONSTANTE_CHECKOUT_ERROR );
            //         	}
            //         	else
            //         	{
            //             	thread = new ErrorCommandThread( AppPropertiesService
            //                        .getProperty(ConstanteUtils.PROPERTY_MESSAGE_CHECKOUT_ERROR_LOGIN_MDP_EMPTY ), ConstanteUtils.CONSTANTE_CHECKOUT_ERROR );
            //         	}
            //         	
            //         	ThreadUtils.launchThread( thread, user );
            //        	 
        }

        return ConstanteUtils.CONSTANTE_EMPTY_STRING;
    }

    /* (non-Javadoc)
         * @see fr.paris.lutece.plugins.deployment.service.ISvnService#doSvnTagSite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, fr.paris.lutece.plugins.deployment.business.User)
         */
    public String doSvnTagSite( String strSiteName, String strUrlSite, String strTagName, String strNextVersion,
        String strVersion, SvnUser user, CommandResult commandResult )
    {
        String strSiteLocalBasePath = ReleaserUtils.getPathCheckoutSite( strSiteName );

        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( user.getLogin(  ),
                user.getPasword(  ) );

        String strSrcURL = SvnUtils.getSvnUrlTrunkSite( strUrlSite );
        String strDstURL = SvnUtils.getSvnUrlTagSite( strUrlSite, strTagName );

        ReleaseSvnCommitClient commitClient = new ReleaseSvnCommitClient( authManager,
                SVNWCUtil.createDefaultOptions( false ) );
        ReleaseSvnCopyClient copyClient = new ReleaseSvnCopyClient( authManager, SVNWCUtil.createDefaultOptions( false ) );

        try
        {
            final StringBuffer sbLog = commandResult.getLog(  );
            commitClient.setEventHandler( new ISVNEventHandler(  )
                {
                    public void checkCancelled(  ) throws SVNCancelException
                    {
                        // Do nothing
                    }

                    public void handleEvent( SVNEvent event, double progress )
                        throws SVNException
                    {
                        sbLog.append( ( ( event.getAction(  ) == SVNEventAction.COMMIT_ADDED ) ? "Commit "
                                                                                               : event.getAction(  ) ) +
                            " " + ObjectUtils.toString( event.getFile(  ) ) + "\n" );
                    }
                } );

            copyClient.setEventHandler( new ISVNEventHandler(  )
                {
                    public void checkCancelled(  ) throws SVNCancelException
                    {
                        // Do nothing
                    }

                    public void handleEvent( SVNEvent event, double progress )
                        throws SVNException
                    {
                        sbLog.append( ( ( event.getAction(  ) == SVNEventAction.COPY ) ? "Tag " : event.getAction(  ) ) +
                            " " + ObjectUtils.toString( event.getFile(  ) ) + "\n" );
                    }
                } );

            sbLog.append( "Preparing tag\n" );
            sbLog.append( "Updating pom version to " + strVersion + "...\n" );
           // sbLog.append( ReleaserUtils.updateReleaseVersion( strSiteLocalBasePath, strVersion,
            //        "[site-release] Prepare tag for " + strSiteName, commitClient ) );
            sbLog.append( "Pom updated\n" );

            sbLog.append( "Tagging site to " + strTagName + "...\n" );
            
            
            String strErrorDuringTag=SvnUtils.doTagSite( strSiteName, strTagName, strSrcURL, strDstURL, copyClient );
           
            if(StringUtils.isEmpty(strErrorDuringTag))
            {
	            sbLog.append( "Tag done\n" );
	
	            sbLog.append( "Updating pom to next development " + strNextVersion + "\n" );
	           // sbLog.append( ReleaserUtils.updateReleaseVersion( strSiteLocalBasePath, strNextVersion,
	            //        "[site-release] Update pom version for " + strSiteName, commitClient ) );
	            sbLog.append( "Pom updated\n" );
            }
            else
            {
                ReleaserUtils.addTechnicalError(commandResult, strErrorDuringTag);
            }
            
        }
        catch ( Exception e )
        {
            //commandResult.setStatus( ICommandThread.STATUS_EXCEPTION );
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
            	AppLogService.error(e1);
            }

            commandResult.getLog(  ).append( errorLog );

            /**
            _result.setIdError( ReleaseLogger.logError(
                                                        _result.getLog(  ).toString(  ),
                                                        e ) );
                                                        **/
            ReleaserUtils.addTechnicalError(commandResult, errorLog,e);
        }

        return ConstanteUtils.CONSTANTE_EMPTY_STRING;
    }

	
}
