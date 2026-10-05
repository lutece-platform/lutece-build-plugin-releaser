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
package fr.paris.lutece.plugins.releaser.business.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import fr.paris.lutece.plugins.releaser.util.MapperJsonUtil;

/**
 * JSON round trip of the releaser to Jenkins contract
 */
public class PlatformReleasePlanTest
{
    /**
     * A plan survives a JSON round trip, including enums, the nested aggregate and the version updates maps.
     *
     * @throws IOException
     *             on JSON error
     */
    @Test
    public void testPlanRoundTrip( ) throws IOException
    {
        PlatformReleasePlan plan = new PlatformReleasePlan( );
        plan.setCampaignId( 12 );
        plan.setCampaignName( "Lutece 8.0.2" );
        plan.setStep( 1 );
        plan.setStepCode( PlatformStepCode.GLOBAL_POM );
        plan.setReleaseType( PlatformReleaseType.STABLE );
        plan.setCoreMajor( 8 );
        plan.setDryRun( true );

        PlatformPlanResource component = new PlatformPlanResource( );
        component.setGroupId( "fr.paris.lutece.tools" );
        component.setArtifactId( "build-config" );
        component.setType( "jar" );
        component.setScmUrl( "https://github.com/lutece-platform/tools-maven-build-config.git" );
        component.setBranch( "develop" );
        component.setMasterBranch( "master" );
        component.setCurrentVersion( "3.0.1-SNAPSHOT" );
        component.setTargetVersion( "3.0.1" );
        component.setNextSnapshotVersion( "3.0.2-SNAPSHOT" );
        plan.getComponents( ).add( component );

        PlatformPlanResource aggregate = new PlatformPlanResource( );
        aggregate.setGroupId( "fr.paris.lutece.tools" );
        aggregate.setArtifactId( "lutece-global-pom" );
        aggregate.setType( "pom" );
        aggregate.setCurrentVersion( "8.0.2-SNAPSHOT" );
        aggregate.setTargetVersion( "8.0.2" );
        aggregate.setNextSnapshotVersion( "8.0.3-SNAPSHOT" );
        aggregate.getVersionUpdates( ).getProperties( ).put( "maven-lutece-plugin.version", "7.1.1" );
        aggregate.getVersionUpdates( ).getDependencies( ).put( "fr.paris.lutece.tools:build-config", "3.0.1" );
        plan.setAggregate( aggregate );

        String strJson = MapperJsonUtil.getJson( plan );
        assertTrue( strJson.contains( "\"stepCode\":\"GLOBAL_POM\"" ) );
        assertTrue( strJson.contains( "\"releaseType\":\"STABLE\"" ) );

        PlatformReleasePlan parsed = MapperJsonUtil.parse( strJson, PlatformReleasePlan.class );
        assertEquals( 12, parsed.getCampaignId( ) );
        assertEquals( PlatformStepCode.GLOBAL_POM, parsed.getStepCode( ) );
        assertEquals( PlatformReleaseType.STABLE, parsed.getReleaseType( ) );
        assertTrue( parsed.isDryRun( ) );
        assertEquals( 1, parsed.getComponents( ).size( ) );
        assertEquals( "fr.paris.lutece.tools:build-config", parsed.getComponents( ).get( 0 ).getCoordinates( ) );
        assertEquals( "master", parsed.getComponents( ).get( 0 ).getMasterBranch( ) );
        assertNull( parsed.getComponents( ).get( 0 ).getParentVersion( ) );
        assertTrue( parsed.getComponents( ).get( 0 ).getVersionUpdates( ).isEmpty( ) );
        assertEquals( "7.1.1", parsed.getAggregate( ).getVersionUpdates( ).getProperties( ).get( "maven-lutece-plugin.version" ) );
        assertEquals( "3.0.1", parsed.getAggregate( ).getVersionUpdates( ).getDependencies( ).get( "fr.paris.lutece.tools:build-config" ) );
    }

    /**
     * A step report written by the pipeline is parsed, unknown fields being ignored.
     *
     * @throws IOException
     *             on JSON error
     */
    @Test
    public void testStepResultParse( ) throws IOException
    {
        String strJson = "{\"releasedVersions\":{\"fr.paris.lutece.tools:build-config\":\"3.0.1\"},\"aggregateVersion\":\"8.0.2\",\"report\":\"ok\",\"buildNumber\":42}";

        PlatformStepResult result = MapperJsonUtil.parse( strJson, PlatformStepResult.class );
        assertEquals( "3.0.1", result.getReleasedVersions( ).get( "fr.paris.lutece.tools:build-config" ) );
        assertEquals( "8.0.2", result.getAggregateVersion( ) );
        assertEquals( "ok", result.getReport( ) );
    }
}
