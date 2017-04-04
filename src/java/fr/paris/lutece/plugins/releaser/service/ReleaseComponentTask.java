package fr.paris.lutece.plugins.releaser.service;

import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.workflow.WorkflowService;


/**
 * 
 * NotificationClientTask
 *
 */
public class ReleaseComponentTask implements Runnable {

    private static final long WAIT_TIME=1000;
	private int _nIdWorkflow;
	private WorkflowReleaseContext _wfContext;
	private HttpServletRequest _request;
	private AdminUser _user;
	private Locale _locale;
    
	
	
	
	public ReleaseComponentTask(int nIdWf,WorkflowReleaseContext context,HttpServletRequest request,AdminUser user,Locale locale) {
		
	    _nIdWorkflow=nIdWf;
	    _wfContext=context;
	    _request=request;
	    _user=user;
	    _locale=locale;
	}

	

	/**
	 * runWorkflowRelease
	 *  
	 */
	  public void run( ){

	    State state = WorkflowService.getInstance(  )
                .getState( _wfContext.getId( ),
                        WorkflowReleaseContext.WORKFLOW_RESOURCE_TYPE, _nIdWorkflow,
                        ConstanteUtils.CONSTANTE_ID_NULL );
        ReleaserUtils.startCommandResult( _wfContext );
       
        
        
            
        
        
        
        try
        {
            
            //Wait all component released  before releasing site
            if(_wfContext.getSite( ) !=null)
            {
                while(!testAllComponentReleased( _wfContext.getSite( ) ))
                {
                   
                    try
                    {
                        this.wait( WAIT_TIME );
                    }
                    catch( InterruptedException e )
                    {
                        AppLogService.error( e );
                    }
                }
                
                if(hasErrorDuringReleaseComponent( _wfContext.getSite( ) ))
                {
                    ReleaserUtils.addTechnicalError( _wfContext.getCommandResult( ), "The site can not be retrieved because one  of component of the site is in error" );
                 }
            }
          

           Collection<Action> listActions= WorkflowService.getInstance(  ).getActions( _wfContext.getId( ), WorkflowReleaseContext.WORKFLOW_RESOURCE_TYPE,_nIdWorkflow,_user);
            
            for(Action action:listActions)
            {
                WorkflowService.getInstance(  ).doProcessAction( _wfContext.getId( ), WorkflowReleaseContext.WORKFLOW_RESOURCE_TYPE, action.getId( ), -1, _request, _locale, true ); 
                if(_wfContext.getComponent( )!=null)
                {
                    //Save in database the release and the next snapshot version
                    ComponentService.getService( ).setLastReleaseVersion(_wfContext.getComponent( ).getArtifactId( ) ,_wfContext.getComponent( ).getTargetVersion( ));
                    _wfContext.getComponent( ).setLastAvailableVersion( _wfContext.getComponent( ).getTargetVersion( ) );
                    ComponentService.getService( ).setLastReleaseNextSnapshotVersion(_wfContext.getComponent( ).getArtifactId( ) ,_wfContext.getComponent( ).getNextSnapshotVersion( ));
                    _wfContext.getComponent( ).setLastAvailableSnapshotVersion( _wfContext.getComponent( ).getNextSnapshotVersion( ) );
                }
            }
            
            
        }
        catch( AppException appe )
        {
           if( _wfContext.getComponent( )!=null)
           {
               
               _wfContext.getComponent( ).setErrorLastRelease( true );
           }
            AppLogService.error( appe );
        }
        finally
        {
            ReleaserUtils.stopCommandResult( _wfContext );
            WorkflowReleaseContextService.getService( ).saveWorkflowReleaseContext( _wfContext );
        }
	}



    private boolean testAllComponentReleased(Site site)
    {
        if(!CollectionUtils.isEmpty(site.getComponents( )))
        {
     
           for(Component component:site.getComponents( ))
           {
               //Test if the release is finished
               if(!component.isErrorLastRelease( ) && component.shouldBeReleased( ) )
               {
                   return false;
                   
               }
           }
        }
        
       return true;
        
    }
    
    private boolean hasErrorDuringReleaseComponent(Site site)
    {
        if(!CollectionUtils.isEmpty(site.getComponents( )))
        {
     
           for(Component component:site.getComponents( ))
           {
               //Test if the release is finished
               if(component.isErrorLastRelease( )  )
               {
                   return true;
                   
               }
           }
        }
        
       return false;
        
    }

}
