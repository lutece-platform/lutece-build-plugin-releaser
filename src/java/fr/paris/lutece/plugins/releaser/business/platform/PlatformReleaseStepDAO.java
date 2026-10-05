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
 * This class provides Data Access methods for PlatformReleaseStep objects
 */
public final class PlatformReleaseStepDAO implements IPlatformReleaseStepDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT_COLUMNS = "SELECT id_step, id_platform_release, step_number, status, plan_json, result_json, jenkins_build_url, jenkins_build_number, jenkins_result, date_begin, date_end FROM releaser_platform_release_step";
    private static final String SQL_QUERY_SELECT = SQL_QUERY_SELECT_COLUMNS + " WHERE id_step = ?";
    private static final String SQL_QUERY_SELECT_BY_PLATFORM_RELEASE = SQL_QUERY_SELECT_COLUMNS + " WHERE id_platform_release = ? ORDER BY step_number";
    private static final String SQL_QUERY_INSERT = "INSERT INTO releaser_platform_release_step ( id_platform_release, step_number, status, plan_json, result_json, jenkins_build_url, jenkins_build_number, jenkins_result, date_begin, date_end ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_UPDATE = "UPDATE releaser_platform_release_step SET id_platform_release = ?, step_number = ?, status = ?, plan_json = ?, result_json = ?, jenkins_build_url = ?, jenkins_build_number = ?, jenkins_result = ?, date_begin = ?, date_end = ? WHERE id_step = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM releaser_platform_release_step WHERE id_step = ?";
    private static final String SQL_QUERY_DELETE_BY_PLATFORM_RELEASE = "DELETE FROM releaser_platform_release_step WHERE id_platform_release = ?";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( PlatformReleaseStep step, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            setCommonParameters( daoUtil, step );
            daoUtil.executeUpdate( );

            if ( daoUtil.nextGeneratedKey( ) )
            {
                step.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( PlatformReleaseStep step, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = setCommonParameters( daoUtil, step );
            daoUtil.setInt( nIndex, step.getId( ) );
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
    public void deleteByPlatformRelease( int nIdPlatformRelease, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_BY_PLATFORM_RELEASE, plugin ) )
        {
            daoUtil.setInt( 1, nIdPlatformRelease );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public PlatformReleaseStep load( int nKey, Plugin plugin )
    {
        PlatformReleaseStep step = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                step = dataToObject( daoUtil );
            }
        }

        return step;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<PlatformReleaseStep> selectByPlatformRelease( int nIdPlatformRelease, Plugin plugin )
    {
        List<PlatformReleaseStep> listSteps = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_PLATFORM_RELEASE, plugin ) )
        {
            daoUtil.setInt( 1, nIdPlatformRelease );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listSteps.add( dataToObject( daoUtil ) );
            }
        }

        return listSteps;
    }

    /**
     * Binds the columns shared by the insert and update statements.
     *
     * @param daoUtil
     *            the statement
     * @param step
     *            the step
     * @return the index of the next parameter
     */
    private static int setCommonParameters( DAOUtil daoUtil, PlatformReleaseStep step )
    {
        int nIndex = 1;
        daoUtil.setInt( nIndex++, step.getIdPlatformRelease( ) );
        daoUtil.setInt( nIndex++, step.getStepNumber( ) );
        daoUtil.setString( nIndex++, step.getStatus( ).name( ) );
        daoUtil.setString( nIndex++, step.getPlanJson( ) );
        daoUtil.setString( nIndex++, step.getResultJson( ) );
        daoUtil.setString( nIndex++, step.getJenkinsBuildUrl( ) );
        daoUtil.setInt( nIndex++, step.getJenkinsBuildNumber( ) );
        daoUtil.setString( nIndex++, step.getJenkinsResult( ) );
        daoUtil.setTimestamp( nIndex++, step.getDateBegin( ) );
        daoUtil.setTimestamp( nIndex++, step.getDateEnd( ) );

        return nIndex;
    }

    /**
     * Builds a step from the current row.
     *
     * @param daoUtil
     *            the result set
     * @return the step
     */
    private static PlatformReleaseStep dataToObject( DAOUtil daoUtil )
    {
        int nIndex = 1;
        PlatformReleaseStep step = new PlatformReleaseStep( );
        step.setId( daoUtil.getInt( nIndex++ ) );
        step.setIdPlatformRelease( daoUtil.getInt( nIndex++ ) );
        step.setStepNumber( daoUtil.getInt( nIndex++ ) );
        step.setStatus( PlatformReleaseStatus.valueOf( daoUtil.getString( nIndex++ ) ) );
        step.setPlanJson( daoUtil.getString( nIndex++ ) );
        step.setResultJson( daoUtil.getString( nIndex++ ) );
        step.setJenkinsBuildUrl( daoUtil.getString( nIndex++ ) );
        step.setJenkinsBuildNumber( daoUtil.getInt( nIndex++ ) );
        step.setJenkinsResult( daoUtil.getString( nIndex++ ) );
        step.setDateBegin( daoUtil.getTimestamp( nIndex++ ) );
        step.setDateEnd( daoUtil.getTimestamp( nIndex++ ) );

        return step;
    }
}
