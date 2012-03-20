package httpninja.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import httpninja.HttpConnection;
import httpninja.HttpNinjaClient;
import httpninja.hc.HttpNinjaPNames;
import httpninja.hc.JsoupResponseHandler;

/**
 * This class initialized with proxies list will retrieve html pages from
 * different urls through different proxies base on hostname.
 * 
 */
public class BasicHttpConnection implements HttpConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(BasicHttpConnection.class);

	private List<InetSocketAddress> proxies;
	private int proxiesNum = 0;
	private HttpNinjaClient httpClient;

	public BasicHttpConnection(List<InetSocketAddress> proxies) {
		if (proxies != null)
			setProxies(proxies);

		httpClient = new HttpNinjaClient();
	}

	public void setProxies(List<InetSocketAddress> proxies) {
		this.proxies = proxies;
		this.proxiesNum = proxies.size();
	}

	@Override
	public Document get(String url) {
		HttpGet httpGet = new HttpGet(url);
		HttpContext context = new BasicHttpContext();

		InetSocketAddress proxyAddr = getProxy(httpGet.getURI().getHost());
		if (proxyAddr != null) {
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
			HttpParams params = httpGet.getParams();
			params.setParameter(HttpNinjaPNames.SOCKS_PROXY, proxy);
		}

		try {

			HttpResponse response = httpClient.execute(httpGet, context);
			String actualUrl = HttpNinjaClient.getRequestUrl(context, url);
			return JsoupResponseHandler.handleResponse(response, actualUrl);

		} catch (ClientProtocolException e) {
			LOG.error(e.getMessage(), e);
			return null;
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}

	}

	/**
	 * Get proxy from list based on hostname.
	 * 
	 * @param hostname
	 * @return
	 */
	private InetSocketAddress getProxy(String hostname) {
		if (proxiesNum <= 0)
			return null;
		int idx = Math.abs(hostname.hashCode()) % proxiesNum;
		return proxies.get(idx);
	}

}
