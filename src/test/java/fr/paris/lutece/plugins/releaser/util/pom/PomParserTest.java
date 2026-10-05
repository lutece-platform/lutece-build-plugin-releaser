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

import fr.paris.lutece.plugins.releaser.util.pom.PomParser;
import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.business.Site;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * PomParserTest
 */
public class PomParserTest
{
    private static final String POM_TEST_FILE = "/pom.xml";
    private static final String POM_GLOBAL_LIKE_FILE = "/pom-global-like.xml";
    private static final String POM_PLATFORM_LIKE_FILE = "/pom-platform-like.xml";
    private static final String POM_BOM_LIKE_FILE = "/pom-bom-like.xml";

    @Test
    public void testParse( ) throws IOException
    {
        System.out.println( "testParse" );
        PomParser parser = new PomParser( );
        Site site = new Site( );
        String strPOM = loadFile( POM_TEST_FILE );
        parser.parse( site, strPOM );

        int nDependenciesCount = site.getCurrentDependencies( ).size( );
        System.out.println( "Number of dependencies found :" + nDependenciesCount );

        assertTrue( nDependenciesCount > 0 );
    }

    /**
     * The version and the parent of a pom are read as they are declared, the branch check of a platform component relies on them.
     */
    @Test
    public void testParseVersionAndParent( ) throws IOException
    {
        PomParser parser = new PomParser( );
        String strPOM = loadFile( POM_PLATFORM_LIKE_FILE );

        assertEquals( "8.0.0-SNAPSHOT", parser.parseVersion( strPOM ) );
        Dependency parent = parser.parseParent( strPOM );
        assertEquals( "lutece-global-pom", parent.getArtifactId( ) );
        assertEquals( "8.0.1-SNAPSHOT", parent.getVersion( ) );
        assertNull( parser.parseParent( "<project><artifactId>orphan</artifactId><version>1.0</version></project>" ) );
    }

    /**
     * A site pom whose versions use properties must expose resolved versions.
     */
    @Test
    public void testParseSiteResolvesProperties( ) throws IOException
    {
        PomParser parser = new PomParser( );
        Site site = new Site( );
        parser.parse( site, loadFile( POM_PLATFORM_LIKE_FILE ) );

        assertEquals( "lutece-parent", site.getArtifactId( ) );
        assertEquals( "8.0.0-SNAPSHOT", site.getVersion( ) );
        assertEquals( "8.0.1-SNAPSHOT", site.getParentVersion( ) );
    }

    /**
     * Placeholders are resolved recursively, unknown ones are kept, null is tolerated.
     */
    @Test
    public void testResolveValue( )
    {
        Map<String, String> mapProperties = new HashMap<>( );
        mapProperties.put( "a", "1.0" );
        mapProperties.put( "b", "${a}.1" );
        mapProperties.put( "c", "${b}-${a}" );
        mapProperties.put( "loop", "${loop}" );

        assertEquals( "1.0", PomParser.resolveValue( "${a}", mapProperties ) );
        assertEquals( "1.0.1", PomParser.resolveValue( "${b}", mapProperties ) );
        assertEquals( "1.0.1-1.0", PomParser.resolveValue( "${c}", mapProperties ) );
        assertEquals( "${unknown}", PomParser.resolveValue( "${unknown}", mapProperties ) );
        assertEquals( "${loop}", PomParser.resolveValue( "${loop}", mapProperties ) );
        assertEquals( "2.0.0", PomParser.resolveValue( "2.0.0", mapProperties ) );
        assertNull( PomParser.resolveValue( null, mapProperties ) );
    }

    /**
     * A global-pom like pom : Lutece artifacts come from dependencies, managed dependencies, plugins and managed plugins, with resolved versions.
     */
    @Test
    public void testParseReferencedArtifactsFiltered( ) throws IOException
    {
        PomParser parser = new PomParser( );
        List<Dependency> listArtifacts = parser.parseReferencedArtifacts( loadFile( POM_GLOBAL_LIKE_FILE ), "fr.paris.lutece" );

        Map<String, Dependency> mapByArtifactId = new HashMap<>( );
        for ( Dependency dep : listArtifacts )
        {
            mapByArtifactId.put( dep.getArtifactId( ), dep );
        }

        assertEquals( 5, listArtifacts.size( ) );
        assertEquals( "1.0.0", mapByArtifactId.get( "build-config" ).getVersion( ) );
        assertEquals( "5.0.1-SNAPSHOT", mapByArtifactId.get( "library-lutece-unit-testing" ).getVersion( ) );
        assertEquals( "1.0.2", mapByArtifactId.get( "xdoc2md-maven-plugin" ).getVersion( ) );
        assertEquals( "7.1.0", mapByArtifactId.get( "lutece-maven-plugin" ).getVersion( ) );
        assertEquals( "1.2.3", mapByArtifactId.get( "liberty-maven-plugin" ).getVersion( ) );
        assertEquals( PomParser.TYPE_MAVEN_PLUGIN, mapByArtifactId.get( "lutece-maven-plugin" ).getType( ) );
        assertEquals( "fr.paris.lutece.plugins", mapByArtifactId.get( "library-lutece-unit-testing" ).getGroupId( ) );
        assertEquals( "maven-lutece-plugin.version", mapByArtifactId.get( "lutece-maven-plugin" ).getVersionProperty( ) );
        assertEquals( "build-config.version", mapByArtifactId.get( "build-config" ).getVersionProperty( ) );
        assertNull( mapByArtifactId.get( "xdoc2md-maven-plugin" ).getVersionProperty( ) );
        assertFalse( mapByArtifactId.containsKey( "commons-lang3" ) );
        assertFalse( mapByArtifactId.containsKey( "maven-compiler-plugin" ) );
    }

