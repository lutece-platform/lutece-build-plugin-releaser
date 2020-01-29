package fr.paris.lutece.plugins.releaser.business;

import java.io.Serializable;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

// TODO: Auto-generated Javadoc
/**
 * Credentials used by releaser.
 */
@JsonAutoDetect( fieldVisibility = Visibility.ANY )
public class ReleaserUser implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5957364434700297424L;
    
    /** The map credential. */
    private HashMap<RepositoryType, Credential> _mapCredential;

    /**
     * Instantiates a new releaser user.
     */
    public ReleaserUser( )
    {

        this._mapCredential = new HashMap<>( );
    }

    /**
     * Adds the credential.
     *
     * @param type the type
     * @param credential the credential
     */
    public void addCredential( RepositoryType type, Credential credential )
    {

        _mapCredential.put( type, credential );
    }

    /**
     * Gets the credential.
     *
     * @param type the type
     * @return credential
     */
    public Credential getCredential( RepositoryType type )
    {
        return _mapCredential.get( type );
    }

    /**
     * The Class Credential.
     *
     * @author merlinfe
     */
    public class Credential
    {
        
        /**
         * Instantiates a new credential.
         *
         * @param strLogin the str login
         * @param strPassword the str password
         */
        public Credential( String strLogin, String strPassword )
        {
            super( );
            this._strLogin = strLogin;
            this._strPassword = strPassword;
        }

        /** The str login. */
        private String _strLogin;
        
        /** The str password. */
        private String _strPassword;

        /**
         * Gets the login.
         *
         * @return login
         */
        public String getLogin( )
        {
            return _strLogin;
        }

        /**
         * Gets the password.
         *
         * @return password
         */
        public String getPassword( )
        {
            return _strPassword;
        }

    }

}
