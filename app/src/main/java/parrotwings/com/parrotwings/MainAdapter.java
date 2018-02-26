package parrotwings.com.parrotwings;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import parrotwings.com.parrotwings.PWUtil.*;

/**
 * Created by roman on 21.02.2018.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.PWViewHolder> {
	View.OnClickListener 	mListener;
	List<PWTransaction>		mTransactions;

	MainAdapter(View.OnClickListener listener, List<PWTransaction> trans) {
		mListener = listener;
		mTransactions = trans;
	}

	public static class PWViewHolder extends RecyclerView.ViewHolder {
		TextView	mUser;
		TextView	mDate;
		TextView	mAmount;
		TextView	mBalance;

		PWViewHolder(View itemView) {
			super(itemView);

			mUser		= itemView.findViewById(R.id.mcell_user);
			mDate		= itemView.findViewById(R.id.mcell_date);
			mAmount		= itemView.findViewById(R.id.mcell_amount);
			mBalance	= itemView.findViewById(R.id.mcell_balance);
		}
	}

	@Override
	public PWViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = null;

		v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_main, parent, false);
		if (v == null)
			return null;

		v.setOnClickListener(mListener);
		return new PWViewHolder(v);
	}

	@Override
	public void onBindViewHolder(PWViewHolder holder, int position) {
		PWTransaction	tr = mTransactions.get(position);

		DateFormat	format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		holder.mUser.setText(tr.getUserName());
		holder.mDate.setText(format.format(tr.getDate()));
		holder.mAmount.setText("Amount: " + String.valueOf(tr.getAmount()));
		holder.mBalance.setText("Balance: " + String.valueOf(tr.getBalance()));

		if (tr.getAmount() > 0)
			holder.mAmount.setTextColor(holder.mAmount.getResources().getColor(android.R.color.holo_green_dark));
		else
			holder.mAmount.setTextColor(holder.mAmount.getResources().getColor(android.R.color.holo_red_dark));
	}

	@Override
	public int getItemCount() {
		return mTransactions.size();
	}
}
