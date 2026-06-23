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

import java.io.IOException;
import java.util.List;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.util.github.GitUtils;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

/**
 * GetRemoteInformationsTask
 *
 */
public class GetRemoteInformationsTask implements Runnable
{

    private Component _component;
    private ReleaserUser _user;

    /**
     * Instantiates a new gets the remote informations task.
     *
     * @param component
     *            the component
     * @param user
     *            the releaser user (used to list the component remote branches)
     */
    public GetRemoteInformationsTask( Component component, ReleaserUser user )
    {

        this._component = component;
        this._user = user;
    }

    @Override
    public void run( )
    {
        try
        {
            ComponentService.getService( ).setRemoteInformations( _component, _component.isProject( ) ? false : true );
        }
        catch( HttpAccessException | IOException e )
        {
            AppLogService.error( e );
        }

        // List the remote branches (ls-remote, no clone) so the branch selection can follow the core line.
        setRemoteBranches( );
    }

    /**
     * Populate the component remote branch list via a lightweight ls-remote (no clone).
     */
    private void setRemoteBranches( )
    {
        RepositoryType repositoryType = _component.getRepoType( );
        if ( _user == null || repositoryType == null || RepositoryType.SVN.equals( repositoryType ) )
        {
            return;
        }
        String strRepoUrl = GitUtils.getRepoUrl( _component.getScmUrl( ) );
        String strLogin = _user.getCredential( repositoryType ).getLogin( );
        String strPassword = _user.getCredential( repositoryType ).getPassword( );

        List<String> branchNameList = GitUtils.lsRemoteBranches( strRepoUrl, strLogin, strPassword );
        if ( !branchNameList.isEmpty( ) )
        {
            _component.setBranches( branchNameList );
        }
    }

}
