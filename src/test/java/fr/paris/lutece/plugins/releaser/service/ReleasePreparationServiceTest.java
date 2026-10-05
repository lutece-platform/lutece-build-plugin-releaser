/*
 * Copyright (c) 2002-2026, City of Paris
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
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.service.ReleasePreparationService.NonReleasableReason;

/**
 * Pure logic shared by the site and platform flows : core version of an aggregate, reasons a component cannot be released.
 */
public class ReleasePreparationServiceTest
{
    /**
     * A SNAPSHOT never published in Nexus is reported as such even when the parent of the last Nexus POM belongs to another core version
     * (the parent of a 8.0.0-SNAPSHOT absent from Nexus is unknown, the 7.0.1 read in the 1.0.1-SNAPSHOT POM proves nothing) ; the core
     * mismatch applies on the default branch only, once the SNAPSHOT is known in Nexus.
     */
    @Test
    public void testNonReleasableReasonOrder( )
    {
        Component unknown = snapshotComponent( "8.0.0-SNAPSHOT", "1.0.1-SNAPSHOT", "7.0.1" );
        assertEquals( NonReleasableReason.SNAPSHOT_UNKNOWN_IN_NEXUS, ReleasePreparationService.getNonReleasableReason( unknown, 8, "develop" ) );

        Component core7 = snapshotComponent( "1.0.1-SNAPSHOT", "1.0.1-SNAPSHOT", "7.0.1" );
        assertEquals( NonReleasableReason.BRANCH_CORE_MISMATCH, ReleasePreparationService.getNonReleasableReason( core7, 8, "develop" ) );
        assertNull( ReleasePreparationService.getNonReleasableReason( core7, 7, "develop" ) );

        core7.setBranchReleaseFrom( "develop_core7" );
        assertNull( ReleasePreparationService.getNonReleasableReason( core7, 8, "develop" ) );

        Component legacyParent = snapshotComponent( "1.0.1-SNAPSHOT", "1.0.1-SNAPSHOT", "6.0.0" );
        assertNull( ReleasePreparationService.getNonReleasableReason( legacyParent, 7, "develop" ) );
        assertEquals( NonReleasableReason.BRANCH_CORE_MISMATCH, ReleasePreparationService.getNonReleasableReason( legacyParent, 8, "develop" ) );

        Component noParent = snapshotComponent( "1.0.1-SNAPSHOT", "1.0.1-SNAPSHOT", null );
        assertNull( ReleasePreparationService.getNonReleasableReason( noParent, 8, "develop" ) );

        Component released = snapshotComponent( "1.0.1", "1.0.1-SNAPSHOT", "7.0.1" );
        assertNull( ReleasePreparationService.getNonReleasableReason( released, 8, "develop" ) );

        Component blocked = snapshotComponent( "1.0.1-SNAPSHOT", "1.0.1-SNAPSHOT", "7.0.1" );
        blocked.setBlockingReleaseComment( "already blocked" );
        assertNull( ReleasePreparationService.getNonReleasableReason( blocked, 8, "develop" ) );
    }

    /**
     * The core of an aggregate is the major of its parent version, brackets of a site-pom range stripped ; unreadable gives null. The core
     * line maps the parent numbering onto the core : site-pom 3.x and global-pom 6.x are core 7, 8.x is core 8.
     */
    @Test
    public void testCoreMajorAndLine( )
    {
        Site site = new Site( );
        assertNull( ReleasePreparationService.getCoreMajor( site ) );
        assertNull( ReleasePreparationService.getCoreLine( site ) );

        site.setParentVersion( "[3.0.2]" );
        assertEquals( Integer.valueOf( 3 ), ReleasePreparationService.getCoreMajor( site ) );
        assertEquals( Integer.valueOf( 7 ), ReleasePreparationService.getCoreLine( site ) );

        site.setParentVersion( "[7.0.5]" );
        assertEquals( Integer.valueOf( 7 ), ReleasePreparationService.getCoreLine( site ) );

        site.setParentVersion( "8.0.0" );
        assertEquals( Integer.valueOf( 8 ), ReleasePreparationService.getCoreLine( site ) );

        site.setParentVersion( "unknown" );
        assertNull( ReleasePreparationService.getCoreMajor( site ) );

        assertEquals( 7, ReleasePreparationService.toCoreLine( 6 ) );
        assertEquals( 8, ReleasePreparationService.toCoreLine( 9 ) );
    }

    /**
     * Only a real SNAPSHOT version counts, the Nexus sentinels and blanks do not.
     */
    @Test
    public void testParsableSnapshot( )
    {
        assertTrue( ReleasePreparationService.isParsableSnapshot( "1.0.1-SNAPSHOT" ) );
        assertFalse( ReleasePreparationService.isParsableSnapshot( "1.0.1" ) );
        assertFalse( ReleasePreparationService.isParsableSnapshot( "Snapshot not found" ) );
        assertFalse( ReleasePreparationService.isParsableSnapshot( null ) );
        assertFalse( ReleasePreparationService.isParsableSnapshot( " " ) );
    }

    private static Component snapshotComponent( String strCurrent, String strLastSnapshot, String strParentVersion )
    {
        Component component = new Component( );
        component.setArtifactId( "plugin-test" );
        component.setCurrentVersion( strCurrent );
        component.setLastAvailableSnapshotVersion( strLastSnapshot );
        component.setBranchReleaseFrom( "develop" );
        component.setIsProject( true );
        if ( strParentVersion != null )
        {
            component.setPomParentArtifactId( "lutece-global-pom" );
            component.setPomParentVersion( strParentVersion );
        }

        return component;
    }
}
