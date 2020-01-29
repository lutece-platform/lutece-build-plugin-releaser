package fr.paris.lutece.plugins.releaser.business;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import fr.paris.lutece.plugins.releaser.util.CommandResult;

/**
 * WorkflowReleaseContext
 *
 */

@JsonIgnoreProperties( {
        "releaserUser"
} )
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

    /**
     * @return get Id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * @param _nId
     */
    /**
     * @param _nId
     */
    public void setId( int _nId )
    {
        this._nId = _nId;
    }

    /**
     * @return componet
     */
    public Component getComponent( )
    {
        return _component;
    }

    /**
     * @param _component
     */
    public void setComponent( Component _component )
    {
        this._component = _component;
    }

    /**
     * @return command result
     */
    public CommandResult getCommandResult( )
    {
        return _commandResult;
    }

    /**
     * @param _commandResult command result
     */
    public void setCommandResult( CommandResult _commandResult )
    {
        this._commandResult = _commandResult;
    }

    /**
     * @param site
     */
    public void setSite( Site site )
    {
        _site = site;
    }

    /**
     * @return get Site
     */
    public Site getSite( )
    {
        return _site;
    }

    /**
     * 
     * @return true if the component is a lutece site
     */
    public boolean isLuteceSite( )
    {
        return _component == null && _site != null;
    }

    /**
     * @return getReleaserUser
     */
    @JsonIgnore
    public ReleaserUser getReleaserUser( )
    {
        return _releaserUser;
    }

    /**
     * @param _releaserUser
     */
    @JsonIgnore
    public void setReleaserUser( ReleaserUser _releaserUser )
    {
        this._releaserUser = _releaserUser;
    }

    /**
     * @return getRefBranchRelease
     */
    public String getRefBranchRelease( )
    {
        return _strRefBranchRelease;
    }

    /**
     * @param _strRefBranchRelease
     */
    public void setRefBranchRelease( String _strRefBranchRelease )
    {
        this._strRefBranchRelease = _strRefBranchRelease;
    }

    /**
     * @return getRefBranchDev
     */
    public String getRefBranchDev( )
    {
        return _strRefBranchDev;
    }

    /**
     * @param _strRefBranchDev
     */
    public void setRefBranchDev( String _strRefBranchDev )
    {
        this._strRefBranchDev = _strRefBranchDev;
    }

    /**
     * @return getReleaserResource
     */
    public IReleaserResource getReleaserResource( )
    {

        if ( this.getSite( ) != null )
        {
            return this.getSite( );

        }
        return this.getComponent( );
    }

}
