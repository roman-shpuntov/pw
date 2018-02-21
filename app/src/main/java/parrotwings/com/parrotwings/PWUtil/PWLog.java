package parrotwings.com.parrotwings.PWUtil;

import android.util.Log;

/**
 * Created by roman on 21.02.2018.
 */

public class PWLog {
	public static void info(String message) {
		Log.i("info", message);
	}

	public static void debug(String message) {
		Log.d("debug", message);
	}

	public static void warning(String message) {
		Log.w("warning", message);
	}

	public static void error(String message) {
		Log.e("error", message);
	}

	public static void verbose(String message) {
		Log.v("verbose", message);
	}
}
