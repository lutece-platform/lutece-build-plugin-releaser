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

import javax.validation.constraints.*;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.*;

import fr.paris.lutece.portal.service.rbac.RBACResource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * This is the business class for the object Site.
 */
public class Site extends AbstractReleaserResource implements RBACResource, Serializable
{
	
	// RBAC management
    public static final String RESOURCE_TYPE = "site";

	// site Permissions
    public static final String PERMISSION_RELEASE_SITE = "releaseSitePermission";
    public static final String PERMISSION_MODIFY_SITE = "modifySitePermission";
    public static final String PERMISSION_DELETE_SITE = "deleteSitePermission";
    
    private HashMap<String, Boolean> permissions;

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The n id. */
    // Variables declarations
    private int _nId;

    /** The str artifact id. */
    @NotEmpty( message = "#i18n{releaser.validation.site.ArtifactId.notEmpty}" )
    @Size( max = 50, message = "#i18n{releaser.validation.site.ArtifactId.size}" )
    private String _strArtifactId;

    /** The n id cluster. */
    private int _nIdCluster;
    
    /** The str scm url. */
    @URL( message = "#i18n{portal.validation.message.url}" )
    @NotEmpty( message = "#i18n{releaser.validation.site.ScmUrl.notEmpty}" )
    @Size( max = 255, message = "#i18n{releaser.validation.site.ScmUrl.size}" )
    private String _strScmUrl;

    /** The str name. */
    @NotEmpty( message = "#i18n{releaser.validation.site.Name.notEmpty}" )
    @Size( max = 50, message = "#i18n{releaser.validation.site.Name.size}" )
    private String _strName;

    /** The str description. */
    @NotEmpty( message = "#i18n{releaser.validation.site.Description.notEmpty}" )
    @Size( max = 255, message = "#i18n{releaser.validation.site.Description.size}" )
    private String _strDescription;

    /** The str jira key. */
    @Size( max = 50, message = "#i18n{releaser.validation.site.JiraKey.size}" )
    private String _strJiraKey;

    /** The str tag information. */
    private String _strTagInformation;

    /** The str cluster. */
    private String _strCluster;

    /** The list current dependencies. */
    private List<Dependency> _listCurrentDependencies = new ArrayList<>( );
    
    /** The list components. */
    private List<Component> _listComponents = new ArrayList<>( );

    /** The str current version. */
    private String _strCurrentVersion;
    
    /** The str last release version. */
    private String _strLastReleaseVersion;
    
    /** The str next release version. */
    private String _strNextReleaseVersion;
    
    /** The list target versions. */
    private List<String> _listTargetVersions;
    
    /** The n target version index. */
    private int _nTargetVersionIndex;
    
    /** The str next snapshot version. */
    private String _strNextSnapshotVersion;
    
    /** The str group id. */
    private String _strGroupId;
    
    /** The b theme. */
    private boolean _bTheme;

    /**
     * Returns the Id.
     *
     * @return The Id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the Id.
     *
     * @param nId            The Id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the ArtifactId.
     *
     * @return The ArtifactId
     */
    public String getArtifactId( )
    {
        return _strArtifactId;
    }

    /**
     * Sets the ArtifactId.
     *
     * @param strArtifactId            The ArtifactId
     */
    public void setArtifactId( String strArtifactId )
    {
        _strArtifactId = strArtifactId;
    }

    /**
     * Returns the IdCluster.
     *
     * @return The IdCluster
     */
    public int getIdCluster( )
    {
        return _nIdCluster;
    }

    /**
     * Sets the IdCluster.
     *
     * @param nIdCluster            The IdCluster
     */
    public void setIdCluster( int nIdCluster )
    {
        _nIdCluster = nIdCluster;
    }

    /**
     * Returns the ScmUrl.
     *
     * @return The ScmUrl
     */
    public String getScmUrl( )
    {
        return _strScmUrl;
    }

    /**
     * Sets the ScmUrl.
     *
     * @param strScmUrl            The ScmUrl
     */
    public void setScmUrl( String strScmUrl )
    {
        _strScmUrl = strScmUrl;
    }

    /**
     * Returns the Name.
     *
     * @return The Name
     */
    public String getName( )
    {
        return _strName;
    }

    /**
     * Sets the Name.
     *
     * @param strName            The Name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the Description.
     *
     * @return The Description
     */
    public String getDescription( )
    {
        return _strDescription;
    }

    /**
     * Sets the Description.
     *
     * @param strDescription            The Description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Returns the JiraKey.
     *
     * @return The JiraKey
     */
    public String getJiraKey( )
    {
        return _strJiraKey;
    }

    /**
     * Sets the JiraKey.
     *
     * @param strJiraKey            The JiraKey
     */
    public void setJiraKey( String strJiraKey )
    {
        _strJiraKey = strJiraKey;
    }

    /**
     * Adds the current dependency.
     *
     * @param dependency the dependency
     */
    public void addCurrentDependency( Dependency dependency )
    {
        _listCurrentDependencies.add( dependency );
    }

