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
package net.frontlinesms.data.domain;

import javax.persistence.*;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.csv.CsvExporter;
import net.frontlinesms.csv.CsvUtils;
import net.frontlinesms.data.EntityField;
import net.frontlinesms.data.domain.Contact.Field;

/**
 * @author Alex Anderson alex(at)masabi(dot)com
 */
@Entity
public class KeywordAction {
//> ENTITY FIELDS
	/** Details of the fields that this class has. */
	public enum Field implements EntityField<KeywordAction> {
		TYPE("type");
		/** name of a field */
		private final String fieldName;
		/**
		 * Creates a new {@link Field}
		 * @param fieldName name of the field
		 */
		Field(String fieldName) { this.fieldName = fieldName; }
		/** @see EntityField#getFieldName() */
		public String getFieldName() { return this.fieldName; }
	}
	
//> CONSTRUCTORS
	/** Default constructor, to be used by hibernate. */
	KeywordAction() {}
	
//> CONSTANTS
	/** Action: forward the received message to a group */
	public static final int TYPE_FORWARD = 0;
	/** Action: add the sender's msisdn to a group */
	public static final int TYPE_JOIN = 1;
	/** Action: remove the sender's msisdn from a group */
	public static final int TYPE_LEAVE = 2;
	/** Reply: send a specified reply to the sender's msisdn */
	public static final int TYPE_REPLY = 3;
	/** Survey: a non-action - doesn't do anything but prevents words from being deleted if there are no other actions attached */
	public static final int TYPE_SURVEY = 4;
	/** Action: executes an external command */
	public static final int TYPE_EXTERNAL_CMD = 5;
	/** Action: send an e-mail */
	public static final int TYPE_EMAIL = 6;
	
	// FIXME rename these - we can't have two types of types
	public static final int EXTERNAL_HTTP_REQUEST = 0;
	public static final int EXTERNAL_COMMAND_LINE = 1;
	
	public static final int EXTERNAL_RESPONSE_PLAIN_TEXT = 0;
	public static final int EXTERNAL_RESPONSE_LIST_COMMANDS = 1;
	public static final int EXTERNAL_RESPONSE_DONT_WAIT = 2;
	
	public static final int EXTERNAL_REPLY_AND_FORWARD = 1;
	public static final int EXTERNAL_DO_NOTHING = 2;
	
//> INSTANCE PROPERTIES
	/** Unique id for this entity.  This is for hibernate usage. */
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(unique=true,nullable=false,updatable=false) @SuppressWarnings("unused")
	private long id;
	private int type;
	/** Keyword which this action is attached to */
	@ManyToOne(fetch=FetchType.EAGER, targetEntity=Keyword.class, optional=false)
	private Keyword keyword;
	@ManyToOne(optional=true)
	private Group group;
	@ManyToOne(optional=true)
	private EmailAccount emailAccount;
	private String commandString;
	private int commandInteger;
	private int counter;
	private long startDate;
	private long endDate;
	private String emailRecipients;
	private String emailSubject;
	private int externalCommandType;
	private String externalCommand;
	private int externalCommandResponseType;
	private int externalCommandResponseActionType;
	
//> ACCESSOR METHODS
	/**
	 * Gets the type of this instance of KeywordAction.
	 * @return {@link #type}
	 */
	public int getType() {
		return this.type;
	}
	
	/**
	 * Gets the external command type of this instance of KeywordAction.
	 * @return
	 */
	public int getExternalCommandType() {
		return this.externalCommandType;
	}
	
	/**
	 * Gets the external command response type of this instance of KeywordAction.
	 * @return
	 */
	public int getExternalCommandResponseType() {
		if(getType() != TYPE_EXTERNAL_CMD) throw new IllegalStateException("Cannot get command response from type: " + getType());
		return externalCommandResponseType;
	}
	
	/**
	 * Sets the external command response type of this instance of KeywordAction.
	 * @param type new value for {@link #externalCommandResponseType}
	 */
	public void setExternalCommandResponseType(int type) {
		if(getType() != TYPE_EXTERNAL_CMD) throw new IllegalStateException("Cannot set command response from type: " + getType());
		this.externalCommandResponseType = type;
	}
	
	/**
	 * Gets the external command response action type of this instance of KeywordAction.
	 * @return
	 */
	public int getCommandResponseActionType() {
		if(getType() != TYPE_EXTERNAL_CMD) throw new IllegalStateException("Cannot get command response action from type: " + getType());
		return externalCommandResponseActionType;
	}
	
