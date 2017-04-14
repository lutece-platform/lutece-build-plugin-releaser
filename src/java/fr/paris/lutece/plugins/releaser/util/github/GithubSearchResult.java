package fr.paris.lutece.plugins.releaser.util.github;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class GithubSearchResult
{

    private Integer _bTotalCount;
    
    private List<GithubSearchRepoItem> _listRepoItem;
    
    
    @JsonProperty("items")
    public List<GithubSearchRepoItem> getListRepoItem( )
    {
        return _listRepoItem;
    }
    @JsonProperty("items")
    public void setListRepoItem( List<GithubSearchRepoItem> listRepoItem )
    {
        this._listRepoItem = listRepoItem;
    }
    @JsonProperty("total_count")
    public Integer getTotalCount( )
    {
        return _bTotalCount;
    }
    @JsonProperty("total_count")
    public void setTotalCount( Integer bTotalCount )
    {
        this._bTotalCount = bTotalCount;
    } 
    
    
}
