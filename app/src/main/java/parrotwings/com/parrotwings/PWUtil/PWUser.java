package parrotwings.com.parrotwings.PWUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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

	private	Comparator<PWTransaction>	mDateComparator = new Comparator<PWTransaction>() {
		public int compare(PWTransaction obj1, PWTransaction obj2) {
			return obj2.getDate().compareTo(obj1.getDate());
		}
	};

	private	Comparator<PWTransaction>	mNameComparator = new Comparator<PWTransaction>() {
		public int compare(PWTransaction obj1, PWTransaction obj2) {
			return obj1.getUserName().compareTo(obj2.getUserName());
		}
	};

	private	Comparator<PWTransaction>	mAmountComparator = new Comparator<PWTransaction>() {
		public int compare(PWTransaction obj1, PWTransaction obj2) {
			if (obj2.getAmount() > obj1.getAmount())
				return 1;
			else if (obj2.getAmount() < obj1.getAmount())
				return -1;

			return 0;
		}
	};

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

	public void sortTransactionsByDate() {
		Collections.sort(mTrans, mDateComparator);
	}

	public void sortTransactionsByName() {
		Collections.sort(mTrans, mNameComparator);
	}

	public void sortTransactionsByAmount() {
		Collections.sort(mTrans, mAmountComparator);
	}

	public List<String> getUserList() {
		ArrayList<String>	users = new ArrayList<>();

		for (PWTransaction trans : mTrans)
			users.add(trans.getUserName());

		HashSet<String> hashSet = new HashSet<String>();
		hashSet.addAll(users);

		users.clear();
		users.addAll(hashSet);

		return users;
	}
}
