package fr.paris.lutece.plugins.releaser.util.github;

import org.codehaus.jackson.annotate.JsonProperty;

// TODO: Auto-generated Javadoc
/**
 * The Class GithubSearchRepoItem.
 */
public class GithubSearchRepoItem
{

    /** The str name. */
    private String _strName;
    
    /** The str clone url. */
    private String _strCloneUrl;
    
    /** The str contents url. */
    private String _strContentsUrl;
    
    /** The str full name. */
    private String _strFullName;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName( )
    {
        return _strName;
    }

    /**
     * Sets the name.
     *
     * @param _strName the new name
     */
    public void setName( String _strName )
    {
        this._strName = _strName;
    }

    /**
     * Gets the clone url.
     *
     * @return the clone url
     */
    @JsonProperty( "clone_url" )
    public String getCloneUrl( )
    {
        return _strCloneUrl;
    }

    /**
     * Sets the clone url.
     *
     * @param _strCloneUrl the new clone url
     */
    @JsonProperty( "clone_url" )
    public void setCloneUrl( String _strCloneUrl )
    {
        this._strCloneUrl = _strCloneUrl;
    }

    /**
     * Gets the contents url.
     *
     * @return the contents url
     */
    @JsonProperty( "contents_url" )
    public String getContentsUrl( )
    {
        return _strContentsUrl;
    }

    /**
     * Sets the contents url.
     *
     * @param _strContentsUrl the new contents url
     */
    @JsonProperty( "contents_url" )
    public void setContentsUrl( String _strContentsUrl )
    {
        this._strContentsUrl = _strContentsUrl;
    }

    /**
     * Gets the full name.
     *
     * @return the full name
     */
    @JsonProperty( "full_name" )
    public String getFullName( )
    {
        return _strFullName;
    }

    /**
     * Sets the full name.
     *
     * @param _strFullName the new full name
     */
    @JsonProperty( "full_name" )
    public void setFullName( String _strFullName )
    {
        this._strFullName = _strFullName;
    }

}
