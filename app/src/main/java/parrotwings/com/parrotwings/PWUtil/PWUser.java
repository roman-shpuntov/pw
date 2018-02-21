package parrotwings.com.parrotwings.PWUtil;

/**
 * Created by roman on 21.02.2018.
 */

public class PWUser {
	private	String		mName;
	private	String		mPassword;
	private	String		mEmail;
	private	String		mToken;

	public PWUser() {
		mName		= "";
		mPassword	= "";
		mEmail		= "";
		mToken		= "";
	}

	public PWUser(String name, String password, String email) {
		mName		= name;
		mPassword	= password;
		mEmail		= email;
		mToken		= "";
	}

	public void setToken(String token) {
		mToken = token;
	}

	public String getName() {
		return mName;
	}

	public String getPassword() {
		return mPassword;
	}

	public String getEmail() {
		return mEmail;
	}

	public String getToken() {
		return mToken;
	}
}
