/*
 * Copyright (c) 2002-2017, Mairie de Paris
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
import java.text.MessageFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.workflow.WorkflowService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

/**
 * ComponentService
 */
public class ComponentService
{
    private static final String PROPERTY_COMPONENT_WEBSERVICE = "releaser.component.webservice.url";
    private static final String URL_COMPONENT_WEBSERVICE = AppPropertiesService.getProperty( PROPERTY_COMPONENT_WEBSERVICE );
    private static final String FIELD_COMPONENT = "component";
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_JIRA_CODE = "jira_code";
    private static final String FIELD_ROADMAP_URL = "jira_roadmap_url";
    private static final String FIELD_CLOSED_ISSUES = "jira_current_version_closed_issues";
    private static final String FIELD_OPENED_ISSUES = "jira_current_version_opened_issues";
    private static final String FIELD_SCM_DEVELOPER_CONNECTION = "scm_developer_connection";
    

    private static ObjectMapper _mapper = new ObjectMapper( );

    public static String getLatestVersion( String strArtifactId ) throws HttpAccessException, IOException
    {
        HttpAccess httpAccess = new HttpAccess( );
        String strInfosJSON;
        String strUrl = MessageFormat.format( URL_COMPONENT_WEBSERVICE, strArtifactId );
        strInfosJSON = httpAccess.doGet( strUrl );
        JsonNode nodeRoot = _mapper.readTree( strInfosJSON );
        JsonNode nodeComponent = nodeRoot.path( FIELD_COMPONENT );
        return nodeComponent.get( FIELD_VERSION ).asText( );
    }

    public static void getJiraInfos( Component component )
    {
        try
        {
            HttpAccess httpAccess = new HttpAccess( );
            String strInfosJSON;
            String strUrl = MessageFormat.format( URL_COMPONENT_WEBSERVICE, component.getArtifactId( ) );
            strInfosJSON = httpAccess.doGet( strUrl );
            JsonNode nodeRoot = _mapper.readTree( strInfosJSON );
            JsonNode nodeComponent = nodeRoot.path( FIELD_COMPONENT );
            component.setJiraCode( nodeComponent.get( FIELD_JIRA_CODE ).asText( ) );
            component.setJiraRoadmapUrl( nodeComponent.get( FIELD_ROADMAP_URL ).asText( ) );
            component.setJiraCurrentVersionOpenedIssues( nodeComponent.get( FIELD_OPENED_ISSUES ).asInt( ) );
            component.setJiraCurrentVersionClosedIssues( nodeComponent.get( FIELD_CLOSED_ISSUES ).asInt( ) );
        }
        catch( HttpAccessException | IOException ex )
        {
            AppLogService.error( "Error getting JIRA infos : " + ex.getMessage( ), ex );
        }

    }
    public static void getScmInfos( Component component )
    {
        try
        {
            HttpAccess httpAccess = new HttpAccess( );
            String strInfosJSON;
            String strUrl = MessageFormat.format( URL_COMPONENT_WEBSERVICE, component.getArtifactId( ) );
            strInfosJSON = httpAccess.doGet( strUrl );
            JsonNode nodeRoot = _mapper.readTree( strInfosJSON );
            JsonNode nodeComponent = nodeRoot.path( FIELD_COMPONENT );
            component.setScmDeveloperConnection( nodeComponent.get( FIELD_SCM_DEVELOPER_CONNECTION ).asText( ) );
         
        }
        catch( HttpAccessException | IOException ex )
        {
            AppLogService.error( "Error getting SCM infos : " + ex.getMessage( ), ex );
        }

    }
    
    public static void release( Component component ,Locale locale)
    {
            
        
        WorkflowReleaseContext context=new WorkflowReleaseContext( );
        context.setComponent( component );
        int nIdWorkflow=WorkflowReleaseContextService.getService( ).getIdWorkflow( context );
        int nIdWorkflowContext=WorkflowReleaseContextService.getService( ).addWorkflowDeploySiteContext( context );
        
        State state = WorkflowService.getInstance(  )
                .getState( nIdWorkflowContext,
                        WorkflowReleaseContext.WORKFLOW_RESOURCE_TYPE, nIdWorkflow,
                        ConstanteUtils.CONSTANTE_ID_NULL );
        ReleaserUtils.startCommandResult( context );
        try
        {
            WorkflowService.getInstance(  ).doProcessAutomaticReflexiveActions(nIdWorkflowContext, WorkflowReleaseContext.WORKFLOW_RESOURCE_TYPE, state.getId( ), -1, locale);
           
        }
        catch( AppException appe )
        {
            AppLogService.error( appe );
        }
        finally
        {
            ReleaserUtils.stopCommandResult( context );
        }
         
    }
    
    
    public static boolean isGitComponent(Component component)
    {
        return !StringUtils.isEmpty( component.getScmDeveloperConnection( ))  && component.getScmDeveloperConnection( ).trim( ).startsWith(ConstanteUtils.CONSTANTE_SUFFIX_GIT);
     }
    
}
