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

import java.sql.Timestamp;
import java.util.List;

import fr.paris.lutece.test.LuteceTestCase;

/**
 * Persistence test of the platform release campaign and its steps
 */
public class PlatformReleaseBusinessTest extends LuteceTestCase
{
    private static final String NAME1 = "Lutece 8.0.2";
    private static final String NAME2 = "Lutece 8.0.2 beta";
    private static final String USER = "admin";
    private static final String PLAN = "{\"step\":1}";
    private static final String RESULT = "{\"released\":[]}";
    private static final String BUILD_URL = "https://jenkins/job/lutece-release-platform-step/12/";

    /**
     * Create, update, load and remove a campaign with its steps.
     */
    public void testBusiness( )
    {
        PlatformRelease platformRelease = new PlatformRelease( );
        platformRelease.setName( NAME1 );
        platformRelease.setReleaseType( PlatformReleaseType.STABLE );
        platformRelease.setCoreMajor( 8 );
        platformRelease.setUserName( USER );
        platformRelease.setDateCreation( new Timestamp( System.currentTimeMillis( ) ) );
        platformRelease.setDateUpdate( platformRelease.getDateCreation( ) );

        PlatformReleaseHome.create( platformRelease );
        assertTrue( platformRelease.getId( ) > 0 );

        PlatformRelease stored = PlatformReleaseHome.findByPrimaryKey( platformRelease.getId( ) );
        assertEquals( NAME1, stored.getName( ) );
        assertEquals( PlatformReleaseType.STABLE, stored.getReleaseType( ) );
        assertEquals( 8, stored.getCoreMajor( ) );
        assertEquals( 1, stored.getCurrentStep( ) );
        assertEquals( PlatformReleaseStatus.READY, stored.getStatus( ) );
        assertFalse( stored.isExportVerified( ) );
        assertEquals( USER, stored.getUserName( ) );

        platformRelease.setName( NAME2 );
        platformRelease.setReleaseType( PlatformReleaseType.BETA );
        platformRelease.setCurrentStep( 2 );
        platformRelease.setStatus( PlatformReleaseStatus.RUNNING );
        platformRelease.setExportVerified( true );
        PlatformReleaseHome.update( platformRelease );

        stored = PlatformReleaseHome.findByPrimaryKey( platformRelease.getId( ) );
        assertEquals( NAME2, stored.getName( ) );
        assertEquals( PlatformReleaseType.BETA, stored.getReleaseType( ) );
        assertEquals( 2, stored.getCurrentStep( ) );
        assertEquals( PlatformReleaseStatus.RUNNING, stored.getStatus( ) );
        assertTrue( stored.isExportVerified( ) );

        PlatformReleaseStep step2 = createStep( platformRelease.getId( ), 2 );
        PlatformReleaseStep step1 = createStep( platformRelease.getId( ), 1 );

        step1.setStatus( PlatformReleaseStatus.SUCCESS );
        step1.setPlanJson( PLAN );
        step1.setResultJson( RESULT );
        step1.setJenkinsBuildUrl( BUILD_URL );
        step1.setJenkinsBuildNumber( 12 );
        step1.setJenkinsResult( "SUCCESS" );
        step1.setDateBegin( new Timestamp( System.currentTimeMillis( ) ) );
        step1.setDateEnd( new Timestamp( System.currentTimeMillis( ) ) );
        PlatformReleaseStepHome.update( step1 );

        PlatformReleaseStep storedStep = PlatformReleaseStepHome.findByPrimaryKey( step1.getId( ) );
        assertEquals( PlatformReleaseStatus.SUCCESS, storedStep.getStatus( ) );
        assertEquals( PLAN, storedStep.getPlanJson( ) );
        assertEquals( RESULT, storedStep.getResultJson( ) );
        assertEquals( BUILD_URL, storedStep.getJenkinsBuildUrl( ) );
        assertEquals( 12, storedStep.getJenkinsBuildNumber( ) );
        assertEquals( "SUCCESS", storedStep.getJenkinsResult( ) );
        assertNotNull( storedStep.getDateBegin( ) );
        assertNotNull( storedStep.getDateEnd( ) );

        PlatformRelease withSteps = PlatformReleaseHome.findByPrimaryKeyWithSteps( platformRelease.getId( ) );
        List<PlatformReleaseStep> listSteps = withSteps.getSteps( );
        assertEquals( 2, listSteps.size( ) );
        assertEquals( 1, listSteps.get( 0 ).getStepNumber( ) );
        assertEquals( 2, listSteps.get( 1 ).getStepNumber( ) );
        assertEquals( step2.getId( ), listSteps.get( 1 ).getId( ) );

        assertFalse( PlatformReleaseHome.getPlatformReleasesList( ).isEmpty( ) );

        PlatformReleaseHome.remove( platformRelease.getId( ) );
        assertNull( PlatformReleaseHome.findByPrimaryKey( platformRelease.getId( ) ) );
        assertTrue( PlatformReleaseStepHome.findByPlatformRelease( platformRelease.getId( ) ).isEmpty( ) );
    }

    /**
     * The 5 step definitions are initialized by init_db_release_platform.sql and can be updated.
     */
    public void testStepDefinitions( )
    {
        List<PlatformStepDefinition> listDefinitions = PlatformStepDefinitionHome.findAll( );
        assertEquals( PlatformStepCode.getStepCount( ), listDefinitions.size( ) );

        for ( PlatformStepCode code : PlatformStepCode.values( ) )
        {
            PlatformStepDefinition definition = listDefinitions.get( code.getStepNumber( ) - 1 );
            assertEquals( code.getStepNumber( ), definition.getStepNumber( ) );
            assertEquals( code, definition.getCode( ) );
            assertTrue( definition.getScmUrl( ).startsWith( "https://github.com/lutece-platform/" ) );
        }

        PlatformStepDefinition core = PlatformStepDefinitionHome.findByCode( PlatformStepCode.CORE );
        String strOriginalUrl = core.getScmUrl( );
        core.setScmUrl( "https://github.com/lutece-platform/lutece-core-fork.git" );
        PlatformStepDefinitionHome.update( core );
        assertEquals( core.getScmUrl( ), PlatformStepDefinitionHome.findByStepNumber( PlatformStepCode.CORE.getStepNumber( ) ).getScmUrl( ) );

        core.setScmUrl( strOriginalUrl );
        PlatformStepDefinitionHome.update( core );
    }

    /**
     * Creates a step in TODO status.
     *
     * @param nIdPlatformRelease
     *            the campaign id
     * @param nStepNumber
     *            the step number
     * @return the created step
     */
    private static PlatformReleaseStep createStep( int nIdPlatformRelease, int nStepNumber )
    {
        PlatformReleaseStep step = new PlatformReleaseStep( );
        step.setIdPlatformRelease( nIdPlatformRelease );
        step.setStepNumber( nStepNumber );
        PlatformReleaseStepHome.create( step );
        assertTrue( step.getId( ) > 0 );

        return step;
    }
}
