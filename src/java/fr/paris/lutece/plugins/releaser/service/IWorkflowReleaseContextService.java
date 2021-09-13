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

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.portal.business.user.AdminUser;

// TODO: Auto-generated Javadoc
/**
 * The Interface IWorkflowReleaseContextService.
 */
public interface IWorkflowReleaseContextService
{

    /**
     * Start workflow release context.
     *
     * @param context
     *            the context
     * @param nIdWorkflow
     *            the n id workflow
     * @param locale
     *            the locale
     * @param request
     *            the request
     * @param user
     *            the user
     */
    void startWorkflowReleaseContext( WorkflowReleaseContext context, int nIdWorkflow, Locale locale, HttpServletRequest request, AdminUser user );

    /**
     * Adds the workflow release context.
     *
     * @param context
     *            the context
     * @return the int
     */
    int addWorkflowReleaseContext( WorkflowReleaseContext context );

    /**
     * Save workflow release context.
     *
     * @param context
     *            the context
     */
    void saveWorkflowReleaseContext( WorkflowReleaseContext context );

    /**
     * Gets the workflow release context.
     *
     * @param nIdContext
     *            the n id context
     * @return the workflow release context
     */
    WorkflowReleaseContext getWorkflowReleaseContext( int nIdContext );

    /**
     * Gets the list workflow release context history.
     *
     * @param strArtifactId
     *            the str artifact id
     * @return the list workflow release context history
     */
    List<WorkflowReleaseContext> getListWorkflowReleaseContextHistory( String strArtifactId );

    /**
     * Gets the id workflow.
     *
     * @param context
     *            the context
     * @return the id workflow
     */
    int getIdWorkflow( WorkflowReleaseContext context );

    /**
     * Checkout repository.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void checkoutRepository( WorkflowReleaseContext context, Locale locale );

    /**
     * Merge develop master.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void mergeDevelopMaster( WorkflowReleaseContext context, Locale locale );

    /**
     * Release prepare component.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void releasePrepareComponent( WorkflowReleaseContext context, Locale locale );

    /**
     * Release prepare site.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void releasePrepareSite( WorkflowReleaseContext context, Locale locale );

    /**
     * Release perform component.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void releasePerformComponent( WorkflowReleaseContext context, Locale locale );

    /**
     * Release perform site.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void releasePerformSite( WorkflowReleaseContext context, Locale locale );

    /**
     * Send tweet.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void sendTweet( WorkflowReleaseContext context, Locale locale );

    /**
     * Update jira versions.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void updateJiraVersions( WorkflowReleaseContext context, Locale locale );

    /**
     * Start release in progress.
     *
     * @param strArtifactId
     *            the str artifact id
     */
    void startReleaseInProgress( String strArtifactId );

    /**
     * Stop release in progress.
     *
     * @param strArtifactId
     *            the str artifact id
     */
    void stopReleaseInProgress( String strArtifactId );

    /**
     * Checks if is release in progress.
     *
     * @param strArtifactId
     *            the str artifact id
     * @return true, if is release in progress
     */
    boolean isReleaseInProgress( String strArtifactId );

    /**
     * Inits the.
     */
    void init( );

}
