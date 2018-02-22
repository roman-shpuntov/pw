package parrotwings.com.parrotwings;

import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import parrotwings.com.parrotwings.PWUtil.PWState;
import parrotwings.com.parrotwings.PWUtil.PWTransaction;

public class TransactionActivity extends AppCompatActivity implements PWState.PWStateInterface {
	private EditText			mUser;
	private EditText			mAmount;
	private ListView			mList;
	private Button				mSend;
	private TransactionAdapter	mAdapter;

	@Override
	public void onReady() {}

	@Override
	public void onInTransaction(PWTransaction trans) {

	}

	@Override
	public void onOutTransaction(final PWTransaction trans) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				hideKeyboard(TransactionActivity.this.getWindow().getDecorView().findViewById(android.R.id.content));

				Toast.makeText(TransactionActivity.this,
						"Complete transaction for user: '" + trans.getUserName() + "'. Now your balance: " + trans.getBalance(),
						Toast.LENGTH_LONG).show();
			}
		});
	}

	private void hideKeyboard(View view) {
		InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transaction);

		ActionBar bar = getSupportActionBar();
		if (bar != null) {
			bar.setDisplayHomeAsUpEnabled(true);
			bar.setDisplayShowHomeEnabled(true);
		}

		mUser = findViewById(R.id.trans_user);
		mAmount = findViewById(R.id.trans_amount);
		mList = findViewById(R.id.trans_list);
		mSend = findViewById(R.id.trans_send);

		mAdapter = new TransactionAdapter(this, PWState.getInstance().getUser().getUserList());
		mList.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();

		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				String user = mAdapter.getItem(i);
				mUser.setText(user);
			}
		});

		mSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				long amount = Long.parseLong(mAmount.getText().toString());
				long balance = PWState.getInstance().getUser().getBalance();
				if (balance < amount) {
					Toast.makeText(TransactionActivity.this, "Insufficient funds on your account. Please change your amount and try again", Toast.LENGTH_LONG).show();
					return;
				}

				int rc = PWState.getInstance().transaction(mUser.getText().toString(), amount);
				if (rc != 0)
					Toast.makeText(TransactionActivity.this, "Something wrong on transaction. Please try again later", Toast.LENGTH_LONG).show();
			}
		});

		PWState.getInstance().addListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		PWState.getInstance().removeListener(this);
	}
}
