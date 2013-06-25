package com.emo.pitaya.email;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.web.util.UriComponentsBuilder;

public class EmailSendTask implements Runnable {

	private final HtmlContent htmlContent;
	private final Recipient[] recipients;
	private final String smtp;
	private final String subject;
	private final String sender;

	public static class Recipient {
		public final String email;
		public final String name;

		public Recipient(final String email, final String name) {
			this.name = name;
			this.email = email;
		}

		public Recipient(final String email) {
			this.name = null;
			this.email = email;
		}

		public void declareToIn(final Email email) throws EmailException {
			if (name == null) {
				email.addTo(this.email);
			} else {
				email.addTo(this.email, name);
			}
		}
	}
	
	public static class HtmlContent {
		
		private final URL rootUrl;
		private final URL contentUrl;
		
		public HtmlContent(final URL url, final boolean containsContentDotHtml) throws Exception {
			this.rootUrl = url;
			this.contentUrl = (containsContentDotHtml) ? ((UriComponentsBuilder
					.fromUri(url.toURI())).pathSegment("content.html").build()
					.toUri().toURL()) : url;
		}
		
		public String cidifyImages(final HtmlEmail email) throws Exception {
			final Pattern regex = Pattern.compile("\\<\\s*[iI][mM][gG][^\\>]+[sS][rR][cC]\\s*\\=\\s*[\\\"\\']([^\\\"\\']+)[\\\"\\']");
			final String content = IOUtils.toString(contentUrl.openStream());
			final Matcher m = regex.matcher(content);
			
			StringBuffer modifiedContent = new StringBuffer();
			
			while(m.find()) {
				final String replacement = m.group();
				final String path = m.group(1);
				
				final URL imageUrl = (!path.startsWith("http://") && !path.startsWith("https://"))?((UriComponentsBuilder
						.fromUri(rootUrl.toURI())).pathSegment(path).build()
						.toUri().toURL()):new URL(path);
				
				final String cid = email.embed(imageUrl, path);
								
				m.appendReplacement(modifiedContent, replacement.replace(path, "cid:" + cid));
			}
			
			m.appendTail(modifiedContent);
			
			return modifiedContent.toString();
		}
		
		public String urlifyImages() throws Exception {
			final Pattern regex = Pattern.compile("\\<\\s*[iI][mM][gG][^\\>]+[sS][rR][cC]\\s*\\=\\s*[\\\"\\']([^\\\"\\']+)[\\\"\\']");
			final String content = IOUtils.toString(contentUrl.openStream());
			final Matcher m = regex.matcher(content);
			
			StringBuffer modifiedContent = new StringBuffer();
			
			while(m.find()) {
				final String replacement = m.group();
				final String path = m.group(1);
				final URL imageUrl = (!path.startsWith("http://") && !path.startsWith("https://"))?((UriComponentsBuilder
						.fromUri(rootUrl.toURI())).pathSegment(path).build()
						.toUri().toURL()):null;
				
				m.appendReplacement(modifiedContent, replacement.replace(path, (imageUrl == null)?path:imageUrl.toString()).replaceAll("\\$", ""));
			}
			
			m.appendTail(modifiedContent);
			
			return modifiedContent.toString();
		}
	}

	public EmailSendTask(final String smtp, final String sender, final URL url,
			final boolean containsContentDotHtml, final Recipient... recipients)
			throws Exception {
		final URL subjectUrl = (UriComponentsBuilder.fromUri(url.toURI()))
				.pathSegment("subject.txt").build().toUri().toURL();
		final URL senderUrl = (UriComponentsBuilder.fromUri(url.toURI()))
				.pathSegment("sender.txt").build().toUri().toURL();

		this.subject = IOUtils.toString(subjectUrl.openStream()).toString()
				.replaceAll("[\\r\\n]", "").trim();

		if (null == sender) {
			this.sender = IOUtils.toString(senderUrl.openStream()).toString()
					.replaceAll("[\\r\\n]", "").trim();
		} else {
			this.sender = sender;
		}

		this.htmlContent = new HtmlContent(url, containsContentDotHtml);

		this.smtp = smtp;
		this.recipients = recipients;
	}

	@Override
	public void run() {
		HtmlEmail email = new HtmlEmail();
		email.setHostName(smtp);

		try {
			for (Recipient recipient : recipients) {
				recipient.declareToIn(email);
			}

			email.setFrom(sender);
			email.setSubject(subject);

			// set the html message
			email.setHtmlMsg(htmlContent.cidifyImages(email));

			// set the alternative message
			email.setTextMsg("Your email client does not support HTML messages");

			// send the email
			email.send();
		} catch (Exception e) {
			throw new RuntimeException(
					"an error has occured while building or sending email", e);
		}
	}

}