	/**
	 * Sets the external command response action type of this instance of KeywordAction.
	 * @param type
	 */
	public void setCommandResponseActionType(int type) {
		if(getType() != TYPE_EXTERNAL_CMD) throw new IllegalStateException("Cannot get command response action from type: " + getType());
		this.externalCommandResponseActionType = type;
	}
	
	/**
	 * Sets the external command type of this instance of KeywordAction.
	 * @param type
	 */
	public void setExternalCommandType(int type) {
		this.externalCommandType = type;
	}
	
	/**
	 * Gets the state of this instance of KeywordAction.
	 * @return {@link #alive}
	 */
	public boolean isAlive() {
		long now = System.currentTimeMillis();
		return now >= this.startDate && now <= this.endDate;
	}
	
	/**
	 * Gets this action start date.
	 * @return {@link #startDate}
	 */
	public long getStartDate() {
		return this.startDate;
	}
	
	/**
	 * Gets this action email recipients.
	 * @return {@link #emailRecipients}
	 */
	public String getEmailRecipients() {
		if(this.type != TYPE_EMAIL) throw new IllegalStateException("Cannot get email recipients from action of type: " + type);
		return this.emailRecipients;
	}
	
	/**
	 * Gets this action email subject.
	 * @return {@link #emailSubject} 
	 */
	public String getEmailSubject() {
		if(this.type != TYPE_EMAIL) throw new IllegalStateException("Cannot get email subject from action of type: " + type);
		return this.emailSubject;
	}
	
	/**
	 * Gets this action end date.
	 * @return {@link #endDate}
	 */
	public long getEndDate() {
		return this.endDate;
	}
	
	/**
	 * Sets this action start date.
	 * @param date new value for {@link #startDate} 
	 */
	public void setStartDate(long date) {
		this.startDate = date;
	}
	
	/**
	 * Sets this action end date.
	 * @param date new value for {@link #endDate} 
	 */
	public void setEndDate(long date) {
		this.endDate = date;
	}
	
	/**
	 * Sets the group of this instance of KeywordAction.
	 * @param group new value for {@link #group}
	 */
	public void setGroup(Group group) {
		if (!hasGroup()) throw new IllegalStateException("Cannot get group from action of type: " + type);
		this.group = group;
	}
	
	/**
	 * Check if this action can have a group attached to it. 
	 * @return <code>true</code> if a group may be attached to an action of this type; <code>false</code> otherwise.
	 */
	private boolean hasGroup() {
		return this.type == TYPE_JOIN
				|| this.type==TYPE_LEAVE
				|| this.type==TYPE_FORWARD
				|| this.type==TYPE_EXTERNAL_CMD;
	}
	
	/**
	 * Sets the email recipients of this instance of KeywordAction.
	 * @param recipients new value for {@link #emailRecipients}
	 */
	public void setEmailRecipients(String recipients) {
		if(this.type != TYPE_EMAIL) throw new IllegalStateException("Cannot set email recipients from action of type: " + type);
		this.emailRecipients = recipients;
	}
	
	/**
	 * Sets the email subject of this instance of KeywordAction.
	 * @param subject new value for {@link #emailSubject}
	 */
	public void setEmailSubject(String subject) {
		if(this.type != TYPE_EMAIL) throw new IllegalStateException("Cannot set email subject from action of type: " + type);
		this.emailSubject = subject;
	}
	
	/**
	 * Sets the email account of this instance of KeywordAction.
	 * @param emailAccount new value for {@link #emailAccount} 
	 */
	public void setEmailAccount(EmailAccount emailAccount) {
		if(this.type != TYPE_EMAIL) throw new IllegalStateException("Cannot get group from action of type: " + type);
		this.emailAccount = emailAccount;
	}
	
	/**
	 * Sets the forward text of this instance of KeywordAction.
	 * @param text new value for {@link #forwardText}
	 */
	public void setForwardText(String text) {
		if (type != TYPE_FORWARD) throw new IllegalStateException("Cannot get forward text from action of type: " + type);
		this.commandString = text;
	}
	
	/**
	 * Sets the command text of this instance of KeywordAction.
	 * @param commandText new value for {@link #commandText}
	 */
	public void setCommandText(String commandText) {
		this.commandString = commandText;
	}
	
