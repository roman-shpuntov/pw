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

public class LoginActivity extends AppCompatActivity implements PWState.PWStateInterface {
	private EditText	mEmail;
	private EditText	mPassword;
	private Button		mRegistration;
	private Button		mSignin;
	private ProgressBar	mProgress;

	@Override
	public void onReady() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
				Toast.makeText(LoginActivity.this, error.getDescription(), Toast.LENGTH_LONG).show();
				mProgress.setVisibility(View.INVISIBLE);
				enableUI(true);
			}
		});
	}

	@Override
	public void onMessage(PWError error) {}

	@Override
	public void onInTransaction(PWTransaction trans) {}

	@Override
	public void onOutTransaction(PWTransaction trans) {}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		PWState.getInstance().addListener(this);

		mEmail		= findViewById(R.id.login_email);
		mPassword	= findViewById(R.id.login_password);
		mProgress	= findViewById(R.id.login_progress);

		mProgress.setVisibility(View.INVISIBLE);

		mRegistration = findViewById(R.id.login_reg);
		mRegistration.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivity(intent);
				finish();
			}
		});

		mSignin = findViewById(R.id.login_signin);
		mSignin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String	email	= mEmail.getText().toString();
				String	passwd	= mPassword.getText().toString();

				if (!PWChecker.isCorrectEmail(email)) {
					Toast.makeText(LoginActivity.this, "Please provide correct email.", Toast.LENGTH_LONG).show();
					return;
				}

				if (!PWChecker.isCorrectPassword(passwd)) {
					Toast.makeText(LoginActivity.this, "Please provide correct password (min " +
						PWChecker.PASSWORD_MIN_LENGTH + " and max " + PWChecker.PASSWORD_MAX_LENGTH + " symbols).", Toast.LENGTH_LONG).show();
					return;
				}

				int rc = PWState.getInstance().login(email, passwd);
				if (rc != 0) {
					PWLog.error("Login failed on login");
					Toast.makeText(LoginActivity.this, "Something wrong on login. Please try again later.", Toast.LENGTH_LONG).show();
				}
				else {
					mProgress.setVisibility(View.VISIBLE);
					enableUI(false);
				}
			}
		});

		// DEBUG
		//mEmail.setText("email123@domain.com");
		//mPassword.setText("password123");

		mEmail.setText("email456@domain.com");
		mPassword.setText("password456");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		PWState.getInstance().removeListener(this);
	}

	private void enableUI(boolean enable) {
		mEmail.setEnabled(enable);
		mPassword.setEnabled(enable);

		mRegistration.setEnabled(enable);
		mSignin.setEnabled(enable);
	}
}
