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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformPlanResource;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleasePlan;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseType;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepCode;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepResult;

/**
 * Jenkins parameters and fallback result of a platform step
 */
public class PlatformStepTaskTest
{
    /**
     * A step pipeline receives the whole plan as JSON in a single parameter.
     *
     * @throws IOException
     *             on JSON error
     */
    @Test
    public void testStepPipelineParameters( ) throws IOException
    {
        PlatformReleasePlan plan = plan( PlatformStepCode.CORE, PlatformReleaseType.STABLE, "8.0.2", "8.0.3-SNAPSHOT", false );

        Map<String, String> mapParams = PlatformStepTask.buildJobParameters( plan );
        assertEquals( 1, mapParams.size( ) );
        assertTrue( mapParams.get( PlatformStepTask.PARAM_RELEASE_PLAN ).contains( "\"stepCode\":\"CORE\"" ) );
        assertTrue( mapParams.get( PlatformStepTask.PARAM_RELEASE_PLAN ).contains( "\"targetVersion\":\"8.0.2\"" ) );
    }

    /**
     * The last step drives the existing lutece-platform pipeline with its own parameters, the pre-release number being read from the version.
     *
     * @throws IOException
     *             on JSON error
     */
    @Test
    public void testPlatformPipelineParameters( ) throws IOException
    {
        PlatformReleasePlan plan = plan( PlatformStepCode.PLATFORM_STARTERS, PlatformReleaseType.BETA, "8.0.0-beta-02", "8.0.0-SNAPSHOT", true );

        Map<String, String> mapParams = PlatformStepTask.buildJobParameters( plan );
        assertEquals( PlatformStepTask.RELEASE_TARGET_ALL, mapParams.get( PlatformStepTask.PARAM_RELEASE_TARGET ) );
        assertEquals( "8.0.0-beta-02", mapParams.get( PlatformStepTask.PARAM_RELEASE_VERSION ) );
        assertEquals( "8.0.0-SNAPSHOT", mapParams.get( PlatformStepTask.PARAM_NEXT_SNAPSHOT_VERSION ) );
        assertEquals( "true", mapParams.get( PlatformStepTask.PARAM_DRY_RUN ) );
        assertEquals( PlatformStepTask.PRERELEASE_BETA, mapParams.get( PlatformStepTask.PARAM_PRERELEASE_TYPE ) );
        assertEquals( "02", mapParams.get( PlatformStepTask.PARAM_PRERELEASE_NUMBER ) );
        assertEquals( "8", mapParams.get( PlatformStepTask.PARAM_LUTECE_MAJOR ) );
        assertEquals( "develop", mapParams.get( PlatformStepTask.PARAM_MONOREPO_BRANCH ) );
        assertEquals( "", mapParams.get( PlatformStepTask.PARAM_MASTER_BRANCH ) );
        assertEquals( "", mapParams.get( PlatformStepTask.PARAM_CORE_VERSION ) );
        assertEquals( "false", mapParams.get( PlatformStepTask.PARAM_ALLOW_SNAPSHOT_DEPENDENCIES ) );
        assertEquals( PlatformStepTask.GIT_USER_NAME, mapParams.get( PlatformStepTask.PARAM_GIT_USER_NAME ) );

        plan.getAggregate( ).setMasterBranch( "master" );
        plan.getAggregate( ).setCoreVersion( "8.0.1" );
        plan.getPipelineParameters( ).put( PlatformStepTask.PARAM_ALLOW_SNAPSHOT_DEPENDENCIES, "true" );
        plan.getPipelineParameters( ).put( PlatformStepTask.PARAM_RELEASE_TARGET, "lutece-bom" );
        mapParams = PlatformStepTask.buildJobParameters( plan );
        assertEquals( "master", mapParams.get( PlatformStepTask.PARAM_MASTER_BRANCH ) );
        assertEquals( "8.0.1", mapParams.get( PlatformStepTask.PARAM_CORE_VERSION ) );
        assertEquals( "true", mapParams.get( PlatformStepTask.PARAM_ALLOW_SNAPSHOT_DEPENDENCIES ) );
        assertEquals( "lutece-bom", mapParams.get( PlatformStepTask.PARAM_RELEASE_TARGET ) );
        assertEquals( "8", mapParams.get( PlatformStepTask.PARAM_LUTECE_MAJOR ) );
    }

