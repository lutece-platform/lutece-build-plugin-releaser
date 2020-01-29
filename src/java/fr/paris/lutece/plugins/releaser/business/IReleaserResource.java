package fr.paris.lutece.plugins.releaser.business;

import org.apache.commons.lang.StringUtils;
/**
 * 
 *IReleaserResource
 *
 */
public interface IReleaserResource
{
   /**
    * 
    * @return scm url
    */
	String getScmUrl( );

	/**
	 * 
	 * @return artifact Id
	 */
    String getArtifactId( );

    /**
     * 
     * @return taget version
     */
    String getTargetVersion( );

    /**
     * 
     * @return repo type
     */
    RepositoryType getRepoType( );

}
