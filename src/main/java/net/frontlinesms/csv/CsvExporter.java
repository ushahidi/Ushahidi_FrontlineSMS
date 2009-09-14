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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.repository.*;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import org.apache.log4j.Logger;

import static net.frontlinesms.FrontlineSMSConstants.*;

/**
 * This file contains methods for exporting data from the FrontlineSMS service
 * to CSV files.
 * 
 * FIXME 
 * 
 * @author Alex Anderson alex(at)masabi(dot)com
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com </li>
 */
public class CsvExporter {	
	private static final String DEFAULT_MESSAGE_EXPORT_FORMAT =
		MARKER_SENDER_NUMBER + "," +
		MARKER_SENDER_NAME + "," + 
		MARKER_RECIPIENT_NUMBER + "," + 
		MARKER_RECIPIENT_NAME + "," + 
		MARKER_MESSAGE_CONTENT + "," +
		MARKER_MESSAGE_DATE;
	private static final String GROUPS_DELIMITER = "\\";
	
	private static Logger LOG = Utils.getLogger(CsvExporter.class); 
	
	/**
	 * Exports the passed messages to a file.
	 * 
	 * @param exportFileName Filenane to be exported.
	 * @param messages List of messages to be exported.
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
	 * @throws IOException
	 */
	public static void export(File exportFile, List<? extends Message> messages, String messageFormat, String dateFormat, ContactDao contactFactory) throws IOException {
		LOG.trace("ENTER");
		if (messageFormat == null) messageFormat = DEFAULT_MESSAGE_EXPORT_FORMAT;
		if (dateFormat == null) dateFormat = InternationalisationUtils.getI18NString(DEFAULT_EXPORT_DATE_FORMAT);
		LOG.debug("Message format [" + messageFormat + "]");
		LOG.debug("Date format [" + dateFormat + "]");
		LOG.debug("Filename [" + exportFile.getAbsolutePath() + "]");
		FileWriter fileWriter = null;
		BufferedWriter writer = null;

		DateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		fileWriter = new FileWriter(exportFile);
		writer = new BufferedWriter(fileWriter);
		
		CsvUtils.writeLine(writer, messageFormat, 
				MARKER_MESSAGE_DATE,		/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_DATE),
				MARKER_SENDER_NAME, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_SENDER_NAME),
				MARKER_SENDER_NUMBER,		/*->*/ InternationalisationUtils.getI18NString(COMMON_SENDER_NUMBER),
				MARKER_RECIPIENT_NAME,		/*->*/ InternationalisationUtils.getI18NString(COMMON_RECIPIENT_NAME),
				MARKER_RECIPIENT_NUMBER,	/*->*/ InternationalisationUtils.getI18NString(COMMON_RECIPIENT_NUMBER),
				MARKER_MESSAGE_CONTENT,		/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_CONTENT));
		for (Message message : messages) {
			Contact sender = contactFactory.getFromMsisdn(message.getSenderMsisdn());
			String senderName = sender == null ? "" : sender.getName();
			Contact recipient = contactFactory.getFromMsisdn(message.getRecipientMsisdn());
			String recipientName = recipient == null ? "" : recipient.getName();
			CsvUtils.writeLine(writer, messageFormat, 
				MARKER_MESSAGE_DATE,		/*->*/ dateFormatter.format(new Date(message.getDate())),
				MARKER_SENDER_NAME,			/*->*/ senderName,
				MARKER_SENDER_NUMBER,		/*->*/ message.getSenderMsisdn(),
				MARKER_RECIPIENT_NAME,		/*->*/ recipientName,
				MARKER_RECIPIENT_NUMBER,	/*->*/ message.getSenderMsisdn(),
				MARKER_MESSAGE_CONTENT,		/*->*/ message.getTextContent().replace('\n', ' ').replace('\r', ' '));
		}

		if (writer != null) writer.close(); 
		if (fileWriter != null) fileWriter.close(); 
		LOG.trace("EXIT");
	}

	/**
	 * Exports the passed messages to a file, using the given format.
	 * 
	 * @param exportFile Filenane to be exported.
	 * @param messages List of messages to be exported.
	 * @param messageFormat The desired message format.
	 * @throws IOException
	 */
	public static void exportMessages(File exportFile, Collection<Message> messages, String messageFormat, ContactDao contactFactory) throws IOException {
		LOG.trace("ENTER : messages: " + messages.size());
		LOG.debug("Message format [" + messageFormat + "]");
		LOG.debug("Filename [" + exportFile.getAbsolutePath() + "]");
		
		FileWriter fileWriter = null;
		BufferedWriter writer = null;
	
		DateFormat dateFormatter = new SimpleDateFormat(InternationalisationUtils.getI18NString(DEFAULT_EXPORT_DATE_FORMAT));
		fileWriter = new FileWriter(exportFile);
		writer = new BufferedWriter(fileWriter);
		CsvUtils.writeLine(writer, messageFormat,
				MARKER_MESSAGE_TYPE, InternationalisationUtils.getI18NString(COMMON_MESSAGE_TYPE),
				MARKER_MESSAGE_STATUS, InternationalisationUtils.getI18NString(COMMON_MESSAGE_STATUS),
				MARKER_MESSAGE_DATE, InternationalisationUtils.getI18NString(COMMON_MESSAGE_DATE),
				MARKER_MESSAGE_CONTENT, InternationalisationUtils.getI18NString(COMMON_MESSAGE_CONTENT),
				MARKER_SENDER_NUMBER, InternationalisationUtils.getI18NString(COMMON_SENDER_NUMBER),
				MARKER_RECIPIENT_NUMBER, InternationalisationUtils.getI18NString(COMMON_RECIPIENT_NUMBER),
				MARKER_CONTACT_NAME, InternationalisationUtils.getI18NString(COMMON_CONTACT_NAME),
				MARKER_CONTACT_OTHER_PHONE, InternationalisationUtils.getI18NString(COMMON_CONTACT_OTHER_PHONE_NUMBER),
				MARKER_CONTACT_EMAIL, InternationalisationUtils.getI18NString(COMMON_CONTACT_E_MAIL_ADDRESS),
				MARKER_CONTACT_NOTES, InternationalisationUtils.getI18NString(COMMON_CONTACT_NOTES));
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

			CsvUtils.writeLine(writer, messageFormat,
				MARKER_MESSAGE_TYPE, message.getType() == Message.TYPE_RECEIVED ? InternationalisationUtils.getI18NString(COMMON_RECEIVED) : InternationalisationUtils.getI18NString(COMMON_SENT),
				MARKER_MESSAGE_STATUS, UiGeneratorController.getMessageStatusAsString(message.getStatus()),
				MARKER_MESSAGE_DATE, dateFormatter.format(new Date(message.getDate())),
				MARKER_MESSAGE_CONTENT, message.getTextContent().replace('\n', ' ').replace('\r', ' '),
				MARKER_SENDER_NUMBER, message.getSenderMsisdn(),
				MARKER_RECIPIENT_NUMBER, message.getRecipientMsisdn(),
				MARKER_CONTACT_NAME, name,
				MARKER_CONTACT_OTHER_PHONE, otherPhone,
				MARKER_CONTACT_EMAIL, email,
				MARKER_CONTACT_NOTES, notes);
		}
		if (writer != null) writer.close();
		if (fileWriter != null) fileWriter.close();
		LOG.trace("EXIT");
	}

	/**
	 * Exports the passed contacts to a file, using the given format.
	 * 
	 * @param exportFile Filenane to be exported.
	 * @param contacts List of contacts to be exported.
	 * @param contactFormat The desired contact format.
	 * @throws IOException
	 */
	public static void exportContacts(File exportFile, List<? extends Contact> contacts, String contactFormat) throws IOException {
		LOG.trace("ENTER");
		LOG.debug("Contact format [" + contactFormat + "]");
		LOG.debug("Filename [" + exportFile.getAbsolutePath() + "]");
		
		FileWriter fileWriter = null;
		BufferedWriter writer = null;
	
		fileWriter = new FileWriter(exportFile);
		writer = new BufferedWriter(fileWriter);
		CsvUtils.writeLine(writer, contactFormat,
				MARKER_CONTACT_NAME, InternationalisationUtils.getI18NString(COMMON_NAME),
				MARKER_CONTACT_PHONE, InternationalisationUtils.getI18NString(COMMON_PHONE_NUMBER),
				MARKER_CONTACT_OTHER_PHONE, InternationalisationUtils.getI18NString(COMMON_OTHER_PHONE_NUMBER),
				MARKER_CONTACT_EMAIL, InternationalisationUtils.getI18NString(COMMON_E_MAIL_ADDRESS),
				MARKER_CONTACT_STATUS, InternationalisationUtils.getI18NString(COMMON_CURRENT_STATUS),
				MARKER_CONTACT_NOTES, InternationalisationUtils.getI18NString(COMMON_NOTES),
				MARKER_CONTACT_GROUPS, InternationalisationUtils.getI18NString(COMMON_AT_LEAST_ONE_GROUP));
		for (Contact contact : contacts) {
			CsvUtils.writeLine(writer, contactFormat, 
				MARKER_CONTACT_NAME, contact.getName(),
				MARKER_CONTACT_PHONE, contact.getMsisdn(),
				MARKER_CONTACT_OTHER_PHONE, contact.getOtherMsisdn(),
				MARKER_CONTACT_EMAIL, contact.getEmailAddress(),
				MARKER_CONTACT_STATUS, contact.isActive() == true ? InternationalisationUtils.getI18NString(COMMON_ACTIVE) : InternationalisationUtils.getI18NString(COMMON_DORMANT),
				MARKER_CONTACT_NOTES, contact.getNotes(),
				MARKER_CONTACT_GROUPS, Utils.contactGroupsAsString(contact, GROUPS_DELIMITER));
		}
		if (writer != null) writer.close();
		if (fileWriter != null) fileWriter.close();
		LOG.trace("EXIT");
	}

	/**
	 * Exports the passed keywords to a file, using the given format.
	 * 
	 * @param exportFile Filename to be exported.
	 * @param keywords List of keywords to be exported.
	 * @param rowFormat The desired message format.
	 * @throws IOException
	 */
	public static void exportKeywords(File exportFile, List<? extends Keyword> keywords, String rowFormat, ContactDao contactFactory, MessageDao messageFactory, int messageType) throws IOException {
		LOG.trace("ENTER");
		LOG.debug("Keyword format [" + rowFormat + "]");
		LOG.debug("Filename [" + exportFile.getAbsolutePath() + "]");
		
		FileWriter fileWriter = null;
		BufferedWriter writer = null;
	
		DateFormat dateFormatter = new SimpleDateFormat(InternationalisationUtils.getI18NString(DEFAULT_EXPORT_DATE_FORMAT));
		fileWriter = new FileWriter(exportFile);
		writer = new BufferedWriter(fileWriter);
		CsvUtils.writeLine(writer, rowFormat, 
				MARKER_KEYWORD_KEY, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_KEYWORD),
				MARKER_KEYWORD_DESCRIPTION, /*->*/ InternationalisationUtils.getI18NString(COMMON_KEYWORD_DESCRIPTION),
				MARKER_MESSAGE_TYPE, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_TYPE),
				MARKER_MESSAGE_DATE, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_DATE),
				MARKER_MESSAGE_CONTENT, 	/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_CONTENT),
				MARKER_SENDER_NUMBER, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_SENDER),
				MARKER_RECIPIENT_NUMBER, 	/*->*/ InternationalisationUtils.getI18NString(COMMON_MESSAGE_RECIPIENT),
				MARKER_CONTACT_NAME, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_CONTACT_NAME),
				MARKER_CONTACT_OTHER_PHONE, /*->*/ InternationalisationUtils.getI18NString(COMMON_CONTACT_OTHER_PHONE_NUMBER),
				MARKER_CONTACT_EMAIL, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_CONTACT_E_MAIL_ADDRESS),
				MARKER_CONTACT_NOTES, 		/*->*/ InternationalisationUtils.getI18NString(COMMON_CONTACT_NOTES));
		for (Keyword keyword : keywords) {
			if (messageType == -1) {
				// User dont want any message from this keywords.
				CsvUtils.writeLine(writer, rowFormat, 
						MARKER_KEYWORD_KEY, 		/*->*/ keyword.getKeywordString(),
						MARKER_KEYWORD_DESCRIPTION, /*->*/ keyword.getDescription());
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
					
					CsvUtils.writeLine(writer, rowFormat, 
								MARKER_KEYWORD_KEY,			/*->*/ keyword.getKeywordString(),
								MARKER_KEYWORD_DESCRIPTION,	/*->*/ keyword.getDescription(),
								MARKER_MESSAGE_TYPE,		/*->*/ message.getType() == Message.TYPE_RECEIVED ? InternationalisationUtils.getI18NString(COMMON_RECEIVED) : InternationalisationUtils.getI18NString(COMMON_SENT),
								MARKER_MESSAGE_DATE, 		/*->*/ dateFormatter.format(new Date(message.getDate())),
								MARKER_MESSAGE_CONTENT, 	/*->*/ message.getTextContent().replace('\n', ' ').replace('\r', ' '),
								MARKER_SENDER_NUMBER,		/*->*/ message.getSenderMsisdn(),
								MARKER_RECIPIENT_NUMBER,	/*->*/ message.getRecipientMsisdn(),
								MARKER_CONTACT_NAME,		/*->*/ name,
								MARKER_CONTACT_OTHER_PHONE, /*->*/ otherPhone,
								MARKER_CONTACT_EMAIL,		/*->*/ email,
								MARKER_CONTACT_NOTES,		/*->*/ notes);
				}
			}
		}
		if (writer != null) writer.close();
		if (fileWriter != null) fileWriter.close();
		LOG.trace("EXIT");
	}
}