	/**
	 * Sets the command line of this instance of KeywordAction.
	 * @param commandLine new value for {@link #commandLine}
	 */
	public void setCommandLine(String commandLine) {
		this.externalCommand = commandLine;
	}
	
	/**
	 * Gets how many times this action was executed.
	 * @return {@link #counter}
	 */
	public int getCounter() {
		return this.counter;
	}
	
	/**
	 * Increments how many times this action was executed.
	 */
	public void incrementCounter() {
		++counter;
	}
	
	/**
	 * Gets the group related to this keyword action, or throws an exception if this
	 * type of action does not have groups associated with it.
	 * @return {@link #group}
	 */
	public Group getGroup() {
		if(!hasGroup()) throw new IllegalStateException("Cannot get group from action of type: " + type);
		return this.group;
	}

	/**
	 * Gets the email account related to this keyword action, or throws an exception if this
	 * type of action does not have email account associated with it.
	 * @return {@link #emailAccount}
	 */
	public EmailAccount getEmailAccount() {
		if(this.type != TYPE_EMAIL) throw new IllegalStateException("Cannot get group from action of type: " + type);
		return this.emailAccount;
	}
	
	/**
	 * Gets the reply text for this action (if it is of TYPE_REPLY or TYPE_EMAIL).
	 * @return {@link #unformattedReplyText}
	 */
	public String getUnformattedReplyText() {
		if(type != TYPE_REPLY && type != TYPE_EMAIL) throw new IllegalStateException("Cannot get reply text from action of type: " + type);
		return this.commandString;
	}
	
	/**
	 * If this action is of TYPE_REPLY or TYPE_EMAIL, sets the reply text. 
	 * @param replyText new value for {@link #replyText}
	 */
	public void setReplyText(String replyText) {
		if(type != TYPE_REPLY && type != TYPE_EMAIL) throw new IllegalStateException("Cannot set reply text from action of type: " + type);
		this.commandString = replyText;
	}
	
	/**
	 * Gets the forward text for this action (if it is of TYPE_FORWARD).
	 * @return
	 */
	public String getUnformattedForwardText() {
		if(type != TYPE_FORWARD) throw new IllegalStateException("Cannot get forward text from action of type: " + type);
		return this.commandString;
	}
	
	/**
	 * Gets the command text for this action (if it is of TYPE_EXTERNAL_CMD).
	 */
	public String getUnformattedCommandText() {
		if(getType() != TYPE_EXTERNAL_CMD) throw new IllegalStateException("Cannot get command text from type: " + getType());
		return this.commandString;
	}
	
	/**
	 * Gets the command line for this action (if it is of TYPE_EXTERNAL_CMD).
	 */
	public String getUnformattedCommand() {
		if(getType() != TYPE_EXTERNAL_CMD) throw new IllegalStateException("Cannot get command from type: " + getType());
		return this.externalCommand;
	}
	
	/**
	 * Gets the keyword that this action is associated with.
	 * @return {@link #keyword}
	 */
	public Keyword getKeyword() {
		return this.keyword;
	}
	
//> PRIVATE ACCESSORS
	/** @param type value for {@link #type} */
	private void setType(int type) {
		this.type = type;
	}
	/** @param keyword value for {@link #keyword} */
	private void setKeyword(Keyword keyword) {
		this.keyword = keyword;
	}
	/** @param counter value for {@link #counter} */
	private void setCounter(int counter) {
		this.counter = counter;
	}

//> STATIC HELPER METHODS
	public static class KeywordUtils {
		public static final String personaliseMessage(Contact contact, String messageText) {
			// Replace any user-defined variables they might have been included
			messageText = messageText.replace(FrontlineSMSConstants.USER_MARKER_TO_NAME, contact.getName());
			return messageText;
		}
		
		/**
		 * Creates the formatted reply text for this action from an incoming message.
		 * 
		 * If this action is not of TYPE_REPLY, throws an IllegalStateException.
		 * @param senderMsisdn
		 * @param incomingMessageText
		 * @return
		 * TODO remove incomingKeyword parameter
		 */
		public static final String getReplyText(KeywordAction action, Contact sender, String senderMsisdn, String incomingMessageText, String incomingKeyword) throws IllegalStateException {
			String senderDisplayName;
			if(sender != null) senderDisplayName = sender.getDisplayName();
			else senderDisplayName = senderMsisdn;
			return formatText(action.getUnformattedReplyText(), false, action, senderMsisdn, senderDisplayName, incomingMessageText, null);
		}
		
