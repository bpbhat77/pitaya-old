package com.emo.pitaya.email;

import java.net.URL;

import org.junit.Test;

import com.emo.pitaya.email.EmailSendTask.Recipient;

public class EmailSendTaskTest {

	@Test
	public void test() throws Exception {
		final EmailSendTask est = new EmailSendTask("smtp.orange.fr", "boufflers.cedric@orange.fr", new URL("http://process.orange.fr/options/mail_creation_avfw_1_poste.eml"), true, new Recipient("cedric.boufflers@gmail.com", "Boubou"));
		est.run();
	}

}
