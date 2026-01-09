/*
 * Copyright (c) 2002-2021, City of Paris
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
package fr.paris.lutece.plugins.releaser.util;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.service.WorkflowReleaseContextService;
import fr.paris.lutece.plugins.releaser.util.github.GitUtils;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

// TODO: Auto-generated Javadoc
/**
 * The Class ReleaserUtils.
 *
 * @author merlinfe
 */
public class ReleaserUtils
{

    /** The Constant REGEX_ID. */
    public static final String REGEX_ID = "^[\\d]+$";

    /**
     * Gets the worklow context data key.
     *
     * @param strArtifactId
     *            the str artifact id
     * @param nContextId
     *            the n context id
     * @return the worklow context data key
     */
    public static String getWorklowContextDataKey( String strArtifactId, int nContextId )
    {
        return ConstanteUtils.CONSTANTE_RELEASE_CONTEXT_PREFIX + strArtifactId + "_" + nContextId;
    }

    /**
     * Gets the last release version data key.
     *
     * @param strArtifactId
     *            the str artifact id
     * @return the last release version data key
     */
    public static String getLastReleaseVersionDataKey( String strArtifactId )
    {
        return ConstanteUtils.CONSTANTE_LAST_RELEASE_VERSION_PREFIX + strArtifactId;
    }

    /**
     * Gets the last release next snapshot version data key.
     *
     * @param strArtifactId
     *            the str artifact id
     * @return the last release next snapshot version data key
     */
    public static String getLastReleaseNextSnapshotVersionDataKey( String strArtifactId )
    {
        return ConstanteUtils.CONSTANTE_LAST_RELEASE_NEXT_SNPASHOT_VERSION_PREFIX + strArtifactId;
    }

