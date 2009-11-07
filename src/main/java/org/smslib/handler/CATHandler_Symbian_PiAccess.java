/**
 * 
 */
package org.smslib.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.smslib.CSerialDriver;
import org.smslib.CService;
import org.smslib.SMSLibDeviceException;
import org.smslib.UnableToReconnectException;
import org.smslib.CService.MessageClass;

/**
 * {@link CATHandler} extension to work around foibles of the Symbian application piAccess.  This
 * app implements more extensive support for AT commands on Symbian phones.
 * @author Alex
 */
public class CATHandler_Symbian_PiAccess extends CATHandler {
//> STATIC CONSTANTS
	/** Max length of {@link #listMessagesLinesSeen} before none more can be added. */
	private static final int MAX_LISTMESSAGES_SEEN_LINES_SIZE = 500;

//> INSTANCE PROPERTIES
	/** Logging object */
	private CATHandler_Symbian_PiAccess log = this;
	/** Set containing hashes of the {@link #listMessages(MessageClass)} we have seen */
	private TreeSet<Integer> listMessagesLinesSeen = new TreeSet<Integer>();

//> CONSTRUCTORS
	/** @see CATHandler#CATHandler(CSerialDriver, Logger, CService) */
	public CATHandler_Symbian_PiAccess(CSerialDriver serialDriver, Logger log, CService srv) {
		super(serialDriver, log, srv);
	}

//> CATHandler OVERRIDES
	/** @see CATHandler#switchToCmdMode() */
	@Override
	protected void switchToCmdMode() throws IOException {
		// apparently piAccess begins in command mode, so this method does not need to do anything.
		log.debug("Not attempting to switch to command mode.");
		return;
	}
	
	/** @see CATHandler#keepGsmLinkOpen() */
	@Override
	protected boolean keepGsmLinkOpen() throws IOException {
		// piAccess does not support this command, so there's no point in sending it.  Let's assume
		// that the link is always open.
		log.debug("Not attempting to keep GSM link open.");
		return true;
	}
	
	/**
	 * This listMessages implementation is specially made for the piAccess handler.
	 * @throws IOException 
	 * @throws SMSLibDeviceException 
	 */
	@Override
	protected String listMessages(MessageClass messageClass) throws IOException, SMSLibDeviceException {
		log.trace("listMessages");
		
		String messageList = super.listMessages(messageClass);
		
		if(log.isDebugEnabled()) log.debug("returned: " + messageList);
		
		if(messageClass == MessageClass.UNREAD) {
			messageList = filterUnreadMessages(messageList);
		}
		
		return messageList;
	}

	/**
	 * PiAccess fails to delete messages or mark them as read.  It will repeatedly return the same message,
	 * so we must filter the list of messages when reading {@link MessageClass#UNREAD}.
	 * TODO this method should actually examine messageLines in pairs, otherwise it could cause some big issues 
	 * @param messageList
	 * @return all lines of the messageList that have not been seen before
	 * @throws UnableToReconnectException
	 * @throws IOException 
	 */
	private String filterUnreadMessages(String messageList) throws UnableToReconnectException, IOException {
		BufferedReader reader = new BufferedReader(new StringReader(messageList));
		StringBuilder bob = new StringBuilder();
		
		String line;
		// Read until the end of the response.  This is indicated by either a
		// null line, or a line reading "OK".
		// TODO this may cause some messages to be deleted by the user before they are processed.  Can't think of a simple way round this...
		while ((line = reader.readLine()) != null) {
			if(line.trim().length() > 0) {
				if(isListMessageLineUnseen(line)) {
					log.debug("Keeping message line: " + line);
					bob.append(line);
					bob.append('\n');
				} else {
					log.debug("Discarding message line: " + line);
				}
			}
		}
		
		log.debug("listMessages completed: " + bob.toString());
		
		return bob.toString();
	}
	
	/**
	 * Check if a line from 
	 * @param messageLine
	 * @return <code>true</code> if this {@link #listMessages(MessageClass)} line has not been seen by this handler instance before; <code>false</code> otherwise
	 * @throws UnableToReconnectException When the length of {@link #listMessagesLinesSeen} exceeds {@link #MAX_LISTMESSAGES_SEEN_LINES_SIZE}
	 */
	private boolean isListMessageLineUnseen(String messageLine) throws UnableToReconnectException {
		if(this.listMessagesLinesSeen.size() >= MAX_LISTMESSAGES_SEEN_LINES_SIZE) {
			log.info("Too many messages read from device.  Please clear device SMS memory manually.");
			throw new UnableToReconnectException("Too many messages read from device.  Please clear device SMS memory manually.");
		}
		boolean unseen = this.listMessagesLinesSeen.add(messageLine.hashCode());
		System.out.println("CATHandler_Symbian_PiAccess.isMessageUnseen() '" + messageLine + "' -> " + unseen);
		return unseen;
	}

	/**
	 * This deleteMessage implementation is specially made for the piAccess handler.  PiAccess fails to
	 * delete other messages, and will repeatedly return the same message 
	 */
	@Override
	protected boolean deleteMessage(int memIndex, String memLocation) throws IOException {
		// Let's pretend that we've deleted the message - doesn't seem to work at the moment
		// TODO may want to call super.deleteMessage() here in case piAccess is ever fixed.
		log.debug("Not attempting to delete messages.");
		return true;
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	
//> TEMP LOGGING METHODS
	/** @return true */
	public boolean isDebugEnabled() { return true; } 
	/** Logging method 
	 * @param message message to log */
	public void info(String message) {
		log("INFO", message);
	}
	/** Logging method 
	 * @param message message to log */
	public void trace(String message) {
		log("TRACE", message);
	}
	/** Logging method 
	 * @param message message to log */
	public void debug(String message) {
		log("DEBUG", message);
	}
	/** Logging method */
	private void log(String level, String message) {
		System.out.println("[" + level + "| " + Thread.currentThread().getName() + " | " + this.getClass() + "]" + message);
	}
}