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
package fr.paris.lutece.plugins.releaser.util;

import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;

// TODO: Auto-generated Javadoc
/**
 * The Class CVSFactoryService.
 */
public class CVSFactoryService
{

    /** The gitlab service. */
    private static IVCSResourceService _gitlabService;

    /** The github service. */
    private static IVCSResourceService _githubService;

    /**
     * Instantiates a new CVS factory service.
     */
    public CVSFactoryService( )
    {

    }

    /**
     * Inits the.
     */
    private static void init( )
    {

        _gitlabService = SpringContextService.getBean( ConstanteUtils.BEAN_GITLAB_RESOURCE_SERVICE );
        _githubService = SpringContextService.getBean( ConstanteUtils.BEAN_GITHUB_RESOURCE_SERVICE );

    }

    /**
     * Gets the service.
     *
     * @param repositoryType
     *            the repository type
     * @return the service
     */
    public static IVCSResourceService getService( RepositoryType repositoryType )
    {

        if ( _gitlabService == null || _githubService == null )
        {
            init( );
        }

        if ( RepositoryType.GITHUB.equals( repositoryType ) )
        {
            return _githubService;
        }
        if ( RepositoryType.GITLAB.equals( repositoryType ) )
        {
            return _gitlabService;
        }

        // Unsupported repository : fail with an explicit error on every action path.
        throw new AppException( "Dépôt non supporté (seuls GitHub et GitLab sont gérés) : type=" + repositoryType );

    }

}
