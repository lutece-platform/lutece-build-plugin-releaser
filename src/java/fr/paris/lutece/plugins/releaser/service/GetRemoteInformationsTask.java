package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

/**
 * GetRemoteInformationsTask
 *
 */
public class GetRemoteInformationsTask implements Runnable
{

    private Component _component;

    /**
     * @param component
     */
    public GetRemoteInformationsTask( Component component )
    {

        this._component = component;
    }

    @Override
    public void run( )
    {
        try
        {
            ComponentService.getService( ).setRemoteInformations( _component, _component.isProject( ) ? false : true );
        }
        catch( HttpAccessException | IOException e )
        {
            AppLogService.error( e );
        }

    }

}
