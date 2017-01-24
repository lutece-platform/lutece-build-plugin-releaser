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

import javax.validation.constraints.*;
import org.hibernate.validator.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the business class for the object Site
 */
public class Site implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations
    private int _nId;

    @NotEmpty( message = "#i18n{releaser.validation.site.ArtifactId.notEmpty}" )
    @Size( max = 50, message = "#i18n{releaser.validation.site.ArtifactId.size}" )
    private String _strArtifactId;

    private int _nIdCluster;
    @URL( message = "#i18n{portal.validation.message.url}" )
    @NotEmpty( message = "#i18n{releaser.validation.site.ScmUrl.notEmpty}" )
    @Size( max = 255, message = "#i18n{releaser.validation.site.ScmUrl.size}" )
    private String _strScmUrl;

    @NotEmpty( message = "#i18n{releaser.validation.site.Name.notEmpty}" )
    @Size( max = 50, message = "#i18n{releaser.validation.site.Name.size}" )
    private String _strName;

    @NotEmpty( message = "#i18n{releaser.validation.site.Description.notEmpty}" )
    @Size( max = 255, message = "#i18n{releaser.validation.site.Description.size}" )
    private String _strDescription;

    @Size( max = 50, message = "#i18n{releaser.validation.site.JiraKey.size}" )
    private String _strJiraKey;

    private String _strVersion;
    private String _strCluster;

    private List<Dependency> _listCurrentDependencies = new ArrayList<>( );
    private List<Component> _listComponents = new ArrayList<>( );

    /**
     * Returns the Id
     * 
     * @return The Id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the Id
     * 
     * @param nId
     *            The Id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

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
     * Returns the IdCluster
     * 
     * @return The IdCluster
     */
    public int getIdCluster( )
    {
        return _nIdCluster;
    }

    /**
     * Sets the IdCluster
     * 
     * @param nIdCluster
     *            The IdCluster
     */
    public void setIdCluster( int nIdCluster )
    {
        _nIdCluster = nIdCluster;
    }

    /**
     * Returns the ScmUrl
     * 
     * @return The ScmUrl
     */
    public String getScmUrl( )
    {
        return _strScmUrl;
    }

    /**
     * Sets the ScmUrl
     * 
     * @param strScmUrl
     *            The ScmUrl
     */
    public void setScmUrl( String strScmUrl )
    {
        _strScmUrl = strScmUrl;
    }

    /**
     * Returns the Name
     * 
     * @return The Name
     */
    public String getName( )
    {
        return _strName;
    }

    /**
     * Sets the Name
     * 
     * @param strName
     *            The Name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the Description
     * 
     * @return The Description
     */
    public String getDescription( )
    {
        return _strDescription;
    }

    /**
     * Sets the Description
     * 
     * @param strDescription
     *            The Description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Returns the JiraKey
     * 
     * @return The JiraKey
     */
    public String getJiraKey( )
    {
        return _strJiraKey;
    }

    /**
     * Sets the JiraKey
     * 
     * @param strJiraKey
     *            The JiraKey
     */
    public void setJiraKey( String strJiraKey )
    {
        _strJiraKey = strJiraKey;
    }

    public void addCurrentDependency( Dependency dependency )
    {
        _listCurrentDependencies.add( dependency );
    }

    public List<Dependency> getCurrentDependencies( )
    {
        return _listCurrentDependencies;
    }

    public void addComponent( Component component )
    {
        _listComponents.add( component );
    }

    public List<Component> getComponents( )
    {
        return _listComponents;
    }

    /**
     * Returns the Version
     * 
     * @return The Version
     */
    public String getVersion( )
    {
        return _strVersion;
    }

    /**
     * Sets the Version
     * 
     * @param strVersion
     *            The Version
     */
    public void setVersion( String strVersion )
    {
        _strVersion = strVersion;
    }

    /**
     * Returns the Cluster
     * 
     * @return The Cluster
     */
    public String getCluster( )
    {
        return _strCluster;
    }

    /**
     * Sets the Cluster
     * 
     * @param strCluster
     *            The Cluster
     */
    public void setCluster( String strCluster )
    {
        _strCluster = strCluster;
    }

}
