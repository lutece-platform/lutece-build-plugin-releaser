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
package fr.paris.lutece.plugins.releaser.util;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * CommandResult.
 */
public class CommandResult implements Cloneable, Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The status error. */
    public static int STATUS_ERROR = 0;

    /** The status ok. */
    public static int STATUS_OK = 1;

    /** The error type info. */
    public static int ERROR_TYPE_INFO = 0;

    /** The error type stop. */
    public static int ERROR_TYPE_STOP = 1;

    /** The str log. */
    private StringBuffer _strLog;

    /** The n status. */
    private int _nStatus;

    /** The n error type. */
    private int _nErrorType;

    /** The b running. */
    private boolean _bRunning;

    /** The d begin. */
    private Date _dBegin;

    /** The d end. */
    private Date _dEnd;

    /** The progress value. */
    private int progressValue;

    /** The str error. */
    private String _strError;

    /** The m result informations. */
    private Map<String, String> _mResultInformations = new HashMap<String, String>( );

    /**
     * "Getter method" pour la variable {@link #_strLog}.
     *
     * @return La variable {@link #_strLog}
     */
    public StringBuffer getLog( )
    {
        return _strLog;
    }

    /**
     * "Setter method" pour la variable {@link #_strLog}.
     *
     * @param strLog
     *            La nouvelle valeur de la variable {@link #_strLog}
     */
    public void setLog( StringBuffer strLog )
    {
        _strLog = strLog;
    }

    /**
     * "Getter method" pour la variable {@link #_nStatus}.
     *
     * @return La variable {@link #_nStatus}
     */
    public int getStatus( )
    {
        return _nStatus;
    }

    /**
     * "Setter method" pour la variable {@link #_nStatus}.
     *
     * @param nStatus
     *            La nouvelle valeur de la variable {@link #_nStatus}
     */
    public void setStatus( int nStatus )
    {
        _nStatus = nStatus;
    }

    /**
     * "Getter method" pour la variable {@link #_bRunning}.
     *
     * @return La variable {@link #_bRunning}
     */
    public boolean isRunning( )
    {
        return _bRunning;
    }

    /**
     * "Setter method" pour la variable {@link #_bRunning}.
     *
     * @param bRunning
     *            La nouvelle valeur de la variable {@link #_bRunning}
     */
    public void setRunning( boolean bRunning )
    {
        _bRunning = bRunning;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Object clone( ) throws CloneNotSupportedException
    {
        CommandResult clone = (CommandResult) super.clone( );
        clone._bRunning = this._bRunning;
        clone._nStatus = this._nStatus;
        clone._strLog = this._strLog;
        clone._strError = this._strError;

        return clone;
    }

    /**
     * "Getter method" pour la variable {@link #_strError}.
     *
     * @return La variable {@link #_strError}
     */
    public String getError( )
    {
        return _strError;
    }

    /**
     * "Setter method" pour la variable {@link #_strError}.
     *
     * @param strIdError
     *            strIdError
     */
    public void setError( String strIdError )
    {
        _strError = strIdError;
    }

    /**
     * Gets the result informations.
     *
     * @return reult informations
     */
    public Map<String, String> getResultInformations( )
    {
        return _mResultInformations;
    }

    /**
     * Sets the result informations.
     *
     * @param _mResultInformations
     *            result informations
     */
    public void setResultInformations( Map<String, String> _mResultInformations )
    {
        this._mResultInformations = _mResultInformations;
    }

    /**
     * Gets the error type.
     *
     * @return error type
     */
    public int getErrorType( )
    {
        return _nErrorType;
    }

    /**
     * Sets the error type.
     *
     * @param _nErrorType
     *            error type
     */
    public void setErrorType( int _nErrorType )
    {
        this._nErrorType = _nErrorType;
    }

    /**
     * Gets the date begin.
     *
     * @return Date Begin
     */
    public Date getDateBegin( )
    {
        return _dBegin;
    }

    /**
     * Sets the date begin.
     *
     * @param _dBegin
     *            date begin
     */
    public void setDateBegin( Date _dBegin )
    {
        this._dBegin = _dBegin;
    }

    /**
     * Gets the date end.
     *
     * @return date end
     */
    public Date getDateEnd( )
    {
        return _dEnd;
    }

    /**
     * Sets the date end.
     *
     * @param _dEnd
     *            date end
     */
    public void setDateEnd( Date _dEnd )
    {
        this._dEnd = _dEnd;
    }

    /**
     * Gets the progress value.
     *
     * @return progress value
     */
    public int getProgressValue( )
    {
        return progressValue;
    }

    /**
     * Sets the progress value.
     *
     * @param progressValue
     *            progress value
     */
    public void setProgressValue( int progressValue )
    {
        this.progressValue = progressValue;
    }

}
