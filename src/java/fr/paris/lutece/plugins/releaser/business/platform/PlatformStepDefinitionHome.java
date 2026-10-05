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
 * This class provides instances management methods (create, find, ...) for PlatformStepDefinition objects
 */
public final class PlatformStepDefinitionHome
{
    // Static variable pointed at the DAO instance
    private static IPlatformStepDefinitionDAO _dao = SpringContextService.getBean( ConstanteUtils.BEAN_PLATFORM_STEP_DEFINITION_DAO );
    private static Plugin _plugin = PluginService.getPlugin( ConstanteUtils.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private PlatformStepDefinitionHome( )
    {
    }

    /**
     * Create an instance of the definition class
     *
     * @param definition
     *            The instance of the PlatformStepDefinition which contains the informations to store
     * @return The instance of the definition which has been created
     */
    public static PlatformStepDefinition create( PlatformStepDefinition definition )
    {
        _dao.insert( definition, _plugin );

        return definition;
    }

    /**
     * Update of the definition which is specified in parameter
     *
     * @param definition
     *            The instance of the PlatformStepDefinition which contains the data to store
     * @return The instance of the definition which has been updated
     */
    public static PlatformStepDefinition update( PlatformStepDefinition definition )
    {
        _dao.store( definition, _plugin );

        return definition;
    }

    /**
     * Returns the definition of a step
     *
     * @param nStepNumber
     *            The step number
     * @return the definition, or null if not found
     */
    public static PlatformStepDefinition findByStepNumber( int nStepNumber )
    {
        return _dao.load( nStepNumber, _plugin );
    }

    /**
     * Returns the definition of a step
     *
     * @param code
     *            The step code
     * @return the definition, or null if not found
     */
    public static PlatformStepDefinition findByCode( PlatformStepCode code )
    {
        return _dao.load( code.getStepNumber( ), _plugin );
    }

    /**
     * Returns every step definition, ordered by step number
     *
     * @return the definitions
     */
    public static List<PlatformStepDefinition> findAll( )
    {
        return _dao.selectAll( _plugin );
    }
}
