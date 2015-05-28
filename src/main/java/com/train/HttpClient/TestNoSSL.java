package com.train.HttpClient;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.junit.Test;

public class TestNoSSL extends TestSSL {
	private static Logger logger = Logger.getLogger(TestNoSSL.class.getName());

	/**
	 * Trust every server - dont check for any certificate
	 */
	private static void trustAllHosts() {
		final String TAG = "trustAllHosts";
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				logger.info("checkClientTrusted");
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				logger.info( "checkServerTrusted");
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testConnect() {
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpClient client = new HttpClient(connectionManager);
		client.getState().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(userID, password));

		PostMethod authpost = new PostMethod(httpSchema + url);

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
		GetMethod getMethod = new GetMethod(httpSchema + index);

		try {
			executeWithResponse(client, getMethod);
		} catch (Exception e) {
			e.printStackTrace();
			authpost.releaseConnection();

		}
	}

}
