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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformPlanResource;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformRelease;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleasePlan;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStatus;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseStep;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseType;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepCode;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepResult;
import fr.paris.lutece.plugins.releaser.business.platform.PomVersionDecision;
import fr.paris.lutece.plugins.releaser.util.MapperJsonUtil;
import fr.paris.lutece.plugins.releaser.util.version.Version;

/**
 * Pure logic of the platform release service : version selection by release type, release order, step locking, CSV export
 */
public class PlatformReleaseServiceTest
{
    /**
     * The candidate versions of 4.0.2-SNAPSHOT (see Version.getNextReleaseVersions) : RC, stable, minor, major, beta.
     */
    private static final List<String> CANDIDATES = Version.getNextReleaseVersions( "4.0.2-SNAPSHOT", "4.0.1" );

    /**
     * Each release type picks its own entry, stable being the plain version.
     */
    @Test
    public void testSelectTargetVersion( )
    {
        assertEquals( "4.0.2", PlatformReleaseService.selectTargetVersion( CANDIDATES, "4.0.2", PlatformReleaseType.STABLE ) );
        assertTrue( Version.isCandidate( PlatformReleaseService.selectTargetVersion( CANDIDATES, "4.0.2", PlatformReleaseType.RC ) ) );
        assertTrue( Version.isBeta( PlatformReleaseService.selectTargetVersion( CANDIDATES, "4.0.2", PlatformReleaseType.BETA ) ) );
        assertEquals( "4.0.2", PlatformReleaseService.selectTargetVersion( Arrays.asList( "4.0.2-SNAPSHOT" ), "4.0.2", PlatformReleaseType.BETA ) );
    }

    /**
     * A flagged snapshot component gets the version of the campaign type and the matching next snapshot.
     */
    @Test
    public void testApplyReleaseTypeOnComponent( )
    {
        Component component = component( "plugin-forms", "4.0.2-SNAPSHOT" );
        component.setIsProject( true );
        component.setTargetVersions( CANDIDATES );
        component.setTargetVersion( "4.0.2" );

        PlatformReleaseService.applyReleaseType( component, PlatformReleaseType.RC );
        assertTrue( Version.isCandidate( component.getTargetVersion( ) ) );
        assertEquals( "4.0.2-SNAPSHOT", component.getNextSnapshotVersion( ) );
        assertEquals( CANDIDATES.indexOf( component.getTargetVersion( ) ), component.getTargetVersionIndex( ) );

        PlatformReleaseService.applyReleaseType( component, PlatformReleaseType.STABLE );
        assertEquals( "4.0.2", component.getTargetVersion( ) );
        assertEquals( "4.0.3-SNAPSHOT", component.getNextSnapshotVersion( ) );

        Component notFlagged = component( "plugin-workflow", "4.0.2-SNAPSHOT" );
        notFlagged.setTargetVersions( CANDIDATES );
        notFlagged.setTargetVersion( "4.0.2-SNAPSHOT" );
        PlatformReleaseService.applyReleaseType( notFlagged, PlatformReleaseType.RC );
        assertEquals( "4.0.2-SNAPSHOT", notFlagged.getTargetVersion( ) );
    }

    /**
     * Libraries first, then other artifacts, plugins, modules ; POM order kept inside a group.
     */
    @Test
    public void testSortComponents( )
    {
        List<Component> listComponents = new ArrayList<>( );
        listComponents.add( component( "module-forms-solr", "1.0.0-SNAPSHOT" ) );
        listComponents.add( component( "plugin-forms", "1.0.0-SNAPSHOT" ) );
        listComponents.add( component( "lutece-core", "1.0.0-SNAPSHOT" ) );
        listComponents.add( component( "library-workflow-core", "1.0.0-SNAPSHOT" ) );
        listComponents.add( component( "plugin-workflow", "1.0.0-SNAPSHOT" ) );
        listComponents.add( component( "library-lucene", "1.0.0-SNAPSHOT" ) );

        List<Component> listSorted = PlatformReleaseService.sortComponents( listComponents );
        assertEquals( "library-workflow-core", listSorted.get( 0 ).getArtifactId( ) );
        assertEquals( "library-lucene", listSorted.get( 1 ).getArtifactId( ) );
        assertEquals( "lutece-core", listSorted.get( 2 ).getArtifactId( ) );
        assertEquals( "plugin-forms", listSorted.get( 3 ).getArtifactId( ) );
        assertEquals( "plugin-workflow", listSorted.get( 4 ).getArtifactId( ) );
        assertEquals( "module-forms-solr", listSorted.get( 5 ).getArtifactId( ) );
        assertEquals( 6, listComponents.size( ) );
    }

