package com.emo.pitaya.email.html;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.mail.HtmlEmail;
import org.springframework.web.util.UriComponentsBuilder;

public class LoadedHtmlContent implements HtmlContent {
	private final URL baseUrl;
	private final String content;

	public LoadedHtmlContent(final URL baseUrl, final String content) {
		this.baseUrl = baseUrl;
		this.content = content;
	}

	private final static Pattern IMG_TAG_PATTERN = Pattern
			.compile("\\<\\s*[iI][mM][gG][^\\>]+[sS][rR][cC]\\s*\\=\\s*[\\\"\\']([^\\\"\\']+)[\\\"\\']");

	private final boolean isAbsolutePath(final String path) {
		final String lowerPath = path.toLowerCase();

		return lowerPath.startsWith("http://")
				|| lowerPath.startsWith("https://");
	}

	public CidifiedHtmlContent cidify(final HtmlEmail email,
			final boolean ignoreAbsoluteLink) throws Exception {
		final Matcher m = IMG_TAG_PATTERN.matcher(content);
		StringBuffer modifiedContent = new StringBuffer();

		while (m.find()) {
			final String replacement = m.group();
			final String path = m.group(1);

			final boolean isAbsolutePath = isAbsolutePath(path);

			if (ignoreAbsoluteLink && isAbsolutePath) {
				m.appendReplacement(modifiedContent, replacement.replace("$", "\\$"));
			} else {
				final URL imageUrl = (!isAbsolutePath) ? ((UriComponentsBuilder
						.fromUri(baseUrl.toURI())).pathSegment(path).build()
						.toUri().toURL()) : new URL(path);

				final String cid = email.embed(imageUrl, path);
				
				m.appendReplacement(modifiedContent,
						replacement.replace(path, "cid:" + cid));
			}
		}

		m.appendTail(modifiedContent);

		return new CidifiedHtmlContent(modifiedContent.toString());
	}

	public UrlifiedHtmlContent urlify() throws Exception {
		final Matcher m = IMG_TAG_PATTERN.matcher(content);

		StringBuffer modifiedContent = new StringBuffer();

		while (m.find()) {
			final String replacement = m.group();
			final String path = m.group(1);
			
			final URL imageUrl = (!isAbsolutePath(path)) ? ((UriComponentsBuilder
					.fromUri(baseUrl.toURI())).pathSegment(path).build()
					.toUri().toURL()) : null;

			m.appendReplacement(
					modifiedContent,
					replacement.replace(path,
							(imageUrl == null) ? path : imageUrl.toString())
							.replaceAll("\\$", "\\$"));
		}

		m.appendTail(modifiedContent);

		return new UrlifiedHtmlContent(modifiedContent.toString());
	}

	@Override
	public String toString() {
		return content;
	}
}
