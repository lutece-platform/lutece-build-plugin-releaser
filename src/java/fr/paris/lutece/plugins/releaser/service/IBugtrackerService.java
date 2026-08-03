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

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.util.json.ErrorJsonResponse;

/**
 * IBugtrackerService : bugtracker integration (Redmine implementation) for the release workflow.
 */
public interface IBugtrackerService
{

    /**
     * Inits the service (connection to the bugtracker server).
     */
    void init( );

    /**
     * Updates the bugtracker versions for a released component : creates the next open version,
     * moves the still-open issues of the released version onto it, then closes the released version.
     * If no bugtracker project matches the component repository, the project is created first.
     *
     * @param component
     *            the component being released
     * @param commandResult
     *            the command result (release console)
     */
    void updateComponentVersions( Component component, CommandResult commandResult );

    /**
     * Checks live that the bugtracker server answers (cheap API call).
     *
     * @return true if the bugtracker is reachable
     */
    boolean isReachable( );

    /**
     * Tells whether a projects index (possibly stale) is available for lookups.
     *
     * @return true if an index is available
     */
    boolean hasProjectIndex( );

    /**
     * Finds the identifier of the bugtracker project whose repository URL matches the given SCM URL.
     *
     * @param strScmUrl
     *            the component SCM URL (scm:git:... accepted)
     * @return the project identifier, or null if none matches or no index is available
     */
    String findProjectIdentifier( String strScmUrl );

    /**
     * Populates the bugtracker informations of a component : the roadmap URL when a project matches its
     * repository, or an informative release comment (GitHub only) when no project matches.
     * Silently skipped when the projects index is unavailable (bugtracker down).
     *
     * @param component
     *            the component
     */
    void populateBugtrackerInfo( Component component );

    /**
     * Checks the bugtracker prerequisites before launching a component release : bugtracker reachable, project existing
     * (created on user confirmation). Returns the error response to send back to the user, or null if the release can start.
     *
     * @param component
     *            the component to release
     * @param bForce
     *            true when the user confirmed a previous prompt
     * @param bCreateProject
     *            true when the user confirmed the project creation
     * @param bSkipCreate
     *            true to skip the project creation step (after a failed creation)
     * @param strDescription
     *            the project description entered by the user
     * @param bPublic
     *            whether the project to create is public
     * @return the error response (prompt or warning), or null to proceed
     */
    ErrorJsonResponse prepareReleaseInBugtracker( Component component, boolean bForce, boolean bCreateProject, boolean bSkipCreate, String strDescription,
            boolean bPublic );

}
