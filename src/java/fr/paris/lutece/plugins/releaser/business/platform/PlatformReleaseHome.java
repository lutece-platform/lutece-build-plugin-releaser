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

import java.util.List;

import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

/**
 * This class provides instances management methods (create, find, ...) for PlatformRelease objects
 */
public final class PlatformReleaseHome
{
    // Static variable pointed at the DAO instance
    private static IPlatformReleaseDAO _dao = SpringContextService.getBean( ConstanteUtils.BEAN_PLATFORM_RELEASE_DAO );
    private static Plugin _plugin = PluginService.getPlugin( ConstanteUtils.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private PlatformReleaseHome( )
    {
    }

    /**
     * Create an instance of the platformRelease class
     *
     * @param platformRelease
     *            The instance of the PlatformRelease which contains the informations to store
     * @return The instance of platformRelease which has been created with its primary key.
     */
    public static PlatformRelease create( PlatformRelease platformRelease )
    {
        _dao.insert( platformRelease, _plugin );

        return platformRelease;
    }

    /**
     * Update of the platformRelease which is specified in parameter
     *
     * @param platformRelease
     *            The instance of the PlatformRelease which contains the data to store
     * @return The instance of the platformRelease which has been updated
     */
    public static PlatformRelease update( PlatformRelease platformRelease )
    {
        _dao.store( platformRelease, _plugin );

        return platformRelease;
    }

    /**
     * Remove the platformRelease whose identifier is specified in parameter, with its steps
     *
     * @param nKey
     *            The platformRelease Id
     */
    public static void remove( int nKey )
    {
        PlatformReleaseStepHome.removeByPlatformRelease( nKey );
        _dao.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a platformRelease whose identifier is specified in parameter, without its steps
     *
     * @param nKey
     *            The platformRelease primary key
     * @return an instance of PlatformRelease, or null if not found
     */
    public static PlatformRelease findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Returns an instance of a platformRelease whose identifier is specified in parameter, with its steps loaded
     *
     * @param nKey
     *            The platformRelease primary key
     * @return an instance of PlatformRelease, or null if not found
     */
    public static PlatformRelease findByPrimaryKeyWithSteps( int nKey )
    {
        PlatformRelease platformRelease = _dao.load( nKey, _plugin );

        if ( platformRelease != null )
        {
            platformRelease.setSteps( PlatformReleaseStepHome.findByPlatformRelease( nKey ) );
        }

        return platformRelease;
    }

    /**
     * Returns the campaign with the given name, names being unique
     *
     * @param strName
     *            The campaign name
     * @return an instance of PlatformRelease, or null if not found
     */
    public static PlatformRelease findByName( String strName )
    {
        return _dao.loadByName( strName, _plugin );
    }

    /**
     * Load the data of all the platformRelease objects and returns them as a list, most recent first
     *
     * @return the list which contains the data of all the platformRelease objects
     */
    public static List<PlatformRelease> getPlatformReleasesList( )
    {
        return _dao.selectPlatformReleasesList( _plugin );
    }
}
