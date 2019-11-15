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
package fr.paris.lutece.plugins.releaser.util.version;

import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.util.AppLogService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Version
 */
public class Version implements Comparable
{
    public static final String NOT_AVAILABLE = "Not available";
    private static final String QUALIFIER_SNAPSHOT = "SNAPSHOT";
    private static final String QUALIFIER_CANDIDATE = "RC";
    private static final String PATTERN_NUMBER = "\\d+";
    private static final String QUALIFIER_VERSION_FORMAT = "%02d";

    private int _nMajor;
    private int _nMinor;
    private int _nPatch;
    private String _strQualifier;
    private String _strQualifierRadix;
    private int _nQualifierNumber;

    /** Constructor */
    public Version( )
    {
    }

    /**
     * Constructor
     * 
     * @param nMajor
     *            major digit
     * @param nMinor
     *            minor digit
     * @param nPatch
     *            patch digit
     * @param strQualifier
     *            qualifier
     */
    public Version( int nMajor, int nMinor, int nPatch, String strQualifier )
    {
        _nMajor = nMajor;
        _nMinor = nMinor;
        _nPatch = nPatch;
        _strQualifier = strQualifier;
    }

    /**
     * @return the nMajor
     */
    public int getMajor( )
    {
        return _nMajor;
    }

    /**
     * @param nMajor
     *            the nMajor to set
     */
    public void setMajor( int nMajor )
    {
        _nMajor = nMajor;
    }

    /**
     * @return the nMinor
     */
    public int getMinor( )
    {
        return _nMinor;
    }

    /**
     * @param nMinor
     *            the nMinor to set
     */
    public void setMinor( int nMinor )
    {
        _nMinor = nMinor;
    }

    /**
     * @return the nPatch
     */
    public int getPatch( )
    {
        return _nPatch;
    }

    /**
     * @param nPatch
     *            the nPatch to set
     */
    public void setPatch( int nPatch )
    {
        _nPatch = nPatch;
    }

    /**
     * @return the Qualifier
     */
    public String getQualifier( )
    {
        return _strQualifier;
    }

    /**
     * @param strQualifier
     *            the Qualifier to set
     */
    public void setQualifier( String strQualifier )
    {
        Pattern pattern = Pattern.compile( PATTERN_NUMBER );
        Matcher matcher = pattern.matcher( strQualifier );
        if( matcher.find() )
        {
            String strNumber = matcher.group();
            _nQualifierNumber = Integer.parseInt( strNumber );
            _strQualifierRadix = strQualifier.substring( 0 , strQualifier.indexOf( strNumber ));
        }
        _strQualifier = strQualifier;
    }

    @Override
    public int compareTo( Object object )
    {
        Version version = (Version) object;
        int nDiff = _nMajor - version.getMajor( );
        if ( nDiff != 0 )
        {
            return nDiff;
        }
        nDiff = _nMinor - version.getMinor( );
        if ( nDiff != 0 )
        {
            return nDiff;
        }
        nDiff = _nPatch - version.getPatch( );
        return nDiff;
    }

