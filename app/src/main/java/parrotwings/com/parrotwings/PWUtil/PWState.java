package parrotwings.com.parrotwings.PWUtil;

import android.view.MotionEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by roman on 21.02.2018.
 */

public class PWState implements PWParser.PWParserInterface {
	public interface PWStateInterface {
		void onReady();
		void onError(PWError error);
		void onMessage(PWError error);
		void onInTransaction(PWTransaction trans);
		void onOutTransaction(PWTransaction trans);
	}

	private	static final int	STATE_NONE				= 0;
	private	static final int	STATE_LOGGEDIN			= 1;
	private	static final int	STATE_REGISTERED		= 2;
	private	static final int	STATE_LIST				= 3;
	private	static final int	STATE_READY				= 4;

	private	static final String	REGISTER_EXIST_ERROR	= "A user with that email already exists";
	private	static final String	REGISTER_NO_INFO_ERROR	= "You must send username and password";

	private	static final String	LOGIN_NO_INFO_ERROR		= "You must send email and password.";
	private	static final String	LOGIN_EMAIL_PWD_ERROR	= "Invalid email or password.";

	private static volatile PWState		mInstance;
	private List<PWStateInterface>		mListeners;
	private PWUser						mUser;
	private int							mOldState;
	private int							mNewState;
	private Timer						mTimer;
	private List<PWTransaction>			mOutTransList;
	private List<PWTransaction>			mInTransList;
	private List<PWError>				mErrors;
	private List<PWError>				mMessages;
	private PWParser					mParser;

	private PWError generalErrorWihError(PWError error) {
		return new PWError(error.getCode(), PWError.GENERAL_ERROR_DESC + " System message: " + error.getDescription());
	}

	@Override
	public void onResponseRegister(PWError result) {
		if (!result.isSuccess()) {
			if (!result.isHTTPSuccess()) {
				String	s = result.getDescription();
				if (s.compareTo(REGISTER_EXIST_ERROR) == 0)
					mErrors.add(new PWError(result.getCode(), s + ". Please provide another email."));
				else if (s.compareTo(REGISTER_NO_INFO_ERROR) == 0)
					mErrors.add(new PWError(result.getCode(), "Please provide another username, email and password."));
				else
					mErrors.add(generalErrorWihError(result));

				return;
			}

			mErrors.add(result);
			return;
		}

		if (extractToken(result.getDescription()) == 0) {
			mNewState = STATE_REGISTERED;
			return;
		}

		mErrors.add(new PWError());
	}

	@Override
	public void onResponseLogin(PWError result) {
		if (!result.isSuccess()) {
			if (!result.isHTTPSuccess()) {
				String	s = result.getDescription();
				if (s.compareTo(LOGIN_NO_INFO_ERROR) == 0)
					mErrors.add(new PWError(result.getCode(), "Please provide email and password."));
				else if (s.compareTo(LOGIN_EMAIL_PWD_ERROR) == 0)
					mErrors.add(new PWError(result.getCode(), "Please provide correct email and password."));
				else
					mErrors.add(generalErrorWihError(result));

				return;
			}

			mErrors.add(result);
			return;
		}

		if (extractToken(result.getDescription()) == 0) {
			mNewState = STATE_LOGGEDIN;
			return;
		}

		mErrors.add(new PWError());
	}

	@Override
	public void onResponseInfo(PWError result) {
		if (!result.isSuccess()) {
			mErrors.add(generalErrorWihError(result));
			return;
		}

		if (extractInfo(result.getDescription()) == 0) {
			mNewState = STATE_LIST;
			return;
		}

		mErrors.add(new PWError());
	}

	@Override
	public void onResponseList(PWError result) {
		if (!result.isSuccess()) {
			mErrors.add(generalErrorWihError(result));
			return;
		}

		if (extractList(result.getDescription()) == 0) {
			mNewState = STATE_READY;
			return;
		}

		mErrors.add(new PWError());
	}

	@Override
	public void onResponseTransaction(PWError result) {
		if (!result.isSuccess()) {
			if (!result.isHTTPSuccess())
				mMessages.add(new PWError(result.getCode(), "System message: " + result.getDescription()));
			else
				mErrors.add(generalErrorWihError(result));
			return;
		}

		if (extractTransaction(result.getDescription()) == 0)
			return;

		mErrors.add(new PWError());
	}

	private int extractToken(String result) {
		String token = null;

		try {
			JSONObject object = new JSONObject(result);
			token = object.getString(PWParser.API_TOKEN);
		}
		catch (Exception e) {
			PWLog.error("pwstate failed on extractToken json");
			return -1;
		}

		if (token == null) {
			return -1;
		}

		mUser.setToken(token);
		return 0;
	}

	private int extractInfo(String result) {
		long balance	= 0;
		String name		= null;
		String email	= null;

		try {
			JSONObject object = new JSONObject(result);
			JSONObject user = object.getJSONObject(PWParser.API_INFO_USER);
			name = user.getString(PWParser.API_INFO_NAME);
			balance = user.getLong(PWParser.API_INFO_BALANCE);
			email = user.getString(PWParser.API_INFO_EMAIL);
		}
		catch (Exception e) {
			PWLog.error("pwstate failed on extractInfo json");
			return -1;
		}

		if (name == null || email == null)
			return -1;

		if (email.compareTo(mUser.getEmail()) != 0)
			return -1;

		mUser.setName(name);
		mUser.setBalance(balance);

		return 0;
	}

