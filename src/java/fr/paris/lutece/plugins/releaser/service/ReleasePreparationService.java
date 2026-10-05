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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import fr.paris.lutece.plugins.releaser.business.AbstractReleaserResource;
import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.business.ReleaserUser;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.version.Version;
import fr.paris.lutece.plugins.releaser.util.version.VersionParsingException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

/**
 * Prepares an aggregate (site, theme, platform POM loaded in a transient site) and its components for release : component list from the POM
 * dependencies, remote informations, target and next snapshot versions, release branches. Knows nothing about persistence or screens.
 */
public final class ReleasePreparationService
{
    private static final String MESSAGE_AVOID_SNAPSHOT = "releaser.message.avoidSnapshot";
    private static final String MESSAGE_UPGRADE_SELECTED = "releaser.message.upgradeSelected";
    private static final String MESSAGE_TO_BE_RELEASED = "releaser.message.toBeReleased";
    private static final String MESSAGE_MORE_RECENT_VERSION_AVAILABLE = "releaser.message.moreRecentVersionAvailable";
    private static final String MESSAGE_AN_RELEASE_VERSION_ALREADY_EXIST = "releaser.message.releleaseVersionAlreadyExist";
    private static final String MESSAGE_SNAPSHOT_VERSION_OUTDATED = "releaser.message.snapshotVersionOutdated";
    private static final String MESSAGE_NO_VERSION_IN_SITE_POM = "releaser.message.noVersionInSitePom";
    private static final String MESSAGE_BRANCHES_NOT_LISTED = "releaser.message.branchesNotListed";
    private static final String MESSAGE_THEME_NOT_RELEASABLE = "releaser.message.themeNotReleasable";

    /** Message of a default branch built for another core version than the aggregate, shared by the site and platform flows */
    public static final String MESSAGE_BRANCH_CORE_MISMATCH = "releaser.message.branchCoreMismatch";

    /** The parents that tell the core version a component is built for */
    public static final List<String> CORE_LINE_PARENTS = Arrays.asList( "lutece-global-pom", "lutece-site-pom" );

    /** First major of the core line parents numbered like the core : lutece-global-pom 4.x to 7.x and lutece-site-pom 3.x to 7.x are core 7 */
    public static final int CORE_8_FIRST_PARENT_MAJOR = 8;

    private static final int CORE_8 = 8;
    private static final int CORE_7 = 7;

    /**
     * The reasons a component cannot be released by the aggregate it belongs to, whatever the user does.
     */
    public enum NonReleasableReason
    {
        /** Themes are never released from here */
        THEME,
        /** The SNAPSHOT of the aggregate POM was never published in Nexus */
        SNAPSHOT_UNKNOWN_IN_NEXUS,
        /** The default branch is built for another core version */
        BRANCH_CORE_MISMATCH
    }

    /**
     * Utility class.
     */
    private ReleasePreparationService( )
    {
    }

    /**
     * Define which version between last released or current snapshot should be the origin for next release versions. Ex of cases :<br>
     * last release : 3.2.1 current : 4.0.0-SNAPSHOT -- current <br>
     * last release : 3.2.1 current : 3.2.2-SNAPSHOT -- last or current <br>
     * last release : missing current : 1.0.0-SNAPSHOT -- current <br>
     * last release : 3.2.1-RC-02 current : 3.2.1-SNAPSHOT -- last <br>
     *
     * @param strLastRelease
     *            The last release
     * @param strCurrentVersion
     *            The current release
     * @return The origin version
     */
    public static String getOriginVersion( String strLastRelease, String strCurrentVersion )
    {
        String strOriginVersion = strCurrentVersion;
        if ( ( strLastRelease != null ) && Version.isCandidate( strLastRelease ) )
        {
            strOriginVersion = strLastRelease;
        }

        return strOriginVersion;
    }

