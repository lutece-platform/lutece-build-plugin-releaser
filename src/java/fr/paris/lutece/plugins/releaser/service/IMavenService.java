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

import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.svn.SvnUser;

// TODO: Auto-generated Javadoc
/**
 * The Interface IMavenService.
 */
public interface IMavenService
{

    /**
     * mvnReleasePrepare.
     *
     * @param strSiteName
     *            the str site name
     * @param strTagName
     *            the str tag name
     * @param strMavenProfile
     *            the str maven profile
     * @param user
     *            the user
     * @param commandResult
     *            the command result
     */
    void mvnSiteAssembly( String strSiteName, String strTagName, String strMavenProfile, SvnUser user, CommandResult commandResult );

    /**
     * Mvn release prepare.
     *
     * @param strPathPom
     *            the str path pom
     * @param strReleaseVersion
     *            the str release version
     * @param strTag
     *            the str tag
     * @param strDevelopmentVersion
     *            the str development version
     * @param strUsername
     *            the str username
     * @param strPassword
     *            the str password
     * @param commandResult
     *            the command result
     * @return the string
     */
    String mvnReleasePrepare( String strPathPom, String strReleaseVersion, String strTag, String strDevelopmentVersion, String strUsername, String strPassword,
            CommandResult commandResult );

    /**
     * Mvn release perform.
     *
     * @param strPathPom
     *            the str path pom
     * @param strUsername
     *            the str username
     * @param strPassword
     *            the str password
     * @param commandResult
     *            the command result
     * @param bPrivateRepository true if the perform must be push in private repository           
     * @return the string
     */
    String mvnReleasePerform( String strPathPom, String strUsername, String strPassword, CommandResult commandResult,boolean bPrivateRepository );

    /**
     * Inits the.
     */
    void init( );
}
