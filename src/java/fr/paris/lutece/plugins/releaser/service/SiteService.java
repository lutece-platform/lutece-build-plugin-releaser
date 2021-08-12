/*
 * Copyright (c) 2002-2017, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */

package fr.paris.lutece.plugins.releaser.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser.Credential;
import fr.paris.lutece.plugins.releaser.business.jaxb.maven.Model;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.SiteHome;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.CVSFactoryService;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.pom.PomParser;
import fr.paris.lutece.plugins.releaser.util.pom.PomUpdater;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import fr.paris.lutece.plugins.releaser.util.version.VersionParsingException;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

// TODO: Auto-generated Javadoc
/**
 * SiteService.
 */
public class SiteService
{
    
    /** The Constant NB_POOL_REMOTE_INFORMATION. */
    private static final int NB_POOL_REMOTE_INFORMATION = 60;
    
    /** The Constant MESSAGE_AVOID_SNAPSHOT. */
    private static final String MESSAGE_AVOID_SNAPSHOT = "releaser.message.avoidSnapshot";
    
    /** The Constant MESSAGE_UPGRADE_SELECTED. */
    private static final String MESSAGE_UPGRADE_SELECTED = "releaser.message.upgradeSelected";
    
    /** The Constant MESSAGE_TO_BE_RELEASED. */
    private static final String MESSAGE_TO_BE_RELEASED = "releaser.message.toBeReleased";
    
    /** The Constant MESSAGE_MORE_RECENT_VERSION_AVAILABLE. */
    private static final String MESSAGE_MORE_RECENT_VERSION_AVAILABLE = "releaser.message.moreRecentVersionAvailable";
    
    /** The Constant MESSAGE_AN_RELEASE_VERSION_ALREADY_EXIST. */
    private static final String MESSAGE_AN_RELEASE_VERSION_ALREADY_EXIST = "releaser.message.releleaseVersionAlreadyExist";
    
    /** The Constant MESSAGE_WRONG_POM_PARENT_SITE_VERSION. */
    private static final String MESSAGE_WRONG_POM_PARENT_SITE_VERSION = "releaser.message.wrongPomParentSiteVersion";

    /**
     * Load a site from its id.
     *
     * @param nSiteId            The site id
     * @param request the request
     * @param locale the locale
     * @return A site object
     */
    public static Site getSite( int nSiteId, HttpServletRequest request, Locale locale )
    {
        Site site = SiteHome.findByPrimaryKey( nSiteId );
        String strPom = null;
        ReleaserUser user = ReleaserUtils.getReleaserUser( request, locale );

        if ( user != null )
        {
            Credential credential = user.getCredential( site.getRepoType( ) );

            strPom = CVSFactoryService.getService( site.getRepoType( ) ).fetchPom( site, credential.getLogin( ), credential.getPassword( ) );

            if ( strPom != null )
            {
                PomParser parser = new PomParser( );
                parser.parse( site, strPom );
                initSite( site, request, locale );
            }
        }
        else
        {
            throw new AppException( ConstanteUtils.ERROR_TYPE_AUTHENTICATION_ERROR );
        }

        return site;
    }

    /**
     * Inits the site.
     *
     * @param site the site
     * @param request the request
     * @param locale the locale
     */
    private static void initSite( Site site, HttpServletRequest request, Locale locale )
    {

        ReleaserUser user = ReleaserUtils.getReleaserUser( request, locale );
        Credential credential = user.getCredential( site.getRepoType( ) );

        // Find last release in the repository
        String strLastReleaseVersion = CVSFactoryService.getService( site.getRepoType( ) ).getLastRelease( site, credential.getLogin( ),
                credential.getPassword( ) );
        site.setLastReleaseVersion( strLastReleaseVersion );

        // To find next releases

        String strOriginVersion = getOriginVersion( strLastReleaseVersion, site.getVersion( ) );

        site.setNextReleaseVersion( Version.getReleaseVersion( strOriginVersion ) );
        site.setNextSnapshotVersion( Version.getNextSnapshotVersion( strOriginVersion ) );
        site.setTargetVersions( Version.getNextReleaseVersions( strOriginVersion ) );

        initComponents( site );
    }

