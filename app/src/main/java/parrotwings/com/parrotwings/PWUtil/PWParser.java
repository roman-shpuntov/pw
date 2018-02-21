package parrotwings.com.parrotwings.PWUtil;

import android.util.PrintWriterPrinter;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by roman on 21.02.2018.
 */

public class PWParser implements PWConnection.PWConnectionInterface {
	public interface PWParserInterface {
		void onRegistered();
		void onLoggedin();
		void onIncome();
	}

	private	static final String		API_BASE_URL		= "http://193.124.114.46:3001";
	private	static final String		API_REGISTER		= "/users";
	private	static final String		API_LOGIN			= "/sessions/create";
	private	static final String		API_LIST			= "/api/protected/transactions";
	private	static final String		API_TRANSACTION		= "/api/protected/transactions";
	private	static final String		API_INFO			= "/api/protected/user-info";
	private	static final String		API_FILTER			= "/api/protected/users/list";

	private	static final String		APPLICATION_JSON	= "application/json";
	private	static final String		CONTENT_TYPE		= "Content-type";

	private	static final int		REQUEST_NONE		= 0;
	private	static final int		REQUEST_REGISTER	= 1;
	private	static final int		REQUEST_LOGIN		= 2;
	private	static final int		REQUEST_LIST		= 3;
	private	static final int		REQUEST_TRANSACTION	= 4;
	private	static final int		REQUEST_INFO		= 5;
	private	static final int		REQUEST_FILTER		= 6;

	private static volatile PWParser	mInstance;
	private List<PWParserInterface>		mListeners;
	private int							mRequest;

	@Override
	public void onRecv(String result) {
		PWLog.debug("pwparser recv result " + result + " request " + mRequest);

		switch (mRequest) {
			case REQUEST_REGISTER: {
				ListIterator<PWParserInterface> itr = mListeners.listIterator();
				while (itr.hasNext()) {
					PWParserInterface iface = itr.next();
					iface.onRegistered();
				}
			}
			break;

			case REQUEST_LOGIN: {
				ListIterator<PWParserInterface> itr = mListeners.listIterator();
				while (itr.hasNext()) {
					PWParserInterface iface = itr.next();
					iface.onLoggedin();
				}
			}
			break;

			case REQUEST_LIST:
				break;

			case REQUEST_TRANSACTION:
				break;

			case REQUEST_INFO:
				break;

			case REQUEST_FILTER:
				break;

			default:
				break;
		}

		mRequest = REQUEST_NONE;
	}

	public void addListener(PWParserInterface listener) {
		mListeners.add(listener);
	}

	public void removeListener(PWParserInterface listener) {
		mListeners.remove(listener);
	}

	private PWParser() {
		mRequest = REQUEST_NONE;
		mListeners = new LinkedList<PWParserInterface>();
		PWConnection.getInstance().addListener(this);
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

	private boolean isBusy() {
		return mRequest != REQUEST_NONE;
	}

	public int register(PWUser user) {
		if (isBusy())
			return -1;

		JSONObject json = new JSONObject();
		try {
			json.put("username", user.getName());
			json.put("password", user.getPassword());
			json.put("email", user.getEmail());
		} catch (Exception e) {
			PWLog.error("pwparser register failed on json.put");
			return -1;
		}

		mRequest = REQUEST_REGISTER;
		int rc = PWConnection.getInstance().send(PWConnection.TYPE_POST, API_BASE_URL + API_REGISTER, json.toString(), CONTENT_TYPE, APPLICATION_JSON);
		if (rc != 0)
			mRequest = REQUEST_NONE;

		return rc;
	}

	public int login(PWUser user) {
		if (isBusy())
			return -1;

		JSONObject json = new JSONObject();
		try {
			json.put("email", user.getEmail());
			json.put("password", user.getPassword());
		} catch (Exception e) {
			PWLog.error("pwparser login failed on json.put");
			return -1;
		}

		mRequest = REQUEST_LOGIN;
		int rc = PWConnection.getInstance().send(PWConnection.TYPE_POST, API_BASE_URL + API_LOGIN, json.toString(), CONTENT_TYPE, APPLICATION_JSON);
		if (rc != 0)
			mRequest = REQUEST_NONE;

		return rc;
	}
}
