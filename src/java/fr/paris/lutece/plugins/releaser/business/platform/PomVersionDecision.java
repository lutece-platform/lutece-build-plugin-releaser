/*
 * Copyright (c) 2002-2026, City of Paris
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
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
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
 * The decision about a version the platform POM references (lutece-core, parent POM) before the platform step : what the POM says, what is
 * expected, and what the user chose. The pipeline sets the target in the POM before the release ; a blocked decision forbids the launch.
 */
public class PomVersionDecision implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * The reasons the version referenced by the POM is refused.
     */
    public enum Problem
    {
        /** The expected version is known and the POM references another one */
        MISMATCH,
        /** No expected version is known and the POM references a version of another core */
        WRONG_CORE
    }

    private String _strCurrent;
    private String _strExpected;
    private boolean _bExpectedReleased;
    private Problem _problem;
    private boolean _bKeepAllowed;
    private String _strTarget;
    private boolean _bKept;
    private boolean _bBlocked;

    /**
     * Returns the version referenced by the POM.
     *
     * @return the current version
     */
    public String getCurrent( )
    {
        return _strCurrent;
    }

    /**
     * Sets the version referenced by the POM.
     *
     * @param strCurrent
     *            the current version
     */
    public void setCurrent( String strCurrent )
    {
        _strCurrent = strCurrent;
    }

    /**
     * Returns the expected version : released by the campaign, or the last one published.
     *
     * @return the expected version, null when none is known
     */
    public String getExpected( )
    {
        return _strExpected;
    }

    /**
     * Sets the expected version.
     *
     * @param strExpected
     *            the expected version
     */
    public void setExpected( String strExpected )
    {
        _strExpected = strExpected;
    }

    /**
     * Whether the expected version was released by the campaign (true) or is the last one published (false).
     *
     * @return true when released by the campaign
     */
    public boolean isExpectedReleased( )
    {
        return _bExpectedReleased;
    }

    /**
     * Sets whether the expected version was released by the campaign.
     *
     * @param bExpectedReleased
     *            true when released by the campaign
     */
    public void setExpectedReleased( boolean bExpectedReleased )
    {
        _bExpectedReleased = bExpectedReleased;
    }

    /**
     * Returns the problem of the version referenced by the POM.
     *
     * @return the problem, null when the version is acceptable
     */
    public Problem getProblem( )
    {
        return _problem;
    }

    /**
     * Sets the problem.
     *
     * @param problem
     *            the problem
     */
    public void setProblem( Problem problem )
    {
        _problem = problem;
    }

    /**
     * Whether keeping the version of the POM is an acceptable choice.
     *
     * @return true when the version may be kept
     */
    public boolean isKeepAllowed( )
    {
        return _bKeepAllowed;
    }

    /**
     * Sets whether keeping the version of the POM is acceptable.
     *
     * @param bKeepAllowed
     *            true when the version may be kept
     */
    public void setKeepAllowed( boolean bKeepAllowed )
    {
        _bKeepAllowed = bKeepAllowed;
    }

    /**
     * Returns the version the pipeline must set in the POM before the release.
     *
     * @return the target version, null to leave the POM unchanged
     */
    public String getTarget( )
    {
        return _strTarget;
    }

    /**
     * Sets the target version.
     *
     * @param strTarget
     *            the target version
     */
    public void setTarget( String strTarget )
    {
        _strTarget = strTarget;
    }

    /**
     * Whether the user chose to keep the version of the POM.
     *
     * @return true when kept
     */
    public boolean isKept( )
    {
        return _bKept;
    }

    /**
     * Sets whether the version of the POM is kept.
     *
     * @param bKept
     *            true when kept
     */
    public void setKept( boolean bKept )
    {
        _bKept = bKept;
    }

    /**
     * Whether the step is blocked by this version : a problem exists and no choice solves it.
     *
     * @return true when blocked
     */
    public boolean isBlocked( )
    {
        return _bBlocked;
    }

    /**
     * Sets whether the step is blocked by this version.
     *
     * @param bBlocked
     *            true when blocked
     */
    public void setBlocked( boolean bBlocked )
    {
        _bBlocked = bBlocked;
    }
}
