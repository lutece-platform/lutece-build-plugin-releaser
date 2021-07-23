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

	/**
     * Load the list of sites into each cluster object and returns the list of clusters
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
     * Load the list of sites into each cluster object and returns the list of clusters
     * 
     * @return the list which contains the data of all the cluster objects
     */
    public static List<Cluster> getUserClusters( AdminUser adminUser)
    {
        List<Cluster> listCluster = ClusterHome.getClustersList( );
        List<Cluster> listAuthorizedClusters = new ArrayList<Cluster>( );
        
        for ( Cluster cluster : listCluster )
        {   			
    		HashMap<String, Boolean> clusterPermissions = new HashMap<String, Boolean>( );
    		boolean bAuthoriseViewCluster = false;
    		   	        
   			// Add site to the cluster permission
   	        if (RBACService.isAuthorized( Cluster.RESOURCE_TYPE, cluster.getResourceId(), 
       				ClusterResourceIdService.PERMISSION_ADD_SITE_TO_CLUSTER, adminUser ))
   	        {
   	        	clusterPermissions.put(Cluster.PERMISSION_ADD_SITES_TO_CLUSTER, true);
   	        	bAuthoriseViewCluster = true;
   	        }
   			else 
   			{
   				clusterPermissions.put(Cluster.PERMISSION_ADD_SITES_TO_CLUSTER, false);
   			}
       	                        
			// Modify cluster permission
	        if (RBACService.isAuthorized( Cluster.RESOURCE_TYPE, cluster.getResourceId(), 
    				ClusterResourceIdService.PERMISSION_MODIFY, adminUser ))
	        {
	        	clusterPermissions.put(Cluster.PERMISSION_MODIFY_CLUSTER, true);
	        	bAuthoriseViewCluster = true;
	        }
			else 
			{
				clusterPermissions.put(Cluster.PERMISSION_MODIFY_CLUSTER, false);
			}
	                	        
	        // Delete cluster permission
	        if (RBACService.isAuthorized( Cluster.RESOURCE_TYPE, cluster.getResourceId(), 
    				ClusterResourceIdService.PERMISSION_DELETE, adminUser ))
	        {
	        	clusterPermissions.put(Cluster.PERMISSION_DELETE_CLUSTER, true);
	        	bAuthoriseViewCluster = true;
	        } 
			else 
			{
				clusterPermissions.put(Cluster.PERMISSION_DELETE_CLUSTER, false);
			}   
	        
	        // Add permissions to the cluster 
	        cluster.setPermissions( clusterPermissions );
	        
	        // Add autorized sites
	        List<Site> listAuthorizedSites = SiteService.getAuthorizedSites( cluster.getId( ), adminUser);
		 
        	if ( listAuthorizedSites != null )
        	{
        		cluster.setSites( listAuthorizedSites );
        		
        		if ( !listAuthorizedSites.isEmpty() )
            	{
            		bAuthoriseViewCluster = true;
            	}   
        	}    	        
	            
        	if (bAuthoriseViewCluster)
    		listAuthorizedClusters.add( cluster );
		}
	    
        return listAuthorizedClusters;
    }	

    public static boolean IsAddClusterAuthorized (AdminUser adminUser)
    {
    	
        if ( RBACService.isAuthorized( new Cluster(), ClusterResourceIdService.PERMISSION_ADD, adminUser ) )
        {
        	return true;
        }	        
    	
    	return false;
    }

    public static boolean IsUserAuthorized (AdminUser adminUser, String clusterId, String permission)
    {
    	
    	boolean bAuthorized = false;
    	
    	if ( RBACService.isAuthorized( Cluster.RESOURCE_TYPE, clusterId, permission, adminUser ) )
        {
    		bAuthorized = true;
        }  
    	
    	return bAuthorized;
    }
}
