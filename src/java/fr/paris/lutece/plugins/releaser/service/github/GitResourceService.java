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
package fr.paris.lutece.plugins.releaser.service.github;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.service.ComponentService;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.IVCSResourceService;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.releaser.util.file.FileUtils;
import fr.paris.lutece.plugins.releaser.util.github.GitUtils;
import fr.paris.lutece.portal.service.util.AppException;

// TODO: Auto-generated Javadoc
/**
 * SvnSiteService.
 */
public class GitResourceService implements IVCSResourceService
{

    /**
     * Fetch the pom.xml content from a repository
     *
     * @param site
     *            the site
     * @param strGitLogin
     *            the str git login
     * @param strGitPwd
     *            the str git pwd
     * @return The POM content
     */
    public String fetchPom( Site site, String strGitLogin, String strGitPwd )
    {

        String strPom = null;
        CommandResult commandResult = new CommandResult( );
        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        commandResult.setLog( new StringBuffer( ) );
        context.setCommandResult( commandResult );
        context.setSite( site );
        doCheckoutRepository( context, strGitLogin, strGitPwd );
        strPom = FileUtils.readFile( ReleaserUtils.getLocalPomPath( context ) );

        return strPom;
    }

    /**
     * Gets the last release found in the SVN repository.
     *
     * @param site
     *            the site
     * @param strGitLogin
     *            the str git login
     * @param strGitPwd
     *            the str git pwd
     * @return The version if found otherwise null
     */
    public String getLastRelease( Site site, String strGitLogin, String strGitPwd )
    {

        WorkflowReleaseContext context = new WorkflowReleaseContext( );
        context.setSite( site );
        Git git = GitUtils.getGit( ReleaserUtils.getLocalPath( context ) );
        List<String> listTags = GitUtils.getTagNameList( git );
        String strLastRelease = null;

        if ( !CollectionUtils.isEmpty( listTags ) )
        {
            strLastRelease = listTags.get( 0 );
        }

        if ( strLastRelease != null && strLastRelease.contains( "-" ) )
        {

            String [ ] tabRelease = strLastRelease.split( "-" );
            strLastRelease = tabRelease [tabRelease.length - 1];
        }
        else
        {
            strLastRelease = "";
        }

        return strLastRelease;

    }

    /**
     * Do checkout repository.
     *
     * @param context
     *            the context
     * @param strLogin
     *            the str login
     * @param strPassword
     *            the str password
     * @return the string
     */
    @Override
    public String doCheckoutRepository( WorkflowReleaseContext context, String strLogin, String strPassword )
    {

        Git git = null;
        String strBranchReleaseFrom = null;

        CommandResult commandResult = context.getCommandResult( );
        ReleaserUtils.logStartAction( context, " Clone Repository" );
        String strLocalComponentPath = ReleaserUtils.getLocalPath( context );

        File file = new File( strLocalComponentPath );

        if ( file.exists( ) )
        {

            commandResult.getLog( ).append( "Local repository " + strLocalComponentPath + " exist\nCleaning Local folder...\n" );
            if ( !FileUtils.delete( file, commandResult.getLog( ) ) )
            {
                commandResult.setError( commandResult.getLog( ).toString( ) );

            }
            commandResult.getLog( ).append( "Local repository has been cleaned\n" );
        }

        commandResult.getLog( ).append( "Cloning repository ...\n" );
        try
        {

            // PROGRESS 5%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );
            git = GitUtils.cloneRepo( strLocalComponentPath, context.getReleaserResource( ).getScmUrl( ), commandResult, strLogin, strLogin, strPassword );

            GitUtils.createLocalBranch( git, GitUtils.MASTER_BRANCH, commandResult );

            strBranchReleaseFrom = ReleaserUtils.getBranchReleaseFrom( context );
            if ( !strBranchReleaseFrom.equals( GitUtils.DEFAULT_RELEASE_BRANCH ) )
            {
                GitUtils.createLocalBranch( git, strBranchReleaseFrom, commandResult );
                context.setRefBranchReleaseFrom( GitUtils.getRefBranch( git, strBranchReleaseFrom, commandResult ) );
            }
            else
            {
                GitUtils.createLocalBranch( git, GitUtils.DEFAULT_RELEASE_BRANCH, commandResult );
                context.setRefBranchReleaseFrom( GitUtils.getRefBranch( git, GitUtils.DEFAULT_RELEASE_BRANCH, commandResult ) );

                context.setRefBranchRelease( GitUtils.getRefBranch( git, GitUtils.MASTER_BRANCH, commandResult ) );
            }

            commandResult.getLog( ).append( "the repository has been successfully cloned.\n" );
            commandResult.getLog( ).append( "Checkout branch \"" + strBranchReleaseFrom + "\" ...\n" );
            GitUtils.checkoutRepoBranch( git, strBranchReleaseFrom, commandResult );
            // PROGRESS 10%
            commandResult.setProgressValue( commandResult.getProgressValue( ) + 5 );

            if ( context.getSite( ) == null && context.getComponent( ) != null && ComponentService.getService( )
                    .isErrorSnapshotComponentInformations( context.getComponent( ), ReleaserUtils.getLocalPomPath( context ) ) )
            {
                ReleaserUtils.addTechnicalError( commandResult, "The cloned component does not match the release informations" );

            }

        }
        catch( AppException e )
        {

            if ( e.getCause( ) != null && e.getCause( ) instanceof TransportException )
            {

                ReleaserUtils.addTechnicalError( commandResult, ConstanteUtils.ERROR_TYPE_AUTHENTICATION_ERROR, e );
            }

        }

        finally
        {
            if ( git != null )
            {

                git.close( );

            }
        }

        commandResult.getLog( ).append( "Checkout branch develop successfull\n" );

        ReleaserUtils.logEndAction( context, " Clone Repository" );

        return ConstanteUtils.CONSTANTE_EMPTY_STRING;
    }

