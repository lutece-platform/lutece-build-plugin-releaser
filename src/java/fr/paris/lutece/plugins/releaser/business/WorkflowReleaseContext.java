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
package fr.paris.lutece.plugins.releaser.business;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import fr.paris.lutece.plugins.releaser.util.CommandResult;

// TODO: Auto-generated Javadoc
/**
 * WorkflowReleaseContext.
 */

@JsonIgnoreProperties( {
        "releaserUser"
} )
public class WorkflowReleaseContext implements Serializable
{

    /** The Constant WORKFLOW_RESOURCE_TYPE. */
    public static final String WORKFLOW_RESOURCE_TYPE = "WORKFLOW_RELEASE_CONTEXT";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8956577881980599537L;

    /** The n id. */
    private int _nId;

    /** The component. */
    private Component _component;

    /** The site. */
    private Site _site;

    /** The command result. */
    private CommandResult _commandResult;

    /** The releaser user. */
    @JsonIgnore
    private ReleaserUser _releaserUser;

    /** The str ref branch release. */
    private String _strRefBranchRelease;

    /** The str ref branch dev. */
    private String _strRefBranchDev;

    /**
     * Gets the id.
     *
     * @return get Id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the id.
     *
     * @param _nId
     *            the new id
     */
    public void setId( int _nId )
    {
        this._nId = _nId;
    }

    /**
     * Gets the component.
     *
     * @return componet
     */
    public Component getComponent( )
    {
        return _component;
    }

    /**
     * Sets the component.
     *
     * @param _component
     *            the new component
     */
    public void setComponent( Component _component )
    {
        this._component = _component;
    }

    /**
     * Gets the command result.
     *
     * @return command result
     */
    public CommandResult getCommandResult( )
    {
        return _commandResult;
    }

    /**
     * Sets the command result.
     *
     * @param _commandResult
     *            command result
     */
    public void setCommandResult( CommandResult _commandResult )
    {
        this._commandResult = _commandResult;
    }

    /**
     * Sets the site.
     *
     * @param site
     *            the new site
     */
    public void setSite( Site site )
    {
        _site = site;
    }

    /**
     * Gets the site.
     *
     * @return get Site
     */
    public Site getSite( )
    {
        return _site;
    }

    /**
     * Checks if is lutece site.
     *
     * @return true if the component is a lutece site
     */
    public boolean isLuteceSite( )
    {
        return _component == null && _site != null;
    }

    /**
     * Gets the releaser user.
     *
     * @return getReleaserUser
     */
    @JsonIgnore
    public ReleaserUser getReleaserUser( )
    {
        return _releaserUser;
    }

    /**
     * Sets the releaser user.
     *
     * @param _releaserUser
     *            the new releaser user
     */
    @JsonIgnore
    public void setReleaserUser( ReleaserUser _releaserUser )
    {
        this._releaserUser = _releaserUser;
    }

    /**
     * Gets the ref branch release.
     *
     * @return getRefBranchRelease
     */
    public String getRefBranchRelease( )
    {
        return _strRefBranchRelease;
    }

    /**
     * Sets the ref branch release.
     *
     * @param _strRefBranchRelease
     *            the new ref branch release
     */
    public void setRefBranchRelease( String _strRefBranchRelease )
    {
        this._strRefBranchRelease = _strRefBranchRelease;
    }

    /**
     * Gets the ref branch dev.
     *
     * @return getRefBranchDev
     */
    public String getRefBranchDev( )
    {
        return _strRefBranchDev;
    }

    /**
     * Sets the ref branch dev.
     *
     * @param _strRefBranchDev
     *            the new ref branch dev
     */
    public void setRefBranchDev( String _strRefBranchDev )
    {
        this._strRefBranchDev = _strRefBranchDev;
    }

    /**
     * Gets the releaser resource.
     *
     * @return getReleaserResource
     */
    public IReleaserResource getReleaserResource( )
    {

        if ( this.getSite( ) != null )
        {
            return this.getSite( );

        }
        return this.getComponent( );
    }

}