    /**
     * Define which version between last released or current snapshot should be the origin for next release versions. Ex of cases :<br>
     * last release : 3.2.1 current : 4.0.0-SNAPSHOT -- current  <br>
     * last release : 3.2.1 current : 3.2.2-SNAPSHOT -- last or current <br>
     * last release : missing current : 1.0.0-SNAPSHOT -- current <br>
     * last release : 3.2.1-RC-02 current : 3.2.1-SNAPSHOT -- last <br>
     * 
     * @param strLastRelease
     *            The last release
     * @param strCurrentVersion
     *            The current release
     * @return The origin version
     */
    public static String getOriginVersion( String strLastRelease, String strCurrentVersion )
    {
        String strOriginVersion = strCurrentVersion;
        if ( ( strLastRelease != null ) && Version.isCandidate( strLastRelease ) )
        {
            strOriginVersion = strLastRelease;
        }
        return strOriginVersion;
    }

    /**
     * Initialize the component list for a given site.
     *
     * @param site            The site
     */
    private static void initComponents( Site site )
    {
        for ( Dependency dependency : site.getCurrentDependencies( ) )
        {
            Component component = new Component( );

            component.setIsProject( isProjectComponent( site, dependency.getArtifactId( ) ) );
            component.setArtifactId( dependency.getArtifactId( ) );
            component.setGroupId( dependency.getGroupId( ) );
            component.setType( dependency.getType( ) );
            String currentVersion = dependency.getVersion( ).replace( "[", "" ).replace( "]", "" );
            component.setCurrentVersion( currentVersion );
            site.addComponent( component );
        }

        ExecutorService executor = Executors.newFixedThreadPool( NB_POOL_REMOTE_INFORMATION );

        List<Future> futures = new ArrayList<Future>( site.getCurrentDependencies( ).size( ) );

        for ( Component component : site.getComponents( ) )
        {
            futures.add( executor.submit( new GetRemoteInformationsTask( component ) ) );
        }

        // wait all futures stop before continue
        for ( Future future : futures )
        {

            try
            {
                future.get( );
            }
            catch( InterruptedException e )
            {
                AppLogService.error( e );
            }
            catch( ExecutionException e )
            {
                // TODO Auto-generated catch block
                AppLogService.error( e );
            }

        }

        executor.shutdown( );

        for ( Component component : site.getComponents( ) )
        {

            ComponentService.getService( ).updateRemoteInformations( component );
            defineTargetVersion( component );
            defineNextSnapshotVersion( component );
            component.setName( ReleaserUtils.getComponentName( component.getScmDeveloperConnection( ), component.getArtifactId( ) ) );
        }

    }

    /**
     * Define the target version for a given component : <br>
     * - current version for non project component <br>
     * - nex release for project component.
     *
     * @param component            The component
     */
    private static void defineTargetVersion( Component component )
    {
        if ( component.isProject( ) && component.isSnapshotVersion( ) )
        {
            if ( component.getLastAvailableVersion( ) != null && !component.getCurrentVersion( ).equals( component.getLastAvailableSnapshotVersion( ) )
                    || component.isTheme( ) )
            {
                component.setTargetVersion( component.getLastAvailableVersion( ) );
            }
            else
            {
                component.setTargetVersions( Version.getNextReleaseVersions( component.getCurrentVersion( ) ) );
                String strTargetVersion = Version.getReleaseVersion( component.getCurrentVersion( ) );
                component.setTargetVersion( strTargetVersion );
            }
        }
        else
        {
            component.setTargetVersion( component.getCurrentVersion( ) );
        }
    }

    /**
     * Define the next snapshot version for a given component.
     *
     * @param component            The component
     */
    private static void defineNextSnapshotVersion( Component component )
    {
        String strNextSnapshotVersion = Version.NOT_AVAILABLE;
        if ( !component.getCurrentVersion( ).equals( component.getLastAvailableSnapshotVersion( ) ) || component.isTheme( ) )
        {
            component.setNextSnapshotVersion( component.getLastAvailableSnapshotVersion( ) );
        }
        else
        {
            try
            {
                Version version = Version.parse( component.getTargetVersion( ) );
                boolean bSnapshot = true;
                strNextSnapshotVersion = version.nextPatch( bSnapshot ).toString( );
            }
            catch( VersionParsingException ex )
            {
                AppLogService.error( "Error parsing version for component " + component.getArtifactId( ) + " : " + ex.getMessage( ), ex );

            }
            component.setNextSnapshotVersion( strNextSnapshotVersion );
        }

    }

    /**
     * Checks if is project component.
     *
     * @param site the site
     * @param strArtifactId the str artifact id
     * @return true, if is project component
     */
    private static boolean isProjectComponent( Site site, String strArtifactId )
    {
        return new Boolean( DatastoreService.getDataValue( getComponetIsProjectDataKey( site, strArtifactId ), Boolean.FALSE.toString( ) ) );
    }

