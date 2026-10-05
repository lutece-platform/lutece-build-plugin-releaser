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

import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

/**
 * This class provides Data Access methods for PlatformStepDefinition objects
 */
public final class PlatformStepDefinitionDAO implements IPlatformStepDefinitionDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT_COLUMNS = "SELECT step_number, code, name, scm_url FROM releaser_platform_step_definition";
    private static final String SQL_QUERY_SELECT = SQL_QUERY_SELECT_COLUMNS + " WHERE step_number = ?";
    private static final String SQL_QUERY_SELECT_ALL = SQL_QUERY_SELECT_COLUMNS + " ORDER BY step_number";
    private static final String SQL_QUERY_INSERT = "INSERT INTO releaser_platform_step_definition ( step_number, code, name, scm_url ) VALUES ( ?, ?, ?, ? )";
    private static final String SQL_QUERY_UPDATE = "UPDATE releaser_platform_step_definition SET code = ?, name = ?, scm_url = ? WHERE step_number = ?";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( PlatformStepDefinition definition, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, definition.getStepNumber( ) );
            daoUtil.setString( nIndex++, definition.getCode( ).name( ) );
            daoUtil.setString( nIndex++, definition.getName( ) );
            daoUtil.setString( nIndex++, definition.getScmUrl( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( PlatformStepDefinition definition, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, definition.getCode( ).name( ) );
            daoUtil.setString( nIndex++, definition.getName( ) );
            daoUtil.setString( nIndex++, definition.getScmUrl( ) );
            daoUtil.setInt( nIndex++, definition.getStepNumber( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public PlatformStepDefinition load( int nStepNumber, Plugin plugin )
    {
        PlatformStepDefinition definition = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nStepNumber );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                definition = dataToObject( daoUtil );
            }
        }

        return definition;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<PlatformStepDefinition> selectAll( Plugin plugin )
    {
        List<PlatformStepDefinition> listDefinitions = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listDefinitions.add( dataToObject( daoUtil ) );
            }
        }

        return listDefinitions;
    }

    /**
     * Builds a definition from the current row.
     *
     * @param daoUtil
     *            the result set
     * @return the definition
     */
    private static PlatformStepDefinition dataToObject( DAOUtil daoUtil )
    {
        int nIndex = 1;
        PlatformStepDefinition definition = new PlatformStepDefinition( );
        definition.setStepNumber( daoUtil.getInt( nIndex++ ) );
        definition.setCode( PlatformStepCode.valueOf( daoUtil.getString( nIndex++ ) ) );
        definition.setName( daoUtil.getString( nIndex++ ) );
        definition.setScmUrl( daoUtil.getString( nIndex++ ) );

        return definition;
    }
}
