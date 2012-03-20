package httpninja.hc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * This class override httpcomponents plain socket factory to be able to connect
 * via socks proxy.
 */
public class HttpNinjaPlainSocketFactory implements SchemeSocketFactory {

	@Override
	public Socket createSocket(HttpParams params) throws IOException {
		if (params == null) {
			throw new IllegalArgumentException(
					"HTTP parameters may not be null");
		}
		Proxy proxy = (Proxy) params.getParameter(HttpNinjaPNames.SOCKS_PROXY);

		if (proxy == null) {
			return new Socket();
		} else {
			return new Socket(proxy);
		}
	}

	@Override
	public Socket connectSocket(Socket socket, InetSocketAddress remoteAddress,
			InetSocketAddress localAddress, HttpParams params)
			throws IOException, UnknownHostException, ConnectTimeoutException {

		if (remoteAddress == null) {
			throw new IllegalArgumentException("Remote address may not be null");
		}
		if (params == null) {
			throw new IllegalArgumentException(
					"HTTP parameters may not be null");
		}
		Socket sock = socket;

		if (sock == null) {
			sock = createSocket(params);
		}
		if (localAddress != null) {
			sock.setReuseAddress(HttpConnectionParams.getSoReuseaddr(params));
			sock.bind(localAddress);
		}
		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
		int soTimeout = HttpConnectionParams.getSoTimeout(params);

		try {
			sock.setSoTimeout(soTimeout);
			sock.connect(remoteAddress, connTimeout);
		} catch (SocketTimeoutException ex) {
			throw new ConnectTimeoutException("Connect to " + remoteAddress
					+ " timed out");
		}
		return sock;
	}

	@Override
	public boolean isSecure(Socket sock) throws IllegalArgumentException {

		if (sock == null) {
			throw new IllegalArgumentException("Socket may not be null.");
		}

		// This check is performed last since it calls a method implemented
		// by the argument object. getClass() is final in java.lang.Object.
		if (sock.isClosed()) {
			throw new IllegalArgumentException("Socket is closed.");
		}

		return false;
	}

}
