package fr.paris.lutece.plugins.releaser.util.pom;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxPomHandler extends DefaultHandler
{
    private static final String TAG_PARENT = "parent";
    private static final String TAG_VERSION = "version";
    private static final String TAG_ARTIFACT_ID = "artifactId";
    private static final String TAG_JIRA = "jiraProjectName";
    private static final String TAG_SCM = "scm";
    private static final String TAG_URL = "url";
    private static final String TAG_CONNECTION = "connection";
    private static final String TAG_DEVELOPPER_CONNECTION = "developerConnection";
    private String _strParentPomVersion;
    private String _strCoreVersion;
    private String _strJiraKey;
    private StringBuilder _sbScmUrl = new StringBuilder( );
    private StringBuilder _sbScmConnection = new StringBuilder( );
    private StringBuilder _sbScmDeveloperConnection = new StringBuilder( );

    private boolean _bPomParent;
    private boolean _bVersion;
    private boolean _bArtifactId;
    private boolean _bCore;
    private boolean _bJira;
    private boolean _bSCM;
    private boolean _bURL;
    private boolean _bConnection;
    private boolean _bDevelopperConnection;

    /**
     * Returns Parent Pom version
     * 
     * @return The Parent Pom version
     */
    public String getParentPomVersion( )
    {
        return _strParentPomVersion;
    }

    /**
     * Returns Core version
     * 
     * @return The Core version
     */
    public String getCoreVersion( )
    {
        return _strCoreVersion;
    }

    /**
     * Returns JIRA key
     * 
     * @return The JIRA key
     */
    public String getJiraKey( )
    {
        return _strJiraKey;
    }

    /**
     * Returns the SCM URL
     * 
     * @return The SCM URL
     */
    public String getScmUrl( )
    {
        return _sbScmUrl.toString( );
    }

    /**
     * Returns the SCM Connection
     * 
     * @return The SCM Connection
     */
    public String getScmConnection( )
    {
        return _sbScmConnection.toString( );
    }

    /**
     * Returns the SCM Developer Connection
     * 
     * @return the SCM Developer Connection
     */
    public String getScmDeveloperConnection( )
    {
        return _sbScmDeveloperConnection.toString( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
    {
        if ( qName.equalsIgnoreCase( TAG_PARENT ) )
        {
            _bPomParent = true;
        }
        else
            if ( qName.equalsIgnoreCase( TAG_VERSION ) )
            {
                _bVersion = true;
            }
            else
                if ( qName.equalsIgnoreCase( TAG_ARTIFACT_ID ) )
                {
                    _bArtifactId = true;
                }
                else
                    if ( qName.equalsIgnoreCase( TAG_JIRA ) )
                    {
                        _bJira = true;
                    }
                    else
                        if ( qName.equalsIgnoreCase( TAG_SCM ) )
                        {
                            _bSCM = true;
                        }
                        else
                            if ( qName.equalsIgnoreCase( TAG_URL ) )
                            {
                                _bURL = true;
                            }
                            else
                                if ( qName.equalsIgnoreCase( TAG_CONNECTION ) )
                                {
                                    _bConnection = true;
                                }
                                else
                                    if ( qName.equalsIgnoreCase( TAG_DEVELOPPER_CONNECTION ) )
                                    {
                                        _bDevelopperConnection = true;
                                    }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void endElement( String uri, String localName, String qName ) throws SAXException
    {
        if ( qName.equalsIgnoreCase( TAG_PARENT ) )
        {
            _bPomParent = false;
        }
        else
            if ( qName.equalsIgnoreCase( TAG_VERSION ) )
            {
                _bVersion = false;
            }
            else
                if ( qName.equalsIgnoreCase( TAG_ARTIFACT_ID ) )
                {
                    _bArtifactId = false;
                }
                else
                    if ( qName.equalsIgnoreCase( TAG_JIRA ) )
                    {
                        _bJira = false;
                    }
                    else
                        if ( qName.equalsIgnoreCase( TAG_SCM ) )
                        {
                            _bSCM = false;
                        }
                        else
                            if ( qName.equalsIgnoreCase( TAG_URL ) )
                            {
                                _bURL = false;
                            }
                            else
                                if ( qName.equalsIgnoreCase( TAG_CONNECTION ) )
                                {
                                    _bConnection = false;
                                }
                                else
                                    if ( qName.equalsIgnoreCase( TAG_DEVELOPPER_CONNECTION ) )
                                    {
                                        _bDevelopperConnection = false;
                                    }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void characters( char [ ] ch, int start, int length ) throws SAXException
    {
        if ( _bPomParent && _bVersion )
        {
            _strParentPomVersion = new String( ch, start, length );
        }
        else
            if ( _bArtifactId )
            {
                String strArtifactId = new String( ch, start, length );
                _bCore = strArtifactId.equals( "lutece-core" );
            }
            else
                if ( _bCore && _bVersion )
                {
                    _strCoreVersion = new String( ch, start, length );
                }
                else
                    if ( _bJira )
                    {
                        _strJiraKey = new String( ch, start, length );
                    }
                    else
                        if ( _bSCM && _bURL )
                        {
                            _sbScmUrl.append( new String( ch, start, length ) );
                        }
                        else
                            if ( _bSCM && _bConnection )
                            {
                                _sbScmConnection.append( new String( ch, start, length ) );
                            }
                            else
                                if ( _bSCM && _bDevelopperConnection )
                                {
                                    _sbScmDeveloperConnection.append( new String( ch, start, length ) );
                                }
    }
}
