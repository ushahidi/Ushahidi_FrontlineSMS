/**
 * 
 */
package net.frontlinesms.email.pop;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import net.frontlinesms.Utils;
import net.frontlinesms.email.EmailFilter;

import org.apache.log4j.Logger;

import com.sun.mail.pop3.POP3SSLStore;
import com.sun.mail.pop3.POP3Store;

/**
 * Object that reads messages from a POP email account.
 * 
 * @author Alex
 *
 */
public class PopMessageReceiver {
	private static final String FOLDER_INBOX = "INBOX";
	private static Logger LOG = Utils.getLogger(PopMessageReceiver.class);
	/** Properties for connecting to the POP server. */
	private final Properties pop3Props = new Properties();
	/** Object that filters emails to reduce email spam. */
	private EmailFilter emailFilter;
	/** Object that will process received messages. */
	private final PopMessageProcessor processor;
	/** Flag indicating this should use SSL when connecting to the email server. */
	private boolean useSsl;
	/** Port to connect to on the POP server. */
	private int hostPort;
	/** Username on the POP server. */
	private String hostUsername;
	/** Password for accessing the POP server. */
	private String hostPassword;
	/** Address of the POP server. */
	private String hostAddress;

	public PopMessageReceiver(PopMessageProcessor processor) {
		if(processor == null) throw new IllegalArgumentException("Processor must not be null.");
		this.processor = processor;	
	}

	/**
	 * Blocking methods that attempts to read messages from a POP email account.
	 * @throws PopReceiveException If there was a problem receiving messages with this object.
	 */
	public void receive() throws PopReceiveException {
		LOG.trace("ENTER : " + hostUsername + "@" + hostAddress + ":" + hostPort);

		Store store = null;
		Folder folder = null;

		pop3Props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
		pop3Props.setProperty("mail.pop3.port", Integer.toString(hostPort));
		pop3Props.setProperty("mail.pop3.socketFactory.port", Integer.toString(hostPort));

		URLName url = new URLName("pop3", hostAddress, hostPort, "", hostUsername, hostPassword);

		Session session = Session.getInstance(pop3Props, null);

		LOG.trace("Using SSL: " + useSsl);
		if (useSsl) {
			store = new POP3SSLStore(session, url);
		} else {
			store = new POP3Store(session, url);
		}

		try {
			LOG.trace("Connecting to email store: " + hostAddress + ":" + hostPort);
			store.connect();

			// Get a handle on the INBOX folder.
			folder = store.getDefaultFolder().getFolder(FOLDER_INBOX);
			if(folder == null) throw new MessagingException("Inbox handle was null.");

			try {
				LOG.trace("Attempting to open folder for read/write.");
				folder.open(Folder.READ_WRITE);
			} catch (MessagingException ex) {
				LOG.trace("Opening folder for Read/write failed.  Attempting to open folder for read only.");
				folder.open(Folder.READ_ONLY);
			}

			Message[] messages = folder.getMessages();

			// Loop over all of the messages
			for (Message message : messages) {
				if(emailFilter == null || emailFilter.accept(message)) {
					if(emailFilter != null) LOG.info("Email accepted by filter.  Beginning processing.");
					try {
						// We've got a message, so try and find it's text content.
						String messageText = getMessageText(message);
						processor.processPopMessage(getSender(message), message.getSentDate(), message.getSubject(), messageText);
					} catch (Exception ex) {
						LOG.warn("Error processing email.");
					}
				} else {
					LOG.info("Email rejected by filter.");
				}
			}

			LOG.trace("EXIT : POP email account checked without error.");
		} catch(MessagingException ex) {
			LOG.error("Unable to connect to POP account.", ex);
			throw new PopReceiveException(ex);
		} finally {
			// Attempt to close our folder
			if(folder != null) try { folder.close(true); } catch(MessagingException ex) { LOG.warn("Error closing POP folder.", ex); }

			// Attempt to close the message store
			if(store != null) try { store.close(); } catch(MessagingException ex) { LOG.warn("Error closing POP store.", ex); }
		}
	}

	/**
	 * Attempts to extract the message content from an email message.
	 * @param message
	 * @return The text content of the supplied email message, or <code>null</code> if none could be found.
	 * @throws MessagingException
	 * @throws IOException
	 */
	static String getMessageText(Message message) throws MessagingException, IOException {
		Object messageContent = message.getContent();
		if (messageContent instanceof String) {
			// We've got a simple text message, so just return the text
			return (String)messageContent;
		} else if (messageContent instanceof Multipart) {
			// We've got a multipart message, so we need to check through the parts to find the
			// most text-like part.
			Multipart multipart = (Multipart) messageContent;
			String messageText = getMessageText(multipart, "text/plain");
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
	static String getMessageText(Multipart multipartContent, String acceptableMimeType) throws MessagingException, IOException {
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
	 * @return
	 * @throws MessagingException 
	 */
	static String getSender(Message message) throws MessagingException {
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

	/**
	 * @return the useSsl
	 */
	public boolean isUseSsl() {
		return useSsl;
	}

	/**
	 * @param useSsl the useSsl to set
	 */
	public void setUseSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}

	/**
	 * @return the hostPort
	 */
	public int getHostPort() {
		return hostPort;
	}

	/**
	 * @param hostPort the hostPort to set
	 */
	public void setHostPort(int hostPort) {
		this.hostPort = hostPort;
	}

	/**
	 * @return the hostUsername
	 */
	public String getHostUsername() {
		return hostUsername;
	}

	/**
	 * @param hostUsername the hostUsername to set
	 */
	public void setHostUsername(String hostUsername) {
		this.hostUsername = hostUsername;
	}

	/**
	 * @return the hostPassword
	 */
	public String getHostPassword() {
		return hostPassword;
	}

	/**
	 * @param hostPassword the hostPassword to set
	 */
	public void setHostPassword(String hostPassword) {
		this.hostPassword = hostPassword;
	}

	/**
	 * @return the hostAddress
	 */
	public String getHostAddress() {
		return hostAddress;
	}

	/**
	 * @param hostAddress the hostAddress to set
	 */
	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}
}
