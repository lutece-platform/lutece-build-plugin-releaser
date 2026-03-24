package fr.paris.lutece.plugins.releaser.util.version;

import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.portal.service.util.AppLogService;

public class VersionUtils 
{
	 
    public static List<String> sortVersionsList( List<String> listVersions, boolean bAscending )
    {
        List<String> listSorted = new ArrayList<>( );

        if ( listVersions != null && !listVersions.isEmpty() )
        {
            List<Version> listParsedVersions = new ArrayList<>( );
            
        	for ( String strVersion : listVersions )
            {
                try
                {
                    listParsedVersions.add( Version.parse( strVersion ) );
                }
                catch ( VersionParsingException e )
                {
                    AppLogService.error( "Error parsing version, excluded from sorted list : " + strVersion );
                }
            }

            listParsedVersions.sort( ( v1, v2 ) -> bAscending ? v1.compareTo( v2 ) : v2.compareTo( v1 ) );

            for ( Version version : listParsedVersions )
            {
                listSorted.add( version.toString( ) );
            }
        }
        
        return listSorted;
    }

    public static String getLastVersion( List<String> listVersions )
    {
    	String strLastVersion = null;
    	
        if (listVersions != null && !listVersions.isEmpty() )
		{
			List<String> listReleaseVersions = VersionUtils.sortVersionsList(listVersions, true );
		    strLastVersion = listReleaseVersions.get(listReleaseVersions.size( ) - 1 );
		}

        return strLastVersion.toString();
    }
}
