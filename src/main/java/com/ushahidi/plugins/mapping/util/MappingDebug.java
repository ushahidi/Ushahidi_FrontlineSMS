package com.ushahidi.plugins.mapping.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.data.repository.MessageDao;

import com.ushahidi.plugins.mapping.managers.FormsManager;
import com.ushahidi.plugins.mapping.managers.TextFormsManager;

/**
 * MappingDebug
 * @author dalezak
 *
 */
public class MappingDebug {
	
	private static MappingLogger LOG = MappingLogger.getLogger(MappingDebug.class);	
	
	private final FormsManager formsManager;
	private final TextFormsManager textformsManager;
	private final MessageDao messageDao;
	private final ContactDao contactDao;
	
	/**
	 * MappingDebug
	 * @param pluginController MappingPluginController
	 */
	public MappingDebug(FormsManager formsManager, TextFormsManager textformsManager, MessageDao messageDao, ContactDao contactDao) {
		this.formsManager = formsManager;
		this.textformsManager = textformsManager;
		this.messageDao = messageDao;
		this.contactDao = contactDao;
	}
	
	/**
	 * Start debug terminal
	 */
	public void startDebugTerminal() {
		Thread thread = new DebugTerminal();
		thread.start();
    }
	
	private String getSenderMsisdn() {
		List<Contact> contacts = this.contactDao.getAllContacts();
		for(int index = contacts.size() - 1; index >= 0; index--){
			Contact contact = contacts.get(index);
			if (contact.getPhoneNumber() != null) {
				return contact.getPhoneNumber();
			}
			else if (contact.getOtherPhoneNumber() != null) {
				return contact.getOtherPhoneNumber();
			}
		}
		return null;
	}
	
	/**
	 * Inner threaded class for listening to System.in
	 * @author dalezak
	 *
	 */
	private class DebugTerminal extends Thread {
		public void run() {
			List<String> exitKeywords = Arrays.asList("exit", "x", "quit", "q");
			LOG.error("Debug Terminal Started...");
	        Scanner scanner = new Scanner(System.in);
	        while(true) { 
	            String message = scanner.nextLine().trim();
	            String[] words = message.split(" ", 2);
	            if (exitKeywords.contains(message.toLowerCase())) {
	            	break;
	            }
	            else if (message.toLowerCase().startsWith("form")){
	            	String title = words.length > 1 ? words[1] : null;
	            	formsManager.addFormResponse(title);
	            	LOG.debug("Form Created: %s", title);
	            }
	            else if (message.toLowerCase().startsWith("textform")){
	            	String title = words.length > 1 ? words[1] : null;
	            	textformsManager.addTextFormAnswers(title);
	            	LOG.debug("TextForm Created: %s", title);
	            }
	            else if (message.equalsIgnoreCase("help")){
	            	LOG.debug("Enter 'form' to create a sample Form, 'textform' to create a sample TextForm or 'exit' to terminate console.");
	            }
	            else {
	            	long dateReceived = Calendar.getInstance().getTimeInMillis();
	            	String senderMsisdn = getSenderMsisdn();
	            	LOG.debug("Message Created: %s", message);
	            	messageDao.saveMessage(FrontlineMessage.createIncomingMessage(dateReceived, senderMsisdn, null, message));
	            }
	        }
		 }
	}
	
}