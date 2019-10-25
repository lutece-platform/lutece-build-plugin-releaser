package fr.paris.lutece.plugins.releaser.business;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import fr.paris.lutece.plugins.releaser.util.CommandResult;
@JsonIgnoreProperties({"releaserUser"})
public class WorkflowReleaseContext implements Serializable
{

    public static final String WORKFLOW_RESOURCE_TYPE = "WORKFLOW_RELEASE_CONTEXT";
   
    private static final long serialVersionUID = 8956577881980599537L;
    private int _nId;
    private Component _component;
    private Site _site;
    private CommandResult _commandResult;
    @JsonIgnore
    private ReleaserUser _releaserUser;
    private String _strRefBranchRelease;
    private String _strRefBranchDev;
    
    
 
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
    
    @JsonIgnore
    public ReleaserUser getReleaserUser( )
    {
        return _releaserUser;
    }
    @JsonIgnore
    public void setReleaserUser( ReleaserUser _releaserUser )
    {
        this._releaserUser = _releaserUser;
    }

    public String getRefBranchRelease( )
    {
        return _strRefBranchRelease;
    }

    public void setRefBranchRelease( String _strRefBranchRelease )
    {
        this._strRefBranchRelease = _strRefBranchRelease;
    }

    public String getRefBranchDev( )
    {
        return _strRefBranchDev;
    }

    public void setRefBranchDev( String _strRefBranchDev )
    {
        this._strRefBranchDev = _strRefBranchDev;
    }

  
   public  IReleaserResource getReleaserResource()
   {
       
       if(this.getSite()!=null)
       {
           return this.getSite();
           
       }
        return this.getComponent( );
   }
    
}
