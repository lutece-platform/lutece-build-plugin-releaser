package fr.paris.lutece.plugins.releaser.service;

import java.util.List;
import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.SiteHome;
import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceList;

/**
*
* class SiteResourceIdService
*
*/
public final class SiteResourceIdService extends ResourceIdService
{
	private static final String PROPERTY_LABEL_RESOURCE_TYPE = "releaser.rbac.site.resourceType";
    private static final String PROPERTY_LABEL_CREATE = "releaser.rbac.site.permission.create";
    private static final String PROPERTY_LABEL_VIEW = "releaser.rbac.site.permission.view";
    private static final String PROPERTY_LABEL_DELETE = "releaser.rbac.site.permission.delete";
     
    private static final String PLUGIN_NAME = "releaser";

    /** Permission for creating site */
    public static final String PERMISSION_ADD = "ADD";

    /** Permission for viewing site */
    public static final String PERMISSION_VIEW = "VIEW";

    /** Permission for deleting site */
    public static final String PERMISSION_DELETE = "DELETE";

    /** Permission for modifying site */
    public static final String PERMISSION_MODIFY = "MODIFY";

    /** Creates a new instance of SuggestTypeResourceIdService */
    public SiteResourceIdService(  )
    {
        setPluginName( PLUGIN_NAME );
    }

	@Override
	public void register() {
		ResourceType rt = new ResourceType(  );
        rt.setResourceIdServiceClass( SiteResourceIdService.class.getName(  ) );
        rt.setPluginName( PLUGIN_NAME );
        rt.setResourceTypeKey( Site.RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );
      
        Permission p = new Permission(  );
        p.setPermissionKey( PERMISSION_ADD );
        p.setPermissionTitleKey( PROPERTY_LABEL_CREATE );
        rt.registerPermission( p );

        p = new Permission(  );
        p.setPermissionKey( PERMISSION_VIEW );
        p.setPermissionTitleKey( PROPERTY_LABEL_VIEW );
        rt.registerPermission( p );

        p = new Permission(  );
        p.setPermissionKey( PERMISSION_DELETE );
        p.setPermissionTitleKey( PROPERTY_LABEL_DELETE );
        rt.registerPermission( p );

        ResourceTypeManager.registerResourceType( rt );		
	}

        
	@Override
	public ReferenceList getResourceIdList(Locale local) {
		
		ReferenceList referenceListSite = new ReferenceList(  );
        List<Site> listSites = SiteHome.getSitesList();

        for ( Site site : listSites )
        {
            referenceListSite.addItem( site.getId(), site.getName() );
        }
        
        return referenceListSite;       
	}

	@Override
	public String getTitle( String strId, Locale locale ) 
	{

        int nIdSite = -1;

        try
        {
            nIdSite = Integer.parseInt( strId );
        }
        catch ( NumberFormatException ne )
        {
            AppLogService.error( ne );
        }

        Site site = SiteHome.findByPrimaryKey( nIdSite );

        return site.getName();
	}

}
