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

import javax.validation.constraints.*;
import org.hibernate.validator.constraints.*;
import fr.paris.lutece.portal.service.rbac.RBACResource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * This is the business class for the object Cluster
 */
public class Cluster implements RBACResource, Serializable
{
    private static final long serialVersionUID = 1L;

    // RBAC management
    public static final String RESOURCE_TYPE = "cluster";

    // Cluster permissions
    public static final String PERMISSION_ADD_CLUSTER = "addClusterPermission";
    public static final String PERMISSION_MODIFY_CLUSTER = "modifyClusterPermission";
    public static final String PERMISSION_DELETE_CLUSTER = "deleteClusterPermission";
    public static final String PERMISSION_ADD_SITES_TO_CLUSTER = "addSitesToClusterPermission";

    private HashMap<String, Boolean> permissions;

    // Variables declarations
    private int _nId;

    @NotEmpty( message = "#i18n{releaser.validation.cluster.Name.notEmpty}" )
    @Size( max = 50, message = "#i18n{releaser.validation.cluster.Name.size}" )
    private String _strName;

    @NotEmpty( message = "#i18n{releaser.validation.cluster.Description.notEmpty}" )
    @Size( max = 255, message = "#i18n{releaser.validation.cluster.Description.size}" )
    private String _strDescription;

    private List<Site> _listSites;

    /**
     * Returns the Id
     * 
     * @return The Id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the Id
     * 
     * @param nId
     *            The Id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the Name
     * 
     * @return The Name
     */
    public String getName( )
    {
        return _strName;
    }

    /**
     * Sets the Name
     * 
     * @param strName
     *            The Name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the Description
     * 
     * @return The Description
     */
    public String getDescription( )
    {
        return _strDescription;
    }

    /**
     * Sets the Description
     * 
     * @param strDescription
     *            The Description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Returns the list of sites
     * 
     * @return The list
     */
    public List<Site> getSites( )
    {
        return _listSites;
    }

    /**
     * Sets the list of sites
     * 
     * @param listSites
     *            The list of site
     */
    public void setSites( List<Site> listSites )
    {
        _listSites = listSites;
    }

    @Override
    public String getResourceTypeCode( )
    {
        return RESOURCE_TYPE;
    }

    @Override
    public String getResourceId( )
    {
        return String.valueOf( _nId );
    }

    public HashMap<String, Boolean> getPermissions( )
    {
        return permissions;
    }

    public void setPermissions( HashMap<String, Boolean> permissions )
    {
        this.permissions = permissions;
    }

}
