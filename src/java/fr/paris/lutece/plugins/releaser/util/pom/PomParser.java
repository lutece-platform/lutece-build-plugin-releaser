/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.releaser.util.pom;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import fr.paris.lutece.plugins.releaser.business.jaxb.maven.Build;
import fr.paris.lutece.plugins.releaser.business.jaxb.maven.DependencyManagement;
import fr.paris.lutece.plugins.releaser.business.jaxb.maven.Model;
import fr.paris.lutece.plugins.releaser.business.jaxb.maven.Plugin;
import fr.paris.lutece.plugins.releaser.business.jaxb.maven.PluginManagement;
import fr.paris.lutece.plugins.releaser.business.Component;
import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.portal.service.util.AppLogService;

// TODO: Auto-generated Javadoc
/**
 * PomParser.
 */
public class PomParser
{
    // Tags

    /** Start of a Maven property placeholder. */
    private static final String PLACEHOLDER_START = "${";

    /** End of a Maven property placeholder. */
    private static final String PLACEHOLDER_END = "}";

    /** Implicit property : the project version. */
    private static final String PROPERTY_PROJECT_VERSION = "project.version";

    /** Implicit property : the parent version. */
    private static final String PROPERTY_PROJECT_PARENT_VERSION = "project.parent.version";

    /** Type given to Maven build plugins extracted as dependencies. */
    public static final String TYPE_MAVEN_PLUGIN = "maven-plugin";

    /** Default groupId of a Maven build plugin declared without groupId. */
    private static final String DEFAULT_PLUGIN_GROUP_ID = "org.apache.maven.plugins";

    /** Maximum number of nested placeholder resolutions. */
    private static final int MAX_RESOLUTION_DEPTH = 10;

    /** The list dependencies. */
    private ArrayList<Dependency> _listDependencies = new ArrayList<Dependency>( );

    /**
     * Gets the dependencies.
     *
     * @return the dependencies
     */
    public List<Dependency> getDependencies( )
    {
        return _listDependencies;
    }

    /**
     * Parses the.
     *
     * @param site
     *            the site
     * @param strPOM
     *            the str POM
     */
    public void parse( Site site, String strPOM )
    {
        try
        {
            InputSource isPOM = new InputSource( new StringReader( strPOM ) );
            Model model = unmarshal( Model.class, isPOM );

            Map<String, String> mapProperties = getProperties( model );

            filledSite( site, model, mapProperties );

            fr.paris.lutece.plugins.releaser.business.jaxb.maven.Model.Dependencies dependencies = model.getDependencies( );

            if ( dependencies != null )
            {
                for ( fr.paris.lutece.plugins.releaser.business.jaxb.maven.Dependency jaxDependency : dependencies.getDependency( ) )
                {
                    filledDependency( site, jaxDependency, mapProperties );

                }
            }

            if ( model.getParent( ) != null && model.getParent( ).getVersion( ) != null )
            {
                site.setParentVersion( model.getParent().getVersion() );
            }
        }
        catch( JAXBException e )
        {
            AppLogService.error( e );
        }
    }




    /**
     * Parses the pom path.
     *
     * @param component
     *            the component
     * @param pomPath
     *            the pom path
     */   
	public void parsePomPath(Component component, String pomPath ) 
	{
	  try 
	  {	
	    parse(component,new FileInputStream(pomPath));
	  }
	  catch (FileNotFoundException e) 
	  {
		  AppLogService.error( e );
	  }
	}
	
