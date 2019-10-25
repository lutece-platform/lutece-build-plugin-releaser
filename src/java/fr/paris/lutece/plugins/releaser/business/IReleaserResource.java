package fr.paris.lutece.plugins.releaser.business;

import org.apache.commons.lang.StringUtils;

public interface IReleaserResource
{
   String getScmUrl();
   
   String getArtifactId( );
  
   String getTargetVersion();
    
   RepositoryType getRepoType();
  

}
