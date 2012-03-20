package httpninja.hc;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupResponseHandler {
	private static final Logger LOG = LoggerFactory
			.getLogger(JsoupResponseHandler.class);

	public static Document handleResponse(HttpResponse response, String baseUrl)
			throws ClientProtocolException, IOException {
		StatusLine statusLine = response.getStatusLine();

		LOG.debug("" + statusLine.getStatusCode() + ": "
				+ statusLine.getReasonPhrase() + " "
				+ statusLine.getProtocolVersion().getProtocol());

		int statusCode = statusLine.getStatusCode();
		Header locationHeader = response.getFirstHeader("location");
		if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
				&& locationHeader != null) {

		}
		if (statusLine.getStatusCode() >= 300) {
			throw new HttpResponseException(statusLine.getStatusCode(),
					statusLine.getReasonPhrase());
		}

		HttpEntity entity = response.getEntity();
		if (entity == null) {
			throw new IllegalStateException(
					"This response does not contains body.");
		}

		Header encodingHeader = entity.getContentEncoding();

		try {
			Document document = Jsoup.parse(
					entity.getContent(),
					encodingHeader == null ? "UTF-8" : encodingHeader
							.getValue(), baseUrl);
			return document;
		} catch (Exception t) {
			LOG.error(t.getMessage(), t);
		}

		// Release resources and consume entity.
		try {
			EntityUtils.consume(response.getEntity());
		} catch (Exception e) {
			LOG.warn("Error consuming content.", e);
		}
		
		return null;
	}

}