    /**
     * Gets the local path.
     *
     * @param context
     *            the context
     * @return the local path
     */
    public static String getLocalPath( WorkflowReleaseContext context )
    {
        String strPath = null;
        if ( context.getSite( ) != null )
        {
            strPath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_SITE_BASE_PAH ) + File.separator + context.getSite( ).getArtifactId( );

        }
        else
        {

            strPath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_COMPONENT_BASE_PAH ) + File.separator
                    + context.getComponent( ).getName( );

        }

        return strPath;

    }

    /**
     * Gets the local pom path.
     *
     * @param context
     *            the context
     * @return the local pom path
     */
    public static String getLocalPomPath( WorkflowReleaseContext context )
    {

        return getLocalPath( context ) + File.separator + ConstanteUtils.CONSTANTE_POM_XML;

    }

    /**
     * Gets the local pom path.
     *
     * @param context
     *            the context
     * @return the local pom path
     */
    public static String getLocalEffectivePomPath( WorkflowReleaseContext context )
    {

        return  getLocalPomPath(context) +".effective";

    }
    


    /**
     * Gets the local site path.
     *
     * @param site
     *            the site
     * @return the local site path
     */
    public static String getLocalSitePath( Site site )
    {
        String strCheckoutBasePath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_SITE_BASE_PAH );

        return strCheckoutBasePath + File.separator + site.getArtifactId( );
    }

    /**
     * Gets the local site pom path.
     *
     * @param site
     *            the site
     * @return the local site pom path
     */
    public static String getLocalSitePomPath( Site site )
    {
        return getLocalSitePath( site ) + File.separator + ConstanteUtils.CONSTANTE_POM_XML;
    }
    //
    // public static String getLocalComponentPath( String strComponentName )
    // {
    // String strLocaleComponentBasePath = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_LOCAL_COMPONENT_BASE_PAH );
    //
    // return strLocaleComponentBasePath + File.separator + strComponentName;
    // }
    //
    // public static String getLocalComponentPomPath( String strComponentName )
    // {
    // return getLocalComponentPath( strComponentName ) + File.separator + ConstanteUtils.CONSTANTE_POM_XML;
    // }

    /**
     * Gets the component name.
     *
     * @param strScmDeveloperConnection
     *            the str scm developer connection
     * @param strArtifactId
     *            the str artifact id
     * @return the component name
     */
    public static String getComponentName( String strScmDeveloperConnection, String strArtifactId )
    {

        String strComponentName = strArtifactId;
        if ( !StringUtils.isEmpty( strScmDeveloperConnection ) && strScmDeveloperConnection.contains( "/" ) && strScmDeveloperConnection.contains( ".git" ) )
        {
            String [ ] tabDevConnection = strScmDeveloperConnection.split( "/" );
            strComponentName = tabDevConnection [tabDevConnection.length - 1].replace( ".git", "" );

        }
        return strComponentName;
    }

    /**
     * Gets the site tag name.
     *
     * @param site
     *            the site
     * @return the site tag name
     */
    public static String getSiteTagName( Site site )
    {

        String strTagName = "";

        if ( site != null )
        {
            strTagName = site.getArtifactId( ) + "-" + site.getNextReleaseVersion( );
        }
        return strTagName;
    }

    /**
     * Adds the info error.
     *
     * @param commandResult
     *            the command result
     * @param strError
     *            the str error
     * @param e
     *            the e
     */
    public static void addInfoError( CommandResult commandResult, String strError, Exception e )
    {

        if ( e != null )
        {
            AppLogService.error( strError, e );
        }
        else
        {
            AppLogService.error( strError );
        }

        if ( commandResult != null )
        {
            commandResult.setError( strError );
            commandResult.setStatus( CommandResult.STATUS_ERROR );
            commandResult.setErrorType( CommandResult.ERROR_TYPE_INFO );
        }

    }

    /**
     * Adds the technical error.
     *
     * @param commandResult
     *            the command result
     * @param strError
     *            the str error
     * @param e
     *            the e
     * @throws AppException
     *             the app exception
     */
    public static void addTechnicalError( CommandResult commandResult, String strError, Exception e ) throws AppException
    {

        if ( e != null )
        {
            AppLogService.error( strError, e );
        }
        else
        {
            AppLogService.error( strError );
        }

        if ( commandResult != null )
        {
            commandResult.setError( strError );
            commandResult.setStatus( CommandResult.STATUS_ERROR );
            commandResult.setRunning( false );
            commandResult.setErrorType( CommandResult.ERROR_TYPE_STOP );
            commandResult.setDateEnd( new Date( ) );
        }
        if ( e != null )
        {
            throw new AppException( strError, e );
        }
        else
        {
            throw new AppException( strError );
        }
    }

    /**
     * Adds the technical error.
     *
     * @param commandResult
     *            the command result
     * @param strError
     *            the str error
     * @throws AppException
     *             the app exception
     */
    public static void addTechnicalError( CommandResult commandResult, String strError ) throws AppException
    {
        addTechnicalError( commandResult, strError, null );
    }

    /**
     * Start command result.
     *
     * @param context
     *            the context
     */
    public static void startCommandResult( WorkflowReleaseContext context )
    {
        CommandResult commandResult = new CommandResult( );
        commandResult.setDateBegin( new Date( ) );
        commandResult.setLog( new StringBuffer( ) );
        commandResult.setRunning( true );
        commandResult.setStatus( CommandResult.STATUS_OK );
        commandResult.setProgressValue( 0 );
        context.setCommandResult( commandResult );
        WorkflowReleaseContextService.getService( )
                .startReleaseInProgress( context.getComponent( ) != null ? context.getComponent( ).getArtifactId( ) : context.getSite( ).getArtifactId( ) );

    }

    /**
     * Log start action.
     *
     * @param context
     *            the context
     * @param strActionName
     *            the str action name
     */
    public static void logStartAction( WorkflowReleaseContext context, String strActionName )
    {

        context.getCommandResult( ).getLog( ).append( "******************Start Action: \"" + strActionName + "\" *******************\n\r" );

    }

    /**
     * Log end action.
     *
     * @param context
     *            the context
     * @param strActionName
     *            the str action name
     */
    public static void logEndAction( WorkflowReleaseContext context, String strActionName )
    {
        context.getCommandResult( ).getLog( ).append( "******************End Action:\"" + strActionName + "\" *******************\n\r" );

    }

    /**
     * Stop command result.
     *
     * @param context
     *            the context
     */
    public static void stopCommandResult( WorkflowReleaseContext context )
    {
        context.getCommandResult( ).setRunning( false );
        context.getCommandResult( ).setDateEnd( new Date( ) );
        context.getCommandResult( ).setProgressValue( 100 );
        WorkflowReleaseContextService.getService( )
                .stopReleaseInProgress( context.getComponent( ) != null ? context.getComponent( ).getArtifactId( ) : context.getSite( ).getArtifactId( ) );

    }

    /**
     * convert a string to int.
     *
     * @param strParameter
     *            the string parameter to convert
     * @return the conversion
     */
    public static int convertStringToInt( String strParameter )
    {
        int nIdParameter = ConstanteUtils.CONSTANTE_ID_NULL;

        try
        {
            if ( ( strParameter != null ) && strParameter.matches( REGEX_ID ) )
            {
                nIdParameter = Integer.parseInt( strParameter );
            }
        }
        catch( NumberFormatException ne )
        {
            AppLogService.error( ne );
        }

        return nIdParameter;
    }

    /**
     * Gets the releaser user.
     *
     * @param request
     *            the request
     * @param locale
     *            the locale
     * @return the releaser user
     */
    public static ReleaserUser getReleaserUser( HttpServletRequest request, Locale locale )
    {

        ReleaserUser releaserUser = null;

        if ( isApplicationAccountEnable( ) )
        {

            releaserUser = new ReleaserUser( );
            releaserUser.addCredential( RepositoryType.GITHUB,
                    releaserUser.new Credential( AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITHUB_RELEASE_ACCOUNT_LOGIN ),
                            ConstanteUtils.PROPERTY_GITHUB_RELEASE_ACCOUNT_PASSWORD ) );
            releaserUser.addCredential( RepositoryType.GITLAB,
                    releaserUser.new Credential( AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITLAB_RELEASE_ACCOUNT_LOGIN ),
                            ConstanteUtils.PROPERTY_GITLAB_RELEASE_ACCOUNT_PASSWORD ) );
            releaserUser.addCredential( RepositoryType.SVN,
                    releaserUser.new Credential( AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_SVN_RELEASE_ACCOUNT_LOGIN ),
                            ConstanteUtils.PROPERTY_GITLAB_RELEASE_ACCOUNT_PASSWORD ) );

        }
        else
        {

            HttpSession session = ( request != null ) ? request.getSession( true ) : null;

            if ( session != null )
            {
                return (ReleaserUser) session.getAttribute( ConstanteUtils.ATTRIBUTE_RELEASER_USER );
            }

        }

        return releaserUser;
    }

    /**
     * Populate releaser user.
     *
     * @param request
     *            the request
     * @param user
     *            the user
     */
    public static void populateReleaserUser( HttpServletRequest request, ReleaserUser user )
    {

        RepositoryType [ ] tabCredentialType = RepositoryType.values( );
        for ( int i = 0; i < tabCredentialType.length; i++ )
        {

            if ( !StringUtils.isEmpty( request.getParameter( tabCredentialType [i] + "_account_login" ) ) )
            {
                user.addCredential( tabCredentialType [i], user.new Credential( request.getParameter( tabCredentialType [i] + "_account_login" ),
                        request.getParameter( tabCredentialType [i] + "_account_password" ) ) );

            }

        }
    }

    /**
     * Sets the releaser user.
     *
     * @param request
     *            the request
     * @param releaserUser
     *            the releaser user
     */
    public static void setReleaserUser( HttpServletRequest request, ReleaserUser releaserUser )
    {

        HttpSession session = ( request != null ) ? request.getSession( true ) : null;

        if ( session != null )
        {
            session.setAttribute( ConstanteUtils.ATTRIBUTE_RELEASER_USER, releaserUser );
        }

    }

    /**
     * Checks if is application account enable.
     *
     * @return true, if is application account enable
     */
    public static boolean isApplicationAccountEnable( )
    {

        return AppPropertiesService.getPropertyBoolean( ConstanteUtils.PROPERTY_APPLICATION_ACCOUNT_ENABLE, false );

    }

    /**
     * Compare version.
     *
     * @param strVersion1
     *            the str version 1
     * @param strVersion2
     *            the str version 2
     * @return the int
     */
    public static int compareVersion( String strVersion1, String strVersion2 )
    {

        if ( strVersion1 != null && strVersion2 != null )
        {
            ComparableVersion cVersion1 = new ComparableVersion( strVersion1 );
            ComparableVersion cVersion2 = new ComparableVersion( strVersion2 );

            return cVersion1.compareTo( cVersion2 );
        }
        return -1;

    }

    public static String cleanPWDInLog( String strLog )
    {

        Pattern pattern2 = Pattern.compile( "(?<prot>https|http):\\/\\/(?<user>\\S+):(?<pwd>\\S+)@" );
        Matcher matcher2 = pattern2.matcher( strLog );

        return matcher2.replaceAll( "${prot}${user}:cleanpwd@" );

    }

    public static String getBranchReleaseFrom( WorkflowReleaseContext context )
    {

        String strBranchReleaseFrom = null;

        if ( context.getSite( ) != null && context.getSite( ).getBranchReleaseFrom( ) != null )
        {
            strBranchReleaseFrom = context.getSite( ).getBranchReleaseFrom( );
        }
        else
            if ( context.getComponent( ) != null && context.getComponent( ).getBranchReleaseFrom( ) != null )
            {
                strBranchReleaseFrom = context.getComponent( ).getBranchReleaseFrom( );
            }
            else
            {
                strBranchReleaseFrom = GitUtils.DEFAULT_RELEASE_BRANCH;
            }

        return strBranchReleaseFrom;
    }

}
