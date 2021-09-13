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
package fr.paris.lutece.plugins.releaser.service;

import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.workflow.WorkflowService;

// TODO: Auto-generated Javadoc
/**
 * NotificationClientTask.
 */
public class ReleaseComponentTask implements Runnable
{

    /** The Constant WAIT_TIME. */
    private static final long WAIT_TIME = 1000;

    /** The n id workflow. */
    private int _nIdWorkflow;

    /** The wf context. */
    private WorkflowReleaseContext _wfContext;

    /** The request. */
    private HttpServletRequest _request;

    /** The user. */
    private AdminUser _user;

    /** The locale. */
    private Locale _locale;

    /**
     * Instantiates a new release component task.
     *
     * @param nIdWf
     *            the n id wf
     * @param context
     *            the context
     * @param request
     *            the request
     * @param user
     *            the user
     * @param locale
     *            the locale
     */
    public ReleaseComponentTask( int nIdWf, WorkflowReleaseContext context, HttpServletRequest request, AdminUser user, Locale locale )
    {

        _nIdWorkflow = nIdWf;
        _wfContext = context;
        _request = request;
        _user = user;
        _locale = locale;
    }

    /**
     * runWorkflowRelease.
     */
    public void run( )
    {

        State state = WorkflowService.getInstance( ).getState( _wfContext.getId( ), WorkflowReleaseContext.WORKFLOW_RESOURCE_TYPE, _nIdWorkflow,
                ConstanteUtils.CONSTANTE_ID_NULL );
        ReleaserUtils.startCommandResult( _wfContext );

        try
        {

            // Wait all component released before releasing site
            if ( _wfContext.getSite( ) != null )
            {
                while ( !testAllComponentReleased( _wfContext.getSite( ) ) )
                {

                    try
                    {
                        Thread.sleep( WAIT_TIME );
                    }
                    catch( InterruptedException e )
                    {
                        AppLogService.error( e );
                    }
                }

                if ( hasErrorDuringReleaseComponent( _wfContext.getSite( ) ) )
                {
                    ReleaserUtils.addTechnicalError( _wfContext.getCommandResult( ),
                            "The site can not be retrieved because one  of component of the site is in error" );
                }
            }

            Collection<Action> listActions = WorkflowService.getInstance( ).getActions( _wfContext.getId( ), WorkflowReleaseContext.WORKFLOW_RESOURCE_TYPE,
                    _nIdWorkflow, _user );

            for ( Action action : listActions )
            {
                WorkflowService.getInstance( ).doProcessAction( _wfContext.getId( ), WorkflowReleaseContext.WORKFLOW_RESOURCE_TYPE, action.getId( ), -1,
                        _request, _locale, true );
                if ( _wfContext.getComponent( ) != null )
                {

                    // Save in database the release and the next snapshot version
                    ComponentService.getService( ).setLastReleaseVersion( _wfContext.getComponent( ).getArtifactId( ),
                            _wfContext.getComponent( ).getTargetVersion( ) );
                    _wfContext.getComponent( ).setLastAvailableVersion( _wfContext.getComponent( ).getTargetVersion( ) );
                    ComponentService.getService( ).setLastReleaseNextSnapshotVersion( _wfContext.getComponent( ).getArtifactId( ),
                            _wfContext.getComponent( ).getNextSnapshotVersion( ) );
                    _wfContext.getComponent( ).setLastAvailableSnapshotVersion( _wfContext.getComponent( ).getNextSnapshotVersion( ) );

                }

                break;
            }

        }
        catch( AppException appe )
        {
            if ( _wfContext.getComponent( ) != null )
            {

                _wfContext.getComponent( ).setErrorLastRelease( true );
            }
            AppLogService.error( appe );
        }
        finally
        {
            ReleaserUtils.stopCommandResult( _wfContext );
            WorkflowReleaseContextService.getService( ).saveWorkflowReleaseContext( _wfContext );
        }
    }

    /**
     * Test all component released.
     *
     * @param site
     *            the site
     * @return true, if successful
     */
    private boolean testAllComponentReleased( Site site )
    {
        if ( !CollectionUtils.isEmpty( site.getComponents( ) ) )
        {

            for ( Component component : site.getComponents( ) )
            {
                // Test if the release is finished
                if ( !component.isErrorLastRelease( ) && component.shouldBeReleased( ) )
                {
                    return false;

                }
            }
        }

        return true;

    }

    /**
     * Checks for error during release component.
     *
     * @param site
     *            the site
     * @return true, if successful
     */
    private boolean hasErrorDuringReleaseComponent( Site site )
    {
        if ( !CollectionUtils.isEmpty( site.getComponents( ) ) )
        {

            for ( Component component : site.getComponents( ) )
            {
                // Test if the release is finished
                if ( component.isErrorLastRelease( ) )
                {
                    return true;

                }
            }
        }

        return false;

    }

}