    /**
     * A step is locked until the previous one is completed ; global-pom, site-pom and core can be skipped once preparable, the plugins and
     * starters steps never ; the campaign type is forced to stable on the stable-only steps.
     */
    @Test
    public void testStepLocking( )
    {
        PlatformRelease campaign = campaign( PlatformReleaseType.BETA );

        assertTrue( PlatformReleaseService.canPrepareStep( campaign, 1 ) );
        assertTrue( !PlatformReleaseService.canPrepareStep( campaign, 2 ) );
        assertTrue( PlatformReleaseService.canSkipStep( campaign, 1 ) );
        assertTrue( !PlatformReleaseService.canSkipStep( campaign, 3 ) );
        assertTrue( !PlatformReleaseService.canSkipStep( campaign, 5 ) );

        PlatformReleaseService.getStep( campaign, 1 ).setStatus( PlatformReleaseStatus.SKIPPED );
        assertTrue( !PlatformReleaseService.canPrepareStep( campaign, 1 ) );
        assertTrue( PlatformReleaseService.canPrepareStep( campaign, 2 ) );
        assertTrue( !PlatformReleaseService.canPrepareStep( campaign, 3 ) );

        PlatformReleaseService.getStep( campaign, 2 ).setStatus( PlatformReleaseStatus.SKIPPED );
        assertTrue( PlatformReleaseService.canSkipStep( campaign, 3 ) );
        PlatformReleaseService.getStep( campaign, 3 ).setStatus( PlatformReleaseStatus.SKIPPED );
        assertTrue( PlatformReleaseService.canPrepareStep( campaign, 4 ) );
        assertTrue( !PlatformReleaseService.canSkipStep( campaign, 4 ) );
        PlatformReleaseService.getStep( campaign, 4 ).setStatus( PlatformReleaseStatus.SUCCESS );
        assertTrue( !PlatformReleaseService.canPrepareStep( campaign, 5 ) );
        campaign.setExportVerified( true );
        assertTrue( PlatformReleaseService.canPrepareStep( campaign, 5 ) );
        assertTrue( !PlatformReleaseService.canSkipStep( campaign, 5 ) );

        PlatformReleaseService.getStep( campaign, 2 ).setStatus( PlatformReleaseStatus.RUNNING );
        assertTrue( !PlatformReleaseService.canPrepareStep( campaign, 2 ) );

        assertEquals( PlatformReleaseType.STABLE, PlatformReleaseService.getStepReleaseType( campaign, PlatformStepCode.SITE_POM ) );
        assertEquals( PlatformReleaseType.BETA, PlatformReleaseService.getStepReleaseType( campaign, PlatformStepCode.PLATFORM_PLUGINS ) );
    }

    /**
     * The plan of a step holds the flagged components in release order and, for the aggregate, a property update when the version is held by a
     * POM property and a dependency update otherwise. A non flagged component is not released and does not update the aggregate.
     */
    @Test
    public void testBuildPlan( )
    {
        PlatformRelease campaign = campaign( PlatformReleaseType.BETA );
        campaign.setName( "Lutece 8.0.2 beta" );
        campaign.setCoreMajor( 8 );

        Site site = new Site( );
        site.setGroupId( "fr.paris.lutece" );
        site.setArtifactId( "lutece-parent" );
        site.setScmUrl( "https://github.com/lutece-platform/lutece-platform.git" );
        site.setBranchReleaseFrom( "develop" );
        site.setVersion( "8.0.0-SNAPSHOT" );
        site.setNextReleaseVersion( "8.0.0-beta-01" );
        site.setNextSnapshotVersion( "8.0.0-SNAPSHOT" );

        site.addCurrentDependency( dependency( "plugin-forms", "4.0.2-SNAPSHOT", "lutece.plugin-forms.version" ) );
        site.addCurrentDependency( dependency( "library-workflow-core", "4.0.2-SNAPSHOT", null ) );
        site.addCurrentDependency( dependency( "plugin-workflow", "4.0.2-SNAPSHOT", "lutece.plugin-workflow.version" ) );

        Component forms = component( "plugin-forms", "4.0.2-SNAPSHOT" );
        forms.setIsProject( true );
        forms.setTargetVersions( CANDIDATES );
        forms.setTargetVersion( "4.0.2" );
        forms.setBranchReleaseFrom( "develop" );
        PlatformReleaseService.applyReleaseType( forms, PlatformReleaseType.BETA );

        Component workflowCore = component( "library-workflow-core", "4.0.2-SNAPSHOT" );
        workflowCore.setIsProject( true );
        workflowCore.setTargetVersions( CANDIDATES );
        workflowCore.setTargetVersion( "4.0.2" );
        workflowCore.setBranchReleaseFrom( "develop" );
        PlatformReleaseService.applyReleaseType( workflowCore, PlatformReleaseType.BETA );

        Component workflow = component( "plugin-workflow", "4.0.2-SNAPSHOT" );
        workflow.setTargetVersion( "4.0.2-SNAPSHOT" );

        site.addComponent( forms );
        site.addComponent( workflowCore );
        site.addComponent( workflow );

        PlatformReleasePlan plan = PlatformReleaseService.buildPlan( campaign, PlatformStepCode.PLATFORM_PLUGINS.getStepNumber( ), site, true );

        assertEquals( PlatformStepCode.PLATFORM_PLUGINS, plan.getStepCode( ) );
        assertEquals( PlatformReleaseType.BETA, plan.getReleaseType( ) );
        assertTrue( plan.isDryRun( ) );
        assertEquals( 2, plan.getComponents( ).size( ) );
        assertEquals( "library-workflow-core", plan.getComponents( ).get( 0 ).getArtifactId( ) );
        assertEquals( "plugin-forms", plan.getComponents( ).get( 1 ).getArtifactId( ) );
        assertTrue( Version.isBeta( plan.getComponents( ).get( 1 ).getTargetVersion( ) ) );
        assertEquals( null, plan.getComponents( ).get( 1 ).getMasterBranch( ) );

        PlatformPlanResource aggregate = plan.getAggregate( );
        assertEquals( "lutece-parent", aggregate.getArtifactId( ) );
        assertEquals( "8.0.0-beta-01", aggregate.getTargetVersion( ) );
        assertEquals( null, aggregate.getParentVersion( ) );
        assertEquals( 1, aggregate.getVersionUpdates( ).getProperties( ).size( ) );
        assertEquals( forms.getTargetVersion( ), aggregate.getVersionUpdates( ).getProperties( ).get( "lutece.plugin-forms.version" ) );
        assertEquals( 1, aggregate.getVersionUpdates( ).getDependencies( ).size( ) );
        assertEquals( workflowCore.getTargetVersion( ), aggregate.getVersionUpdates( ).getDependencies( ).get( "fr.paris.lutece.plugins:library-workflow-core" ) );
    }

