package com.train.HttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import org.junit.Test;

public class TestSSL {
	protected final int timeOut = 120 * 1000;
	protected String userID = "hzhangse@cn.ibm.com";
	protected String password = "passw8rd";
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

	protected int executeWithResponse(HttpClient client, HttpMethod method)
			throws CommunicationException {
		int status = 0;
		try {
			client.getHttpConnectionManager().getParams()
					.setConnectionTimeout(timeOut);
			status = client.executeMethod(method);
			
			getResponse(method);
		} catch (HttpException e) {
			throw new CommunicationException(e.getMessage());
		} catch (IOException e) {
			throw new CommunicationException(e.getMessage());
		}

		return status;
	}

	@Test
	public void testConnect() {
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpClient client = new HttpClient(connectionManager);
		client.getState().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(userID, password));
		Protocol myhttps = new Protocol(httpsSchema,
				new AtlasSSLSocketFactory(), 443);
		Protocol.registerProtocol(httpsSchema, myhttps);
		PostMethod authpost = new PostMethod(httpsSchema+url);
		
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
		GetMethod getMethod = new GetMethod(httpsSchema+index);

		try {
			executeWithResponse(client, getMethod);
		} catch (Exception e) {
			e.printStackTrace();
			authpost.releaseConnection();

		}
	}
	
}
