
package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.maven.MavenUtils;
import fr.paris.lutece.plugins.releaser.util.pom.PomParser;
import fr.paris.lutece.plugins.releaser.util.pom.SaxPomHandler;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import fr.paris.lutece.plugins.releaser.util.version.VersionParsingException;
import fr.paris.lutece.plugins.releaser.util.version.VersionUtils;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

/**
 * Version Service
 */
public final class MavenRepoComponentInfoProvider
{
    // Maven repos URLs
	private static final String PROPERTY_MAVEN_URL_PREFIX = "releaser.maven.repository.url";
	private static final List<String> PROPERTIES_MAVEN_URL = AppPropertiesService.getKeys( PROPERTY_MAVEN_URL_PREFIX );
		
	// Maven Releases Paths
	private static final String PROPERTY_RELEASES_PATH_PREFIX = "releaser.maven.repository.releases";
	private static final List<String> PROPERTIES_RELEASES_PATH = AppPropertiesService.getKeys( PROPERTY_RELEASES_PATH_PREFIX );
	
	// Maven Sanapshots Paths
    private static final String PROPERTY_SNAPSHOTS_PATH_PREFIX = "releaser.maven.repository.snapshots";
	private static final List<String> PROPERTIES_SNAPSHOTS_PATH = AppPropertiesService.getKeys( PROPERTY_SNAPSHOTS_PATH_PREFIX );

	// Maven path - to see the tree structure
    private static final String PROPERTY_MAVEN_REPO_PATH_TREE = "releaser.maven.repository.treestructure.path";
	private static final String PATH_MAVEN_REPO_TREE = AppPropertiesService.getProperty( PROPERTY_MAVEN_REPO_PATH_TREE );

    // Maven path - To get file
	private static final String PROPERTY_MAVEN_REPO_PATH_FILE = "releaser.maven.repository.getfile.path";
    private static final String PATH_MAVEN_REPO_FILE = AppPropertiesService.getProperty( PROPERTY_MAVEN_REPO_PATH_FILE );	

    private static final String RELEASE_NOT_FOUND = "Release not found";
    private static final String SNAPSHOT_NOT_FOUND = "Snapshot not found";
    private static final String EXCEPTION_MESSAGE = "LuteceTools - MavenRepoService : Error retrieving pom infos : ";
    private static final String KIND_RELEASES = "releases";
    private static final String KIND_SNAPSHOTS = "snapshots";

    private static MavenRepoComponentInfoProvider _instance;

    /**
     * Private constructor
     */
    private MavenRepoComponentInfoProvider( )
    {
    }

    /**
     * Returns the unique instance
     * 
     * @return the unique instance
     */
    public static synchronized MavenRepoComponentInfoProvider getInstance( )
    {
    	if ( _instance == null )
        {
            _instance = new MavenRepoComponentInfoProvider( );
        }
        return _instance;
    }
    