    /**
     * Define the aggregate versions from its origin version : next release, next snapshot, target versions list and cycling index.
     *
     * @param site
     *            The aggregate
     * @param strLastReleaseVersion
     *            The last released version, may be null
     */
    public static void defineAggregateVersions( Site site, String strLastReleaseVersion )
    {
        site.setLastReleaseVersion( strLastReleaseVersion );

        String strOriginVersion = getOriginVersion( strLastReleaseVersion, site.getVersion( ) );

        site.setNextReleaseVersion( Version.getReleaseVersion( strOriginVersion ) );
        site.setNextSnapshotVersion( Version.getNextSnapshotVersion( strOriginVersion ) );
        site.setTargetVersions( Version.getNextReleaseVersions( strOriginVersion, strLastReleaseVersion ) );
        site.setTargetVersionIndex( Math.max( 0, site.getTargetVersions( ).indexOf( site.getNextReleaseVersion( ) ) ) );
    }

    /**
     * Initialize the component list of an aggregate from its current dependencies : remote informations, target versions, next snapshot versions
     * and release branches. The project selector decides which components are to be released.
     *
     * @param site
     *            The aggregate
     * @param user
     *            The releaser user (credentials)
     * @param projectSelector
     *            Returns true for components to be released
     */
    public static void initComponents( Site site, ReleaserUser user, Predicate<Component> projectSelector )
    {
        for ( Dependency dependency : site.getCurrentDependencies( ) )
        {
            Component component = new Component( );

            component.setArtifactId( dependency.getArtifactId( ) );
            component.setGroupId( dependency.getGroupId( ) );
            component.setType( dependency.getType( ) );
            if ( dependency.getVersion( ) == null )
            {
                dependency.setVersion( ConstanteUtils.NO_VERSION_DEFINED_IN_POM );
            }
            String currentVersion = dependency.getVersion( ).replace( "[", "" ).replace( "]", "" );
            component.setCurrentVersion( currentVersion );
            component.setIsProject( projectSelector.test( component ) );
            site.addComponent( component );
        }

        ExecutorService executor = Executors.newFixedThreadPool( ConstanteUtils.NB_POOL_REMOTE_INFORMATION );

        List<Future> futures = new ArrayList<Future>( site.getCurrentDependencies( ).size( ) );

        for ( Component component : site.getComponents( ) )
        {
            futures.add( executor.submit( new GetRemoteInformationsTask( component, user ) ) );
        }

        for ( Future future : futures )
        {
            try
            {
                future.get( );
            }
            catch( InterruptedException | ExecutionException e )
            {
                AppLogService.error( e );
            }
        }

        executor.shutdown( );

        for ( Component component : site.getComponents( ) )
        {
            ComponentService.getService( ).updateComponentForReleaseBranchFrom( component, null );

            defineTargetVersion( component );
            defineNextSnapshotVersion( component );
            component.setName( ReleaserUtils.getComponentName( component.getScmDeveloperConnection( ), component.getArtifactId( ) ) );

            String strComponentBranch = getComponentBranch( component, site );
            component.setBranchReleaseFrom( strComponentBranch );
        }
    }