    /**
     * Parses the.
     *
     * @param component
     *            the component
     * @param strPOM
     *            the str POM
     */
    public void parse( Component component, String strPOM )
    {

        try
        {
            InputSource isPOM = new InputSource( new StringReader( strPOM ) );
            Model model = unmarshal( Model.class, isPOM );
            component.setArtifactId( model.getArtifactId( ) );
            component.setGroupId( model.getGroupId( ) );
            component.setCurrentVersion( model.getVersion( ) );
            
            if(model.getProperties() != null ){
                Optional<Element> op= model.getProperties().getAny().stream().filter(x->x.getTagName().equals("targetJdk")).findFirst();
                if(op.isPresent())
                {
                  component.setTargetJdk(op.get().getTextContent());
                }   
            
                }
            if ( model.getScm( ) != null )
            {
                component.setScmDeveloperConnection( model.getScm( ).getDeveloperConnection( ) );
                component.setBranchReleaseVersion( model.getVersion( ) );

            }

        }
        catch( JAXBException e )
        {
            AppLogService.error( e );
        }

    }

    /**
     * Parses the.
     *
     * @param component
     *            the component
     * @param inputStream
     *            the input stream
     */
    public void parse( Component component, InputStream inputStream )
    {

        try
        {
            Model model = PomUpdater.unmarshal( Model.class, inputStream );
            component.setArtifactId( model.getArtifactId( ) );
            component.setGroupId( model.getGroupId( ) );
            component.setCurrentVersion( model.getVersion( ) );
            if(model.getProperties() != null ){

                Optional<Element> op= model.getProperties().getAny().stream().filter(x->x.getTagName().equals("targetJdk")).findFirst();
                if(op.isPresent())
                {
                  component.setTargetJdk(op.get().getTextContent());
                }   
            
          } 
            if ( model.getScm( ) != null )
            {
                component.setScmDeveloperConnection( model.getScm( ).getDeveloperConnection( ) );
                component.setBranchReleaseVersion( model.getVersion( ) );

            }
        }
        catch( JAXBException e )
        {
            AppLogService.error( e );
        }
        finally
        {

            try
            {
                inputStream.close( );
            }
            catch( IOException e )
            {
                AppLogService.error( e );
            }
        }

    }
    
    /**
     * Filled site.
     *
     * @param site
     *            the site
     * @param model
     *            the model
     * @param mapProperties
     *            the properties of the pom, used to resolve placeholders
     */
    private void filledSite( Site site, Model model, Map<String, String> mapProperties )
    {
        site.setArtifactId( model.getArtifactId( ) );
        site.setGroupId( resolveValue( model.getGroupId( ), mapProperties ) );
        site.setVersion( resolveValue( model.getVersion( ), mapProperties ) );
    }

    /**
     * Filled dependency.
     *
     * @param site
     *            the site
     * @param jaxDependency
     *            the jax dependency
     * @param mapProperties
     *            the properties of the pom, used to resolve placeholders
     */
    private void filledDependency( Site site, fr.paris.lutece.plugins.releaser.business.jaxb.maven.Dependency jaxDependency, Map<String, String> mapProperties )
    {
        site.addCurrentDependency( toDependency( jaxDependency, mapProperties ) );
    }

    /**
     * Converts a JAXB dependency into a releaser dependency, resolving property placeholders in the version.
     *
     * @param jaxDependency
     *            the JAXB dependency
     * @param mapProperties
     *            the properties of the pom
     * @return the dependency
     */
    private static Dependency toDependency( fr.paris.lutece.plugins.releaser.business.jaxb.maven.Dependency jaxDependency, Map<String, String> mapProperties )
    {
        Dependency dep = new Dependency( );
        dep.setArtifactId( jaxDependency.getArtifactId( ) );
        dep.setVersion( resolveValue( jaxDependency.getVersion( ), mapProperties ) );
        dep.setVersionProperty( getVersionProperty( jaxDependency.getVersion( ) ) );
        dep.setGroupId( resolveValue( jaxDependency.getGroupId( ), mapProperties ) );
        dep.setType( jaxDependency.getType( ) );

        return dep;
    }