    public void setComponentRemoteInformations( Component component )
    {
        String strArtifactId = component.getArtifactId( );

        // The Nexus path is derived from the groupId : without it the component cannot be located. Block with a clear message.
        if ( StringUtils.isBlank( component.getGroupId( ) ) )
        {
            String strMessage = "Le groupId est absent : composant introuvable dans Nexus.";
            component.setBlockingReleaseComment( strMessage );
            AppLogService.error( "MavenRepoComponentInfoProvider - " + strMessage );
            return;
        }

        try
        {
            String strComponentPath = getComponentPath( component );
            
            // Get release versions : a component in both repositories is an anomaly to fix in Nexus.
            Map<String, String> mapReleaseUrls = getAvailableUrls( PROPERTIES_RELEASES_PATH, strComponentPath );
            if ( blockIfSeveralRepositories( component, KIND_RELEASES, mapReleaseUrls ) )
            {
                return;
            }
            String strReleaseRepository = MavenUtils.getSingleRepository( mapReleaseUrls );
        	String strReleaseUrl = strReleaseRepository != null ? mapReleaseUrls.get( strReleaseRepository ) : null;

        	if ( strReleaseUrl != null )
        	{
	            List<String> listReleaseVersions = getVersionList( strReleaseUrl );
	            
	            if ( listReleaseVersions != null && !listReleaseVersions.isEmpty() )
	            {
	            	listReleaseVersions = VersionUtils.sortVersionsList(listReleaseVersions, true );
		            component.setReleaseVersions( listReleaseVersions );
		            component.setLastAvailableVersion( VersionUtils.getLastVersion( listReleaseVersions ) );
	            }
	            else
	            {
	            	component.setLastAvailableVersion( RELEASE_NOT_FOUND );
	            }
        	}
        	else 
        	{
        		AppLogService.info( "Error getting component url. No component found in Maven Release Repository : " );
        	}

            // Get snapshot versions and scmDeveloperConnection.
            Map<String, String> mapSnapshotUrls = getAvailableUrls( PROPERTIES_SNAPSHOTS_PATH, strComponentPath );
            if ( blockIfSeveralRepositories( component, KIND_SNAPSHOTS, mapSnapshotUrls ) )
            {
                return;
            }
            String strSnapshotRepository = MavenUtils.getSingleRepository( mapSnapshotUrls );
            String strSnapshotUrl = strSnapshotRepository != null ? mapSnapshotUrls.get( strSnapshotRepository ) : null;

            // Composant absent du dépôt Nexus snapshot.
            if ( strSnapshotUrl == null )
            {
                component.setBlockingReleaseComment( "Composant absent du dépôt Nexus snapshot : release impossible." );
                AppLogService.error( "MavenRepoComponentInfoProvider - Component " + strArtifactId + " not found in Maven Snapshot Repository" );
                return;
            }

            List<String> listSnapshotVersions = getVersionList( strSnapshotUrl );

            if ( listSnapshotVersions == null || listSnapshotVersions.isEmpty( ) )
            {
                component.setLastAvailableSnapshotVersion( SNAPSHOT_NOT_FOUND );
                component.setBlockingReleaseComment( "Aucune version SNAPSHOT publiée dans Nexus : release impossible." );
                AppLogService.error( "MavenRepoComponentInfoProvider - No snapshot version found in Nexus for " + strArtifactId );
                return;
            }

            listSnapshotVersions = VersionUtils.sortVersionsList( listSnapshotVersions, true );
            String strLastSnapshot = VersionUtils.getLastVersion( listSnapshotVersions );
            component.setSnapshotVersions( listSnapshotVersions );
            component.setLastAvailableSnapshotVersion( strLastSnapshot );

            // Read scmDeveloperConnection from the last snapshot pom (uses the local version).
            getPomInfos( component, getSnapshotPomUrl( strSnapshotUrl, strArtifactId, strLastSnapshot ) );

            // The parent must come from the current snapshot when Nexus has another one : it tells the core version of that line.
            String strCurrentVersion = component.getCurrentVersion( );
            if ( strCurrentVersion != null && !strCurrentVersion.equals( strLastSnapshot ) && listSnapshotVersions.contains( strCurrentVersion ) )
            {
                getPomParent( component, getSnapshotPomUrl( strSnapshotUrl, strArtifactId, strCurrentVersion ) );
            }

            // Balise <scm> absente du POM.
            if ( StringUtils.isBlank( component.getScmDeveloperConnection( ) ) )
            {
                component.setBlockingReleaseComment( "La balise SCM est absente du POM : release impossible." );
                AppLogService.error( "MavenRepoComponentInfoProvider - Missing SCM tag in POM for " + strArtifactId );
                return;
            }

            // Dépôt non supporté (ni GitHub ni GitLab).
            if ( component.getRepoType( ) == null )
            {
                component.setBlockingReleaseComment( "Le dépôt n'est ni GitHub ni GitLab : release impossible." );
                AppLogService.error( "MavenRepoComponentInfoProvider - Unsupported repository for " + strArtifactId + " : "
                        + component.getScmDeveloperConnection( ) );
                return;
            }

            // Releases in the repository of the other platform : informative only.
            String strMisplaced = MavenUtils.getMisplacedReleasesMessage( strReleaseRepository, component.getRepoType( ) );
            if ( strMisplaced != null )
            {
                component.addReleaseComment( strMisplaced );
                AppLogService.info( "MavenRepoComponentInfoProvider - " + strArtifactId + " : " + strMisplaced );
            }

            // Infos bugtracker : URL de roadmap, ou commentaire informatif si projet manquant.
            BugtrackerService.getService( ).populateBugtrackerInfo( component );
        }
        catch( Exception ex )
        {
            AppLogService.error( "MavenRepoComponentInfoProvider - Error getting remote informations for "
                    + strArtifactId + " : " + ex.getMessage( ), ex );
        }
    }
    
