package fr.paris.lutece.plugins.releaser.business;

public class ReleaserUser
{

    private String  _strSvnSiteAccountLogin;
    private String  _strSvnSiteAccountPassword;
    private String  _strSvnComponentAccountLogin;
    private String  _strSvnComponentAccountPassword;
    private String  _strGithubComponentAccountLogin;
    private String  _strGithubComponentAccountPassword;
   
    
    
    public String getSvnSiteAccountLogin( )
    {
        return _strSvnSiteAccountLogin;
    }

    public void setSvnSiteAccountLogin( String _strSvnSiteAccountLogin )
    {
        this._strSvnSiteAccountLogin = _strSvnSiteAccountLogin;
    }

    public String getSvnSiteAccountPassword( )
    {
        return _strSvnSiteAccountPassword;
    }

    public void setSvnSiteAccountPassword( String _strSvnSiteAccountPassword )
    {
        this._strSvnSiteAccountPassword = _strSvnSiteAccountPassword;
    }

    public String getSvnComponentAccountLogin( )
    {
        return _strSvnComponentAccountLogin;
    }

    public void setSvnComponentAccountLogin( String _strSvnComponentAccountLogin )
    {
        this._strSvnComponentAccountLogin = _strSvnComponentAccountLogin;
    }

    public String getSvnComponentAccountPassword( )
    {
        return _strSvnComponentAccountPassword;
    }

    public void setSvnComponentAccountPassword( String _strSvnComponentAccountPassword )
    {
        this._strSvnComponentAccountPassword = _strSvnComponentAccountPassword;
    }

    public String getGithubComponentAccountLogin( )
    {
        return _strGithubComponentAccountLogin;
    }

    public void setGithubComponentAccountLogin( String _strGithubComponentAccountLogin )
    {
        this._strGithubComponentAccountLogin = _strGithubComponentAccountLogin;
    }

    public String getGithubComponentAccountPassword( )
    {
        return _strGithubComponentAccountPassword;
    }

    public void setGithubComponentAccountPassword( String _strGithubComponentAccountPassword )
    {
        this._strGithubComponentAccountPassword = _strGithubComponentAccountPassword;
    }
    
    
     

}
