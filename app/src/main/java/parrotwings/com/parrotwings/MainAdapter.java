package parrotwings.com.parrotwings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import parrotwings.com.parrotwings.PWUtil.PWTransaction;

/**
 * Created by roman on 21.02.2018.
 */

public class MainAdapter extends ArrayAdapter<PWTransaction> {
	private static class ViewHolder {
		TextView	user;
		TextView	date;
		TextView	amount;
		TextView	balance;
	}

	public MainAdapter(Context context, List<PWTransaction> trans) {
		super(context, R.layout.cell_main, trans);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		PWTransaction	tr = getItem(position);
		ViewHolder		viewHolder;

		if (view == null) {
			LayoutInflater inflater	= LayoutInflater.from(getContext());

			viewHolder			= new ViewHolder();
			view				= inflater.inflate(R.layout.cell_main, parent, false);
			viewHolder.user		= view.findViewById(R.id.mcell_user);
			viewHolder.date		= view.findViewById(R.id.mcell_date);
			viewHolder.amount	= view.findViewById(R.id.mcell_amount);
			viewHolder.balance	= view.findViewById(R.id.mcell_balance);

			view.setTag(viewHolder);
		} else
			viewHolder = (ViewHolder) view.getTag();

		DateFormat	format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		viewHolder.user.setText(tr.getUserName());
		viewHolder.date.setText(format.format(tr.getDate()));
		viewHolder.amount.setText(String.valueOf(tr.getAmount()));
		viewHolder.balance.setText(String.valueOf(tr.getBalance()));

		if (tr.getAmount() > 0)
			viewHolder.amount.setTextColor(view.getResources().getColor(android.R.color.holo_green_dark));
		else
			viewHolder.amount.setTextColor(view.getResources().getColor(android.R.color.holo_red_dark));

		return view;
	}
}
