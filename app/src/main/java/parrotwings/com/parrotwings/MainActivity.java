package parrotwings.com.parrotwings;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import parrotwings.com.parrotwings.PWUtil.*;

public class MainActivity extends PWAppCompatActivity implements PWState.PWStateInterface, PopupMenu.OnMenuItemClickListener{
	private TextView			mName;
	private TextView			mBalance;
	private RecyclerView		mRV;
	private TextView			mEmpty;
	private MainAdapter			mAdapter;
	private	PWTransaction		mNewTrans;
	private List<PWTransaction>	mTransactions;
	private LinearLayout		mDownBar;
	private	static int			mChanging = 0;

	private final int			POPUP_GROUPID	= 1;

	private Comparator<PWTransaction> mDateComparator = new Comparator<PWTransaction>() {
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

	private	Comparator<PWTransaction>	mComparator = mDateComparator;

	@Override
	public void onReady() {}

	@Override
	public void onError(final PWError error) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				PWState.getInstance().logout();

				Intent intent = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	@Override
	public void onMessage(final PWError error) {
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
					Toast.LENGTH_SHORT).show();

				updateInfo();
				mEmpty.setVisibility(View.GONE);

				mTransactions.add(trans);
				sortTransactions();
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
					Toast.LENGTH_SHORT).show();

				updateInfo();
				mEmpty.setVisibility(View.GONE);

				mTransactions.add(trans);
				sortTransactions();
			}
		});
	}

	private void updateInfo() {
		mName.setText(PWState.getInstance().getUser().getName());
		mBalance.setText("Balance: " + PWState.getInstance().getBalance());
	}

	class RecyclerViewOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			int i = mRV.indexOfChild(v);
			mNewTrans = mTransactions.get(i);

			PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
			popupMenu.setOnMenuItemClickListener(MainActivity.this);
			popupMenu.getMenu().add(POPUP_GROUPID, R.id.litem_new, 1, "New transaction from...");
			popupMenu.show();
		}
	}

	Foreground.Listener mListener = new Foreground.Listener() {
		public void onBecameForeground(){}

		public void onBecameBackground(){
			PWState.getInstance().logout();
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

		mName		= findViewById(R.id.main_user);
		mBalance	= findViewById(R.id.main_balance);
		mRV			= findViewById(R.id.main_rv);
		mEmpty		= findViewById(R.id.main_empty);
		mDownBar	= findViewById(R.id.main_bar);

		LinearLayoutManager llm = new LinearLayoutManager(this);
		mRV.setLayoutManager(llm);

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRV.getContext(), llm.getOrientation());
		mRV.addItemDecoration(dividerItemDecoration);

		DisplayMetrics	displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		Bitmap bitmap = PWGradient.bitmapGradient(
			(int) (displayMetrics.widthPixels * displayMetrics.density), 1,
			getResources().getColor(R.color.colorGradientStart),
			getResources().getColor(R.color.colorGradientEnd));
		mDownBar.setBackground(new BitmapDrawable(getResources(), bitmap));

		mTransactions = new ArrayList<>(PWState.getInstance().getTransactions());
		mAdapter = new MainAdapter(new RecyclerViewOnClickListener(), mTransactions);
		mRV.setAdapter(mAdapter);
		sortTransactions();

		if (mTransactions.size() == 0)
			mEmpty.setVisibility(View.VISIBLE);
		else
			mEmpty.setVisibility(View.GONE);

		PWState.getInstance().addListener(this);

		if ((mChanging & ActivityInfo.CONFIG_ORIENTATION) != ActivityInfo.CONFIG_ORIENTATION)
			Toast.makeText(MainActivity.this, "Welcome to " + getResources().getString(R.string.app_name) + " system.", Toast.LENGTH_SHORT).show();

		Foreground.get(getApplication()).addListener(mListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		PWState.getInstance().removeListener(this);
		mChanging = getChangingConfigurations();

		Foreground.get(getApplication()).removeListener(mListener);
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
				sortTransactionsByDate();
				return true;

			case R.id.mitem_name:
				item.setChecked(true);
				sortTransactionsByName();
				return true;

			case R.id.mitem_amount:
				item.setChecked(true);
				sortTransactionsByAmount();
				return true;

			case R.id.mitem_logout: {
				PWState.getInstance().logout();

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

	public void sortTransactions() {
		Collections.sort(mTransactions, mComparator);
		mAdapter.notifyDataSetChanged();
	}

	public void sortTransactionsByDate() {
		mComparator = mDateComparator;
		Collections.sort(mTransactions, mDateComparator);
		mAdapter.notifyDataSetChanged();
	}

	public void sortTransactionsByName() {
		mComparator = mNameComparator;
		Collections.sort(mTransactions, mNameComparator);
		mAdapter.notifyDataSetChanged();
	}

	public void sortTransactionsByAmount() {
		mComparator = mAmountComparator;
		Collections.sort(mTransactions, mAmountComparator);
		mAdapter.notifyDataSetChanged();
	}
}
