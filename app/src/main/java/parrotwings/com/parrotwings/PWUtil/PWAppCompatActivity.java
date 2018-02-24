package parrotwings.com.parrotwings.PWUtil;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import parrotwings.com.parrotwings.R;

/**
 * Created by roman on 23.02.2018.
 */

public class PWAppCompatActivity extends AppCompatActivity {
	@Override
	protected void onResume() {
		super.onResume();
		updateBar();
	}

	protected void updateBar() {
		ActionBar bar = getSupportActionBar();
		if (bar != null) {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			Bitmap bitmap = PWGradient.bitmapGradient((int) (displayMetrics.widthPixels * displayMetrics.density), 1,
					getResources().getColor(R.color.colorGradientStart), getResources().getColor(R.color.colorGradientEnd));
			BitmapDrawable background = new BitmapDrawable(getResources(), bitmap);
			bar.setBackgroundDrawable(background);
		}
	}
}