	private int extractList(String result) {
		PWTransaction		trans = null;
		List<PWTransaction>	xtrans = new ArrayList<>();

		try {
			JSONObject object = new JSONObject(result);
			JSONArray list = object.getJSONArray(PWParser.API_LIST_TOKEN);
			for (int i=0; i<list.length(); i++) {
				JSONObject	item = list.getJSONObject(i);
				Date		date = new Date(item.getString(PWParser.API_LIST_DATE));

				trans = new PWTransaction(
					item.getLong(PWParser.API_LIST_ID), date,
					item.getLong(PWParser.API_LIST_AMOUNT),
					item.getLong(PWParser.API_LIST_BALANCE),
					item.getString(PWParser.API_LIST_USERNAME));

				xtrans.add(trans);
			}
		}
		catch (Exception e) {
			PWLog.error("pwstate failed on extractList json");
			return -1;
		}

		List<PWTransaction>	itrans = mUser.syncTransactionsAndUsers(xtrans);
		if (mNewState == STATE_READY) {
			if (itrans.size() != 0) {
				mInTransList.addAll(itrans);

				long balance = 0;
				for (PWTransaction t : itrans)
					balance += t.getBalance();

				mUser.setBalance(balance);
			}
		}

		return 0;
	}

	private int extractTransaction(String result) {
		long balance	= 0;
		long amount		= 0;
		long id			= 0;
		String name		= null;
		Date date		= null;

		PWTransaction	trans = null;
		try {
			JSONObject object = new JSONObject(result);
			JSONObject tok = object.getJSONObject(PWParser.API_TRANS_TOKEN);
			date = new Date(tok.getString(PWParser.API_TRANS_DATE));
			name = tok.getString(PWParser.API_TRANS_USERNAME);
			balance = tok.getLong(PWParser.API_TRANS_BALANCE);
			amount = tok.getLong(PWParser.API_TRANS_AMOUNT);
			id = tok.getLong(PWParser.API_TRANS_ID);

			trans = new PWTransaction(id, date, amount, balance, name);
		}
		catch (Exception e) {
			PWLog.error("pwstate failed on extractTransaction json");
			return -1;
		}

		if (trans == null)
			return -1;

		mUser.addTransaction(trans);
		mUser.syncUsers();
		mUser.setBalance(balance);
		mOutTransList.add(trans);

		return 0;
	}

	class ProcessingTask extends TimerTask {
		@Override
		public void run() {
			for (PWError error : mErrors) {
				ListIterator<PWStateInterface> itr = mListeners.listIterator();
				while (itr.hasNext()) {
					PWStateInterface iface = itr.next();
					iface.onError(error);
				}
			}
			mErrors.clear();

			for (PWError message : mMessages) {
				ListIterator<PWStateInterface> itr = mListeners.listIterator();
				while (itr.hasNext()) {
					PWStateInterface iface = itr.next();
					iface.onMessage(message);
				}
			}
			mMessages.clear();

			if (mNewState != mOldState) {
				mOldState = mNewState;

				switch (mNewState) {
					case STATE_LOGGEDIN:
					case STATE_REGISTERED:
						mParser.info(mUser);
						break;

					case STATE_LIST:
						mParser.list(mUser);
						break;

					case STATE_READY:
						ListIterator<PWStateInterface> itr = mListeners.listIterator();
						while (itr.hasNext()) {
							PWStateInterface iface = itr.next();
							iface.onReady();
						}
						break;

					default:
						break;
				}
			}

			if (mNewState == STATE_READY) {
				if (mOutTransList.size() != 0) {
					PWTransaction trans = mOutTransList.get(0);
					mOutTransList.remove(0);

					ListIterator<PWStateInterface> itr = mListeners.listIterator();
					while (itr.hasNext()) {
						PWStateInterface iface = itr.next();
						iface.onOutTransaction(trans);
					}
				}

				if (mInTransList.size() != 0) {
					PWTransaction trans = mInTransList.get(0);
					mInTransList.remove(0);

					ListIterator<PWStateInterface> itr = mListeners.listIterator();
					while (itr.hasNext()) {
						PWStateInterface iface = itr.next();
						iface.onInTransaction(trans);
					}
				}

				mParser.list(mUser);
			}
		}
	}

	private PWState() {
		logout();
		mListeners = new LinkedList<>();
	}

	public static PWState getInstance() {
		if (mInstance == null) {
			synchronized (PWState.class) {
				if (mInstance == null) {
					mInstance = new PWState();
				}
			}
		}

		return mInstance;
	}

	public PWUser getUser() {
		return mUser;
	}

	public void addListener(PWStateInterface listener) {
		mListeners.add(listener);
	}

	public void removeListener(PWStateInterface listener) {
		mListeners.remove(listener);
	}

	public void logout() {
		if (mParser != null) {
			mParser.logout();
			mParser.removeListener(this);
			mParser = null;
		}

		mParser = new PWParser();
		mParser.addListener(this);

		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}

		mErrors		= new ArrayList<>();
		mMessages	= new ArrayList<>();
		mOldState	= STATE_NONE;
		mNewState	= STATE_NONE;
		mUser		= new PWUser();

		mInTransList	= new ArrayList<>();
		mOutTransList	= new ArrayList<>();

		mTimer = new Timer();
		mTimer.schedule(new ProcessingTask(), 1000, 1000);
	}

	public int register(String name, String email, String password) {
		mUser = new PWUser(name, email, password);
		return mParser.register(mUser);
	}

	public int login(String email, String password) {
		mUser = new PWUser(email, password);
		return mParser.login(mUser);
	}

	public int transaction(String name, long amount) {
		return mParser.transaction(mUser, name, amount);
	}
}
