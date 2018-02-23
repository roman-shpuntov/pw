package parrotwings.com.parrotwings;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import parrotwings.com.parrotwings.PWUtil.PWGradientTextView;

public class LogoActivity extends AppCompatActivity {
	private Timer				mTimer;
	private PWGradientTextView	mName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_logo);

		mName = findViewById(R.id.logo_name);
	}

	@Override
	protected void onResume() {
		mTimer = new Timer();
		mTimer.schedule(new closeActivityTask(), 10000);

		super.onResume();
	}

	class closeActivityTask extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTimer.cancel();

					Intent intent = new Intent(LogoActivity.this, LoginActivity.class);
					startActivity(intent);
					finish();
				}
			});
		}
	}
}
