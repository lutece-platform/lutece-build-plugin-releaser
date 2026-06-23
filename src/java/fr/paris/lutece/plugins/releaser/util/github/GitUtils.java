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
package fr.paris.lutece.plugins.releaser.util.github;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.MapperJsonUtil;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.signrequest.BasicAuthorizationAuthenticator;

// TODO: Auto-generated Javadoc
/**
 * The Class GitUtils.
 */
public class GitUtils
{

    /** The Constant MASTER_BRANCH. */
    public static final String MASTER_BRANCH = "master";

    /** The Constant DEVELOP_BRANCH. */
    public static final String DEFAULT_RELEASE_BRANCH = "develop";

    /** The Constant DEVELOP_BRANCH — source branch of the develop/master flow, paired with MASTER_BRANCH. */
    public static final String DEVELOP_BRANCH = "develop";

    /** The Constant CONSTANTE_REF_TAG. */
    private static final String CONSTANTE_REF_TAG = "refs/tags/";

    /** The Constant CONSTANTE_REF_HEADS. */
    private static final String CONSTANTE_REF_HEADS = "refs/heads/";

    /**
     * Derive the target master* branch from a release-from branch.
     * Only branches explicitly listed in the {@link ConstanteUtils#PROPERTY_MERGE_BACK_BRANCHES} configuration are
     * merged back into a master* counterpart, built by swapping the leading {@link #DEVELOP_BRANCH} prefix with
     * {@link #MASTER_BRANCH} (e.g. {@code develop_core7} → {@code master_core7}). Any other branch (including
     * release/maintenance branches that merely start with "develop", e.g. {@code develop-plugin-xxx-1.0.6-branch})
     * has no master* counterpart and is released without a merge-back.
     *
     * @param strBranchReleaseFrom
     *            the source branch
     * @return the target master* branch, or {@code null} if no master* merge applies
     */
    public static String getTargetMasterBranch( String strBranchReleaseFrom )
    {
        if ( strBranchReleaseFrom == null || !strBranchReleaseFrom.startsWith( DEVELOP_BRANCH ) )
        {
            return null;
        }

        if ( !isMergeBackBranch( strBranchReleaseFrom ) )
        {
            return null;
        }

        return MASTER_BRANCH + strBranchReleaseFrom.substring( DEVELOP_BRANCH.length( ) );
    }

