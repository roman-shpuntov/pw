package parrotwings.com.parrotwings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import parrotwings.com.parrotwings.PWUtil.*;

import static java.lang.Math.abs;

public class TransactionActivity extends PWAppCompatActivity implements PWState.PWStateInterface {
	private AutoCompleteTextView	mUser;
	private EditText				mAmount;
	private ListView				mList;
	private ImageButton				mSend;
	private ImageView				mDownBar;
	private TextView				mEmpty;
	private TransactionAdapter		mAdapter;

	public static final String	INTENT_USER_NAME	= "INTENT_USER_NAME";
	public static final String	INTENT_AMOUNT		= "INTENT_AMOUNT";

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
	public void onInTransaction(PWTransaction trans) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mEmpty.setVisibility(View.GONE);
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onOutTransaction(PWTransaction trans) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mEmpty.setVisibility(View.GONE);
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	public void hideKeyboard(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

		mUser		= findViewById(R.id.trans_user);
		mAmount		= findViewById(R.id.trans_amount);
		mList		= findViewById(R.id.trans_list);
		mSend		= findViewById(R.id.trans_send);
		mDownBar	= findViewById(R.id.trans_bar);
		mEmpty		= findViewById(R.id.trans_empty);

		mAdapter = new TransactionAdapter(this, PWState.getInstance().getUser().getUserList());
		mList.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();

		DisplayMetrics	displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		Bitmap bitmap = PWGradient.bitmapGradient(
				(int) (displayMetrics.widthPixels * displayMetrics.density),
				(int) (mDownBar.getLayoutParams().height * displayMetrics.density),
				getResources().getColor(R.color.colorGradientStart),
				getResources().getColor(R.color.colorGradientEnd));
		mDownBar.setImageBitmap(bitmap);

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
				hideKeyboard(view);

				String samount = mAmount.getText().toString();
				long amount = 0;

				try {
					amount = Long.parseLong(samount);
				} catch (NumberFormatException e) {
					Toast.makeText(TransactionActivity.this, "Please provide correct amount value.", Toast.LENGTH_LONG).show();
					return;
				}

				long balance = PWState.getInstance().getUser().getBalance();
				if (balance < amount) {
					Toast.makeText(TransactionActivity.this, "Insufficient funds on your account. Please change your amount.", Toast.LENGTH_LONG).show();
					return;
				}

				if (amount <= 0) {
					Toast.makeText(TransactionActivity.this, "Please provide correct amount value.", Toast.LENGTH_LONG).show();
					return;
				}

				int rc = PWState.getInstance().transaction(mUser.getText().toString(), amount);
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
			mUser.setText(intent.getStringExtra(INTENT_USER_NAME));
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, PWState.getInstance().getUser().getUserList());
		mUser.setAdapter(adapter);

		if (PWState.getInstance().getUser().getUserList().size() == 0)
			mEmpty.setVisibility(View.VISIBLE);
		else
			mEmpty.setVisibility(View.GONE);

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
}