		/**
		 * Creates the formatted reply text for this action from an incoming message.
		 * 
		 * If this action is not of TYPE_REPLY, throws an IllegalStateException.
		 * @param senderMsisdn
		 * @param incomingMessageText
		 * @return
		 */
		public static final String getEmailSubject(KeywordAction action, Contact sender, String senderMsisdn, String incomingMessageText, String incomingKeyword) throws IllegalStateException {
			String senderDisplayName;
			if(sender != null) senderDisplayName = sender.getDisplayName();
			else senderDisplayName = senderMsisdn;
			
			return formatText(action.getEmailSubject(), false, action, senderMsisdn, senderDisplayName, incomingMessageText, null);
		}
		
		/**
		 * Creates the formatted external command or email for this action from an incoming message.
		 * 
		 * If this action is not of TYPE_EXTERNAL_CMD, throws an IllegalStateException.
		 * @param senderMsisdn
		 * @param incomingMessageText
		 * @return
		 */
		public static final String getExternalCommand(KeywordAction action, Contact sender, String senderMsisdn, String incomingMessageText, int refNo) throws IllegalStateException {
			String senderDisplayName;
			if (sender != null) senderDisplayName = sender.getDisplayName();
			else senderDisplayName = senderMsisdn;
			return formatText(action.getUnformattedCommand(), true, action, senderMsisdn, senderDisplayName, incomingMessageText, refNo);
		}
		
		
		/**
		 * Creates the formatted external command reply for this action from an incoming message.
		 * 
		 * If this action is not of TYPE_EXTERNAL_CMD, throws an IllegalStateException.
		 * @param action
		 * @param response
		 * @return
		 * @throws IllegalStateException
		 */
		public static final String getExternalCommandReplyMessage(KeywordAction action, String response) throws IllegalStateException {
			return KeywordUtils.getFormattedCommandReply(action, response);
		}
		
		/**
		 * Creates the formatted forward text for this action from an incoming message.
		 * 
		 * If this action is not of TYPE_FORWARD an IllegalStateException should be thrown.
		 * @param sender The Contact object representing the sender of this message, or NULL if this msisdn is not associated with a Contact.
		 * @param senderMsisdn The msisdn from which the message
		 * @param incomingMessageText The text of the received message.
		 * @return
		 */
		public static final String getForwardText(KeywordAction action, Contact sender, String senderMsisdn, String incomingMessageText) throws IllegalStateException {
			String senderDisplayName;
			if(sender != null) senderDisplayName = sender.getDisplayName();
			else senderDisplayName = senderMsisdn;
			return formatText(action.getUnformattedForwardText(), false, action, senderMsisdn, senderDisplayName, incomingMessageText, null);
		}
		
		/**
		 * Formats a message, inserting particular variables where their presence has been requested by placeholders.
		 */
		protected static final String getFormattedCommandReply(KeywordAction action, String response) {
			String command = action.getUnformattedCommandText();
			 
			command = command.replace(CsvUtils.MARKER_COMMAND_RESPONSE, response);
			
			return command;
		}
		
		/**
		 * Remove the keyword from the start of a received message.  If called on text that does not start with the
		 * keyword, the text will be returned unchanged.
		 * @param messageText
		 * @param keywordString
		 * @return
		 */
		static final String removeKeyword(String messageText, String keywordString) {
			String keywordInMessage = extractKeyword(messageText, keywordString);
			if(keywordInMessage == null) {
				return messageText;
			} else {
				if(messageText.length() == keywordString.length()) return "";
				else return messageText.substring(keywordString.length() + 1);
			}
		}
		
		/**
		 * Extracts the keyword from the start of a message, and returns the keyword as it appeared in
		 * the message.
		 * @param messageText
		 * @param keywordString
		 * @return the keyword string <em>as it appears at the start of messageText</em> or <code>null</code> if messageText does not start with keyword.
		 */
		static final String extractKeyword(String messageText, String keywordString) {
			int keywordLength = keywordString.length();
			if(messageText.length() == keywordLength) {
				if(messageText.equalsIgnoreCase(keywordString)) {
					return messageText;
				}
			} else if(messageText.length() > keywordLength) {
				String keywordInMessage = messageText.substring(0, keywordLength);
				if(keywordInMessage.equalsIgnoreCase(keywordString)) {
					char charAfterKeyword = messageText.charAt(keywordLength);
					if(charAfterKeyword == ' '
							||charAfterKeyword == '\n'
							||charAfterKeyword == '\r') {
						return keywordInMessage;
					}
				}
			}
			return null;
		}
		
