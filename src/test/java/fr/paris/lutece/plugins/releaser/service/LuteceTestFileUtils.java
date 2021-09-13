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
package fr.paris.lutece.plugins.releaser.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import fr.paris.lutece.plugins.releaser.business.RepositoryType;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class LuteceTestFileUtils
{

    /**
     * get Property in test properties file
     * 
     * @param strProperty
     *            the property key
     * @return the property in test properties file
     */
    public static Properties getTestProperties( )
    {
        Properties properties = null;
        try
        {
            URL url = Thread.currentThread( ).getContextClassLoader( ).getResource( "releaser-test.properties" );
            FileInputStream file = new FileInputStream( url.getPath( ) );

            properties = new Properties( );
            properties.load( file );

        }
        catch( IOException ex )
        {
            throw new RuntimeException( "Unable to load test file : " + ex.getMessage( ) );
        }

        return properties;
    }

    public static void injectTestProperties( String strResourcesDir, String strClassName ) throws IOException
    {

        File luteceProperties = new File( strResourcesDir, "WEB-INF/conf/plugins/releaser.properties" );
        Properties props = new Properties( );
        try ( InputStream is = new FileInputStream( luteceProperties ) )
        {
            props.load( is );
        }

        // inject properties Test file
        Properties testProperties = getTestProperties( );
        Enumeration<?> names = testProperties.propertyNames( );

        while ( names.hasMoreElements( ) )
        {
            String name = (String) names.nextElement( );
            props.put( name, testProperties.getProperty( name ) );
        }
        // rewrite WEB-INF/conf/plugins/releaser.properties
        try ( OutputStream os = new FileOutputStream( luteceProperties ) )
        {
            props.store( os, "saved for junit " + strClassName );
        }
        AppPropertiesService.reloadAll( );
    }

    public static String getProperty( String strProperty, RepositoryType type )
    {
        return AppPropertiesService.getProperty( "releaser.test." + type.name( ).toLowerCase( ) + "." + strProperty );

    }
}
