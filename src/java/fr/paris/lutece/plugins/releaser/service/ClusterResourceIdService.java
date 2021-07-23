package fr.paris.lutece.plugins.releaser.service;

import java.util.List;
import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.Cluster;
import fr.paris.lutece.plugins.releaser.business.ClusterHome;
import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceList;

public class ClusterResourceIdService extends ResourceIdService
{
	private static final String PROPERTY_LABEL_RESOURCE_TYPE = "releaser.rbac.cluster.resourceType";
    private static final String PROPERTY_LABEL_ADD = "releaser.rbac.cluster.permission.add";
    private static final String PROPERTY_LABEL_MODIFY = "releaser.rbac.cluster.permission.modify";
    private static final String PROPERTY_LABEL_DELETE = "releaser.rbac.cluster.permission.delete";
    private static final String PROPERTY_LABEL_ADD_SITE_TO_CLUSTER = "releaser.rbac.cluster.permission.addSite";
    
    private static final String PLUGIN_NAME = "releaser";

    /** Permission for creating Cluster */
    public static final String PERMISSION_ADD = "ADD";

    /** Permission for creating Cluster */
    public static final String PERMISSION_ADD_SITE_TO_CLUSTER = "ADD_SITE_TO_CLUSTER";
    
    /** Permission for deleting Cluster */
    public static final String PERMISSION_DELETE = "DELETE";

    /** Permission for modifying Cluster */
    public static final String PERMISSION_MODIFY = "MODIFY";

    /** Creates a new instance of SuggestTypeResourceIdService */
    public ClusterResourceIdService(  )
    {
        setPluginName( PLUGIN_NAME );
    }

	@Override
	public void register() 
	{

		ResourceType rt = new ResourceType(  );
        rt.setResourceIdServiceClass( ClusterResourceIdService.class.getName(  ) );
        rt.setPluginName( PLUGIN_NAME );
        rt.setResourceTypeKey( Cluster.RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );
      
        Permission p;

        p = new Permission(  );
        p.setPermissionKey( PERMISSION_ADD );
        p.setPermissionTitleKey( PROPERTY_LABEL_ADD );
        rt.registerPermission( p );

        p = new Permission(  );
        p.setPermissionKey( PERMISSION_MODIFY );
        p.setPermissionTitleKey( PROPERTY_LABEL_MODIFY );
        rt.registerPermission( p );

        p = new Permission(  );
        p.setPermissionKey( PERMISSION_DELETE );
        p.setPermissionTitleKey( PROPERTY_LABEL_DELETE );
        rt.registerPermission( p );

        p = new Permission(  );
        p.setPermissionKey( PERMISSION_ADD_SITE_TO_CLUSTER );
        p.setPermissionTitleKey( PROPERTY_LABEL_ADD_SITE_TO_CLUSTER );
        rt.registerPermission( p );

        ResourceTypeManager.registerResourceType( rt );
		
	}

	@Override
	public ReferenceList getResourceIdList(Locale locale) {

		ReferenceList referenceListCluster = new ReferenceList(  );
        List<Cluster> listClusters = ClusterHome.getClustersList();

        for ( Cluster cluster : listClusters )
        {
            referenceListCluster.addItem( cluster.getId(), cluster.getName() );
        }
        
        return referenceListCluster;    
	}

	@Override
	public String getTitle(String strId, Locale locale) 
	{

        int nIdCluster = -1;

        try
        {
            nIdCluster = Integer.parseInt( strId );
        }
        catch ( NumberFormatException ne )
        {
            AppLogService.error( ne );
        }

        Cluster cluster = ClusterHome.findByPrimaryKey( nIdCluster );

        return cluster.getName();
	}

}
