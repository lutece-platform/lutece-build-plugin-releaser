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

import fr.paris.lutece.plugins.releaser.util.version.Version;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import static junit.framework.TestCase.*;

/**
 * SiteServiceTest
 */
public class SiteServiceTest
{
    @Ignore
    @Test
    public void testGetOriginVersion( )
    {
        System.out.println( "getOriginVersion" );

        String strLastRelease = null;
        String strCurrent = "3.2.1-SNAPSHOT";
        String strOrigin = SiteService.getOriginVersion( strLastRelease, strCurrent );
        printNextReleases( strLastRelease, strCurrent, strOrigin );
        assertEquals( strOrigin, strCurrent );

        strLastRelease = "3.2.1";
        strCurrent = "3.2.2-SNAPSHOT";
        strOrigin = SiteService.getOriginVersion( strLastRelease, strCurrent );
        printNextReleases( strLastRelease, strCurrent, strOrigin );
        assertEquals( strOrigin, strCurrent );

        strLastRelease = "3.2.1";
        strCurrent = "4.0.0-SNAPSHOT";
        strOrigin = SiteService.getOriginVersion( strLastRelease, strCurrent );
        printNextReleases( strLastRelease, strCurrent, strOrigin );
        assertEquals( strOrigin, strCurrent );

        strLastRelease = "3.2.1-RC-02";
        strCurrent = "3.2.1-SNAPSHOT";
        strOrigin = SiteService.getOriginVersion( strLastRelease, strCurrent );
        printNextReleases( strLastRelease, strCurrent, strOrigin );
        assertEquals( strOrigin, strLastRelease );

    }

    @Ignore
    private void printNextReleases( String strLastRelease, String strCurrent, String strOrigin )
    {
        System.out.print( "Last release:" + strLastRelease + "   Current: " + strCurrent + "   Origin:" + strOrigin );
        List<String> listNextReleases = Version.getNextReleaseVersions( strOrigin );
        int i = 0;
        for ( String strVersion : listNextReleases )
        {
            String strSeparator = ( i++ == 0 ) ? "\n -> next releases : " : ", ";
            System.out.print( strSeparator + strVersion );
        }
        i = 0;
        for ( String strVersion : listNextReleases )
        {
            String strSeparator = ( i++ == 0 ) ? "\n -> next snapshots : " : ", ";
            System.out.print( strSeparator + Version.getNextSnapshotVersion( strVersion ) );
        }
        System.out.print( '\n' );
    }

}
