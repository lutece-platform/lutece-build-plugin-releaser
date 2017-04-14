package fr.paris.lutece.plugins.releaser.business;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;

public class ReleaserUser
{

    private String  _strSvnSiteAccountLogin;
    private String  _strSvnSiteAccountPassword;
    private String  _strSvnComponentAccountLogin;
    private String  _strSvnComponentAccountPassword;
    @NotEmpty( message = "#i18n{releaser.validation.releaseruser.GithubComponentAccountLogin.notEmpty}" )
    private String  _strGithubComponentAccountLogin;
    @NotEmpty( message = "#i18n{releaser.validation.releaseruser.GithubComponentAccountPassword.notEmpty}" )
    private String  _strGithubComponentAccountPassword;
   
    
    
    public String getSvnSiteAccountLogin( )
    {
        return _strSvnSiteAccountLogin;
    }

    public void setSvnSiteAccountLogin( String _strSvnSiteAccountLogin )
    {
        this._strSvnSiteAccountLogin = _strSvnSiteAccountLogin;
    }
    @JsonIgnore
    public String getSvnSiteAccountPassword( )
    {
        return _strSvnSiteAccountPassword;
    }
    @JsonIgnore
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

    @JsonIgnore
    public String getSvnComponentAccountPassword( )
    {
        return _strSvnComponentAccountPassword;
    }
    @JsonIgnore
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
    
    @JsonIgnore
    public String getGithubComponentAccountPassword( )
    {
        return _strGithubComponentAccountPassword;
    }
    @JsonIgnore
    public void setGithubComponentAccountPassword( String _strGithubComponentAccountPassword )
    {
        this._strGithubComponentAccountPassword = _strGithubComponentAccountPassword;
    }
    
    
     

}
