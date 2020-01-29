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
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.VersionInput;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

// TODO: Auto-generated Javadoc
/**
 * JIRA Service name.
 */
public class JiraComponentService implements IJiraService
{
    
    /** The Constant PROPERTY_JIRA_SEARCH_SERVICE. */
    private static final String PROPERTY_JIRA_SEARCH_SERVICE = "releaser.component.jiraSearchQuery";
    
    /** The Constant PROPERTY_JIRA_USER. */
    private static final String PROPERTY_JIRA_USER = "lutecetools.jira.user";
    
    /** The Constant PROPERTY_JIRA_USER_PWD. */
    private static final String PROPERTY_JIRA_USER_PWD = "lutecetools.jira.pwd";

    /** The url jira server. */
    private static String URL_JIRA_SERVER;
    
    /** The jira user. */
    private static String JIRA_USER;
    
    /** The jira user pwd. */
    private static String JIRA_USER_PWD;

    /** The Constant CONSTANTE_SNAPSHOT_VERSION. */
    private static final String CONSTANTE_SNAPSHOT_VERSION = "-SNAPSHOT";
    
    /** The factory. */
    private static AsynchronousJiraRestClientFactory _factory;
    
    /** The auth. */
    private static BasicHttpAuthenticationHandler _auth;
    
    /** The instance. */
    private static IJiraService _instance;

    /**
     * Constructor.
     */
    public JiraComponentService( )
    {

    }

    /**
     * Gets the service.
     *
     * @return the service
     */
    public static IJiraService getService( )
    {
        if ( _instance == null )
        {
            _instance = SpringContextService.getBean( ConstanteUtils.BEAN_JIRA_SERVICE );
            _instance.init( );
        }

        return _instance;

    }

    /**
     * Inits the.
     */
    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.plugins.releaser.service.IJiraService#init()
     */
    @Override
    public void init( )
    {
        String strProxyHost = AppPropertiesService.getProperty( "httpAccess.proxyHost" );
        String strProxyPort = AppPropertiesService.getProperty( "httpAccess.proxyPort" );
        String strProxyUserName = AppPropertiesService.getProperty( "httpAccess.proxyUserName" );
        String strProxyPassword = AppPropertiesService.getProperty( "httpAccess.proxyPassword" );

        if ( !StringUtils.isEmpty( strProxyHost ) )
        {

            System.getProperties( ).put( "http.proxyHost", strProxyHost );
            System.getProperties( ).put( "http.proxyPort", strProxyPort );
            System.getProperties( ).put( "https.proxyHost", strProxyHost );
            System.getProperties( ).put( "https.proxyPort", strProxyPort );
            System.getProperties( ).put( "https.proxyUser", strProxyUserName );
            System.getProperties( ).put( "https.proxyPassword", strProxyPassword );
            System.getProperties( ).put( "https.proxySet", "true" );
            AppLogService.info( "LuteceTools : Using httpaccess.properties defined proxy to connect to JIRA." );
        }

        URL_JIRA_SERVER = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_URL_JIRA_SERVICE, "https://dev.lutece.paris.fr/jira/" );
        JIRA_USER = AppPropertiesService.getProperty( PROPERTY_JIRA_USER );
        JIRA_USER_PWD = AppPropertiesService.getProperty( PROPERTY_JIRA_USER_PWD );

        _factory = new AsynchronousJiraRestClientFactory( );
        _auth = new BasicHttpAuthenticationHandler( JIRA_USER, JIRA_USER_PWD );

    }

    /**
     * Update component versions.
     *
     * @param component the component
     * @param commandResult the command result
     */
    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.plugins.releaser.service.IJiraService#updateComponentVersions(fr.paris.lutece.plugins.releaser.business.Component,
     * fr.paris.lutece.plugins.releaser.util.CommandResult)
     */
    @Override
    public synchronized void updateComponentVersions( Component component, CommandResult commandResult )
    {
        JiraRestClient client = null;
        String strJiraKey = component.getJiraCode( );
        VersionRestClient clientVersion = null;

        if ( strJiraKey != null )
        {
            try
            {

                String strJiraCurrentVersion = component.getCurrentVersion( ).replace( CONSTANTE_SNAPSHOT_VERSION, "" );
                String strJiraReleaseVersionName = component.getTargetVersion( );
                String strJiraNewVersion = component.getNextSnapshotVersion( ).replace( CONSTANTE_SNAPSHOT_VERSION, "" );

                client = _factory.create( new URI( URL_JIRA_SERVER ), _auth );
                Project project = client.getProjectClient( ).getProject( strJiraKey ).claim( );
                // create new version
                clientVersion = client.getVersionRestClient( );
                if ( !StringUtils.isEmpty( strJiraNewVersion ) )
                {
                    VersionInput newJiraVersion = new VersionInput( component.getJiraCode( ), strJiraNewVersion, null, null, false, false );
                    clientVersion.createVersion( newJiraVersion ).claim( );

                }

                // Release current Version
                for ( Version version : project.getVersions( ) )
                {
                    if ( !version.isReleased( ) && version.getName( ).equals( strJiraCurrentVersion ) )
                    {

                        // Move Issues before released
                        String strSearch = MessageFormat.format(
                                AppPropertiesService.getProperty( PROPERTY_JIRA_SEARCH_SERVICE,
                                        "project = {0} AND fixVersion in ({1}) AND status in (Open, \"In Progress\", \"To Do\")" ),
                                component.getJiraCode( ), strJiraCurrentVersion );
                        Promise<SearchResult> searchJqlPromise = client.getSearchClient( ).searchJql( strSearch );

                        for ( Issue issue : searchJqlPromise.claim( ).getIssues( ) )
                        {
                            if ( issue.getProject( ).getKey( ).equals( component.getJiraCode( ) ) )
                            {
                                // final FieldInput fieldInput = new FieldInput("fixVersions", tabVersion);
                                List<ComplexIssueInputFieldValue> fieldList = new ArrayList<ComplexIssueInputFieldValue>( );
                                Map<String, Object> mapValues = new HashMap<String, Object>( );
                                mapValues.put( "name", strJiraNewVersion );
                                ComplexIssueInputFieldValue fieldValue = new ComplexIssueInputFieldValue( mapValues );
                                fieldList.add( fieldValue );
                                client.getIssueClient( )
                                        .updateIssue( issue.getKey( ), new IssueInputBuilder( ).setFieldValue( "fixVersions", fieldList ).build( ) ).claim( );

                            }

                        }

                        // release version
                        VersionInput updateVersion = new VersionInput( project.getKey( ), strJiraReleaseVersionName, version.getDescription( ), new DateTime( ),
                                version.isArchived( ), true );

                        clientVersion.updateVersion( version.getSelf( ), updateVersion ).claim( );

                        break;

                    }

                }
            }
            catch( RestClientException ex )
            {

                ReleaserUtils.addInfoError( commandResult, "Error updating Jira version", ex );
            }

            catch( Exception ex )
            {
                ReleaserUtils.addInfoError( commandResult, commandResult.getLog( ).toString( ), ex );
            }
            finally
            {
                if ( client != null )
                {
                    try
                    {
                        client.close( );

                    }
                    catch( IOException ex )
                    {
                        ReleaserUtils.addInfoError( commandResult, "Error using Jira Client API : " + ex.getMessage( ), ex );
                    }
                }
            }
        }
        else
        {
            ReleaserUtils.addInfoError( commandResult, "Can not update jira version, No Jira key is define for the component", null );
        }

    }

}
