package parrotwings.com.parrotwings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import parrotwings.com.parrotwings.PWUtil.*;

public class MainActivity extends PWAppCompatActivity implements PWState.PWStateInterface, PopupMenu.OnMenuItemClickListener{
	private TextView		mName;
	private TextView		mBalance;
	private ListView		mList;
	private MainAdapter		mAdapter;
	private	PWTransaction	mNewTrans;

	private final int		POPUP_GROUPID			= 1;

	@Override
	public void onReady() {}

	@Override
	public void onError(final PWError error) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this, error.getDescription(), Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onInTransaction(final PWTransaction trans) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this,
					"Incoming transaction from user: '" + trans.getUserName() + "'. Now your balance: " + trans.getBalance(),
					Toast.LENGTH_LONG).show();

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
				Toast.makeText(MainActivity.this,
					"Complete transaction for user: '" + trans.getUserName() + "'. Now your balance: " + trans.getBalance(),
					Toast.LENGTH_LONG).show();

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

		setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

		mName		= findViewById(R.id.main_user);
		mBalance	= findViewById(R.id.main_balance);
		mList		= findViewById(R.id.main_list);

		PWState.getInstance().getUser().sortTransactionsByDate();
		mAdapter = new MainAdapter(this, PWState.getInstance().getUser().getTransactions());
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				mNewTrans = mAdapter.getItem(i);

				PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
				popupMenu.setOnMenuItemClickListener(MainActivity.this);
				popupMenu.getMenu().add(POPUP_GROUPID, R.id.litem_new, 1, "New transaction from...");
				popupMenu.show();
			}
		});

		mAdapter.notifyDataSetChanged();

		PWState.getInstance().addListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		PWState.getInstance().removeListener(this);
		if (isFinishing())
			PWState.getInstance().logout();
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateInfo();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.litem_new && mNewTrans != null) {
			Intent intent = new Intent(this, TransactionActivity.class);
			intent.putExtra(TransactionActivity.INTENT_USER_NAME, mNewTrans.getUserName());
			intent.putExtra(TransactionActivity.INTENT_AMOUNT, mNewTrans.getAmount());
			startActivity(intent);
			return true;
		}

		return false;
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
				item.setChecked(true);
				PWState.getInstance().getUser().sortTransactionsByDate();
				mAdapter.notifyDataSetChanged();
				return true;

			case R.id.mitem_name:
				item.setChecked(true);
				PWState.getInstance().getUser().sortTransactionsByName();
				mAdapter.notifyDataSetChanged();
				return true;

			case R.id.mitem_amount:
				item.setChecked(true);
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
