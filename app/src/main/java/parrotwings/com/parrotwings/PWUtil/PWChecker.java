package parrotwings.com.parrotwings.PWUtil;

/**
 * Created by roman on 24.02.2018.
 */

public class PWChecker {
	public static final int PASSWORD_MIN_LENGTH		 = 6;
	public static final int PASSWORD_MAX_LENGTH		 = 32;

	public static final int USERNAME_MIN_LENGTH		 = 6;
	public static final int USERNAME_MAX_LENGTH		 = 32;

	public static boolean isCorrectEmail(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}

	public static boolean isCorrectPassword(String password) {
		int	len = password.length();
		return len >= PASSWORD_MIN_LENGTH && len <= PASSWORD_MAX_LENGTH;
	}

	public static boolean isCorrectUsername(String username) {
		int	len = username.length();
		return len >= USERNAME_MIN_LENGTH && len <= USERNAME_MAX_LENGTH;
	}
}
