package parrotwings.com.parrotwings;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import parrotwings.com.parrotwings.PWUtil.*;

public class TransactionActivity extends AppCompatActivity {
	private EditText			mUser;
	private EditText			mAmount;
	private ListView			mList;
	private Button				mSend;
	private TransactionAdapter	mAdapter;

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
					Toast.makeText(TransactionActivity.this, "Insufficient funds on your account. Please change your amount and try again", Toast.LENGTH_LONG).show();
					return;
				}

				if (amount < 0) {
					Toast.makeText(TransactionActivity.this, "Please provide correct amount value.", Toast.LENGTH_LONG).show();
					return;
				}

				int rc = PWState.getInstance().transaction(mUser.getText().toString(), amount);
				if (rc != 0)
					Toast.makeText(TransactionActivity.this, "Something wrong on transaction. Please try again later", Toast.LENGTH_LONG).show();
			}
		});
	}
}