    /**
     * Update develop branch.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     * @param strMessage
     *            the str message
     */
    @Override
    public void updateDevelopBranch( WorkflowReleaseContext context, Locale locale, String strMessage )
    {
        updateBranch( context, GitUtils.DEFAULT_RELEASE_BRANCH, locale, strMessage );
    }

    /**
     * Update branch.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     * @param strMessage
     *            the str message
     */
    @Override
    public void updateBranch( WorkflowReleaseContext context, String strBranch, Locale locale, String strMessage )
    {

        String strLogin = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getLogin( );
        String strPassword = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getPassword( );

        FileRepository fLocalRepo = null;
        Git git = null;
        CommandResult commandResult = context.getCommandResult( );
        String strLocalComponentPath = ReleaserUtils.getLocalPath( context );

        try
        {

            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );

            git = new Git( fLocalRepo );
            git.checkout( ).setName( strBranch ).call( );
            git.add( ).addFilepattern( "." ).setUpdate( true ).call( );
            git.commit( ).setCommitter( strLogin, strLogin ).setMessage( strMessage ).call( );
            git.push( ).setCredentialsProvider( new UsernamePasswordCredentialsProvider( strLogin, strPassword ) ).call( );
        }
        catch( InvalidRemoteException e )
        {

            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }
        catch( TransportException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }
        catch( IOException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
        catch( GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }

        finally
        {

            if ( fLocalRepo != null )
            {

                fLocalRepo.close( );

            }
            if ( git != null )
            {

                git.close( );

            }

        }

    }