    /**
     * Returns the property name when a version is declared as a single property reference ({@code ${name}}), null otherwise.
     *
     * @param strRawVersion
     *            the version as written in the pom
     * @return the property name, or null
     */
    private static String getVersionProperty( String strRawVersion )
    {
        if ( strRawVersion == null )
        {
            return null;
        }

        String strTrimmed = strRawVersion.trim( );
        if ( strTrimmed.startsWith( PLACEHOLDER_START ) && strTrimmed.endsWith( PLACEHOLDER_END ) && strTrimmed.indexOf( PLACEHOLDER_START, 1 ) < 0 )
        {
            return strTrimmed.substring( PLACEHOLDER_START.length( ), strTrimmed.length( ) - PLACEHOLDER_END.length( ) );
        }

        return null;
    }

    /**
     * Converts a JAXB build plugin into a releaser dependency of type {@link #TYPE_MAVEN_PLUGIN}, resolving property placeholders in the version.
     *
     * @param jaxPlugin
     *            the JAXB plugin
     * @param mapProperties
     *            the properties of the pom
     * @return the dependency
     */
    private static Dependency toDependency( Plugin jaxPlugin, Map<String, String> mapProperties )
    {
        Dependency dep = new Dependency( );
        dep.setArtifactId( jaxPlugin.getArtifactId( ) );
        dep.setVersion( resolveValue( jaxPlugin.getVersion( ), mapProperties ) );
        dep.setVersionProperty( getVersionProperty( jaxPlugin.getVersion( ) ) );
        dep.setGroupId( resolveValue( StringUtils.defaultIfBlank( jaxPlugin.getGroupId( ), DEFAULT_PLUGIN_GROUP_ID ), mapProperties ) );
        dep.setType( TYPE_MAVEN_PLUGIN );

        return dep;
    }

    /**
     * Returns the properties declared in the pom, plus the implicit {@code project.version} and {@code project.parent.version}.
     *
     * @param model
     *            the JAXB model of the pom
     * @return the properties (never null)
     */
    public static Map<String, String> getProperties( Model model )
    {
        Map<String, String> mapProperties = new LinkedHashMap<>( );

        if ( model.getProperties( ) != null )
        {
            for ( Element element : model.getProperties( ).getAny( ) )
            {
                mapProperties.put( element.getTagName( ), StringUtils.trim( element.getTextContent( ) ) );
            }
        }

        if ( model.getParent( ) != null && model.getParent( ).getVersion( ) != null )
        {
            mapProperties.put( PROPERTY_PROJECT_PARENT_VERSION, model.getParent( ).getVersion( ) );
        }

        String strVersion = model.getVersion( ) != null ? model.getVersion( ) : mapProperties.get( PROPERTY_PROJECT_PARENT_VERSION );

        if ( strVersion != null )
        {
            mapProperties.put( PROPERTY_PROJECT_VERSION, strVersion );
        }

        return mapProperties;
    }

    /**
     * Resolves the Maven property placeholders ({@code ${name}}) of a value, recursively. Unknown placeholders are left as is.
     *
     * @param strValue
     *            the value, possibly null
     * @param mapProperties
     *            the properties
     * @return the resolved value
     */
    public static String resolveValue( String strValue, Map<String, String> mapProperties )
    {
        String strResolved = strValue;

        for ( int nDepth = 0; strResolved != null && strResolved.contains( PLACEHOLDER_START ) && nDepth < MAX_RESOLUTION_DEPTH; nDepth++ )
        {
            String strNext = resolveOnce( strResolved, mapProperties );

            if ( strNext.equals( strResolved ) )
            {
                break;
            }

            strResolved = strNext;
        }

        return strResolved;
    }

