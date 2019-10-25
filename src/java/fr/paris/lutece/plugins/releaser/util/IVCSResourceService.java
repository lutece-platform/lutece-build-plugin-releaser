package fr.paris.lutece.plugins.releaser.util;

import java.util.Locale;

import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;

public interface IVCSResourceService
{

    
     String fetchPom( Site site,String strLogin,String strPassword);
    
     String getLastRelease( Site site,String strLogin,String strPassword );
     
     String doCheckoutRepository( WorkflowReleaseContext context, String strLogin, String strPassword );

    void updateDevelopBranch( WorkflowReleaseContext context, Locale locale, String strMessage );

    void updateMasterBranch( WorkflowReleaseContext context, Locale locale );

    void rollbackRelease(WorkflowReleaseContext context, Locale locale );

    void checkoutDevelopBranch( WorkflowReleaseContext context, Locale locale );
     
     
     
     
     
     
     
     
     
     
}
