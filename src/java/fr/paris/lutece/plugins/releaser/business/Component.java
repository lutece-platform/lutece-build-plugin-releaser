/*
 * Copyright (c) 2002-2017, Mairie de Paris
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

import java.util.List;

import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.version.Version;

/**
 * This is the business class for the object Component
 */
public class Component extends AbstractReleaserResource
{
    // Variables declarations
    private String _strArtifactId;
    private String _strGroupId;
    private String _strType;
    private String _strCurrentVersion;
    private String _strTargetVersion;
    private String _strLastAvailableVersion;
    private String _strLastAvailableSnapshotVersion;
    private String _strNextSnapshotVersion;
  
    private String _strDescription; 
    private boolean _bIsProject;
    private String _strJiraCode;
    private String _strJiraRoadmapUrl;
    private int _nJiraCurrentVersionClosedIssues;
    private int _nJiraCurrentVersionOpenedIssues;   
    private List<String> _listTargetVersions;
    private int _nTargetVersionIndex;
    private boolean _bDowngrade;
    private boolean _bUpgrade;
    private boolean _bErrorLastRelease;
    private String _strName;
    private String _strCloneUrl;
    private String _strFullName;
    private String _strTweetMessage;
    private String _strScmDeveloperConnection;
   
    
        

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
     * Returns the JiraCode
     * 
     * @return The JiraCode
     */
    public String getJiraCode( )
    {
        return _strJiraCode;
    }

    /**
     * Sets the JiraCode
     * 
     * @param strJiraCode
     *            The JiraCode
     */
    public void setJiraCode( String strJiraCode )
    {
        _strJiraCode = strJiraCode;
    }

    /**
     * Returns the JiraRoadmapUrl
     * 
     * @return The JiraRoadmapUrl
     */
    public String getJiraRoadmapUrl( )
    {
        return _strJiraRoadmapUrl;
    }

    /**
     * Sets the JiraRoadmapUrl
     * 
     * @param strJiraRoadmapUrl
     *            The JiraRoadmapUrl
     */
    public void setJiraRoadmapUrl( String strJiraRoadmapUrl )
    {
        _strJiraRoadmapUrl = strJiraRoadmapUrl;
    }

    /**
     * Returns the JiraCurrentVersionClosedIssues
     * 
     * @return The JiraCurrentVersionClosedIssues
     */
    public int getJiraCurrentVersionClosedIssues( )
    {
        return _nJiraCurrentVersionClosedIssues;
    }

    /**
     * Sets the JiraCurrentVersionClosedIssues
     * 
     * @param nJiraCurrentVersionClosedIssues
     *            The JiraCurrentVersionClosedIssues
     */
    public void setJiraCurrentVersionClosedIssues( int nJiraCurrentVersionClosedIssues )
    {
        _nJiraCurrentVersionClosedIssues = nJiraCurrentVersionClosedIssues;
    }

    /**
     * Returns the JiraCurrentVersionOpenedIssues
     * 
     * @return The JiraCurrentVersionOpenedIssues
     */
    public int getJiraCurrentVersionOpenedIssues( )
    {
        return _nJiraCurrentVersionOpenedIssues;
    }

    /**
     * Sets the JiraCurrentVersionOpenedIssues
     * 
     * @param nJiraCurrentVersionOpenedIssues
     *            The JiraCurrentVersionOpenedIssues
     */
    public void setJiraCurrentVersionOpenedIssues( int nJiraCurrentVersionOpenedIssues )
    {
        _nJiraCurrentVersionOpenedIssues = nJiraCurrentVersionOpenedIssues;
    }
    
    /**
     * Set target versions list
     * @param listTargetVersions The target versions list 
     */
    public void setTargetVersions( List<String> listTargetVersions )
    {
        _listTargetVersions = listTargetVersions;
    }
    
    /**
     * Gets the target versions list
     * @return the target versions list
     */
    public List<String> getTargetVersions()
    {
        return _listTargetVersions;
    }
    
    /**
     * Set the target version index
     * @param nIndex the target version index
     */
    public void setTargetVersionIndex( int nIndex )
    {
        _nTargetVersionIndex = nIndex;
    }
    
    /**
     * Get the target version index
     * @return the target version index 
     */
    public int getTargetVersionIndex()
    {
        return _nTargetVersionIndex;
    }
    
    /**
     * Returns the IsProject
     * 
     * @return True if should be released
     */
    public boolean shouldBeReleased( )
    {

        if(this.isProject( ) && this.isSnapshotVersion( ) && this.getTargetVersion( )!=null && !Version.isSnapshot( this.getTargetVersion( ))&& !this.isTheme( ) && !this.isDowngrade( ) &&  this.getCurrentVersion( ).equals( this.getLastAvailableSnapshotVersion( ) )  )
        {
             return true;   
        }
        return false;
   }



    
    /**
     * 
     * @return scm developer connection url
     */
    public String getScmDeveloperConnection( )
    {
        return _strScmDeveloperConnection;
    }
    /**
     * 
     * @param _strScmDeveloperConnection scm developer connection url
     */
    public void setScmDeveloperConnection( String _strScmDeveloperConnection )
    {
        this._strScmDeveloperConnection = _strScmDeveloperConnection;
    }
    
    /**
     * 
     * @return
     */
    public boolean isSnapshotVersion()
    {
         return  Version.isSnapshot(getCurrentVersion( ));
    }
    
    
    public boolean isUpgrade( )
    {
        return _bUpgrade;
    }

    public void setUpgrade( boolean _bUpgrade )
    {
        this._bUpgrade = _bUpgrade;
    }

    public boolean isDowngrade( )
    {
        return _bDowngrade;
    }

    public void setDowngrade( boolean _bDowngrade )
    {
        this._bDowngrade = _bDowngrade;
    }

 
    public boolean isTheme( )
    {
        return getType( )!=null && ConstanteUtils.CONSTANTE_TYPE_LUTECE_SITE.equals( getType( ) );
    }

    public String getLastAvailableSnapshotVersion( )
    {
        return _strLastAvailableSnapshotVersion;
    }

    public void setLastAvailableSnapshotVersion( String _strLastAvailableSnapshotVersion )
    {
        this._strLastAvailableSnapshotVersion = _strLastAvailableSnapshotVersion;
    }

    public boolean isErrorLastRelease( )
    {
        return _bErrorLastRelease;
    }

    public void setErrorLastRelease( boolean _bErrorLastRelease )
    {
        this._bErrorLastRelease = _bErrorLastRelease;
    }

    public String getName( )
    {
        return _strName;
    }

    public void setName( String _strName )
    {
        this._strName = _strName;
    }

    public String getCloneUrl( )
    {
        return _strCloneUrl;
    }

    public void setCloneUrl( String _strCloneUrl )
    {
        this._strCloneUrl = _strCloneUrl;
    }

    public String getFullName( )
    {
        return _strFullName;
    }

    public void setFullName( String strFullName )
    {
        this._strFullName = strFullName;
    }

    public String getTweetMessage( )
    {
        return _strTweetMessage;
    }

    public void setTweetMessage( String _strTweetMessage )
    {
        this._strTweetMessage = _strTweetMessage;
    }

    @Override
    public String getScmUrl( )
    {
        // TODO Auto-generated method stub
        return getScmDeveloperConnection( );
    }

            
}
