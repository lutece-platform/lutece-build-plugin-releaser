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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformPlanResource;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformRelease;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleasePlan;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformReleaseType;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformResourceVerification;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepCode;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformStepMode;
import fr.paris.lutece.plugins.releaser.business.platform.PlatformVerificationCheck;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;

/**
 * Census of the step 0 : which SNAPSHOT are taken, the status of the instant checks and the plan sent to the step pipeline.
 */
public class PlatformPrecheckServiceTest
{
    private static final String GITHUB = "scm:git:https://github.com/lutece-platform/";
    private static final String GITLAB = "scm:git:https://dev.lutece.paris.fr/gitlab/bild/";

    /**
     * Only the SNAPSHOT that are not themes are taken, blocked or not.
     */
    @Test
    public void testCandidates( )
    {
        assertTrue( PlatformPrecheckService.isCandidate( component( "plugin-a", "1.0.1-SNAPSHOT", GITHUB ) ) );
        assertFalse( PlatformPrecheckService.isCandidate( component( "plugin-b", "1.0.0", GITHUB ) ) );

        Component theme = component( "theme-a", "1.0.1-SNAPSHOT", GITHUB );
        theme.setType( ConstanteUtils.CONSTANTE_TYPE_LUTECE_SITE );
        assertFalse( PlatformPrecheckService.isCandidate( theme ) );

        Component blocked = component( "plugin-c", "1.0.1-SNAPSHOT", GITHUB );
        blocked.setBlockingReleaseComment( "SNAPSHOT inconnue de Nexus" );
        assertTrue( PlatformPrecheckService.isCandidate( blocked ) );
    }

    /**
     * The blocking comment and the credential problem are to fix, the other comments are warnings, otherwise the pipeline still has to verify.
     */
    @Test
    public void testInstantChecks( )
    {
        Site site = site( "lutece-core", "7.1.10-SNAPSHOT" );
        Component blocked = component( "plugin-blocked", "1.0.1-SNAPSHOT", GITHUB );
        blocked.setBlockingReleaseComment( "Branche du mauvais core" );
        blocked.addReleaseComment( "Roadmap Redmine" );
        Component informed = component( "plugin-informed", "2.0.1-SNAPSHOT", GITHUB );
        informed.addReleaseComment( "Releases dans le dépôt Nexus public" );
        Component gitlab = component( "plugin-gitlab", "3.0.1-SNAPSHOT", GITLAB );
        Component clean = component( "plugin-clean", "4.0.1-SNAPSHOT", GITHUB );
        site.addComponent( blocked );
        site.addComponent( informed );
        site.addComponent( gitlab );
        site.addComponent( clean );

        Map<String, PlatformResourceVerification> mapItems = new LinkedHashMap<>( );
        Map<String, PlatformPlanResource> mapResources = new LinkedHashMap<>( );
        PlatformPrecheckService.addStep( mapItems, mapResources, PlatformStepCode.CORE, site,
                Collections.singletonMap( RepositoryType.GITLAB, "Identifiants GITLAB manquants" ) );

        assertEquals( Arrays.asList( "fr.paris.lutece:lutece-core", "fr.paris.lutece.plugins:plugin-blocked", "fr.paris.lutece.plugins:plugin-informed",
                "fr.paris.lutece.plugins:plugin-gitlab", "fr.paris.lutece.plugins:plugin-clean" ), new ArrayList<>( mapItems.keySet( ) ) );

        PlatformResourceVerification itemBlocked = mapItems.get( "fr.paris.lutece.plugins:plugin-blocked" );
        assertEquals( PlatformVerificationCheck.STATUS_TO_FIX, itemBlocked.getStatus( ) );
        assertEquals( 3, itemBlocked.getStep( ) );
        assertEquals( "1.0.1-SNAPSHOT", itemBlocked.getVersion( ) );
        assertEquals( PlatformPrecheckService.CHECK_RELEASABLE, itemBlocked.getChecks( ).get( 0 ).getCheck( ) );
        assertEquals( "Roadmap Redmine", itemBlocked.getChecks( ).get( 1 ).getDetails( ) );

        assertEquals( PlatformVerificationCheck.STATUS_WARNING, mapItems.get( "fr.paris.lutece.plugins:plugin-informed" ).getStatus( ) );
        PlatformResourceVerification itemGitlab = mapItems.get( "fr.paris.lutece.plugins:plugin-gitlab" );
        assertEquals( PlatformVerificationCheck.STATUS_TO_FIX, itemGitlab.getStatus( ) );
        assertEquals( PlatformPrecheckService.CHECK_CREDENTIALS, itemGitlab.getChecks( ).get( 0 ).getCheck( ) );
        assertEquals( PlatformReleaseService.VERIFICATION_NOT_VERIFIED, mapItems.get( "fr.paris.lutece.plugins:plugin-clean" ).getStatus( ) );

        PlatformPlanResource core = mapResources.get( "fr.paris.lutece:lutece-core" );
        assertEquals( ConstanteUtils.DEPENDENCY_TYPE_LUTECE_CORE, core.getType( ) );
        assertEquals( "https://github.com/lutece-platform/lutece-core.git", core.getScmUrl( ) );
        assertEquals( "develop_core7", core.getBranch( ) );
    }