    /**
     * Replaces one level of placeholders in a value.
     *
     * @param strValue
     *            the value
     * @param mapProperties
     *            the properties
     * @return the value with the known placeholders replaced
     */
    private static String resolveOnce( String strValue, Map<String, String> mapProperties )
    {
        StringBuilder sb = new StringBuilder( );
        int nPos = 0;
        int nStart = strValue.indexOf( PLACEHOLDER_START );

        while ( nStart >= 0 )
        {
            int nEnd = strValue.indexOf( PLACEHOLDER_END, nStart );

            if ( nEnd < 0 )
            {
                break;
            }

            String strName = strValue.substring( nStart + PLACEHOLDER_START.length( ), nEnd );
            String strReplacement = mapProperties.get( strName );

            sb.append( strValue, nPos, nStart );
            sb.append( strReplacement != null ? strReplacement : strValue.substring( nStart, nEnd + 1 ) );

            nPos = nEnd + 1;
            nStart = strValue.indexOf( PLACEHOLDER_START, nPos );
        }

        sb.append( strValue.substring( nPos ) );

        return sb.toString( );
    }

    /**
     * Returns the version of a pom, inherited from its parent when the project declares none.
     *
     * @param strPOM
     *            the pom content
     * @return the version, null if absent or if the pom cannot be parsed
     */
    public String parseVersion( String strPOM )
    {
        try
        {
            Model model = unmarshal( Model.class, new InputSource( new StringReader( strPOM ) ) );
            if ( model.getVersion( ) != null )
            {
                return model.getVersion( );
            }

            return model.getParent( ) != null ? model.getParent( ).getVersion( ) : null;
        }
        catch( JAXBException e )
        {
            AppLogService.error( e );
            return null;
        }
    }

    /**
     * Returns the parent of a pom as a dependency : groupId, artifactId and version.
     *
     * @param strPOM
     *            the pom content
     * @return the parent, null if the pom has none or cannot be parsed
     */
    public Dependency parseParent( String strPOM )
    {
        try
        {
            Model model = unmarshal( Model.class, new InputSource( new StringReader( strPOM ) ) );
            if ( model.getParent( ) == null )
            {
                return null;
            }
            Dependency parent = new Dependency( );
            parent.setGroupId( model.getParent( ).getGroupId( ) );
            parent.setArtifactId( model.getParent( ).getArtifactId( ) );
            parent.setVersion( model.getParent( ).getVersion( ) );

            return parent;
        }
        catch( JAXBException e )
        {
            AppLogService.error( e );
            return null;
        }
    }

    /**
     * Returns the modules declared by a pom, in declaration order.
     *
     * @param strPOM
     *            the pom content
     * @return the module names (empty if the pom has none or cannot be parsed)
     */
    public List<String> parseModules( String strPOM )
    {
        try
        {
            Model model = unmarshal( Model.class, new InputSource( new StringReader( strPOM ) ) );
            if ( model.getModules( ) != null && model.getModules( ).getModule( ) != null )
            {
                return new ArrayList<>( model.getModules( ).getModule( ) );
            }
        }
        catch( JAXBException e )
        {
            AppLogService.error( e );
        }

        return Collections.emptyList( );
    }

    /**
     * Returns the value of a property of a pom, resolved against the other properties.
     *
     * @param strPOM
     *            the pom content
     * @param strName
     *            the property name
     * @return the value, null if the property is absent or the pom cannot be parsed
     */
    public String parseProperty( String strPOM, String strName )
    {
        try
        {
            Map<String, String> mapProperties = getProperties( unmarshal( Model.class, new InputSource( new StringReader( strPOM ) ) ) );
            String strValue = mapProperties.get( strName );

            return strValue != null ? resolveValue( strValue, mapProperties ) : null;
        }
        catch( JAXBException e )
        {
            AppLogService.error( e );
            return null;
        }
    }

