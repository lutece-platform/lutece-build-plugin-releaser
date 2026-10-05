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
package fr.paris.lutece.plugins.releaser.business;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * AbstractReleaserResource
 *
 */
public abstract class AbstractReleaserResource implements IReleaserResource
{

    private List<String> _listReleaseComments = new ArrayList<>( );

    /** Blocking anomaly message : survives comment resets. */
    private String _strBlockingReleaseComment;

    /** Parent POM of the resource as declared on its branch */
    private String _strPomParentGroupId;
    private String _strPomParentArtifactId;
    private String _strPomParentVersion;

    /** Last version of the parent POM, set only when newer than the declared one */
    private String _strLatestPomParentVersion;

    /** Parent POM version chosen for the release, null to keep the declared one */
    private String _strTargetPomParentVersion;

    /**
     * Returns the groupId of the parent POM declared on the branch.
     *
     * @return the parent groupId, null when unknown
     */
    public String getPomParentGroupId( )
    {
        return _strPomParentGroupId;
    }

    /**
     * Sets the groupId of the parent POM declared on the branch.
     *
     * @param strPomParentGroupId
     *            the parent groupId
     */
    public void setPomParentGroupId( String strPomParentGroupId )
    {
        _strPomParentGroupId = strPomParentGroupId;
    }

    /**
     * Returns the artifactId of the parent POM declared on the branch.
     *
     * @return the parent artifactId, null when unknown
     */
    public String getPomParentArtifactId( )
    {
        return _strPomParentArtifactId;
    }

    /**
     * Sets the artifactId of the parent POM declared on the branch.
     *
     * @param strPomParentArtifactId
     *            the parent artifactId
     */
    public void setPomParentArtifactId( String strPomParentArtifactId )
    {
        _strPomParentArtifactId = strPomParentArtifactId;
    }

    /**
     * Returns the version of the parent POM declared on the branch.
     *
     * @return the parent version, null when unknown
     */
    public String getPomParentVersion( )
    {
        return _strPomParentVersion;
    }

    /**
     * Sets the version of the parent POM declared on the branch.
     *
     * @param strPomParentVersion
     *            the parent version
     */
    public void setPomParentVersion( String strPomParentVersion )
    {
        _strPomParentVersion = strPomParentVersion;
    }

    /**
     * Returns the last version of the parent POM, when newer than the declared one.
     *
     * @return the proposed parent version, null when the declared one is up to date or unknown
     */
    public String getLatestPomParentVersion( )
    {
        return _strLatestPomParentVersion;
    }

    /**
     * Sets the last version of the parent POM.
     *
     * @param strLatestPomParentVersion
     *            the proposed parent version
     */
    public void setLatestPomParentVersion( String strLatestPomParentVersion )
    {
        _strLatestPomParentVersion = strLatestPomParentVersion;
    }

    /**
     * Returns the parent POM version chosen for the release.
     *
     * @return the chosen version, null to keep the declared one
     */
    public String getTargetPomParentVersion( )
    {
        return _strTargetPomParentVersion;
    }

    /**
     * Sets the parent POM version chosen for the release.
     *
     * @param strTargetPomParentVersion
     *            the chosen version, null to keep the declared one
     */
    public void setTargetPomParentVersion( String strTargetPomParentVersion )
    {
        _strTargetPomParentVersion = strTargetPomParentVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepositoryType getRepoType( )
    {

        if ( !StringUtils.isEmpty( getScmUrl( ) ) )
        {
            if ( getScmUrl( ).contains( "https://github." ) )
            {
                return RepositoryType.GITHUB;
            }
            if ( getScmUrl( ).contains( "gitlab" ) )
            {
                return RepositoryType.GITLAB;
            }
        }
        // Unsupported repository or missing scm.
        return null;

    }

    /**
     * Returns the release comments list
     *
     * @return The release comments
     */
    public List<String> getReleaseComments( )
    {
        return _listReleaseComments;
    }

    /**
     * Returns the ReleaseComment
     *
     * @return The ReleaseComment
     */
    public String getReleaseComment( )
    {
        return _listReleaseComments.isEmpty( ) ? null : String.join( "<br>\n", _listReleaseComments );
    }

    /**
     * Sets the ReleaseComment
     *
     * @param strReleaseComment
     *            The ReleaseComment
     */
    public void addReleaseComment( String strReleaseComment )
    {
        _listReleaseComments.add( strReleaseComment );
    }

    /**
     * Reset comments. The blocking comment, if any, is kept.
     */
    public void resetComments( )
    {
        _listReleaseComments.clear( );
        if ( _strBlockingReleaseComment != null )
        {
            _listReleaseComments.add( _strBlockingReleaseComment );
        }
    }

    /**
     * Sets the blocking release comment and adds it to the displayed comments.
     *
     * @param strComment
     *            the blocking comment
     */
    public void setBlockingReleaseComment( String strComment )
    {
        _strBlockingReleaseComment = strComment;
        if ( strComment != null && !_listReleaseComments.contains( strComment ) )
        {
            _listReleaseComments.add( strComment );
        }
    }

    /**
     * Returns the blocking release comment.
     *
     * @return the blocking comment, or null if none
     */
    public String getBlockingReleaseComment( )
    {
        return _strBlockingReleaseComment;
    }

}