    /**
     * A resource met by two steps keeps its first step, and the aggregate of the plugins step is not taken : it is released by the last step.
     */
    @Test
    public void testFirstStepKeptAndAggregateOfPluginsStepLeftOut( )
    {
        Map<String, PlatformResourceVerification> mapItems = new LinkedHashMap<>( );
        Map<String, PlatformPlanResource> mapResources = new LinkedHashMap<>( );
        Site plugins = site( "lutece-platform", "7.2.0-SNAPSHOT" );
        plugins.addComponent( component( "plugin-a", "1.0.1-SNAPSHOT", GITHUB ) );
        PlatformPrecheckService.addStep( mapItems, mapResources, PlatformStepCode.PLATFORM_PLUGINS, plugins, Collections.emptyMap( ) );
        Site starters = site( "lutece-platform", "7.2.0-SNAPSHOT" );
        starters.addComponent( component( "plugin-a", "1.0.1-SNAPSHOT", GITHUB ) );
        PlatformPrecheckService.addStep( mapItems, mapResources, PlatformStepCode.PLATFORM_STARTERS, starters, Collections.emptyMap( ) );

        assertEquals( Arrays.asList( "fr.paris.lutece.plugins:plugin-a", "fr.paris.lutece:lutece-platform" ), new ArrayList<>( mapItems.keySet( ) ) );
        assertEquals( 4, mapItems.get( "fr.paris.lutece.plugins:plugin-a" ).getStep( ) );
        assertEquals( 5, mapItems.get( "fr.paris.lutece:lutece-platform" ).getStep( ) );
    }

    /**
     * The plan verifies every resource not already to fix, in PRECHECK mode, and lists the whole census.
     */
    @Test
    public void testPlan( )
    {
        Site site = site( "lutece-core", "7.1.10-SNAPSHOT" );
        Component blocked = component( "plugin-blocked", "1.0.1-SNAPSHOT", GITHUB );
        blocked.setBlockingReleaseComment( "Branche du mauvais core" );
        site.addComponent( blocked );
        site.addComponent( component( "plugin-clean", "4.0.1-SNAPSHOT", GITHUB ) );
        Map<String, PlatformResourceVerification> mapItems = new LinkedHashMap<>( );
        Map<String, PlatformPlanResource> mapResources = new LinkedHashMap<>( );
        PlatformPrecheckService.addStep( mapItems, mapResources, PlatformStepCode.CORE, site, Collections.emptyMap( ) );

        PlatformRelease campaign = new PlatformRelease( );
        campaign.setId( 12 );
        campaign.setName( "test0" );
        campaign.setReleaseType( PlatformReleaseType.BETA );
        campaign.setCoreMajor( 7 );
        PlatformReleasePlan plan = PlatformPrecheckService.buildPlan( campaign, mapItems, mapResources );

        assertEquals( PlatformStepMode.PRECHECK, plan.getMode( ) );
        assertEquals( PlatformPrecheckService.STEP_NUMBER, plan.getStep( ) );
        assertEquals( 7, plan.getCoreMajor( ) );
        List<String> listPlanned = new ArrayList<>( );
        for ( PlatformPlanResource resource : plan.getComponents( ) )
        {
            listPlanned.add( resource.getCoordinates( ) );
        }
        assertEquals( Arrays.asList( "fr.paris.lutece:lutece-core", "fr.paris.lutece.plugins:plugin-clean" ), listPlanned );
        assertEquals( Arrays.asList( "fr.paris.lutece:lutece-core", "fr.paris.lutece.plugins:plugin-blocked", "fr.paris.lutece.plugins:plugin-clean" ),
                plan.getCampaignCoordinates( ) );
        assertEquals( null, plan.getAggregate( ) );
    }

    /**
     * Builds the aggregate of a step on its core 7 branch.
     *
     * @param strArtifactId
     *            the artifactId
     * @param strVersion
     *            the version of its POM
     * @return the site
     */
    private static Site site( String strArtifactId, String strVersion )
    {
        Site site = new Site( );
        site.setGroupId( "fr.paris.lutece" );
        site.setArtifactId( strArtifactId );
        site.setVersion( strVersion );
        site.setScmUrl( "https://github.com/lutece-platform/" + strArtifactId + ".git" );
        site.setBranchReleaseFrom( "develop_core7" );

        return site;
    }

    /**
     * Builds a component of the lutece plugins group.
     *
     * @param strArtifactId
     *            the artifactId
     * @param strCurrentVersion
     *            the version of the POM
     * @param strScmBase
     *            the base URL of its repository
     * @return the component
     */
    private static Component component( String strArtifactId, String strCurrentVersion, String strScmBase )
    {
        Component component = new Component( );
        component.setGroupId( "fr.paris.lutece.plugins" );
        component.setArtifactId( strArtifactId );
        component.setCurrentVersion( strCurrentVersion );
        component.setLastAvailableSnapshotVersion( strCurrentVersion );
        component.setScmDeveloperConnection( strScmBase + strArtifactId + ".git" );

        return component;
    }
}
