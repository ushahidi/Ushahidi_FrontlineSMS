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
package net.frontlinesms;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.repository.*;
import net.frontlinesms.data.*;
import net.frontlinesms.listener.IncomingMessageListener;
import net.frontlinesms.listener.UIListener;
import net.frontlinesms.smsdevice.SmsDevice;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.smslib.CIncomingMessage;
import org.smslib.CStatusReportMessage;
import org.smslib.sms.SmsMessageEncoding;

/**
 * Processor of incoming messages for {@link FrontlineSMS}.
 * @author Alex
 */
public class IncomingMessageProcessor extends Thread {
	/** Time, in millis, thread should sleep for after message processing failed. */
	private static final int THREAD_SLEEP_AFTER_PROCESSING_FAILED = 5000;

	private static final Logger LOG = Utils.getLogger(IncomingMessageProcessor.class);
	
	/** Set hi when the thread should terminate. */
	private boolean keepAlive;
	/** Queue of messages to process. */
	private final BlockingQueue<IncomingMessageDetails> incomingMessageQueue = new LinkedBlockingQueue<IncomingMessageDetails>();
	
//> DATA ACCESS OBJECTS
	private final FrontlineSMS frontlineSms;
	private final ContactDao contactDao;
	private final KeywordDao keywordDao;
	private final GroupDao groupDao;
	private final MessageDao messageFactory;
	private EmailDao emailDao;

	private UIListener uiListener;
	/** Set of listeners for incoming message events. */
	private Set<IncomingMessageListener> incomingMessageListeners = new HashSet<IncomingMessageListener>();
	
	private final EmailServerHandler emailServerManager;

	/**
	 * @param frontlineSms
	 * @param contactFactory
	 * @param keywordFactory
	 * @param groupFactory
	 * @param messageFactory
	 * @param formsFactory
	 * @param formsMessageHandler
	 * @param emailFactory
	 * @param emailServerManager
	 */
	public IncomingMessageProcessor(FrontlineSMS frontlineSms,
			ContactDao contactFactory, KeywordDao keywordFactory,
			GroupDao groupFactory,
			MessageDao messageFactory, EmailDao emailFactory,
			EmailServerHandler emailServerManager) {
		super("Incoming message processor");
		this.frontlineSms = frontlineSms;
		this.contactDao = contactFactory;
		this.keywordDao = keywordFactory;
		this.groupDao = groupFactory;
		this.messageFactory = messageFactory;
		this.emailDao = emailFactory;
		this.emailServerManager = emailServerManager;
	}
	
	public void setUiListener(UIListener uiListener) {
		this.uiListener = uiListener;
	}

	public void queue(SmsDevice receiver, CIncomingMessage incomingMessage) {
		LOG.trace("Adding message to queue: " + receiver.hashCode() + ":" + incomingMessage.hashCode());
		incomingMessageQueue.add(new IncomingMessageDetails(receiver, incomingMessage));
	}
	
	public void die() {
		keepAlive = false;
		incomingMessageQueue.notify();
	}
	
