package parrotwings.com.parrotwings.PWUtil;

/**
 * Created by roman on 22.02.2018.
 */

public class PWError {
	public static final int		GENERAL_ERROR		= -1;
	public static final String	GENERAL_ERROR_DESC	= "General system error.";

	public static final int		OK					= 0;

	// reserve HTTP codes from 100 to 999
	public static final int		HTTP_START			= 100;
	public static final int		HTTP_OK_START		= 200;
	public static final int		HTTP_OK_END			= 299;
	public static final int		HTTP_END			= 999;

	private int		mCode = OK;
	private String	mDescription = "";

	public PWError() {}

	public PWError(int code, String description) {
		mCode = code;
		mDescription = description;
	}

	public int getCode() {
		return mCode;
	}

	public String getDescription() {
		return mDescription;
	}

	public boolean isSystemSuccess() {
		return mCode > 0;
	}

	public boolean isHTTPSuccess() {
		return (mCode >= HTTP_OK_START) && (mCode <= HTTP_OK_END);
	}

	public boolean isSuccess() {
		return isSystemSuccess() && isHTTPSuccess();
	}
}
