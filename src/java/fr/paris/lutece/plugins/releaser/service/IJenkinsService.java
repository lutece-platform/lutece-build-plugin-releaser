package fr.paris.lutece.plugins.releaser.service;

public interface IJenkinsService 
{	
	 /**
    * Inits the.
    */
   public abstract void init( );
   
   String TriggerPipeline ( String strRepositoryUrl, String strBranchToRelease );
}
