package fr.paris.lutece.plugins.releaser.service;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.util.CommandResult;

public interface IJiraService
{

    public abstract void init( );

    public abstract void updateComponentVersions( Component component, CommandResult commandResult );

}
