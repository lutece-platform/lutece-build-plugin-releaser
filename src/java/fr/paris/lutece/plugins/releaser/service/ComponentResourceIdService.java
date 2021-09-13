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

import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.util.ReferenceList;

public class ComponentResourceIdService extends ResourceIdService
{
    private static final String PROPERTY_LABEL_RESOURCE_TYPE = "releaser.rbac.component.resourceType";
    private static final String PROPERTY_LABEL_SEARCH = "releaser.rbac.component.permission.search";

    private static final String PLUGIN_NAME = "releaser";

    /** Permission for search Component to release */
    public static final String PERMISSION_SEARCH = "SEARCH";

    /** Creates a new instance of SuggestTypeResourceIdService */
    public ComponentResourceIdService( )
    {
        setPluginName( PLUGIN_NAME );
    }

    @Override
    public void register( )
    {

        ResourceType rt = new ResourceType( );
        rt.setResourceIdServiceClass( ComponentResourceIdService.class.getName( ) );
        rt.setPluginName( PLUGIN_NAME );
        rt.setResourceTypeKey( Component.RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );

        Permission p;

        p = new Permission( );
        p.setPermissionKey( PERMISSION_SEARCH );
        p.setPermissionTitleKey( PROPERTY_LABEL_SEARCH );
        rt.registerPermission( p );

        ResourceTypeManager.registerResourceType( rt );
    }

    @Override
    public ReferenceList getResourceIdList( Locale locale )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTitle( String strId, Locale locale )
    {
        // TODO Auto-generated method stub
        return null;
    }

}