		static String formatText(String unformattedText, boolean urlEncode, KeywordAction action, String senderMsisdn, String senderDisplayName, String incomingMessageText, Integer refNo) {
			String keywordString = action.getKeyword().getKeywordString();
			
			String keywordInMessage = extractKeyword(incomingMessageText, keywordString);
			String messageWithoutKeyword = removeKeyword(incomingMessageText, keywordString);
			
			String smsReferenceNumber = null;
			if(refNo != null) smsReferenceNumber = Integer.toString(refNo);
			
			if(urlEncode) {
				senderMsisdn = Utils.urlEncode(senderMsisdn);
				keywordInMessage = Utils.urlEncode(keywordInMessage);
				senderDisplayName = Utils.urlEncode(senderDisplayName);
				messageWithoutKeyword = Utils.urlEncode(messageWithoutKeyword);
			}
			
			// TODO perhaps all variables should be subbed?
			return Utils.replace(unformattedText,
					CsvUtils.MARKER_SENDER_NUMBER,		/*->*/ senderMsisdn,
					CsvUtils.MARKER_KEYWORD_KEY,		/*->*/ keywordInMessage,
					CsvUtils.MARKER_SENDER_NAME,		/*->*/ senderDisplayName,
					CsvUtils.MARKER_SMS_ID,			/*->*/ smsReferenceNumber,
					// N.B. message content should always be substituted last to prevent injection attacks
					CsvUtils.MARKER_MESSAGE_CONTENT,	/*->*/ messageWithoutKeyword 
					);
		}
	}
	
//> STATIC FACTORY METHODS
	/**
	 * Creates a keyword action to automatically REPLY to messages.
	 * @param keyword The keyword that triggers this action
	 * @param replyText The text to reply with when this action is triggered
	 * @param start 
	 * @param end 
	 * @return a new instance of KeywordAction
	 */
	public static KeywordAction createReplyAction(Keyword keyword, String replyText, long start, long end) {
		KeywordAction action = new KeywordAction();
		action.setType(TYPE_REPLY);
		action.setKeyword(keyword);
		action.setReplyText(replyText);
		action.setStartDate(start);
		action.setEndDate(end);
		return action;
	}
	
	/**
	 * Creates a keyword action to automatically send Email.
	 * @param keyword The keyword that triggers this action
	 * @param replyText The text to reply with when this action is triggered
	 * @param account 
	 * @param to 
	 * @param subject 
	 * @param start 
	 * @param end 
	 * @return a new instance of KeywordAction
	 */
	public static KeywordAction createEmailAction(Keyword keyword, String replyText, EmailAccount account, String to, String subject,long start, long end) {
		KeywordAction action = new KeywordAction();
		action.setType(TYPE_EMAIL);
		action.setKeyword(keyword);
		action.setReplyText(replyText);
		action.setEmailAccount(account);
		action.setEmailRecipients(to);
		action.setEmailSubject(subject);
		action.setStartDate(start);
		action.setEndDate(end);
		return action;
	}
	
	/**
	 * Creates a keyword action to automatically execute a external command.
	 * @param keyword The keyword that triggers this action
	 * @param commandLine The command to be executed
	 * @param commandType 
	 * <li> HTTP request  
	 * <li> Command line execution.
	 * @param responseType 
	 * <li> Plain Text 
	 * <li> Command list
	 * <li> No response
	 * @param responseActionType
	 * <li> Forward to Group Only 
	 * <li> Auto Reply Only
	 * <li> Both
	 * <li> Neither
	 * @param commandMsg The message to be sent with response.
	 * @param toFwd The group to be forwarded.
	 * @param start
	 * @param end
	 * @return a new instance of KeywordAction
	 */
	public static KeywordAction createExternalCommandAction(Keyword keyword, String commandLine, int commandType, int responseType,
			int responseActionType, String commandMsg, Group toFwd, long start, long end) {
		KeywordAction action = new KeywordAction();
		action.setType(TYPE_EXTERNAL_CMD);
		action.setKeyword(keyword);
		action.setCommandLine(commandLine);
		action.setExternalCommandType(commandType);
		action.setExternalCommandResponseType(responseType);
		action.setCommandResponseActionType(responseActionType);
		action.setGroup(toFwd);
		action.setStartDate(start);
		action.setEndDate(end);
		return action;
	}
	