    /**
     * The master branch follows the target version, not the type of the campaign : a stable component released in a beta campaign is merged,
     * a beta or RC component released in a stable campaign is not.
     */
    @Test
    public void testMasterBranchFollowsTargetVersion( )
    {
        assertTrue( PlatformReleaseService.isMergedIntoMaster( "4.0.2" ) );
        assertFalse( PlatformReleaseService.isMergedIntoMaster( "4.0.2-beta-01" ) );
        assertFalse( PlatformReleaseService.isMergedIntoMaster( "4.0.2-RC-02" ) );
        assertFalse( PlatformReleaseService.isMergedIntoMaster( null ) );

        assertEquals( null, PlatformReleaseService.getMasterBranch( "develop", "4.0.2-beta-01" ) );
        assertEquals( null, PlatformReleaseService.getMasterBranch( "release-4.0.x", "4.0.2" ) );

        Site site = new Site( );
        site.setGroupId( "fr.paris.lutece" );
        site.setArtifactId( "lutece-parent" );
        site.setBranchReleaseFrom( "release-8.0.x" );
        site.setVersion( "8.0.0-SNAPSHOT" );
        site.setNextReleaseVersion( "8.0.0" );

        Component betaInStable = component( "plugin-forms", "4.0.2-SNAPSHOT" );
        betaInStable.setIsProject( true );
        betaInStable.setTargetVersion( "4.0.2-beta-01" );
        betaInStable.setBranchReleaseFrom( "develop" );
        site.addComponent( betaInStable );

        PlatformReleasePlan plan = PlatformReleaseService.buildPlan( campaign( PlatformReleaseType.STABLE ), PlatformStepCode.CORE.getStepNumber( ), site,
                true );
        assertEquals( null, plan.getComponents( ).get( 0 ).getMasterBranch( ) );
    }

    /**
     * A flagged component whose snapshot is not the last one available was already released : it is left out of the plan, like in a site
     * release, and only its released version goes to the aggregate POM.
     */
    @Test
    public void testAlreadyReleasedComponentLeftOutOfPlan( )
    {
        Site site = new Site( );
        site.setGroupId( "fr.paris.lutece" );
        site.setArtifactId( "lutece-parent" );
        site.setBranchReleaseFrom( "release-8.0.x" );
        site.setVersion( "8.0.0-SNAPSHOT" );
        site.setNextReleaseVersion( "8.0.0" );
        site.addCurrentDependency( dependency( "plugin-forms", "4.0.2-SNAPSHOT", "lutece.plugin-forms.version" ) );
        site.addCurrentDependency( dependency( "plugin-workflow", "1.0.13-SNAPSHOT", "lutece.plugin-workflow.version" ) );

        Component toRelease = component( "plugin-forms", "4.0.2-SNAPSHOT" );
        toRelease.setIsProject( true );
        toRelease.setTargetVersion( "4.0.2" );
        toRelease.setBranchReleaseFrom( "release-4.0.x" );
        site.addComponent( toRelease );

        Component alreadyReleased = component( "plugin-workflow", "1.0.13-SNAPSHOT" );
        alreadyReleased.setIsProject( true );
        alreadyReleased.setLastAvailableSnapshotVersion( "1.0.14-SNAPSHOT" );
        alreadyReleased.setTargetVersion( "1.0.13" );
        alreadyReleased.setBranchReleaseFrom( "release-1.0.x" );
        site.addComponent( alreadyReleased );

        PlatformReleasePlan plan = PlatformReleaseService.buildPlan( campaign( PlatformReleaseType.STABLE ), PlatformStepCode.PLATFORM_PLUGINS.getStepNumber( ),
                site, true );

        assertEquals( 1, plan.getComponents( ).size( ) );
        assertEquals( "plugin-forms", plan.getComponents( ).get( 0 ).getArtifactId( ) );
        assertEquals( "1.0.13", plan.getAggregate( ).getVersionUpdates( ).getProperties( ).get( "lutece.plugin-workflow.version" ) );
        assertEquals( "4.0.2", plan.getAggregate( ).getVersionUpdates( ).getProperties( ).get( "lutece.plugin-forms.version" ) );
    }

