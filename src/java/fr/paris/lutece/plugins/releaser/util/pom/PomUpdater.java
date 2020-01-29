package fr.paris.lutece.plugins.releaser.util.pom;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.business.jaxb.maven.Model;
import fr.paris.lutece.plugins.releaser.business.jaxb.maven.ObjectFactory;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

// TODO: Auto-generated Javadoc
/**
 * The Class PomUpdater.
 */
public class PomUpdater
{

    /**
     * Update site before tag.
     *
     * @param site the site
     * @param strSiteLocalPomPath the str site local pom path
     * @return the string
     * @throws JAXBException the JAXB exception
     */
    public static String updateSiteBeforeTag( Site site, String strSiteLocalPomPath ) throws JAXBException
    {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try
        {

            inputStream = new FileInputStream( strSiteLocalPomPath );
            Model model = unmarshal( Model.class, inputStream );

            model.setDescription( site.getTagInformation( ) );

            fr.paris.lutece.plugins.releaser.business.jaxb.maven.Model.Dependencies dependencies = model.getDependencies( );
            // update dependencies
            if ( dependencies != null )
            {
                for ( fr.paris.lutece.plugins.releaser.business.jaxb.maven.Dependency jaxDependency : dependencies.getDependency( ) )
                {
                    for ( Component component : site.getComponents( ) )
                    {

                        if ( jaxDependency.getArtifactId( ).equals( component.getArtifactId( ) ) )
                        {

                            jaxDependency.setVersion( "[" + component.getTargetVersion( ) + "]" );
                        }
                    }

                }
            }

            String strParentSiteVersion = model.getParent( ).getVersion( );
            String strPomParentReferenceVersion = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_POM_PARENT_SITE_VERSION );

            if ( ReleaserUtils.compareVersion( strParentSiteVersion, strPomParentReferenceVersion ) < 0 )
            {
                // update pom parent version
                String strPomParentArtifactId = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_POM_PARENT_ARTIFCAT_ID );
                String strPomParentGroupId = AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_POM_PARENT_GROUP_ID );
                model.getParent( ).setArtifactId( strPomParentArtifactId );
                model.getParent( ).setGroupId( strPomParentGroupId );
                model.getParent( ).setVersion( strPomParentReferenceVersion );
            }

            outputStream = new FileOutputStream( strSiteLocalPomPath );

            save( model, outputStream );

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

    /**
     * Update site after tag.
     *
     * @param site the site
     * @param strSiteLocalPomPath the str site local pom path
     * @return the string
     * @throws JAXBException the JAXB exception
     */
    public static String updateSiteAfterTag( Site site, String strSiteLocalPomPath ) throws JAXBException
    {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try
        {

            inputStream = new FileInputStream( strSiteLocalPomPath );
            Model model = unmarshal( Model.class, inputStream );

            model.setVersion( site.getNextSnapshotVersion( ) );
            model.setDescription( "" );

            fr.paris.lutece.plugins.releaser.business.jaxb.maven.Model.Dependencies dependencies = model.getDependencies( );
            // update dependencies
            if ( dependencies != null )
            {
                for ( fr.paris.lutece.plugins.releaser.business.jaxb.maven.Dependency jaxDependency : dependencies.getDependency( ) )
                {
                    for ( Component component : site.getComponents( ) )
                    {

                        if ( jaxDependency.getArtifactId( ).equals( component.getArtifactId( ) ) )
                        {

                            if ( component.isProject( ) && component.isSnapshotVersion( ) )
                            {
                                jaxDependency.setVersion( component.getNextSnapshotVersion( ) );
                            }

                        }
                    }

                }
            }

            outputStream = new FileOutputStream( strSiteLocalPomPath );

            save( model, outputStream );

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

    /**
     * Save.
     *
     * @param model the model
     * @param outputStream the output stream
     * @throws JAXBException the JAXB exception
     */
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

    /**
     * Unmarshal.
     *
     * @param <T> the generic type
     * @param docClass the doc class
     * @param inputStream the input stream
     * @return the t
     * @throws JAXBException the JAXB exception
     */
    public static <T> T unmarshal( Class<T> docClass, InputStream inputStream ) throws JAXBException
    {
        String packageName = docClass.getPackage( ).getName( );
        JAXBContext jc = JAXBContext.newInstance( packageName );
        Unmarshaller u = jc.createUnmarshaller( );
        JAXBElement<T> doc = (JAXBElement<T>) u.unmarshal( inputStream );
        return doc.getValue( );
    }

}
