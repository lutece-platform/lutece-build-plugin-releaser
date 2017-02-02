/*
 * Copyright (c) 2002-2013, Mairie de Paris
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


public class SvnUser
{
    public static final String MARK_CURRENT_THREAD = "current_thread";
    public static final String MARK_AUTHENTIFICATION_MANAGER = "authentification_manager";
    public static final String MARK_OUR_CLIENT_MANAGER = "our_client_manager";
    private String _strLogin;
    private String _strPaswword;
    private Map<String, Object> _mUserContext;

    public SvnUser(  )
    {
        setUserContext( new HashMap<String, Object>(  ) );
    }

    public void setLogin( String strLogin )
    {
        this._strLogin = strLogin;
    }

    public String getLogin(  )
    {
        return _strLogin;
    }

    public void setPassword( String strPaswword )
    {
        this._strPaswword = strPaswword;
    }

    public String getPasword(  )
    {
        return _strPaswword;
    }

    public void setUserContext( Map<String, Object> _mUserContex )
    {
        this._mUserContext = _mUserContex;
    }

    public Map<String, Object> getUserContext(  )
    {
        return _mUserContext;
    }
}
