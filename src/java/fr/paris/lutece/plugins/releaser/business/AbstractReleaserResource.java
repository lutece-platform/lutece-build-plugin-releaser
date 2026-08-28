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
     * Reset comments
     */
    public void resetComments( )
    {
        _listReleaseComments.clear( );
    }

}