	public void run() {
		keepAlive = true;
		while(keepAlive) {
			IncomingMessageDetails incomingMessageDetails = null;
			LOG.trace("Getting incoming message from queue.");
			try {
				incomingMessageDetails = incomingMessageQueue.take();
			} catch(InterruptedException ex) {
				LOG.warn("Thread interrupted.", ex);
			}
		
			if (incomingMessageDetails == null) {
				// we may have popped out when queue was notified, which means job may be null
				LOG.trace("There were no messages in the queue.");
				continue;
			} else {
				try {
					// We've got a new message, so process it.				
					CIncomingMessage incomingMessage = incomingMessageDetails.message;
					SmsDevice receiver = incomingMessageDetails.receiver;
					LOG.trace("Got message from queue: " + receiver.hashCode() + ":" + incomingMessage.hashCode());
					
					// Check the incoming message details with the KeywordFactory to make sure there are no details
					// that should be hidden before creating the message object...
					String incomingSenderMsisdn = incomingMessage.getOriginator();
					LOG.debug("Sender [" + incomingSenderMsisdn + "]");
					int type = incomingMessage.getType();
					if (type == CIncomingMessage.MessageType.StatusReport) {
						// Match the status report with a previously sent message, and update that message's
						// status.  If no message is found to match this to, just ditch the status report.  This
						// means that shredding is of no concern here.
						CStatusReportMessage statusReport = (CStatusReportMessage) incomingMessage;
						// Here, we strip the first four characters off the originator's number.  This is because we
						// cannot be sure if the numbers supplied by the PhoneHandler are localised, or international
						// with or without leading +.
						Message message = messageFactory.getMessageForStatusUpdate(statusReport.getOriginator().substring(4), incomingMessage.getRefNo());
						if (message != null) {
							LOG.debug("It's a delivery report for message [" + message + "]");
							switch(statusReport.getDeliveryStatus()) {
							case CStatusReportMessage.DeliveryStatus.Delivered:
								message.setStatus(Message.STATUS_DELIVERED);
								break;
							case CStatusReportMessage.DeliveryStatus.Aborted:
								message.setStatus(Message.STATUS_FAILED);
								break;
							}
							if (uiListener != null) {
								uiListener.outgoingMessageEvent(message);
							}
						}
					} else {
						// This is an incoming message, so process accordingly
						Message incoming;
						if (incomingMessage.getMessageEncoding() == SmsMessageEncoding.GSM_7BIT || incomingMessage.getMessageEncoding() == SmsMessageEncoding.UCS2) {
							// Only do the keyword stuff if this isn't a delivery report
							String incomingMessageText = incomingMessage.getText();
							LOG.debug("It's a incoming message [" + incomingMessageText + "]");
							incoming = Message.createIncomingMessage(incomingMessage.getDate(), incomingSenderMsisdn, receiver.getMsisdn(), incomingMessageText.trim());
							messageFactory.saveMessage(incoming);
							handleTextMessage(incoming, incomingMessage.getRefNo());
						} else {
							Contact sender = contactDao.getFromMsisdn(incomingSenderMsisdn);
							if(sender == null) {
								try {
									sender = new Contact(null, incomingSenderMsisdn, null, null, null, true);
									contactDao.saveContact(sender);
								} catch (DuplicateKeyException ex) {
									LOG.error(ex);
								}
							}
							
							// Save the binary message
							incoming = Message.createBinaryIncomingMessage(incomingMessage.getDate(), incomingSenderMsisdn, receiver.getMsisdn(), -1, incomingMessage.getBinary());
							messageFactory.saveMessage(incoming);
						}

						for(IncomingMessageListener listener : this.incomingMessageListeners) {
							listener.incomingMessageEvent(incoming);
						}
						if (uiListener != null) {
							uiListener.incomingMessageEvent(incoming);
						}
					}
				} catch(Throwable t) {
					// There was a problem processing the message.  At this stage, any issue should be a database
					// connectivity issue.  Stop processing messages for a while, and re-queue this one.
					LOG.warn("Error processing message.  It will be queued for re-processing.");
					incomingMessageQueue.add(incomingMessageDetails);
					Utils.sleep_ignoreInterrupts(THREAD_SLEEP_AFTER_PROCESSING_FAILED);
				}
			}
		}
		LOG.trace("EXIT");
	}

	/**
	 * Processes keyword actions for a text message.
	 * @param incoming
	 * @param refNo 
	 */
	private void handleTextMessage(final Message incoming, final int refNo) {
		Keyword keyword = keywordDao.getFromMessageText(incoming.getTextContent());
		if (keyword == null) keyword = keywordDao.getFromMessageText("");
		if (keyword != null) {
			final Collection<KeywordAction> actions;
			LOG.debug("The message contains keyword [" + keyword.getKeywordString() + "]");
			actions = keyword.getActions();
			// TODO process pre-message actions (e.g. "shred") TODO this should actually be done BEFORE the message object is persisted

			if(actions.size() > 0) {
				LOG.debug("Executing actions for keyword, if the contact is allowed!");
				Contact contact = contactDao.getFromMsisdn(incoming.getSenderMsisdn());
				//If we could not find this contact, we execute the action.
				//If we found a contact, he/she needs to be allowed to execute the action.
				if (contact == null || contact.isActive()) {
					// TODO why are we creating new threads here?  Looks like a bad idea; why is it necessary?
					new Thread() {
						public void run() {
							for (KeywordAction action : actions) {
								if (action.isAlive()) {
									handleIncomingMessageAction_post(action, incoming, refNo);
								}
							}
						}
					}.start();
				}
			}
		}
	}
	

