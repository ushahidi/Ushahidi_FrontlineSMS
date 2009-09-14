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

public class CATHandler_SonyEricsson extends CATHandler
{
	public CATHandler_SonyEricsson(CSerialDriver serialDriver, Logger log, CService srv)
	{
		super(serialDriver, log, srv);
	}

	protected void reset() throws IOException
	{
		serialDriver.send("AT+CFUN=1,1\r");
		sleepWithoutInterruption(DELAY_RESET);
		serialDriver.clearBuffer();
	}

	protected boolean setPduMode() throws IOException
	{
		serialDriver.send("AT+CMGF=0\r");
		String respStr = serialDriver.getResponse();
		return (respStr.matches("\\s+OK\\s+") || respStr.matches("\\s*\\+CMGF: 0\\s*OK\\s"));
	}

	protected boolean disableIndications() throws IOException
	{
		String atDisableIndications = "AT\r";
		serialDriver.send("AT+CNMI=?\r");
		String cnmiTestResponse = serialDriver.getResponse().toUpperCase();

		if (cnmiTestResponse.indexOf("+CNMI: (2)") >= 0) atDisableIndications = "AT+CNMI=2,0,0,0\r";
		else if (cnmiTestResponse.indexOf("+CNMI: (3)") >= 0) atDisableIndications = "AT+CNMI=3,0,0,0\r";
		else return false;
		serialDriver.send(atDisableIndications);
		return (serialDriver.getResponse().matches("\\s+OK\\s+"));
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
					serialDriver.send(CUtils.replace("AT+CMGS=\"{1}\"\r", "\"{1}\"", Integer.toString(size)));
					sleepWithoutInterruption(DELAY_CMGS);
					while (!serialDriver.dataAvailable())
					{
						responseRetries++;
						if (responseRetries == 4) throw new NoResponseException();
						if (log != null) log.info("CATHandler_SonyEricsson().SendMessage(): Still waiting for response (I) (" + responseRetries + ")...");
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
						if (log != null) log.info("CATHandler_SonyEricsson().SendMessage(): Still waiting for response (II) (" + responseRetries + ")...");
						response = serialDriver.getResponse();
					}
					if (response.indexOf("OK\r") >= 0)
					{
						int i;
						String tmp = "";
		
						i = response.indexOf(":");
						while (!Character.isDigit(response.charAt(i)))
							i++;
						while (Character.isDigit(response.charAt(i)))
						{
							tmp += response.charAt(i);
							i++;
						}
						refNo = Integer.parseInt(tmp);
						break;
					}
					else if (response.indexOf("CMS ERROR:") >= 0)
					{
						errorRetries++;
						AtCmsError.log(log, response, pdu);
						if (errorRetries >= 4) {
							if (log != null) log.error("GSM CMS Errors: Quit retrying, message lost.");
							refNo = -1;
							break;
						}
						else if (log != null) log.error("GSM CMS Errors: Possible collision, retrying.  AT response: '" + response + "'");
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