    /**
     * The configured list of release-from branches that follow a master* counterpart
     * (property {@link ConstanteUtils#PROPERTY_MERGE_BACK_BRANCHES}, e.g. {@code develop,develop_core7,develop7.x}).
     *
     * @return the list of branches (never null, possibly empty)
     */
    public static List<String> getMergeBackBranches( )
    {
        String strMergeBackBranches = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_MERGE_BACK_BRANCHES, StringUtils.EMPTY );
        return Arrays.asList( strMergeBackBranches.split( "\\s*,\\s*" ) );
    }

    /**
     * Whether the given branch is one of the configured "follow master" branches
     * ({@link ConstanteUtils#PROPERTY_MERGE_BACK_BRANCHES}).
     *
     * @param strBranch
     *            the branch
     * @return true if the branch is in the configured list
     */
    public static boolean isMergeBackBranch( String strBranch )
    {
        return strBranch != null && getMergeBackBranches( ).contains( strBranch );
    }

    /**
     * Clone repo.
     *
     * @param sClonePath
     *            the s clone path
     * @param sRepoURL
     *            the s repo URL
     * @param commandResult
     *            the command result
     * @param strGitHubUserLogin
     *            the str git hub user login
     * @param strUserName
     *            the str user name
     * @param strPassword
     *            the str password
     * @return the git
     */
    public static Git cloneRepo( String sClonePath, String sRepoURL, CommandResult commandResult, String strGitHubUserLogin, String strUserName,
            String strPassword )
    {
        Git git = null;
        Repository repository = null;
        try
        {
            FileRepositoryBuilder builder = new FileRepositoryBuilder( );
            File fGitDir = new File( sClonePath );

            CloneCommand clone = Git.cloneRepository( ).setCredentialsProvider( new UsernamePasswordCredentialsProvider( strUserName, strPassword ) )
                    .setBare( false ).setCloneAllBranches( true ).setDirectory( fGitDir ).setURI( getRepoUrl( sRepoURL ) );

            git = clone.call( );

            repository = builder.setGitDir( fGitDir ).readEnvironment( ).findGitDir( ).build( );
            repository.getConfig( ).setString( "user", null, "name", strGitHubUserLogin );
            repository.getConfig( ).setString( "user", null, "email", strGitHubUserLogin + "@users.noreply.github.com" );
            repository.getConfig( ).save( );

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
            if ( repository != null )
            {
                repository.close( );
            }
        }
        return git;

    }

    /**
     * Checkout repo branch.
     *
     * @param git
     *            the git
     * @param sBranchName
     *            the s branch name
     * @param commandResult
     *            the command result
     */
    public static void checkoutRepoBranch( Git git, String sBranchName, CommandResult commandResult )
    {
        try
        {
            git.checkout( ).setName( sBranchName ).call( );

        }
        catch( InvalidRemoteException e )
        {

            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }
        catch( TransportException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }

        catch( GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }

    }

    /**
     * Creates the local branch.
     *
     * @param git
     *            the git
     * @param sBranchName
     *            the s branch name
     * @param commandResult
     *            the command result
     */
    public static void createLocalBranch( Git git, String sBranchName, CommandResult commandResult )
    {
        try
        {
            git.branchCreate( ).setName( sBranchName ).setUpstreamMode( SetupUpstreamMode.SET_UPSTREAM ).setStartPoint( "origin/" + sBranchName )
                    .setForce( true ).call( );
        }
        catch( InvalidRemoteException e )
        {

            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }
        catch( TransportException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );

        }

        catch( GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }

    }

    /**
     * Gets the ref branch.
     *
     * @param git
     *            the git
     * @param sBranchName
     *            the s branch name
     * @param commandResult
     *            the command result
     * @return the ref branch
     */
    public static String getRefBranch( Git git, String sBranchName, CommandResult commandResult )
    {

        String refLastCommit = null;
        try
        {
            git.checkout( ).setName( sBranchName ).call( );
            refLastCommit = getLastCommitId( git );
        }

        catch( RefAlreadyExistsException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
        catch( RefNotFoundException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
        catch( InvalidRefNameException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
        catch( CheckoutConflictException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
        catch( GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
        return refLastCommit;
    }

    /**
     * Push force.
     *
     * @param git
     *            the git
     * @param strRefSpec
     *            the str ref spec
     * @param strUserName
     *            the str user name
     * @param strPassword
     *            the str password
     * @throws InvalidRemoteException
     *             the invalid remote exception
     * @throws TransportException
     *             the transport exception
     * @throws GitAPIException
     *             the git API exception
     */
    public static void pushForce( Git git, String strRefSpec, String strUserName, String strPassword )
            throws InvalidRemoteException, TransportException, GitAPIException
    {

        git.push( ).setRemote( "origin" ).setRefSpecs( new RefSpec( strRefSpec ) ).setForce( true )
                .setCredentialsProvider( new UsernamePasswordCredentialsProvider( strUserName, strPassword ) ).call( );

    }

    /**
     * Pull repo branch.
     *
     * @param git
     *            the git
     * @param sBranchName
     *            the s branch name
     * @param strUserName
     *            the str user name
     * @param strPassword
     *            the str password
     * @return the pull result
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws WrongRepositoryStateException
     *             the wrong repository state exception
     * @throws InvalidConfigurationException
     *             the invalid configuration exception
     * @throws DetachedHeadException
     *             the detached head exception
     * @throws InvalidRemoteException
     *             the invalid remote exception
     * @throws CanceledException
     *             the canceled exception
     * @throws RefNotFoundException
     *             the ref not found exception
     * @throws NoHeadException
     *             the no head exception
     * @throws TransportException
     *             the transport exception
     * @throws GitAPIException
     *             the git API exception
     */
    public static PullResult pullRepoBranch( Git git, String sBranchName, String strUserName, String strPassword )
            throws IOException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException,
            RefNotFoundException, NoHeadException, TransportException, GitAPIException
    {
        PullResult pPullResult = git.pull( ).setCredentialsProvider( new UsernamePasswordCredentialsProvider( strUserName, strPassword ) ).setRemote( "origin" )
                .setRemoteBranchName( sBranchName ).call( );

        return pPullResult;
    }

    /**
     * Merge repo branch.
     *
     * @param git
     *            the git
     * @param strBranchToMerge
     *            the str branch to merge
     * @return the merge result
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws WrongRepositoryStateException
     *             the wrong repository state exception
     * @throws InvalidConfigurationException
     *             the invalid configuration exception
     * @throws DetachedHeadException
     *             the detached head exception
     * @throws InvalidRemoteException
     *             the invalid remote exception
     * @throws CanceledException
     *             the canceled exception
     * @throws RefNotFoundException
     *             the ref not found exception
     * @throws NoHeadException
     *             the no head exception
     * @throws TransportException
     *             the transport exception
     * @throws GitAPIException
     *             the git API exception
     */
    public static MergeResult mergeRepoBranch( Git git, String strBranchToMerge )
            throws IOException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException,
            RefNotFoundException, NoHeadException, TransportException, GitAPIException
    {
        List<Ref> call = git.branchList( ).call( );
        Ref mergedBranchRef = null;
        for ( Ref ref : call )
        {
            if ( ref.getName( ).equals( "refs/heads/" + strBranchToMerge ) )
            {
                mergedBranchRef = ref;
                break;
            }
        }
        MergeResult mergeResult = git.merge( ).include( mergedBranchRef ).call( );
        return mergeResult;
    }

    /**
     * Gets the last log.
     *
     * @param git
     *            the git
     * @param nMaxCommit
     *            the n max commit
     * @return the last log
     * @throws NoHeadException
     *             the no head exception
     * @throws GitAPIException
     *             the git API exception
     */
    public static String getLastLog( Git git, int nMaxCommit ) throws NoHeadException, GitAPIException
    {
        Iterable<RevCommit> logList = git.log( ).setMaxCount( 1 ).call( );
        Iterator i = logList.iterator( );
        String sCommitMessages = "";
        while ( i.hasNext( ) )
        {
            RevCommit revCommit = (RevCommit) i.next( );
            sCommitMessages += revCommit.getFullMessage( );
            sCommitMessages += "\n";
            sCommitMessages += revCommit.getCommitterIdent( );
        }
        return sCommitMessages;
    }

    /**
     * Gets the last commit id.
     *
     * @param git
     *            the git
     * @return the last commit id
     * @throws NoHeadException
     *             the no head exception
     * @throws GitAPIException
     *             the git API exception
     */
    public static String getLastCommitId( Git git ) throws NoHeadException, GitAPIException
    {
        Iterable<RevCommit> logList = git.log( ).setMaxCount( 1 ).call( );
        Iterator i = logList.iterator( );
        String strCommitId = null;
        while ( i.hasNext( ) )
        {
            RevCommit revCommit = (RevCommit) i.next( );
            strCommitId = revCommit.getName( );

        }
        return strCommitId;
    }

    /**
     * Merge back the source branch (or the last release tag if any) into the target master branch and push.
     *
     * @param git
     *            the git
     * @param strSourceBranch
     *            the source branch to merge from (e.g. "develop", "develop_core7")
     * @param strTargetMasterBranch
     *            the target master branch to merge into (e.g. "master", "master_core7")
     * @param strUserName
     *            the str user name
     * @param strPassword
     *            the str password
     * @param commandResult
     *            the command result
     * @return the merge result
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws GitAPIException
     *             the git API exception
     */
    public static MergeResult mergeBack( Git git, String strSourceBranch, String strTargetMasterBranch, String strUserName, String strPassword,
            CommandResult commandResult ) throws IOException, GitAPIException
    {

        // Fail the release with a clear alert if the target master* branch does not exist on the remote.
        if ( git.getRepository( ).findRef( "refs/remotes/origin/" + strTargetMasterBranch ) == null )
        {
            ReleaserUtils.addTechnicalError( commandResult,
                    "Remote branch origin/" + strTargetMasterBranch + " not found. Cannot merge " + strSourceBranch + " back into "
                            + strTargetMasterBranch + ". Please create the " + strTargetMasterBranch + " branch on the remote and retry." );
            return null;
        }

        // JGit's checkout requires a local branch ref; cloneAllBranches only creates
        // refs/remotes/origin/*. Create the local tracking branch on first use.
        boolean localExists = false;
        for ( Ref ref : git.branchList( ).call( ) )
        {
            if ( ref.getName( ).equals( "refs/heads/" + strTargetMasterBranch ) )
            {
                localExists = true;
                break;
            }
        }
        if ( !localExists )
        {
            git.branchCreate( ).setName( strTargetMasterBranch ).setUpstreamMode( SetupUpstreamMode.SET_UPSTREAM )
                    .setStartPoint( "origin/" + strTargetMasterBranch ).setForce( true ).call( );
        }

        Ref tag = getTagLinkedToLastRelease( git );

        git.checkout( ).setName( strTargetMasterBranch ).call( );
        List<Ref> call = git.branchList( ).call( );

        Ref mergedBranchRef = null;
        for ( Ref ref : call )
        {
            if ( ref.getName( ).equals( "refs/heads/" + strSourceBranch ) )
            {
                mergedBranchRef = ref;
                break;
            }
        }

        if ( tag != null )
        {
            mergedBranchRef = tag;
        }
        MergeResult mergeResult = git.merge( ).include( mergedBranchRef ).call( );
        if ( mergeResult.getMergeStatus( ).equals( MergeResult.MergeStatus.CHECKOUT_CONFLICT )
                || mergeResult.getMergeStatus( ).equals( MergeResult.MergeStatus.CONFLICTING )
                || mergeResult.getMergeStatus( ).equals( MergeResult.MergeStatus.FAILED )
                || mergeResult.getMergeStatus( ).equals( MergeResult.MergeStatus.NOT_SUPPORTED ) )
        {

            ReleaserUtils.addTechnicalError( commandResult, mergeResult.getMergeStatus( ).toString( ) + "\nPlease merge manually " + strSourceBranch
                    + " into " + strTargetMasterBranch + " branch." );
        }
        else
        {
            git.push( ).setCredentialsProvider( new UsernamePasswordCredentialsProvider( strUserName, strPassword ) ).call( );
            commandResult.getLog( ).append( mergeResult.getMergeStatus( ) );
        }
        return mergeResult;

    }

    /**
     * Search repo.
     *
     * @param strSearch
     *            the str search
     * @param strOrganization
     *            the str organization
     * @param strUserName
     *            the str user name
     * @param strPassword
     *            the str password
     * @return the github search result
     */
    public static GithubSearchResult searchRepo( String strSearch, String strOrganization, String strUserName, String strPassword )
    {
        HttpAccess httpAccess = new HttpAccess( );

        GithubSearchResult searchResult = null;

        String strUrl = null;
        try
        {
            strUrl = MessageFormat.format( AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITHUB_SEARCH_REPO_API ),
                    URLEncoder.encode( strSearch, "UTF-8" ), strOrganization );
        }
        catch( UnsupportedEncodingException e1 )
        {
            AppLogService.error( e1 );
        }

        String strResponse = "";

        try
        {

            String strApiToken = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITHUB_SEARCH_REPO_API_TOKEN );
            Map<String, String> mapHeaderToken = new HashMap<String, String>( );
            mapHeaderToken.put( "Authorization", "token " + strApiToken );
            strResponse = httpAccess.doGet( strUrl, null, null, mapHeaderToken );

            if ( !StringUtils.isEmpty( strResponse ) )
            {
                searchResult = MapperJsonUtil.parse( strResponse, GithubSearchResult.class );

            }

        }
        catch( HttpAccessException ex )
        {
            AppLogService.error( ex );
        }
        catch( IOException e )
        {
            AppLogService.error( e );
        }

        return searchResult;
    }

    /**
     * Gets the file content.
     *
     * @param strFullName
     *            the str full name
     * @param strPathFile
     *            the str path file
     * @param strBranch
     *            the str branch
     * @param strUserName
     *            the str user name
     * @param strPassword
     *            the str password
     * @return the file content
     */
    public static String getFileContent( String strFullName, String strPathFile, String strBranch, String strUserName, String strPassword )
    {
        HttpAccess httpAccess = new HttpAccess( );
        String strUrl = "https://raw.githubusercontent.com/" + strFullName + "/" + strBranch + "/" + strPathFile;
        // Map<String,String> hashHeader=new HashMap<>( );
        // hashHeader.put( "accept", "application/vnd.github.VERSION.raw" );
        String strResponse = "";

        try
        {

            strResponse = httpAccess.doGet( strUrl, new BasicAuthorizationAuthenticator( strUserName, strPassword ), null );

        }
        catch( HttpAccessException ex )
        {
            AppLogService.error( ex );
        }

        return strResponse;
    }

    /**
     * Gets the tag linked to last release.
     *
     * @param git
     *            the git
     * @return the tag linked to last release
     * @throws GitAPIException
     *             the git API exception
     */
    private static Ref getTagLinkedToLastRelease( Git git ) throws GitAPIException
    {
        final String TOKEN = "[maven-release-plugin] prepare release ";
        Ref res = null;
        String sTagName = null;

        Iterable<RevCommit> logList = git.log( ).setMaxCount( 10 ).call( );
        Iterator i = logList.iterator( );
        String sCommitMessages = "";
        while ( i.hasNext( ) )
        {
            RevCommit revCommit = (RevCommit) i.next( );

            sCommitMessages = revCommit.getFullMessage( );
            int index = sCommitMessages.indexOf( TOKEN );
            if ( index >= 0 )
            {
                sTagName = sCommitMessages.replace( TOKEN, "" );
                break;
            }
        }

        if ( ( sTagName != null ) && ( !( sTagName.trim( ).equals( "" ) ) ) )
        {
            List<Ref> tags = git.tagList( ).call( );
            for ( int j = 0; j < tags.size( ); j++ )
            {
                Ref tag = tags.get( tags.size( ) - 1 - j );
                String tagName = tag.getName( );
                if ( ( "refs/tags/" + sTagName ).startsWith( tag.getName( ) ) )
                {
                    res = tag;
                    break;
                }
            }
        }

        return res;
    }

    /**
     * Gets the tag name list.
     *
     * @param git
     *            the git
     * @return the tag name list
     */
    public static List<String> getTagNameList( Git git )
    {
        List<String> listTagName = null;
        if ( git != null )
        {
            listTagName = new ArrayList<>( );
            Collection<Ref> colTags = git.getRepository( ).getTags( ).values( );
            for ( Ref ref : colTags )
            {
                listTagName.add( ref.getName( ).replace( CONSTANTE_REF_TAG, "" ) );
            }
        }

        return listTagName;
    }

    /**
     * Gets the repo url.
     *
     * @param strRepoUrl
     *            the str repo url
     * @return the repo url
     */
    public static String getRepoUrl( String strRepoUrl )
    {

        if ( strRepoUrl != null && strRepoUrl.startsWith( "scm:git:" ) )
        {
            strRepoUrl = strRepoUrl.substring( 8 );
        }

        return strRepoUrl;

    }

    /**
     * Gets the git.
     *
     * @param strClonePath
     *            the str clone path
     * @return the git
     */
    public static Git getGit( String strClonePath )
    {
        Git git = null;
        Repository repository = null;

        File fGitDir = new File( strClonePath + "/.git" );

        if ( !fGitDir.exists( ) )
        {
            return null;
        }

        try
        {
            FileRepositoryBuilder builder = new FileRepositoryBuilder( );
            repository = builder.setGitDir( fGitDir ).readEnvironment( ).findGitDir( ).build( );

            git = new Git( repository );
        }
        catch( IOException e )
        {
            AppLogService.error( e.getMessage( ), e );
        }

        return git;
    }

    /**
     * List the remote branch names without cloning (git ls-remote --heads). Lightweight : no working tree,
     * no packfile, no disk usage.
     *
     * @param repoUrl
     *            the remote repository URL
     * @param login
     *            the credential login
     * @param pwd
     *            the credential password
     * @return the list of remote branch names (e.g. "develop", "develop_core7"), empty on error
     */
    public static List<String> lsRemoteBranches( String repoUrl, String login, String pwd )
    {
        List<String> branchNameList = new ArrayList<String>( );
        if ( StringUtils.isBlank( repoUrl ) )
        {
            return branchNameList;
        }
        try
        {
            Collection<Ref> refs = Git.lsRemoteRepository( ).setHeads( true ).setRemote( repoUrl )
                    .setCredentialsProvider( new UsernamePasswordCredentialsProvider( login, pwd ) ).call( );
            for ( Ref ref : refs )
            {
                String strName = ref.getName( );
                if ( strName != null && strName.startsWith( CONSTANTE_REF_HEADS ) )
                {
                    branchNameList.add( strName.substring( CONSTANTE_REF_HEADS.length( ) ) );
                }
            }
        }
        catch( GitAPIException e )
        {
            AppLogService.error( "GitUtils - lsRemoteBranches error on " + repoUrl + " : " + e.getMessage( ), e );
        }
        return branchNameList;
    }

    public static List<String> getBranchList( String repoUrl, File localRepo, CommandResult commandResult, String login, String pwd )
    {
        Git git = null;
        List<String> branchNameList =  new ArrayList<String>( );

        try
        {
            CredentialsProvider credential = new UsernamePasswordCredentialsProvider( login, pwd );
            
            git = Git.cloneRepository( ).setCredentialsProvider( credential ).setURI( repoUrl ).setDirectory( localRepo ).setCloneAllBranches( true ).call( );

            List<Ref> branchList = git.branchList( ).setListMode( ListMode.ALL ).call( );
            if ( !branchList.isEmpty( ) )
            {
                for ( Ref ref : branchList )
                {
                    String [ ] refSplit = ref.getName( ).split( "/" );

                    if ( refSplit [1].equals( "remotes" ) && refSplit [2].equals( "origin" ) )
                    {
                        branchNameList.add( refSplit [3] );
                    }
                }
            }
        }
        catch( InvalidRemoteException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
            branchNameList.add( "InvalidRemoteException" );
        }
        catch( TransportException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
            branchNameList.add( "TransportException" );
        }
        catch( GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
            branchNameList.add( "GitAPIException)" );
        }
        finally
        {
            if ( git != null )
            {
                git.close( );
            }
        }

        return branchNameList;
    }

    /**
     * Checkout a tag in detached HEAD state.
     * Used by the "release from tag" workflow : the tag's content becomes the working tree
     * without moving any branch label.
     *
     * @param git
     *            the git
     * @param strTagName
     *            the tag name (without "refs/tags/" prefix)
     * @param commandResult
     *            the command result
     */
    public static void checkoutTag( Git git, String strTagName, CommandResult commandResult )
    {
        try
        {
            git.checkout( ).setName( CONSTANTE_REF_TAG + strTagName ).call( );
        }
        catch( GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
    }

    /**
     * Push only a tag ref to origin, without pushing any branch.
     * Used by the "release from tag" workflow to publish the newly created stable tag
     * while keeping the local detached commit out of the remote.
     *
     * @param git
     *            the git
     * @param strTagName
     *            the tag name (without "refs/tags/" prefix)
     * @param strUserName
     *            the str user name
     * @param strPassword
     *            the str password
     * @param commandResult
     *            the command result
     */
    public static void pushTagOnly( Git git, String strTagName, String strUserName, String strPassword, CommandResult commandResult )
    {
        try
        {
            String strRef = CONSTANTE_REF_TAG + strTagName;
            git.push( ).setRemote( "origin" ).setRefSpecs( new RefSpec( strRef + ":" + strRef ) )
                    .setCredentialsProvider( new UsernamePasswordCredentialsProvider( strUserName, strPassword ) ).call( );
        }
        catch( GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
    }

    /**
     * Ensure a local tracking branch exists for {@code strBranchName}.
     * JGit's checkout requires a local ref; a fresh clone only has {@code refs/remotes/origin/*}.
     * If the local branch already exists, does nothing. If only the remote ref exists, creates a
     * local tracking branch from {@code origin/<branchName>}. Fails via
     * {@link ReleaserUtils#addTechnicalError} if neither exists.
     *
     * @param git
     *            the git
     * @param strBranchName
     *            the branch name (without {@code refs/heads/} or {@code origin/} prefix)
     * @param commandResult
     *            the command result
     */
    public static void ensureLocalBranch( Git git, String strBranchName, CommandResult commandResult )
    {
        try
        {
            if ( git.getRepository( ).findRef( "refs/heads/" + strBranchName ) != null )
            {
                return;
            }
            if ( git.getRepository( ).findRef( "refs/remotes/origin/" + strBranchName ) == null )
            {
                ReleaserUtils.addTechnicalError( commandResult,
                        "Remote branch origin/" + strBranchName + " not found. Cannot create local tracking branch." );
                return;
            }
            git.branchCreate( ).setName( strBranchName ).setUpstreamMode( SetupUpstreamMode.SET_UPSTREAM )
                    .setStartPoint( "origin/" + strBranchName ).setForce( true ).call( );
        }
        catch( IOException | GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
        }
    }

    /**
     * Merge a branch into the current branch using the "ours" strategy.
     * Records the merge in history without changing the working tree :
     * the next merge from the same source will treat its commits as already integrated,
     * which prevents future conflicts on lines both branches modified independently.
     *
     * @param git
     *            the git
     * @param strBranchToMerge
     *            the branch to mark as integrated (e.g. "develop", "develop_core7")
     * @param strMessage
     *            the merge commit message
     * @param commandResult
     *            the command result
     * @return the merge result, or {@code null} if an error occurred
     */
    public static MergeResult mergeOursStrategy( Git git, String strBranchToMerge, String strMessage, CommandResult commandResult )
    {
        try
        {
            Ref refToMerge = git.getRepository( ).findRef( strBranchToMerge );
            if ( refToMerge == null )
            {
                ReleaserUtils.addTechnicalError( commandResult, "Branch not found for ours-merge: " + strBranchToMerge );
                return null;
            }
            return git.merge( ).setStrategy( MergeStrategy.OURS ).include( refToMerge ).setCommit( true ).setMessage( strMessage ).call( );
        }
        catch( IOException | GitAPIException e )
        {
            ReleaserUtils.addTechnicalError( commandResult, e.getMessage( ), e );
            return null;
        }
    }
}
