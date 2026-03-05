package fr.paris.lutece.plugins.releaser.service;

import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;

public interface IJenkinsService 
{	
	 /**
    * Inits the.
    */
   public abstract void init( );
   
   String TriggerPipeline ( WorkflowReleaseContext context );
}