    /**
     * Returns the URL of the component in every repository holding it, by repository name (private, public), in the order of the properties.
     *
     * @param listRepoPathTypeProperties
     *            the path properties of the repositories (releases or snapshots)
     * @param strComponentPath
     *            the Nexus path of the component
     * @return the URLs by repository name, empty when no repository holds the component
     */
    private Map<String, String> getAvailableUrls( List<String> listRepoPathTypeProperties, String strComponentPath )
    {
    	HttpAccess httpAccess = new HttpAccess( );
    	Map<String, String> mapUrls = new LinkedHashMap<>( );

    	for (String strUrlProperty : PROPERTIES_MAVEN_URL)
    	{
    		String[] tabUrl = strUrlProperty.split("\\.");
    		String strRepository = tabUrl[tabUrl.length - 1].trim( );
    		for (String strTypeProperty : listRepoPathTypeProperties)
        	{
    			String[] tabRepoType = strTypeProperty.split("\\.");
    			if ( tabRepoType[tabRepoType.length - 1].trim( ).equals( strRepository ) )
    			{
    				String url =  AppPropertiesService.getProperty(strUrlProperty) + PATH_MAVEN_REPO_TREE
    						+ AppPropertiesService.getProperty(strTypeProperty) + strComponentPath;
    				String strHtml = null;
    				try
    	            {
    	    			strHtml = httpAccess.doGet( url );
    	            }
    	            catch ( HttpAccessException e )
    	            {
    	                AppLogService.info( "LuteceTools - MavenRepoService : Not available url : " + url );
    	            }

    	    		if ( strHtml != null && !strHtml.isEmpty())
    	    		{
    	    			mapUrls.put( strRepository, url );
    	    		}
    			}

        	}
    	}

    	return mapUrls;
    }

    /**
     * Blocks the release of a component found in several repositories of the same kind, with the versions of each repository in the message.
     *
     * @param component
     *            the component
     * @param strKind
     *            the kind of repositories (releases or snapshots)
     * @param mapUrls
     *            the URLs of the component by repository name
     * @return true when the component is blocked
     */
    private boolean blockIfSeveralRepositories( Component component, String strKind, Map<String, String> mapUrls )
    {
        if ( mapUrls.size( ) < 2 )
        {
            return false;
        }

        Map<String, List<String>> mapVersions = new LinkedHashMap<>( );
        for ( Map.Entry<String, String> entry : mapUrls.entrySet( ) )
        {
            mapVersions.put( entry.getKey( ), VersionUtils.sortVersionsList( getVersionList( entry.getValue( ) ), true ) );
        }

        String strMessage = MavenUtils.getSeveralRepositoriesMessage( strKind, mapVersions );
        component.setBlockingReleaseComment( strMessage );
        AppLogService.error( "MavenRepoComponentInfoProvider - " + component.getArtifactId( ) + " : " + strMessage );

        return true;
    }

    
    private List<String> getVersionList( String strUrl )
    {
        List<String> listVersions = new ArrayList<>( );
        
    	if (strUrl != null && !strUrl.isEmpty())
    	{
	        try
	        {
	            HttpAccess httpAccess = new HttpAccess( );
	            String strHtml = httpAccess.doGet( strUrl );
	            List<String> listAnchors = getAnchorsList( strHtml );
	
	            for ( String strAnchor : listAnchors )
	            {
	                if ( strAnchor.matches( "^[\\d].*" ) )
	                {
	                	listVersions.add( strAnchor );	                	
	                }
	            }	            
	        }
	        catch( HttpAccessException e )
	        {
	            AppLogService.error( "MavenRepoComponentInfoProvider - Error retrieving versions from "
	                    + strUrl + " : " + e.getMessage( ), e );
	        }
    	}

        return listVersions;
    }
    
