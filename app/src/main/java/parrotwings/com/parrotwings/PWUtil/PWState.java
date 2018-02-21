package parrotwings.com.parrotwings.PWUtil;

import org.json.JSONObject;

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
		void onIncome();
	}

	private	static final int	STATE_NONE			= 0;
	private	static final int	STATE_LOGGEDIN		= 1;
	private	static final int	STATE_REGISTERED	= 2;
	private	static final int	STATE_READY			= 3;

	private static volatile PWState		mInstance;
	private List<PWStateInterface>		mListeners;
	private PWUser						mUser;
	private int							mOldState;
	private int							mNewState;
	private Timer						mTimer;

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
			mNewState = STATE_READY;
	}

	private int extractToken(String result) {
		String token = null;

		try {
			JSONObject object = new JSONObject(result);
			token = object.getString(PWParser.API_TOKEN);
		}
		catch (Exception e) {
			PWLog.error("pwstate failed on onResponseRegister json");
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
			PWLog.error("pwstate failed on onResponseRegister json");
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

	class ProcessingTask extends TimerTask {
		@Override
		public void run() {
			if (mNewState != mOldState) {
				mOldState = mNewState;

				switch (mNewState) {
					case STATE_LOGGEDIN:
					case STATE_REGISTERED:
						PWParser.getInstance().info(mUser);
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
		}
	}

	private PWState() {
		mOldState = STATE_NONE;
		mNewState = STATE_NONE;
		mUser = new PWUser();
		mListeners = new LinkedList<>();

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
}
