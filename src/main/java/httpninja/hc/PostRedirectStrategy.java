package httpninja.hc;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This strategy accept redirects during POST request. Default httpcomponents
 * implementation doens't allow it.
 */
public class PostRedirectStrategy extends DefaultRedirectStrategy {
	private static final Logger LOG = LoggerFactory
			.getLogger(PostRedirectStrategy.class);

	public boolean isRedirected(final HttpRequest request,
			final HttpResponse response, final HttpContext context)
			throws ProtocolException {
		LOG.debug("Check if redirect is needed.");
		if (response == null) {
			throw new IllegalArgumentException("HTTP response may not be null");
		}

		int statusCode = response.getStatusLine().getStatusCode();
		String method = request.getRequestLine().getMethod();
		Header locationHeader = response.getFirstHeader("location");
		if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)
				&& ((statusCode == HttpStatus.SC_MOVED_TEMPORARILY && locationHeader != null) || (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT))) {
			LOG.debug("POST redirect is needed.");
			return true;
		}

		return super.isRedirected(request, response, context);
	}

}
