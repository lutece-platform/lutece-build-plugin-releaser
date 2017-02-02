package fr.paris.lutece.plugins.releaser.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class LuteceTestFileUtils
{

    /**
     * get Property in test properties file
     * @param strProperty the property key
     * @return the property in test properties file
     */
    public static Properties getTestProperties(  )
    {
        Properties properties =null;
        try
        
        {
            URL url = Thread.currentThread(  ).getContextClassLoader(  ).getResource( "releaser-test.properties" );
            FileInputStream file = new FileInputStream( url.getPath(  ) );
           
            properties = new Properties(  );
            properties.load( file );
            
            
         }
        catch ( IOException ex )
        {
            throw new RuntimeException( "Unable to load test file : " + ex.getMessage(  ) );
        }

        return properties;
    }
    
    public static  void injectTestProperties(String strResourcesDir,String strClassName)throws IOException
    {
        
        File luteceProperties = new File( strResourcesDir, "WEB-INF/conf/plugins/releaser.properties" );
        Properties props = new Properties( );
        try ( InputStream is = new FileInputStream( luteceProperties ) )
        {
            props.load( is );
        }
       
        //inject properties Test file
        Properties testProperties=getTestProperties( );
        Enumeration<?> names = testProperties.propertyNames( );

        while ( names.hasMoreElements( ) )
        {
            String name = (String) names.nextElement( );
            props.put( name, testProperties.getProperty( name ) );
         }
       //rewrite WEB-INF/conf/plugins/releaser.properties
        try ( OutputStream os = new FileOutputStream( luteceProperties ) )
        {
            props.store( os, "saved for junit " + strClassName );
        }
        AppPropertiesService.reloadAll( );   
      }
}
