// SMSLib for Java
// An open-source API Library for sending and receiving SMS via a GSM modem.
// Copyright (C) 2002-2007, Thanasis Delenikas, Athens/GREECE
// Web Site: http://www.smslib.org
//
// SMSLib is distributed under the LGPL license.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA

package org.smslib.handler;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.smslib.CSerialDriver;
import org.smslib.CService;
import org.smslib.NoResponseException;
import org.smslib.UnrecognizedHandlerProtocolException;
import org.smslib.CService.MessageClass;

public class CATHandler_Mock extends CATHandler {
	private String listMessagesResponse;
	
	public CATHandler_Mock(CSerialDriver serialDriver, Logger log, CService srv) {
		super(serialDriver, log, srv);
	}
	
	@Override
	protected int sendMessage(int size, String pdu, String phone, String text) throws IOException, NoResponseException, UnrecognizedHandlerProtocolException {
		System.out.println("size  : " + size);
		System.out.println("pdu   : " + pdu);
		System.out.println("phone : " + phone);
		System.out.println("text  : " + text);
		return 0;
	}
	
	@Override
	protected String listMessages(MessageClass messageClass) throws IOException, UnrecognizedHandlerProtocolException {
		if(this.listMessagesResponse == null) {
			throw new RuntimeException("listMessagesResponse not set, so I don't know how to respond to this!");
		} else {
			return this.listMessagesResponse;
		}
	}
}