    /**
     * Extracts every artifact referenced by a pom : dependencies, managed dependencies, build plugins and managed plugins. Versions are resolved
     * against the pom properties. Artifacts are deduplicated on groupId:artifactId (first declaration wins) and optionally filtered on a groupId prefix.
     *
     * @param strPOM
     *            the pom content
     * @param strGroupIdPrefix
     *            the groupId prefix to keep, or null to keep every artifact
     * @return the artifacts, in declaration order (empty if the pom cannot be parsed)
     */
    public List<Dependency> parseReferencedArtifacts( String strPOM, String strGroupIdPrefix )
    {
        Map<String, Dependency> mapArtifacts = new LinkedHashMap<>( );

        try
        {
            Model model = unmarshal( Model.class, new InputSource( new StringReader( strPOM ) ) );
            Map<String, String> mapProperties = getProperties( model );

            if ( model.getDependencies( ) != null )
            {
                for ( fr.paris.lutece.plugins.releaser.business.jaxb.maven.Dependency jaxDependency : model.getDependencies( ).getDependency( ) )
                {
                    addArtifact( mapArtifacts, toDependency( jaxDependency, mapProperties ), strGroupIdPrefix );
                }
            }

            DependencyManagement dependencyManagement = model.getDependencyManagement( );

            if ( dependencyManagement != null && dependencyManagement.getDependencies( ) != null )
            {
                for ( fr.paris.lutece.plugins.releaser.business.jaxb.maven.Dependency jaxDependency : dependencyManagement.getDependencies( ).getDependency( ) )
                {
                    addArtifact( mapArtifacts, toDependency( jaxDependency, mapProperties ), strGroupIdPrefix );
                }
            }

            Build build = model.getBuild( );

            if ( build != null && build.getPlugins( ) != null )
            {
                for ( Plugin jaxPlugin : build.getPlugins( ).getPlugin( ) )
                {
                    addArtifact( mapArtifacts, toDependency( jaxPlugin, mapProperties ), strGroupIdPrefix );
                }
            }

            PluginManagement pluginManagement = build != null ? build.getPluginManagement( ) : null;

            if ( pluginManagement != null && pluginManagement.getPlugins( ) != null )
            {
                for ( Plugin jaxPlugin : pluginManagement.getPlugins( ).getPlugin( ) )
                {
                    addArtifact( mapArtifacts, toDependency( jaxPlugin, mapProperties ), strGroupIdPrefix );
                }
            }
        }
        catch( JAXBException e )
        {
            AppLogService.error( e );
        }

        return new ArrayList<>( mapArtifacts.values( ) );
    }

    /**
     * Adds an artifact to the extraction result if it matches the groupId filter and is not already present.
     *
     * @param mapArtifacts
     *            the result, keyed by groupId:artifactId
     * @param dependency
     *            the artifact
     * @param strGroupIdPrefix
     *            the groupId prefix to keep, or null
     */
    private static void addArtifact( Map<String, Dependency> mapArtifacts, Dependency dependency, String strGroupIdPrefix )
    {
        if ( StringUtils.isBlank( dependency.getArtifactId( ) ) )
        {
            return;
        }

        if ( StringUtils.isNotBlank( strGroupIdPrefix ) && !StringUtils.startsWith( dependency.getGroupId( ), strGroupIdPrefix ) )
        {
            return;
        }

        mapArtifacts.putIfAbsent( dependency.getGroupId( ) + ":" + dependency.getArtifactId( ), dependency );
    }

