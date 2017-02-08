package fr.paris.lutece.plugins.releaser.service;

import fr.paris.lutece.plugins.releaser.util.CommandResult;

public interface ITwitterService
{

    void sendTweet(String strTweet, CommandResult commandResult );
    
    void init( );
}
