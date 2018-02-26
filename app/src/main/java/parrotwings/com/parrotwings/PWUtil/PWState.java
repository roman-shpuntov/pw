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
import java.util.concurrent.atomic.AtomicInteger;

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

	private class PWXUser extends PWUser {
		private	String						mToken = "";
		private long						mBalance = 0;
		private List<PWTransaction>			mTrans = new ArrayList<>();

		PWXUser() {
			super();
		}

		PWXUser(PWUser user) {
			super(user.getName(), user.getEmail(), user.getPassword());
		}

		public void setToken(String token) {
			mToken = token;
		}

		public void setBalance(long balance) {
			mBalance = balance;
		}

		public String getToken() {
			return mToken;
		}

		public long getBalance() {
			return mBalance;
		}

		public List<PWTransaction> syncTransactions(List<PWTransaction> trans) {
			List<PWTransaction>	itrans = new ArrayList<>();
			long count = 0;

			for (PWTransaction t : trans) {
				long id = t.getID();
				boolean find = false;
				for (PWTransaction etrans : mTrans) {
					if (id == etrans.getID()) {
						find = true;
						count++;
						break;
					}
				}

				if (!find) {
					mTrans.add(t);
					if (t.getAmount() > 0)
						itrans.add(t);
				}
			}

			return itrans;
		}

		public List<PWTransaction> getTransactions() {
			return new ArrayList<>(mTrans);
		}

		public void addTransaction(PWTransaction trans) {
			mTrans.add(trans);
		}
	}

	private	static final int	STATE_NONE				= 0;
	private	static final int	STATE_READY				= 1;

	private	static final String	REGISTER_EXIST_ERROR	= "A user with that email already exists";
	private	static final String	REGISTER_NO_INFO_ERROR	= "You must send username and password";

	private	static final String	LOGIN_NO_INFO_ERROR		= "You must send email and password.";
	private	static final String	LOGIN_EMAIL_PWD_ERROR	= "Invalid email or password.";

	private static volatile PWState		mInstance;
	private List<PWStateInterface>		mListeners;
	private PWXUser						mXUser;
	private int							mState;
	private Timer						mTimer;
	private PWParser					mParser;
	private AtomicInteger				mListCounter;
	private boolean						mReadyNotified;

	private PWError generalErrorWihError(PWError error) {
		return new PWError(error.getCode(), PWError.GENERAL_ERROR_DESC + " System message: " + error.getDescription());
	}

	@Override
	public void onResponseRegister(PWError result) {
		if (!result.isSuccess()) {
			if (!result.isHTTPSuccess()) {
				String	s = result.getDescription();
				if (s.compareTo(REGISTER_EXIST_ERROR) == 0)
					notifyError(new PWError(result.getCode(), s + ". Please provide another email."));
				else if (s.compareTo(REGISTER_NO_INFO_ERROR) == 0)
					notifyError(new PWError(result.getCode(), "Please provide another username, email and password."));
				else
					notifyError(generalErrorWihError(result));

				return;
			}

			notifyError(result);
			return;
		}

		if (extractToken(result.getDescription()) == 0) {
			mParser.info(mXUser, mXUser.getToken());
			return;
		}

		notifyError(new PWError());
	}

	@Override
	public void onResponseLogin(PWError result) {
		if (!result.isSuccess()) {
			if (!result.isHTTPSuccess()) {
				String	s = result.getDescription();
				if (s.compareTo(LOGIN_NO_INFO_ERROR) == 0)
					notifyError(new PWError(result.getCode(), "Please provide email and password."));
				else if (s.compareTo(LOGIN_EMAIL_PWD_ERROR) == 0)
					notifyError(new PWError(result.getCode(), "Please provide correct email and password."));
				else
					notifyError(generalErrorWihError(result));

				return;
			}

			notifyError(result);
			return;
		}

		if (extractToken(result.getDescription()) == 0) {
			PWLog.debug("pwstate onResponseLogin info");
			mParser.info(mXUser, mXUser.getToken());
			return;
		}

		notifyError(new PWError());
	}

	@Override
	public void onResponseInfo(PWError result) {
		if (!result.isSuccess()) {
			notifyError(generalErrorWihError(result));
			return;
		}

		if (extractInfo(result.getDescription()) == 0) {
			PWLog.debug("pwstate onResponseInfo info");

			mListCounter.incrementAndGet();
			mParser.list(mXUser, mXUser.getToken());
			return;
		}

		notifyError(new PWError());
	}

	@Override
	public void onResponseList(PWError result) {
		mListCounter.decrementAndGet();

		if (!result.isSuccess()) {
			notifyError(generalErrorWihError(result));
			return;
		}

		if (extractList(result.getDescription()) == 0) {
			if (!mReadyNotified) {
				mState = STATE_READY;
				notifyOnReady();
				mReadyNotified = true;
			}
			return;
		}

		notifyError(new PWError());
	}

	@Override
	public void onResponseTransaction(PWError result) {
		if (!result.isSuccess()) {
			if (!result.isHTTPSuccess())
				notifyMessage(new PWError(result.getCode(), "System message: " + result.getDescription()));
			else
				notifyError(generalErrorWihError(result));
			return;
		}

		if (extractTransaction(result.getDescription()) == 0)
			return;

		notifyError(new PWError());
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

		mXUser.setToken(token);
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

		if (email.compareTo(mXUser.getEmail()) != 0)
			return -1;

		mXUser.setName(name);
		mXUser.setBalance(balance);

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

		List<PWTransaction>	itrans = mXUser.syncTransactions(xtrans);
		if (mState == STATE_READY) {
			if (itrans.size() != 0) {
				int last = itrans.size() - 1;
				mXUser.setBalance(itrans.get(last).getBalance());
				notifyInTrans(itrans);
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

		mXUser.addTransaction(trans);
		mXUser.setBalance(balance);
		notifyOutTrans(trans);

		return 0;
	}

	private void notifyError(PWError error) {
		List<PWStateInterface> llist = getListeners();

		ListIterator<PWStateInterface> itr = llist.listIterator();
		while (itr.hasNext()) {
			PWStateInterface iface = itr.next();
			iface.onError(error);
		}
	}

	private void notifyMessage(PWError message) {
		List<PWStateInterface> llist = getListeners();

		ListIterator<PWStateInterface> itr = llist.listIterator();
		while (itr.hasNext()) {
			PWStateInterface iface = itr.next();
			iface.onMessage(message);
		}
	}

	private void notifyOutTrans(PWTransaction trans) {
		List<PWStateInterface> llist = getListeners();

		ListIterator<PWStateInterface> itr = llist.listIterator();
		while (itr.hasNext()) {
			PWStateInterface iface = itr.next();
			iface.onOutTransaction(trans);
		}
	}

	private void notifyInTrans(List<PWTransaction> trans) {
		List<PWStateInterface> llist = getListeners();

		for (PWTransaction t : trans) {
			ListIterator<PWStateInterface> itr = llist.listIterator();
			while (itr.hasNext()) {
				PWStateInterface iface = itr.next();
				iface.onInTransaction(t);
			}
		}
	}

	private void notifyOnReady() {
		List<PWStateInterface> llist = getListeners();

		ListIterator<PWStateInterface> itr = llist.listIterator();
		while (itr.hasNext()) {
			PWStateInterface iface = itr.next();
			iface.onReady();
		}
	}

	private List<PWStateInterface> getListeners() {
		List<PWStateInterface> llist;
		synchronized (mListeners) {
			llist = new ArrayList<>(mListeners);
		}

		return llist;
	}

	class ProcessingTask extends TimerTask {
		@Override
		public void run() {
			if (mState == STATE_READY) {
				if (mListCounter.compareAndSet(0, 1)) {
					PWLog.debug("pwstate ProcessingTask list");
					mParser.list(mXUser, mXUser.getToken());
				}
			}
		}
	}

	private PWState() {
		logout();

		mListeners		= new LinkedList<>();
		mReadyNotified	= false;
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
		return mXUser;
	}

	public void addListener(PWStateInterface listener) {
		synchronized(mListeners) {
			mListeners.add(listener);
		}
	}

	public void removeListener(PWStateInterface listener) {
		synchronized(mListeners) {
			mListeners.remove(listener);
		}
	}

	public List<PWTransaction> getTransactions() {
		return mXUser.getTransactions();
	}

	public long getBalance() {
		return mXUser.getBalance();
	}

	public void logout() {
		PWLog.debug("pwstate logout");

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

		mState			= STATE_NONE;
		mXUser			= new PWXUser();
		mReadyNotified	= false;
		mListCounter	= new AtomicInteger(0);

		mTimer = new Timer();
		mTimer.schedule(new ProcessingTask(), 1000, 1000);
	}

	public int register(String name, String email, String password) {
		PWLog.debug("pwstate register");

		mXUser = new PWXUser(new PWUser(name, email, password));
		return mParser.register(mXUser);
	}

	public int login(String email, String password) {
		PWLog.debug("pwstate login");

		mXUser = new PWXUser(new PWUser(email, password));
		return mParser.login(mXUser);
	}

	public int transaction(String name, long amount) {
		PWLog.debug("pwstate transaction");

		return mParser.transaction(mXUser, mXUser.getToken(), name, amount);
	}
}
