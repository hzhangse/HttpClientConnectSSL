package com.train.HttpClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;


public class AtlasSSLSocketFactory implements ProtocolSocketFactory {

    private SSLContext sslContext = null;
    
    private static X509TrustManager x509TrustManager = 
        new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, 
                    String authType) {}
            public void checkServerTrusted(X509Certificate[] chain,
                    String authType) {
                // if do noting, accept any certificate from server
                // otherwise, filter some certificates
                /* X509Certificate c1  = chain[0];
                   System.out.println(c1.getIssuerDN());
                   System.out.println("cert: " + chain[0].toString() + ", authType: " + authType);
                   System.out.println("authType: " + authType);*/
            }
            
            public X509Certificate[] getAcceptedIssuers() { // FIXME: Crash here?
                return null;
            }
    };
    
    /**
     * 
     */
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(host, port);
    }

    /**
     * 
     */
    public Socket createSocket(String host, int port, InetAddress localAddr,
            int localPort) throws IOException, UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(host, port,
                localAddr, localPort);
    }

    /**
     * 
     */
    public Socket createSocket(String host, int port, InetAddress localAddr,
            int localPort, HttpConnectionParams params) throws IOException,
            UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        SocketFactory socketfactory = getSSLContext().getSocketFactory();
        if (timeout == 0) {
            return socketfactory.createSocket(host, port, localAddr, localPort);
        } 
        else {
            Socket socket = socketfactory.createSocket();
            String[] protocols = { "TLSv1","TLSv1.2","TLSv1.1" };  
            
          ( (SSLSocket) socket).setEnabledProtocols(protocols);  
           
            SocketAddress localaddr = new InetSocketAddress(localAddr, localPort);
            SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
            return socket;
        }
    }
    
    /**
     * 
     * @return
     */
    private static SSLContext createSSLContext() {
        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, new TrustManager[] { x509TrustManager },
                    new java.security.SecureRandom());
            return context;
        }
        catch (Exception e) {
            throw new HttpClientError(e.toString());
        }
    }

    /**
     * @return
     */
    private SSLContext getSSLContext() {
        if (this.sslContext == null) {
            this.sslContext = createSSLContext();
        }
        return this.sslContext;
    }

}