	/**
	 * Handle relevant incoming message actions AFTER the message has been created with the messageFactory.
	 * 
	 * @param action The action to executed.
	 * @param incoming The incoming message that triggered this action.
	 * @param refNo message reference number of the incoming text
	 */
	private void handleIncomingMessageAction_post(KeywordAction action, Message incoming, int refNo) {
		LOG.trace("ENTER");
		String incomingSenderMsisdn = incoming.getSenderMsisdn();
		String incomingMessageText = incoming.getTextContent();
		switch (action.getType()) {
		case KeywordAction.TYPE_FORWARD:
			// Generate a message, and then forward it to the group attached to this action.
			LOG.debug("It is a forward action!");
			String forwardedMessageText = KeywordAction.KeywordUtils.getForwardText(action, contactDao.getFromMsisdn(incomingSenderMsisdn), incomingSenderMsisdn, incomingMessageText);
			LOG.debug("Message to forward [" + forwardedMessageText + "]");
			for (Contact contact : action.getGroup().getDirectMembers()) {
				if (contact.isActive()) {
					LOG.debug("Sending to [" + contact.getName() + "]");
					frontlineSms.sendTextMessage(contact.getMsisdn(), KeywordAction.KeywordUtils.personaliseMessage(contact, forwardedMessageText));
				}
			}
			break;
		case KeywordAction.TYPE_JOIN: {
			LOG.debug("It is a group join action!");
			
			// If the contact does not exist, we need to persist him so that we can add him to a group.
			// Otherwise, get the contact from the database.
			Contact contact = contactDao.getFromMsisdn(incomingSenderMsisdn);
			try {
				if (contact == null) {
					contact = new Contact(null, incomingSenderMsisdn, null, null, null, true);
					contactDao.saveContact(contact);
				}
				Group group = action.getGroup();
				LOG.debug("Adding contact [" + contact.getName() + "], Number [" + contact.getMsisdn() + "] to Group [" + group.getName() + "]");
				boolean contactAdded = group.addContact(contact);
				if(contactAdded) {
					groupDao.updateGroup(group);
					if(uiListener != null) {
						uiListener.contactAddedToGroup(contact, group);
					}
				}
			} catch(DuplicateKeyException ex) {
				// Due to previous check, this should never be thrown...
				// Not much we can do if it is!
				// FIXME throwing this exception could spit out otherwise-shredded data
				// into the logs!
				throw new RuntimeException(ex);
			}
		}	break;
		case KeywordAction.TYPE_LEAVE: {
			LOG.debug("It is a group leave action!");
			Contact contact = contactDao.getFromMsisdn(incomingSenderMsisdn);
			if (contact != null) {
				Group group = action.getGroup();
				LOG.debug("Removing contact [" + contact.getName() + "] from Group [" + group.getName() + "]");
				if(group.removeContact(contact)) {
					this.groupDao.updateGroup(group);
				}
				if (uiListener != null) {
					uiListener.contactRemovedFromGroup(contact, group);
				}
			}
		}	break;
		case KeywordAction.TYPE_REPLY:
			// Generate a message, and then send it back to the sender of the received message.
			LOG.debug("It is an auto-reply action!");
			String reply = KeywordAction.KeywordUtils.getReplyText(action, contactDao.getFromMsisdn(incomingSenderMsisdn), incomingSenderMsisdn, incomingMessageText, null);
			LOG.debug("Sending [" + reply + "] to [" + incomingSenderMsisdn + "]");
			Message msgReply = frontlineSms.sendTextMessage(incomingSenderMsisdn, reply);
			// FIXME the following two lines should be re-instated once the DAO pattern is finalised:
			// action.addMessageToAction(incoming);
			// action.addMessageToAction(msgReply);
			break;
		case KeywordAction.TYPE_EXTERNAL_CMD:
			// Executes a external command
			LOG.debug("It is an external command action!");
			try {
				executeExternalCommand(action, incomingSenderMsisdn, incomingMessageText, refNo);
			} catch (IOException e) {
				LOG.debug("Problem executing external command.", e);
			} catch (InterruptedException e) {
				LOG.debug("Problem executing external command.", e);
			} catch (JDOMException e) {
				LOG.debug("Problem executing external command.", e);
			}
			break;
		case KeywordAction.TYPE_EMAIL:
			LOG.debug("It is an e-mail action!");
			Email email = new Email(
					action.getEmailAccount(),
					action.getEmailRecipients(),
					KeywordAction.KeywordUtils.getEmailSubject(action, contactDao.getFromMsisdn(incomingSenderMsisdn), incomingSenderMsisdn, incomingMessageText, null),
					KeywordAction.KeywordUtils.getReplyText(action, contactDao.getFromMsisdn(incomingSenderMsisdn), incomingSenderMsisdn, incomingMessageText, null)
			);
			emailDao.saveEmail(email);
			LOG.debug("Sending [" + email.getEmailContent() + "] from [" + email.getEmailFrom().getAccountName() + "] to [" + email.getEmailRecipients() + "]");
			emailServerManager.sendEmail(email);
			break;
		case KeywordAction.TYPE_SURVEY:
			// FIXME the following line should be re-instated once the DAO pattern is finalised:
			// action.addMessageToAction(incoming);
			break;
		}
		action.incrementCounter();
		if (uiListener != null) {
			uiListener.keywordActionExecuted(action);
		}
		LOG.debug("Number of hits for this action [" + action + "] is [" + action.getCounter() + "]");
		LOG.trace("EXIT");
	}
	