    /**
     * Update component as project status.
     *
     * @param site the site
     * @param strArtifactId the str artifact id
     * @param bIsProject the b is project
     */
    public static void updateComponentAsProjectStatus( Site site, String strArtifactId, Boolean bIsProject )
    {
        DatastoreService.setDataValue( getComponetIsProjectDataKey( site, strArtifactId ), bIsProject.toString( ) );

    }

    /**
     * Removes the component as project by site.
     *
     * @param nIdSite the n id site
     */
    public static void removeComponentAsProjectBySite( int nIdSite )
    {
        DatastoreService.removeDataByPrefix( getPrefixIsProjectDataKey( nIdSite ) );

    }

    /**
     * Gets the componet is project data key.
     *
     * @param site the site
     * @param strArtifactId the str artifact id
     * @return the componet is project data key
     */
    private static String getComponetIsProjectDataKey( Site site, String strArtifactId )
    {

        return getPrefixIsProjectDataKey( site.getId( ) ) + strArtifactId;

    }

    /**
     * Gets the prefix is project data key.
     *
     * @param nIdSite the n id site
     * @return the prefix is project data key
     */
    private static String getPrefixIsProjectDataKey( int nIdSite )
    {

        return ConstanteUtils.CONSTANTE_COMPONENT_PROJECT_PREFIX + "_" + nIdSite + "_";

    }

    /**
     * Build release comments for a given site.
     *
     * @param site            The site
     * @param locale            The locale to use for comments
     */
    public static void buildComments( Site site, Locale locale )
    {
        site.resetComments( );
        buildReleaseComments( site, locale );

        for ( Component component : site.getComponents( ) )
        {
            component.resetComments( );
            buildReleaseComments( component, locale );
        }
    }

    /**
     * Build release comments for a given component.
     *
     * @param component            The component
     * @param locale            The locale to use for comments
     */
    private static void buildReleaseComments( Component component, Locale locale )
    {

        if ( !component.isProject( ) )
        {
            if ( Version.isSnapshot( component.getTargetVersion( ) ) )
            {
                String strComment = I18nService.getLocalizedString( MESSAGE_AVOID_SNAPSHOT, locale );
                component.addReleaseComment( strComment );
            }
            else
                if ( component.getLastAvailableVersion( ) != null  
                    && component.getTargetVersion( )!=null 
                    && component.getLastAvailableVersion( )!=null
                        && ReleaserUtils.compareVersion( component.getTargetVersion( ), component.getLastAvailableVersion( ) ) < 0 )
                {
                    String [ ] arguments = {
                            component.getLastAvailableVersion( )
                    };
                    String strComment = I18nService.getLocalizedString( MESSAGE_MORE_RECENT_VERSION_AVAILABLE, arguments, locale );

                    component.addReleaseComment( strComment );
                }
        }
        else
        {
            if ( component.isSnapshotVersion( ) )
            {
                if ( ReleaserUtils.compareVersion( component.getCurrentVersion( ), component.getLastAvailableSnapshotVersion( ) ) < 0 )
                {

                    String [ ] arguments = {
                            component.getLastAvailableVersion( )
                    };
                    String strComment = I18nService.getLocalizedString( MESSAGE_UPGRADE_SELECTED, arguments, locale );
                    component.addReleaseComment( strComment );
                }
                else
                    if ( !component.shouldBeReleased( ) && !component.isDowngrade( ) )
                    {

                        String [ ] arguments = {
                                component.getLastAvailableVersion( )
                        };
                        String strComment = I18nService.getLocalizedString( MESSAGE_AN_RELEASE_VERSION_ALREADY_EXIST, arguments, locale );
                        component.addReleaseComment( strComment );
                    }

                    else
                        if ( component.shouldBeReleased( ) )
                        {

                            String strComment = I18nService.getLocalizedString( MESSAGE_TO_BE_RELEASED, locale );
                            component.addReleaseComment( strComment );
                        }
            }
            else
                if ( ReleaserUtils.compareVersion( component.getCurrentVersion( ), component.getLastAvailableVersion( ) ) < 0 )
                {
                    String [ ] arguments = {
                            component.getLastAvailableVersion( )
                    };
                    String strComment = I18nService.getLocalizedString( MESSAGE_MORE_RECENT_VERSION_AVAILABLE, arguments, locale );

                    component.addReleaseComment( strComment );
                }
        }
    }

