package fr.paris.lutece.plugins.releaser.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class PluginUtils.
 */
public class PluginUtils
{

    /** The Constant CORE_PLUGIN_NAME1. */
    public static final String CORE_PLUGIN_NAME1 = "core";
    
    /** The Constant CORE_PLUGIN_NAME2. */
    public static final String CORE_PLUGIN_NAME2 = "lutece-core";

    /** The Constant SUFFIX_CONTEXT_FILE. */
    private static final String SUFFIX_CONTEXT_FILE = ".xml";
    
    /** The Constant CONSTANTE_PLUGIN_PATH. */
    private static final String CONSTANTE_PLUGIN_PATH = "webapp/WEB-INF/plugins/";
    
    /** The Constant CONSTANTE_CORE_CONF. */
    private static final String CONSTANTE_CORE_CONF = "webapp/WEB-INF/conf/";
    
    /** The Constant CONSTANTE_CORE_APP_INFO. */
    private static final String CONSTANTE_CORE_APP_INFO = "src/java/fr/paris/lutece/portal/service/init/AppInfo.java";
    
    /** The Constant REGEXP_VERSION_APP_INFO. */
    private static final String REGEXP_VERSION_APP_INFO = "(.*)private static final String APP_VERSION = \"(.*)\";";
    
    /** The Constant PATTERN_VERSION_APP_INFO. */
    private static final Pattern PATTERN_VERSION_APP_INFO = Pattern.compile( REGEXP_VERSION_APP_INFO );

    /**
     * Checks if is core.
     *
     * @param strPluginName            plugin name
     * @return <code>true</code> if core or lutece-core, <code>false</code> otherwise.
     */
    public static boolean isCore( String strPluginName )
    {
        return CORE_PLUGIN_NAME1.equals( strPluginName ) || CORE_PLUGIN_NAME2.equals( strPluginName );
    }

    /**
     * Gets the core XML file.
     *
     * @param strComponentPath the str component path
     * @return the core XML file
     */
    public static String getCoreXMLFile( String strComponentPath )
    {
        String strCodeXML = strComponentPath + ( strComponentPath.endsWith( File.separator ) ? "" : File.separator ) + CONSTANTE_CORE_CONF + "core.xml";
        File fileCoreXML = new File( strCodeXML );

        if ( !fileCoreXML.exists( ) )
        {
            return "";
        }

        return strCodeXML;
    }

    /**
     * Gets the plugin XML file.
     *
     * @param strComponentPath the str component path
     * @return the plugin XML file
     */
    public static String [ ] getPluginXMLFile( String strComponentPath )
    {
        String strDirConfPlugins = strComponentPath + ( strComponentPath.endsWith( File.separator ) ? "" : File.separator ) + CONSTANTE_PLUGIN_PATH;
        File dirConfPlugins = new File( strDirConfPlugins );

        if ( !dirConfPlugins.exists( ) )
        {
            return new String [ 0];
        }

        FilenameFilter filterContext = new ContextFileFilter( );
        String [ ] filesName = dirConfPlugins.list( filterContext );
        for ( int nIndex = 0; nIndex < filesName.length; nIndex++ )
        {
            if ( !filesName [nIndex].startsWith( strDirConfPlugins ) )
            {
                filesName [nIndex] = strDirConfPlugins + filesName [nIndex];
            }
        }

        return filesName;
    }

    /**
     * Gets the app info file.
     *
     * @param strBasePath the str base path
     * @return the app info file
     */
    public static String getAppInfoFile( String strBasePath )
    {
        String strAppInfo = strBasePath + File.separator + CONSTANTE_CORE_APP_INFO;

        File fileAppInfo = new File( strAppInfo );
        if ( !fileAppInfo.exists( ) )
        {
            return "";
        }

        return strAppInfo;
    }

