package fr.paris.lutece.plugins.releaser.service;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.util.CommandResult;


/**
 * IJiraService.
 */
public interface IJiraService
{

	/**
	 * Inits the.
	 */
    public abstract void init( );
	
	/**
	 * Update component versions.
	 *
	 * @param component comment
	 * @param commandResult commandResult
	 */
    public abstract void updateComponentVersions( Component component, CommandResult commandResult );

}