    /**
     * Pre-release type and number derived from the campaign type and the target version.
     */
    /**
     * An UNSTABLE build of the lutece-platform pipeline published its release despite a warning.
     */
    @Test
    public void testPublishedResults( )
    {
        assertTrue( PlatformStepTask.isPublished( "SUCCESS" ) );
        assertTrue( PlatformStepTask.isPublished( "UNSTABLE" ) );
        assertTrue( !PlatformStepTask.isPublished( "FAILURE" ) );
        assertTrue( !PlatformStepTask.isPublished( "ABORTED" ) );
        assertTrue( !PlatformStepTask.isPublished( null ) );
    }

    @Test
    public void testCredentialParameters( )
    {
        assertTrue( PlatformStepTask.buildCredentialParameters( null ).isEmpty( ) );

        ReleaserUser user = new ReleaserUser( );
        user.addCredential( RepositoryType.GITHUB, user.new Credential( "jdoe", "ghp_token" ) );
        Map<String, String> mapParams = PlatformStepTask.buildCredentialParameters( user );
        assertEquals( "jdoe", mapParams.get( PlatformStepTask.PARAM_GITHUB_LOGIN ) );
        assertEquals( "ghp_token", mapParams.get( PlatformStepTask.PARAM_GITHUB_TOKEN ) );
        assertFalse( mapParams.containsKey( PlatformStepTask.PARAM_GITLAB_LOGIN ) );

        user.addCredential( RepositoryType.GITLAB, user.new Credential( "jdoe", "" ) );
        assertFalse( PlatformStepTask.buildCredentialParameters( user ).containsKey( PlatformStepTask.PARAM_GITLAB_TOKEN ) );
    }

    @Test
    public void testPrerelease( )
    {
        assertEquals( PlatformStepTask.PRERELEASE_STABLE, PlatformStepTask.getPrereleaseType( PlatformReleaseType.STABLE ) );
        assertEquals( PlatformStepTask.PRERELEASE_RC, PlatformStepTask.getPrereleaseType( PlatformReleaseType.RC ) );
        assertEquals( "03", PlatformStepTask.getPrereleaseNumber( "7.2.0-RC-03" ) );
        assertEquals( "", PlatformStepTask.getPrereleaseNumber( "7.2.0" ) );
        assertEquals( "", PlatformStepTask.getPrereleaseNumber( null ) );
    }

    /**
     * Without report from Jenkins, the result assumes every resource was released at its target version.
     */
    @Test
    public void testResultFromPlan( )
    {
        PlatformReleasePlan plan = plan( PlatformStepCode.GLOBAL_POM, PlatformReleaseType.STABLE, "8.0.2", "8.0.3-SNAPSHOT", false );
        PlatformPlanResource component = new PlatformPlanResource( );
        component.setGroupId( "fr.paris.lutece.tools" );
        component.setArtifactId( "build-config" );
        component.setTargetVersion( "3.0.1" );
        plan.getComponents( ).add( component );

        PlatformStepResult result = PlatformStepTask.buildResultFromPlan( plan );
        assertEquals( "3.0.1", result.getReleasedVersions( ).get( "fr.paris.lutece.tools:build-config" ) );
        assertEquals( "8.0.2", result.getAggregateVersion( ) );
        assertTrue( result.getReport( ).contains( PlatformStepTask.STEP_REPORT_ARTIFACT ) );
    }

    /**
     * Builds a plan with an aggregate on the develop branch.
     *
     * @param code
     *            the step code
     * @param releaseType
     *            the release type
     * @param strTarget
     *            the aggregate target version
     * @param strNext
     *            the aggregate next snapshot
     * @param bDryRun
     *            the dry run flag
     * @return the plan
     */
    private static PlatformReleasePlan plan( PlatformStepCode code, PlatformReleaseType releaseType, String strTarget, String strNext, boolean bDryRun )
    {
        PlatformReleasePlan plan = new PlatformReleasePlan( );
        plan.setCampaignId( 1 );
        plan.setStep( code.getStepNumber( ) );
        plan.setStepCode( code );
        plan.setReleaseType( releaseType );
        plan.setCoreMajor( 8 );
        plan.setDryRun( bDryRun );

        PlatformPlanResource aggregate = new PlatformPlanResource( );
        aggregate.setGroupId( "fr.paris.lutece" );
        aggregate.setArtifactId( "lutece-parent" );
        aggregate.setBranch( "develop" );
        aggregate.setTargetVersion( strTarget );
        aggregate.setNextSnapshotVersion( strNext );
        plan.setAggregate( aggregate );

        return plan;
    }
}
