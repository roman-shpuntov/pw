package parrotwings.com.parrotwings;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import parrotwings.com.parrotwings.PWUtil.PWConnection;
import parrotwings.com.parrotwings.PWUtil.PWParser;
import parrotwings.com.parrotwings.PWUtil.PWState;
import parrotwings.com.parrotwings.PWUtil.PWTransaction;
import parrotwings.com.parrotwings.PWUtil.PWUser;

public class MainActivity extends AppCompatActivity implements PWState.PWStateInterface {
	private TextView		mName;
	private TextView		mBalance;
	private ListView		mList;
	private MainAdapter		mAdapter;

	@Override
	public void onReady() {}

	@Override
	public void onError() {}

	@Override
	public void onInTransaction(final PWTransaction trans) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(MainActivity.this,
						"Incoming transaction from user: '" + trans.getUserName() + "'. Now your balance: " + trans.getBalance(),
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();

				updateInfo();
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onOutTransaction(final PWTransaction trans) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(MainActivity.this,
						"Complete transaction for user: '" + trans.getUserName() + "'. Now your balance: " + trans.getBalance(),
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();

				updateInfo();
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	private void updateInfo() {
		mName.setText(PWState.getInstance().getUser().getName());
		mBalance.setText("Balance: " + PWState.getInstance().getUser().getBalance());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mName		= findViewById(R.id.main_user);
		mBalance	= findViewById(R.id.main_balance);
		mList		= findViewById(R.id.main_list);

		PWState.getInstance().getUser().sortTransactionsByDate();
		mAdapter = new MainAdapter(this, PWState.getInstance().getUser().getTransactions());
		mList.setAdapter(mAdapter);

		mAdapter.notifyDataSetChanged();

		PWState.getInstance().addListener(this);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.mitem_trans: {
				Intent intent = new Intent(this, TransactionActivity.class);
				startActivity(intent);
				return true;
			}

			case R.id.mitem_date:
				PWState.getInstance().getUser().sortTransactionsByDate();
				mAdapter.notifyDataSetChanged();
				return true;

			case R.id.mitem_name:
				PWState.getInstance().getUser().sortTransactionsByName();
				mAdapter.notifyDataSetChanged();
				return true;

			case R.id.mitem_amount:
				PWState.getInstance().getUser().sortTransactionsByAmount();
				mAdapter.notifyDataSetChanged();
				return true;

			case R.id.mitem_logout: {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				finish();
				return true;
			}

			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}
}