	/**
	 * Executes a external command (HTTP or Command Line) and threats its response
	 * according to what is defined in the action.
	 * 
	 * @param action
	 * @param incoming
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws JDOMException 
	 */
	private void executeExternalCommand(KeywordAction action, String incomingSenderMsisdn, String incomingMessageText, int refNo) throws IOException, InterruptedException, JDOMException {
		LOG.trace("ENTER");
		String cmd = KeywordAction.KeywordUtils.getExternalCommand(
				action,
				contactDao.getFromMsisdn(incomingSenderMsisdn),
				incomingSenderMsisdn,
				incomingMessageText,
				refNo
		);
		LOG.debug("Command to be executed [" + cmd + "]");
		String response = null;

		if (action.getExternalCommandResponseType() != KeywordAction.EXTERNAL_RESPONSE_LIST_COMMANDS) {
			LOG.debug("Response will be plain text or nothing at all.");
			//Executes the command and handle the response as plain text, or no response at all.
			if (action.getExternalCommandType() == KeywordAction.EXTERNAL_HTTP_REQUEST) {
				LOG.debug("Executing HTTP request...");
				response = Utils.makeHttpRequest(cmd, action.getExternalCommandResponseType() == KeywordAction.EXTERNAL_RESPONSE_PLAIN_TEXT);
			} else {
				LOG.debug("Executing external program...");
				response = Utils.executeExternalProgram(cmd, action.getExternalCommandResponseType() == KeywordAction.EXTERNAL_RESPONSE_PLAIN_TEXT);
			}
			if (action.getExternalCommandResponseType() == KeywordAction.EXTERNAL_RESPONSE_PLAIN_TEXT) {
				LOG.debug("Response [" + response + "]");
				handleResponse(action, incomingSenderMsisdn, response);
			}
		} else {
			//LIST OF COMMANDS TO EXECUTE
			LOG.debug("Response will be an XML with Frontline Commands.");
			InputStream toRead = null;
			if (action.getExternalCommandType() == KeywordAction.EXTERNAL_HTTP_REQUEST) {
				LOG.debug("Executing HTTP request...");
				toRead = Utils.makeHttpRequest(cmd);
			} else {
				LOG.debug("Executing external program...");
				toRead = Utils.executeExternalProgram(cmd);
			}
			LOG.debug("Reading XML from response...");
			XMLReader reader = new XMLReader(toRead);
			for (XMLMessage msg : reader.readMessages()) {
				LOG.debug("Message found!");
				LOG.debug("Data [" + msg.getData() + "]");
				if (msg.getType() == XMLMessage.TYPE_TEXT) {
					//We add everything to the numbers list, to send in the end.
					//Contacts
					for (String contact : msg.getToContacts()) {
						Contact c = contactDao.getContactByName(contact);
						if (c!= null && c.isActive()) {
							msg.addNumber(c.getMsisdn());
						}
					}
					//Groups
					for (String group : msg.getToGroups()) {
						Group g = groupDao.getGroupByName(group);
						if (g != null) {
							for (Contact c : g.getDirectMembers()) {
								if (c.isActive()) {
									msg.addNumber(c.getMsisdn());
								}
							}
						}
					}
					//All recipients are in the numbers list now.
					for (String number : msg.getToNumbers()) {
						LOG.debug("Sending to [" + number + "]");
						frontlineSms.sendTextMessage(number, msg.getData());
					}
				} else {
					//TODO BINARY MESSAGE
				}
			}
		}
		LOG.trace("EXIT");
	}

