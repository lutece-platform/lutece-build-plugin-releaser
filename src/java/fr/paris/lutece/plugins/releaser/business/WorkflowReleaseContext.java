package fr.paris.lutece.plugins.releaser.business;

import java.io.Serializable;
import java.util.Date;

import fr.paris.lutece.plugins.releaser.util.CommandResult;

public class WorkflowReleaseContext implements Serializable
{

    public static final String WORKFLOW_RESOURCE_TYPE = "WORKFLOW_RELEASE_CONTEXT";
   
    private static final long serialVersionUID = 8956577881980599537L;
    private int _nId;
    private Component _component;
    private Site _site;
    private CommandResult _commandResult;
    private String _strSvnUserLogin;
    private String _strSvnUserPassword;
    private String _strGitHubUserLogin;
    private String _strGitHubUserPassord;


 
    public int getId( )
    {
        return _nId;
    }

    public void setId( int _nId )
    {
        this._nId = _nId;
    }

    public Component getComponent( )
    {
        return _component;
    }

    public void setComponent( Component _component )
    {
        this._component = _component;
    }

    public CommandResult getCommandResult( )
    {
        return _commandResult;
    }

    public void setCommandResult( CommandResult _commandResult )
    {
        this._commandResult = _commandResult;
    }

    public String getSvnUserLogin( )
    {
        return _strSvnUserLogin;
    }

    public void setSvnUserLogin( String _strSvnUserLogin )
    {
        this._strSvnUserLogin = _strSvnUserLogin;
    }

    public String getSvnUserPassword( )
    {
        return _strSvnUserPassword;
    }

    public void setSvnUserPassword( String _strSvnUserPassword )
    {
        this._strSvnUserPassword = _strSvnUserPassword;
    }

    public String getGitHubUserLogin( )
    {
        return _strGitHubUserLogin;
    }

    public void setGitHubUserLogin( String _strGitHubUserLogin )
    {
        this._strGitHubUserLogin = _strGitHubUserLogin;
    }

    public String getGitHubUserPassord( )
    {
        return _strGitHubUserPassord;
    }

    public void setGitHubUserPassord( String _strGitHubUserPassord )
    {
        this._strGitHubUserPassord = _strGitHubUserPassord;
    }
    
    public void setSite(Site site)
    {
        _site=site;    
    }

    public Site getSite()
    {
        return _site;
    }
    
    /**
     * 
     * @return true if the component is a lutece site
     */
    public boolean isLuteceSite( )
    {
        return _component==null && _site!=null;
    }
    
    
    
}
