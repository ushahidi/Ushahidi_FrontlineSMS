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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.frontlinesms.data.domain.*;
import net.frontlinesms.listener.EmailListener;

import org.apache.log4j.Logger;

/**
 * EmailServerHandler should be run as a separate thread.
 * 
 * It handles the email accounts available to send emails from.
 * 
 * @author Carlos Eduardo Genz
 * <li> kadu(at)masabi(dot)com
 */
public class EmailServerHandler extends Thread implements EmailListener {
	
	/** List of emails queued to be sent. */
	private final ConcurrentLinkedQueue<Email> outbox = new ConcurrentLinkedQueue<Email>();
	/** List of servers handlers that this manager is currently looking after. */
	private final Map<EmailAccount, EmailSender> serverHandlers = new ConcurrentHashMap<EmailAccount, EmailSender>();
	/** Listener to be passed Email Listener events from this */
	private EmailListener emailListener;
	/** Flag indicating that the thread should continue running. */
	private boolean running;	

	private static Logger LOG = Utils.getLogger(EmailServerHandler.class);
	
	public EmailServerHandler() {
		super("EmailServerHandler");
	}
	
	public void setEmailListener(EmailListener emailListener) {
		this.emailListener = emailListener;
	}
	
	public void run() {
		LOG.trace("ENTER");
		running = true;
		while (running) {
			Email email;
			while ((email = outbox.poll()) != null) {
				LOG.debug("Got e-mail [" + email + "] from outbox");
				try {
					LOG.debug("Looking for a thread for this e-mail server [" + email.getEmailFrom().getAccountServer() + "]");
					if (serverHandlers.containsKey(email.getEmailFrom())) {
						LOG.debug("A Thread for the server already exists!");
						if (serverHandlers.get(email.getEmailFrom()).isAlive()) {
							LOG.debug("And it is alive! Adding this e-mail to the thread outbox.");
							serverHandlers.get(email.getEmailFrom()).sendEmail(email);
						} else {
							LOG.debug("And it is dead! Creating a new thread for server [" + email.getEmailFrom().getAccountServer() + "]...");
							EmailSender sender = new EmailSender(email.getEmailFrom(), this);
							sender.sendEmail(email);
							sender.start();
							serverHandlers.put(email.getEmailFrom(), sender);
						}
					} else {
						LOG.debug("No thread found! Creating a new thread for server [" + email.getEmailFrom().getAccountServer() + "]...");
						EmailSender sender = new EmailSender(email.getEmailFrom(), this);
						sender.sendEmail(email);
						sender.start();
						serverHandlers.put(email.getEmailFrom(), sender);
					}
				} catch (Exception e) {
					//This exception may happens because the account was deleted.
					LOG.debug("Error retrieving account details", e);
				}
			}
			Utils.sleep_ignoreInterrupts(5000);
		}
		LOG.trace("EXIT");
	}

	public void serverRemoved(EmailAccount account) {
		LOG.trace("ENTER");
		if (serverHandlers.containsKey(account)) {
			LOG.debug("Stopping the account thread [" + account.getAccountName() + "]");
			serverHandlers.get(account).stopRunning();
			serverHandlers.remove(account);
		}
		LOG.trace("EXIT");
	}
	
	/**
	 * Remove the supplied email from outbox.
	 * 
	 * @param deleted
	 */
	public void removeFromOutbox(Email deleted) {
		LOG.trace("ENTER");
		outbox.remove(deleted);
		LOG.debug("Email [" + deleted + "] removed from outbox. Size is [" + outbox.size() + "]");
		LOG.trace("EXIT");
	}
	
	/**
	 * Send the email.
	 */
	public void sendEmail(Email outgoing) {
		LOG.trace("ENTER");
		outgoing.setStatus(Email.STATUS_OUTBOX);
		outbox.add(outgoing);
		if (emailListener != null) {
			emailListener.outgoingEmailEvent(null, outgoing);
		}
		LOG.debug("E-mail added to outbox. Size is [" + outbox.size() + "]");
		LOG.trace("EXIT");
	}
	
	/**
	 * Flags the internal thread to stop running.
	 */
	public void stopRunning() {
		this.running = false;
		for (EmailAccount acc : serverHandlers.keySet()) {
			serverHandlers.get(acc).stopRunning();
		}
	}

	public void outgoingEmailEvent(EmailSender sender, Email email) {
		if (emailListener != null) {
			emailListener.outgoingEmailEvent(sender, email);
		}
	}	
}
