package com.emo.pitaya.email;

import java.net.URL;
import java.net.URLEncoder;

import org.junit.Test;

import com.emo.pitaya.email.EmailSendTask.Recipient;

public class EmailSendTaskTest {

	@Test
	public void test() throws Exception {
//		final EmailSendTask est = new EmailSendTask("smtp.orange.fr", "boufflers.cedric@orange.fr", new URL("http://process.orange.fr/options/mail_creation_avfw_1_poste.eml"), true, new Recipient("cedric.boufflers@gmail.com", "Boubou"), new Recipient("boufflers.cedric@orange.fr", "Boubou"));
	
		for(int i = 0; i < 1; ++i) {
			final EmailSendTask est = new EmailSendTask("smtp.orange.fr", "boufflers.cedric@orange.fr", new URL("http://localhost:8080/app/template/aaa/?toto=fdsdsssf&foo=" + URLEncoder.encode("{\"bar\":\"ereqd\"}", "UTF-8")), false, new Recipient("cedric.boufflers@gmail.com", "Boubou" + i), new Recipient("boufflers.cedric@orange.fr", "Boubou" + i));
			est.run();
		}
	}

}
