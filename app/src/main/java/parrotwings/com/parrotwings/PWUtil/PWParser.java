package parrotwings.com.parrotwings.PWUtil;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Created by roman on 21.02.2018.
 */

public class PWParser implements PWConnection.PWConnectionInterface {
	private	static final String		BASE_URL			= "http://193.124.114.46:3001";
	private	static final String		REGISTER			= "/users";
	private	static final String		LOGIN				= "/sessions/create";
	private	static final String		LIST				= "/api/protected/transactions";
	private	static final String		TRANSACTION			= "/api/protected/transactions";
	private	static final String		INFO				= "/api/protected/user-info";
	private	static final String		FILTER				= "/api/protected/users/list";
	private	static final String		APPLICATION_JSON	= "application/json";

	private static volatile PWParser	mInstance;
	private PWConnection				mConnection;

	@Override
	public void pwConnectionRecv(String result) {
		PWLog.debug("pwparser recv result " + result);
	}

	private PWParser() {
		mConnection = PWConnection.getInstance();
		mConnection.addListener(this);
	}

	public static PWParser getInstance() {
		if (mInstance == null) {
			synchronized (PWParser.class) {
				if (mInstance == null) {
					mInstance = new PWParser();
				}
			}
		}

		return mInstance;
	}

	public int register(PWUser user) {
		JSONObject json = new JSONObject();
		try {
			json.put("username", user.getName());
			json.put("password", user.getPassword());
			json.put("email", user.getEmail());
		} catch (Exception e) {
			PWLog.error("pwconnection failed on json.put");
			return -1;
		}

		return mConnection.send(PWConnection.TYPE_POST, BASE_URL + REGISTER, json.toString(), "Content-type", "application/json");
	}
}