    /**
     * Rebuilds the release comments of every component of an aggregate.
     *
     * @param site
     *            The aggregate
     * @param locale
     *            The locale of the comments
     */
    public static void buildComponentsComments( Site site, Locale locale )
    {
        Integer nCoreMajor = getCoreLine( site );
        String strDefaultBranch = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_BRANCH_DEFAULT );
        for ( Component component : site.getComponents( ) )
        {
            component.resetComments( );
            buildComponentComments( component, locale );
            if ( nCoreMajor != null && getNonReleasableReason( component, nCoreMajor, strDefaultBranch ) == NonReleasableReason.BRANCH_CORE_MISMATCH )
            {
                component.addReleaseComment( I18nService.getLocalizedString( MESSAGE_BRANCH_CORE_MISMATCH, getCoreMismatchArguments( component, nCoreMajor ), locale ) );
            }
        }
    }

    /**
     * Decides why a component cannot be released by its aggregate, in the order the checks must apply : a component already blocked or not in
     * SNAPSHOT is left as is ; a theme ; a current SNAPSHOT ahead of the last one in Nexus, before any check relying on the Nexus POM ; a
     * default branch whose parent belongs to another core version than the aggregate. The site flow reports the reason, the platform flow
     * blocks on it.
     *
     * @param component
     *            the component, Nexus information loaded
     * @param nCoreMajor
     *            the core version of the aggregate
     * @param strDefaultBranch
     *            the default branch of the components
     * @return the reason, null when the component is releasable
     */
    public static NonReleasableReason getNonReleasableReason( Component component, int nCoreMajor, String strDefaultBranch )
    {
        if ( component.getBlockingReleaseComment( ) != null || !component.isSnapshotVersion( ) )
        {
            return null;
        }
        if ( component.isTheme( ) )
        {
            return NonReleasableReason.THEME;
        }
        String strLastSnapshot = component.getLastAvailableSnapshotVersion( );
        if ( isParsableSnapshot( strLastSnapshot ) && ReleaserUtils.compareVersion( component.getCurrentVersion( ), strLastSnapshot ) > 0 )
        {
            return NonReleasableReason.SNAPSHOT_UNKNOWN_IN_NEXUS;
        }
        Integer nParentCoreLine = getParentCoreLine( component );
        if ( nParentCoreLine != null && nParentCoreLine != nCoreMajor && Objects.equals( component.getBranchReleaseFrom( ), strDefaultBranch ) )
        {
            return NonReleasableReason.BRANCH_CORE_MISMATCH;
        }

        return null;
    }

    /**
     * The arguments of the core mismatch message : branch, core of the branch, parent artifactId and version, expected core.
     *
     * @param component
     *            the component
     * @param nCoreMajor
     *            the core version of the aggregate
     * @return the arguments
     */
    public static String [ ] getCoreMismatchArguments( Component component, int nCoreMajor )
    {
        return new String [ ] {
                component.getBranchReleaseFrom( ), String.valueOf( getParentCoreLine( component ) ), component.getPomParentArtifactId( ),
                component.getPomParentVersion( ), Integer.toString( nCoreMajor )
        };
    }

    /**
     * Returns the core version a component or an aggregate is built for, told by its parent POM when that parent belongs to the core line
     * (lutece-global-pom, lutece-site-pom).
     *
     * @param resource
     *            the component or aggregate
     * @return 7 or 8, null when the parent is unknown, of another kind or unreadable
     */
    public static Integer getParentCoreLine( AbstractReleaserResource resource )
    {
        if ( resource.getPomParentArtifactId( ) == null || !CORE_LINE_PARENTS.contains( resource.getPomParentArtifactId( ) )
                || resource.getPomParentVersion( ) == null )
        {
            return null;
        }
        try
        {
            return toCoreLine( Version.parse( resource.getPomParentVersion( ) ).getMajor( ) );
        }
        catch( VersionParsingException e )
        {
            return null;
        }
    }

    /**
     * Converts the major of a core line parent into the core version it is built for : the parents are numbered like the core from 8, the
     * earlier numbering (lutece-global-pom 4.x to 7.x, lutece-site-pom 3.x to 7.x) belongs to core 7.
     *
     * @param nParentMajor
     *            the parent major
     * @return 7 or 8
     */
    public static int toCoreLine( int nParentMajor )
    {
        return nParentMajor >= CORE_8_FIRST_PARENT_MAJOR ? CORE_8 : CORE_7;
    }

    /**
     * Returns the core version of an aggregate : its parent major converted by {@link #toCoreLine(int)}.
     *
     * @param site
     *            the aggregate
     * @return 7 or 8, null when the parent version is missing or unreadable
     */
    public static Integer getCoreLine( Site site )
    {
        Integer nCoreMajor = getCoreMajor( site );

        return nCoreMajor != null ? toCoreLine( nCoreMajor ) : null;
    }

    /**
     * Returns the core version of an aggregate, read from the major of its parent POM version : the lutece-site-pom of a site, the core of
     * a platform campaign.
     *
     * @param site
     *            the aggregate
     * @return the core major, null when the parent version is missing or unreadable
     */
    public static Integer getCoreMajor( Site site )
    {
        try
        {
            String strParentVersion = site.getParentVersion( ) != null ? site.getParentVersion( ).replace( "[", "" ).replace( "]", "" ) : null;
            return Version.parse( strParentVersion ).getMajor( );
        }
        catch( VersionParsingException | NullPointerException e )
        {
            return null;
        }
    }

    /**
     * Whether a value parses to a SNAPSHOT version, false for null, blank and the "not found" sentinels without logging.
     *
     * @param strVersion
     *            the value
     * @return true for a SNAPSHOT version
     */
    public static boolean isParsableSnapshot( String strVersion )
    {
        if ( strVersion == null || strVersion.trim( ).isEmpty( ) )
        {
            return false;
        }
        try
        {
            return Version.parse( strVersion ).isSnapshot( );
        }
        catch( VersionParsingException e )
        {
            return false;
        }
    }

    /**
     * Builds the release comments of a component : missing version, bugtracker informations, outdated snapshot, newer version available, to be
     * released. A blocking anomaly owns the display, no other comment is added after it.
     *
     * @param component
     *            The component
     * @param locale
     *            The locale of the comments
     */
    public static void buildComponentComments( Component component, Locale locale )
    {
        if ( component.getBlockingReleaseComment( ) != null )
        {
            return;
        }

        if ( ConstanteUtils.NO_VERSION_DEFINED_IN_POM.equals( component.getCurrentVersion( ) ) )
        {
            component.addReleaseComment( I18nService.getLocalizedString( MESSAGE_NO_VERSION_IN_SITE_POM, locale ) );
            return;
        }

        BugtrackerService.getService( ).populateBugtrackerInfo( component );

        if ( component.getRepoType( ) != null && ( component.getBranches( ) == null || component.getBranches( ).isEmpty( ) ) )
        {
            String [ ] arguments = {
                    component.getRepoType( ).name( ), component.getBranchReleaseFrom( )
            };
            component.addReleaseComment( I18nService.getLocalizedString( MESSAGE_BRANCHES_NOT_LISTED, arguments, locale ) );
        }

        if ( component.isSnapshotVersion( ) && !component.getCurrentVersion( ).equals( component.getLastAvailableSnapshotVersion( ) ) )
        {
            String [ ] arguments = {
                    component.getCurrentVersion( ), component.getLastAvailableSnapshotVersion( )
            };
            component.addReleaseComment( I18nService.getLocalizedString( MESSAGE_SNAPSHOT_VERSION_OUTDATED, arguments, Locale.getDefault( ) ) );
        }

        if ( !component.isProject( ) )
        {
            if ( Version.isSnapshot( component.getTargetVersion( ) ) )
            {
                component.addReleaseComment( I18nService.getLocalizedString( MESSAGE_AVOID_SNAPSHOT, locale ) );
            }
            else
                if ( component.getLastAvailableVersion( ) != null && component.getTargetVersion( ) != null
                        && ReleaserUtils.compareVersion( component.getTargetVersion( ), component.getLastAvailableVersion( ) ) < 0 )
                {
                    String [ ] arguments = {
                            component.getLastAvailableVersion( )
                    };
                    component.addReleaseComment( I18nService.getLocalizedString( MESSAGE_MORE_RECENT_VERSION_AVAILABLE, arguments, locale ) );
                }
        }
        else
        {
            if ( component.isSnapshotVersion( ) )
            {
                String [ ] arguments = {
                        component.getCurrentVersion( ), component.getLastAvailableSnapshotVersion( ), component.getLastAvailableVersion( )
                };
                if ( ReleaserUtils.compareVersion( component.getCurrentVersion( ), component.getLastAvailableSnapshotVersion( ) ) < 0 )
                {
                    component.addReleaseComment( I18nService.getLocalizedString( MESSAGE_UPGRADE_SELECTED, arguments, locale ) );
                }
                else
                    if ( !component.shouldBeReleased( ) && !component.isDowngrade( ) )
                    {
                        component.addReleaseComment( I18nService.getLocalizedString(
                                component.isTheme( ) ? MESSAGE_THEME_NOT_RELEASABLE : MESSAGE_AN_RELEASE_VERSION_ALREADY_EXIST, arguments, locale ) );
                    }
                    else
                        if ( component.shouldBeReleased( ) )
                        {
                            component.addReleaseComment( I18nService.getLocalizedString( MESSAGE_TO_BE_RELEASED, locale ) );
                        }
            }
            else
                if ( ReleaserUtils.compareVersion( component.getCurrentVersion( ), component.getLastAvailableVersion( ) ) < 0 )
                {
                    String [ ] arguments = {
                            component.getLastAvailableVersion( )
                    };
                    component.addReleaseComment( I18nService.getLocalizedString( MESSAGE_MORE_RECENT_VERSION_AVAILABLE, arguments, locale ) );
                }
        }
    }

    /**
     * Refreshes a component newly flagged as project : remote informations, release branch, target and next snapshot versions, name.
     *
     * @param component
     *            The component
     */
    public static void refreshProjectComponent( Component component )
    {
        try
        {
            ComponentService.getService( ).setRemoteInformations( component, false );
        }
        catch( HttpAccessException | IOException e )
        {
            AppLogService.error( e );
        }
        ComponentService.getService( ).updateComponentForReleaseBranchFrom( component, null );
        defineTargetVersion( component );
        defineNextSnapshotVersion( component );
        component.setName( ReleaserUtils.getComponentName( component.getScmDeveloperConnection( ), component.getArtifactId( ) ) );
    }

    /**
     * Returns the branch on which a component must be released, derived from the core line of the aggregate parent POM.
     *
     * @param component
     *            the component
     * @param site
     *            the aggregate being released (provides the parent POM version)
     * @return the component release branch
     */
    private static String getComponentBranch( Component component, Site site )
    {
        String strDefaultBranch = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_BRANCH_DEFAULT );
        int nParentMajorForDefault = AppPropertiesService.getPropertyInt( ConstanteUtils.PROPERTY_BRANCH_PARENT_MAJOR_FOR_DEFAULT, CORE_8_FIRST_PARENT_MAJOR );

        Integer nParentMajor = getCoreMajor( site );
        if ( nParentMajor == null )
        {
            return strDefaultBranch;
        }

        if ( nParentMajor >= nParentMajorForDefault )
        {
            return strDefaultBranch;
        }

        if ( ConstanteUtils.TAG_LUTECE_CORE.equals( component.getArtifactId( ) ) )
        {
            return AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_BRANCH_DEVELOPMENT_FOR_CORE7 );
        }

        String strLegacyComponentBranch = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_BRANCH_DEVELOPMENT_FOR_LUTECE7 );
        if ( component.getBranches( ) != null && component.getBranches( ).contains( strLegacyComponentBranch ) )
        {
            return strLegacyComponentBranch;
        }

        String strLegacyCoreBranch = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_BRANCH_DEVELOPMENT_FOR_CORE7 );
        if ( component.getBranches( ) != null && component.getBranches( ).contains( strLegacyCoreBranch ) )
        {
            return strLegacyCoreBranch;
        }

        return strDefaultBranch;
    }

    /**
     * Define the target version for a given component : <br>
     * - current version for non project component <br>
     * - next release for project component.
     *
     * @param component
     *            The component
     */
    public static void defineTargetVersion( Component component )
    {
        if ( component.isProject( ) && component.isSnapshotVersion( ) )
        {
            if ( component.getLastAvailableVersion( ) != null && !component.getCurrentVersion( ).equals( component.getLastAvailableSnapshotVersion( ) )
                    || component.isTheme( ) )
            {
                component.setTargetVersion( component.getLastAvailableVersion( ) );
            }
            else
            {
                component.setTargetVersions( Version.getNextReleaseVersions( component.getCurrentVersion( ), component.getLastAvailableVersion( ) ) );
                String strTargetVersion = Version.getReleaseVersion( component.getCurrentVersion( ) );
                component.setTargetVersion( strTargetVersion );
                component.setTargetVersionIndex( Math.max( 0, component.getTargetVersions( ).indexOf( strTargetVersion ) ) );
            }
        }
        else
        {
            component.setTargetVersion( component.getCurrentVersion( ) );
        }
    }

    /**
     * Define the next snapshot version for a given component from its target version.
     *
     * @param component
     *            The component
     */
    public static void defineNextSnapshotVersion( Component component )
    {
        String strNextSnapshotVersion = Version.NOT_AVAILABLE;
        if ( !ConstanteUtils.NO_VERSION_DEFINED_IN_POM.equals( component.getTargetVersion( ) ) )
        {
            try
            {
                Version version = Version.parse( component.getTargetVersion( ) );
                boolean bSnapshot = true;
                strNextSnapshotVersion = version.nextPatch( bSnapshot ).toString( );
            }
            catch( VersionParsingException ex )
            {
                AppLogService.error( "Error parsing version for component " + component.getArtifactId( ) + " : " + ex.getMessage( ), ex );
            }
        }

        component.setNextSnapshotVersion( strNextSnapshotVersion );
    }
}