    /**
     * The released versions of the attempts of a step add up, the newest attempt winning on a same component and giving the aggregate version.
     */
    @Test
    public void testMergeResults( )
    {
        PlatformStepResult first = new PlatformStepResult( );
        first.getReleasedVersions( ).put( "fr.paris.lutece.plugins:library-a", "1.0.0" );
        first.getReleasedVersions( ).put( "fr.paris.lutece.plugins:plugin-b", "2.0.0" );
        first.setReport( "failed on plugin-c" );

        PlatformStepResult second = new PlatformStepResult( );
        second.getReleasedVersions( ).put( "fr.paris.lutece.plugins:plugin-b", "2.0.1" );
        second.getReleasedVersions( ).put( "fr.paris.lutece.plugins:plugin-c", "3.0.0" );
        second.setAggregateVersion( "8.0.2" );

        assertEquals( second, PlatformReleaseService.mergeResults( null, second ) );

        PlatformStepResult merged = PlatformReleaseService.mergeResults( first, second );
        assertEquals( 3, merged.getReleasedVersions( ).size( ) );
        assertEquals( "1.0.0", merged.getReleasedVersions( ).get( "fr.paris.lutece.plugins:library-a" ) );
        assertEquals( "2.0.1", merged.getReleasedVersions( ).get( "fr.paris.lutece.plugins:plugin-b" ) );
        assertEquals( "8.0.2", merged.getAggregateVersion( ) );
        assertEquals( "failed on plugin-c", merged.getReport( ) );

        PlatformStepResult dryRun = new PlatformStepResult( );
        dryRun.setReport( "dry run after the failure" );
        PlatformStepResult afterDryRun = PlatformReleaseService.mergeResults( first, dryRun );
        assertEquals( 2, afterDryRun.getReleasedVersions( ).size( ) );
        assertEquals( "1.0.0", afterDryRun.getReleasedVersions( ).get( "fr.paris.lutece.plugins:library-a" ) );
        assertEquals( "dry run after the failure", afterDryRun.getReport( ) );
    }

    /**
     * The versions released by the previous steps and by the failed attempts of the current step are all known when preparing it again.
     *
     * @throws IOException
     *             on JSON error
     */
    @Test
    public void testReleasedVersionsIncludeFailedAttempt( ) throws IOException
    {
        PlatformRelease campaign = campaign( PlatformReleaseType.STABLE );

        PlatformStepResult step1 = new PlatformStepResult( );
        step1.getReleasedVersions( ).put( "fr.paris.lutece.tools:build-config", "3.0.1" );
        step1.setAggregateVersion( "8.0.2" );
        PlatformReleaseService.getStep( campaign, 1 ).setStatus( PlatformReleaseStatus.SUCCESS );
        PlatformReleaseService.getStep( campaign, 1 ).setResultJson( MapperJsonUtil.getJson( step1 ) );

        PlatformStepResult step3Partial = new PlatformStepResult( );
        step3Partial.getReleasedVersions( ).put( "fr.paris.lutece.plugins:library-workflow-core", "4.0.2" );
        PlatformReleaseService.getStep( campaign, 3 ).setStatus( PlatformReleaseStatus.FAILED );
        PlatformReleaseService.getStep( campaign, 3 ).setResultJson( MapperJsonUtil.getJson( step3Partial ) );

        java.util.Map<String, String> mapReleased = PlatformReleaseService.getReleasedVersions( campaign, 3 );
        assertEquals( 2, mapReleased.size( ) );
        assertEquals( "3.0.1", mapReleased.get( "fr.paris.lutece.tools:build-config" ) );
        assertEquals( "4.0.2", mapReleased.get( "fr.paris.lutece.plugins:library-workflow-core" ) );

        assertEquals( 1, PlatformReleaseService.getReleasedVersions( campaign, 2 ).size( ) );
    }

