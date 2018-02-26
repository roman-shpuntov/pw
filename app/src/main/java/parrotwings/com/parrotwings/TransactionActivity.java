package parrotwings.com.parrotwings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import parrotwings.com.parrotwings.PWUtil.*;

import static java.lang.Math.abs;

public class TransactionActivity extends PWAppCompatActivity implements PWState.PWStateInterface {
	private AutoCompleteTextView	mUserName;
	private EditText				mAmount;
	private ListView				mList;
	private ImageButton				mSend;
	private LinearLayout			mDownBar;
	private TextView				mEmpty;
	private TextView				mBalance;
	private TextView				mUser;
	private TransactionAdapter		mAdapter;
	private List<PWTransaction>		mTransactions;
	private List<String>			mUsers;

	public static final String	INTENT_USER_NAME	= "INTENT_USER_NAME";
	public static final String	INTENT_AMOUNT		= "INTENT_AMOUNT";

	private Comparator<String> mNameComparator = new Comparator<String>() {
		public int compare(String obj1, String obj2) {
			return obj1.compareTo(obj2);
		}
	};

	@Override
	public void onReady() {}

	@Override
	public void onError(PWError error) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		});
	}

	@Override
	public void onMessage(PWError error) {}

	@Override
	public void onInTransaction(final PWTransaction trans) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTransactions.add(trans);
				mEmpty.setVisibility(View.GONE);

				updateInfo();
				updateUsers();
			}
		});
	}

	@Override
	public void onOutTransaction(final PWTransaction trans) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTransactions.add(trans);
				mEmpty.setVisibility(View.GONE);

				updateInfo();
				updateUsers();
			}
		});
	}

	private void updateInfo() {
		mUser.setText(PWState.getInstance().getUser().getName());
		mBalance.setText("Balance: " + PWState.getInstance().getBalance());
	}

	public void hideKeyboard(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	private void updateUsers() {
		ArrayList<String>	users = new ArrayList<>();
		for (PWTransaction t : mTransactions)
			users.add(t.getUserName());

		HashSet<String> hashSet = new HashSet<>();
		hashSet.addAll(users);

		mUsers.clear();
		mUsers.addAll(hashSet);

		Collections.sort(mUsers, mNameComparator);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transaction);

		setSupportActionBar((Toolbar) findViewById(R.id.trans_toolbar));

		ActionBar bar = getSupportActionBar();
		if (bar != null) {
			bar.setDisplayHomeAsUpEnabled(true);
			bar.setDisplayShowHomeEnabled(true);
		}

		mUserName	= findViewById(R.id.trans_username);
		mAmount		= findViewById(R.id.trans_amount);
		mList		= findViewById(R.id.trans_list);
		mSend		= findViewById(R.id.trans_send);
		mDownBar	= findViewById(R.id.trans_bar);
		mEmpty		= findViewById(R.id.trans_empty);
		mBalance	= findViewById(R.id.trans_balance);
		mUser		= findViewById(R.id.trans_user);

		mTransactions = new ArrayList<>(PWState.getInstance().getTransactions());
		mUsers = new ArrayList<>();
		mAdapter = new TransactionAdapter(this, mUsers);
		mList.setAdapter(mAdapter);
		updateUsers();

		DisplayMetrics	displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		Bitmap bitmap = PWGradient.bitmapGradient(
				(int) (displayMetrics.widthPixels * displayMetrics.density), 1,
				getResources().getColor(R.color.colorGradientStart),
				getResources().getColor(R.color.colorGradientEnd));
		mDownBar.setBackground(new BitmapDrawable(getResources(), bitmap));

		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				String user = mAdapter.getItem(i);
				mUserName.setText(user);
			}
		});

		mSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideKeyboard(view);

				String	userName	= mUserName.getText().toString();
				String	samount		= mAmount.getText().toString();
				long	amount		= 0;

				try {
					amount = Long.parseLong(samount);
				} catch (NumberFormatException e) {
					Toast.makeText(TransactionActivity.this, "Please provide correct amount value.", Toast.LENGTH_LONG).show();
					return;
				}

				long balance = PWState.getInstance().getBalance();
				if (balance < amount) {
					Toast.makeText(TransactionActivity.this, "Insufficient funds on your account. Please change your amount.", Toast.LENGTH_LONG).show();
					return;
				}

				if (amount <= 0) {
					Toast.makeText(TransactionActivity.this, "Please provide correct amount value.", Toast.LENGTH_LONG).show();
					return;
				}

				if (userName.compareTo(PWState.getInstance().getUser().getName()) == 0) {
					Toast.makeText(TransactionActivity.this, "You can not transfer to myself.", Toast.LENGTH_LONG).show();
					return;
				}

				int rc = PWState.getInstance().transaction(userName, amount);
				if (rc != 0) {
					PWLog.error("Transaction failed on transaction");
					Toast.makeText(TransactionActivity.this, "Something wrong on transaction. Please try again later.", Toast.LENGTH_LONG).show();
				}
			}
		});

		Intent intent = getIntent();
		if (intent.getExtras() != null) {
			long amount = intent.getLongExtra(INTENT_AMOUNT, 0);
			amount = abs(amount);
			mAmount.setText(String.valueOf(amount));
			mUserName.setText(intent.getStringExtra(INTENT_USER_NAME));
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mUsers);
		mUserName.setAdapter(adapter);

		if (mUsers.size() == 0)
			mEmpty.setVisibility(View.VISIBLE);
		else
			mEmpty.setVisibility(View.GONE);

		// TODO: sort users

		PWState.getInstance().addListener(this);
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PWState.getInstance().removeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateInfo();
	}
}
