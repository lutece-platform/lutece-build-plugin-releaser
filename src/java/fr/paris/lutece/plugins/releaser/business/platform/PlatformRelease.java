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
package fr.paris.lutece.plugins.releaser.business.platform;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import fr.paris.lutece.portal.service.rbac.RBACResource;

/**
 * A platform release campaign : the release of the Lutece platform for one line (7 or 8) and one release type, driven step by step (global-pom,
 * site-pom, core, platform plugins, platform starters).
 */
public class PlatformRelease implements RBACResource, Serializable
{
    /** The RBAC resource type. */
    public static final String RESOURCE_TYPE = "platform_release";

    /** The RBAC permission : manage the campaigns (create, prepare and launch steps, export). */
    public static final String PERMISSION_MANAGE = "managePlatformReleasePermission";

    private static final long serialVersionUID = 1L;

    /** The id. */
    private int _nId;

    /** The name. */
    @NotEmpty( message = "#i18n{releaser.validation.platformRelease.name.notEmpty}" )
    @Size( max = 255, message = "#i18n{releaser.validation.platformRelease.name.size}" )
    private String _strName;

    /** The release type, chosen explicitly at creation. */
    private PlatformReleaseType _releaseType;

    /** The core version (major of lutece-core : 7 or 8). */
    private int _nCoreMajor;

    /** The current step number (1..5). */
    private int _nCurrentStep = 1;

    /** The status of the campaign. */
    private PlatformReleaseStatus _status = PlatformReleaseStatus.READY;

    /** Whether the export of the plan has been verified by a human, which unlocks the last step. */
    private boolean _bExportVerified;

    /** The name of the admin user who created the campaign. */
    private String _strUserName;

    /** The creation date. */
    private Timestamp _dateCreation;

    /** The last update date. */
    private Timestamp _dateUpdate;

    /** The steps, loaded on demand. */
    private List<PlatformReleaseStep> _listSteps = new ArrayList<>( );

    /**
     * Returns the id.
     *
     * @return the id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the id.
     *
     * @param nId
     *            the id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName( )
    {
        return _strName;
    }

    /**
     * Sets the name.
     *
     * @param strName
     *            the name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the release type.
     *
     * @return the release type
     */
    public PlatformReleaseType getReleaseType( )
    {
        return _releaseType;
    }

    /**
     * Sets the release type.
     *
     * @param releaseType
     *            the release type
     */
    public void setReleaseType( PlatformReleaseType releaseType )
    {
        _releaseType = releaseType;
    }

    /**
     * Returns the core version.
     *
     * @return the major of lutece-core (7 or 8)
     */
    public int getCoreMajor( )
    {
        return _nCoreMajor;
    }

    /**
     * Sets the core version.
     *
     * @param nCoreMajor
     *            the major of lutece-core (7 or 8)
     */
    public void setCoreMajor( int nCoreMajor )
    {
        _nCoreMajor = nCoreMajor;
    }

    /**
     * Returns the current step number.
     *
     * @return the current step number
     */
    public int getCurrentStep( )
    {
        return _nCurrentStep;
    }

    /**
     * Sets the current step number.
     *
     * @param nCurrentStep
     *            the current step number
     */
    public void setCurrentStep( int nCurrentStep )
    {
        _nCurrentStep = nCurrentStep;
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public PlatformReleaseStatus getStatus( )
    {
        return _status;
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the status
     */
    public void setStatus( PlatformReleaseStatus status )
    {
        _status = status;
    }

    /**
     * Tells whether the export of the plan has been verified.
     *
     * @return true if verified
     */
    public boolean isExportVerified( )
    {
        return _bExportVerified;
    }

    /**
     * Sets whether the export of the plan has been verified.
     *
     * @param bExportVerified
     *            true if verified
     */
    public void setExportVerified( boolean bExportVerified )
    {
        _bExportVerified = bExportVerified;
    }

    /**
     * Returns the name of the user who created the campaign.
     *
     * @return the user name
     */
    public String getUserName( )
    {
        return _strUserName;
    }

    /**
     * Sets the name of the user who created the campaign.
     *
     * @param strUserName
     *            the user name
     */
    public void setUserName( String strUserName )
    {
        _strUserName = strUserName;
    }

    /**
     * Returns the creation date.
     *
     * @return the creation date
     */
    public Timestamp getDateCreation( )
    {
        return _dateCreation;
    }

    /**
     * Sets the creation date.
     *
     * @param dateCreation
     *            the creation date
     */
    public void setDateCreation( Timestamp dateCreation )
    {
        _dateCreation = dateCreation;
    }

    /**
     * Returns the last update date.
     *
     * @return the last update date
     */
    public Timestamp getDateUpdate( )
    {
        return _dateUpdate;
    }

    /**
     * Sets the last update date.
     *
     * @param dateUpdate
     *            the last update date
     */
    public void setDateUpdate( Timestamp dateUpdate )
    {
        _dateUpdate = dateUpdate;
    }

    /**
     * Returns the steps.
     *
     * @return the steps, ordered by step number (empty until loaded)
     */
    public List<PlatformReleaseStep> getSteps( )
    {
        return _listSteps;
    }

    /**
     * Sets the steps.
     *
     * @param listSteps
     *            the steps
     */
    public void setSteps( List<PlatformReleaseStep> listSteps )
    {
        _listSteps = listSteps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceTypeCode( )
    {
        return RESOURCE_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceId( )
    {
        return Integer.toString( _nId );
    }
}