    /**
     * The CSV export lists the components then the aggregate of every step holding a plan : whether each one was released, read from the
     * report of the step, then what happened to it (published, POM updated, simulated, failed).
     *
     * @throws IOException
     *             on JSON error
     */
    @Test
    public void testExportCsv( ) throws IOException
    {
        PlatformRelease campaign = campaign( PlatformReleaseType.STABLE );

        PlatformReleasePlan plan = new PlatformReleasePlan( );
        plan.setStep( 1 );
        plan.setStepCode( PlatformStepCode.GLOBAL_POM );
        plan.getComponents( ).add( resource( "fr.paris.lutece.tools", "build-config", "3.0.1-SNAPSHOT", "3.0.1", "3.0.2-SNAPSHOT" ) );
        plan.setAggregate( resource( "fr.paris.lutece.tools", "lutece-global-pom", "8.0.2-SNAPSHOT", "8.0.2", "8.0.3-SNAPSHOT" ) );

        PlatformReleaseStep step1 = PlatformReleaseService.getStep( campaign, 1 );
        step1.setPlanJson( MapperJsonUtil.getJson( plan ) );
        step1.setStatus( PlatformReleaseStatus.SUCCESS );

        PlatformReleasePlan pluginsPlan = new PlatformReleasePlan( );
        pluginsPlan.setStep( 4 );
        pluginsPlan.setStepCode( PlatformStepCode.PLATFORM_PLUGINS );
        pluginsPlan.getComponents( ).add( resource( "fr.paris.lutece.plugins", "plugin-forms", "4.0.2-SNAPSHOT", "4.0.2", "4.0.3-SNAPSHOT" ) );
        pluginsPlan.getComponents( ).add( resource( "fr.paris.lutece.plugins", "plugin-workflow", "5.1.0-SNAPSHOT", "5.1.0", "5.1.1-SNAPSHOT" ) );
        pluginsPlan.setAggregate( resource( "fr.paris.lutece", "lutece-parent", "8.0.0-SNAPSHOT", "8.0.0", "8.0.1-SNAPSHOT" ) );

        PlatformReleaseStep step4 = PlatformReleaseService.getStep( campaign, 4 );
        step4.setPlanJson( MapperJsonUtil.getJson( pluginsPlan ) );
        step4.setStatus( PlatformReleaseStatus.SUCCESS );

        String [ ] lines = PlatformReleaseService.exportCsv( campaign ).split( "\n" );
        assertEquals( 6, lines.length );
        assertEquals( "étape;groupId;artifactId;versionAvant;versionCible;nextSnapshot;branche;parentAvant;parentCible;releaseStatus;statut", lines [0] );
        assertEquals( "1;fr.paris.lutece.tools;build-config;3.0.1-SNAPSHOT;3.0.1;3.0.2-SNAPSHOT;develop;;;RELEASED;PUBLISHED", lines [1] );
        assertEquals( "1;fr.paris.lutece.tools;lutece-global-pom;8.0.2-SNAPSHOT;8.0.2;8.0.3-SNAPSHOT;develop;;;RELEASED;PUBLISHED", lines [2] );
        assertEquals( "4;fr.paris.lutece.plugins;plugin-forms;4.0.2-SNAPSHOT;4.0.2;4.0.3-SNAPSHOT;develop;;;RELEASED;PUBLISHED", lines [3] );
        assertEquals( "4;fr.paris.lutece;lutece-parent;8.0.0-SNAPSHOT;;;develop;;;NOT_RELEASED;POM_VERSIONS_UPDATED", lines [5] );

        PlatformStepResult partial = new PlatformStepResult( );
        partial.getReleasedVersions( ).put( "fr.paris.lutece.plugins:plugin-forms", "4.0.2" );
        step4.setResultJson( MapperJsonUtil.getJson( partial ) );
        step4.setStatus( PlatformReleaseStatus.FAILED );
        lines = PlatformReleaseService.exportCsv( campaign ).split( "\n" );
        assertEquals( "4;fr.paris.lutece.plugins;plugin-forms;4.0.2-SNAPSHOT;4.0.2;4.0.3-SNAPSHOT;develop;;;RELEASED;PUBLISHED", lines [3] );
        assertEquals( "4;fr.paris.lutece.plugins;plugin-workflow;5.1.0-SNAPSHOT;5.1.0;5.1.1-SNAPSHOT;develop;;;NOT_RELEASED;NOT_PROCESSED", lines [4] );
        assertEquals( "4;fr.paris.lutece;lutece-parent;8.0.0-SNAPSHOT;;;develop;;;NOT_RELEASED;POM_NOT_UPDATED", lines [5] );

        partial.getFailedVersions( ).put( "fr.paris.lutece.plugins:plugin-workflow", "tests failed" );
        partial.setPomUpdated( true );
        step4.setResultJson( MapperJsonUtil.getJson( partial ) );
        lines = PlatformReleaseService.exportCsv( campaign ).split( "\n" );
        assertEquals( "4;fr.paris.lutece.plugins;plugin-workflow;5.1.0-SNAPSHOT;5.1.0;5.1.1-SNAPSHOT;develop;;;NOT_RELEASED;ROLLED_BACK", lines [4] );
        assertEquals( "4;fr.paris.lutece;lutece-parent;8.0.0-SNAPSHOT;;;develop;;;NOT_RELEASED;POM_VERSIONS_UPDATED", lines [5] );

        pluginsPlan.setDryRun( true );
        step4.setPlanJson( MapperJsonUtil.getJson( pluginsPlan ) );
        step4.setResultJson( null );
        step4.setStatus( PlatformReleaseStatus.READY );
        lines = PlatformReleaseService.exportCsv( campaign ).split( "\n" );
        assertEquals( "4;fr.paris.lutece.plugins;plugin-forms;4.0.2-SNAPSHOT;4.0.2;4.0.3-SNAPSHOT;develop;;;NOT_RELEASED;SIMULATED", lines [3] );
        assertEquals( "4;fr.paris.lutece;lutece-parent;8.0.0-SNAPSHOT;;;develop;;;NOT_RELEASED;SIMULATED", lines [5] );
    }

