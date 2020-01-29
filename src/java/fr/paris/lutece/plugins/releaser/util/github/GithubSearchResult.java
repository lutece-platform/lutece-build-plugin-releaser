package fr.paris.lutece.plugins.releaser.util.github;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

// TODO: Auto-generated Javadoc
/**
 * The Class GithubSearchResult.
 */
public class GithubSearchResult
{

    /** The b total count. */
    private Integer _bTotalCount;

    /** The list repo item. */
    private List<GithubSearchRepoItem> _listRepoItem;

    /**
     * Gets the list repo item.
     *
     * @return the list repo item
     */
    @JsonProperty( "items" )
    public List<GithubSearchRepoItem> getListRepoItem( )
    {
        return _listRepoItem;
    }

    /**
     * Sets the list repo item.
     *
     * @param listRepoItem the new list repo item
     */
    @JsonProperty( "items" )
    public void setListRepoItem( List<GithubSearchRepoItem> listRepoItem )
    {
        this._listRepoItem = listRepoItem;
    }

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    @JsonProperty( "total_count" )
    public Integer getTotalCount( )
    {
        return _bTotalCount;
    }

    /**
     * Sets the total count.
     *
     * @param bTotalCount the new total count
     */
    @JsonProperty( "total_count" )
    public void setTotalCount( Integer bTotalCount )
    {
        this._bTotalCount = bTotalCount;
    }

}
