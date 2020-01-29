package fr.paris.lutece.plugins.releaser.business;

import org.apache.commons.lang.StringUtils;

/**
 * AbstractReleaserResource
 *
 */
public abstract class AbstractReleaserResource implements IReleaserResource
{

    private String _strReleaseComment;

    /** {@inheritDoc}
	 */ 
    @Override
    public RepositoryType getRepoType( )
    {

        RepositoryType repositoryType = null;
        if ( !StringUtils.isEmpty( getScmUrl( ) ) )
        {
            if ( getScmUrl( ).endsWith( ".git" ) )
            {

                if ( getScmUrl( ).contains( "https://github." ) )
                {
                    repositoryType = RepositoryType.GITHUB;

                }
                else
                {
                    repositoryType = RepositoryType.GITLAB;
                }

            }
            else
            {
                repositoryType = RepositoryType.SVN;

            }

        }
        return repositoryType;

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

    /**
     * Reset comments
     */
    public void resetComments( )
    {
        _strReleaseComment = null;
    }

}
