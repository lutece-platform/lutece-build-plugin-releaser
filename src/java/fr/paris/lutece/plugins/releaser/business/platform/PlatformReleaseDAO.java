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

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

/**
 * This class provides Data Access methods for PlatformRelease objects
 */
public final class PlatformReleaseDAO implements IPlatformReleaseDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT_COLUMNS = "SELECT id_platform_release, name, release_type, core_major, current_step, status, export_verified, user_name, date_creation, date_update FROM releaser_platform_release";
    private static final String SQL_QUERY_SELECT = SQL_QUERY_SELECT_COLUMNS + " WHERE id_platform_release = ?";
    private static final String SQL_QUERY_SELECT_BY_NAME = SQL_QUERY_SELECT_COLUMNS + " WHERE name = ?";
    private static final String SQL_QUERY_SELECTALL = SQL_QUERY_SELECT_COLUMNS + " ORDER BY id_platform_release DESC";
    private static final String SQL_QUERY_INSERT = "INSERT INTO releaser_platform_release ( name, release_type, core_major, current_step, status, export_verified, user_name, date_creation, date_update ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_UPDATE = "UPDATE releaser_platform_release SET name = ?, release_type = ?, core_major = ?, current_step = ?, status = ?, export_verified = ?, user_name = ?, date_creation = ?, date_update = ? WHERE id_platform_release = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM releaser_platform_release WHERE id_platform_release = ?";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( PlatformRelease platformRelease, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            setCommonParameters( daoUtil, platformRelease );
            daoUtil.executeUpdate( );

            if ( daoUtil.nextGeneratedKey( ) )
            {
                platformRelease.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( PlatformRelease platformRelease, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = setCommonParameters( daoUtil, platformRelease );
            daoUtil.setInt( nIndex, platformRelease.getId( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public PlatformRelease load( int nKey, Plugin plugin )
    {
        PlatformRelease platformRelease = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                platformRelease = dataToObject( daoUtil );
            }
        }

        return platformRelease;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public PlatformRelease loadByName( String strName, Plugin plugin )
    {
        PlatformRelease platformRelease = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_NAME, plugin ) )
        {
            daoUtil.setString( 1, strName );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                platformRelease = dataToObject( daoUtil );
            }
        }

        return platformRelease;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<PlatformRelease> selectPlatformReleasesList( Plugin plugin )
    {
        List<PlatformRelease> listPlatformReleases = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listPlatformReleases.add( dataToObject( daoUtil ) );
            }
        }

        return listPlatformReleases;
    }

    /**
     * Binds the columns shared by the insert and update statements.
     *
     * @param daoUtil
     *            the statement
     * @param platformRelease
     *            the campaign
     * @return the index of the next parameter
     */
    private static int setCommonParameters( DAOUtil daoUtil, PlatformRelease platformRelease )
    {
        int nIndex = 1;
        daoUtil.setString( nIndex++, platformRelease.getName( ) );
        daoUtil.setString( nIndex++, platformRelease.getReleaseType( ).name( ) );
        daoUtil.setInt( nIndex++, platformRelease.getCoreMajor( ) );
        daoUtil.setInt( nIndex++, platformRelease.getCurrentStep( ) );
        daoUtil.setString( nIndex++, platformRelease.getStatus( ).name( ) );
        daoUtil.setBoolean( nIndex++, platformRelease.isExportVerified( ) );
        daoUtil.setString( nIndex++, platformRelease.getUserName( ) );
        daoUtil.setTimestamp( nIndex++, platformRelease.getDateCreation( ) );
        daoUtil.setTimestamp( nIndex++, platformRelease.getDateUpdate( ) );

        return nIndex;
    }

    /**
     * Builds a campaign from the current row.
     *
     * @param daoUtil
     *            the result set
     * @return the campaign
     */
    private static PlatformRelease dataToObject( DAOUtil daoUtil )
    {
        int nIndex = 1;
        PlatformRelease platformRelease = new PlatformRelease( );
        platformRelease.setId( daoUtil.getInt( nIndex++ ) );
        platformRelease.setName( daoUtil.getString( nIndex++ ) );
        platformRelease.setReleaseType( PlatformReleaseType.valueOf( daoUtil.getString( nIndex++ ) ) );
        platformRelease.setCoreMajor( daoUtil.getInt( nIndex++ ) );
        platformRelease.setCurrentStep( daoUtil.getInt( nIndex++ ) );
        platformRelease.setStatus( PlatformReleaseStatus.valueOf( daoUtil.getString( nIndex++ ) ) );
        platformRelease.setExportVerified( daoUtil.getBoolean( nIndex++ ) );
        platformRelease.setUserName( daoUtil.getString( nIndex++ ) );
        platformRelease.setDateCreation( daoUtil.getTimestamp( nIndex++ ) );
        platformRelease.setDateUpdate( daoUtil.getTimestamp( nIndex++ ) );

        return platformRelease;
    }
}
