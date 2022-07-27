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

import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;

// TODO: Auto-generated Javadoc
/**
 * The Interface IVCSResourceService.
 */
public interface IVCSResourceService
{

    /**
     * Fetch pom.
     *
     * @param site
     *            the site
     * @param strLogin
     *            the str login
     * @param strPassword
     *            the str password
     * @return the string
     */
    String fetchPom( Site site, String strLogin, String strPassword );

    /**
     * Gets the last release.
     *
     * @param site
     *            the site
     * @param strLogin
     *            the str login
     * @param strPassword
     *            the str password
     * @return the last release
     */
    String getLastRelease( Site site, String strLogin, String strPassword );

    /**
     * Do checkout repository.
     *
     * @param context
     *            the context
     * @param strLogin
     *            the str login
     * @param strPassword
     *            the str password
     * @return the string
     */
    String doCheckoutRepository( WorkflowReleaseContext context, String strLogin, String strPassword );

    /**
     * Update develop branch.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     * @param strMessage
     *            the str message
     */
    void updateDevelopBranch( WorkflowReleaseContext context, Locale locale, String strMessage );

    /**
     * Update develop branch.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     * @param strMessage
     *            the str message
     */
    void updateBranch( WorkflowReleaseContext context, String strBranch, Locale locale, String strMessage );

    /**
     * Update master branch.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void updateMasterBranch( WorkflowReleaseContext context, Locale locale );

    /**
     * Rollback release.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void rollbackRelease( WorkflowReleaseContext context, Locale locale );

    /**
     * Checkout develop branch.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void checkoutDevelopBranch( WorkflowReleaseContext context, Locale locale );

    /**
     * Checkout branch.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    void checkoutBranch( WorkflowReleaseContext context, String strBranch, Locale locale );

}
