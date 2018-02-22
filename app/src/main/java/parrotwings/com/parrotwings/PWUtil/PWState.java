package parrotwings.com.parrotwings.PWUtil;

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
		void onError();
		void onInTransaction(PWTransaction trans);
		void onOutTransaction(PWTransaction trans);
	}

	private	static final int	STATE_NONE			= 0;
	private	static final int	STATE_LOGGEDIN		= 1;
	private	static final int	STATE_REGISTERED	= 2;
	private	static final int	STATE_LIST			= 3;
	private	static final int	STATE_READY			= 4;

	private static volatile PWState		mInstance;
	private List<PWStateInterface>		mListeners;
	private PWUser						mUser;
	private int							mOldState;
	private int							mNewState;
	private Timer						mTimer;
	private List<PWTransaction>			mOutTransList;
	private List<PWTransaction>			mInTransList;
	private int							mErrors;

	@Override
	public void onResponseRegister(String result) {
		if (extractToken(result) == 0)
			mNewState = STATE_REGISTERED;
	}

	@Override
	public void onResponseLogin(String result) {
		if (extractToken(result) == 0)
			mNewState = STATE_LOGGEDIN;
	}

	@Override
	public void onResponseInfo(String result) {
		if (extractInfo(result) == 0)
			mNewState = STATE_LIST;
	}

	@Override
	public void onResponseList(String result) {
		if (extractList(result) == 0)
			mNewState = STATE_READY;
	}

	@Override
	public void onResponseTransaction(String result) {
		extractTransaction(result);
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

		if (token == null)
			return -1;

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

		List<PWTransaction>	itrans = mUser.syncTransactions(xtrans);
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

		try {
			JSONObject object = new JSONObject(result);
			JSONObject tok = object.getJSONObject(PWParser.API_TRANS_TOKEN);
			date = new Date(tok.getString(PWParser.API_TRANS_DATE));
			name = tok.getString(PWParser.API_TRANS_USERNAME);
			balance = tok.getLong(PWParser.API_TRANS_BALANCE);
			amount = tok.getLong(PWParser.API_TRANS_AMOUNT);
			id = tok.getLong(PWParser.API_TRANS_ID);

			PWTransaction trans = new PWTransaction(
					id, date, amount, balance, name);

			mOutTransList.add(trans);
		}
		catch (Exception e) {
			PWLog.error("pwstate failed on extractTransaction json");
			return -1;
		}

		if (name == null)
			return -1;

		mUser.setBalance(balance);

		return 0;
	}

	class ProcessingTask extends TimerTask {
		@Override
		public void run() {
			if (mErrors != 0) {
				mErrors = 0;

				ListIterator<PWStateInterface> itr = mListeners.listIterator();
				while (itr.hasNext()) {
					PWStateInterface iface = itr.next();
					iface.onError();
				}
			}

			if (mNewState != mOldState) {
				mOldState = mNewState;

				switch (mNewState) {
					case STATE_LOGGEDIN:
					case STATE_REGISTERED:
						PWParser.getInstance().info(mUser);
						break;

					case STATE_LIST:
						PWParser.getInstance().list(mUser);
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

				PWParser.getInstance().list(mUser);
			}
		}
	}

	private PWState() {
		mErrors = 0;
		mOldState = STATE_NONE;
		mNewState = STATE_NONE;
		mUser = new PWUser();
		mListeners = new LinkedList<>();
		mInTransList = new ArrayList<>();
		mOutTransList = new ArrayList<>();

		PWParser.getInstance().addListener(this);

		mTimer = new Timer();
		mTimer.schedule(new ProcessingTask(), 1000, 1000);
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

	public int register(String name, String email, String password) {
		mUser = new PWUser(name, email, password);
		return PWParser.getInstance().register(mUser);
	}

	public int login(String email, String password) {
		mUser = new PWUser(email, password);
		return PWParser.getInstance().login(mUser);
	}

	public int transaction(String name, long amount) {
		return PWParser.getInstance().transaction(mUser, name, amount);
	}
}
