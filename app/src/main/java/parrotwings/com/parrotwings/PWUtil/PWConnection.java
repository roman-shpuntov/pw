package parrotwings.com.parrotwings.PWUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by roman on 21.02.2018.
 */

public class PWConnection {
	public interface PWConnectionInterface {
		void onRecv(String request, PWError result);
	}

	private	HttpClient						mClient;
	private List<PWConnectionInterface>		mListeners;
	private	String							mBaseURL;
	private Thread							mThread;
	private	List<JSONObject>				mTransfer;
	private boolean							mRunning;
	private Lock							mLock;
	private Condition						mReady;

	public static final String				REQUEST_INVALID		= "REQUEST_INVALID";

	public static final String				TYPE_POST			= "POST";
	public static final String				TYPE_GET			= "GET";

	public static final String				OBJECT_REQUEST		= "REQUEST";
	public static final String				OBJECT_TYPE			= "TYPE";
	public static final String				OBJECT_URL			= "URL";
	public static final String				OBJECT_PAYLOAD		= "PAYLOAD";
	public static final String				OBJECT_HEADER		= "HEADER";

	private HTTPResponse doTransfer(JSONObject json) {
		String	type;
		String	req = REQUEST_INVALID;
		try {
			type	= json.getString(OBJECT_TYPE);
			req		= json.getString(OBJECT_REQUEST);
		} catch (Exception e) {
			PWLog.error("pwconnection no type");
			return new HTTPResponse(req);
		}

		if (type.compareTo(TYPE_POST) == 0)
			return new HTTPResponse(postRequest(json), req);
		else if (type.compareTo(TYPE_GET) == 0)
			return new HTTPResponse(getRequest(json), req);

		return new HTTPResponse(req);
	}

	public PWConnection(String baseURL) {
		mBaseURL	= baseURL;
		mClient		= new DefaultHttpClient();
		mListeners	= new LinkedList<>();
		mTransfer	= new ArrayList<>();
		mRunning	= true;
		mLock		= new ReentrantLock();
		mReady		= mLock.newCondition();

		mThread = new Thread(new Runnable() {
			@Override
			public void run() {
				List<JSONObject> transfer = new ArrayList<>();

				while (mRunning) {
					mLock.lock();
					try {
						if (mTransfer.size() == 0)
							mReady.await();
						transfer.addAll(mTransfer);
						mTransfer.clear();
					} catch (InterruptedException e) {}
					mLock.unlock();

					for (JSONObject json : transfer) {
						HTTPResponse resp = doTransfer(json);

						ListIterator<PWConnectionInterface> itr = mListeners.listIterator();
						while (itr.hasNext()) {
							PWConnectionInterface iface = itr.next();
							iface.onRecv(resp.request, resp.error);
						}
					}

					transfer.clear();
				}
			}
		});

		mThread.start();
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
			return new PWError();
		}

		InputStream inputStream;
		String result = PWError.GENERAL_ERROR_DESC;
		int code = PWError.GENERAL_ERROR;
		try {
			HttpPost		request	= new HttpPost(mBaseURL + url);
			StringEntity	se		= new StringEntity(pload);

			request.setEntity(se);
			if (fillHeader(request, json) != 0)
				return new PWError();

			HttpResponse response = mClient.execute(request);
			inputStream = response.getEntity().getContent();
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);
			code = response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			PWLog.error("pwconnection failed on post " + e.getLocalizedMessage());
			result = e.getLocalizedMessage();
		}

		return new PWError(code, result);
	}

	private PWError getRequest(JSONObject json) {
		String		url;
		try {
			url = json.getString(OBJECT_URL);
		} catch (Exception e) {
			PWLog.error("pwconnection no url");
			return new PWError();
		}

		InputStream inputStream;
		String result = PWError.GENERAL_ERROR_DESC;
		int code = PWError.GENERAL_ERROR;
		try {
			HttpGet	request = new HttpGet(mBaseURL + url);
			if (fillHeader(request, json) != 0)
				return new PWError();

			HttpResponse response = mClient.execute(request);
			inputStream = response.getEntity().getContent();
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);
			code = response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			PWLog.error("pwconnection failed on get " + e.getLocalizedMessage());
			result = e.getLocalizedMessage();
		}

		return new PWError(code, result);
	}

	private class HTTPResponse {
		PWError	error	= new PWError();
		String	request	= REQUEST_INVALID;

		HTTPResponse() {}

		HTTPResponse(String r) {
			request = r;
		}

		HTTPResponse(PWError e, String r) {
			error = e;
			request = r;
		}
	}

	public int send(JSONObject json) {
		PWLog.debug("pwconnection send");

		mLock.lock();
		mTransfer.add(json);
		mReady.signal();
		mLock.unlock();

		return 0;
	}

	public void close() {
		PWLog.debug("pwconnection close");

		mRunning = false;

		mLock.lock();
		mReady.signal();
		mLock.unlock();

		try {
			mThread.join(3000);
		} catch (InterruptedException e) {}
	}
}
