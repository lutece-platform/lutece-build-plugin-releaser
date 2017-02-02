package fr.paris.lutece.plugins.releaser.util.github;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;





public class GitUtils  {
	
    public static final String MASTER_BRANCH = "master";
    public static final String DEVELOP_BRANCH = "develop";
    

	public static  void cloneRepo(String sClonePath, String sRepoURL) throws IOException, InvalidRemoteException, TransportException, GitAPIException
	{
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		File fGitDir = new File(sClonePath);
		Repository repository = builder.setGitDir(fGitDir).readEnvironment().findGitDir().build();

		CloneCommand clone = Git.cloneRepository().setBare(false).setCloneAllBranches(true).setDirectory(fGitDir).setURI(sRepoURL);
		
		clone.call();
		
		repository.close();
		
	}
	
	
	public static void checkoutRepoBranch(Git git, String sBranchName) throws IOException, RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException
	{
		git.checkout().setName(sBranchName).call();
		
	}
	
	public static void createLocalBranch(Git git, String sBranchName) throws IOException, RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, GitAPIException
	{
		git.branchCreate() 
	       .setName(sBranchName)
	       .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
	       .setStartPoint("origin/" + sBranchName)
	       .setForce(true)
	       .call();
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
	
	

	
	
}
