package fr.paris.lutece.plugins.releaser.service;

import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.plugins.releaser.business.Cluster;
import fr.paris.lutece.plugins.releaser.business.ClusterHome;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.SiteHome;
import fr.paris.lutece.portal.business.user.AdminUser;import fr.paris.lutece.portal.service.rbac.RBACService;

/**
 * ClusterService.
 */
public class ClusterService 
{
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
    public static List<Cluster> getClustersListWithPermissedSites( AdminUser adminUser)
    {
        List<Cluster> listCluster = ClusterHome.getClustersList( );
        List<Cluster> listClusterWithPermissedSites = new ArrayList<Cluster>( );
        
        for ( Cluster cluster : listCluster )
        {
			Cluster clusterWithPermissedList = cluster;			
			List<Site> listPermissedSites = new ArrayList<Site>( );
			
        	List<Site> listSite = SiteHome.findByCluster( cluster.getId( ) );
        	for ( Site site : listSite )
        	{        			
        		if ( RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId(), 
        				SiteResourceIdService.PERMISSION_VIEW, adminUser ) )
        		{
        			listPermissedSites.add( site );
        		}	             
        	}
        	
        	if ( listPermissedSites != null && !listPermissedSites.isEmpty( ) )
        	{
        		clusterWithPermissedList.setSites( listPermissedSites );
        		listClusterWithPermissedSites.add( clusterWithPermissedList );
        	}
        	
        }
        
        return listClusterWithPermissedSites;
    }

	
	
}
