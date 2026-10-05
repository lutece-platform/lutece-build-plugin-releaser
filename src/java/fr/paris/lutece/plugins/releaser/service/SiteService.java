/*
 * Copyright (c) 2002-2021, City of Paris
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

import org.apache.commons.lang3.StringUtils;
import javax.servlet.http.HttpServletRequest;import javax.xml.bind.JAXBException;
import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser.Credential;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.business.jaxb.maven.Model;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.SiteHome;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.CVSFactoryService;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.git.GitUtils;
import fr.paris.lutece.plugins.releaser.util.pom.PomParser;
import fr.paris.lutece.plugins.releaser.util.pom.PomUpdater;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import fr.paris.lutece.plugins.releaser.util.version.VersionParsingException;
import fr.paris.lutece.plugins.releaser.util.version.VersionUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import java.io.File;
import org.eclipse.jgit.api.Git;

// TODO: Auto-generated Javadoc
/**
 * SiteService.
 */
public class SiteService
{

    /** The Constant MESSAGE_WRONG_POM_PARENT_SITE_VERSION. */
    private static final String MESSAGE_WRONG_POM_PARENT_SITE_VERSION = "releaser.message.wrongPomParentSiteVersion";

    /** Minimum pom parent version to create docker image. */
    public static String POM_PARENT_MIN_VERSION_TO_CREATE_DOCKET_IMAGE = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_POM_PARENT_MIN_VERSION_TO_CREATE_DOCKET_IMAGE );


    /**
     * Load a site from its id.
     *
     * @param nSiteId
     *            The site id
     * @param request
     *            the request
     * @param locale
     *            the locale
     * @return A site object
     */
    public static Site getSite( int nSiteId, HttpServletRequest request, Locale locale )
    {
        Site site = SiteHome.findByPrimaryKey( nSiteId );
        String strPom = null;
        ReleaserUser user = ReleaserUtils.getReleaserUser( request, locale );

        if ( user != null )
        {
            site.setBranchReleaseFrom( AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_BRANCH_DEFAULT ) );
            
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
     * @param site
     *            the site
     * @param request
     *            the request
     * @param locale
     *            the locale
     */
    private static void initSite( Site site, HttpServletRequest request, Locale locale )
    {
        ReleaserUser user = ReleaserUtils.getReleaserUser( request, locale );
        Credential credential = user.getCredential( site.getRepoType( ) );

        // Find last release in the repository (may be empty for a site never released : normalize to null)
        String strLastReleaseVersion = StringUtils.trimToNull( CVSFactoryService.getService( site.getRepoType( ) ).getLastRelease( site,
                credential.getLogin( ), credential.getPassword( ) ) );
        ReleasePreparationService.defineAggregateVersions( site, strLastReleaseVersion );

		site.setCreateDckerImage(isSiteCreateDockerImage( site ) );

        initComponents( site, user );
    }
    
    public static boolean isSiteCreateDockerImage( Site site )
    {
    	try 
        {
        	String strPomParentVersion = site.getParentVersion();
        	
            if ( strPomParentVersion != null)
            {
            	strPomParentVersion.replace("[", "").replace("]", "");
            	Version vPomParentVersion = Version.parse( strPomParentVersion );
    			
    			if (vPomParentVersion.getMajor() >= Integer.valueOf( POM_PARENT_MIN_VERSION_TO_CREATE_DOCKET_IMAGE ) )
    			{
    				return true;
    			}
            }
			
		} catch (VersionParsingException e) {
			AppLogService.error( e );
		}
    	
    	return false;
    }
        
    /**
     * Initialize the component list of a site : components to be released are those flagged as project in the Datastore.
     *
     * @param site
     *            The site
     * @param user
     *            The releaser user (credentials)
     */
    private static void initComponents( Site site, ReleaserUser user )
    {
        ReleasePreparationService.initComponents( site, user, component -> isProjectComponent( site, component.getArtifactId( ) ) );
    }

    /**
     * Checks if is project component.
     *
     * @param site
     *            the site
     * @param strArtifactId
     *            the str artifact id
     * @return true, if is project component
     */
    private static boolean isProjectComponent( Site site, String strArtifactId )
    {
        return new Boolean( DatastoreService.getDataValue( getComponetIsProjectDataKey( site, strArtifactId ), Boolean.FALSE.toString( ) ) );
    }

    /**
     * Update component as project status.
     *
     * @param site
     *            the site
     * @param strArtifactId
     *            the str artifact id
     * @param bIsProject
     *            the b is project
     */
    public static void updateComponentAsProjectStatus( Site site, String strArtifactId, Boolean bIsProject )
    {
        DatastoreService.setDataValue( getComponetIsProjectDataKey( site, strArtifactId ), bIsProject.toString( ) );

    }

    /**
     * Removes the component as project by site.
     *
     * @param nIdSite
     *            the n id site
     */
    public static void removeComponentAsProjectBySite( int nIdSite )
    {
        DatastoreService.removeDataByPrefix( getPrefixIsProjectDataKey( nIdSite ) );

    }

    /**
     * Gets the componet is project data key.
     *
     * @param site
     *            the site
     * @param strArtifactId
     *            the str artifact id
     * @return the componet is project data key
     */
    private static String getComponetIsProjectDataKey( Site site, String strArtifactId )
    {
        return getPrefixIsProjectDataKey( site.getId( ) ) + strArtifactId;
    }

    /**
     * Gets the prefix is project data key.
     *
     * @param nIdSite
     *            the n id site
     * @return the prefix is project data key
     */
    private static String getPrefixIsProjectDataKey( int nIdSite )
    {
        return ConstanteUtils.CONSTANTE_COMPONENT_PROJECT_PREFIX + "_" + nIdSite + "_";
    }

    /**
     * Build release comments for a given site.
     *
     * @param site
     *            The site
     * @param locale
     *            The locale to use for comments
     */
    public static void buildComments( Site site, Locale locale )
    {
        site.resetComments( );
        buildReleaseComments( site, locale );
        ReleasePreparationService.buildComponentsComments( site, locale );
    }

    /**
     * Upgrade component.
     *
     * @param site
     *            the site
     * @param strArtifactId
     *            the str artifact id
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
     * @param site
     *            the site
     * @param strArtifactId
     *            the str artifact id
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
     * @param site
     *            the site
     * @param strArtifactId
     *            the str artifact id
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
     * @param site
     *            the site
     * @param strArtifactId
     *            the str artifact id
     */
    public static void cancelDowngradeComponent( Site site, String strArtifactId )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) && component.isSnapshotVersion( ) )
            {
                component.setDowngrade( false );
                ReleasePreparationService.defineTargetVersion( component );
                ReleasePreparationService.defineNextSnapshotVersion( component );
            }
        }
    }

    /**
     * Release component.
     *
     * @param site
     *            the site
     * @param strArtifactId
     *            the str artifact id
     * @param locale
     *            the locale
     * @param user
     *            the user
     * @param request
     *            the request
     * @return the int
     */
    public static int releaseComponent( Site site, String strArtifactId, Locale locale, AdminUser user, HttpServletRequest request, boolean bForce )
    {
        for ( Component component : site.getComponents( ) )
        {
            if ( component.getArtifactId( ).equals( strArtifactId ) )
            {
                if ( component.shouldBeReleased( ) )
                {
                    // Release component
                    return ComponentService.getService( ).release( component, locale, user, request );
                }
                // Confirmed Git/Nexus divergence on a non merge-back branch : the release is otherwise valid, force it.
                if ( bForce && ComponentService.getService( ).isReleaseConfirmationRequiredByDivergence( component ) )
                {
                    return ComponentService.getService( ).release( component, locale, user, request, true );
                }
                break;
            }
        }
        return ConstanteUtils.CONSTANTE_ID_NULL;
    }

    /**
     * Release site.
     *
     * @param site
     *            the site
     * @param locale
     *            the locale
     * @param user
     *            the user
     * @param request
     *            the request
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
     * @param site
     *            The site
     * @param strArtifactId
     *            The component artifact id
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
                    ReleasePreparationService.refreshProjectComponent( component );
                }

            }
        }
    }

    /**
     * Change the next release version.
     *
     * @param site
     *            The site
     * @param strArtifactId
     *            The component artifact id
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
     * @param site
     *            The site
     */
    public static void changeNextReleaseVersion( Site site )
    {
        List<String> listTargetVersions = site.getTargetVersions( );
        if ( listTargetVersions == null || listTargetVersions.isEmpty( ) )
        {
            return;
        }
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
     * @param site
     *            the site
     * @param locale
     *            the locale
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

    public static List<Site> getAuthorizedSites( int clusterId, AdminUser adminUser )
    {
        List<Site> listAuthorizedSites = new ArrayList<Site>( );
        List<Site> listSite = SiteHome.findByCluster( clusterId );

        // Assign site's permissions
        for ( Site site : listSite )
        {
            boolean bAutoriseViewSite = false;

            HashMap<String, Boolean> sitePermissions = new HashMap<String, Boolean>( );

            // Release site permission
            if ( RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId( ), SiteResourceIdService.PERMISSION_RELEASE, adminUser ) )
            {
                sitePermissions.put( Site.PERMISSION_RELEASE_SITE, true );
                bAutoriseViewSite = true;
            }
            else
            {
                sitePermissions.put( Site.PERMISSION_RELEASE_SITE, false );
            }

            // Modify site permission
            if ( RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId( ), SiteResourceIdService.PERMISSION_MODIFY, adminUser ) )
            {
                sitePermissions.put( Site.PERMISSION_MODIFY_SITE, true );
                bAutoriseViewSite = true;
            }
            else
            {
                sitePermissions.put( Site.PERMISSION_MODIFY_SITE, false );
            }

            // Delete site permission
            if ( RBACService.isAuthorized( Site.RESOURCE_TYPE, site.getResourceId( ), SiteResourceIdService.PERMISSION_DELETE, adminUser ) )
            {
                sitePermissions.put( Site.PERMISSION_DELETE_SITE, true );
                bAutoriseViewSite = true;
            }
            else
            {
                sitePermissions.put( Site.PERMISSION_DELETE_SITE, false );
            }

            // Set permissions
            if ( bAutoriseViewSite )
            {
                // Add permissions to the site
                site.setPermissions( sitePermissions );

                // Add the site to list of Authorized sites
                listAuthorizedSites.add( site );
            }
        }

        return listAuthorizedSites;
    }

    public static boolean IsUserAuthorized( AdminUser adminUser, String siteId, String permission )
    {
        boolean bAuthorized = false;

        if ( RBACService.isAuthorized( Site.RESOURCE_TYPE, siteId, permission, adminUser ) )
        {
            bAuthorized = true;
        }

        return bAuthorized;
    }

    public static boolean IsSiteAlreadyExist( String siteName, String artifactId, String scmUrl )
    {
        String clusterName = SiteHome.findDuplicateSite( siteName, artifactId, scmUrl );
        if ( clusterName != null )
            return true;

        return false;
    }

    /**
     * Gets the branch list for a site repository.
     *
     * @param site
     *            the site
     * @param user
     *            the releaser user
     * @return the site with branch list populated
     */
    public static Site getSiteBranchList( Site site, ReleaserUser user )
    {
        Credential credential = user.getCredential( site.getRepoType( ) );
        String strLogin = credential.getLogin( );
        String strPwd = credential.getPassword( );

        CommandResult commandResult = new CommandResult( );
        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        commandResult.setLog( new StringBuffer( ) );
        context.setCommandResult( commandResult );
        context.setSite( site );

        String strLocalPath = ReleaserUtils.getLocalPath( context );
        String strRepoUrl = GitUtils.getRepoUrl( site.getScmUrl( ) );
        File fLocalRepo = new File( strLocalPath );

        if ( fLocalRepo.exists( ) )
        {
            if ( !fr.paris.lutece.plugins.releaser.util.file.FileUtils.delete( fLocalRepo, commandResult.getLog( ) ) )
            {
                commandResult.setError( commandResult.getLog( ).toString( ) );
            }
        }

        List<String> branchNameList = GitUtils.getBranchList( strRepoUrl, fLocalRepo, commandResult, strLogin, strPwd );
        branchNameList.remove( "master" );

        site.setBranches( branchNameList );

        return site;
    }

    /**
     * Changes the site branch and reloads version information and components from the new branch POM.
     *
     * @param site
     *            the site
     * @param strBranchName
     *            the target branch name
     * @param user
     *            the releaser user
     * @param request
     *            the request
     * @param locale
     *            the locale
     * @return the site with updated version info and components
     */
    public static Site changeSiteBranch( Site site, String strBranchName, ReleaserUser user, HttpServletRequest request, Locale locale )
    {
        // Update branch list: add back current branch, remove new one
        if ( site.getBranches( ) != null )
        {
            if ( !site.getBranches( ).contains( site.getBranchReleaseFrom( ) ) )
            {
                site.getBranches( ).add( site.getBranchReleaseFrom( ) );
            }
            site.getBranches( ).remove( strBranchName );
        }

        site.setBranchReleaseFrom( strBranchName );

        CommandResult commandResult = new CommandResult( );
        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        commandResult.setLog( new StringBuffer( ) );
        context.setCommandResult( commandResult );
        context.setSite( site );

        String strLocalPath = ReleaserUtils.getLocalPath( context );
        File fLocalRepo = new File( strLocalPath );

        try
        {
            Git git = Git.open( fLocalRepo );

            // Checkout the new branch
            GitUtils.createLocalBranch( git, strBranchName, commandResult );
            GitUtils.checkoutRepoBranch( git, strBranchName, commandResult );

            // Read POM from new branch
            String strPom = fr.paris.lutece.plugins.releaser.util.file.FileUtils.readFile( ReleaserUtils.getLocalPomPath( context ) );

            if ( strPom != null )
            {
                // Clear existing dependencies and components before re-parsing
                site.getCurrentDependencies( ).clear( );
                site.getComponents( ).clear( );

                // Re-parse POM to get new version and dependencies
                PomParser parser = new PomParser( );
                parser.parse( site, strPom );

                // Recalculate last release from stored tags, filtered by the new branch's POM major
                String strLastReleaseVersion = null;
                try
                {
                    int nMajor = Version.parse( site.getVersion( ) ).getMajor( );
                    strLastReleaseVersion = VersionUtils.getLastVersionUsingMajor( site.getTags( ), nMajor );
                }
                catch ( VersionParsingException e )
                {
                    AppLogService.error( "Error parsing site version : " + e.getMessage( ), e );
                }
                ReleasePreparationService.defineAggregateVersions( site, strLastReleaseVersion );
                site.setCreateDckerImage( isSiteCreateDockerImage( site ) );

                // Re-initialize components from new POM dependencies
                initComponents( site, user );
            }
        }
        catch( IOException e )
        {
            AppLogService.error( e.getMessage( ), e );
        }

        return site;
    }

}
