package parrotwings.com.parrotwings.PWUtil;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by roman on 21.02.2018.
 */

public class PWParser implements PWConnection.PWConnectionInterface {
	public interface PWParserInterface {
		void onResponseRegister(String result);
		void onResponseLogin(String result);
		void onResponseInfo(String result);
		void onResponseList(String result);
		void onResponseTransaction(String result);
	}

	public static final String		API_TOKEN			= "id_token";

	public static final String		API_INFO_USER		= "user_info_token";
	public static final String		API_INFO_NAME		= "name";
	public static final String		API_INFO_EMAIL		= "email";
	public static final String		API_INFO_BALANCE	= "balance";

	public static final String		API_LIST_TOKEN		= "trans_token";
	public static final String		API_LIST_ID			= "id";
	public static final String		API_LIST_DATE		= "date";
	public static final String		API_LIST_USERNAME	= "username";
	public static final String		API_LIST_AMOUNT		= "amount";
	public static final String		API_LIST_BALANCE	= "balance";

	public static final String		API_TRANS_TOKEN		= "trans_token";
	public static final String		API_TRANS_ID		= "id";
	public static final String		API_TRANS_DATE		= "date";
	public static final String		API_TRANS_USERNAME	= "username";
	public static final String		API_TRANS_AMOUNT	= "amount";
	public static final String		API_TRANS_BALANCE	= "balance";

	private	static final String		API_BASE_URL		= "http://193.124.114.46:3001";
	private	static final String		API_REGISTER		= "/users";
	private	static final String		API_LOGIN			= "/sessions/create";
	private	static final String		API_LIST			= "/api/protected/transactions";
	private	static final String		API_TRANSACTION		= "/api/protected/transactions";
	private	static final String		API_INFO			= "/api/protected/user-info";
	private	static final String		API_FILTER			= "/api/protected/users/list";

	private	static final String		APPLICATION_JSON	= "application/json";
	private	static final String		CONTENT_TYPE		= "Content-type";
	private	static final String		AUTHORIZATION		= "Authorization";
	private	static final String		BEARER				= "Bearer";

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
					iface.onResponseRegister(result);
				}
				break;
			}

			case REQUEST_LOGIN: {
				ListIterator<PWParserInterface> itr = mListeners.listIterator();
				while (itr.hasNext()) {
					PWParserInterface iface = itr.next();
					iface.onResponseLogin(result);
				}
				break;
			}

			case REQUEST_LIST: {
				ListIterator<PWParserInterface> itr = mListeners.listIterator();
				while (itr.hasNext()) {
					PWParserInterface iface = itr.next();
					iface.onResponseList(result);
				}
				break;
			}

			case REQUEST_TRANSACTION: {
				ListIterator<PWParserInterface> itr = mListeners.listIterator();
				while (itr.hasNext()) {
					PWParserInterface iface = itr.next();
					iface.onResponseTransaction(result);
				}
				break;
			}

			case REQUEST_INFO: {
				ListIterator<PWParserInterface> itr = mListeners.listIterator();
				while (itr.hasNext()) {
					PWParserInterface iface = itr.next();
					iface.onResponseInfo(result);
				}
				break;
			}

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
		mListeners = new LinkedList<>();
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
		int rc = PWConnection.getInstance().send(PWConnection.TYPE_POST, API_BASE_URL + API_REGISTER,
				json.toString(), CONTENT_TYPE, APPLICATION_JSON);
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
		int rc = PWConnection.getInstance().send(PWConnection.TYPE_POST, API_BASE_URL + API_LOGIN,
				json.toString(), CONTENT_TYPE, APPLICATION_JSON);
		if (rc != 0)
			mRequest = REQUEST_NONE;

		return rc;
	}

	public int info(PWUser user) {
		if (isBusy())
			return -1;

		mRequest = REQUEST_INFO;
		int rc = PWConnection.getInstance().send(PWConnection.TYPE_GET, API_BASE_URL + API_INFO,
				"", AUTHORIZATION, BEARER + " " + user.getToken());
		if (rc != 0)
			mRequest = REQUEST_NONE;

		return rc;
	}

	public int list(PWUser user) {
		if (isBusy())
			return -1;

		mRequest = REQUEST_LIST;
		int rc = PWConnection.getInstance().send(PWConnection.TYPE_GET, API_BASE_URL + API_LIST,
				"", AUTHORIZATION, BEARER + " " + user.getToken());
		if (rc != 0)
			mRequest = REQUEST_NONE;

		return rc;
	}

	public int transaction(PWUser user, String name, long amount) {
		if (isBusy())
			return -1;

		JSONObject json = new JSONObject();
		try {
			json.put("name", name);
			json.put("amount", amount);
		} catch (Exception e) {
			PWLog.error("pwparser transaction failed on json.put");
			return -1;
		}

		mRequest = REQUEST_TRANSACTION;
		int rc = PWConnection.getInstance().send(PWConnection.TYPE_POST, API_BASE_URL + API_TRANSACTION,
				json.toString(), CONTENT_TYPE, APPLICATION_JSON, AUTHORIZATION, BEARER + " " + user.getToken());
		if (rc != 0)
			mRequest = REQUEST_NONE;

		return rc;
	}
}
