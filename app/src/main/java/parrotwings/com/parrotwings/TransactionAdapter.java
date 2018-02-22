package parrotwings.com.parrotwings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by roman on 22.02.2018.
 */

public class TransactionAdapter extends ArrayAdapter<String> {
	private static class ViewHolder {
		TextView	user;
	}

	public TransactionAdapter(Context context, List<String> users) {
		super(context, R.layout.cell_trans, users);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		String		user = getItem(position);
		ViewHolder	viewHolder;

		if (view == null) {
			LayoutInflater inflater	= LayoutInflater.from(getContext());

			viewHolder			= new ViewHolder();
			view				= inflater.inflate(R.layout.cell_trans, parent, false);
			viewHolder.user		= view.findViewById(R.id.tcell_user);

			view.setTag(viewHolder);
		} else
			viewHolder = (ViewHolder) view.getTag();

		viewHolder.user.setText(user);

		return view;
	}
}
