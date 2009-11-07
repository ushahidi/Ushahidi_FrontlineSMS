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

import java.util.ArrayList;
import java.util.List;

import net.frontlinesms.arcane.data.AutoReply;
import net.frontlinesms.arcane.data.AutoReplyTrigger;
import net.frontlinesms.arcane.data.ArcaneContact;
import net.frontlinesms.arcane.data.ArcaneReceivedMessage;
import net.frontlinesms.arcane.data.ArcaneReceivedMessage_UnknownSender;
import net.frontlinesms.arcane.data.ArcaneSentMessage;
import net.frontlinesms.arcane.data.Survey;

class ArcaneDataBundle {
	private final List<ArcaneContact> contacts = new ArrayList<ArcaneContact>();
	private final List<AutoReplyTrigger> autoReplyTriggers = new ArrayList<AutoReplyTrigger>();
	private final List<ArcaneReceivedMessage_UnknownSender> receivedMessages_unknownSender = new ArrayList<ArcaneReceivedMessage_UnknownSender>();
	private final List<AutoReply> autoReplies = new ArrayList<AutoReply>();
	private final List<ArcaneReceivedMessage> receivedMessages = new ArrayList<ArcaneReceivedMessage>();
	private final List<ArcaneSentMessage> sentMessages = new ArrayList<ArcaneSentMessage>();
	private final List<Survey> surveys = new ArrayList<Survey>();
	
	ArcaneDataBundle() {	
	}
	
	public List<ArcaneContact> getContacts() {
		return contacts;
	}
	public List<AutoReplyTrigger> getAutoReplyTriggers() {
		return autoReplyTriggers;
	}
	public List<ArcaneReceivedMessage_UnknownSender> getReceivedMessages_unknownSender() {
		return receivedMessages_unknownSender;
	}
	public List<AutoReply> getAutoReplies() {
		return autoReplies;
	}
	public List<ArcaneReceivedMessage> getReceivedMessages() {
		return receivedMessages;
	}
	public List<ArcaneSentMessage> getSentMessages() {
		return sentMessages;
	}
	public List<Survey> getSurveys() {
		return surveys;
	}
}
