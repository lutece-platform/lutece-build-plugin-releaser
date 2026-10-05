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
package fr.paris.lutece.plugins.releaser.service.platform;

import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.platform.PlatformRelease;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseHome;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStatus;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStepHome;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseType;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepCode;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.util.ReferenceList;

/**
 * RBAC resource list of the campaigns and campaign lifecycle on the HSQL database
 */
public class PlatformReleaseResourceIdServiceTest extends LuteceTestCase
{
    private static final String NAME = "Lutece 8.0.3 RBAC";

    /**
     * A created campaign is listed as an RBAC resource and found by its id ; its steps are created with the first one ready ; removing it
     * cleans its steps.
     */
    public void testCampaignLifecycleAndResourceList( )
    {
        PlatformRelease campaign = PlatformReleaseService.createPlatformRelease( NAME, PlatformReleaseType.RC, 8, "admin" );
        assertTrue( campaign.getId( ) > 0 );
        assertEquals( PlatformStepCode.getStepCount( ), campaign.getSteps( ).size( ) );
        assertEquals( PlatformReleaseStatus.READY, campaign.getSteps( ).get( 0 ).getStatus( ) );
        assertEquals( PlatformReleaseStatus.TODO, campaign.getSteps( ).get( 1 ).getStatus( ) );

        PlatformReleaseResourceIdService service = new PlatformReleaseResourceIdService( );
        ReferenceList referenceList = service.getResourceIdList( Locale.FRENCH );
        assertTrue( referenceList.toMap( ).containsValue( NAME ) );
        assertEquals( NAME, service.getTitle( String.valueOf( campaign.getId( ) ), Locale.FRENCH ) );
        assertNull( service.getTitle( "not-a-number", Locale.FRENCH ) );

        PlatformReleaseService.skipStep( campaign, 1 );
        PlatformRelease stored = PlatformReleaseHome.findByPrimaryKeyWithSteps( campaign.getId( ) );
        assertEquals( PlatformReleaseStatus.SKIPPED, stored.getSteps( ).get( 0 ).getStatus( ) );
        assertEquals( PlatformReleaseStatus.READY, stored.getSteps( ).get( 1 ).getStatus( ) );
        assertEquals( 2, stored.getCurrentStep( ) );

        PlatformReleaseService.removePlatformRelease( campaign.getId( ) );
        assertNull( PlatformReleaseHome.findByPrimaryKey( campaign.getId( ) ) );
        assertTrue( PlatformReleaseStepHome.findByPlatformRelease( campaign.getId( ) ).isEmpty( ) );
    }
}
