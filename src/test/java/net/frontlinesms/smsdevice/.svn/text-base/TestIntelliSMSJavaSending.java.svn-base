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
package net.frontlinesms.smsdevice;

import net.frontlinesms.AbstractTestCase;
import net.frontlinesms.hex.HexUtils;

import org.smslib.TpduUtils;

import IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK.*;

/**
 * This tests Intelli SMS sending using a java implementation.
 *
 * @author Carlos Eduardo Endler Genz
 * @date 19/01/2009
 */
public class TestIntelliSMSJavaSending extends AbstractTestCase {
	private IntelliSMS intelliSMS = new IntelliSMS();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		intelliSMS.Username = PROPERTIES.getProperty("intellisms.username");
		intelliSMS.Password = PROPERTIES.getProperty("intellisms.password");
	}
	
	/**
	 * Sends a SMS text message, using IntelliSMS settings stored in
	 * the test properties file.
	 * 
	 * @throws IntelliSMSException
	 */
	public void testSendSMS() throws IntelliSMSException {
		long start = System.currentTimeMillis();
		String[] sendTo = PROPERTIES.getProperty("intellisms.send.to").split(",");
		String msg = PROPERTIES.getProperty("intellisms.msg");
		String sender = PROPERTIES.getProperty("intellisms.sender") == null ? "" : PROPERTIES.getProperty("intellisms.sender");
		SendStatusCollection collection = intelliSMS.SendMessage(sendTo, msg, sender);
		if (collection.isEmpty()) fail("No status sent back.");
		assertNotNull(collection.get(0).MessageId);
		assertNotNull(collection.get(0).ResultCode);
		System.out.println("Message id [" + collection.get(0).MessageId + "]");
		System.out.println("Result code [" + collection.get(0).ResultCode + "]");
		
		System.out.println("Time to send: " + (System.currentTimeMillis() - start));
	}
	
	/**
	 * Sends a SMS text message, using IntelliSMS settings stored in
	 * the test properties file.
	 * 
	 * @throws IntelliSMSException
	 */
	public void testSendBinarySMS() throws IntelliSMSException {
		long start = System.currentTimeMillis();
		String[] sendTo = PROPERTIES.getProperty("intellisms.send.to").split(",");
		byte[] content = new byte[] {0,1,2};
		String sender = PROPERTIES.getProperty("intellisms.sender") == null ? "" : PROPERTIES.getProperty("intellisms.sender");
		byte[][] messagePayloads = TpduUtils.getPayloads(content, 0, 0);
		int totalParts = messagePayloads.length;
		for (int i = 1; i <= totalParts; i++) {
			SendStatusCollection results = intelliSMS.SendBinaryMessage(sendTo,
					HexUtils.encode(TpduUtils.generateUDH(i, totalParts, 0,
							0, 0)),
							HexUtils.encode(messagePayloads[i-1]),
							sender);
			if (results.isEmpty()) fail("No status sent back.");
			assertNotNull(results.get(0).MessageId);
			assertNotNull(results.get(0).ResultCode);
			System.out.println("Message id [" + results.get(0).MessageId + "]");
			System.out.println("Result code [" + results.get(0).ResultCode + "]");
		}
		System.out.println("Time to send: " + (System.currentTimeMillis() - start));
	}
	
	/**
	 * Sends a SMS text message, using IntelliSMS settings stored in
	 * the test properties file, expecting an error.
	 */
	public void testSendSMSError() {
		intelliSMS.Username = "dummy";
		intelliSMS.Password = "dummyPass";
		String[] sendTo = PROPERTIES.getProperty("intellisms.send.to").split(",");
		String msg = PROPERTIES.getProperty("intellisms.msg");
		String sender = PROPERTIES.getProperty("intellisms.sender") == null ? "" : PROPERTIES.getProperty("intellisms.sender");
		try {
			intelliSMS.SendMessage(sendTo, msg, sender);
			fail("Exception was expected.");
		} catch (Exception e) {
			// Success!! We were expecting an error.
		}		
	}
	
	/**
	 * Gets the account balance value, in credits.
	 * 
	 * @throws IntelliSMSException
	 */
	public void testGetBalance() throws IntelliSMSException {
		System.out.println(intelliSMS.GetBalance());
	}
	
}
