/*
 * Copyright (c) 2002-2016, Mairie de Paris
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
package fr.paris.lutece.plugins.releaser.business;

/**
 * This is the business class for the object Component
 */
public class Component
{
    // Variables declarations
    private String _strArtifactId;
    private String _strGroupId;
    private String _strType;
    private String _strCurrentVersion;
    private String _strTargetVersion;
    private String _strLastAvailableVersion;
    private String _strNextSnapshotVersion;
    private String _strReleaseComment;
    private boolean _bIsProject;

    /**
     * Returns the ArtifactId
     * 
     * @return The ArtifactId
     */
    public String getArtifactId( )
    {
        return _strArtifactId;
    }

    /**
     * Sets the ArtifactId
     * 
     * @param strArtifactId
     *            The ArtifactId
     */
    public void setArtifactId( String strArtifactId )
    {
        _strArtifactId = strArtifactId;
    }

    /**
     * Returns the GroupId
     * 
     * @return The GroupId
     */
    public String getGroupId( )
    {
        return _strGroupId;
    }

    /**
     * Sets the GroupId
     * 
     * @param strGroupId
     *            The GroupId
     */
    public void setGroupId( String strGroupId )
    {
        _strGroupId = strGroupId;
    }

    /**
     * Returns the Type
     * 
     * @return The Type
     */
    public String getType( )
    {
        return _strType;
    }

    /**
     * Sets the Type
     * 
     * @param strType
     *            The Type
     */
    public void setType( String strType )
    {
        _strType = strType;
    }

    /**
     * Returns the CurrentVersion
     * 
     * @return The CurrentVersion
     */
    public String getCurrentVersion( )
    {
        return _strCurrentVersion;
    }

    /**
     * Sets the CurrentVersion
     * 
     * @param strCurrentVersion
     *            The CurrentVersion
     */
    public void setCurrentVersion( String strCurrentVersion )
    {
        _strCurrentVersion = strCurrentVersion;
    }

    /**
     * Returns the TargetVersion
     * 
     * @return The TargetVersion
     */
    public String getTargetVersion( )
    {
        return _strTargetVersion;
    }

    /**
     * Sets the TargetVersion
     * 
     * @param strTargetVersion
     *            The TargetVersion
     */
    public void setTargetVersion( String strTargetVersion )
    {
        _strTargetVersion = strTargetVersion;
    }

    /**
     * Returns the IsProject
     * 
     * @return The IsProject
     */
    public boolean isProject( )
    {
        return _bIsProject;
    }

    /**
     * Sets the IsProject
     * 
     * @param bIsProject
     *            The IsProject
     */
    public void setIsProject( boolean bIsProject )
    {
        _bIsProject = bIsProject;
    }

    /**
     * Returns the LastAvailableVersion
     * 
     * @return The LastAvailableVersion
     */
    public String getLastAvailableVersion( )
    {
        return _strLastAvailableVersion;
    }

    /**
     * Sets the LastAvailableVersion
     * 
     * @param strLastAvailableVersion
     *            The LastAvailableVersion
     */
    public void setLastAvailableVersion( String strLastAvailableVersion )
    {
        _strLastAvailableVersion = strLastAvailableVersion;
    }

    /**
     * Returns the NextSnapshotVersion
     * 
     * @return The NextSnapshotVersion
     */
    public String getNextSnapshotVersion( )
    {
        return _strNextSnapshotVersion;
    }

    /**
     * Sets the NextSnapshotVersion
     * 
     * @param strNextSnapshotVersion
     *            The NextSnapshotVersion
     */
    public void setNextSnapshotVersion( String strNextSnapshotVersion )
    {
        _strNextSnapshotVersion = strNextSnapshotVersion;
    }

    /**
     * Returns the ReleaseComment
     * 
     * @return The ReleaseComment
     */
    public String getReleaseComment( )
    {
        return _strReleaseComment;
    }

    /**
     * Sets the ReleaseComment
     * 
     * @param strReleaseComment
     *            The ReleaseComment
     */
    public void addReleaseComment( String strReleaseComment )
    {
        if ( _strReleaseComment != null )
        {
            _strReleaseComment = _strReleaseComment + "<br>\n" + strReleaseComment;
        }
        else
        {
            _strReleaseComment = strReleaseComment;
        }
    }

    public void resetComments()
    {
        _strReleaseComment = null;
    }
}
