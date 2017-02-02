package fr.paris.lutece.plugins.releaser.util.pom;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import fr.paris.lutece.plugins.releaser.business.jaxb.maven.Model;
import fr.paris.lutece.plugins.releaser.business.jaxb.maven.ObjectFactory;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.util.AppLogService;

public class PomUpdater
{

    public static String updateSiteVersion( String strPomFile, String strNewVersion ) throws JAXBException
    {
        
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try
        {
                inputStream = new FileInputStream( strPomFile );
            
            

            Model model = unmarshal( Model.class, inputStream );

            if ( !model.getVersion( ).equals( strNewVersion ) )
            {
                model.setVersion( strNewVersion );

                outputStream = new FileOutputStream( strPomFile );

                save( model, outputStream );
            }

            else
            {
                return "Pom already up to date\n";
            }
        }
        catch( FileNotFoundException e )
        {
          AppLogService.error( e );
        }
        finally
        {
            if ( outputStream != null )
            {
                try
                {
                    outputStream.close( );
                }
                catch( IOException ex )
                {
                    // nothing...
                    AppLogService.error( ex );
                }
            }
        }

        return "";
    }

    public static void save( Model model, OutputStream outputStream ) throws JAXBException
    {
        String packageName = model.getClass( ).getPackage( ).getName( );
        ObjectFactory factory = new ObjectFactory( );
        JAXBElement<Model> element = factory.createProject( model );

        JAXBContext jc = JAXBContext.newInstance( packageName );
        Marshaller m = jc.createMarshaller( );
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, "http://maven.apache.org/maven-v4_0_0.xsd" );
        m.marshal( element, outputStream );
    }

    public static <T> T unmarshal( Class<T> docClass, InputStream inputStream ) throws JAXBException
    {
        String packageName = docClass.getPackage( ).getName( );
        JAXBContext jc = JAXBContext.newInstance( packageName );
        Unmarshaller u = jc.createUnmarshaller( );
        JAXBElement<T> doc = (JAXBElement<T>) u.unmarshal( inputStream );
        return doc.getValue( );
    }

}
