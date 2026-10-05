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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The result of a platform step reported by the Jenkins step pipeline (step-report.json) : released versions keyed by "groupId:artifactId",
 * aggregate version and free text report.
 */
public class PlatformStepResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Map<String, String> _mapReleasedVersions = new LinkedHashMap<>( );
    private Map<String, String> _mapFailedVersions = new LinkedHashMap<>( );
    private List<String> _listNotProcessed = new ArrayList<>( );
    private boolean _bPomUpdated;
    private String _strAggregateVersion;
    private String _strReport;

    /**
     * Returns the released versions : "groupId:artifactId" to released version.
     *
     * @return the released versions
     */
    public Map<String, String> getReleasedVersions( )
    {
        return _mapReleasedVersions;
    }

    /**
     * Sets the released versions.
     *
     * @param mapReleasedVersions
     *            the released versions
     */
    public void setReleasedVersions( Map<String, String> mapReleasedVersions )
    {
        _mapReleasedVersions = mapReleasedVersions;
    }

    /**
     * Returns the released version of the aggregate, null when the step has no aggregate.
     *
     * @return the aggregate version
     */
    public String getAggregateVersion( )
    {
        return _strAggregateVersion;
    }

    /**
     * Sets the released version of the aggregate.
     *
     * @param strAggregateVersion
     *            the aggregate version
     */
    public void setAggregateVersion( String strAggregateVersion )
    {
        _strAggregateVersion = strAggregateVersion;
    }

    /**
     * Returns the free text report of the pipeline.
     *
     * @return the report
     */
    public String getReport( )
    {
        return _strReport;
    }

    /**
     * Sets the free text report of the pipeline.
     *
     * @param strReport
     *            the report
     */
    public void setReport( String strReport )
    {
        _strReport = strReport;
    }

    /**
     * Returns the components whose release failed and was rolled back : "groupId:artifactId" to the reason.
     *
     * @return the failed components
     */
    public Map<String, String> getFailedVersions( )
    {
        return _mapFailedVersions;
    }

    /**
     * Sets the failed components.
     *
     * @param mapFailedVersions
     *            the failed components
     */
    public void setFailedVersions( Map<String, String> mapFailedVersions )
    {
        _mapFailedVersions = mapFailedVersions != null ? mapFailedVersions : new LinkedHashMap<>( );
    }

    /**
     * Returns the components of the plan the pipeline never reached ("groupId:artifactId").
     *
     * @return the components not processed
     */
    public List<String> getNotProcessed( )
    {
        return _listNotProcessed;
    }

    /**
     * Sets the components not processed.
     *
     * @param listNotProcessed
     *            the components not processed
     */
    public void setNotProcessed( List<String> listNotProcessed )
    {
        _listNotProcessed = listNotProcessed != null ? listNotProcessed : new ArrayList<>( );
    }

    /**
     * Whether the POM of the aggregate was updated and pushed with the released versions, even when the step failed.
     *
     * @return true when the POM was pushed
     */
    public boolean isPomUpdated( )
    {
        return _bPomUpdated;
    }

    /**
     * Sets whether the POM of the aggregate was pushed.
     *
     * @param bPomUpdated
     *            true when pushed
     */
    public void setPomUpdated( boolean bPomUpdated )
    {
        _bPomUpdated = bPomUpdated;
    }
}
