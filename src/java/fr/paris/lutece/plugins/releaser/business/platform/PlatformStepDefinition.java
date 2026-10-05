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

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

/**
 * The configurable part of a platform release step : its label and the repository of its aggregate. The behaviour of the step is fixed by its
 * {@link PlatformStepCode}. One row per step, editable in the platform configuration screen.
 */
public class PlatformStepDefinition implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** The step number (primary key). */
    private int _nStepNumber;

    /** The code. */
    private PlatformStepCode _code;

    /** The label. */
    @NotEmpty( message = "#i18n{releaser.validation.platformStepDefinition.name.notEmpty}" )
    @Size( max = 100, message = "#i18n{releaser.validation.platformStepDefinition.name.size}" )
    private String _strName;

    /** The repository URL of the aggregate. */
    @NotEmpty( message = "#i18n{releaser.validation.platformStepDefinition.scmUrl.notEmpty}" )
    @URL( message = "#i18n{releaser.validation.platformStepDefinition.scmUrl.url}" )
    @Size( max = 255, message = "#i18n{releaser.validation.platformStepDefinition.scmUrl.size}" )
    private String _strScmUrl;

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
     * Returns the code.
     *
     * @return the code
     */
    public PlatformStepCode getCode( )
    {
        return _code;
    }

    /**
     * Sets the code.
     *
     * @param code
     *            the code
     */
    public void setCode( PlatformStepCode code )
    {
        _code = code;
    }

    /**
     * Returns the label.
     *
     * @return the label
     */
    public String getName( )
    {
        return _strName;
    }

    /**
     * Sets the label.
     *
     * @param strName
     *            the label
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the repository URL of the aggregate.
     *
     * @return the repository URL
     */
    public String getScmUrl( )
    {
        return _strScmUrl;
    }

    /**
     * Sets the repository URL of the aggregate.
     *
     * @param strScmUrl
     *            the repository URL
     */
    public void setScmUrl( String strScmUrl )
    {
        _strScmUrl = strScmUrl;
    }
}