    /**
     * Builds a campaign with its 5 steps, the first one ready, without persistence.
     *
     * @param releaseType
     *            the campaign type
     * @return the campaign
     */
    private static PlatformRelease campaign( PlatformReleaseType releaseType )
    {
        PlatformRelease campaign = new PlatformRelease( );
        campaign.setId( 1 );
        campaign.setReleaseType( releaseType );
        for ( PlatformStepCode code : PlatformStepCode.values( ) )
        {
            PlatformReleaseStep step = new PlatformReleaseStep( );
            step.setStepNumber( code.getStepNumber( ) );
            step.setStatus( code.getStepNumber( ) == 1 ? PlatformReleaseStatus.READY : PlatformReleaseStatus.TODO );
            campaign.getSteps( ).add( step );
        }

        return campaign;
    }

    /**
     * Builds a component.
     *
     * @param strArtifactId
     *            the artifactId
     * @param strCurrentVersion
     *            the current version
     * @return the component
     */
    private static Component component( String strArtifactId, String strCurrentVersion )
    {
        Component component = new Component( );
        component.setGroupId( "fr.paris.lutece.plugins" );
        component.setArtifactId( strArtifactId );
        component.setCurrentVersion( strCurrentVersion );
        component.setLastAvailableSnapshotVersion( strCurrentVersion );

        return component;
    }

    /**
     * Builds a POM dependency of the lutece plugins group.
     *
     * @param strArtifactId
     *            the artifactId
     * @param strVersion
     *            the version
     * @param strVersionProperty
     *            the property holding the version, or null
     * @return the dependency
     */
    private static Dependency dependency( String strArtifactId, String strVersion, String strVersionProperty )
    {
        Dependency dependency = new Dependency( );
        dependency.setGroupId( "fr.paris.lutece.plugins" );
        dependency.setArtifactId( strArtifactId );
        dependency.setVersion( strVersion );
        dependency.setVersionProperty( strVersionProperty );

        return dependency;
    }

    /**
     * Builds a plan resource on the develop branch.
     *
     * @param strGroupId
     *            the groupId
     * @param strArtifactId
     *            the artifactId
     * @param strCurrent
     *            the current version
     * @param strTarget
     *            the target version
     * @param strNext
     *            the next snapshot version
     * @return the resource
     */
    private static PlatformPlanResource resource( String strGroupId, String strArtifactId, String strCurrent, String strTarget, String strNext )
    {
        PlatformPlanResource resource = new PlatformPlanResource( );
        resource.setGroupId( strGroupId );
        resource.setArtifactId( strArtifactId );
        resource.setBranch( "develop" );
        resource.setCurrentVersion( strCurrent );
        resource.setTargetVersion( strTarget );
        resource.setNextSnapshotVersion( strNext );

        return resource;
    }

    /**
     * Only a flagged SNAPSHOT component whose POM version is behind the last SNAPSHOT published in Nexus may be released from that one ; a
     * component ahead of Nexus, a downgrade, a theme or a sentinel value are not candidates.
     */
    @Test
    public void testLatestSnapshotCandidates( )
    {
        Site site = new Site( );
        site.addComponent( snapshotComponent( "behind", "1.0.13-SNAPSHOT", "1.0.14-SNAPSHOT", true ) );
        site.addComponent( snapshotComponent( "ahead", "0.1.12-SNAPSHOT", "0.1.9-SNAPSHOT", true ) );
        site.addComponent( snapshotComponent( "aligned", "2.0.0-SNAPSHOT", "2.0.0-SNAPSHOT", true ) );
        site.addComponent( snapshotComponent( "notFlagged", "1.0.13-SNAPSHOT", "1.0.14-SNAPSHOT", false ) );
        site.addComponent( snapshotComponent( "sentinel", "1.0.13-SNAPSHOT", "Snapshot not found", true ) );
        Component downgrade = snapshotComponent( "downgrade", "1.0.13-SNAPSHOT", "1.0.14-SNAPSHOT", true );
        downgrade.setDowngrade( true );
        site.addComponent( downgrade );

        assertEquals( Arrays.asList( "behind" ), PlatformReleaseService.getLatestSnapshotCandidates( site ) );
    }

    /**
     * The credentials of a host are missing when the user gave none, refused when every component of the host had its branch listing rejected
     * for authentication, fine when at least one component listed its branches.
     */
    @Test
    public void testCredentialProblems( )
    {
        Site site = new Site( );
        Component github = hostedComponent( "plugin-forms", "scm:git:https://github.com/lutece-platform/lutece-form-plugin-forms.git" );
        github.setBranches( Arrays.asList( "develop", "develop_core7" ) );
        Component gitlab = hostedComponent( "plugin-paris", "scm:git:https://dev.lutece.paris.fr/gitlab/bild/plugin-paris.git" );
        gitlab.setRemoteError( "https://dev.lutece.paris.fr/gitlab/bild/plugin-paris.git: not authorized" );
        site.addComponent( github );
        site.addComponent( gitlab );

        assertEquals( Arrays.asList( RepositoryType.GITHUB, RepositoryType.GITLAB ), PlatformReleaseService.getNeededRepositoryTypes( site ) );

        ReleaserUser user = new ReleaserUser( );
        user.addCredential( RepositoryType.GITHUB, user.new Credential( "jdoe", "token" ) );
        Map<RepositoryType, PlatformReleaseService.CredentialProblem> mapProblems = PlatformReleaseService.getCredentialProblems( site, user );
        assertEquals( PlatformReleaseService.CredentialProblem.MISSING, mapProblems.get( RepositoryType.GITLAB ) );
        assertEquals( 1, mapProblems.size( ) );

        user.addCredential( RepositoryType.GITLAB, user.new Credential( "jdoe", "wrong" ) );
        mapProblems = PlatformReleaseService.getCredentialProblems( site, user );
        assertEquals( PlatformReleaseService.CredentialProblem.REFUSED, mapProblems.get( RepositoryType.GITLAB ) );
        assertEquals( 1, mapProblems.size( ) );

        gitlab.setRemoteError( "Connection timed out" );
        assertTrue( PlatformReleaseService.getCredentialProblems( site, user ).isEmpty( ) );
    }

