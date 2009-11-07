// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2008, Thanasis Delenikas, Athens/GREECE.
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.smslib.v3;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class representing an delivery/status report message.
 */
public class StatusReportMessage extends InboundMessage
{
	private static final long serialVersionUID = 1766207060957595040L;

	private String recipient;

	private Date sent;

	private Date received;

	private DeliveryStatuses status;

	private String refNo;

	public StatusReportMessage(String pdu, int memIndex, String memLocation)
	{
		super(MessageTypes.STATUSREPORT, memIndex, memLocation);
		int index, i, j, k;
		i = Integer.parseInt(pdu.substring(0, 2), 16);
		index = (i + 1) * 2;
		index += 2;
		refNo = "" + Integer.parseInt(pdu.substring(index, index + 2), 16);
		index += 2;
		i = Integer.parseInt(pdu.substring(index, index + 2), 16);
		j = index + 4;
		String recipient = "";
		for (k = 0; k < i; k += 2)
			recipient = recipient + pdu.charAt(j + k + 1) + pdu.charAt(j + k);
		recipient = "+" + recipient;
		if (recipient.charAt(recipient.length() - 1) == 'F')
		{
			recipient = recipient.substring(0, recipient.length() - 1);
			i++;
		}
		this.recipient = recipient;
		index = j + i;
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
		String dateOctect = pdu.substring(index, index + 12);
		StringBuffer dateOk = new StringBuffer();
		for (int x = 0; x < 12; x = x + 2)
			dateOk.append(new char[] { dateOctect.charAt(x + 1), dateOctect.charAt(x) });
		try
		{
			this.sent = sdf.parse(dateOk.toString());
		}
		catch (ParseException e)
		{
			this.sent = null;
		}
		index += 14;
		dateOctect = pdu.substring(index, index + 12);
		dateOk = new StringBuffer();
		for (int x = 0; x < 12; x = x + 2)
			dateOk.append(new char[] { dateOctect.charAt(x + 1), dateOctect.charAt(x) });
		try
		{
			this.received = sdf.parse(dateOk.toString());
		}
		catch (ParseException e)
		{
			this.received = null;
		}
		index += 14;
		i = Integer.parseInt(pdu.substring(index, index + 2), 16);
		if ((i & 0x60) == 0)
		{
			setText("00 - Succesful Delivery.");
			status = DeliveryStatuses.DELIVERED;
		}
		if ((i & 0x20) == 0x20)
		{
			setText("01 - Errors, will retry dispatch.");
			status = DeliveryStatuses.KEEPTRYING;
		}
		if ((i & 0x40) == 0x40)
		{
			setText("02 - Errors, stopped retrying dispatch.");
			status = DeliveryStatuses.ABORTED;
		}
		if ((i & 0x60) == 0x60)
		{
			setText("03 - Errors, stopped retrying dispatch.");
			status = DeliveryStatuses.ABORTED;
		}
		setDate(null);
	}

	public StatusReportMessage(String refNo, int memIndex, String memLocation, Date dateOriginal, Date dateReceived)
	{
		super(MessageTypes.STATUSREPORT, memIndex, memLocation);
		this.refNo = refNo;
		this.sent = dateOriginal;
		this.received = dateReceived;
		setText("");
		status = DeliveryStatuses.UNKNOWN;
		setDate(null);
	}

	/**
	 * Returns the recipient of the original outbound message that created this
	 * status report.
	 * 
	 * @return The recipient of the original outbound message.
	 */
	public String getRecipient()
	{
		return recipient;
	}

	/**
	 * Returns the date that the recipient received the original outbound
	 * message.
	 * 
	 * @return The receive date.
	 * @see #getSent()
	 */
	public Date getReceived()
	{
		return new java.util.Date(received.getTime());
	}

	public void setReceived(Date received)
	{
		this.received = received;
	}

	/**
	 * Returns the date when the original outbound message was sent.
	 * 
	 * @return The sent date.
	 * @see #getReceived()
	 */
	public Date getSent()
	{
		return new java.util.Date(sent.getTime());
	}

	public void setSent(Date sent)
	{
		this.sent = sent;
	}

	/**
	 * The status of the original outbound message. Use this field to see what
	 * happened with the original message.
	 * 
	 * @return The status of the outbound message;
	 * @see DeliveryStatuses
	 */
	public DeliveryStatuses getStatus()
	{
		return status;
	}

	public void setStatus(DeliveryStatuses status)
	{
		this.status = status;
	}

	/**
	 * Returns the Reference Number of the original outbound message that this
	 * status report refers to.
	 * 
	 * @return The Reference Number of the original outbound message.
	 */
	public String getRefNo()
	{
		return refNo;
	}
}
