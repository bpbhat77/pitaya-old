package com.emo.pitaya.email.html;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class UnloadedHtmlContent implements HtmlContent {

	private final URL baseUrl;
	private final boolean useContentDotHtml;

	private final static String CONTENT_HTML = "content.html";

	public UnloadedHtmlContent(final URL url, final boolean useContentDotHtml) {
		this.baseUrl = url;
		this.useContentDotHtml = useContentDotHtml;
	}

	public LoadedHtmlContent load() {
		final URL contentUrl;
		try {
			contentUrl = (useContentDotHtml) ? ((UriComponentsBuilder
					.fromUri(baseUrl.toURI())).pathSegment(CONTENT_HTML)
					.build().toUri().toURL()) : baseUrl;
		} catch (Exception e) {
			throw new RuntimeException("failed to build html content url : "
					+ e.getMessage(), e);
		}

		final InputStream is;

		try {
			is = contentUrl.openStream();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to open stream for html content url : "
							+ e.getMessage(), e);
		}

		try {
			return new LoadedHtmlContent(baseUrl, IOUtils.toString(is, "UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to transform html content stream to string : "
							+ e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	@Override
	public String toString() {
		return load().toString();
	}
}