    /**
     * Builds a component hosted on a Git platform.
     *
     * @param strArtifactId
     *            the artifactId
     * @param strScmUrl
     *            the SCM URL
     * @return the component
     */
    private static Component hostedComponent( String strArtifactId, String strScmUrl )
    {
        Component component = new Component( );
        component.setArtifactId( strArtifactId );
        component.setScmDeveloperConnection( strScmUrl );

        return component;
    }

    /**
     * Builds a SNAPSHOT component with its Nexus informations.
     *
     * @param strArtifactId
     *            the artifactId
     * @param strCurrent
     *            the version of the aggregate POM
     * @param strLastSnapshot
     *            the last SNAPSHOT published in Nexus
     * @param bProject
     *            true when flagged as to be released
     * @return the component
     */
    /**
     * The core of an aggregate comes from its core line parent (lutece-platform, lutece-core), otherwise from its own version when the
     * aggregate is itself a core line parent (lutece-global-pom, lutece-site-pom) ; nothing else tells it.
     */
    @Test
    public void testAggregateCoreLine( )
    {
        Site platform = new Site( );
        platform.setArtifactId( "lutece-platform" );
        platform.setVersion( "8.0.1-SNAPSHOT" );
        assertEquals( null, PlatformReleaseService.getAggregateCoreLine( platform ) );
        platform.setPomParentArtifactId( "lutece-global-pom" );
        platform.setPomParentVersion( "7.0.5" );
        assertEquals( Integer.valueOf( 7 ), PlatformReleaseService.getAggregateCoreLine( platform ) );

        Site globalPom = new Site( );
        globalPom.setArtifactId( "lutece-global-pom" );
        globalPom.setVersion( "8.0.3-SNAPSHOT" );
        assertEquals( Integer.valueOf( 8 ), PlatformReleaseService.getAggregateCoreLine( globalPom ) );
        globalPom.setVersion( "7.0.9-SNAPSHOT" );
        assertEquals( Integer.valueOf( 7 ), PlatformReleaseService.getAggregateCoreLine( globalPom ) );
    }

    /**
     * A campaign is running when one of its steps is ; the export may be verified only between the success of step 4 and the run of step 5 ;
     * the stamp of a step changes when it is run.
     */
    @Test
    public void testRunningAndExportVerification( )
    {
        PlatformRelease campaign = campaign( PlatformReleaseType.STABLE );
        assertFalse( PlatformReleaseService.isRunning( campaign ) );
        assertFalse( PlatformReleaseService.canVerifyExport( campaign ) );

        PlatformReleaseStep stepPlugins = PlatformReleaseService.getStep( campaign, 4 );
        String strStamp = PlatformReleaseService.getStepStamp( stepPlugins );
        stepPlugins.setStatus( PlatformReleaseStatus.RUNNING );
        assertTrue( PlatformReleaseService.isRunning( campaign ) );
        assertFalse( strStamp.equals( PlatformReleaseService.getStepStamp( stepPlugins ) ) );

        stepPlugins.setStatus( PlatformReleaseStatus.SUCCESS );
        assertFalse( PlatformReleaseService.isRunning( campaign ) );
        assertTrue( PlatformReleaseService.canVerifyExport( campaign ) );

        PlatformReleaseService.getStep( campaign, 5 ).setStatus( PlatformReleaseStatus.RUNNING );
        assertFalse( PlatformReleaseService.canVerifyExport( campaign ) );
        PlatformReleaseService.getStep( campaign, 5 ).setStatus( PlatformReleaseStatus.FAILED );
        assertTrue( PlatformReleaseService.canVerifyExport( campaign ) );
        PlatformReleaseService.getStep( campaign, 5 ).setStatus( PlatformReleaseStatus.SUCCESS );
        assertFalse( PlatformReleaseService.canVerifyExport( campaign ) );

        assertEquals( "none", PlatformReleaseService.getStepStamp( null ) );
    }

