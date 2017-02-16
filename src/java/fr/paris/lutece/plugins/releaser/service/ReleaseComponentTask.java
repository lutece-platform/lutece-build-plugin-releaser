package fr.paris.lutece.plugins.releaser.service;

import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

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
          

           Collection<Action> listActions= WorkflowService.getInstance(  ).getActions( _wfContext.getId( ), WorkflowReleaseContext.WORKFLOW_RESOURCE_TYPE,_nIdWorkflow,_user);
            
            for(Action action:listActions)
            {
                WorkflowService.getInstance(  ).doProcessAction( _wfContext.getId( ), WorkflowReleaseContext.WORKFLOW_RESOURCE_TYPE, action.getId( ), -1, _request, _locale, true ); 
               
            }
            
            
        }
        catch( AppException appe )
        {
            AppLogService.error( appe );
        }
        finally
        {
            ReleaserUtils.stopCommandResult( _wfContext );
            WorkflowReleaseContextService.getService( ).saveWorkflowReleaseContext( _wfContext );
        }
	}



    

}
