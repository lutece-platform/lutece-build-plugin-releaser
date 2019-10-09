package fr.paris.lutece.plugins.releaser.util;

import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.portal.service.spring.SpringContextService;

public class CVSFactoryService
{
   private static IVCSSiteService _svnService;
   private  static IVCSSiteService _gitlabService;
   private  static IVCSSiteService _githubService;
   
   
   
   public CVSFactoryService()
   {
       
       
   }
   
   
   private static void init()
   {
       
       _svnService=SpringContextService.getBean( ConstanteUtils.BEAN_SVN_RESOURCE_SERVICE );
       _gitlabService=SpringContextService.getBean( ConstanteUtils.BEAN_GITLAB_RESOURCE_SERVICE );
       _githubService=SpringContextService.getBean( ConstanteUtils.BEAN_GITHUB_RESOURCE_SERVICE );

   }
   
   
  public static IVCSSiteService getService(RepositoryType repositoryType)
   {
       
       if(_svnService ==null || _gitlabService==null || _githubService==null)
       {
           init( );
       }
       
       if(repositoryType.equals( RepositoryType.GITHUB ))
       {
           return _githubService;
       }
       else if(repositoryType.equals( RepositoryType.GITLAB))
       {
           return _gitlabService;
       }
       
       return _svnService;
  
    }
   
}
