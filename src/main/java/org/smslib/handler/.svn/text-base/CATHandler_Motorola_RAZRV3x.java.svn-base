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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.smslib.*;
import org.apache.log4j.*;

public class CATHandler_Motorola_RAZRV3x extends CATHandler
{
	public CATHandler_Motorola_RAZRV3x(CSerialDriver serialDriver, Logger log, CService srv)
	{
		super(serialDriver, log, srv);
	}

	@Override
	protected String listMessages(int messageClass) throws IOException, UnrecognizedHandlerProtocolException {
		String response = super.listMessages(messageClass);
		BufferedReader reader = new BufferedReader(new StringReader(response));
		String line;
		StringBuilder resp = new StringBuilder();
		while ( (line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() > 0) {
				if (!line.toUpperCase().contains("ERROR")) {
					resp.append(line).append("\n");
				}
			}
		}
		resp.append("OK");
		return resp.toString();
	}
}
