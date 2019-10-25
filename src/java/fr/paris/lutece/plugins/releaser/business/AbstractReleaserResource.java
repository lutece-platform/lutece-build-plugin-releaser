package fr.paris.lutece.plugins.releaser.business;

import org.apache.commons.lang.StringUtils;

public abstract class AbstractReleaserResource implements IReleaserResource
{

    public RepositoryType getRepoType()
    {
        
        RepositoryType repositoryType=null;
        if(!StringUtils.isEmpty( getScmUrl() ))
        {
            if(getScmUrl( ).endsWith( ".git"))
            {
                
               if(getScmUrl().contains("https://github." ))
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
