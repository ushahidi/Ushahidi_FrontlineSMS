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

import org.smslib.*;
import org.smslib.CService.MessageClass;
import org.apache.log4j.*;

/**
 * CAT Handler for Nokia Series 40 3rd Edition Feature Pack 2.  The phones in this
 * classification can be used for SENDING SMS, but not for RECEIVING.  Phones in this
 * class include:
 * <li>Nokia 6300
 * A full list can be seen at: http://www.forum.nokia.com/devices/matrix_s40_3ed_fp2_1.html
 * @author Alex
 *
 */
public class CATHandler_Nokia_S40_3ed extends CATHandler {
//> STATIC CONSTANTS
	/** Logging object */
	private static final Logger LOG = Logger.getLogger(CATHandler_Nokia_S40_3ed.class);
	
//> CONSTRUCTOR
	/** @see CATHandler#CATHandler(CSerialDriver, Logger, CService) */
	public CATHandler_Nokia_S40_3ed(CSerialDriver serialDriver, Logger log, CService srv) {
		super(serialDriver, log, srv);
		LOG.warn("Receiving SMS not currently supported");
	}

//> ACCESSORS
	/** @see CATHandler#getStorageLocations() */	
	@Override
	protected void getStorageLocations() throws IOException {
		LOG.warn("Receiving SMS not currently supported");
	}

	/** @see CATHandler#listMessages(int) */
	@Override
	protected String listMessages(MessageClass messageClass) throws IOException, UnrecognizedHandlerProtocolException {
		LOG.warn("Receiving SMS not currently supported");
		return "";
	}
	
	/** @see CATHandler#deleteMessage(int, String) */
	@Override
	protected boolean deleteMessage(int memIndex, String memLocation) throws IOException {
		LOG.warn("Receiving SMS not currently supported");
		return false;
	}

	/**
	 * For some reason, support for standard use of the SMS inbox using
	 * AT commands has been disabled in this class of phone.
	 * @return <code>false</code>, as this class of phone does not support access to the SMS inbox
	 * @see CATHandler#supportsReceive()
	 */
	@Override
	protected boolean supportsReceive() {
		return false;
	}
}
