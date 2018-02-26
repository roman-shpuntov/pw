package parrotwings.com.parrotwings.PWUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * Created by roman on 21.02.2018.
 */

public class PWUser {
	private	String		mName = "";
	private	String		mPassword = "";
	private	String		mEmail = "";

	public PWUser() {}

	public PWUser(String name, String email, String password) {
		mName		= name;
		mPassword	= password;
		mEmail		= email;
	}

	public PWUser(String email, String password) {
		mPassword	= password;
		mEmail		= email;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}

	public String getPassword() {
		return mPassword;
	}

	public String getEmail() {
		return mEmail;
	}
}
