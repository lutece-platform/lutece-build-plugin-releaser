package fr.paris.lutece.plugins.releaser.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.paris.lutece.plugins.releaser.business.Cluster;
import fr.paris.lutece.plugins.releaser.business.ClusterHome;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.SiteHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.rbac.RBACService;

/**
 * ClusterService.
 */
public class ClusterService 
{
	// Permissions
    private static final String PERMISSION_ADD_SITE = "addSitePermission";
    private static final String PERMISSION_VIEW_SITE = "viewSitePermission";
    private static final String PERMISSION_MODIFY_SITE = "modifySitePermission";
    private static final String PERMISSION_DELETE_SITE = "deleteSitePermission";

    
	/**
     * Load the liste of sites into each cluster object and returns the list of clusters
     * 
     * @return the list which contains the data of all the cluster objects
     */
    public static List<Cluster> getClustersListWithSites( AdminUser adminUser)
    {
        List<Cluster> listCluster = ClusterHome.getClustersList( );
        
        for ( Cluster cluster : listCluster )
        {
        	List<Site> listSite = SiteHome.findByCluster( cluster.getId( ) );
        	for ( Site site : listSite )
        	{
    			cluster.getSites().add( site );        			             
        	}        	
        }
        
        return listCluster;
    }
    
    /**
     * Load the liste of sites into each cluster object and returns the list of clusters
     * 
     * @return the list which contains the data of all the cluster objects
     */
    public static List<Cluster> getClustersListWithAuthorizedSites( AdminUser adminUser)
    {
        List<Cluster> listCluster = ClusterHome.getClustersList( );
        List<Cluster> listClusterWithAuthorizedSites = new ArrayList<Cluster>( );
        HashMap<String, Boolean> sitePermissions = new HashMap<String, Boolean>( );
        
        for ( Cluster cluster : listCluster )
        {
			Cluster clusterWithAuthorizedList = cluster;			
			List<Site> listAuthorizedSites = new ArrayList<Site>( );
			
        	List<Site> listSite = SiteHome.findByCluster( cluster.getId( ) );
        	for ( Site site : listSite )
        	{        			
        		if ( RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId(), 
        				SiteResourceIdService.PERMISSION_VIEW, adminUser ) )
        		{
        			sitePermissions.clear();
        			
        			// Add site's permissions
        			sitePermissions.put(PERMISSION_VIEW_SITE, true);
        			
        			if (RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId(), 
            				SiteResourceIdService.PERMISSION_ADD, adminUser ))
        	        {
        		        sitePermissions.put(PERMISSION_ADD_SITE, true);
        	        }
        			else 
        			{
        				sitePermissions.put(PERMISSION_ADD_SITE, false);
        			}
                
        	        if (RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId(), 
            				SiteResourceIdService.PERMISSION_MODIFY, adminUser ))
        	        {
        	        	sitePermissions.put(PERMISSION_MODIFY_SITE, true);
        	        }
        			else 
        			{
        				sitePermissions.put(PERMISSION_MODIFY_SITE, false);
        			}
        	        
        	        if (RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId(), 
            				SiteResourceIdService.PERMISSION_DELETE, adminUser ))
        	        {
        	        	sitePermissions.put(PERMISSION_DELETE_SITE, true);
        	        } 
        			else 
        			{
        				sitePermissions.put(PERMISSION_DELETE_SITE, false);
        			}   
        	        
        	        // Add permissions to the site
        	        site.setPermissions( sitePermissions );
        	        
        	        // Add the site to list of Authorized sites
        			listAuthorizedSites.add( site );
        		}	     
        	}
        	
        	if ( listAuthorizedSites != null && !listAuthorizedSites.isEmpty( ) )
        	{
        		clusterWithAuthorizedList.setSites( listAuthorizedSites );
        		listClusterWithAuthorizedSites.add( clusterWithAuthorizedList );
        		
        	}        	
        }
        
        return listClusterWithAuthorizedSites;
    }

	
	
}
