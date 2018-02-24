package parrotwings.com.parrotwings.PWUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by roman on 21.02.2018.
 */

public class PWParser implements PWConnection.PWConnectionInterface {
	public interface PWParserInterface {
		void onResponseRegister(PWError result);
		void onResponseLogin(PWError result);
		void onResponseInfo(PWError result);
		void onResponseList(PWError result);
		void onResponseTransaction(PWError result);
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

	private	static final int		REQUEST_NONE		= PWConnection.INVALID_REQUEST;
	private	static final int		REQUEST_REGISTER	= REQUEST_NONE + 1;
	private	static final int		REQUEST_LOGIN		= REQUEST_REGISTER + 1;
	private	static final int		REQUEST_LIST		= REQUEST_LOGIN + 1;
	private	static final int		REQUEST_TRANSACTION	= REQUEST_LIST + 1;
	private	static final int		REQUEST_INFO		= REQUEST_TRANSACTION + 1;
	private	static final int		REQUEST_FILTER		= REQUEST_INFO + 1;

	private List<PWParserInterface>		mListeners;
	private PWConnection				mConnection;

	@Override
	public void onRecv(int request, PWError result) {
		//PWLog.verbose("pwparser recv result code " + result.getCode() + " description " + result.getDescription() + " request " + request);

		switch (request) {
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
	}

	public void addListener(PWParserInterface listener) {
		mListeners.add(listener);
	}

	public void removeListener(PWParserInterface listener) {
		mListeners.remove(listener);
	}

	public PWParser() {
		mListeners = new LinkedList<>();
		mConnection = new PWConnection(API_BASE_URL);
		mConnection.addListener(this);
	}

	public int register(PWUser user) {
		JSONObject payload = new JSONObject();
		try {
			payload.put("username", user.getName());
			payload.put("password", user.getPassword());
			payload.put("email", user.getEmail());
		} catch (Exception e) {
			PWLog.error("pwparser register failed on payload.put");
			return -1;
		}

		JSONObject header = new JSONObject();
		try {
			header.put(CONTENT_TYPE, APPLICATION_JSON);
		} catch (Exception e) {
			PWLog.error("pwparser register failed on header.put");
			return -1;
		}

		JSONObject json = new JSONObject();
		try {
			json.put(PWConnection.OBJECT_REQUEST, REQUEST_REGISTER);
			json.put(PWConnection.OBJECT_TYPE, PWConnection.TYPE_POST);
			json.put(PWConnection.OBJECT_URL, API_REGISTER);
			json.put(PWConnection.OBJECT_PAYLOAD, payload);
			json.put(PWConnection.OBJECT_HEADER, header);
		} catch (Exception e) {
			PWLog.error("pwparser register failed on json.put");
			return -1;
		}

		return mConnection.send(json);
	}

	public int login(PWUser user) {
		JSONObject payload = new JSONObject();
		try {
			payload.put("email", user.getEmail());
			payload.put("password", user.getPassword());
		} catch (Exception e) {
			PWLog.error("pwparser login failed on payload.put");
			return -1;
		}

		JSONObject header = new JSONObject();
		try {
			header.put(CONTENT_TYPE, APPLICATION_JSON);
		} catch (Exception e) {
			PWLog.error("pwparser login failed on header.put");
			return -1;
		}

		JSONObject json = new JSONObject();
		try {
			json.put(PWConnection.OBJECT_REQUEST, REQUEST_LOGIN);
			json.put(PWConnection.OBJECT_TYPE, PWConnection.TYPE_POST);
			json.put(PWConnection.OBJECT_URL, API_LOGIN);
			json.put(PWConnection.OBJECT_PAYLOAD, payload);
			json.put(PWConnection.OBJECT_HEADER, header);
		} catch (Exception e) {
			PWLog.error("pwparser login failed on json.put");
			return -1;
		}

		return mConnection.send(json);
	}

	public int info(PWUser user) {
		JSONObject header = new JSONObject();
		try {
			header.put(AUTHORIZATION, BEARER + " " + user.getToken());
		} catch (Exception e) {
			PWLog.error("pwparser info failed on header.put");
			return -1;
		}

		JSONObject json = new JSONObject();
		try {
			json.put(PWConnection.OBJECT_REQUEST, REQUEST_INFO);
			json.put(PWConnection.OBJECT_TYPE, PWConnection.TYPE_GET);
			json.put(PWConnection.OBJECT_URL, API_INFO);
			json.put(PWConnection.OBJECT_HEADER, header);
		} catch (Exception e) {
			PWLog.error("pwparser info failed on json.put");
			return -1;
		}

		return mConnection.send(json);
	}

	public int list(PWUser user) {
		JSONObject header = new JSONObject();
		try {
			header.put(AUTHORIZATION, BEARER + " " + user.getToken());
		} catch (Exception e) {
			PWLog.error("pwparser list failed on header.put");
			return -1;
		}

		JSONObject json = new JSONObject();
		try {
			json.put(PWConnection.OBJECT_REQUEST, REQUEST_LIST);
			json.put(PWConnection.OBJECT_TYPE, PWConnection.TYPE_GET);
			json.put(PWConnection.OBJECT_URL, API_LIST);
			json.put(PWConnection.OBJECT_HEADER, header);
		} catch (Exception e) {
			PWLog.error("pwparser list failed on json.put");
			return -1;
		}

		return mConnection.send(json);
	}

	public int transaction(PWUser user, String name, long amount) {
		JSONObject payload = new JSONObject();
		try {
			payload.put("name", name);
			payload.put("amount", amount);
		} catch (Exception e) {
			PWLog.error("pwparser transaction failed on payload.put");
			return -1;
		}

		JSONObject header = new JSONObject();
		try {
			header.put(CONTENT_TYPE, APPLICATION_JSON);
			header.put(AUTHORIZATION, BEARER + " " + user.getToken());
		} catch (Exception e) {
			PWLog.error("pwparser transaction failed on header.put");
			return -1;
		}

		JSONObject json = new JSONObject();
		try {
			json.put(PWConnection.OBJECT_REQUEST, REQUEST_TRANSACTION);
			json.put(PWConnection.OBJECT_TYPE, PWConnection.TYPE_POST);
			json.put(PWConnection.OBJECT_URL, API_TRANSACTION);
			json.put(PWConnection.OBJECT_PAYLOAD, payload);
			json.put(PWConnection.OBJECT_HEADER, header);
		} catch (Exception e) {
			PWLog.error("pwparser transaction failed on json.put");
			return -1;
		}

		return mConnection.send(json);
	}

	public void logout() {}
}
