package httpninja;

import java.net.InetSocketAddress;
import java.util.List;

import httpninja.impl.BasicHttpConnection;

public class HttpNinjaSpringBean {
	private HttpConnection httpConnection;

	public HttpNinjaSpringBean() {
		httpConnection = new BasicHttpConnection(null);
	}

	public void setProxies(List<InetSocketAddress> proxies) {
		httpConnection.setProxies(proxies);
	}
	
	public HttpConnection getHttpConnection() {
		return httpConnection;
	}
}
