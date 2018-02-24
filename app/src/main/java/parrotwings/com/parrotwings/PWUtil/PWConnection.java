package parrotwings.com.parrotwings.PWUtil;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by roman on 21.02.2018.
 */

public class PWConnection {
	public interface PWConnectionInterface {
		void onRecv(int request, PWError result);
	}

	private	HttpClient						mClient;
	private List<PWConnectionInterface>		mListeners;
	private	String							mBaseURL;

	public static final int					INVALID_REQUEST		= 0;

	public static final String				TYPE_POST			= "POST";
	public static final String				TYPE_GET			= "GET";

	public static final String				OBJECT_REQUEST		= "REQUEST";
	public static final String				OBJECT_TYPE			= "TYPE";
	public static final String				OBJECT_URL			= "URL";
	public static final String				OBJECT_PAYLOAD		= "PAYLOAD";
	public static final String				OBJECT_HEADER		= "HEADER";

	public PWConnection(String baseURL) {
		mBaseURL = baseURL;
		mClient = new DefaultHttpClient();
		mListeners = new LinkedList<>();
	}

	public void addListener(PWConnectionInterface listener) {
		mListeners.add(listener);
	}

	public void removeListener(PWConnectionInterface listener) {
		mListeners.remove(listener);
	}

	private static String convertInputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;
	}

	private int fillHeader(AbstractHttpMessage req, JSONObject json) {
		JSONObject	header;

		try {
			header = json.getJSONObject(OBJECT_HEADER);

			Iterator<String> iter = header.keys();
			while (iter.hasNext()) {
				String key = iter.next();
				try {
					req.setHeader(key, header.getString(key));
				} catch (JSONException e) {
					PWLog.error("pwconnection fillHeader failed on parse header");
					return -1;
				}
			}
		} catch (Exception e) {}

		return 0;
	}

	private PWError postRequest(JSONObject json) {
		String		url;
		String		pload;
		try {
			url		= json.getString(OBJECT_URL);
			pload	= json.getString(OBJECT_PAYLOAD);
		} catch (Exception e) {
			PWLog.error("pwconnection no url/pload");
			return new PWError(PWError.GENERAL_ERROR, PWError.GENERAL_ERROR_DESC);
		}

		InputStream inputStream;
		String result = PWError.GENERAL_ERROR_DESC;
		int code = PWError.GENERAL_ERROR;
		try {
			HttpPost		request	= new HttpPost(mBaseURL + url);
			StringEntity	se		= new StringEntity(pload);

			request.setEntity(se);
			if (fillHeader(request, json) != 0)
				return new PWError(PWError.GENERAL_ERROR, PWError.GENERAL_ERROR_DESC);

			HttpResponse response = mClient.execute(request);
			inputStream = response.getEntity().getContent();
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);
			code = response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			PWLog.error("pwconnection failed on post" + e.getLocalizedMessage());
		}

		return new PWError(code, result);
	}

	private PWError getRequest(JSONObject json) {
		String		url;
		try {
			url = json.getString(OBJECT_URL);
		} catch (Exception e) {
			PWLog.error("pwconnection no url");
			return new PWError(PWError.GENERAL_ERROR, PWError.GENERAL_ERROR_DESC);
		}

		InputStream inputStream;
		String result = PWError.GENERAL_ERROR_DESC;
		int code = PWError.GENERAL_ERROR;
		try {
			HttpGet	request = new HttpGet(mBaseURL + url);
			if (fillHeader(request, json) != 0)
				return new PWError(PWError.GENERAL_ERROR, PWError.GENERAL_ERROR_DESC);

			HttpResponse response = mClient.execute(request);
			inputStream = response.getEntity().getContent();
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);
			code = response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			PWLog.error("pwconnection failed on get" + e.getLocalizedMessage());
		}

		return new PWError(code, result);
	}

	private class HTTPResponse {
		PWError	error	= new PWError(PWError.GENERAL_ERROR, PWError.GENERAL_ERROR_DESC);
		int		request	= INVALID_REQUEST;

		HTTPResponse() {}

		HTTPResponse(int r) {
			request = r;
		}

		HTTPResponse(PWError e, int r) {
			error = e;
			request = r;
		}
	}

	private class HttpJSONAsyncTask extends AsyncTask<JSONObject, Void, HTTPResponse> {
		@Override
		protected HTTPResponse doInBackground(JSONObject... json) {
			String	type;
			int		req = INVALID_REQUEST;
			try {
				type	= json[0].getString(OBJECT_TYPE);
				req		= json[0].getInt(OBJECT_REQUEST);
			} catch (Exception e) {
				PWLog.error("pwconnection no type");
				return new HTTPResponse(req);
			}

			if (type.compareTo(TYPE_POST) == 0)
				return new HTTPResponse(postRequest(json[0]), req);
			else if (type.compareTo(TYPE_GET) == 0)
				return new HTTPResponse(getRequest(json[0]), req);

			return new HTTPResponse(req);
		}

		@Override
		protected void onPostExecute(HTTPResponse result) {
			//PWLog.verbose("pwconnection result " + result.error.getDescription());

			ListIterator<PWConnectionInterface> itr = mListeners.listIterator();
			while (itr.hasNext()) {
				PWConnectionInterface iface = itr.next();
				iface.onRecv(result.request, result.error);
			}
		}
	}

	public int send(JSONObject json) {
		AsyncTask	task = new HttpJSONAsyncTask().execute(json);
		return (task == null)?-1:0;
	}
}