    /**
     * Extracts the artifacts whose version is driven by a naming convention on the pom properties, such as {@code lutece.<artifactId>.version}. The
     * groupId of each artifact is looked up in the managed dependencies of an optional bill of materials, resolved with the same properties.
     *
     * @param strPOM
     *            the pom holding the version properties
     * @param strPropertyPrefix
     *            the property prefix (ex : {@code lutece.})
     * @param strPropertySuffix
     *            the property suffix (ex : {@code .version})
     * @param strBomPOM
     *            the bill of materials pom, or null
     * @return the artifacts, in property declaration order (groupId null when unknown ; empty if the pom cannot be parsed)
     */
    public List<Dependency> parseVersionProperties( String strPOM, String strPropertyPrefix, String strPropertySuffix, String strBomPOM )
    {
        List<Dependency> listArtifacts = new ArrayList<>( );

        try
        {
            Model model = unmarshal( Model.class, new InputSource( new StringReader( strPOM ) ) );
            Map<String, String> mapProperties = getProperties( model );
            Map<String, Dependency> mapManaged = getManagedDependencies( strBomPOM, mapProperties );

            for ( Map.Entry<String, String> entry : mapProperties.entrySet( ) )
            {
                String strName = entry.getKey( );

                if ( !strName.startsWith( strPropertyPrefix ) || !strName.endsWith( strPropertySuffix )
                        || strName.length( ) <= strPropertyPrefix.length( ) + strPropertySuffix.length( ) )
                {
                    continue;
                }

                String strArtifactId = strName.substring( strPropertyPrefix.length( ), strName.length( ) - strPropertySuffix.length( ) );
                Dependency managed = mapManaged.get( strArtifactId );

                Dependency dep = new Dependency( );
                dep.setArtifactId( strArtifactId );
                dep.setGroupId( managed != null ? managed.getGroupId( ) : null );
                dep.setType( managed != null ? managed.getType( ) : null );
                dep.setVersion( resolveValue( entry.getValue( ), mapProperties ) );
                dep.setVersionProperty( strName );
                listArtifacts.add( dep );
            }
        }
        catch( JAXBException e )
        {
            AppLogService.error( e );
        }

        return listArtifacts;
    }

    /**
     * Returns the groupId and type of every dependency managed by a bill of materials, keyed by artifactId.
     *
     * @param strBomPOM
     *            the bill of materials pom, or null
     * @param mapParentProperties
     *            the properties of the parent pom, used to resolve the bill of materials
     * @return the managed dependencies by artifactId (empty if no bill of materials)
     * @throws JAXBException
     *             if the bill of materials cannot be parsed
     */
    private static Map<String, Dependency> getManagedDependencies( String strBomPOM, Map<String, String> mapParentProperties ) throws JAXBException
    {
        if ( StringUtils.isBlank( strBomPOM ) )
        {
            return Collections.emptyMap( );
        }

        Model bom = unmarshal( Model.class, new InputSource( new StringReader( strBomPOM ) ) );
        Map<String, String> mapProperties = new LinkedHashMap<>( mapParentProperties );
        mapProperties.putAll( getProperties( bom ) );
        Map<String, Dependency> mapManaged = new LinkedHashMap<>( );

        DependencyManagement dependencyManagement = bom.getDependencyManagement( );

        if ( dependencyManagement != null && dependencyManagement.getDependencies( ) != null )
        {
            for ( fr.paris.lutece.plugins.releaser.business.jaxb.maven.Dependency jaxDependency : dependencyManagement.getDependencies( ).getDependency( ) )
            {
                Dependency managed = new Dependency( );
                managed.setArtifactId( jaxDependency.getArtifactId( ) );
                managed.setGroupId( resolveValue( jaxDependency.getGroupId( ), mapProperties ) );
                managed.setType( jaxDependency.getType( ) );
                mapManaged.putIfAbsent( jaxDependency.getArtifactId( ), managed );
            }
        }

        return mapManaged;
    }

    /**
     * Unmarshal.
     *
     * @param <T>
     *            the generic type
     * @param docClass
     *            the doc class
     * @param inputSource
     *            the input source
     * @return the t
     * @throws JAXBException
     *             the JAXB exception
     */
    public static <T> T unmarshal( Class<T> docClass, InputSource inputSource ) throws JAXBException
    {
        String packageName = docClass.getPackage( ).getName( );
        JAXBContext jc = JAXBContext.newInstance( packageName );
        Unmarshaller u = jc.createUnmarshaller( );
        JAXBElement<T> doc = (JAXBElement<T>) u.unmarshal( inputSource );

        return doc.getValue( );
    }

}
