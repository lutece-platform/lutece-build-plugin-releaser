/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.paris.lutece.plugins.releaser.util.svn;

import fr.paris.lutece.test.LuteceTestCase;
import org.junit.Test;

/**
 *
 * @author levy
 */
public class SvnSiteServiceTest extends LuteceTestCase
{


    /**
     * Test of getLastRelease method, of class SvnSiteService.
     */
    @Test
    public void testGetLastRelease()
    {
        System.out.println( "getLastRelease" );
        String strSiteArtifactId = "moncompte";
        String strTrunkUrl = "http://dev.lutece.paris.fr/svn/sites/gru/multi-sites/moncompte/trunk/";
        String result = SvnSiteService.getLastRelease( strSiteArtifactId, strTrunkUrl );
        System.out.println( result ); 
    }
    
}
