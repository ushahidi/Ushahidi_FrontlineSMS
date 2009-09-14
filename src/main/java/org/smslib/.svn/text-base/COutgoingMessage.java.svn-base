//SMSLib for Java
//An open-source API Library for sending and receiving SMS via a GSM modem.
//Copyright (C) 2002-2007, Thanasis Delenikas, Athens/GREECE
//Copyright (C) 2009 Alex Anderson, Masabi Ltd.
//Web Site: http://www.smslib.org

//SMSLib is distributed under the LGPL license.

//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.

//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//Lesser General Public License for more details.

//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA

package org.smslib;

import java.util.*;


/**
 * This class represents a normal (text) outgoing / outbound message.
 * PDU Type: SMS-SUBMIT
 */
public class COutgoingMessage extends CMessage {
	/** The date that this SMS is sent */
	protected Date dispatchDate;
	/**
	 * The message validity period, in hours.  If set to 0 or a negative number, the maximum validity possible will be requested.
	 * Maps to: [Optional: TP-VP] TP-Validity-Period
	 * Parameter identifying the time from which the message is no longer valid.
	 */
	protected int validityPeriod;
	/**
	 * Maps to: [TP-SRR] TP-Status-Report-Request
	 * Parameter indicating if the MS is requesting a status report.
	 */
	protected boolean statusReportRequest;

	protected COutgoingMessage() {
		super(CMessage.MessageType.Outgoing, null, null, null, "");

		setDate(new Date());
		this.messageEncoding = MessageEncoding.Enc7Bit;
	}

	/**
	 * General constructor for an outgoing message. Only the text and the recipient's number is required. The message encoding is set to 7bit by default.
	 * @param recipient The recipient's number - should be in international format.
	 * @param text The message text.
	 */
	public COutgoingMessage(String recipient, String text) {
		super(CMessage.MessageType.Outgoing, new Date(), null, recipient, text);

		setDate(new Date());
		
		// For now, we decide RIGHT HERE whether we are using unicode or standard 7-bit GSM encoding.
		// In future, this should probably be set explicitly.
		if(GsmAlphabet.areAllCharactersValidGSM(text))
			this.messageEncoding = MessageEncoding.Enc7Bit;
		else this.messageEncoding = MessageEncoding.EncUcs2;
	}

	public COutgoingMessage(String recipient, byte[] binary) {
		super(CMessage.MessageType.Outgoing, new Date(), null, recipient, binary);

		this.messageBinary = binary;
		setDate(new Date());
		this.messageEncoding = MessageEncoding.Enc8Bit;
	}

	/**
	 * Sets the Recipient's number. The number should be in international format.
	 * @param recipient The Recipient's number.
	 */
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	/**
	 * Returns the message recipient number. Number is in international format.
	 * @return The Recipient's number.
	 */
	public String getRecipient() {
		return recipient;
	}

	/**
	 * Sets the validity period. By default, an outgoing message has the maximum allowed validity period.
	 * @param hours The validity period in hours.
	 */
	public void setValidityPeriod(int hours) {
		this.validityPeriod = hours;
	}

	/**
	 * Returns the defined validity period in hours.
	 * @return The validity period (hours).
	 */
	public int getValidityPeriod() {
		return validityPeriod;
	}

	/**
	 * Sets the delivery status report functionality. Set this to true if you want to enable delivery status report for this specific message.
	 * @param statusReport True if you want to enable delivery status reports.
	 */
	public void setStatusReport(boolean statusReportRequest) {
		this.statusReportRequest = statusReportRequest;
	}

	/**
	 * Returns the state of the delivery status report request.
	 * @return True if delivery status report request is enabled.
	 * @see #setValidityPeriod(int)
	 */
	public boolean getStatusReport() {
		return statusReportRequest;
	}

	/**
	 * Returns the date of dispatch - the date when this message was send from SMSLib. Returns NULL if the message has not been sent yet.
	 * 
	 * @return The dispatch date.
	 */
	public Date getDispatchDate() {
		if (dispatchDate != null) return (Date) dispatchDate.clone();
		else return null;
	}

	protected String[] generatePdus(String smscNumber, int mpRefNo) {
		switch(messageEncoding) {
			case MessageEncoding.Enc7Bit:
				return TpduUtils.generatePdus_gsm7bit(messageText, smscNumber, this.getRecipient(), mpRefNo, sourcePort, destinationPort, this.statusReportRequest, validityPeriod, protocolIdentifier, this.getDcsByte());
			case MessageEncoding.EncUcs2:
				return TpduUtils.generatePdus_ucs2(messageText, smscNumber, this.getRecipient(), mpRefNo, sourcePort, destinationPort, this.statusReportRequest, validityPeriod, protocolIdentifier, this.getDcsByte());
			default:
				return TpduUtils.generatePdus_8bit(messageBinary, smscNumber, this.getRecipient(), mpRefNo, this.sourcePort, this.destinationPort, this.statusReportRequest, this.validityPeriod, this.protocolIdentifier, this.getDcsByte());	
		}
	}
}
