package parrotwings.com.parrotwings.PWUtil;

import java.util.Date;

/**
 * Created by roman on 21.02.2018.
 */

public class PWTransaction {
	private long		mID = 0;
	private Date		mDate = new Date();
	private long		mAmount = 0;
	private long		mBalance = 0;
	private String		mUserName = "";

	PWTransaction() {}

	PWTransaction(long id, Date date, long amount, long balance, String userName) {
		mID = id;
		mDate = date;
		mAmount = amount;
		mBalance = balance;
		mUserName = userName;
	}

	public long getID() {
		return mID;
	}

	public String getUserName() {
		return mUserName;
	}

	public Date getDate() {
		return mDate;
	}

	public long getAmount() {
		return mAmount;
	}

	public long getBalance() {
		return mBalance;
	}
}
