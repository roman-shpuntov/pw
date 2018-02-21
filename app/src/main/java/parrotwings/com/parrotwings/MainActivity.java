package parrotwings.com.parrotwings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import parrotwings.com.parrotwings.PWUtil.PWConnection;
import parrotwings.com.parrotwings.PWUtil.PWParser;
import parrotwings.com.parrotwings.PWUtil.PWState;
import parrotwings.com.parrotwings.PWUtil.PWTransaction;
import parrotwings.com.parrotwings.PWUtil.PWUser;

public class MainActivity extends AppCompatActivity {
	private TextView					mName;
	private TextView					mBalance;
	private ListView					mList;
	private MainAdapter					mAdapter;
//	private ArrayList<PWTransaction>	mArrayList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mName		= findViewById(R.id.main_user);
		mBalance	= findViewById(R.id.main_balance);
		mList		= findViewById(R.id.main_list);

		mName.setText(PWState.getInstance().getUser().getName());
		mBalance.setText("Balance: " + PWState.getInstance().getUser().getBalance());

		mAdapter	= new MainAdapter(this, PWState.getInstance().getUser().getTransactions());

		mList.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}
}
