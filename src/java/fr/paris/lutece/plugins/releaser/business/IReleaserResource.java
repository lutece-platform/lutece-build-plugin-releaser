package fr.paris.lutece.plugins.releaser.business;

import org.apache.commons.lang.StringUtils;

public interface IReleaserResource
{
   String getScmUrl();
   
   String getArtifactId( );
  
   String getTargetVersion();
    
   default RepositoryType getRepoType()
   {
       
       RepositoryType repositoryType=null;
       if(!StringUtils.isEmpty( getScmUrl() ))
       {
           if(getScmUrl( ).endsWith( ".git"))
           {
               
              if(getScmUrl().startsWith("https://github." ))
              {
                  repositoryType= RepositoryType.GITHUB;
                  
              }
              else
              {
                  repositoryType= RepositoryType.GITLAB; 
              }
               
           }
           else
           {
               repositoryType= RepositoryType.SVN; 
               
           }
           
       }
       return repositoryType;
       
   }


}
