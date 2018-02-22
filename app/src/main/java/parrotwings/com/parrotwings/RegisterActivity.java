package parrotwings.com.parrotwings;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import parrotwings.com.parrotwings.PWUtil.PWState;
import parrotwings.com.parrotwings.PWUtil.PWTransaction;

public class RegisterActivity extends AppCompatActivity implements PWState.PWStateInterface {
	private EditText	mEmail;
	private EditText	mName;
	private EditText	mPassword;
	private Button		mBack;
	private Button		mSignup;

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
				if (rc != 0)
					Toast.makeText(RegisterActivity.this, "Something wrong on registration. Please try again later", Toast.LENGTH_LONG).show();
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
}
