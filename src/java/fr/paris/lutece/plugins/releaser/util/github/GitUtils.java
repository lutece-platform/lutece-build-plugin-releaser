package fr.paris.lutece.plugins.releaser.util.github;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
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
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
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
    public static final String DEVELOP_BRANCH = "develop";
    
    /** The Constant CONSTANTE_REF_TAG. */
    private static final String CONSTANTE_REF_TAG = "refs/tags/";

    /**
     * Clone repo.
     *
     * @param sClonePath the s clone path
     * @param sRepoURL the s repo URL
     * @param commandResult the command result
     * @param strGitHubUserLogin the str git hub user login
     * @param strUserName the str user name
     * @param strPassword the str password
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
     * @param git the git
     * @param sBranchName the s branch name
     * @param commandResult the command result
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
     * @param git the git
     * @param sBranchName the s branch name
     * @param commandResult the command result
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
     * @param git the git
     * @param sBranchName the s branch name
     * @param commandResult the command result
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
     * @param git the git
     * @param strRefSpec the str ref spec
     * @param strUserName the str user name
     * @param strPassword the str password
     * @throws InvalidRemoteException the invalid remote exception
     * @throws TransportException the transport exception
     * @throws GitAPIException the git API exception
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
     * @param git the git
     * @param sBranchName the s branch name
     * @param strUserName the str user name
     * @param strPassword the str password
     * @return the pull result
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws WrongRepositoryStateException the wrong repository state exception
     * @throws InvalidConfigurationException the invalid configuration exception
     * @throws DetachedHeadException the detached head exception
     * @throws InvalidRemoteException the invalid remote exception
     * @throws CanceledException the canceled exception
     * @throws RefNotFoundException the ref not found exception
     * @throws NoHeadException the no head exception
     * @throws TransportException the transport exception
     * @throws GitAPIException the git API exception
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
     * @param git the git
     * @param strBranchToMerge the str branch to merge
     * @return the merge result
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws WrongRepositoryStateException the wrong repository state exception
     * @throws InvalidConfigurationException the invalid configuration exception
     * @throws DetachedHeadException the detached head exception
     * @throws InvalidRemoteException the invalid remote exception
     * @throws CanceledException the canceled exception
     * @throws RefNotFoundException the ref not found exception
     * @throws NoHeadException the no head exception
     * @throws TransportException the transport exception
     * @throws GitAPIException the git API exception
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
     * @param git the git
     * @param nMaxCommit the n max commit
     * @return the last log
     * @throws NoHeadException the no head exception
     * @throws GitAPIException the git API exception
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
     * @param git the git
     * @return the last commit id
     * @throws NoHeadException the no head exception
     * @throws GitAPIException the git API exception
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
     * Merge back.
     *
     * @param git the git
     * @param strUserName the str user name
     * @param strPassword the str password
     * @param commandResult the command result
     * @return the merge result
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws GitAPIException the git API exception
     */
    public static MergeResult mergeBack( Git git, String strUserName, String strPassword, CommandResult commandResult ) throws IOException, GitAPIException
    {

        Ref tag = getTagLinkedToLastRelease( git );

        git.checkout( ).setName( MASTER_BRANCH ).call( );
        List<Ref> call = git.branchList( ).call( );

        Ref mergedBranchRef = null;
        for ( Ref ref : call )
        {
            if ( ref.getName( ).equals( "refs/heads/" + DEVELOP_BRANCH ) )
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

            ReleaserUtils.addTechnicalError( commandResult,
                    mergeResult.getMergeStatus( ).toString( ) + "\nPlease merge manually master into" + DEVELOP_BRANCH + "branch." );
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
     * @param strSearch the str search
     * @param strOrganization the str organization
     * @param strUserName the str user name
     * @param strPassword the str password
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

        	String strApiToken=AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITHUB_SEARCH_REPO_API_TOKEN );
        	Map<String,String> mapHeaderToken=new HashMap<String, String>();
        	mapHeaderToken.put("Authorization", "token "+strApiToken );
            strResponse = httpAccess.doGet(strUrl, null, null,mapHeaderToken );
            

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
     * @param strFullName the str full name
     * @param strPathFile the str path file
     * @param strBranch the str branch
     * @param strUserName the str user name
     * @param strPassword the str password
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
     * @param git the git
     * @return the tag linked to last release
     * @throws GitAPIException the git API exception
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
     * @param git the git
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
     * @param strRepoUrl the str repo url
     * @return the repo url
     */
    private static String getRepoUrl( String strRepoUrl )
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
     * @param strClonePath the str clone path
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

}
