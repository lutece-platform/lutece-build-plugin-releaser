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

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;
import fr.paris.lutece.plugins.releaser.util.CommandResult;
import fr.paris.lutece.plugins.releaser.util.ConstanteUtils;
import fr.paris.lutece.plugins.releaser.util.ReleaserUtils;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

// TODO: Auto-generated Javadoc
/**
 * The Class TwitterService.
 */
public class TwitterService implements ITwitterService
{

    /** The Constant TWITTER_KEY_OAUTH_CONSUMER_KEY. */
    private static final String TWITTER_KEY_OAUTH_CONSUMER_KEY = "oauth.consumerKey";

    /** The Constant TWITTER_KEY_OAUTH_CONSUMER_SECRET. */
    private static final String TWITTER_KEY_OAUTH_CONSUMER_SECRET = "oauth.consumerSecret";

    /** The Constant TWITTER_KEY_OAUTH_ACCESS_TOKEN. */
    private static final String TWITTER_KEY_OAUTH_ACCESS_TOKEN = "oauth.accessToken";

    /** The Constant TWITTER_KEY_OAUTH_ACCESS_TOKEN_SECRET. */
    private static final String TWITTER_KEY_OAUTH_ACCESS_TOKEN_SECRET = "oauth.accessTokenSecret";

    /** The Constant TWITTER_KEY_OAUTH_ACCESS_TOKEN_URL. */
    private static final String TWITTER_KEY_OAUTH_ACCESS_TOKEN_URL = "oauth.accessTokenURL";

    /** The Constant TWITTER_KEY_OAUTH_REQUEST_TOKEN_URL. */
    private static final String TWITTER_KEY_OAUTH_REQUEST_TOKEN_URL = "oauth.requestTokenURL";

    /** The Constant TWITTER_KEY_OAUTH_AUTHORIZATION_URL. */
    private static final String TWITTER_KEY_OAUTH_AUTHORIZATION_URL = "oauth.authorizationURL";

    /** The Constant TWITTER_KEY_HTTP_PROXY_HOST. */
    private static final String TWITTER_KEY_HTTP_PROXY_HOST = " http.proxyHost";

    /** The Constant TWITTER_KEY_HTTP_PROXY_PORT. */
    private static final String TWITTER_KEY_HTTP_PROXY_PORT = " http.proxyPort";

    /** The twitter. */
    private static Twitter _twitter = null;

    /** The instance. */
    private static ITwitterService _instance = null;

    /**
     * Gets the service.
     *
     * @return the service
     */
    public static ITwitterService getService( )
    {
        if ( _instance == null )
        {
            _instance = SpringContextService.getBean( ConstanteUtils.BEAN_TWITTER_SERVICE );
            _instance.init( );
        }

        return _instance;
    }

    /**
     * Instantiates a new twitter service.
     */
    public TwitterService( )
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * Send tweet.
     *
     * @param strTweet
     *            the str tweet
     * @param commandResult
     *            the command result
     */
    public void sendTweet( String strTweet, CommandResult commandResult )
    {

        try
        {
            if ( !StringUtils.isEmpty( strTweet ) )
            {
                Status status = _twitter.updateStatus( strTweet );
                commandResult.getLog( ).append( "Le Tweet " + status.getText( ) + " a été envoyé depuis " + status.getUser( ).getScreenName( ) + "\n" );
            }

        }
        catch( TwitterException e )
        {

            ReleaserUtils.addInfoError( commandResult, "Une erreur est surnvenue lors de l'envoi du tweet:" + strTweet, e );
        }

    }

    /**
     * Configure la lib tweeter.
     */
    public void init( )
    {
        Properties props = new Properties( );
        props.put( TWITTER_KEY_OAUTH_CONSUMER_KEY, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_TWITTER_OAUTH_CONSUMER_KEY ) );
        props.put( TWITTER_KEY_OAUTH_CONSUMER_SECRET, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_TWITTER_OAUTH_CONSUMER_SECRET ) );
        props.put( TWITTER_KEY_OAUTH_ACCESS_TOKEN, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_TWITTER_OAUTH_ACCESS_TOKEN ) );
        props.put( TWITTER_KEY_OAUTH_ACCESS_TOKEN_SECRET, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_TWITTER_OAUTH_ACCESS_TOKEN_SECRET ) );
        props.put( TWITTER_KEY_OAUTH_REQUEST_TOKEN_URL, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_TWITTER_OAUTH_REQUEST_TOKEN_URL ) );
        props.put( TWITTER_KEY_OAUTH_AUTHORIZATION_URL, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_TWITTER_OAUTH_AUTHORIZATION_URL ) );
        props.put( TWITTER_KEY_OAUTH_ACCESS_TOKEN_URL, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_TWITTER_OAUTH_ACCESSTOKEN_URL ) );
        props.put( TWITTER_KEY_HTTP_PROXY_HOST, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PROXY_HOST ) );
        props.put( TWITTER_KEY_HTTP_PROXY_PORT, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_PROXY_PORT ) );

        Configuration conf = new PropertyConfiguration( props );
        Authorization auth = AuthorizationFactory.getInstance( conf );

        _twitter = new TwitterFactory( conf ).getInstance( auth );

    }

}
