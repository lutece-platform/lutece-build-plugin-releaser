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
package fr.paris.lutece.plugins.releaser.business;

import java.io.Serializable;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

// TODO: Auto-generated Javadoc
/**
 * Credentials used by releaser.
 */
@JsonAutoDetect( fieldVisibility = Visibility.ANY )
public class ReleaserUser implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5957364434700297424L;

    /** The map credential. */
    private HashMap<RepositoryType, Credential> _mapCredential;

    /**
     * Instantiates a new releaser user.
     */
    public ReleaserUser( )
    {

        this._mapCredential = new HashMap<>( );
    }

    /**
     * Adds the credential.
     *
     * @param type
     *            the type
     * @param credential
     *            the credential
     */
    public void addCredential( RepositoryType type, Credential credential )
    {

        _mapCredential.put( type, credential );
    }

    /**
     * Gets the credential.
     *
     * @param type
     *            the type
     * @return credential
     */
    public Credential getCredential( RepositoryType type )
    {
        return _mapCredential.get( type );
    }

    /**
     * The Class Credential.
     *
     * @author merlinfe
     */
    public class Credential
    {

        /**
         * Instantiates a new credential.
         *
         * @param strLogin
         *            the str login
         * @param strPassword
         *            the str password
         */
        public Credential( String strLogin, String strPassword )
        {
            super( );
            this._strLogin = strLogin;
            this._strPassword = strPassword;
        }

        /** The str login. */
        private String _strLogin;

        /** The str password. */
        private String _strPassword;

        /**
         * Gets the login.
         *
         * @return login
         */
        public String getLogin( )
        {
            return _strLogin;
        }

        /**
         * Gets the password.
         *
         * @return password
         */
        public String getPassword( )
        {
            return _strPassword;
        }

    }

}