    /**
     * Derive the Nexus tree path of a component from its groupId : in a Maven repository the path of an artifact is
     * always {@code groupId(. -> /) / artifactId}. This generalizes the previous per-type dispatch (core/themes/plugins)
     * and also covers any other groupId such as {@code fr.paris.lutece.tools} (e.g. lutece-site-pom). The caller
     * guarantees a non-blank groupId (see {@link #setComponentRemoteInformations}).
     *
     * @param component
     *            the component
     * @return the Nexus tree path (e.g. "fr/paris/lutece/plugins/plugin-xxx")
     */
    private String getComponentPath( Component component )
    {
        return component.getGroupId( ).replace( ".", "/" ) + "/" + component.getArtifactId( );
    }
    
    private String getPomUrl( String strDirUrl, String strArtifactId, String strVersion )
    {
        String strPomUrl = strDirUrl;
        
        if ( strDirUrl.substring( strDirUrl.length() - 1)  != "/" ) 
        {
        	strPomUrl = strPomUrl + "/";
        }
        
        strPomUrl = strPomUrl + strVersion + "/";
        
        strPomUrl = strPomUrl +  strArtifactId + "-" + strVersion + ".pom";

        return strPomUrl;
    }
    
    private String getSnapshotPomUrl( String strDirUrl, String strArtifactId, String strVersion )
    {
        String strSnapshotPomUrl = strDirUrl;
        
        if ( !strSnapshotPomUrl.substring( strSnapshotPomUrl.length() - 1).equals("/") ) 
        {
        	strSnapshotPomUrl = strSnapshotPomUrl + "/";
        }
        strSnapshotPomUrl = strSnapshotPomUrl + strVersion  + "/";
        String strPomUrl = strSnapshotPomUrl;
        
        try
        {
            HttpAccess httpAccess = new HttpAccess( );
            List<String> listVersions = new ArrayList<>( );
            
            String strHtml = httpAccess.doGet( strSnapshotPomUrl.toString() );
            List<String> listElement = getAnchorsList( strHtml );
            String strPomFileName = getPomFileName(listElement);
            
            if ( strPomFileName == null || strPomFileName.isEmpty() )
            {
            	String strLastDirname = null;
        		int nIteration = 0;
            	for ( String strDirname : listElement )
                {
            		String [ ] tabDir = strDirname.split("-");
            		String [ ] tabVer = strVersion.split("-");
            		
                    if ( tabDir[0].equals(tabVer[0]) && Integer.parseInt( tabDir[tabDir.length -1] ) >= nIteration )
                    {
                    	strLastDirname = strDirname;
                    	nIteration = Integer.parseInt( tabDir[tabDir.length -1] );
                    }                    
                }
            	
            	strSnapshotPomUrl = strSnapshotPomUrl + strLastDirname;
            	
                strHtml = httpAccess.doGet( strSnapshotPomUrl );
                listElement = getAnchorsList( strHtml );                
                strPomFileName = getPomFileName(listElement);
            }
            
            strPomUrl = strPomUrl.replace(PATH_MAVEN_REPO_TREE, PATH_MAVEN_REPO_FILE) + strPomFileName;
        }
        catch ( HttpAccessException e )
        {
        	AppLogService.error( "\n*** ERROR ***  Error retrieving snapshot pom URL : " + e.getMessage( ) );
        }

        return strPomUrl;
    }
    
