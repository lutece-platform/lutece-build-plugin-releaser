package fr.paris.lutece.plugins.releaser.business;

import java.io.Serializable;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

/**
 * 
 * Credentials used by releaser
 *
 */
@JsonAutoDetect( fieldVisibility = Visibility.ANY )
public class ReleaserUser implements Serializable
{

    private static final long serialVersionUID = -5957364434700297424L;
    private HashMap<RepositoryType, Credential> _mapCredential;

    /**
     * 
     */
    public ReleaserUser( )
    {

        this._mapCredential = new HashMap<>( );
    }

    /**
     * @param type
     * @param credential
     */
    public void addCredential( RepositoryType type, Credential credential )
    {

        _mapCredential.put( type, credential );
    }

    /**
     * @param type
     * @return credential
     */
    public Credential getCredential( RepositoryType type )
    {
        return _mapCredential.get( type );
    }

    /**
     * @author merlinfe
     *
     */
    public class Credential
    {
        /**
         * @param strLogin
         * @param strPassword
         */
        public Credential( String strLogin, String strPassword )
        {
            super( );
            this._strLogin = strLogin;
            this._strPassword = strPassword;
        }

        private String _strLogin;
        private String _strPassword;

        /**
         * @return login
         */
        public String getLogin( )
        {
            return _strLogin;
        }

        /**
         * @return password
         */
        public String getPassword( )
        {
            return _strPassword;
        }

    }

}