    /**
     * Upgrade component.
     *
     * @param site the site
     * @param strArtifactId the str artifact id
     */
    public static void upgradeComponent( Site site, String strArtifactId )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) )
            {
                component.setTargetVersion( component.getLastAvailableVersion( ) );
                component.setUpgrade( true );
            }
        }
    }

    /**
     * Cancel upgrade component.
     *
     * @param site the site
     * @param strArtifactId the str artifact id
     */
    public static void cancelUpgradeComponent( Site site, String strArtifactId )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) )
            {

                component.setTargetVersion( component.getCurrentVersion( ) );
                component.setUpgrade( false );
            }
        }
    }

    /**
     * Downgrade component.
     *
     * @param site the site
     * @param strArtifactId the str artifact id
     */
    public static void downgradeComponent( Site site, String strArtifactId )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) && component.isSnapshotVersion( ) )
            {
                component.setTargetVersion( component.getLastAvailableVersion( ) );
                component.setNextSnapshotVersion( component.getLastAvailableSnapshotVersion( ) );
                component.setDowngrade( true );
            }
        }
    }

    /**
     * Cancel downgrade component.
     *
     * @param site the site
     * @param strArtifactId the str artifact id
     */
    public static void cancelDowngradeComponent( Site site, String strArtifactId )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) && component.isSnapshotVersion( ) )
            {
                component.setDowngrade( false );
                defineTargetVersion( component );
                defineNextSnapshotVersion( component );
            }
        }
    }

    /**
     * Release component.
     *
     * @param site the site
     * @param strArtifactId the str artifact id
     * @param locale the locale
     * @param user the user
     * @param request the request
     * @return the int
     */
    public static int releaseComponent( Site site, String strArtifactId, Locale locale, AdminUser user, HttpServletRequest request )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) && component.shouldBeReleased( ) )
            {
                // Release component
                return ComponentService.getService( ).release( component, locale, user, request );

            }
        }
        return ConstanteUtils.CONSTANTE_ID_NULL;
    }

    /**
     * Release site.
     *
     * @param site the site
     * @param locale the locale
     * @param user the user
     * @param request the request
     * @return the map
     */
    public static Map<String, Integer> releaseSite( Site site, Locale locale, AdminUser user, HttpServletRequest request )
    {
        Map<String, Integer> mapResultContext = new HashMap<String, Integer>( );

        Integer nIdWfContext;
        // Release all snapshot compnent
        for ( Component component : site.getComponents( ) )
        {
            if ( component.isProject( ) && component.shouldBeReleased( ) && !component.isTheme( ) )
            {
                component.setErrorLastRelease( false );
                nIdWfContext = ComponentService.getService( ).release( component, locale, user, request );
                mapResultContext.put( component.getArtifactId( ), nIdWfContext );

            }
        }

        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        context.setSite( site );
        context.setReleaserUser( ReleaserUtils.getReleaserUser( request, locale ) );

        int nIdWorkflow = WorkflowReleaseContextService.getService( ).getIdWorkflow( context );
        WorkflowReleaseContextService.getService( ).addWorkflowReleaseContext( context );
        // start
        WorkflowReleaseContextService.getService( ).startWorkflowReleaseContext( context, nIdWorkflow, locale, request, user );
        // Add wf site context
        mapResultContext.put( site.getArtifactId( ), context.getId( ) );

        return mapResultContext;

    }

    /**
     * Add or Remove a component from the project's components list.
     *
     * @param site            The site
     * @param strArtifactId            The component artifact id
     */
    public static void toggleProjectComponent( Site site, String strArtifactId )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) )
            {
                component.setIsProject( !component.isProject( ) );
                updateComponentAsProjectStatus( site, strArtifactId, component.isProject( ) );

                if ( component.isProject( ) )
                {
                    try
                    {
                        ComponentService.getService( ).setRemoteInformations( component, false );
                    }
                    catch( HttpAccessException | IOException e )
                    {
                        AppLogService.error( e );
                    }
                    ComponentService.getService( ).updateRemoteInformations( component );
                    defineTargetVersion( component );
                    defineNextSnapshotVersion( component );
                    component.setName( ReleaserUtils.getComponentName( component.getScmDeveloperConnection( ), component.getArtifactId( ) ) );
                }

            }
        }
    }

    /**
     * Change the next release version.
     *
     * @param site            The site
     * @param strArtifactId            The component artifact id
     */
    public static void changeNextReleaseVersion( Site site, String strArtifactId )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) )
            {
                ComponentService.getService( ).changeNextReleaseVersion( component );
            }
        }
    }

    /**
     * Change the next release version.
     *
     * @param site            The site
     */
    public static void changeNextReleaseVersion( Site site )
    {
        List<String> listTargetVersions = site.getTargetVersions( );
        int nNewIndex = ( site.getTargetVersionIndex( ) + 1 ) % listTargetVersions.size( );
        String strTargetVersion = listTargetVersions.get( nNewIndex );
        site.setNextReleaseVersion( strTargetVersion );
        site.setTargetVersionIndex( nNewIndex );
        site.setNextSnapshotVersion( Version.getNextSnapshotVersion( strTargetVersion ) );
    }

    /**
     * Generate the pom.xml file for a given site
     * 
     * @param site
     *            The site
     * @return The pom.xml content
     */
    public String generateTargetPOM( Site site )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); // To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Builds the release comments.
     *
     * @param site the site
     * @param locale the locale
     */
    private static void buildReleaseComments( Site site, Locale locale )
    {

        if ( !site.isTheme( ) )
        {
            // Check pom
            InputStream inputStream = null;
            String strPomPath = ReleaserUtils.getLocalSitePomPath( site );
            String strPomParentReferenceVersion = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_POM_PARENT_SITE_VERSION );
            try
            {

                inputStream = new FileInputStream( strPomPath );
                Model model = PomUpdater.unmarshal( Model.class, inputStream );
                String strParentSiteVersion = model.getParent( ).getVersion( );
                if ( ReleaserUtils.compareVersion( strParentSiteVersion, strPomParentReferenceVersion ) < 0 )
                {
                    String [ ] arguments = {
                            strPomParentReferenceVersion
                    };
                    String strComment = I18nService.getLocalizedString( MESSAGE_WRONG_POM_PARENT_SITE_VERSION, arguments, locale );
                    site.addReleaseComment( strComment );
                }

            }
            catch( FileNotFoundException e )
            {
                AppLogService.error( e );
            }
            catch( JAXBException e )
            {
                // TODO Auto-generated catch block
                AppLogService.error( e );
            }
        }

    }
    
    public static List<Site> getAuthorizedSites( int clusterId, AdminUser adminUser)
    {

    	List<Site> listAuthorizedSites = new ArrayList<Site>( );
		
    	List<Site> listSite = SiteHome.findByCluster( clusterId );
    	
		// Assign site's permissions
    	for ( Site site : listSite )
    	{        			
    		boolean bAutoriseViewSite = false;

            HashMap<String, Boolean> sitePermissions = new HashMap<String, Boolean>( );
			
			// Release site permission
			if (RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId(), 
    				SiteResourceIdService.PERMISSION_RELEASE, adminUser ))
	        {
		        sitePermissions.put(Site.PERMISSION_RELEASE_SITE, true);
		        bAutoriseViewSite = true;
	        }
			else 
			{
				sitePermissions.put(Site.PERMISSION_RELEASE_SITE, false);
			}
        
			// Modify site permission
	        if (RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId(), 
    				SiteResourceIdService.PERMISSION_MODIFY, adminUser ))
	        {
	        	sitePermissions.put(Site.PERMISSION_MODIFY_SITE, true);
		        bAutoriseViewSite = true;
	        }
			else 
			{
				sitePermissions.put(Site.PERMISSION_MODIFY_SITE, false);
			}
	        	        
	        // Delete site permission
	        if (RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId(), 
    				SiteResourceIdService.PERMISSION_DELETE, adminUser ))
	        {
	        	sitePermissions.put(Site.PERMISSION_DELETE_SITE, true);
		        bAutoriseViewSite = true;
	        } 
			else 
			{
				sitePermissions.put(Site.PERMISSION_DELETE_SITE, false);
			}   
	        
	        
	        // Set permissions
	        if (bAutoriseViewSite)
	        {
    	        // Add permissions to the site
    	        site.setPermissions( sitePermissions );
    	        
    	        // Add the site to list of Authorized sites
    			listAuthorizedSites.add( site );
	        }    			     
    	}
    	
    	return listAuthorizedSites;
    }   

    public static boolean IsUserAuthorized (AdminUser adminUser, String siteId, String permission)
    {
    	
    	boolean bAuthorized = false;
    	
    	if ( RBACService.isAuthorized( Site.RESOURCE_TYPE, siteId, permission, adminUser ) )
        {
    		bAuthorized = true;
        }  
    	
    	return bAuthorized;
    }
    
    public static boolean IsSiteAlreadyExist ( String siteName, String artifactId, String scmUrl )
    {
    	String clusterName = SiteHome.findDuplicateSite( siteName, artifactId, scmUrl );
    	if (clusterName != null)
    		return true;    	
    	
    	return false;
    }

}