    private void getPomInfos( Component component, String strPomUrl )
    {
        try
        {
            HttpAccess httpAccess = new HttpAccess( );
            Map<String, String> headersResponse = null;
            String strPom = null;
       
        	strPom = httpAccess.doGet( strPomUrl );
                      
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance( );
            SAXParser saxParser = saxParserFactory.newSAXParser( );
            SaxPomHandler handler = new SaxPomHandler( );
            saxParser.parse( new InputSource( new StringReader( strPom ) ), handler );
            
            
            component.setScmDeveloperConnection( handler.getScmDeveloperConnection( ) );
            setPomParent( component, strPom );

        }
        catch ( HttpAccessException e )
        {
            AppLogService.error(
            		"\n*** ERROR *** Error reading pom for component " + component.getArtifactId( )
            		+ EXCEPTION_MESSAGE + e.getMessage( ) );
        }
        catch ( IOException | SAXException | ParserConfigurationException e )
        {
            AppLogService.error( EXCEPTION_MESSAGE + e.getMessage( ), e );
        }
    }

    /**
     * Reads the parent of a pom published in Nexus. On failure the parent is reset : an unknown parent is better than the one of another line.
     *
     * @param component
     *            the component
     * @param strPomUrl
     *            the pom URL
     */
    private void getPomParent( Component component, String strPomUrl )
    {
        try
        {
            setPomParent( component, new HttpAccess( ).doGet( strPomUrl ) );
        }
        catch ( HttpAccessException e )
        {
            setPomParent( component, null );
            AppLogService.error( "\n*** ERROR *** Error reading pom " + strPomUrl + " for the parent of component " + component.getArtifactId( )
                    + EXCEPTION_MESSAGE + e.getMessage( ) );
        }
    }

    /**
     * Records the parent declared in a pom.
     *
     * @param component
     *            the component
     * @param strPom
     *            the pom content, null to reset the parent
     */
    private void setPomParent( Component component, String strPom )
    {
        Dependency parent = strPom != null ? new PomParser( ).parseParent( strPom ) : null;
        component.setPomParentGroupId( parent != null ? parent.getGroupId( ) : null );
        component.setPomParentArtifactId( parent != null ? parent.getArtifactId( ) : null );
        component.setPomParentVersion( parent != null ? parent.getVersion( ) : null );
    }
      
    private String getPomFileName(List<String> listElement)
    {
    	String strPomFileName = null;

        for ( String strFilename : listElement )
        {
            if ( strFilename.endsWith( ".pom" ) )
            {
            	strPomFileName = strFilename;
            }
        }
    	return strPomFileName;
    }

    /**
     * Gets anchor list using more optimized method
     * 
     * @param strHtml The HTML code
     * @return The list
     */
    private List<String> getAnchorsList( String strHtml )
    {
        List<String> list = new ArrayList<>( );
        String strCurrent = strHtml;

        int nPos = strCurrent.indexOf( "<a " );

        while ( nPos > 0 )
        {
            strCurrent = strCurrent.substring( nPos );

            int nEndTag = strCurrent.indexOf( ">" );
            int nTagEnd = strCurrent.indexOf( "</a>" );
            list.add( strCurrent.substring( nEndTag + 1, nTagEnd ).replaceAll( "\\/", "" ) );
            strCurrent = strCurrent.substring( nTagEnd + 4 );
            nPos = strCurrent.indexOf( "<a " );
        }

        return list;
    }

}