	/**
	 * Creates a keyword action to automatically add a contact to a group.
	 * @param keyword The keyword that triggers this action
	 * @param group The group to add the sender to when this action is triggered
	 * @return a new instance of KeywordAction
	 */
	public static KeywordAction createGroupJoinAction(Keyword keyword, Group group, long start, long end) {
		KeywordAction action = new KeywordAction();
		action.setType(TYPE_JOIN);
		action.setKeyword(keyword);
		action.setGroup(group);
		action.setStartDate(start);
		action.setEndDate(end);
		return action;
	}
	
	/**
	 * Creates a keyword action to automatically remove a contact from a group.
	 * @param keyword The keyword that triggers this action
	 * @param group The group to remove the sender from when this action is triggered
	 * @return a new instance of KeywordAction
	 */
	public static KeywordAction createGroupLeaveAction(Keyword keyword, Group group, long start, long end) {
		KeywordAction action = new KeywordAction();
		action.setType(TYPE_LEAVE);
		action.setKeyword(keyword);
		action.setGroup(group);
		action.setStartDate(start);
		action.setEndDate(end);
		return action;
	}
	
	/**
	 * Creates a keyword action to automatically forward a message to a group
	 * @param keyword The keyword that triggers the action
	 * @param group The group to forward the message onto
	 * @param forwardText The message text to forward on to the group
	 * @return a new instance of KeywordAction
	 */
	public static KeywordAction createForwardAction(Keyword keyword, Group group, String forwardText, long start, long end) {
		KeywordAction action = new KeywordAction();
		action.setType(TYPE_FORWARD);
		action.setKeyword(keyword);
		action.setGroup(group);
		action.setForwardText(forwardText);
		action.setStartDate(start);
		action.setEndDate(end);
		return action;
	}
	
	/**
	 * Creates a survey action on this keyword if one does not already exist.
	 * @param keyword
	 * @return
	 */
	public static KeywordAction createSurveyAction(Keyword keyword, long start, long end) {
		KeywordAction action = new KeywordAction();
		action.setType(TYPE_SURVEY);
		action.setKeyword(keyword);
		action.setStartDate(start);
		action.setEndDate(end);
		return action;
	}

//> GENERATED CODE
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + commandInteger;
		result = prime * result
				+ ((commandString == null) ? 0 : commandString.hashCode());
		result = prime * result + counter;
		result = prime * result
				+ ((emailRecipients == null) ? 0 : emailRecipients.hashCode());
		result = prime * result
				+ ((emailSubject == null) ? 0 : emailSubject.hashCode());
		result = prime * result + (int) (endDate ^ (endDate >>> 32));
		result = prime * result
				+ ((externalCommand == null) ? 0 : externalCommand.hashCode());
		result = prime * result + externalCommandResponseActionType;
		result = prime * result + externalCommandResponseType;
		result = prime * result + externalCommandType;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
		result = prime * result + (int) (startDate ^ (startDate >>> 32));
		result = prime * result + type;
		return result;
	}

	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeywordAction other = (KeywordAction) obj;
		if (commandInteger != other.commandInteger)
			return false;
		if (commandString == null) {
			if (other.commandString != null)
				return false;
		} else if (!commandString.equals(other.commandString))
			return false;
		if (counter != other.counter)
			return false;
		if (emailRecipients == null) {
			if (other.emailRecipients != null)
				return false;
		} else if (!emailRecipients.equals(other.emailRecipients))
			return false;
		if (emailSubject == null) {
			if (other.emailSubject != null)
				return false;
		} else if (!emailSubject.equals(other.emailSubject))
			return false;
		if (endDate != other.endDate)
			return false;
		if (externalCommand == null) {
			if (other.externalCommand != null)
				return false;
		} else if (!externalCommand.equals(other.externalCommand))
			return false;
		if (externalCommandResponseActionType != other.externalCommandResponseActionType)
			return false;
		if (externalCommandResponseType != other.externalCommandResponseType)
			return false;
		if (externalCommandType != other.externalCommandType)
			return false;
		if (id != other.id)
			return false;
		if (keyword == null) {
			if (other.keyword != null)
				return false;
		} else if (!keyword.equals(other.keyword))
			return false;
		if (startDate != other.startDate)
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
