package parrotwings.com.parrotwings.PWUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 21.02.2018.
 */

public class PWUser {
	private	String				mName = "";
	private	String				mPassword = "";
	private	String				mEmail = "";
	private	String				mToken = "";
	private long				mBalance = 0;
	private List<PWTransaction>	mTrans = new ArrayList<>();;

	public PWUser() {}

	public PWUser(String name, String email, String password) {
		mName		= name;
		mPassword	= password;
		mEmail		= email;
	}

	public PWUser(String email, String password) {
		mPassword	= password;
		mEmail		= email;
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

	public void addTransaction(PWTransaction trans) {
		mTrans.add(trans);
	}

	public List<PWTransaction> getTransactions() {
		return mTrans;
	}
}
