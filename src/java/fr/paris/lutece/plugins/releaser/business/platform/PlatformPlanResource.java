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

/**
 * A resource to release in a platform step : a component of the step or the aggregate itself (JSON contract releaser to Jenkins).
 */
public class PlatformPlanResource implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _strGroupId;
    private String _strArtifactId;
    private String _strType;
    private String _strScmUrl;
    private String _strBranch;
    private String _strMasterBranch;
    private String _strCurrentVersion;
    private String _strTargetVersion;
    private String _strNextSnapshotVersion;
    private String _strParentVersion;
    private String _strCurrentParentVersion;
    private String _strCoreVersion;
    private String _strVersionProperty;
    private PlatformVersionUpdates _versionUpdates = new PlatformVersionUpdates( );

    /**
     * Returns the Maven groupId.
     *
     * @return the groupId
     */
    public String getGroupId( )
    {
        return _strGroupId;
    }

    /**
     * Sets the Maven groupId.
     *
     * @param strGroupId
     *            the groupId
     */
    public void setGroupId( String strGroupId )
    {
        _strGroupId = strGroupId;
    }

    /**
     * Returns the Maven artifactId.
     *
     * @return the artifactId
     */
    public String getArtifactId( )
    {
        return _strArtifactId;
    }

    /**
     * Sets the Maven artifactId.
     *
     * @param strArtifactId
     *            the artifactId
     */
    public void setArtifactId( String strArtifactId )
    {
        _strArtifactId = strArtifactId;
    }

    /**
     * Returns the Maven type (jar, lutece-plugin, lutece-core, maven-plugin, pom...).
     *
     * @return the type
     */
    public String getType( )
    {
        return _strType;
    }

    /**
     * Sets the Maven type.
     *
     * @param strType
     *            the type
     */
    public void setType( String strType )
    {
        _strType = strType;
    }

    /**
     * Returns the Git repository URL.
     *
     * @return the SCM URL
     */
    public String getScmUrl( )
    {
        return _strScmUrl;
    }

    /**
     * Sets the Git repository URL.
     *
     * @param strScmUrl
     *            the SCM URL
     */
    public void setScmUrl( String strScmUrl )
    {
        _strScmUrl = strScmUrl;
    }

    /**
     * Returns the branch to release from.
     *
     * @return the branch
     */
    public String getBranch( )
    {
        return _strBranch;
    }

    /**
     * Sets the branch to release from.
     *
     * @param strBranch
     *            the branch
     */
    public void setBranch( String strBranch )
    {
        _strBranch = strBranch;
    }

    /**
     * Returns the master branch the release is merged into, null for a pre-release.
     *
     * @return the master branch
     */
    public String getMasterBranch( )
    {
        return _strMasterBranch;
    }

    /**
     * Sets the master branch the release is merged into.
     *
     * @param strMasterBranch
     *            the master branch
     */
    public void setMasterBranch( String strMasterBranch )
    {
        _strMasterBranch = strMasterBranch;
    }

    /**
     * Returns the version currently on the branch.
     *
     * @return the current version
     */
    public String getCurrentVersion( )
    {
        return _strCurrentVersion;
    }

    /**
     * Sets the version currently on the branch.
     *
     * @param strCurrentVersion
     *            the current version
     */
    public void setCurrentVersion( String strCurrentVersion )
    {
        _strCurrentVersion = strCurrentVersion;
    }

    /**
     * Returns the version to release.
     *
     * @return the target version
     */
    public String getTargetVersion( )
    {
        return _strTargetVersion;
    }

    /**
     * Sets the version to release.
     *
     * @param strTargetVersion
     *            the target version
     */
    public void setTargetVersion( String strTargetVersion )
    {
        _strTargetVersion = strTargetVersion;
    }

    /**
     * Returns the development version to set after the release.
     *
     * @return the next snapshot version
     */
    public String getNextSnapshotVersion( )
    {
        return _strNextSnapshotVersion;
    }

    /**
     * Sets the development version to set after the release.
     *
     * @param strNextSnapshotVersion
     *            the next snapshot version
     */
    public void setNextSnapshotVersion( String strNextSnapshotVersion )
    {
        _strNextSnapshotVersion = strNextSnapshotVersion;
    }

    /**
     * Returns the parent POM version to set before the release, null to keep the current one.
     *
     * @return the parent version
     */
    public String getParentVersion( )
    {
        return _strParentVersion;
    }

    /**
     * Sets the parent POM version to set before the release.
     *
     * @param strParentVersion
     *            the parent version
     */
    public void setParentVersion( String strParentVersion )
    {
        _strParentVersion = strParentVersion;
    }

    /**
     * Returns the parent POM version declared on the branch before the release, for information.
     *
     * @return the current parent version, null when unknown
     */
    public String getCurrentParentVersion( )
    {
        return _strCurrentParentVersion;
    }

    /**
     * Sets the parent POM version declared on the branch before the release.
     *
     * @param strCurrentParentVersion
     *            the current parent version
     */
    public void setCurrentParentVersion( String strCurrentParentVersion )
    {
        _strCurrentParentVersion = strCurrentParentVersion;
    }

    /**
     * Returns the version of lutece-core the pipeline must set in the POM of the aggregate before the release (platform pipeline).
     *
     * @return the core version, null to leave the POM unchanged
     */
    public String getCoreVersion( )
    {
        return _strCoreVersion;
    }

    /**
     * Sets the core version to set in the POM.
     *
     * @param strCoreVersion
     *            the core version
     */
    public void setCoreVersion( String strCoreVersion )
    {
        _strCoreVersion = strCoreVersion;
    }

    /**
     * Returns the name of the property of the aggregate POM that holds the version of this component, null when the version is declared on
     * the dependency itself. Lets the pipeline update the aggregate POM for the released components only.
     *
     * @return the version property
     */
    public String getVersionProperty( )
    {
        return _strVersionProperty;
    }

    /**
     * Sets the version property.
     *
     * @param strVersionProperty
     *            the version property
     */
    public void setVersionProperty( String strVersionProperty )
    {
        _strVersionProperty = strVersionProperty;
    }

    /**
     * Returns the POM updates to apply before the release.
     *
     * @return the version updates
     */
    public PlatformVersionUpdates getVersionUpdates( )
    {
        return _versionUpdates;
    }

    /**
     * Sets the POM updates to apply before the release.
     *
     * @param versionUpdates
     *            the version updates
     */
    public void setVersionUpdates( PlatformVersionUpdates versionUpdates )
    {
        _versionUpdates = versionUpdates;
    }

    /**
     * Returns the Maven coordinates "groupId:artifactId", the key used in version updates and step results.
     *
     * @return the coordinates
     */
    public String getCoordinates( )
    {
        return _strGroupId + ":" + _strArtifactId;
    }
}