    /**
     * The core version of the platform POM must be the one released by the core step ; without a released core, it must at least belong to
     * the core of the campaign.
     */
    @Test
    public void testPomVersionDecision( )
    {
        PlatformStepResult failedThenReleased = new PlatformStepResult( );
        failedThenReleased.getFailedVersions( ).put( "fr.paris.lutece.plugins:plugin-c", "deploy failed" );
        failedThenReleased.getNotProcessed( ).add( "fr.paris.lutece.plugins:plugin-d" );
        PlatformStepResult retry = new PlatformStepResult( );
        retry.getReleasedVersions( ).put( "fr.paris.lutece.plugins:plugin-c", "3.0.0" );
        retry.setPomUpdated( true );
        PlatformStepResult afterRetry = PlatformReleaseService.mergeResults( failedThenReleased, retry );
        assertTrue( afterRetry.getFailedVersions( ).isEmpty( ) );
        assertEquals( Arrays.asList( "fr.paris.lutece.plugins:plugin-d" ), afterRetry.getNotProcessed( ) );
        assertTrue( afterRetry.isPomUpdated( ) );

        PomVersionDecision.Problem mismatch = PomVersionDecision.Problem.MISMATCH;
        PomVersionDecision.Problem wrongCore = PomVersionDecision.Problem.WRONG_CORE;
        assertEquals( null, PlatformReleaseService.getPomVersionProblem( "8.0.1", "8.0.1", 8 ) );
        assertEquals( mismatch, PlatformReleaseService.getPomVersionProblem( "8.0.1-SNAPSHOT", "8.0.1", 8 ) );
        assertEquals( null, PlatformReleaseService.getPomVersionProblem( "8.0.1-SNAPSHOT", null, 8 ) );
        assertEquals( wrongCore, PlatformReleaseService.getPomVersionProblem( "7.1.10", null, 8 ) );
        assertEquals( null, PlatformReleaseService.getPomVersionProblem( null, "8.0.1", 8 ) );

        assertFalse( PlatformReleaseService.isPomVersionChoiceApplicable( null, "8.0.1", mismatch ) );
        assertTrue( PlatformReleaseService.isPomVersionChoiceApplicable( "8.0.1", "8.0.1", mismatch ) );
        assertFalse( PlatformReleaseService.isPomVersionChoiceApplicable( "8.0.0", "8.0.1", mismatch ) );
        assertFalse( PlatformReleaseService.isPomVersionChoiceApplicable( "keep", "8.0.1", mismatch ) );
        assertTrue( PlatformReleaseService.isPomVersionChoiceApplicable( "keep", null, mismatch ) );
        assertFalse( PlatformReleaseService.isPomVersionChoiceApplicable( "keep", null, wrongCore ) );
        assertTrue( PlatformReleaseService.isPomVersionChoiceApplicable( "8.0.0", null, wrongCore ) );

        List<String> published = Arrays.asList( "7.1.9", "7.1.10", "8.0.0", "8.0.1" );
        PomVersionDecision released = PlatformReleaseService.decidePomVersion( "8.0.1-SNAPSHOT", "8.0.1", published, 8, null );
        assertTrue( released.isBlocked( ) );
        assertTrue( released.isExpectedReleased( ) );
        assertFalse( released.isKeepAllowed( ) );
        assertEquals( "8.0.1", released.getExpected( ) );

        PomVersionDecision skipped = PlatformReleaseService.decidePomVersion( "8.0.0", null, published, 8, null );
        assertTrue( skipped.isBlocked( ) );
        assertFalse( skipped.isExpectedReleased( ) );
        assertTrue( skipped.isKeepAllowed( ) );
        assertEquals( "8.0.1", skipped.getExpected( ) );

        PomVersionDecision kept = PlatformReleaseService.decidePomVersion( "8.0.0", null, published, 8, "keep" );
        assertFalse( kept.isBlocked( ) );
        assertTrue( kept.isKept( ) );
        assertEquals( null, kept.getTarget( ) );

        PomVersionDecision chosen = PlatformReleaseService.decidePomVersion( "8.0.0", null, published, 8, "8.0.1" );
        assertFalse( chosen.isBlocked( ) );
        assertEquals( "8.0.1", chosen.getTarget( ) );

        PomVersionDecision fine = PlatformReleaseService.decidePomVersion( "8.0.1", null, published, 8, null );
        assertEquals( null, fine.getProblem( ) );
        assertFalse( fine.isBlocked( ) );

        PomVersionDecision core7 = PlatformReleaseService.decidePomVersion( "7.1.10", null, published, 7, null );
        assertEquals( null, core7.getProblem( ) );
    }

    /**
     * A Jenkins result or an error message always fits into the result column.
     */
    @Test
    public void testTruncateResult( )
    {
        assertEquals( "SUCCESS", PlatformReleaseService.truncateResult( "SUCCESS" ) );
        assertEquals( null, PlatformReleaseService.truncateResult( null ) );
        StringBuilder sb = new StringBuilder( "ERROR : " );
        for ( int i = 0; i < 300; i++ )
        {
            sb.append( 'x' );
        }
        assertEquals( 255, PlatformReleaseService.truncateResult( sb.toString( ) ).length( ) );
    }

    private static Component snapshotComponent( String strArtifactId, String strCurrent, String strLastSnapshot, boolean bProject )
    {
        Component component = new Component( );
        component.setArtifactId( strArtifactId );
        component.setCurrentVersion( strCurrent );
        component.setLastAvailableSnapshotVersion( strLastSnapshot );
        component.setIsProject( bProject );

        return component;
    }
}
