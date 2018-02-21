package parrotwings.com.parrotwings.PWUtil;

/**
 * Created by roman on 21.02.2018.
 */

public class PWUser {
	private	String		mName;
	private	String		mPassword;
	private	String		mEmail;
	private	String		mToken;
	private long		mBalance;

	public PWUser() {
		mName		= "";
		mPassword	= "";
		mEmail		= "";
		mToken		= "";
		mBalance	= 0;
	}

	public PWUser(String name, String email, String password) {
		mName		= name;
		mPassword	= password;
		mEmail		= email;
		mToken		= "";
		mBalance	= 0;
	}

	public PWUser(String email, String password) {
		mName		= "";
		mPassword	= password;
		mEmail		= email;
		mToken		= "";
		mBalance	= 0;
	}

	public void setToken(String token) {
		mToken = token;
	}

	public void setName(String name) {
		mName = name;
	}

	public void setBalance(long balance) {
		mBalance = balance;
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

	public long getBalance() {
		return mBalance;
	}
}
