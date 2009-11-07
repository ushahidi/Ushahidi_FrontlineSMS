/**
 * 
 */
package net.frontlinesms.email.pop;

import java.io.IOException;

import javax.mail.*;

import net.frontlinesms.junit.BaseTestCase;

/**
 * Unit tests for the {@link PopMessageReceiver} class.
 * 
 * @author Alex
 */
public class PopUtilsTest extends BaseTestCase {
	/**
	 * Unit tests for {@link PopUtils#getMessageText(javax.mail.Multipart, String)}.
	 * @throws MessagingException 
	 * @throws IOException 
	 */
	public void testGetMessageTextFromMultipart() throws MessagingException, IOException {
		String content = "Message Content";
		String desiredContentType = "text/plain";
		String invalidContentType = "text/html";
		MockMailMultipart mult = new MockMailMultipart();
		MockBodyPart bp = new MockBodyPart(content, invalidContentType);	
		mult.addBodyPart(bp);
		bp = new MockBodyPart(content, desiredContentType);
		mult.addBodyPart(bp);
		String text = PopUtils.getMessageText(mult, desiredContentType);
		assertEquals("Checking getting a text from a multipart " + invalidContentType + " and " + desiredContentType + " content", content, text);
		
		// Reverse the order
		mult = new MockMailMultipart();
		bp = new MockBodyPart(content, desiredContentType);
		mult.addBodyPart(bp);
		bp = new MockBodyPart(content, invalidContentType);
		mult.addBodyPart(bp);
		text = PopUtils.getMessageText(mult, desiredContentType);
		assertEquals("Checking getting a text from a multipart " + desiredContentType + " and " + invalidContentType + " content", content, text);
		
		// Empty multipart
		mult = new MockMailMultipart();
		text = PopUtils.getMessageText(mult, desiredContentType);
		assertNull("Checking getting a text from a multipart without content", text);
		
		// Plain text
		mult = new MockMailMultipart();
		bp = new MockBodyPart(content, desiredContentType);
		mult.addBodyPart(bp);
		text = PopUtils.getMessageText(mult, desiredContentType);
		assertEquals("Checking getting a text from a multipart " + desiredContentType, content, text);
		
		// Html
		mult = new MockMailMultipart();
		bp = new MockBodyPart(content, invalidContentType);
		mult.addBodyPart(bp);
		text = PopUtils.getMessageText(mult, desiredContentType);
		assertNull("Checking getting a text from a multipart " + invalidContentType, text);
	}
	
	/**
	 * Unit tests for {@link PopUtils#getSender(javax.mail.Message)}.
	 * @throws MessagingException 
	 */
	public void testGetSender() throws MessagingException {
		// Getting from "from" list.
		MockMailAddress addr = new MockMailAddress("test@masabi.com");
		MockMailMessage message = new MockMailMessage();
		message.addFrom(new Address[] {addr});
		String sender = PopUtils.getSender(message);
		assertEquals("Checking getting sender from 'from' list. 'Reply to' empty.", sender, addr.toString());	
		
		// Getting from "reply to" list.
		addr = new MockMailAddress("test@masabi.com");
		message = new MockMailMessage();
		message.setReplyTo(new Address[] {addr});
		sender = PopUtils.getSender(message);
		assertEquals("Checking getting sender from 'reply to' list. 'From' empty.", sender, addr.toString());	
		
		// Empty lists.
		message = new MockMailMessage();
		sender = PopUtils.getSender(message);
		assertEquals("Checking getting sender from empty lists.", sender, "");
		
		// Null lists.
		message = new MockMailMessage();
		message.setFromList(null);
		message.setReplyToList(null);
		sender = PopUtils.getSender(message);
		assertEquals("Checking getting sender from null lists.", sender, "");
		
		// Using both lists.
		message = new MockMailMessage();
		MockMailAddress addr2 = new MockMailAddress("ah@masabi.com");
		message.addFrom(new Address[] {addr2, addr});
		message.setReplyTo(new Address[] {addr2, addr});
		sender = PopUtils.getSender(message);
		assertEquals("Checking getting sender from both lists not empty.", sender, addr2.toString());	
	}

	/**
	 * Unit tests for {@link PopUtils#getMessageText(javax.mail.Message)}.
	 * @throws MessagingException 
	 * @throws IOException 
	 */
	public void testGetMessageTextFromMessage() throws MessagingException, IOException {
		// This is testing getting text content from non-multipart messages. For multipart messages tests, refer to testGetMessageTextFromMultipart()
		String content = "Message Content";
		MockMailMessage message = new MockMailMessage();
		message.setContent(content, null);
		String text = PopUtils.getMessageText(message);
		assertEquals("Checking getting a text from a non-multipart message, with string content.", content, text);
		
		// No content
		message = new MockMailMessage();
		text = PopUtils.getMessageText(message);
		assertNull("Checking getting a text from a non-multipart message without content (null).", text);
		
		// Non-string content
		message = new MockMailMessage();
		message.setContent(new Integer(10), null);
		text = PopUtils.getMessageText(message);
		assertNull("Checking getting a text from a non-multipart message with a non-string content.", text);
	}
}