    public String getVersion( )
    {
        StringBuilder sbVersion = new StringBuilder( );
        sbVersion.append( _nMajor ).append( '.' ).append( _nMinor ).append( '.' ).append( _nPatch );
        if ( _strQualifier != null )
        {
            if( _strQualifierRadix != null )
            {
                sbVersion.append( '-' ).append( _strQualifierRadix ).append( String.format( QUALIFIER_VERSION_FORMAT , _nQualifierNumber ));
            }
            else
            {
                sbVersion.append( '-' ).append( _strQualifier );
            }
        }
        return sbVersion.toString( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String toString( )
    {
        return getVersion( );
    }

    /**
     * Parse a string to extract version
     * 
     * @param strSource
     *            The source
     * @return The version object
     * @throws VersionParsingException
     *             if parsing failed
     */
    public static Version parse( String strSource ) throws VersionParsingException
    {
        Version version = new Version( );

        try
        {
          
            
            
            
            String strCurrent = strSource!=null?strSource.trim( ):"";

            // Search for qualifier
            int nPos = strCurrent.indexOf( '-' );
            if ( nPos != -1 )
            {
                version.setQualifier( strCurrent.substring( nPos + 1 ) );
                strCurrent = strCurrent.substring( 0, nPos );
            }

            // Search for major digits
            nPos = strCurrent.indexOf( '.' );

            String strMajor = strCurrent.substring( 0, nPos );
            version.setMajor( ReleaserUtils.convertStringToInt( strMajor ) );

            // Search for minor digits
            strCurrent = strCurrent.substring( nPos + 1 );
            nPos = strCurrent.indexOf( '.' );

            if ( nPos != -1 )
            {
                String strMinor = strCurrent.substring( 0, nPos );
                version.setMinor( ReleaserUtils.convertStringToInt( strMinor ) );

                strCurrent = strCurrent.substring( nPos + 1 );
                version.setPatch(  ReleaserUtils.convertStringToInt(strCurrent ) );
            }
            else
            {
                version.setMinor(  ReleaserUtils.convertStringToInt(strCurrent ) );
            }
        }
        catch( Exception e )
        {
            throw new VersionParsingException( "Error parsing version : '" + strSource + "' : " + e.getMessage( ), e );
        }
        return version;
    }

    /**
     * returns the snapshot version 
     * @return the snapshot version 
     */
    public Version snapshot()
    {
        return new Version( _nMajor, _nMinor, _nPatch, QUALIFIER_SNAPSHOT );
    }

    /**
     * Build a new version object with major digit incremented
     * 
     * @param bSnapshot
     *            if snapshot qualifier needed
     * @return The next version object
     */
    public Version nextMajor( boolean bSnapshot )
    {
        String strQualifier = ( bSnapshot ) ? QUALIFIER_SNAPSHOT : null;

        return new Version( _nMajor + 1, 0 , 0, strQualifier );
    }

    /**
     * Build a new version object with minor digit incremented
     * 
     * @param bSnapshot
     *            if snapshot qualifier needed
     * @return The next version object
     */
    public Version nextMinor( boolean bSnapshot )
    {
        String strQualifier = ( bSnapshot ) ? QUALIFIER_SNAPSHOT : null;

        return new Version( _nMajor, _nMinor + 1, 0 , strQualifier );
    }

    /**
     * Build a new version object with patch digit incremented
     * 
     * @param bSnapshot
     *            if snapshot qualifier needed
     * @return The next version object
     */
    public Version nextPatch( boolean bSnapshot )
    {
        String strQualifier = ( bSnapshot ) ? QUALIFIER_SNAPSHOT : null;

        return new Version( _nMajor, _nMinor, _nPatch + 1, strQualifier );
    }

    /**
     * Build a new version object with no qualifier
     * 
     * @return The next version object
     */
    public Version nextRelease( )
    {
        int nPatch = ( isSnapshot() || isCandidate() ) ? _nPatch : _nPatch + 1;
        return new Version( _nMajor, _nMinor, nPatch, null );
    }

    private Version nextCandidate()
    {
        String strQualifier;
        int nPatch = _nPatch;
        if( (_strQualifierRadix != null) && (_strQualifierRadix.equals( "RC-")) )
        {
            strQualifier = String.format( "RC-%02d", _nQualifierNumber + 1 );
        }
        else
        {
            if(  ! QUALIFIER_SNAPSHOT.equals( _strQualifier ))
            {
                nPatch += 1;
            }
            strQualifier = "RC-01";
        }
        return new Version( _nMajor, _nMinor, nPatch, strQualifier );
    }
    
    /**
     * Returns true if the version is qualified as Snapshot
     * @return true if the version is qualified as Snapshot
     */
    public boolean isSnapshot()
    {
        return QUALIFIER_SNAPSHOT.equals( _strQualifier );
    }
    
    /**
     * Returns true if the version is qualified as Candidate
     * @return true if the version is qualified as Candidate
     */
    public boolean isCandidate()
    {
        return ( _strQualifier != null ) && ( _strQualifier.startsWith( QUALIFIER_CANDIDATE ));
    }
    
    /**
     * Check if a given version is a SNAPSHOT
     * 
     * @param strVersion
     *            The version to check
     * @return True if snapshot otherwise false
     */
    public static boolean isSnapshot( String strVersion )
    {
        try
        {
            Version version = parse( strVersion );
            return version.isSnapshot();
        }
        catch( VersionParsingException ex )
        {
            AppLogService.error( "Error parsing version " + strVersion + " : " + ex.getMessage( ), ex );
        }
        return false;
    }

    /**
     * Check if a given version is a RELEASE CANDIDATE
     * 
     * @param strVersion
     *            The version to check
     * @return True if candidate otherwise false
     */
    public static boolean isCandidate( String strVersion )
    {
        try
        {
            Version version = parse( strVersion );
            return version.isCandidate();
       }
        catch( VersionParsingException ex )
        {
            AppLogService.error( "Error parsing version " + strVersion + " : " + ex.getMessage( ), ex );
        }
        return false;
    }

    /**
     * Get a list of next versions for a given version
     * @param strPreviousReleaseVersion The current version
     * @return The list
     */
    public static List<String> getNextReleaseVersions( String strPreviousReleaseVersion )
    {
        List<String> listVersions = new ArrayList<>();
        try
        {
            Version version = parse( strPreviousReleaseVersion );
            listVersions.add( version.nextCandidate().getVersion() );
            listVersions.add( version.nextRelease().getVersion() );
            listVersions.add( version.nextMinor( false ).getVersion() );
            listVersions.add( version.nextMajor( false ).getVersion() );
        }
        catch( VersionParsingException ex )
        {
            AppLogService.error( "Error parsing version " + strPreviousReleaseVersion + " : " + ex.getMessage( ), ex );
        }
        return listVersions;
    }
    
    /**
     * Get the next snapshot version for a given version
     * @param strVersion The current version
     * @return The version
     */
    public static String getNextSnapshotVersion( String strVersion )
    {
        String strSnapshotVersion = NOT_AVAILABLE;
        try
        {
            Version version = Version.parse( strVersion );
            boolean bSnapshot = true;
            if( version.isCandidate() )
            {
                strSnapshotVersion = version.snapshot( ).getVersion();
            }   
            else
            {
                strSnapshotVersion = version.nextPatch( bSnapshot ).getVersion();
            }
        }
        catch( VersionParsingException ex )
        {
            AppLogService.error( "Error parsing version " + strVersion + " : " + ex.getMessage( ), ex );
        }
        return strSnapshotVersion;
    }
    
    /**
     * Get the next release version for a given version
     * @param strVersion The current version
     * @return The version
     */
    public static String getReleaseVersion( String strVersion )
    {
        String strTargetVersion = Version.NOT_AVAILABLE;
        try
        {
            strTargetVersion = Version.parse( strVersion ).nextRelease( ).getVersion( );
        }
        catch( VersionParsingException ex )
        {
            AppLogService.error( "Error parsing version " + strVersion + " : " + ex.getMessage( ), ex );
        }
        return strTargetVersion;
    }


}
