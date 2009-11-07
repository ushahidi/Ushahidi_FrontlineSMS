/**
 * 
 */
package net.frontlinesms.email.pop;

import java.io.IOException;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

import net.frontlinesms.Utils;
import net.frontlinesms.smsdevice.internet.IntelliSmsInternetService;

import org.apache.log4j.Logger;

/**
 * Utility methods for doing common actions on POP {@link Message}s.
 * @author Alex
 */
public class PopUtils {
//> STATIC CONSTANTS
	/** Logging object */
	private static Logger LOG = Utils.getLogger(IntelliSmsInternetService.class);
	/** MIME Type for plain text */
	private static final String MIMETYPE_TEXT_PLAIN = "text/plain";

//> INSTANCE PROPERTIES

//> CONSTRUCTORS

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	/**
	 * Attempts to extract the message content from an email message.
	 * @param message
	 * @return The text content of the supplied email message, or <code>null</code> if none could be found.
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static String getMessageText(javax.mail.Message message) throws MessagingException, IOException {
		Object messageContent = message.getContent();
		if (messageContent instanceof String) {
			// We've got a simple text message, so just return the text
			return (String)messageContent;
		} else if (messageContent instanceof Multipart) {
			// We've got a multipart message, so we need to check through the parts to find the
			// most text-like part.
			Multipart multipart = (Multipart) messageContent;
			String messageText = getMessageText(multipart, MIMETYPE_TEXT_PLAIN);
			if(messageText == null) {
				// We haven't found plain text.  The following should match any HTML-based text content.
				messageText = getMessageText(multipart, "text");
			}
		}
		return null;
	}

	/**
	 * Gets the text content from the supplied multipart message content of an email which matches the supplied MIME type.
	 * @param multipartContent
	 * @param acceptableMimeType The acceptable start of the MIME type for the content we are searching for.
	 * @return The message content of the supplied multipart email message content, or <code>null</code> if no acceptable content could be found.
	 * @throws MessagingException 
	 * @throws IOException 
	 */
	public static String getMessageText(Multipart multipartContent, String acceptableMimeType) throws MessagingException, IOException {
		int partCount = multipartContent.getCount();
		for(int i=0; i<partCount; ++i) {
			BodyPart part = multipartContent.getBodyPart(i);
			String contentType = part.getContentType();
			if (contentType.startsWith(acceptableMimeType)) {
				Object content = part.getContent();
				if (content instanceof String) {
					return (String)content;
				} else {
					LOG.warn("Body part with content type '" + contentType + "' had unexpected Content type: " + content.getClass().getCanonicalName());
				}
			}
		}
		return null;
	}

	/**
	 * Attempts to get a sane value for the sender of an email.
	 * @param message
	 * @return The first from address of the message, if one is supplied.  Otherwise, the first replyTo address, if one is supplied.  Otherwise an empty string.
	 * @throws MessagingException 
	 */
	public static String getSender(javax.mail.Message message) throws MessagingException {
		// First, check the "from" addresses
		// TODO only call getFrom() once?
		if (message.getFrom() != null) {
			for(Address address : message.getFrom()) {
				if(address != null) {
					String sender = address.toString();
					if(sender != null) return sender;
				}
			}
		}
		
		// TODO only call getReplyTo() once?
		if (message.getReplyTo() != null) {
			// No "from" address was found, so check the "reply to" addresses
			for(Address address : message.getReplyTo()) {
				if(address != null) {
					String sender = address.toString();
					if(sender != null) return sender;
				}
			}
		}
		// No address could be found, so return empty.
		return "";
	}
}
