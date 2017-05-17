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

public class TwitterService implements ITwitterService
{

    
    
    private static final String TWITTER_KEY_OAUTH_CONSUMER_KEY="oauth.consumerKey";
    private static final String TWITTER_KEY_OAUTH_CONSUMER_SECRET="oauth.consumerSecret";
    private static final String TWITTER_KEY_OAUTH_ACCESS_TOKEN="oauth.accessToken";
    private static final String TWITTER_KEY_OAUTH_ACCESS_TOKEN_SECRET="oauth.accessTokenSecret";
    private static final String TWITTER_KEY_OAUTH_ACCESS_TOKEN_URL="oauth.accessTokenURL";
    private static final String TWITTER_KEY_OAUTH_REQUEST_TOKEN_URL="oauth.requestTokenURL";
    private static final String TWITTER_KEY_OAUTH_AUTHORIZATION_URL="oauth.authorizationURL";

    private static final String TWITTER_KEY_HTTP_PROXY_HOST=" http.proxyHost";
    private static final String TWITTER_KEY_HTTP_PROXY_PORT=" http.proxyPort";
    
    
    private static Twitter _twitter = null;
    private static ITwitterService _instance = null;
            
    public static ITwitterService getService()
    {
        if(_instance==null)
        {
            _instance=SpringContextService.getBean(ConstanteUtils.BEAN_TWITTER_SERVICE );
            _instance.init( );
        }
            
      return _instance;
    }
    
    
           
        
    
    public TwitterService( )
    {
        // TODO Auto-generated constructor stub
    }

 
    public void sendTweet(String strTweet, CommandResult commandResult )
    {
        
        try
        {
            if(!StringUtils.isEmpty( strTweet ))
            {
                Status status = _twitter.updateStatus( strTweet );
                commandResult.getLog( ).append("Le Tweet " + status.getText(  ) + " a été envoyé depuis " + status.getUser(  ).getScreenName(  ) + "\n");
            }
            else
            {
                ReleaserUtils.addTechnicalError( commandResult, "Le tweet n'a pas été envoyé car il est vide ");
            }
            
        }
        catch ( TwitterException e )
        {
        
                ReleaserUtils.addTechnicalError( commandResult, "Une erreur est surnvenue lors de l'envoi du tweet:"+strTweet, e );
         }
        
    }
    
    
    /**
     * Configure la lib tweeter
     * @param config la config
     */
    public void init( )
    {
        Properties props = new Properties(  );
        props.put( TWITTER_KEY_OAUTH_CONSUMER_KEY, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_TWITTER_OAUTH_CONSUMER_KEY ));
        props.put( TWITTER_KEY_OAUTH_CONSUMER_SECRET, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_TWITTER_OAUTH_CONSUMER_SECRET ) );
        props.put( TWITTER_KEY_OAUTH_ACCESS_TOKEN, AppPropertiesService.getProperty(  ConstanteUtils.PROPERTY_TWITTER_OAUTH_ACCESS_TOKEN ) );
        props.put( TWITTER_KEY_OAUTH_ACCESS_TOKEN_SECRET, AppPropertiesService.getProperty( ConstanteUtils.PROPERTY_TWITTER_OAUTH_ACCESS_TOKEN_SECRET  ) );
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
