package httpninja;

import java.net.InetSocketAddress;
import java.util.List;

import org.jsoup.nodes.Document;

public interface HttpConnection {
	/**
	 * Perform http connection to url.
	 * 
	 * @param url
	 * @return parsed response.
	 */
	public Document get(String url);

	/**
	 * Set the list of proxies to use.
	 * 
	 * @param proxies
	 */
	public void setProxies(List<InetSocketAddress> proxies);
}
