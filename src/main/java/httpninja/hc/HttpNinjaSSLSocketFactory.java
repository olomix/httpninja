package httpninja.hc;

import java.io.IOException;
import java.net.Proxy;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.params.HttpParams;

/**
 * This class override httpcomponents ssl socket factory to be able to connect
 * https via socks proxy.
 */
public class HttpNinjaSSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory {

	public HttpNinjaSSLSocketFactory(TrustSelfSignedStrategy trustStrategy,
			X509HostnameVerifier hostnameVerifier)
			throws KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException {
		super(trustStrategy, hostnameVerifier);
	}

	@Override
	public Socket createSocket(final HttpParams params) throws IOException {
		Proxy proxy = (Proxy) params.getParameter(HttpNinjaPNames.SOCKS_PROXY);

		if (proxy != null) {
			return new Socket(proxy);
		} else {
			return super.createSocket(params);
		}
	}
}
