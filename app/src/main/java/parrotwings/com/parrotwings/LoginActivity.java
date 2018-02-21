package parrotwings.com.parrotwings;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import parrotwings.com.parrotwings.PWUtil.PWState;

public class LoginActivity extends AppCompatActivity implements PWState.PWStateInterface {
	private TextView	mEmail;
	private TextView	mPassword;
	private Button		mRegistration;
	private Button		mSignin;

	@Override
	public void onReady() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onIncome() {}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		PWState.getInstance().addListener(this);

		mEmail		= findViewById(R.id.login_email);
		mPassword	= findViewById(R.id.login_password);

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
				int rc = PWState.getInstance().login(mEmail.getText().toString(), mPassword.getText().toString());
				if (rc != 0)
					Toast.makeText(LoginActivity.this, "Something wrong on login. Please try again later", Toast.LENGTH_LONG).show();
			}
		});

		// DEBUG
		mEmail.setText("email123@domain.com");
		mPassword.setText("password123");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		PWState.getInstance().removeListener(this);
	}
}
