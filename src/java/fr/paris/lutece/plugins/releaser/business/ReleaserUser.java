package fr.paris.lutece.plugins.releaser.business;

import java.io.Serializable;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * 
 * Credentials used by releaser
 *
 */
public class ReleaserUser implements Serializable 
{

    
	private static final long serialVersionUID = -5957364434700297424L;
	public static enum CREDENTIAL_TYPE {GITHUB,GITLAB,SVN};
    private HashMap<CREDENTIAL_TYPE, Credential> _mapCredential;
    public ReleaserUser( ) {
		
		this._mapCredential = new HashMap<>();
	}
    public void addCredential(CREDENTIAL_TYPE type,Credential credential)
    {
       	
    	_mapCredential.put(type, credential);
    }
    
    public Credential getCredential(CREDENTIAL_TYPE type)
    {
       return	_mapCredential.get(type);
    }
    
    public class Credential
    {
    	public Credential(String strLogin, String strPassword) {
			super();
			this._strLogin = strLogin;
			this._strPassword = strPassword;
		}
		private String _strLogin;
    	private String _strPassword;
		
    	public String getLogin() {
			return _strLogin;
		}
		public String getPassword() {
			return _strPassword;
		}
		
	}
    

}
