/*
 * Copyright (c) 2002-2013, Mairie de Paris
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


public class CommandResult implements Cloneable, Serializable
{
    /**
    *
    */
    private static final long serialVersionUID = 1L;

    public static int STATUS_ERROR = 0;
    public static int STATUS_OK= 1;
    public static int ERROR_TYPE_INFO = 0;
    public static int ERROR_TYPE_STOP = 1;
    
    
    private StringBuffer _strLog;
    private int _nStatus;
    private int _nErrorType;
    private boolean _bRunning;
    private Date _dBegin;
    private Date _dEnd;
    private int progressValue;
    
    private String _strError;
 
    private Map<String,String> _mResultInformations=new HashMap<String, String>();
    
    /**
     * "Getter method" pour la variable {@link #_strLog}
     * @return La variable {@link #_strLog}
     */
    public StringBuffer getLog(  )
    {
        return _strLog;
    }

    /**
     * "Setter method" pour la variable {@link #_strLog}
     * @param strLog La nouvelle valeur de la variable {@link #_strLog}
     */
    public void setLog( StringBuffer strLog )
    {
        _strLog = strLog;
    }

    /**
     * "Getter method" pour la variable {@link #_nStatus}
     * @return La variable {@link #_nStatus}
     */
    public int getStatus(  )
    {
        return _nStatus;
    }

    /**
     * "Setter method" pour la variable {@link #_nStatus}
     * @param nStatus La nouvelle valeur de la variable {@link #_nStatus}
     */
    public void setStatus( int nStatus )
    {
        _nStatus = nStatus;
    }

    /**
     * "Getter method" pour la variable {@link #_bRunning}
     * @return La variable {@link #_bRunning}
     */
    public boolean isRunning(  )
    {
        return _bRunning;
    }

    /**
     * "Setter method" pour la variable {@link #_bRunning}
     * @param bRunning La nouvelle valeur de la variable {@link #_bRunning}
     */
    public void setRunning( boolean bRunning )
    {
        _bRunning = bRunning;
    }

    /**
     *
     *{@inheritDoc}
     */
    @Override
    public Object clone(  ) throws CloneNotSupportedException
    {
        CommandResult clone = (CommandResult) super.clone(  );
        clone._bRunning = this._bRunning;
        clone._nStatus = this._nStatus;
        clone._strLog = this._strLog;
        clone._strError = this._strError;

        return clone;
    }

    /**
     * "Getter method" pour la variable {@link #_nIdError}
     * @return La variable {@link #_nIdError}
     */
    public String getError(  )
    {
        return _strError;
    }

    /**
     * "Setter method" pour la variable {@link #_nIdError}
     * @param nIdError La nouvelle valeur de la variable {@link #_nIdError}
     */
    public void setError( String strIdError )
    {
        _strError = strIdError;
    }

	public Map<String,String> getResultInformations() {
		return _mResultInformations;
	}

	public void setResultInformations(Map<String,String> _mResultInformations) {
		this._mResultInformations = _mResultInformations;
	}

	public int getErrorType() {
		return _nErrorType;
	}

	public void setErrorType(int _nErrorType) {
		this._nErrorType = _nErrorType;
	}
	
	 public Date getDateBegin( )
	    {
	        return _dBegin;
	    }

	    public void setDateBegin( Date _dBegin )
	    {
	        this._dBegin = _dBegin;
	    }

	    public Date getDateEnd( )
	    {
	        return _dEnd;
	    }

	    public void setDateEnd( Date _dEnd )
	    {
	        this._dEnd = _dEnd;
	    }

        public int getProgressValue( )
        {
            return progressValue;
        }

        public void setProgressValue( int progressValue )
        {
            this.progressValue = progressValue;
        }

	
}