    /**
     * Gets the current dependencies.
     *
     * @return getCurrentDependencies
     */
    public List<Dependency> getCurrentDependencies( )
    {
        return _listCurrentDependencies;
    }

    /**
     * Adds the component.
     *
     * @param component the component
     */
    public void addComponent( Component component )
    {
        _listComponents.add( component );
    }

    /**
     * Gets the components.
     *
     * @return getComponents
     */
    public List<Component> getComponents( )
    {
        return _listComponents;
    }

    /**
     * Returns the Version.
     *
     * @return The Version
     */
    public String getVersion( )
    {
        return _strCurrentVersion;
    }

    /**
     * Sets the Version.
     *
     * @param strVersion            The Version
     */
    public void setVersion( String strVersion )
    {
        _strCurrentVersion = strVersion;
    }

    /**
     * Returns the Cluster.
     *
     * @return The Cluster
     */
    public String getCluster( )
    {
        return _strCluster;
    }

    /**
     * Sets the Cluster.
     *
     * @param strCluster            The Cluster
     */
    public void setCluster( String strCluster )
    {
        _strCluster = strCluster;
    }

    /**
     * Returns the Last Release Version.
     *
     * @return The Release Version
     */
    public String getLastReleaseVersion( )
    {
        return _strLastReleaseVersion;
    }

    /**
     * Sets the Last Release Version.
     *
     * @param strLastReleaseVersion            The Release Version
     */
    public void setLastReleaseVersion( String strLastReleaseVersion )
    {
        _strLastReleaseVersion = strLastReleaseVersion;
    }

    /**
     * Returns the Next Release Version.
     *
     * @return The Release Version
     */
    public String getNextReleaseVersion( )
    {
        return _strNextReleaseVersion;
    }

    /**
     * Sets the Next Release Version.
     *
     * @param strNextReleaseVersion            The Release Version
     */
    public void setNextReleaseVersion( String strNextReleaseVersion )
    {
        _strNextReleaseVersion = strNextReleaseVersion;
    }

    /**
     * Returns the NextSnapshotVersion.
     *
     * @return The NextSnapshotVersion
     */
    public String getNextSnapshotVersion( )
    {
        return _strNextSnapshotVersion;
    }

    /**
     * Sets the NextSnapshotVersion.
     *
     * @param strNextSnapshotVersion            The NextSnapshotVersion
     */
    public void setNextSnapshotVersion( String strNextSnapshotVersion )
    {
        _strNextSnapshotVersion = strNextSnapshotVersion;
    }

    /**
     * Set target versions list.
     *
     * @param listTargetVersions            The target versions list
     */
    public void setTargetVersions( List<String> listTargetVersions )
    {
        _listTargetVersions = listTargetVersions;
    }

    /**
     * Gets the target versions list.
     *
     * @return the target versions list
     */
    public List<String> getTargetVersions( )
    {
        return _listTargetVersions;
    }

    /**
     * Set the target version index.
     *
     * @param nIndex            the target version index
     */
    public void setTargetVersionIndex( int nIndex )
    {
        _nTargetVersionIndex = nIndex;
    }

    /**
     * Get the target version index.
     *
     * @return the target version index
     */
    public int getTargetVersionIndex( )
    {
        return _nTargetVersionIndex;
    }

    /**
     * Gets the tag information.
     *
     * @return getTagInformation
     */
    public String getTagInformation( )
    {
        return _strTagInformation;
    }

    /**
     * Sets the tag information.
     *
     * @param _strTagInfotmation the new tag information
     */
    public void setTagInformation( String _strTagInfotmation )
    {
        this._strTagInformation = _strTagInfotmation;
    }

    /**
     * Gets the group id.
     *
     * @return getGroupId
     */
    public String getGroupId( )
    {
        return _strGroupId;
    }

    /**
     * Sets the group id.
     *
     * @param _strGroupId the new group id
     */
    public void setGroupId( String _strGroupId )
    {
        this._strGroupId = _strGroupId;
    }

    /**
     * Checks if is theme.
     *
     * @return isTheme
     */
    public boolean isTheme( )
    {
        return _bTheme;
    }

    /**
     * Sets the theme.
     *
     * @param _bTheme the new theme
     */
    public void setTheme( boolean _bTheme )
    {
        this._bTheme = _bTheme;
    }

    /**
     * Gets the target version.
     *
     * @return the target version
     */
    @Override
    public String getTargetVersion( )
    {
        // TODO Auto-generated method stub
        return getNextReleaseVersion( );
    }
    
    /**
    * RBAC resource implementation
    * @return The resource type code
    */
    @Override
    public String getResourceTypeCode(  )
    {
        return RESOURCE_TYPE;
    }

    /**
     * RBAC resource implementation
     * @return The resourceId
     */
    @Override
    public String getResourceId(  )
    {
        return String.valueOf( _nId );
    }

	public HashMap<String, Boolean> getPermissions() {
		return permissions;
	}

	public void setPermissions(HashMap<String, Boolean> permissions) {
		if (this.permissions == null)
			this.permissions = new HashMap<String, Boolean>();
		else
			this.permissions.clear();
		
		if (permissions != null) {
			this.permissions.putAll(permissions);
		}
	}


}