	/**
	 * Handles the command response for this action.
	 * 
	 * @param action
	 * @param incoming
	 * @param response
	 */
	private void handleResponse(KeywordAction action, String incomingSenderMsisdn,
			String response) {
		// PLAIN TEXT RESPONSE so we need to verify if the user wants
		// to auto reply forward the response
		LOG.trace("ENTER");
		int responseActionType = action.getCommandResponseActionType();
		if (responseActionType == KeywordAction.EXTERNAL_DO_NOTHING) {
			LOG.debug("Nothing to do with the response!");
			LOG.trace("EXIT");
			return;
		}
		String message = KeywordAction.KeywordUtils.getExternalCommandReplyMessage(action, response);
		LOG.debug("Message to forward [" + message + "]");
		if (responseActionType == KeywordAction.TYPE_REPLY 
				|| responseActionType == KeywordAction.EXTERNAL_REPLY_AND_FORWARD) {
			//Auto reply
			LOG.debug("Sending to [" + incomingSenderMsisdn + "] as an auto-reply.");
			frontlineSms.sendTextMessage(incomingSenderMsisdn, message);
		}
		if (responseActionType == KeywordAction.TYPE_FORWARD 
				|| responseActionType == KeywordAction.EXTERNAL_REPLY_AND_FORWARD) {
			//Forwarding to a group
			Group fwd = action.getGroup();
			LOG.debug("Forwarding to group [" + fwd.getName() + "]");
			for (Contact contact : fwd.getDirectMembers()) {
				if (contact.isActive()) {
					if (responseActionType != KeywordAction.EXTERNAL_REPLY_AND_FORWARD 
							|| !contact.getMsisdn().equalsIgnoreCase(incomingSenderMsisdn)) {
						//If we have already replied to the sender and he/she is on the group to forward
						//so we don't send the message again.
						LOG.debug("Sending to contact [" + contact.getName() + "]");
					}
					frontlineSms.sendTextMessage(contact.getMsisdn(), message);
				}
			}
		}
		LOG.trace("EXIT");
	}
	
	/**
	 * Adds another {@link IncomingMessageListener} to {@link #incomingMessageListeners}.
	 * @param incomingMessageListener new {@link IncomingMessageListener}
	 */
	void addIncomingMessageListener(IncomingMessageListener incomingMessageListener) {
		this.incomingMessageListeners.add(incomingMessageListener);
	}
	
	private class IncomingMessageDetails {
		private final CIncomingMessage message;
		private final SmsDevice receiver;
		public IncomingMessageDetails(SmsDevice receiver, CIncomingMessage message) {
			this.receiver = receiver;
			this.message = message;
		}
	}
}
