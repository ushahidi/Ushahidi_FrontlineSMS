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
package net.frontlinesms.arcane;

import java.io.IOException;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.arcane.data.*;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.repository.*;
import net.frontlinesms.data.DuplicateKeyException;

/**
 * Class for importing files from the Visual Basic version of FrontlineSMS.
 * @author Alex
 */
public class ArcaneDataImporter {
	private final ArcaneDataBundle mrBundle;
	private final ContactDao contactDao;
	private final GroupDao groupDao;
	private final MessageDao messageDao;

	public ArcaneDataImporter(FrontlineSMS frontlineSms, String dataPath) throws IOException {
		mrBundle = ArcaneDataLoader.loadBundleFromExternalDirectory(dataPath);
		this.contactDao = frontlineSms.getContactDao();
		this.groupDao = frontlineSms.getGroupDao();
		this.messageDao = frontlineSms.getMessageDao();
	}
	
	public void importContactsAndGroups() throws IOException {
		try {
			for(ArcaneContact arcaneContact : mrBundle.getContacts()) {
				Contact contact = contactDao.getFromMsisdn(arcaneContact.getMsisdn());
				if(contact == null) {
					contact = new Contact(arcaneContact.getName(), arcaneContact.getMsisdn(), "", "", arcaneContact.getNotes(), arcaneContact.isActive());
					contactDao.saveContact(contact);
				}
				Group newGroup = extractGroup(arcaneContact);
				newGroup.addContact(contact);
				groupDao.updateGroup(newGroup);
			}
		} catch(DuplicateKeyException ex) {
			// we've covered this!
		}
	}
	
	private Group extractGroup(ArcaneContact arcaneContact) throws DuplicateKeyException {
		String groupString = arcaneContact.getGroup();
		Group group = null;
		
		Group parent;
		String groupName;
		String[] groups = groupString.split("\\\\", 2);
		if(groups.length == 1) {
			parent = null;
			groupName = groups[0];
		} else {
			parent = groupDao.getGroupByName(groups[0]);
			if(parent == null) {
				parent = new Group(null, groups[0]);
				groupDao.saveGroup(parent);
			}
			groupName = groups[1];
		}
		group = groupDao.getGroupByName(groupName);
		if(group == null) {
			group = new Group(parent, groupName);
			groupDao.saveGroup(group);
		}
		return group;
	}
	
	public void importGroups() {
		try {
			for(ArcaneContact arcaneContact : mrBundle.getContacts()) extractGroup(arcaneContact);
		} catch(DuplicateKeyException ex) { /* we've got this covered */ }
	}
	
	public void importContacts() throws IOException {
		try {
			for(ArcaneContact arcaneContact : mrBundle.getContacts()) {
				Contact contact = contactDao.getFromMsisdn(arcaneContact.getMsisdn());
				if(contact == null) {
					contact = new Contact(arcaneContact.getName(), arcaneContact.getMsisdn(), "", "", arcaneContact.getNotes(), arcaneContact.isActive());
					contactDao.saveContact(contact);
				}
			}
		} catch(DuplicateKeyException ex) { /* we've got this covered */ }
	}
	
	public void importReceivedMessages() {
		for(ArcaneReceivedMessage message : mrBundle.getReceivedMessages()) {
			Message m = Message.createIncomingMessage(message.getTimestamp(), message.getMsisdn(), "import", message.getContent());
			messageDao.saveMessage(m);
		}
		for(ArcaneReceivedMessage_UnknownSender message : mrBundle.getReceivedMessages_unknownSender()) {
			Message m = Message.createIncomingMessage(message.getTimestamp(), message.getMsisdn(), "import", message.getContent());
			messageDao.saveMessage(m);
		}
	}
	
	public void importSentMessages() {
		for(ArcaneSentMessage message : mrBundle.getSentMessages()) {
			Message m = Message.createOutgoingMessage(message.getTimestamp(), "import", message.getGroup(), message.getContent());
			m.setStatus(Message.STATUS_SENT);
			messageDao.saveMessage(m);
		}
	}
	
	public void importKeywordActions() {
		mrBundle.getSurveys();
		mrBundle.getAutoReplies();
		mrBundle.getAutoReplyTriggers();
	}
}