    /**
     * Without filter every artifact is returned, including plugins declared without groupId.
     */
    @Test
    public void testParseReferencedArtifactsUnfiltered( ) throws IOException
    {
        PomParser parser = new PomParser( );
        List<Dependency> listArtifacts = parser.parseReferencedArtifacts( loadFile( POM_GLOBAL_LIKE_FILE ), null );

        assertEquals( 7, listArtifacts.size( ) );

        Dependency compiler = null;
        for ( Dependency dep : listArtifacts )
        {
            if ( "maven-compiler-plugin".equals( dep.getArtifactId( ) ) )
            {
                compiler = dep;
            }
        }

        assertNotNull( compiler );
        assertEquals( "org.apache.maven.plugins", compiler.getGroupId( ) );
        assertEquals( "3.11.0", compiler.getVersion( ) );
    }

    /**
     * A lutece-platform like pom : one artifact per lutece.*.version property, groupId resolved through the bill of materials.
     */
    @Test
    public void testParseVersionProperties( ) throws IOException
    {
        PomParser parser = new PomParser( );
        List<Dependency> listArtifacts = parser.parseVersionProperties( loadFile( POM_PLATFORM_LIKE_FILE ), "lutece.", ".version", loadFile( POM_BOM_LIKE_FILE ) );

        Map<String, Dependency> mapByArtifactId = new HashMap<>( );
        for ( Dependency dep : listArtifacts )
        {
            mapByArtifactId.put( dep.getArtifactId( ), dep );
        }

        assertEquals( 6, listArtifacts.size( ) );
        assertFalse( mapByArtifactId.containsKey( "deploy.skip" ) );
        assertEquals( "8.0.2-SNAPSHOT", mapByArtifactId.get( "core" ).getVersion( ) );
        assertNull( mapByArtifactId.get( "core" ).getGroupId( ) );
        assertEquals( "fr.paris.lutece.plugins", mapByArtifactId.get( "plugin-forms" ).getGroupId( ) );
        assertEquals( "4.2.0-SNAPSHOT", mapByArtifactId.get( "plugin-forms" ).getVersion( ) );
        assertEquals( "4.2.0-SNAPSHOT", mapByArtifactId.get( "module-workflow-forms" ).getVersion( ) );
        assertEquals( "fr.paris.lutece.plugins", mapByArtifactId.get( "library-lucene" ).getGroupId( ) );
        assertEquals( "lutece-plugin", mapByArtifactId.get( "plugin-forms" ).getType( ) );
        assertNull( mapByArtifactId.get( "library-lucene" ).getType( ) );
        assertNull( mapByArtifactId.get( "core" ).getType( ) );
        assertNull( mapByArtifactId.get( "forms-starter" ).getGroupId( ) );
        assertEquals( "forms-starter", listArtifacts.get( 0 ).getArtifactId( ) );
        assertEquals( "lutece.core.version", mapByArtifactId.get( "core" ).getVersionProperty( ) );
        assertEquals( "lutece.plugin-forms.version", mapByArtifactId.get( "plugin-forms" ).getVersionProperty( ) );
    }

    /**
     * The modules of a monorepo pom are listed in order ; a pom without modules gives an empty list.
     *
     * @throws IOException
     *             if a fixture cannot be read
     */
    @Test
    public void testParseModules( ) throws IOException
    {
        PomParser parser = new PomParser( );
        String strPom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId>"
                + "<version>1</version><packaging>pom</packaging><modules><module>lutece-bom</module><module>forms-starter</module></modules></project>";

        List<String> listModules = parser.parseModules( strPom );
        assertEquals( 2, listModules.size( ) );
        assertEquals( "lutece-bom", listModules.get( 0 ) );
        assertEquals( "forms-starter", listModules.get( 1 ) );
        assertTrue( parser.parseModules( loadFile( POM_GLOBAL_LIKE_FILE ) ).isEmpty( ) );
    }

    /**
     * Without bill of materials the groupIds are unknown but the versions are still extracted.
     */
    @Test
    public void testParseVersionPropertiesWithoutBom( ) throws IOException
    {
        PomParser parser = new PomParser( );
        List<Dependency> listArtifacts = parser.parseVersionProperties( loadFile( POM_PLATFORM_LIKE_FILE ), "lutece.", ".version", null );

        assertEquals( 6, listArtifacts.size( ) );
        for ( Dependency dep : listArtifacts )
        {
            assertNull( dep.getGroupId( ) );
            assertNotNull( dep.getVersion( ) );
        }
    }

    /**
     * A property is read and resolved against the other properties of the pom.
     */
    @Test
    public void testParseProperty( ) throws IOException
    {
        PomParser parser = new PomParser( );

        assertEquals( "8.0.2-SNAPSHOT", parser.parseProperty( loadFile( POM_PLATFORM_LIKE_FILE ), "lutece.core.version" ) );
        assertNull( parser.parseProperty( loadFile( POM_PLATFORM_LIKE_FILE ), "does.not.exist" ) );
    }

    private String loadFile( String strFilePath ) throws IOException
    {
        return IOUtils.toString( this.getClass( ).getResourceAsStream( strFilePath ), "UTF-8" );
    }

}
