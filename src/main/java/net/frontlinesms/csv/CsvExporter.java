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
package net.frontlinesms.csv;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.frontlinesms.Utils;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.repository.*;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import org.apache.log4j.Logger;

import static net.frontlinesms.FrontlineSMSConstants.*;

/**
 * This file contains methods for exporting data from the FrontlineSMS service to CSV files.
 * @author Alex Anderson alex(at)masabi(dot)com
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com </li>
 */
public class CsvExporter {
	
//> STATIC CONSTANTS
	/** File extension for comma-separated value files */
	public static final String CSV_EXTENSION = ".csv";
	/** Logging object */
	private static Logger LOG = Utils.getLogger(CsvExporter.class);
			
	/** The delimiter to use between group names when they are exported. */
	private static final String GROUPS_DELIMITER = "\\"; 
	
//> UTILITY METHODS
	/**
	 * Exports the passed messages to a file.
	 * 
	 * @param exportFileName Filenane to be exported.
	 * @param messages List of messages to be exported.
	 * @param contactFactory 
	 * @throws IOException
	 */
	public static void export(String exportFileName, List<? extends Message> messages, ContactDao contactFactory) throws IOException {
		export(new File(exportFileName), messages, null, null, contactFactory);
	}
	
	/**
	 * Exports the passed messages to a file, using the message and date formats
	 * informed as well.
	 * 
	 * @param exportFile Filename to be exported.
	 * @param messages List of messages to be exported.
	 * @param messageFormat The desired message format, if null, the default will be used.
	 * @param dateFormat The desired date format, if null, the default will be used.
	 * @param contactFactory 
	 * @throws IOException
	 */
	public static void export(File exportFile, List<? extends Message> messages, CsvRowFormat messageFormat, String dateFormat, ContactDao contactFactory) throws IOException {
		LOG.trace("ENTER");
		if (messageFormat == null) messageFormat = getDefaultMessageExportFormat();
		if (dateFormat == null) dateFormat = InternationalisationUtils.getI18NString(DEFAULT_EXPORT_DATE_FORMAT);
		LOG.debug("Message format [" + messageFormat + "]");
		LOG.debug("Date format [" + dateFormat + "]");
		LOG.debug("Filename [" + exportFile.getAbsolutePath() + "]");

		Utf8FileWriter out = null;
		try {
			DateFormat dateFormatter = new SimpleDateFormat(dateFormat);
			
			out = new Utf8FileWriter(exportFile);
			
			CsvUtils.writeLine(out, messageFormat, 
					CsvUtils.MARKER_MESSAGE_DATE,		/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_DATE),
					CsvUtils.MARKER_SENDER_NAME, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_SENDER_NAME),
					CsvUtils.MARKER_SENDER_NUMBER,		/*->*/ InternationalisationUtils.getI18NString(COMMON_SENDER_NUMBER),
					CsvUtils.MARKER_RECIPIENT_NAME,		/*->*/ InternationalisationUtils.getI18NString(COMMON_RECIPIENT_NAME),
					CsvUtils.MARKER_RECIPIENT_NUMBER,	/*->*/ InternationalisationUtils.getI18NString(COMMON_RECIPIENT_NUMBER),
					CsvUtils.MARKER_MESSAGE_CONTENT,		/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_CONTENT));
			for (Message message : messages) {
				Contact sender = contactFactory.getFromMsisdn(message.getSenderMsisdn());
				String senderName = sender == null ? "" : sender.getName();
				Contact recipient = contactFactory.getFromMsisdn(message.getRecipientMsisdn());
				String recipientName = recipient == null ? "" : recipient.getName();
				CsvUtils.writeLine(out, messageFormat, 
					CsvUtils.MARKER_MESSAGE_DATE,		/*->*/ dateFormatter.format(new Date(message.getDate())),
					CsvUtils.MARKER_SENDER_NAME,			/*->*/ senderName,
					CsvUtils.MARKER_SENDER_NUMBER,		/*->*/ message.getSenderMsisdn(),
					CsvUtils.MARKER_RECIPIENT_NAME,		/*->*/ recipientName,
					CsvUtils.MARKER_RECIPIENT_NUMBER,	/*->*/ message.getSenderMsisdn(),
					CsvUtils.MARKER_MESSAGE_CONTENT,		/*->*/ message.getTextContent().replace('\n', ' ').replace('\r', ' '));
			}
		} finally {
			if (out != null) out.close();
			LOG.trace("EXIT"); 
		}
	}

	/**
	 * Exports the passed messages to a file, using the given format.
	 * 
	 * @param exportFile Filenane to be exported.
	 * @param messages List of messages to be exported.
	 * @param messageFormat The desired message format.
	 * @param contactFactory 
	 * @throws IOException
	 */
	public static void exportMessages(File exportFile, Collection<Message> messages, CsvRowFormat messageFormat, ContactDao contactFactory) throws IOException {
		LOG.trace("ENTER : messages: " + messages.size());
		LOG.debug("Message format [" + messageFormat + "]");
		LOG.debug("Filename [" + exportFile.getAbsolutePath() + "]");
		
		Utf8FileWriter out = null;
	
		try {
			DateFormat dateFormatter = new SimpleDateFormat(InternationalisationUtils.getI18NString(DEFAULT_EXPORT_DATE_FORMAT));
			out = new Utf8FileWriter(exportFile);
			CsvUtils.writeLine(out, messageFormat,
					CsvUtils.MARKER_MESSAGE_TYPE, InternationalisationUtils.getI18NString(COMMON_MESSAGE_TYPE),
					CsvUtils.MARKER_MESSAGE_STATUS, InternationalisationUtils.getI18NString(COMMON_MESSAGE_STATUS),
					CsvUtils.MARKER_MESSAGE_DATE, InternationalisationUtils.getI18NString(COMMON_MESSAGE_DATE),
					CsvUtils.MARKER_MESSAGE_CONTENT, InternationalisationUtils.getI18NString(COMMON_MESSAGE_CONTENT),
					CsvUtils.MARKER_SENDER_NUMBER, InternationalisationUtils.getI18NString(COMMON_SENDER_NUMBER),
					CsvUtils.MARKER_RECIPIENT_NUMBER, InternationalisationUtils.getI18NString(COMMON_RECIPIENT_NUMBER),
					CsvUtils.MARKER_CONTACT_NAME, InternationalisationUtils.getI18NString(COMMON_CONTACT_NAME),
					CsvUtils.MARKER_CONTACT_OTHER_PHONE, InternationalisationUtils.getI18NString(COMMON_CONTACT_OTHER_PHONE_NUMBER),
					CsvUtils.MARKER_CONTACT_EMAIL, InternationalisationUtils.getI18NString(COMMON_CONTACT_E_MAIL_ADDRESS),
					CsvUtils.MARKER_CONTACT_NOTES, InternationalisationUtils.getI18NString(COMMON_CONTACT_NOTES));
			for (Message message : messages) {
				Contact c;
				if (message.getType() == Message.TYPE_RECEIVED) {
					c = contactFactory.getFromMsisdn(message.getSenderMsisdn());
				} else {
					c = contactFactory.getFromMsisdn(message.getRecipientMsisdn());
				}
	
				String name = "";
				String otherPhone = "";
				String email = "";
				String notes = "";
	
				if (c != null) {
					name = c.getName();
					otherPhone = c.getOtherMsisdn();
					email = c.getEmailAddress();
					notes = c.getNotes();
				}
	
				CsvUtils.writeLine(out, messageFormat,
					CsvUtils.MARKER_MESSAGE_TYPE, message.getType() == Message.TYPE_RECEIVED ? InternationalisationUtils.getI18NString(COMMON_RECEIVED) : InternationalisationUtils.getI18NString(COMMON_SENT),
					CsvUtils.MARKER_MESSAGE_STATUS, UiGeneratorController.getMessageStatusAsString(message),
					CsvUtils.MARKER_MESSAGE_DATE, dateFormatter.format(new Date(message.getDate())),
					CsvUtils.MARKER_MESSAGE_CONTENT, message.getTextContent().replace('\n', ' ').replace('\r', ' '),
					CsvUtils.MARKER_SENDER_NUMBER, message.getSenderMsisdn(),
					CsvUtils.MARKER_RECIPIENT_NUMBER, message.getRecipientMsisdn(),
					CsvUtils.MARKER_CONTACT_NAME, name,
					CsvUtils.MARKER_CONTACT_OTHER_PHONE, otherPhone,
					CsvUtils.MARKER_CONTACT_EMAIL, email,
					CsvUtils.MARKER_CONTACT_NOTES, notes);
			}
		} finally {
			if (out != null) out.close();
			LOG.trace("EXIT");
		}
	}

	/**
	 * Exports the passed contacts to a file, using the given format.
	 * 
	 * @param exportFile Filenane to be exported.
	 * @param contacts List of contacts to be exported.
	 * @param contactFormat The desired contact format.
	 * @throws IOException
	 */
	public static void exportContacts(File exportFile, List<? extends Contact> contacts, CsvRowFormat contactFormat) throws IOException {
		LOG.trace("ENTER");
		LOG.debug("Contact format [" + contactFormat + "]");
		LOG.debug("Filename [" + exportFile.getAbsolutePath() + "]");
	
		Utf8FileWriter out = null;
		
		try {
			out = new Utf8FileWriter(exportFile);
			CsvUtils.writeLine(out, contactFormat,
					CsvUtils.MARKER_CONTACT_NAME, InternationalisationUtils.getI18NString(COMMON_NAME),
					CsvUtils.MARKER_CONTACT_PHONE, InternationalisationUtils.getI18NString(COMMON_PHONE_NUMBER),
					CsvUtils.MARKER_CONTACT_OTHER_PHONE, InternationalisationUtils.getI18NString(COMMON_OTHER_PHONE_NUMBER),
					CsvUtils.MARKER_CONTACT_EMAIL, InternationalisationUtils.getI18NString(COMMON_E_MAIL_ADDRESS),
					CsvUtils.MARKER_CONTACT_STATUS, InternationalisationUtils.getI18NString(COMMON_CURRENT_STATUS),
					CsvUtils.MARKER_CONTACT_NOTES, InternationalisationUtils.getI18NString(COMMON_NOTES),
					CsvUtils.MARKER_CONTACT_GROUPS, InternationalisationUtils.getI18NString(COMMON_AT_LEAST_ONE_GROUP));
			for (Contact contact : contacts) {
				CsvUtils.writeLine(out, contactFormat, 
					CsvUtils.MARKER_CONTACT_NAME, contact.getName(),
					CsvUtils.MARKER_CONTACT_PHONE, contact.getMsisdn(),
					CsvUtils.MARKER_CONTACT_OTHER_PHONE, contact.getOtherMsisdn(),
					CsvUtils.MARKER_CONTACT_EMAIL, contact.getEmailAddress(),
					CsvUtils.MARKER_CONTACT_STATUS, Boolean.toString(contact.isActive()),
					CsvUtils.MARKER_CONTACT_NOTES, contact.getNotes(),
					CsvUtils.MARKER_CONTACT_GROUPS, Utils.contactGroupsAsString(contact, GROUPS_DELIMITER));
			}
		} finally {
			if(out!= null) out.close();
			LOG.trace("EXIT");
		}
	}

	/**
	 * Exports the passed keywords to a file, using the given format.
	 * 
	 * @param exportFile Filename to be exported.
	 * @param keywords List of keywords to be exported.
	 * @param rowFormat The desired message format.
	 * @param contactFactory 
	 * @param messageFactory 
	 * @param messageType 
	 * @throws IOException
	 */
	public static void exportKeywords(File exportFile, List<? extends Keyword> keywords, CsvRowFormat rowFormat, ContactDao contactFactory, MessageDao messageFactory, int messageType) throws IOException {
		LOG.trace("ENTER");
		LOG.debug("Keyword format [" + rowFormat + "]");
		LOG.debug("Filename [" + exportFile.getAbsolutePath() + "]");

		
		Utf8FileWriter out = null;
		
		try {
			DateFormat dateFormatter = new SimpleDateFormat(InternationalisationUtils.getI18NString(DEFAULT_EXPORT_DATE_FORMAT));
			out = new Utf8FileWriter(exportFile);
			CsvUtils.writeLine(out, rowFormat, 
					CsvUtils.MARKER_KEYWORD_KEY, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_KEYWORD),
					CsvUtils.MARKER_KEYWORD_DESCRIPTION, /*->*/ InternationalisationUtils.getI18NString(COMMON_KEYWORD_DESCRIPTION),
					CsvUtils.MARKER_MESSAGE_TYPE, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_TYPE),
					CsvUtils.MARKER_MESSAGE_DATE, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_DATE),
					CsvUtils.MARKER_MESSAGE_CONTENT, 	/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_CONTENT),
					CsvUtils.MARKER_SENDER_NUMBER, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_SENDER),
					CsvUtils.MARKER_RECIPIENT_NUMBER, 	/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_RECIPIENT),
					CsvUtils.MARKER_CONTACT_NAME, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_CONTACT_NAME),
					CsvUtils.MARKER_CONTACT_OTHER_PHONE, /*->*/ InternationalisationUtils.getI18NString(COMMON_CONTACT_OTHER_PHONE_NUMBER),
					CsvUtils.MARKER_CONTACT_EMAIL, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_CONTACT_E_MAIL_ADDRESS),
					CsvUtils.MARKER_CONTACT_NOTES, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_CONTACT_NOTES));
			for (Keyword keyword : keywords) {
				if (messageType == -1) {
					// User dont want any message from this keywords.
					CsvUtils.writeLine(out, rowFormat, 
							CsvUtils.MARKER_KEYWORD_KEY, 		/*->*/ keyword.getKeyword(),
							CsvUtils.MARKER_KEYWORD_DESCRIPTION, /*->*/ keyword.getDescription());
				} else {
					for (Message message : messageFactory.getMessagesForKeyword(messageType, keyword)) {
						Contact c;
						if (message.getType() == Message.TYPE_RECEIVED) {
							c = contactFactory.getFromMsisdn(message.getSenderMsisdn());
						} else {
							c = contactFactory.getFromMsisdn(message.getRecipientMsisdn());
						}
	
						String name = "";
						String otherPhone = "";
						String email = "";
						String notes = "";
	
						if (c != null) {
							name = c.getName();
							otherPhone = c.getOtherMsisdn();
							email = c.getEmailAddress();
							notes = c.getNotes();
						}
						
						CsvUtils.writeLine(out, rowFormat, 
									CsvUtils.MARKER_KEYWORD_KEY,			/*->*/ keyword.getKeyword(),
									CsvUtils.MARKER_KEYWORD_DESCRIPTION,	/*->*/ keyword.getDescription(),
									CsvUtils.MARKER_MESSAGE_TYPE,		/*->*/ message.getType() == Message.TYPE_RECEIVED ? InternationalisationUtils.getI18NString(COMMON_RECEIVED) : InternationalisationUtils.getI18NString(COMMON_SENT),
									CsvUtils.MARKER_MESSAGE_DATE, 		/*->*/ dateFormatter.format(new Date(message.getDate())),
									CsvUtils.MARKER_MESSAGE_CONTENT, 	/*->*/ message.getTextContent().replace('\n', ' ').replace('\r', ' '),
									CsvUtils.MARKER_SENDER_NUMBER,		/*->*/ message.getSenderMsisdn(),
									CsvUtils.MARKER_RECIPIENT_NUMBER,	/*->*/ message.getRecipientMsisdn(),
									CsvUtils.MARKER_CONTACT_NAME,		/*->*/ name,
									CsvUtils.MARKER_CONTACT_OTHER_PHONE, /*->*/ otherPhone,
									CsvUtils.MARKER_CONTACT_EMAIL,		/*->*/ email,
									CsvUtils.MARKER_CONTACT_NOTES,		/*->*/ notes);
					}
				}
			}
		} finally {
			if (out != null) out.close();
			LOG.trace("EXIT");
		}
	}
	
	/** @return the default pattern for exporting messages */
	private static final CsvRowFormat getDefaultMessageExportFormat() {
		CsvRowFormat rowFormat = new CsvRowFormat();
		rowFormat.addMarker(CsvUtils.MARKER_SENDER_NUMBER);
		rowFormat.addMarker(CsvUtils.MARKER_SENDER_NAME);
		rowFormat.addMarker(CsvUtils.MARKER_RECIPIENT_NUMBER);
		rowFormat.addMarker(CsvUtils.MARKER_RECIPIENT_NAME);
		rowFormat.addMarker(CsvUtils.MARKER_MESSAGE_CONTENT);
		rowFormat.addMarker(CsvUtils.MARKER_MESSAGE_DATE);
		return rowFormat;
	}
}