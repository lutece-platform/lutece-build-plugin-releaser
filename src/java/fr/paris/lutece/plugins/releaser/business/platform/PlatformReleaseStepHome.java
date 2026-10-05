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
 * This class provides instances management methods (create, find, ...) for PlatformReleaseStep objects
 */
public final class PlatformReleaseStepHome
{
    // Static variable pointed at the DAO instance
    private static IPlatformReleaseStepDAO _dao = SpringContextService.getBean( ConstanteUtils.BEAN_PLATFORM_RELEASE_STEP_DAO );
    private static Plugin _plugin = PluginService.getPlugin( ConstanteUtils.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private PlatformReleaseStepHome( )
    {
    }

    /**
     * Create an instance of the step class
     *
     * @param step
     *            The instance of the PlatformReleaseStep which contains the informations to store
     * @return The instance of step which has been created with its primary key.
     */
    public static PlatformReleaseStep create( PlatformReleaseStep step )
    {
        _dao.insert( step, _plugin );

        return step;
    }

    /**
     * Update of the step which is specified in parameter
     *
     * @param step
     *            The instance of the PlatformReleaseStep which contains the data to store
     * @return The instance of the step which has been updated
     */
    public static PlatformReleaseStep update( PlatformReleaseStep step )
    {
        _dao.store( step, _plugin );

        return step;
    }

    /**
     * Remove the step whose identifier is specified in parameter
     *
     * @param nKey
     *            The step Id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Remove every step of a campaign
     *
     * @param nIdPlatformRelease
     *            The campaign Id
     */
    public static void removeByPlatformRelease( int nIdPlatformRelease )
    {
        _dao.deleteByPlatformRelease( nIdPlatformRelease, _plugin );
    }

    /**
     * Returns an instance of a step whose identifier is specified in parameter
     *
     * @param nKey
     *            The step primary key
     * @return an instance of PlatformReleaseStep, or null if not found
     */
    public static PlatformReleaseStep findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Returns the steps of a campaign, ordered by step number
     *
     * @param nIdPlatformRelease
     *            The campaign Id
     * @return the steps
     */
    public static List<PlatformReleaseStep> findByPlatformRelease( int nIdPlatformRelease )
    {
        return _dao.selectByPlatformRelease( nIdPlatformRelease, _plugin );
    }
}
