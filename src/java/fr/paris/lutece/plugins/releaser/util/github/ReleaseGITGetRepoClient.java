package fr.paris.lutece.plugins.releaser.util.github;


public class ReleaseGITGetRepoClient
{

	private boolean bCancelled;
	
	public ReleaseGITGetRepoClient( )
	{
	}
	
	
	public void doCancel(  )
	{
		bCancelled = true;
	}
}
