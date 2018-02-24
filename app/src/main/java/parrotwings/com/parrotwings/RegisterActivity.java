package parrotwings.com.parrotwings;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import parrotwings.com.parrotwings.PWUtil.*;

public class RegisterActivity extends AppCompatActivity implements PWState.PWStateInterface {
	private EditText	mEmail;
	private EditText	mName;
	private EditText	mPassword;
	private Button		mBack;
	private Button		mSignup;
	private ProgressBar mProgress;

	@Override
	public void onReady() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	@Override
	public void onError(final PWError error) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(RegisterActivity.this, error.getDescription(), Toast.LENGTH_LONG).show();
				mProgress.setVisibility(View.INVISIBLE);
				enableUI(true);
			}
		});
	}

	@Override
	public void onInTransaction(PWTransaction trans) {}

	@Override
	public void onOutTransaction(PWTransaction trans) {}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		PWState.getInstance().addListener(this);

		mEmail		= findViewById(R.id.register_email);
		mName		= findViewById(R.id.register_name);
		mPassword	= findViewById(R.id.register_password);
		mProgress	= findViewById(R.id.register_progress);

		mProgress.setVisibility(View.INVISIBLE);

		mBack = findViewById(R.id.register_back);
		mBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		});

		mSignup = findViewById(R.id.register_signup);
		mSignup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int rc = PWState.getInstance().register(mName.getText().toString(), mEmail.getText().toString(), mPassword.getText().toString());
				if (rc != 0) {
					PWLog.error("Register failed on register");
					Toast.makeText(RegisterActivity.this, "Something wrong on registration. Please try again later", Toast.LENGTH_LONG).show();
				}
				else {
					mProgress.setVisibility(View.VISIBLE);
					enableUI(false);
				}
			}
		});

		// DEBUG
		mEmail.setText("email123@domain.com");
		mName.setText("username123");
		mPassword.setText("password123");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		PWState.getInstance().removeListener(this);
	}

	private void enableUI(boolean enable) {
		mEmail.setEnabled(enable);
		mName.setEnabled(enable);
		mPassword.setEnabled(enable);

		mBack.setEnabled(enable);
		mSignup.setEnabled(enable);
	}
}
