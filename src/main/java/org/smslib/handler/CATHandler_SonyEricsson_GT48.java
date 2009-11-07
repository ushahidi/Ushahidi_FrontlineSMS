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
import org.apache.log4j.*;

public class CATHandler_SonyEricsson_GT48 extends CATHandler_SonyEricsson
{
	public CATHandler_SonyEricsson_GT48(CSerialDriver serialDriver, Logger log, CService srv)
	{
		super(serialDriver, log, srv);
	}

	protected int sendMessage(int size, String pdu, String phone, String text) throws IOException, NoResponseException, UnrecognizedHandlerProtocolException
	{
		int responseRetries, errorRetries;
		String response;
		int refNo;

		int messageProtocol = srv.getProtocol();
		switch (messageProtocol)
		{
			case CService.Protocol.PDU:
				errorRetries = 0;
				while (true)
				{
					responseRetries = 0;
					serialDriver.send(CUtils.replace("AT+CMGS=\"{1}\"\r", "\"{1}\"", "" + size));
					sleepWithoutInterruption(DELAY_CMGS);
					while (!serialDriver.dataAvailable())
					{
						responseRetries++;
						if (responseRetries == 4) throw new NoResponseException();
						if (log != null) log.info("CATHandler_SonyEricsson_GT48().SendMessage(): Still waiting for response (I) (" + responseRetries + ")...");
						sleepWithoutInterruption(srv.getDelayNoResponse());
					}
					responseRetries = 0;
					serialDriver.clearBuffer();
					serialDriver.send(pdu);
					serialDriver.send((char) 26);
					serialDriver.send((char) 13); // special for SonyEricsson
					response = serialDriver.getResponse();
					while (response.length() == 0)
					{
						responseRetries++;
						if (responseRetries == 4) throw new NoResponseException();
						if (log != null) log.info("CATHandler_SonyEricsson_GT48().SendMessage(): Still waiting for response (II) (" + responseRetries + ")...");
						response = serialDriver.getResponse();
					}
					if (response.indexOf("OK\r") >= 0)
					{
						refNo = 1;
						break;
					}
					else if (response.indexOf("CMS ERROR:") >= 0)
					{
						errorRetries++;
						AtCmsError.log(log, response, pdu);
						if (errorRetries == 4)
						{
							if (log != null) log.error("GSM CMS Errors: Quit retrying, message lost...");
							refNo = -1;
							break;
						}
						else if (log != null) log.error("GSM CMS Errors: Possible collision, retrying...");
					}
					else refNo = -1;
				}
				break;
			case CService.Protocol.TEXT:
				refNo = super.sendMessage(size, pdu, phone, text);
				break;
			default:
				throw new UnrecognizedHandlerProtocolException(messageProtocol);
		}
		return refNo;
	}
}
