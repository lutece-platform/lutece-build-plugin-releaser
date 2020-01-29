package fr.paris.lutece.plugins.releaser.service;

import fr.paris.lutece.plugins.releaser.util.CommandResult;

/**
 * The Interface ITwitterService.
 */
public interface ITwitterService
{

    /**
     * Send tweet.
     *
     * @param strTweet the str tweet
     * @param commandResult the command result
     */
    void sendTweet( String strTweet, CommandResult commandResult );

    /**
     * Inits the.
     */
    void init( );
}
