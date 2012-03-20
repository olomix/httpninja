package httpninja;

import httpninja.hc.HttpNinjaPNames;
import httpninja.hc.HttpNinjaPlainSocketFactory;
import httpninja.hc.PostRedirectStrategy;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import httpninja.hc.HttpNinjaSSLSocketFactory;

public class HttpNinjaClient {
	private static final Logger LOG = LoggerFactory
			.getLogger(HttpNinjaClient.class);

	private final DefaultHttpClient httpClient;
	private static final String USER_AGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0)";
	private boolean sslSupported = true;

	public HttpNinjaClient() {
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
        cm.setMaxTotal(200);
		httpClient = new DefaultHttpClient(cm);
		httpClient.setRedirectStrategy(new PostRedirectStrategy());

		HttpParams params = httpClient.getParams();

		HttpProtocolParams.setUserAgent(params, USER_AGENT);

		setCookieSpec();

		SchemeRegistry schemeRegistry = httpClient.getConnectionManager()
				.getSchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80,
				new HttpNinjaPlainSocketFactory()));

		// Register custom https scheme to support https over socks proxy
		try {
			schemeRegistry.register(new Scheme("https", 443,
					new HttpNinjaSSLSocketFactory(
							new TrustSelfSignedStrategy(),
							SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));
		} catch (KeyManagementException e) {
			LOG.error(e.getMessage(), e);
			setSslSupported(false);
		} catch (UnrecoverableKeyException e) {
			LOG.error(e.getMessage(), e);
			setSslSupported(false);
		} catch (NoSuchAlgorithmException e) {
			LOG.error(e.getMessage(), e);
			setSslSupported(false);
		} catch (KeyStoreException e) {
			LOG.error(e.getMessage(), e);
			setSslSupported(false);
		}
	}

	/**
	 * Make cookie handler accept all cookies from site.
	 */
	private void setCookieSpec() {
		CookieSpecFactory csf = new CookieSpecFactory() {
			public CookieSpec newInstance(HttpParams params) {
				return new BrowserCompatSpec() {
					@Override
					public void validate(Cookie cookie, CookieOrigin origin)
							throws MalformedCookieException {
						// Accept all
					}
				};
			}
		};

		httpClient.getCookieSpecs().register(HttpNinjaPNames.COOKIE_ACCEPT_ALL,
				csf);
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				HttpNinjaPNames.COOKIE_ACCEPT_ALL);
	}

	public HttpResponse execute(HttpUriRequest request, HttpContext context)
			throws ClientProtocolException, IOException {
		if (!isSslSupported() && "https".equals(request.getURI().getScheme())) {
			throw new SSLException("HTTPS is not supported");
		}

		return getHttpClient().execute(request, context);
	}

	/**
	 * Get actual request URL from HttpContext. That is a URL of request in case
	 * of redirect or something like
	 * 
	 * @param context
	 * @param defaultUrl
	 * @return last URL of request in case of redirection.
	 */
	public static String getRequestUrl(HttpContext context, String defaultUrl) {
		HttpUriRequest httpRequest = (HttpUriRequest) context
				.getAttribute(ExecutionContext.HTTP_REQUEST);
		if (httpRequest != null) {
			HttpHost host = (HttpHost) context
					.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			URI uri = httpRequest.getURI();
			if (uri.isAbsolute()) {
				return uri.toString();
			} else if (host != null) {
				return host.toURI() + uri;
			}
		}
		return defaultUrl;
	}

	private void setSslSupported(boolean flag) {
		sslSupported = flag;
	}

	private boolean isSslSupported() {
		return sslSupported;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void shutdown() {
		getHttpClient().getConnectionManager().shutdown();
	}

}
