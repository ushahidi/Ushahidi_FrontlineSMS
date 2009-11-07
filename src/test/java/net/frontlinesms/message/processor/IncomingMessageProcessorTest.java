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
package net.frontlinesms.message.processor;

import java.util.*;

import org.smslib.CIncomingMessage;

import thinlet.Thinlet;

import net.frontlinesms.*;
import net.frontlinesms.data.*;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.repository.*;
import net.frontlinesms.listener.UIListener;
import net.frontlinesms.smsdevice.SmsDevice;
import net.frontlinesms.smsdevice.SmsDeviceStatus;
import net.frontlinesms.ui.i18n.*;

/**
 * Class to test the {@link IncomingMessageProcessor} class.
 *
 * @author Carlos Eduardo Endler Genz
 * @date 11/03/2009
 */
public class IncomingMessageProcessorTest extends AbstractTestCase implements UIListener {
	private static final int PROCESSOR_TIME_TO_RUN = 10000;
	private FrontlineSMS frontlineSMS;
	private List<Message> messagesProcessed;
	
	@Override
	protected void setUp() throws Exception {
		try {
			// We need to set the bundle, otherwise we get errors
			LanguageBundle englishBundle = InternationalisationUtils.getLanguageBundleFromClasspath("/resources/languages/frontlineSMS.properties");
			Thinlet.DEFAULT_ENGLISH_BUNDLE = englishBundle.getProperties();
			frontlineSMS = new FrontlineSMS();
			frontlineSMS.setUiListener(this);
			messagesProcessed = new LinkedList<Message>();
		} catch (Throwable e) {
			throw new Exception(e);
		}
	}

	/**
	 * Try processing multiple messages.
	 */
	public void testInserting() {
		SmsDevice receiver = new FakeSmsDevice();
		int messagesNumber = 15;
		for (int i = 0; i < messagesNumber; i++) {
			CIncomingMessage msg = new CIncomingMessage(System.currentTimeMillis(), "Sender", "This is message [" + i + "]");
			frontlineSMS.incomingMessageEvent(receiver, msg);
		}
		// Wait 10 seconds to leave the thread to process the message
		Utils.sleep_ignoreInterrupts(PROCESSOR_TIME_TO_RUN);
		// Received messages must be messagesNumber
		assertEquals("Checking received messages. It should be [" + messagesNumber + "]", messagesNumber, messagesProcessed.size());
	}
	
	/**
	 * Try processing duplicated messages. Just one should be processed.
	 */
	public void testDuplicates() {
		// TODO The body of this test has been commented out.  There is currently some debate as to where the responsibility
		// for preventing duplicate message from being processed should actually lie.  It seems likely that this should be
		// done within the SmsDevice.
		
//		CIncomingMessage msg = new CIncomingMessage(System.currentTimeMillis(), "Sender", "This is a message.");
//		SmsDevice receiver = new FakeSmsDevice();
//		frontlineSMS.incomingMessageEvent(receiver, msg);
//		frontlineSMS.incomingMessageEvent(receiver, msg);
//		// Wait 10 seconds to leave the thread to process the message
//		Utils.sleep_ignoreInterrupts(PROCESSOR_TIME_TO_RUN);
//		// Received messages must be 1
//		assertEquals("Checking received messages. It should be one.", 1, messagesProcessed.size());
	}

	public void contactAddedToGroup(Contact contact, Group group) {}
	public void contactRemovedFromGroup(Contact contact, Group group) {}
	public void keywordActionExecuted(KeywordAction action) {}
	public void outgoingMessageEvent(Message message) {}
	public void smsDeviceEvent(SmsDevice phone, SmsDeviceStatus smsDeviceStatus) {}

	public void incomingMessageEvent(Message message) {
		assertNotNull("Message cannot be null at this point.", message);
		messagesProcessed.add(message);
	}

	@Override
	protected void tearDown() throws Exception {
		messagesProcessed.clear();
	}
	
}
