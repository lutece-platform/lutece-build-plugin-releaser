package fr.paris.lutece.plugins.releaser.util.github;

import org.codehaus.jackson.annotate.JsonProperty;

public class GithubSearchRepoItem
{

   
    private String _strName;
    private String _strCloneUrl;
    private String _strContentsUrl;
    private String _strFullName;
    
    
    
    
    public String getName( )
    {
        return _strName;
    }
   
    public void setName( String _strName )
    {
        this._strName = _strName;
    }
    @JsonProperty("clone_url")
    public String getCloneUrl( )
    {
        return _strCloneUrl;
    }
    @JsonProperty("clone_url")
    public void setCloneUrl( String _strCloneUrl )
    {
        this._strCloneUrl = _strCloneUrl;
    }

    @JsonProperty("contents_url")
    public String getContentsUrl( )
    {
        return _strContentsUrl;
    }

    @JsonProperty("contents_url")
    public void setContentsUrl( String _strContentsUrl )
    {
        this._strContentsUrl = _strContentsUrl;
    }

    
    @JsonProperty("full_name")
    public String getFullName( )
    {
        return _strFullName;
    }
    @JsonProperty("full_name")
    public void setFullName( String _strFullName )
    {
        this._strFullName = _strFullName;
    }
     

}