    /**
     * Update master branch.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    @Override
    public void updateMasterBranch( WorkflowReleaseContext context, Locale locale )
    {

        String strLogin = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getLogin( );
        String strPassword = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getPassword( );

        FileRepository fLocalRepo = null;
        Git git = null;
        CommandResult commandResult = context.getCommandResult( );

        String strLocalComponentPath = ReleaserUtils.getLocalPath( context );

        try
        {

            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );

            git = new Git( fLocalRepo );
            git.checkout( ).setName( GitUtils.DEFAULT_RELEASE_BRANCH ).call( );
            GitUtils.mergeBack( git, strLogin, strPassword, commandResult );

        }
        catch( InvalidRemoteException e )
        {

            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }
        catch( TransportException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }
        catch( IOException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
        catch( GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }

        finally
        {

            if ( fLocalRepo != null )
            {

                fLocalRepo.close( );

            }
            if ( git != null )
            {

                git.close( );

            }

        }

    }

    /**
     * Rollback release.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    @Override
    public void rollbackRelease( WorkflowReleaseContext context, Locale locale )
    {

        ReleaserUtils.logStartAction( context, " Rollback Release prepare" );
        FileRepository fLocalRepo = null;
        Git git = null;
        CommandResult commandResult = context.getCommandResult( );
        String strLocalComponentPath = ReleaserUtils.getLocalPath( context );
        String strLogin = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getLogin( );
        String strPassword = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getPassword( );

        try
        {

            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );

            git = new Git( fLocalRepo );

            // RESET commit on release branch from
            if ( !StringUtils.isEmpty( context.getRefBranchReleaseFrom( ) ) )
            {
                git.checkout( ).setName( ReleaserUtils.getBranchReleaseFrom( context ) ).call( );
                git.reset( ).setRef( context.getRefBranchReleaseFrom( ) ).setMode( ResetType.HARD ).call( );
                git.push( ).setForce( true ).setCredentialsProvider( new UsernamePasswordCredentialsProvider( strLogin, strPassword ) ).call( );

            }

            // Reset Commit on Master (only if the releasing from develop branch)
            if ( !StringUtils.isEmpty( context.getRefBranchRelease( ) ) && context.getRefBranchReleaseFrom( ).equals( GitUtils.DEFAULT_RELEASE_BRANCH ) )
            {

                git.checkout( ).setName( GitUtils.MASTER_BRANCH ).call( );
                git.reset( ).setRef( context.getRefBranchRelease( ) ).setMode( ResetType.HARD ).call( );
                git.push( ).setForce( true ).setCredentialsProvider( new UsernamePasswordCredentialsProvider( strLogin, strPassword ) ).call( );
            }

            // Delete Tag if exist
            List<Ref> call = git.tagList( ).call( );
            String strTagName = context.getReleaserResource( ).getArtifactId( ) + "-" + context.getReleaserResource( ).getTargetVersion( );
            for ( Ref refTag : call )
            {

                if ( refTag.getName( ).contains( strTagName ) )
                {

                    LogCommand log = git.log( ).setMaxCount( 1 );

                    Ref peeledRef = git.getRepository( ).peel( refTag );
                    if ( peeledRef.getPeeledObjectId( ) != null )
                    {
                        log.add( peeledRef.getPeeledObjectId( ) );
                    }
                    else
                    {
                        log.add( refTag.getObjectId( ) );
                    }

                    Iterable<RevCommit> logs = log.call( );
                    for ( RevCommit rev : logs )
                    {
                        // Test if the tag was created by the release
                        if ( !rev.getName( ).equals( context.getRefBranchRelease( ) ) )
                        {

                            git.branchDelete( ).setBranchNames( refTag.getName( ) ).setForce( true ).call( );
                            RefSpec refSpec = new RefSpec( ).setSource( null ).setDestination( refTag.getName( ) );
                            git.push( ).setRefSpecs( refSpec ).setCredentialsProvider( new UsernamePasswordCredentialsProvider( strLogin, strPassword ) )
                                    .setRemote( "origin" ).call( );
                        }

                    }

                }

            }
        }
        catch( InvalidRemoteException e )
        {

            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }
        catch( TransportException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }
        catch( IOException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
        catch( GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }

        finally
        {

            if ( fLocalRepo != null )
            {

                fLocalRepo.close( );

            }
            if ( git != null )
            {

                git.close( );

            }

        }
        ReleaserUtils.logEndAction( context, " Rollback Release prepare" );

    }

    /**
     * Checkout develop branch.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    @Override
    public void checkoutDevelopBranch( WorkflowReleaseContext context, Locale locale )
    {
        checkoutBranch( context, GitUtils.DEFAULT_RELEASE_BRANCH, locale );
    }

    /**
     * Checkout branch.
     *
     * @param context
     *            the context
     * @param locale
     *            the locale
     */
    @Override
    public void checkoutBranch( WorkflowReleaseContext context, String strBranch, Locale locale )
    {
        FileRepository fLocalRepo = null;
        Git git = null;
        CommandResult commandResult = context.getCommandResult( );

        String strLocalComponentPath = ReleaserUtils.getLocalPath( context );
        String strLogin = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getLogin( );
        String strPassword = context.getReleaserUser( ).getCredential( context.getReleaserResource( ).getRepoType( ) ).getPassword( );

        try
        {

            fLocalRepo = new FileRepository( strLocalComponentPath + "/.git" );

            git = new Git( fLocalRepo );
            git.checkout( ).setName( strBranch ).call( );
            PullResult result = GitUtils.pullRepoBranch( git, strBranch, strLogin, strPassword );
            if ( !result.isSuccessful( ) )
            {
                ReleaserUtils.addTechnicalError( commandResult, "error during checkout " + strBranch + " branch" );

            }

        }
        catch( InvalidRemoteException e )
        {

            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }
        catch( TransportException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }
        catch( IOException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
        catch( GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }

        finally
        {

            if ( fLocalRepo != null )
            {

                fLocalRepo.close( );

            }
            if ( git != null )
            {

                git.close( );

            }

        }

    }

}
