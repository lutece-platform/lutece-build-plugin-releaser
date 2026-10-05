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
 * The plan of a platform step sent to the Jenkins step pipeline (JSON contract releaser to Jenkins) : the ordered components to release, then
 * the aggregate to release.
 */
public class PlatformReleasePlan implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _nCampaignId;
    private String _strCampaignName;
    private int _nStep;
    private PlatformStepCode _stepCode;
    private PlatformReleaseType _releaseType;
    private int _nCoreMajor;
    private boolean _bDryRun;
    private List<PlatformPlanResource> _listComponents = new ArrayList<>( );
    private PlatformPlanResource _aggregate;
    private Map<String, String> _mapPipelineParameters = new LinkedHashMap<>( );

    /**
     * Returns the campaign id.
     *
     * @return the campaign id
     */
    public int getCampaignId( )
    {
        return _nCampaignId;
    }

    /**
     * Sets the campaign id.
     *
     * @param nCampaignId
     *            the campaign id
     */
    public void setCampaignId( int nCampaignId )
    {
        _nCampaignId = nCampaignId;
    }

    /**
     * Returns the campaign name.
     *
     * @return the campaign name
     */
    public String getCampaignName( )
    {
        return _strCampaignName;
    }

    /**
     * Sets the campaign name.
     *
     * @param strCampaignName
     *            the campaign name
     */
    public void setCampaignName( String strCampaignName )
    {
        _strCampaignName = strCampaignName;
    }

    /**
     * Returns the step number (1 to 5).
     *
     * @return the step number
     */
    public int getStep( )
    {
        return _nStep;
    }

    /**
     * Sets the step number.
     *
     * @param nStep
     *            the step number
     */
    public void setStep( int nStep )
    {
        _nStep = nStep;
    }

    /**
     * Returns the step code.
     *
     * @return the step code
     */
    public PlatformStepCode getStepCode( )
    {
        return _stepCode;
    }

    /**
     * Sets the step code.
     *
     * @param stepCode
     *            the step code
     */
    public void setStepCode( PlatformStepCode stepCode )
    {
        _stepCode = stepCode;
    }

    /**
     * Returns the release type of the step (stable for the first two steps whatever the campaign type).
     *
     * @return the release type
     */
    public PlatformReleaseType getReleaseType( )
    {
        return _releaseType;
    }

    /**
     * Sets the release type of the step.
     *
     * @param releaseType
     *            the release type
     */
    public void setReleaseType( PlatformReleaseType releaseType )
    {
        _releaseType = releaseType;
    }

    /**
     * Returns the core version (7 or 8).
     *
     * @return the Lutece major
     */
    public int getCoreMajor( )
    {
        return _nCoreMajor;
    }

    /**
     * Sets the core version.
     *
     * @param nCoreMajor
     *            the Lutece major
     */
    public void setCoreMajor( int nCoreMajor )
    {
        _nCoreMajor = nCoreMajor;
    }

    /**
     * Whether the pipeline must simulate without pushing, tagging or deploying.
     *
     * @return true for a dry run
     */
    public boolean isDryRun( )
    {
        return _bDryRun;
    }

    /**
     * Sets the dry run flag.
     *
     * @param bDryRun
     *            true for a dry run
     */
    public void setDryRun( boolean bDryRun )
    {
        _bDryRun = bDryRun;
    }

    /**
     * Returns the components to release, in release order.
     *
     * @return the components
     */
    public List<PlatformPlanResource> getComponents( )
    {
        return _listComponents;
    }

    /**
     * Sets the components to release.
     *
     * @param listComponents
     *            the components
     */
    public void setComponents( List<PlatformPlanResource> listComponents )
    {
        _listComponents = listComponents;
    }

    /**
     * Returns the aggregate released after its components, null when the step has no aggregate to release.
     *
     * @return the aggregate
     */
    public PlatformPlanResource getAggregate( )
    {
        return _aggregate;
    }

    /**
     * Sets the aggregate.
     *
     * @param aggregate
     *            the aggregate
     */
    public void setAggregate( PlatformPlanResource aggregate )
    {
        _aggregate = aggregate;
    }

    /**
     * Returns the parameters of the Jenkins pipeline chosen on the screen of the step (last step : core, release target, next snapshot,
     * SNAPSHOT tolerance), applied over the ones computed from the plan when the job is triggered.
     *
     * @return the pipeline parameters, by Jenkins parameter name
     */
    public Map<String, String> getPipelineParameters( )
    {
        return _mapPipelineParameters;
    }

    /**
     * Sets the pipeline parameters.
     *
     * @param mapPipelineParameters
     *            the pipeline parameters
     */
    public void setPipelineParameters( Map<String, String> mapPipelineParameters )
    {
        _mapPipelineParameters = mapPipelineParameters != null ? mapPipelineParameters : new LinkedHashMap<>( );
    }
}
