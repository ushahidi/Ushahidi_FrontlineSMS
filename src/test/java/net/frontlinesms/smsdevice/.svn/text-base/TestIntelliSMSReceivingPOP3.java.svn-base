/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms.smsdevice;

import java.io.IOException;
import java.util.Properties;

import javax.mail.*;

import net.frontlinesms.*;

import com.sun.mail.pop3.POP3SSLStore;

/**
 * This tests IntelliSMS receiving sms, querying an e-mail box.
 *
 * @author Carlos Eduardo Endler Genz
 * @date 19/01/2009
 */
public class TestIntelliSMSReceivingPOP3 extends AbstractTestCase {

	/**
	 * Reads an specific pattern of e-mail subjects from a pop3 account, looking
	 * for IntelliSMS forwarded messages.
	 * 
	 * @throws MessagingException 
	 * @throws IOException 
	 */
	public void testReadSms() throws MessagingException, IOException {
		Session session = null;
		Store store = null;
		Folder folder = null;

		Properties pop3Props = new Properties();

		pop3Props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
		pop3Props.setProperty("mail.pop3.port", PROPERTIES.getProperty("intellisms.receive.host.port"));
		pop3Props.setProperty("mail.pop3.socketFactory.port", PROPERTIES.getProperty("intellisms.receive.host.port"));

		URLName url = new URLName(
				"pop3",
				PROPERTIES.getProperty("intellisms.receive.host"),
				Integer.valueOf(PROPERTIES.getProperty("intellisms.receive.host.port")),
				"",
				PROPERTIES.getProperty("intellisms.receive.username"),
				Utils.decodeBase64(PROPERTIES.getProperty("intellisms.receive.password"))
		);

		session = Session.getInstance(pop3Props, null);
		store = new POP3SSLStore(session, url);
		store.connect();

		if (store.getDefaultFolder().getFolder("INBOX") == null) 
			fail("Could not find the INBOX folder.");
		// Get a handle on the INBOX folder.
		folder = store.getDefaultFolder().getFolder("INBOX");
		try {
			folder.open(Folder.READ_WRITE);
		} catch (MessagingException ex) {
			folder.open(Folder.READ_ONLY);
		}

		// Loop over all of the messages
		for (Message message : folder.getMessages()) {
			// Received msg: Message from <sender>
			// Content contais the message text.
			/** Delivery report: 
			 * Subject -> Status report for message sent to <recipient>
			 * To number: 447988156550
			 * Status: Message has been delivered */
			String subject = message.getSubject();
			if (subject.startsWith("Message from")) {
				// We've got a message
				System.out.println("Subject: " + subject);
				assertEquals(subject.split(" ").length, 3);
				String from = subject.split(" ")[2];
				System.out.println("Message from: " + from);
				Object messageContent = message.getContent();
				if(messageContent instanceof Multipart) {
					Multipart obj = (Multipart) message.getContent();
					for (int i = 0; i < obj.getCount(); i++) {
						BodyPart part = obj.getBodyPart(i);
						if (part.getContentType().startsWith("text/plain")) {
							String msg = (String) part.getContent();
							System.out.println("Message content: " + msg);
						}
					}
				} else {
					System.out.println("Message content: " + (String)messageContent);
				}
			} else if (subject.startsWith("Status report for message sent to")) {
				// We've got a delivery report
				// TODO we need to get the msg id. Since it is not in the email.
				// We can get delivery reports polling IntelliSMS server with
				// the messages id.
			}
		}
		folder.close(true);
		// Close the message store
		store.close();
	}


}
