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
 * One check of a verified resource, as reported by the step pipeline in verify-report.json : its name (clone, tests, versions,
 * snapshot-dependencies, transitive-snapshot-dependencies, build, tag), its status and the error lines when it did not pass.
 */
public class PlatformVerificationCheck implements Serializable
{
    /** Status of a check or of a resource that passed. */
    public static final String STATUS_OK = "OK";
    /** Status of a check or of a resource to fix before the release. */
    public static final String STATUS_TO_FIX = "TO_FIX";
    /** Status of a check or of a resource with a warning only. */
    public static final String STATUS_WARNING = "WARNING";

    private static final long serialVersionUID = 1L;

    private String _strCheck;
    private String _strStatus;
    private String _strDetails;

    /**
     * Returns the name of the check.
     *
     * @return the name of the check
     */
    public String getCheck( )
    {
        return _strCheck;
    }

    /**
     * Sets the name of the check.
     *
     * @param strCheck
     *            the name of the check
     */
    public void setCheck( String strCheck )
    {
        _strCheck = strCheck;
    }

    /**
     * Returns the status : OK, TO_FIX or WARNING.
     *
     * @return the status
     */
    public String getStatus( )
    {
        return _strStatus;
    }

    /**
     * Sets the status.
     *
     * @param strStatus
     *            the status
     */
    public void setStatus( String strStatus )
    {
        _strStatus = strStatus;
    }

    /**
     * Returns the error lines when the check did not pass, empty otherwise.
     *
     * @return the details
     */
    public String getDetails( )
    {
        return _strDetails;
    }

    /**
     * Sets the error lines.
     *
     * @param strDetails
     *            the details
     */
    public void setDetails( String strDetails )
    {
        _strDetails = strDetails;
    }
}
