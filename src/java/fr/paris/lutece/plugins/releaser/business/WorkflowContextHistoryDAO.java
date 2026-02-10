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

/*
 * Copyright (c) 2002-2020, Mairie de Paris
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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Data Access methods for WorkflowDeployContext objects
 */
public final class WorkflowContextHistoryDAO implements IWorkflowContextHistoryDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_wf_context,  date_begin, date_end, artifact_id,data, user_name FROM releaser_workflow_context_history WHERE id_wf_context = ? ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO releaser_workflow_context_history (  date_begin, date_end, artifact_id,data, user_name ) VALUES ( ?, ?, ?, ?, ?) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM releaser_workflow_context_history WHERE id_wf_context = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE releaser_workflow_context_history SET id_wf_context = ?, date_begin = ?, date_end = ?, artifact_id= ?,  data = ?, user_name = ? WHERE id_wf_context = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_wf_context,  date_begin, date_end, artifact_id ,data, user_name FROM releaser_workflow_context_history";
    private static final String FILTER_BY_ARTIFACT_ID = " WHERE artifact_id= ?";
    private static final String SQL_ORDER_BY_ID=" ORDER BY id_wf_context DESC";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( WorkflowContextHistory workflowContext, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setTimestamp( nIndex++, workflowContext.getDateBegin( ) );
            daoUtil.setTimestamp( nIndex++, workflowContext.getDateEnd( ) );
            daoUtil.setString( nIndex++, workflowContext.getArtifactId( ) );
            daoUtil.setString( nIndex++, workflowContext.getData( ) );
            daoUtil.setString( nIndex++, workflowContext.getUser( ) );

            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                workflowContext.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public WorkflowContextHistory load( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT , plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );
            WorkflowContextHistory workflowContext = null;

            if ( daoUtil.next( ) )
            {
                workflowContext = new WorkflowContextHistory( );
                int nIndex = 1;

                workflowContext.setId( daoUtil.getInt( nIndex++ ) );

                workflowContext.setDateBegin( daoUtil.getTimestamp( nIndex++ ) );
                workflowContext.setDateEnd( daoUtil.getTimestamp( nIndex++ ) );
                workflowContext.setArtifactId( daoUtil.getString( nIndex++ ) );
                workflowContext.setData( daoUtil.getString( nIndex++ ) );
                workflowContext.setUser( daoUtil.getString( nIndex ) );
            }

            daoUtil.free( );
            return workflowContext;
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
            daoUtil.free( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( WorkflowContextHistory workflowContext, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, workflowContext.getId( ) );

            daoUtil.setTimestamp( nIndex++, workflowContext.getDateBegin( ) );
            daoUtil.setTimestamp( nIndex++, workflowContext.getDateEnd( ) );
            daoUtil.setString( nIndex++, workflowContext.getArtifactId( ) );
            daoUtil.setString( nIndex++, workflowContext.getData( ) );
            daoUtil.setString( nIndex++, workflowContext.getUser( ) );
            daoUtil.setInt( nIndex, workflowContext.getId( ) );

            daoUtil.executeUpdate( );
            daoUtil.free( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<WorkflowContextHistory> selectWorkflowDeployContextsList( Plugin plugin )
    {
        List<WorkflowContextHistory> workflowContextList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL +SQL_ORDER_BY_ID, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                WorkflowContextHistory workflowContext = new WorkflowContextHistory( );
                int nIndex = 1;

                workflowContext.setId( daoUtil.getInt( nIndex++ ) );
                workflowContext.setDateBegin( daoUtil.getTimestamp( nIndex++ ) );
                workflowContext.setDateEnd( daoUtil.getTimestamp( nIndex++ ) );
                workflowContext.setArtifactId( daoUtil.getString( nIndex++ ) );
                workflowContext.setData( daoUtil.getString( nIndex++ ) );
                workflowContext.setUser( daoUtil.getString( nIndex ) );

                workflowContextList.add( workflowContext );
            }

            daoUtil.free( );
            return workflowContextList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<WorkflowContextHistory> selectWorkflowDeployContextsListByArtifactId( Plugin plugin, String strArtifactId )
    {
        List<WorkflowContextHistory> workflowContextList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL + FILTER_BY_ARTIFACT_ID + SQL_ORDER_BY_ID, plugin ) )
        {

            daoUtil.setString( 1, strArtifactId );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                WorkflowContextHistory workflowContext = new WorkflowContextHistory( );
                int nIndex = 1;

                workflowContext.setId( daoUtil.getInt( nIndex++ ) );
                workflowContext.setDateBegin( daoUtil.getTimestamp( nIndex++ ) );
                workflowContext.setDateEnd( daoUtil.getTimestamp( nIndex++ ) );
                workflowContext.setArtifactId( daoUtil.getString( nIndex++ ) );
                workflowContext.setData( daoUtil.getString( nIndex++ ) );
                workflowContext.setUser( daoUtil.getString( nIndex ) );

                workflowContextList.add( workflowContext );
            }

            daoUtil.free( );
            return workflowContextList;
        }
    }

}
