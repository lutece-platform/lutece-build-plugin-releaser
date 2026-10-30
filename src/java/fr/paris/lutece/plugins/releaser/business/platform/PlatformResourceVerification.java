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
import java.util.ArrayList;
import java.util.List;

/**
 * The verification of one resource of a platform step, as reported by the step pipeline in verify-report.json : its overall status, the
 * version it was verified for, the commit verified (head of the branch at clone time) and the result of each check.
 */
public class PlatformResourceVerification implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _strStatus;
    private String _strVersion;
    private String _strCommit;
    private String _strNote;
    private String _strSourceTag;
    private String _strUntestedSinceTag;
    private int _nStep;
    private List<PlatformVerificationCheck> _listChecks = new ArrayList<>( );

    /**
     * Returns the overall status : OK, TO_FIX or WARNING (see PlatformVerificationCheck).
     *
     * @return the status
     */
    public String getStatus( )
    {
        return _strStatus;
    }

    /**
     * Sets the overall status.
     *
     * @param strStatus
     *            the status
     */
    public void setStatus( String strStatus )
    {
        _strStatus = strStatus;
    }

    /**
     * Returns the target version the resource was verified for.
     *
     * @return the version
     */
    public String getVersion( )
    {
        return _strVersion;
    }

    /**
     * Sets the target version the resource was verified for.
     *
     * @param strVersion
     *            the version
     */
    public void setVersion( String strVersion )
    {
        _strVersion = strVersion;
    }

    /**
     * Returns the commit verified, empty when the clone failed.
     *
     * @return the commit
     */
    public String getCommit( )
    {
        return _strCommit;
    }

    /**
     * Sets the commit verified.
     *
     * @param strCommit
     *            the commit
     */
    public void setCommit( String strCommit )
    {
        _strCommit = strCommit;
    }

    /**
     * Returns the note of the pipeline, set for the aggregate verified before the version updates, null otherwise.
     *
     * @return the note
     */
    public String getNote( )
    {
        return _strNote;
    }

    /**
     * Sets the note of the pipeline.
     *
     * @param strNote
     *            the note
     */
    public void setNote( String strNote )
    {
        _strNote = strNote;
    }

    /**
     * Returns the beta or RC tag the resource was verified from, its commit being then the commit of the tag.
     *
     * @return the tag, null when verified from the head of its branch
     */
    public String getSourceTag( )
    {
        return _strSourceTag;
    }

    /**
     * Sets the beta or RC tag the resource was verified from.
     *
     * @param strSourceTag
     *            the tag
     */
    public void setSourceTag( String strSourceTag )
    {
        _strSourceTag = strSourceTag;
    }

    /**
     * Returns the last beta or RC tag of the version, set when commits were made after it (untested-commits check to fix).
     *
     * @return the tag, null otherwise
     */
    public String getUntestedSinceTag( )
    {
        return _strUntestedSinceTag;
    }

    /**
     * Sets the last beta or RC tag of the version, followed by untested commits.
     *
     * @param strUntestedSinceTag
     *            the tag
     */
    public void setUntestedSinceTag( String strUntestedSinceTag )
    {
        _strUntestedSinceTag = strUntestedSinceTag;
    }

    /**
     * Returns the step that releases the resource, set by the verification of step 0.
     *
     * @return the step number, 0 when unknown
     */
    public int getStep( )
    {
        return _nStep;
    }

    /**
     * Sets the step that releases the resource.
     *
     * @param nStep
     *            the step number
     */
    public void setStep( int nStep )
    {
        _nStep = nStep;
    }

    /**
     * Returns the result of each check, in the order they ran.
     *
     * @return the checks
     */
    public List<PlatformVerificationCheck> getChecks( )
    {
        return _listChecks;
    }

    /**
     * Sets the result of each check.
     *
     * @param listChecks
     *            the checks
     */
    public void setChecks( List<PlatformVerificationCheck> listChecks )
    {
        _listChecks = listChecks != null ? listChecks : new ArrayList<>( );
    }
}
