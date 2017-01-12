/*
 * Copyright (c) 2002-2015, Mairie de Paris
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

import fr.paris.lutece.plugins.releaser.business.Dependency;
import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.portal.service.util.AppLogService;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * PomParser
 */
public class PomParser
{
    // Tags

    private static final String TAG_DEPENDENCY = "dependency";
    private static final String TAG_GROUP_ID = "groupId";
    private static final String TAG_ARTIFACT_ID = "artifactId";
    private static final String TAG_NAME = "name";
    private static final String TAG_VERSION = "version";
    private static final String TAG_TYPE = "type";
    private static final String TAG_MAIN_NODE = "project";

    private ArrayList<Dependency> _listDependencies = new ArrayList<Dependency>();

    public List<Dependency> getDependencies()
    {
        return _listDependencies;
    }

    public void parse( Site site, String strPOM )
    {
        try
        {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource isPOM = new InputSource( new StringReader( strPOM ) );
            Document doc = dBuilder.parse( isPOM );

            doc.getDocumentElement().normalize();

            NodeList nlSite = doc.getChildNodes().item( 0 ).getChildNodes();
            filledSite( site , nlSite );
            NodeList nlDenpendency = doc.getElementsByTagName( TAG_DEPENDENCY );

            for( int i = 0; i < nlDenpendency.getLength(); i++ )
            {
                Node n = nlDenpendency.item( i );
                // test if node dependency is not in configuration node
                if( n.getNodeType() == Node.ELEMENT_NODE && n.getParentNode().getParentNode().getNodeName().equals( TAG_MAIN_NODE ) )
                {
                    filledDependency( site, n );
                }
            }
        }
        catch( ParserConfigurationException | SAXException | IOException e )
        {
            AppLogService.error( e.getMessage(), e );
        }
    }

    private void filledSite( Site site, NodeList nlSite )
    {
        for( int i = 0; i < nlSite.getLength(); i++ )
        {
            Node p = nlSite.item( i );
            if( p.getNodeType() == Node.ELEMENT_NODE )
            {
                if( p.getNodeName().equals( TAG_ARTIFACT_ID ) )
                {
                    site.setArtifactId( p.getTextContent().trim() );
                }
                if( p.getNodeName().equals( TAG_VERSION ) )
                {
                    site.setVersion( p.getTextContent().trim() );
                }
                if( p.getNodeName().equals( TAG_NAME ) )
                {
                    site.setName( p.getTextContent().trim() );
                }
            }
        }
    }

    private void filledDependency( Site site, Node n )
    {
        Dependency dep = new Dependency();
        NodeList nl = n.getChildNodes();

        for( int i = 0; i < nl.getLength(); i++ )
        {
            Node t = n.getChildNodes().item( i );
            if( t.getNodeType() == Node.ELEMENT_NODE )
            {
                if( t.getNodeName().equals( TAG_ARTIFACT_ID ) )
                {
                    dep.setArtifactId( t.getTextContent().trim() );
                }
                if( t.getNodeName().equals( TAG_VERSION ) )
                {
                    dep.setVersion( t.getTextContent().trim() );
                }
                if( t.getNodeName().equals( TAG_GROUP_ID ) )
                {
                    dep.setGroupId( t.getTextContent().trim() );
                }
                if( t.getNodeName().equals( TAG_TYPE ) )
                {
                    dep.setType( t.getTextContent().trim() );
                }
            }
        }
        site.addCurrentDependency( dep );
    }
}
