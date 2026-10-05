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
import java.sql.Timestamp;

/**
 * One step of a platform release campaign : the plan sent to Jenkins, the build followed and the versions obtained.
 */
public class PlatformReleaseStep implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** The id. */
    private int _nId;

    /** The id of the campaign. */
    private int _nIdPlatformRelease;

    /** The step number (1..5). */
    private int _nStepNumber;

    /** The status. */
    private PlatformReleaseStatus _status = PlatformReleaseStatus.TODO;

    /** The plan sent to Jenkins (JSON). */
    private String _strPlanJson;

    /** The result reported by Jenkins : released versions, report (JSON). */
    private String _strResultJson;

    /** The URL of the Jenkins build. */
    private String _strJenkinsBuildUrl;

    /** The number of the Jenkins build. */
    private int _nJenkinsBuildNumber;

    /** The result of the Jenkins build (SUCCESS, FAILURE, ABORTED...). */
    private String _strJenkinsResult;

    /** The date the step was sent to Jenkins. */
    private Timestamp _dateBegin;

    /** The date the Jenkins build finished. */
    private Timestamp _dateEnd;

    /**
     * Returns the id.
     *
     * @return the id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the id.
     *
     * @param nId
     *            the id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the id of the campaign.
     *
     * @return the id of the campaign
     */
    public int getIdPlatformRelease( )
    {
        return _nIdPlatformRelease;
    }

    /**
     * Sets the id of the campaign.
     *
     * @param nIdPlatformRelease
     *            the id of the campaign
     */
    public void setIdPlatformRelease( int nIdPlatformRelease )
    {
        _nIdPlatformRelease = nIdPlatformRelease;
    }

    /**
     * Returns the step number.
     *
     * @return the step number
     */
    public int getStepNumber( )
    {
        return _nStepNumber;
    }

    /**
     * Sets the step number.
     *
     * @param nStepNumber
     *            the step number
     */
    public void setStepNumber( int nStepNumber )
    {
        _nStepNumber = nStepNumber;
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public PlatformReleaseStatus getStatus( )
    {
        return _status;
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the status
     */
    public void setStatus( PlatformReleaseStatus status )
    {
        _status = status;
    }

    /**
     * Returns the plan sent to Jenkins.
     *
     * @return the plan (JSON)
     */
    public String getPlanJson( )
    {
        return _strPlanJson;
    }

    /**
     * Sets the plan sent to Jenkins.
     *
     * @param strPlanJson
     *            the plan (JSON)
     */
    public void setPlanJson( String strPlanJson )
    {
        _strPlanJson = strPlanJson;
    }

    /**
     * Returns the result reported by Jenkins.
     *
     * @return the result (JSON)
     */
    public String getResultJson( )
    {
        return _strResultJson;
    }

    /**
     * Sets the result reported by Jenkins.
     *
     * @param strResultJson
     *            the result (JSON)
     */
    public void setResultJson( String strResultJson )
    {
        _strResultJson = strResultJson;
    }

    /**
     * Returns the URL of the Jenkins build.
     *
     * @return the URL
     */
    public String getJenkinsBuildUrl( )
    {
        return _strJenkinsBuildUrl;
    }

    /**
     * Sets the URL of the Jenkins build.
     *
     * @param strJenkinsBuildUrl
     *            the URL
     */
    public void setJenkinsBuildUrl( String strJenkinsBuildUrl )
    {
        _strJenkinsBuildUrl = strJenkinsBuildUrl;
    }

    /**
     * Returns the number of the Jenkins build.
     *
     * @return the build number
     */
    public int getJenkinsBuildNumber( )
    {
        return _nJenkinsBuildNumber;
    }

    /**
     * Sets the number of the Jenkins build.
     *
     * @param nJenkinsBuildNumber
     *            the build number
     */
    public void setJenkinsBuildNumber( int nJenkinsBuildNumber )
    {
        _nJenkinsBuildNumber = nJenkinsBuildNumber;
    }

    /**
     * Returns the result of the Jenkins build.
     *
     * @return the result
     */
    public String getJenkinsResult( )
    {
        return _strJenkinsResult;
    }

    /**
     * Sets the result of the Jenkins build.
     *
     * @param strJenkinsResult
     *            the result
     */
    public void setJenkinsResult( String strJenkinsResult )
    {
        _strJenkinsResult = strJenkinsResult;
    }

    /**
     * Returns the date the step was sent to Jenkins.
     *
     * @return the begin date
     */
    public Timestamp getDateBegin( )
    {
        return _dateBegin;
    }

    /**
     * Sets the date the step was sent to Jenkins.
     *
     * @param dateBegin
     *            the begin date
     */
    public void setDateBegin( Timestamp dateBegin )
    {
        _dateBegin = dateBegin;
    }

    /**
     * Returns the date the Jenkins build finished.
     *
     * @return the end date
     */
    public Timestamp getDateEnd( )
    {
        return _dateEnd;
    }

    /**
     * Sets the date the Jenkins build finished.
     *
     * @param dateEnd
     *            the end date
     */
    public void setDateEnd( Timestamp dateEnd )
    {
        _dateEnd = dateEnd;
    }
}
