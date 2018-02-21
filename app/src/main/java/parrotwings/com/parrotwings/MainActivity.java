package parrotwings.com.parrotwings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import parrotwings.com.parrotwings.PWUtil.PWConnection;
import parrotwings.com.parrotwings.PWUtil.PWParser;
import parrotwings.com.parrotwings.PWUtil.PWUser;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PWUser		user	= new PWUser("name.example", "pass.example", "email@example.com");
		PWParser	parser	= PWParser.getInstance();
		parser.register(user);
	}
}
