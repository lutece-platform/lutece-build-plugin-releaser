package fr.paris.lutece.plugins.releaser.util.github;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
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
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;

import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.MapperJsonUtil;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.signrequest.BasicAuthorizationAuthenticator;





public class GitUtils  {
	
    public static final String MASTER_BRANCH = "master";
    public static final String DEVELOP_BRANCH = "develop";
    

	public static  Git cloneRepo(String sClonePath, String sRepoURL, CommandResult commandResult,String strGitHubUserLogin) 
	{
	    Git git=null;
	    Repository repository=null;
	    try
        {
	    FileRepositoryBuilder builder = new FileRepositoryBuilder();
		File fGitDir = new File(sClonePath);
		
		CloneCommand clone = Git.cloneRepository().setBare(false).setCloneAllBranches(true).setDirectory(fGitDir).setURI(getRepoUrl( sRepoURL ));
		
		git=clone.call( );
		
		repository = builder.setGitDir(fGitDir).readEnvironment().findGitDir().build();
        repository.getConfig( ).setString( "user", null, "name", strGitHubUserLogin );
        repository.getConfig( ).setString( "user", null, "email", strGitHubUserLogin + "@users.noreply.github.com" );
        repository.getConfig( ).save( );
      
		
        }
        catch( InvalidRemoteException  e )
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
           
            repository.close();
            
         }
		return git;
		
	}
	
	
	public static void checkoutRepoBranch(Git git, String sBranchName,CommandResult commandResult)
	{
	    try
        {
		git.checkout().setName(sBranchName).call();
		
        }
        catch( InvalidRemoteException  e )
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
	
	public static void createLocalBranch(Git git, String sBranchName,CommandResult commandResult) 
	{
	    try
        {
	    git.branchCreate() 
	       .setName(sBranchName)
	       .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
	       .setStartPoint("origin/" + sBranchName)
	       .setForce(true)
	       .call();
        }
        catch( InvalidRemoteException  e )
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
	
	public static String getRefBranch(Git git, String sBranchName,CommandResult commandResult) 
    {
	    
	    
	    String refLastCommit=null;
        try
        {
             git.checkout().setName(sBranchName).call();
             refLastCommit= getLastCommitId( git );
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

	public static void  pushForce(Git git, String strRefSpec, String strUserName, String strPassword) throws InvalidRemoteException, TransportException, GitAPIException
	{
	
	    git.push( ).setRemote( "origin" ).setRefSpecs( new RefSpec( strRefSpec ) ).setForce( true ).setCredentialsProvider(new UsernamePasswordCredentialsProvider(strUserName, strPassword)).call();
    
	}
	
	public static PullResult pullRepoBranch(Git git, String sRepoURL, String sBranchName) throws IOException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException, TransportException, GitAPIException
	{
		PullResult pPullResult = git.pull().call();		
		return pPullResult;	
	}
	
	public static MergeResult mergeRepoBranch(Git git, String strBranchToMerge) throws IOException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException, TransportException, GitAPIException
	{
		List<Ref> call = git.branchList().call();
		Ref mergedBranchRef = null;
		for (Ref ref : call) 
		{
			if (ref.getName().equals("refs/heads/" + strBranchToMerge)) {
				mergedBranchRef = ref;
				break;
			}
		}
		MergeResult mergeResult = git.merge().include(mergedBranchRef).call();
		return mergeResult;	
	}
	
	public  static String getLastLog(Git git, int nMaxCommit) throws NoHeadException, GitAPIException
	{
		Iterable<RevCommit> logList = git.log().setMaxCount(1).call();
		Iterator i = logList.iterator();
		String sCommitMessages = "";
		while (i.hasNext())
		{
			RevCommit revCommit = (RevCommit) i.next();
			sCommitMessages += revCommit.getFullMessage();
			sCommitMessages += "\n";
			sCommitMessages += revCommit.getCommitterIdent();
		}
		return sCommitMessages;
	}
	
	
	public  static String getLastCommitId(Git git) throws NoHeadException, GitAPIException
    {
        Iterable<RevCommit> logList = git.log().setMaxCount(1).call();
        Iterator i = logList.iterator();
       String strCommitId = null;
        while (i.hasNext())
        {
            RevCommit revCommit = (RevCommit) i.next();
            strCommitId = revCommit.getName( );
          
        }
        return strCommitId;
    }
    
	
	  
    public static MergeResult mergeBack(Git git, String strUserName, String strPassword, CommandResult commandResult) throws IOException, GitAPIException
    {
        
            
             Ref tag = getTagLinkedToLastRelease(git);
            
            git.checkout().setName(MASTER_BRANCH).call();
            List<Ref> call = git.branchList().call();
            
            Ref mergedBranchRef = null;
            for (Ref ref : call) 
            {
                if (ref.getName().equals("refs/heads/"+DEVELOP_BRANCH)) {
                    mergedBranchRef = ref;
                    break;
                }
            }
            
            if (tag != null) {
                mergedBranchRef = tag;
            }
            MergeResult mergeResult = git.merge().include(mergedBranchRef).call();
            if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CHECKOUT_CONFLICT) || mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING) || mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.FAILED) || mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.NOT_SUPPORTED))
            {
                
                ReleaserUtils.addTechnicalError( commandResult, mergeResult.getMergeStatus().toString()
                        + "\nPlease merge manually master into"+ DEVELOP_BRANCH +"branch." );
            }
            else
            {
                git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(strUserName, strPassword)).call();
                commandResult.getLog( ).append(mergeResult.getMergeStatus());
            }
            return mergeResult;
            
        }
    
    
    public  static GithubSearchResult searchRepo(String strSearch,String strOrganization,String strUserName, String strPassword)
    {
        HttpAccess httpAccess = new HttpAccess(  );
        
        GithubSearchResult searchResult=null;
        
        String strUrl=null;
        try
        {
            strUrl = MessageFormat.format( AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_GITHUB_SEARCH_REPO_API ), URLEncoder.encode( strSearch,"UTF-8"),strOrganization  );
        }
        catch( UnsupportedEncodingException e1 )
        {
            AppLogService.error(e1);
        }
        
      
        String strResponse = "";
        
        try
        {
            
            strResponse = httpAccess.doGet(strUrl,  new BasicAuthorizationAuthenticator(  strUserName, strPassword ),null);
            
            if(!StringUtils.isEmpty( strResponse ))
            {
                searchResult=MapperJsonUtil.parse( strResponse, GithubSearchResult.class );
                
            }
            
        }
        catch ( HttpAccessException ex )
        {
            AppLogService.error( ex );
        }
        catch( IOException e )
        {
            AppLogService.error( e );
        }

        return searchResult;
    }
    
    public  static String getFileContent(String strFullName,String strPathFile,String strBranch,String strUserName, String strPassword)
    {
        HttpAccess httpAccess = new HttpAccess(  );
       String strUrl = "https://raw.githubusercontent.com/"+strFullName+"/"+strBranch+"/"+ strPathFile;
       //Map<String,String> hashHeader=new HashMap<>( );  
      //hashHeader.put( "accept", "application/vnd.github.VERSION.raw" );
        String strResponse = "";
        
        try
        {
          

            strResponse = httpAccess.doGet( strUrl, new BasicAuthorizationAuthenticator(  strUserName, strPassword ),null );
            
          

            
        }
        catch ( HttpAccessException ex )
        {
            AppLogService.error( ex );
        }
      

        return strResponse;
    }
    
    
    private static Ref getTagLinkedToLastRelease(Git git) throws GitAPIException {
        final String TOKEN = "[maven-release-plugin] prepare release ";
        Ref res = null;
        String sTagName = null;
        
        Iterable<RevCommit> logList = git.log().setMaxCount(10).call();
        Iterator i = logList.iterator();
        String sCommitMessages = "";
        while (i.hasNext())
        {
            RevCommit revCommit = (RevCommit) i.next();
            sCommitMessages = revCommit.getFullMessage();
            int index = sCommitMessages.indexOf(TOKEN);
            if (index >= 0) {
                sTagName = sCommitMessages.replace(TOKEN, "");
                break;
            }
        }
        
        if ( (sTagName != null) && (!(sTagName.trim().equals(""))) ) {
            List<Ref> tags = git.tagList().call();
            for (int j=0; j<tags.size(); j++) {
                Ref tag = tags.get(tags.size() - 1 - j);
                String tagName = tag.getName();
                if (tagName.equals("refs/tags/" + sTagName)) {
                    res = tag;
                    break;
                }
            }
        }
        
        return res;
    }
	
	
private static String getRepoUrl(String strRepoUrl)
{
    
    if(strRepoUrl!=null && strRepoUrl.startsWith( "scm:git:" ))
     {
        strRepoUrl=strRepoUrl.substring( 8 );
               
               
          }
    
    return strRepoUrl;
	
	
}





}
