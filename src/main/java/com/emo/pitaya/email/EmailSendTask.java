package com.emo.pitaya.email;

import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.web.util.UriComponentsBuilder;

import com.emo.pitaya.email.html.UnloadedHtmlContent;

public class EmailSendTask implements Runnable {

	private final URL url;
	private final boolean useContentDotHtml;

	private final Recipient[] recipients;
	private final String smtp;

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

	public EmailSendTask(final String smtp, final String sender, final URL url,
			final boolean useContentDotHtml, final Recipient... recipients) {

		this.sender = sender;

		this.url = url;
		this.useContentDotHtml = useContentDotHtml;

		this.smtp = smtp;
		this.recipients = recipients;
	}

	@Override
	public void run() {
		HtmlEmail email = new HtmlEmail();
		email.setHostName(smtp);

		try {
			final String sender;

			if (this.sender != null) {
				sender = this.sender;
			} else {
				final URL senderUrl = (UriComponentsBuilder
						.fromUri(url.toURI())).pathSegment("sender.txt")
						.build().toUri().toURL();

				sender = IOUtils.toString(senderUrl.openStream()).toString()
						.replaceAll("[\\r\\n]", "").trim();
			}

			final URL subjectUrl = (UriComponentsBuilder.fromUri(url.toURI()))
					.pathSegment("subject.txt").build().toUri().toURL();

			final String subject = IOUtils.toString(subjectUrl.openStream())
					.toString().replaceAll("[\\r\\n]", "").trim();

			for (Recipient recipient : recipients) {
				recipient.declareToIn(email);
			}

			email.setFrom(sender);
			email.setSubject(subject);

			// set the html message
			email.setHtmlMsg(new UnloadedHtmlContent(url, useContentDotHtml)
					.load().cidify(email, true).toString());

			// set the alternative message
			email.setTextMsg("Your email client does not support HTML messages");

			// send the email
			email.send();
		} catch (Exception e) {
			throw new RuntimeException(
					"an error has occured while building or sending email : "
							+ e.getMessage(), e);
		}
	}

}
