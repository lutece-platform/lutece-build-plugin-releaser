
package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import org.apache.commons.lang3.math.NumberUtils;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.pom.SaxPomHandler;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import fr.paris.lutece.plugins.releaser.util.version.VersionParsingException;
import fr.paris.lutece.plugins.releaser.util.version.VersionUtils;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
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
	private static final String PROPERTY_MAVEN_URL_PREFIX = "lutecetools.maven.repository.url";
	private static final List<String> PROPERTIES_MAVEN_URL = AppPropertiesService.getKeys( PROPERTY_MAVEN_URL_PREFIX );
		
	// Maven Releases Paths
	private static final String PROPERTY_RELEASES_PATH_PREFIX = "lutecetools.maven.repository.releases";
	private static final List<String> PROPERTIES_RELEASES_PATH = AppPropertiesService.getKeys( PROPERTY_RELEASES_PATH_PREFIX );
	
	// Maven Sanapshots Paths
    private static final String PROPERTY_SNAPSHOTS_PATH_PREFIX = "lutecetools.maven.repository.snapshots";
	private static final List<String> PROPERTIES_SNAPSHOTS_PATH = AppPropertiesService.getKeys( PROPERTY_SNAPSHOTS_PATH_PREFIX );

	// Maven path - to see the tree structure
    private static final String PROPERTY_MAVEN_REPO_PATH_TREE = "lutecetools.maven.repository.treestructure.path";
	private static final String PATH_MAVEN_REPO_TREE = AppPropertiesService.getProperty( PROPERTY_MAVEN_REPO_PATH_TREE );

    // Maven path - To get file
	private static final String PROPERTY_MAVEN_REPO_PATH_FILE = "lutecetools.maven.repository.getfile.path";
    private static final String PATH_MAVEN_REPO_FILE = AppPropertiesService.getProperty( PROPERTY_MAVEN_REPO_PATH_FILE );	

    // Path Plugins
    private static final String PROPERTY_MAVEN_PATH_PLUGINS = "lutecetools.maven.repository.path.plugins";
    private static final String URL_MAVEN_PATH_PLUGINS = AppPropertiesService.getProperty( PROPERTY_MAVEN_PATH_PLUGINS );
    
    // Path Pom site
    private static final String PROPERTY_MAVEN_PATH_SITE_POM = "lutecetools.maven.repository.path.site-pom";
    private static final String URL_MAVEN_PATH_SITE_POM = AppPropertiesService.getProperty( PROPERTY_MAVEN_PATH_SITE_POM );
    
    // Path Lutece core
    private static final String PROPERTY_MAVEN_PATH_CORE = "lutecetools.maven.repository.path.core";
    private static final String URL_MAVEN_PATH_CORE = AppPropertiesService.getProperty( PROPERTY_MAVEN_PATH_CORE );
    
    // Path Themes
    private static final String PROPERTY_MAVEN_PATH_THEMES = "lutecetools.maven.repository.path.themes";
    private static final String URL_MAVEN_PATH_THEMES = AppPropertiesService.getProperty( PROPERTY_MAVEN_PATH_THEMES );

    private static final String RELEASE_NOT_FOUND = "Release not found";
    private static final String EXCEPTION_MESSAGE = "LuteceTools - MavenRepoService : Error retrieving pom infos : ";

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
        String strType = getMavenRepoDirectoryType( strArtifactId, component.getType( ) );

        try
        {
            String strComponentPath = getComponentPath( strArtifactId, strType );
            
            // Get release versions
        	String strReleaseUrl = getAvailableUrl( PROPERTIES_RELEASES_PATH, strComponentPath );

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
        		AppLogService.info( "Error getting component url. No coponent found in Maven Release Repository : " );
        	}

        	// Get snapshot versions and scmDeveloperConnection
            String strSnapshotUrl = getAvailableUrl( PROPERTIES_SNAPSHOTS_PATH, strComponentPath );
            if ( strSnapshotUrl != null ) 
            {
	            List<String> listSnapshotVersions = getVersionList( strSnapshotUrl );
	            
	            if ( listSnapshotVersions != null && !listSnapshotVersions.isEmpty() )
	            {
	            	listSnapshotVersions = VersionUtils.sortVersionsList(listSnapshotVersions, true );
		            component.setSnapshotVersions( listSnapshotVersions );
		            component.setLastAvailableSnapshotVersion( VersionUtils.getLastVersion( listSnapshotVersions ) );
	            }
	            else
	            {
	            	component.setLastAvailableSnapshotVersion( RELEASE_NOT_FOUND );
	            }
	            
	            // Get scmDeveloperConnection from Snapshot pom
	            if ( !listSnapshotVersions.isEmpty() )
	            getPomInfos( component, getSnapshotPomUrl( strSnapshotUrl, strArtifactId, component.getLastAvailableSnapshotVersion( ) ) );
         }
        	else 
        	{
        		AppLogService.info( "Error getting component url. No coponent found in Maven Snapshot Repository : " );
        	}
            
        }
        catch( Exception ex )
        {
            AppLogService.error( "MavenRepoComponentInfoProvider - Error getting remote informations for "
                    + strArtifactId + " : " + ex.getMessage( ), ex );
        }
    }
    
    private String getAvailableUrl( List<String> listRepoPathTypeProperties, String strComponentPath )
    {
    	HttpAccess httpAccess = new HttpAccess( );
    	String strHtml = null;
    	
    	for (String strUrlProperty : PROPERTIES_MAVEN_URL)
    	{    		
    		String[] tabUrl = strUrlProperty.split("\\.");
    		for (String strTypeProperty : listRepoPathTypeProperties)
        	{
    			String[] tabRepoType = strTypeProperty.split("\\.");
    			if ( tabRepoType[tabRepoType.length - 1].equals( tabUrl[tabUrl.length - 1] ) )
    			{
    				String url =  AppPropertiesService.getProperty(strUrlProperty) + PATH_MAVEN_REPO_TREE 
    						+ AppPropertiesService.getProperty(strTypeProperty) + strComponentPath;
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
    	    			return url;
    	    		} 
    			}	
    			
        	}    		   			
    	}     
    	
    	return null;
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
    
    private String getComponentPath( String strArtifactId, String strType )
    {
        if ( ConstanteUtils.MAVEN_REPO_LUTECE_CORE.equals(strType ) )
        {
            return URL_MAVEN_PATH_CORE;
        }
        else if ( ConstanteUtils.MAVEN_REPO_LUTECE_SITE.equals( strType ) )
        {
            return URL_MAVEN_PATH_THEMES + strArtifactId;
        }
        else
        {
            return URL_MAVEN_PATH_PLUGINS + strArtifactId;
        }      
    }
 
    private String getMavenRepoDirectoryType( String strArtifactId, String strComponentType )
    {
        String strTypeRepo = null;
        if ( strComponentType != null )
        {
            switch ( strComponentType )
            {
            case ConstanteUtils.DEPENDENCY_TYPE_LUTECE_CORE:

                strTypeRepo = ConstanteUtils.MAVEN_REPO_LUTECE_CORE;
                break;
            case ConstanteUtils.DEPENDENCY_TYPE_LUTECE_SITE:

                strTypeRepo = ConstanteUtils.MAVEN_REPO_LUTECE_SITE;
                break;

            default:
                strTypeRepo = ConstanteUtils.MAVEN_REPO_LUTECE_PLUGIN;
                break;
            }
        }
        else
        {
            if ( ConstanteUtils.TAG_LUTECE_CORE.equals( strArtifactId ) )
            {
                strTypeRepo = ConstanteUtils.MAVEN_REPO_LUTECE_CORE;
            }
            else
            {
                strTypeRepo = ConstanteUtils.MAVEN_REPO_LUTECE_PLUGIN;
            }
        }

        return strTypeRepo;
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
            //component.setParentPomVersion( handler.getParentPomVersion( ) );
            //component.setCoreVersion( handler.getCoreVersion( ) );
            
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