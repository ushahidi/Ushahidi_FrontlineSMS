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

package org.smslib;

import java.util.*;
import java.text.*;

public class CStatusReportMessage extends CIncomingMessage {
	private static final long serialVersionUID = 1L;

	/**
	 * Holds values representing the delivery status of a previously sent message.
	 */
	public static class DeliveryStatus {
		/** Unknown. */
		public static final int Unknown = 0;
		/** Message was delivered. */
		public static final int Delivered = 1;
		/** Message was not delivered, but the SMSC will keep trying. */
		public static final int KeepTrying = 2;
		/** Message was not delivered and the SMSC will abort it. */
		public static final int Aborted = 3;
	}
	/** (Presumably) the date the original message this refers to was sent */
	private long dateOriginal;
	/** The date this status report was received/or when it was generated? */
	private long dateReceived;
	/** Status that this report represents */
	private int status;

	/**
	 * Decodes the supplied SMS PDU to create a status report.
	 * @param pdu
	 * @param memIndex
	 * @param memLocation
	 */
	protected CStatusReportMessage(String pdu, int memIndex, String memLocation) {
		super(MessageType.StatusReport, memIndex, memLocation);

		int index, i, j, k;

		i = Integer.parseInt(pdu.substring(0, 2), 16);
		index = (i + 1) * 2;
		index += 2;
		refNo = Integer.parseInt(pdu.substring(index, index + 2), 16);
		index += 2;

		i = Integer.parseInt(pdu.substring(index, index + 2), 16);
		j = index + 4;
		recipient = "";
		for (k = 0; k < i; k += 2)
			recipient = recipient + pdu.charAt(j + k + 1) + pdu.charAt(j + k);
		recipient = "+" + recipient;
		if (recipient.charAt(recipient.length() - 1) == 'F') {
			recipient = recipient.substring(0, recipient.length() - 1);
			i++;
		}

		index = j + i;

		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
		String dateOctect = pdu.substring(index, index + 12);
		StringBuffer dateOk = new StringBuffer();
		for (int x = 0; x < 12; x = x + 2)
			dateOk.append(new char[]
			{ dateOctect.charAt(x + 1), dateOctect.charAt(x) });

		try {
			this.dateOriginal = sdf.parse(dateOk.toString()).getTime();
		} catch (ParseException e) {
			this.dateOriginal = -1;
		}
		index += 14;

		dateOctect = pdu.substring(index, index + 12);
		dateOk = new StringBuffer();
		for (int x = 0; x < 12; x = x + 2)
			dateOk.append(new char[]{dateOctect.charAt(x + 1), dateOctect.charAt(x)});

		try {
			this.dateReceived = sdf.parse(dateOk.toString()).getTime();
		} catch (ParseException e) {
			this.dateReceived = -1;
		}
		index += 14;

		i = Integer.parseInt(pdu.substring(index, index + 2), 16);

		switch((i >> 5) & 3) {
			case 0:
				messageText = "00 - Succesful Delivery.";
				status = DeliveryStatus.Delivered;
				break;
			case 1:
				messageText = "01 - Errors, will retry dispatch.";
				status = DeliveryStatus.KeepTrying;
				break;
			case 2:
				messageText = "02 - Errors, stopped retrying dispatch.";
				status = DeliveryStatus.Aborted;
				break;
			case 3:
				messageText = "03 - Errors, stopped retrying dispatch.";
				status = DeliveryStatus.Aborted;
				break;
		}
		
//		if ((i & 0x60) == 0)
//		{
//			text = "00 - Succesful Delivery.";
//			status = DeliveryStatus.Delivered;
//		}
//		if ((i & 0x20) == 0x20)
//		{
//			text = "01 - Errors, will retry dispatch.";
//			status = DeliveryStatus.KeepTrying;
//		}
//		if ((i & 0x40) == 0x40)
//		{
//			text = "02 - Errors, stopped retrying dispatch.";
//			status = DeliveryStatus.Aborted;
//		}
//		if ((i & 0x60) == 0x60)
//		{
//			text = "03 - Errors, stopped retrying dispatch.";
//			status = DeliveryStatus.Aborted;
//		}

		date = null;
	}

	protected CStatusReportMessage(int refNo, int memIndex, String memLocation, Date dateOriginal, Date dateReceived)
	{
		super(MessageType.StatusReport, memIndex, memLocation);

		this.refNo = refNo;
		this.dateOriginal = dateOriginal.getTime();
		this.dateReceived = dateReceived.getTime();
		messageText = "";
		status = DeliveryStatus.Unknown;
		date = null;
	}

	/**
	 * Gets the MSISDN this report refers to
	 */
	public String getOriginator() {
		return recipient;
	}

	/**
	 * Returns the delivery status flag.
	 * 
	 * @return The status flag.
	 */
	public int getDeliveryStatus() {
		return status;
	}

	/**
	 * Returns the date of the original SMS message for which a status report was requested.
	 * 
	 * @return The date of the original SMS message.
	 */
	public Date getDateOriginal() {
		return new Date(dateOriginal);
	}

	/**
	 * Returns the date when the original SMS message was received by recipient.
	 * 
	 * @return The date when the message was received.
	 */
	public Date getDateReceived() {
		return new Date(dateReceived);
	}
}
