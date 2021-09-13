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
package fr.paris.lutece.plugins.releaser.util.svn;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SvnUser.
 */
public class SvnUser
{

    /** The Constant MARK_CURRENT_THREAD. */
    public static final String MARK_CURRENT_THREAD = "current_thread";

    /** The Constant MARK_AUTHENTIFICATION_MANAGER. */
    public static final String MARK_AUTHENTIFICATION_MANAGER = "authentification_manager";

    /** The Constant MARK_OUR_CLIENT_MANAGER. */
    public static final String MARK_OUR_CLIENT_MANAGER = "our_client_manager";

    /** The str login. */
    private String _strLogin;

    /** The str paswword. */
    private String _strPaswword;

    /** The m user context. */
    private Map<String, Object> _mUserContext;

    /**
     * Instantiates a new svn user.
     */
    public SvnUser( )
    {
        setUserContext( new HashMap<String, Object>( ) );
    }

    /**
     * Sets the login.
     *
     * @param strLogin
     *            the new login
     */
    public void setLogin( String strLogin )
    {
        this._strLogin = strLogin;
    }

    /**
     * Gets the login.
     *
     * @return the login
     */
    public String getLogin( )
    {
        return _strLogin;
    }

    /**
     * Sets the password.
     *
     * @param strPaswword
     *            the new password
     */
    public void setPassword( String strPaswword )
    {
        this._strPaswword = strPaswword;
    }

    /**
     * Gets the pasword.
     *
     * @return the pasword
     */
    public String getPasword( )
    {
        return _strPaswword;
    }

    /**
     * Sets the user context.
     *
     * @param _mUserContex
     *            the m user contex
     */
    public void setUserContext( Map<String, Object> _mUserContex )
    {
        this._mUserContext = _mUserContex;
    }

    /**
     * Gets the user context.
     *
     * @return the user context
     */
    public Map<String, Object> getUserContext( )
    {
        return _mUserContext;
    }
}
