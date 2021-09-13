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
package fr.paris.lutece.plugins.releaser.util.version;

import java.util.List;
import static junit.framework.TestCase.*;
import org.junit.Test;

/**
 * VersionTest
 */
public class VersionTest
{
    /**
     * Test of parse method, of class Version.
     * 
     * @throws fr.paris.lutece.plugins.releaser.util.version.VersionParsingException
     */
    @Test
    public void testParse( ) throws VersionParsingException
    {
        System.out.println( "parse" );
        String strSource = "12.10.23";
        Version result = Version.parse( strSource );
        assertEquals( strSource, result.getVersion( ) );
        System.out.println( result.getVersion( ) );

        strSource = "12.10";
        result = Version.parse( strSource );
        assertEquals( "12.10.0", result.getVersion( ) );
        System.out.println( result.getVersion( ) );

        strSource = "12.10.23-SNAPSHOT";
        result = Version.parse( strSource );
        assertEquals( strSource, result.getVersion( ) );
        System.out.println( result.getVersion( ) );

        strSource = "12.10-SNAPSHOT";
        result = Version.parse( strSource );
        assertEquals( "12.10.0-SNAPSHOT", result.getVersion( ) );
        System.out.println( result.getVersion( ) );

        strSource = "12.10.0-RC-1";
        result = Version.parse( strSource );
        assertEquals( "12.10.0-RC-01", result.getVersion( ) );
        System.out.println( result.getVersion( ) );
    }

    @Test
    public void testGetNextReleaseVersions( )
    {
        System.out.println( "getNextReleaseVersions" );
        String strSource = "5.5.25-SNAPSHOT";
        List<String> listVersions = Version.getNextReleaseVersions( strSource );
        System.out.println( "Next release versions for : " + strSource );
        for ( String strVersion : listVersions )
        {
            System.out.println( "- " + strVersion );
        }
        // assertEquals( listVersions.get( 0 ), "12.10.24-RC-01" );
        // assertEquals( listVersions.get( 1 ), "12.10.24" );
        // assertEquals( listVersions.get( 2 ), "12.11.0" );
        // assertEquals( listVersions.get( 3 ), "13.0.0" );

        strSource = "5.5.25-RC-01";
        listVersions = Version.getNextReleaseVersions( strSource );
        System.out.println( "Next release versions for : " + strSource );
        for ( String strVersion : listVersions )
        {
            System.out.println( "- " + strVersion );
        }
        assertEquals( listVersions.get( 0 ), "12.10.23-RC-02" );
        assertEquals( listVersions.get( 1 ), "12.10.23" );
        assertEquals( listVersions.get( 2 ), "12.11.0" );
        assertEquals( listVersions.get( 3 ), "13.0.0" );

        strSource = "12.10.23-SNAPSHOT";
        listVersions = Version.getNextReleaseVersions( strSource );
        System.out.println( "Next release versions for : " + strSource );
        for ( String strVersion : listVersions )
        {
            System.out.println( "- " + strVersion );
        }
        assertEquals( listVersions.get( 0 ), "12.10.23-RC-01" );
        assertEquals( listVersions.get( 1 ), "12.10.23" );
        assertEquals( listVersions.get( 2 ), "12.11.0" );
        assertEquals( listVersions.get( 3 ), "13.0.0" );

    }

}
