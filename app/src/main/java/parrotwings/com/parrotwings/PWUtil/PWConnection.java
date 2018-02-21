package parrotwings.com.parrotwings.PWUtil;

import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by roman on 21.02.2018.
 */

public class PWConnection {
	public interface PWConnectionInterface {
		void onRecv(String result);
	}

	private static volatile PWConnection	mInstance;

	private	HttpClient						mClient;
	private List<PWConnectionInterface>		mListeners;

	public static final String				TYPE_POST			= "POST";
	public static final String				TYPE_GET			= "GET";

	public static final int					POSITION_TYPE		= 0;
	public static final int					POSITION_URL		= 1;
	public static final int					POSITION_PAYLOAD	= 2;
	public static final int					POSITION_HEADER		= 3;

	private PWConnection() {
		mClient = new DefaultHttpClient();
		mListeners = new LinkedList<PWConnectionInterface>();
	}

	public static PWConnection getInstance() {
		if (mInstance == null) {
			synchronized (PWConnection.class) {
				if (mInstance == null) {
					mInstance = new PWConnection();
				}
			}
		}

		return mInstance;
	}

	public void addListener(PWConnectionInterface listener) {
		mListeners.add(listener);
	}

	public void removeListener(PWConnectionInterface listener) {
		mListeners.remove(listener);
	}

	private static String convertInputStreamToString(InputStream inputStream) throws IOException{
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;
	}

	private String postRequest(String url, String payload, String... header) {
		InputStream inputStream;
		String result = "";
		try {
			HttpPost request = new HttpPost(url);
			StringEntity se = new StringEntity(payload);
			request.setEntity(se);

			if (header != null) {
				int i;
				for (i=0; i<header.length / 2; i++)
					request.setHeader(header[i * 2], header[i * 2 + 1]);
			}

			HttpResponse response = mClient.execute(request);
			inputStream = response.getEntity().getContent();
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);

		} catch (Exception e) {
			PWLog.error("pwconnection failed on post" + e.getLocalizedMessage());
		}

		return result;
	}

	private String getRequest(String url, String... header) {
		InputStream inputStream;
		String result = "";
		try {
			HttpGet request = new HttpGet(url);

			if (header != null) {
				int i;
				for (i=0; i<header.length / 2; i++)
					request.setHeader(header[i * 2], header[i * 2 + 1]);
			}

			HttpResponse response = mClient.execute(request);
			inputStream = response.getEntity().getContent();
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);

		} catch (Exception e) {
			PWLog.error("pwconnection failed on post" + e.getLocalizedMessage());
		}

		return result;
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String[]	header = null;
			if (urls.length > POSITION_HEADER) {
				int	count = urls.length - POSITION_HEADER;
				header = new String[count];

				int	i;
				for (i=0; i<urls.length - POSITION_HEADER; i++)
					header[i] = urls[i + POSITION_HEADER];
			}

			String	type	= urls[POSITION_TYPE];
			String	url		= urls[POSITION_URL];
			String	pload	= urls[POSITION_PAYLOAD];

			if (type.compareTo(TYPE_POST) == 0)
				return postRequest(url, pload, header);
			else if (type.compareTo(TYPE_GET) == 0)
				return getRequest(url, header);

			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			PWLog.debug("pwconnection result " + result);

			ListIterator<PWConnectionInterface> itr = mListeners.listIterator();
			while (itr.hasNext()) {
				PWConnectionInterface iface = itr.next();
				iface.onRecv(result);
			}
		}
	}

	public int send(String type, String url, String payload, String... header) {
		String[]	params = new String[POSITION_HEADER + header.length];

		params[POSITION_TYPE]		= type;
		params[POSITION_URL]		= url;
		params[POSITION_PAYLOAD]	= payload;

		int	i;
		for(i=0; i<header.length; i++)
			params[POSITION_HEADER + i] = header[i];

		AsyncTask	task = new HttpAsyncTask().execute(params);
		if (task == null)
			return -1;

		return 0;
	}
}
