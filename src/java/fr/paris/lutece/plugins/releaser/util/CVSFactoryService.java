package fr.paris.lutece.plugins.releaser.util;

import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.portal.service.spring.SpringContextService;

// TODO: Auto-generated Javadoc
/**
 * The Class CVSFactoryService.
 */
public class CVSFactoryService
{
    
    /** The svn service. */
    private static IVCSResourceService _svnService;
    
    /** The gitlab service. */
    private static IVCSResourceService _gitlabService;
    
    /** The github service. */
    private static IVCSResourceService _githubService;

    /**
     * Instantiates a new CVS factory service.
     */
    public CVSFactoryService( )
    {

    }

    /**
     * Inits the.
     */
    private static void init( )
    {

        _svnService = SpringContextService.getBean( ConstanteUtils.BEAN_SVN_RESOURCE_SERVICE );
        _gitlabService = SpringContextService.getBean( ConstanteUtils.BEAN_GITLAB_RESOURCE_SERVICE );
        _githubService = SpringContextService.getBean( ConstanteUtils.BEAN_GITHUB_RESOURCE_SERVICE );

    }

    /**
     * Gets the service.
     *
     * @param repositoryType the repository type
     * @return the service
     */
    public static IVCSResourceService getService( RepositoryType repositoryType )
    {

        if ( _svnService == null || _gitlabService == null || _githubService == null )
        {
            init( );
        }

        if ( repositoryType.equals( RepositoryType.GITHUB ) )
        {
            return _githubService;
        }
        else
            if ( repositoryType.equals( RepositoryType.GITLAB ) )
            {
                return _gitlabService;
            }

        return _svnService;

    }

}
