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
package fr.paris.lutece.plugins.releaser.util.maven;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * MavenUtils
 *
 */
public final class MavenUtils
{
    private static final String TOKEN_RELEASE_VERSION = "$1";
    private static final String TOKEN_TAG = "$2";
    private static final String TOKEN_DEVELOPMENT_VERSION = "$3";
    private static final String TOKEN_USERNAME = "$4";
    private static final String TOKEN_PASSWORD = "$5";
    private static final String RELEASE_PREPARE_BASE = "release:prepare";
    private static final String RELEASE_PREPARE_ARGS = "-DignoreSnapshots=true" + " -DreleaseVersion=" + TOKEN_RELEASE_VERSION + " -Dtag=" + TOKEN_TAG
            + " -DdevelopmentVersion=" + TOKEN_DEVELOPMENT_VERSION + " -DforkMode=never" + " -Dusername=" + TOKEN_USERNAME + " -Dpassword=" + TOKEN_PASSWORD
            + " -Darguments=\"-Dmaven.test.skip=true\" --batch-mode";

    private MavenUtils( )
    {
        // nothing
    }

    /**
     * Builds maven arguments for release:prepare (with release:prepare)
     * 
     * @param strReleaseVersion
     *            release version
     * @param strTag
     *            tag
     * @param strDevelopmentVersion
     *            development version
     * @param strUsername
     *            strUsername
     * @param strPassword
     *            strPassword
     * @return the list
     */
    public static List<String> createReleasePrepare( String strReleaseVersion, String strTag, String strDevelopmentVersion, String strUsername,
            String strPassword )
    {
        String strArguments = RELEASE_PREPARE_ARGS.replace( TOKEN_DEVELOPMENT_VERSION, strDevelopmentVersion );
        strArguments = strArguments.replace( TOKEN_RELEASE_VERSION, strReleaseVersion );
        strArguments = strArguments.replace( TOKEN_TAG, strTag );
        strArguments = strArguments.replace( TOKEN_USERNAME, strUsername );
        strArguments = strArguments.replace( TOKEN_PASSWORD, strPassword );

        List<String> listPrepare = new ArrayList<String>( 2 );
        listPrepare.add( RELEASE_PREPARE_BASE );
        listPrepare.add( strArguments );

        return listPrepare;
    }

}
