package parrotwings.com.parrotwings;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import parrotwings.com.parrotwings.PWUtil.PWConnection;
import parrotwings.com.parrotwings.PWUtil.PWParser;
import parrotwings.com.parrotwings.PWUtil.PWState;
import parrotwings.com.parrotwings.PWUtil.PWTransaction;
import parrotwings.com.parrotwings.PWUtil.PWUser;

public class MainActivity extends AppCompatActivity {
	private TextView			mName;
	private TextView			mBalance;
	private ListView			mList;
	private MainAdapter			mAdapter;

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
	}

	@Override
	protected void onResume() {
		super.onResume();

		mName.setText(PWState.getInstance().getUser().getName());
		mBalance.setText("Balance: " + PWState.getInstance().getUser().getBalance());
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
