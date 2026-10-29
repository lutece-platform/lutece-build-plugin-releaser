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

import java.io.Serializable;

/**
 * The decision taken on a resource whose branch holds commits made after the last beta or RC of its version, for a stable release : release
 * it from the SNAPSHOT anyway. It holds for the verified commit only.
 */
public class PlatformUntestedDecision implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _strCommit;
    private String _strUserName;
    private String _strDate;

    /**
     * Returns the verified commit the decision was taken on.
     *
     * @return the commit
     */
    public String getCommit( )
    {
        return _strCommit;
    }

    /**
     * Sets the verified commit the decision was taken on.
     *
     * @param strCommit
     *            the commit
     */
    public void setCommit( String strCommit )
    {
        _strCommit = strCommit;
    }

    /**
     * Returns the user who took the decision.
     *
     * @return the user name
     */
    public String getUserName( )
    {
        return _strUserName;
    }

    /**
     * Sets the user who took the decision.
     *
     * @param strUserName
     *            the user name
     */
    public void setUserName( String strUserName )
    {
        _strUserName = strUserName;
    }

    /**
     * Returns the date of the decision.
     *
     * @return the date
     */
    public String getDate( )
    {
        return _strDate;
    }

    /**
     * Sets the date of the decision.
     *
     * @param strDate
     *            the date
     */
    public void setDate( String strDate )
    {
        _strDate = strDate;
    }
}