    /**
     * Update app info file.
     *
     * @param strFile the str file
     * @param strVersion the str version
     * @param commandResult the command result
     * @return true, if successful
     */
    public static boolean updateAppInfoFile( String strFile, String strVersion, CommandResult commandResult )
    {
        boolean bReplace = false;
        BufferedReader br = null;
        StringBuilder sbNewFileContent = null;
        try
        {
            sbNewFileContent = new StringBuilder( );
            br = new BufferedReader( new FileReader( strFile ) );
            while ( br.ready( ) )
            {
                String strNewLine = br.readLine( );
                Matcher matcher = PATTERN_VERSION_APP_INFO.matcher( strNewLine );
                if ( matcher.matches( ) )
                {
                    if ( matcher.groupCount( ) >= 1 )
                    {
                        String strCurrentVersion = matcher.group( 2 );
                        if ( !strCurrentVersion.equals( strVersion ) )
                        {
                            commandResult.getLog( ).append( "Updating core version from " + strCurrentVersion + " to " + strVersion );
                            strNewLine = strNewLine.replace( strCurrentVersion, strVersion );
                            bReplace = true;
                        }
                        else
                        {
                            commandResult.getLog( ).append( "Version is already " + strVersion );
                        }
                    }
                }

                sbNewFileContent.append( strNewLine + "\n" );
            }

        }
        catch( Exception ex )
        {
            ReleaserUtils.addTechnicalError( commandResult, ex.getMessage( ), ex );
        }
        finally
        {
            if ( br != null )
            {
                try
                {
                    br.close( );
                }
                catch( IOException e1 )
                {
                    ReleaserUtils.addTechnicalError( commandResult, e1.getMessage( ), e1 );
                }
            }
        }

        if ( !bReplace )
        {
            return false;
        }

        FileWriter fw = null;
        try
        {
            fw = new FileWriter( strFile );
            fw.append( sbNewFileContent.toString( ) );
        }
        catch( Exception ex )
        {
            ReleaserUtils.addTechnicalError( commandResult, ex.getMessage( ), ex );
        }
        finally
        {
            if ( fw != null )
            {
                try
                {
                    fw.close( );
                }
                catch( IOException ex )
                {
                    ReleaserUtils.addTechnicalError( commandResult, ex.getMessage( ), ex );
                }
            }
        }
        return false;
    }

    /**
     * Update plugin XML version.
     *
     * @param strFile the str file
     * @param strNewVersion the str new version
     * @param commandResult the command result
     * @return the string
     */
    public static String updatePluginXMLVersion( String strFile, String strNewVersion, CommandResult commandResult )
    {
        FileWriter fw = null;
        boolean bFileClosed = false;
        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance( );
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder( );
            Document doc = docBuilder.parse( strFile );

            doc.getDocumentElement( ).normalize( );

            NodeList versionList = doc.getElementsByTagName( "version" );
            Node version = versionList.item( 0 );

            version.getFirstChild( ).setNodeValue( strNewVersion );

            DOMSource domSource = new DOMSource( doc );

            StringWriter sw = new StringWriter( );
            StreamResult result = new StreamResult( sw );

            TransformerFactory tf = TransformerFactory.newInstance( );
            Transformer transformer = tf.newTransformer( );
            transformer.transform( domSource, result );

            fw = new FileWriter( new File( strFile ) );

            fw.append( sw.toString( ) );

            fw.flush( );

            fw.close( );

            bFileClosed = true;

        }
        catch( Exception ex )
        {
            ReleaserUtils.addTechnicalError( commandResult, ex.getMessage( ), ex );
        }

        finally
        {
            if ( fw != null && !bFileClosed )
            {
                try
                {
                    fw.close( );
                }
                catch( IOException e )
                {
                    e.printStackTrace( );
                }
            }
        }

        return "";
    }

    /**
     * Utils filename filter to identify context files.
     */
    static class ContextFileFilter implements FilenameFilter
    {
        
        /**
         * Filter filename.
         *
         * @param file            The current file
         * @param strName            The file name
         * @return true if the file is a context file otherwise false
         */
        public boolean accept( File file, String strName )
        {
            return strName.endsWith( SUFFIX_CONTEXT_FILE );
        }
    }

}
