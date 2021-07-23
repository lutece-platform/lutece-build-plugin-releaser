package fr.paris.lutece.plugins.releaser.service;

import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.util.ReferenceList;

public class ComponentResourceIdService extends ResourceIdService
{
	private static final String PROPERTY_LABEL_RESOURCE_TYPE = "releaser.rbac.component.resourceType";
    private static final String PROPERTY_LABEL_SEARCH = "releaser.rbac.component.permission.search";
    
    private static final String PLUGIN_NAME = "releaser";

    /** Permission for search Component to release */
    public static final String PERMISSION_SEARCH = "SEARCH";

    /** Creates a new instance of SuggestTypeResourceIdService */
    public ComponentResourceIdService(  )
    {
        setPluginName( PLUGIN_NAME );
    }

	@Override
	public void register() 
	{

		ResourceType rt = new ResourceType(  );
        rt.setResourceIdServiceClass( ComponentResourceIdService.class.getName(  ) );
        rt.setPluginName( PLUGIN_NAME );
        rt.setResourceTypeKey( Component.RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );
      
        Permission p;

        p = new Permission(  );
        p.setPermissionKey( PERMISSION_SEARCH );
        p.setPermissionTitleKey( PROPERTY_LABEL_SEARCH );
        rt.registerPermission( p );

        ResourceTypeManager.registerResourceType( rt );
	}

	@Override
	public ReferenceList getResourceIdList(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle(String strId, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}


}
