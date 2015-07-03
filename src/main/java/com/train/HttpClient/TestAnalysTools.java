package com.train.HttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.CommunicationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.json.JSONObject;

public class TestAnalysTools  {
	Map<String, Object> sycCount = java.util.Collections
			.synchronizedMap(new HashMap<String, Object>());
	final CountDownLatch end = new CountDownLatch(100);

	protected final int timeOut = 120 * 1000;
	protected String userID = "hzhangse@cn.ibm.com";
	protected String password = "passw9rd";
	protected String httpSchema = "http";
	protected String httpsSchema = "https";
	protected String url = "://smallbluetest.ibm.com/services/smallblue/j_security_check";
	protected String index = "://smallbluetest.ibm.com/services/smallblue/index.do";

	protected int executeWithStatus(HttpClient client, HttpMethod method)
			throws CommunicationException {
		int status = 0;
		try {
			client.getHttpConnectionManager().getParams()
					.setConnectionTimeout(timeOut);
			status = client.executeMethod(method);
			System.out.println(status);
		} catch (HttpException e) {
			throw new CommunicationException(e.getMessage());
		} catch (IOException e) {
			throw new CommunicationException(e.getMessage());
		}

		return status;
	}

	protected static String getResponse(HttpMethod method) {
		StringBuilder response = new StringBuilder();
		InputStream response_is;
		try {
			response_is = method.getResponseBodyAsStream();

			InputStreamReader reader = new InputStreamReader(response_is);
			BufferedReader reader_buffered = new BufferedReader(reader);
			String line = reader_buffered.readLine();

			while (line != null) {
				System.out.println(line);
				response.append(line);
				line = reader_buffered.readLine();
			}

			reader_buffered.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response.toString();
	}

	protected String executeWithResponse(HttpClient client, HttpMethod method)
			throws CommunicationException {
		int status = 0;
		String response = "";
		try {
			client.getHttpConnectionManager().getParams()
					.setConnectionTimeout(timeOut);
			status = client.executeMethod(method);

			response = getResponse(method);
		} catch (HttpException e) {
			throw new CommunicationException(e.getMessage());
		} catch (IOException e) {
			throw new CommunicationException(e.getMessage());
		}

		return response;
	}

	public void testSendRequest() throws Exception {
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpClient client = new HttpClient(connectionManager);
		client.getState().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(userID, password));
		Protocol myhttps = new Protocol(httpsSchema,
				new AtlasSSLSocketFactory(), 443);
		Protocol.registerProtocol(httpsSchema, myhttps);
		PostMethod authpost = new PostMethod(httpsSchema + url);

		NameValuePair[] data = new NameValuePair[2];
		data[0] = new NameValuePair("j_username", userID);
		data[1] = new NameValuePair("j_password", password);

		authpost.setRequestBody(data);
		try {
			executeWithStatus(client, authpost);
		} catch (Exception e) {
			e.printStackTrace();
			authpost.releaseConnection();

		}

		ExecutorService pool = Executors.newFixedThreadPool(200);

		for (int i = 0; i < 100; i++) {
			MyThread t = new MyThread();
			t.requestNo = String.valueOf(i);
			t.defHttp = client;
			pool.execute(t);
		}
		end.await();

		System.out.println("total size of processid:" + sycCount.size());
		for (String prossid : sycCount.keySet()) {
			System.out.println(prossid);
		}

		pool.shutdown();
	}

	class MyThread extends Thread {
		public String requestNo = "";
		HttpClient defHttp = null;

		public void run() {
			GetMethod httpGetDoc = new GetMethod(
					"https://smallbluetest.ibm.com/services/smallblue/analyticstool?action=search&keyword=ibm"
							+ requestNo);
			System.out.println("clientNo=" + requestNo);
			String processid = "";
			try {
				String response = executeWithResponse(defHttp, httpGetDoc);
				JSONObject job = new JSONObject(response);
				processid = job.get("processid").toString();

				sycCount.put(processid, job.get("processid"));
			} catch (CommunicationException e) {
				e.printStackTrace();
			} finally {
				httpGetDoc.releaseConnection();
				end.countDown();

			}

		}

	}

	public static void main(String[] args) throws Exception {
		TestAnalysTools tools = new TestAnalysTools();
		tools.testSendRequest();
	}

}
