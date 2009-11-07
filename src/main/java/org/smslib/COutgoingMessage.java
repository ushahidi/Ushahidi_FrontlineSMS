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

import org.smslib.sms.SmsMessageEncoding;
import org.smslib.util.GsmAlphabet;
import org.smslib.util.TpduUtils;

/**
 * This class represents a normal (text) outgoing / outbound message.
 * PDU Type: SMS-SUBMIT
 */
public class COutgoingMessage extends CMessage {
	
//> INSTANCE PROPERTIES
	/** The date that this SMS is sent, in ms since the java epoch.  This is in GMT. */
	private Long dispatchDate;
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

//> CONSTRUCTORS
	/**
	 * Create a new {@link COutgoingMessage}
	 */
	protected COutgoingMessage() {
		super(CMessage.MessageType.Outgoing, null, null, "");
		this.messageEncoding = SmsMessageEncoding.GSM_7BIT;
	}

	/**
	 * General constructor for an outgoing message. Only the text and the recipient's number is required. The message encoding is set to 7bit by default.
	 * @param recipient The recipient's number - should be in international format.
	 * @param text The message text.
	 */
	public COutgoingMessage(String recipient, String text) {
		super(CMessage.MessageType.Outgoing, null, recipient, text);

		// For now, we decide RIGHT HERE whether we are using unicode or standard 7-bit GSM encoding.
		// In future, this should probably be set explicitly.
		if(GsmAlphabet.areAllCharactersValidGSM(text))
			this.messageEncoding = SmsMessageEncoding.GSM_7BIT;
		else this.messageEncoding = SmsMessageEncoding.UCS2;
	}

	/**
	 * Create a new {@link COutgoingMessage} with {@link SmsMessageEncoding#BINARY_8BIT} and a binary payload.
	 * @param recipient
	 * @param binary
	 */
	public COutgoingMessage(String recipient, byte[] binary) {
		super(CMessage.MessageType.Outgoing, null, recipient, binary);

		this.messageBinary = binary;
		this.messageEncoding = SmsMessageEncoding.BINARY_8BIT;
	}

//> ACCESSORS
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
	 * @param statusReportRequest <code>true</code> if you want to enable delivery status reports.
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
	 * @return The dispatch date, or <code>null</code> if {@link #dispatchDate} has not been set.
	 */
	public Long getDispatchDate() {
		return this.dispatchDate;
	}
	
	/** Set {@link #dispatchDate} to the current date & timé. */
	public void setDispatchDate() {
		this.dispatchDate = System.currentTimeMillis();
	}

	/**
	 * Generates the PDUs that this message should be sent as.
	 * @param smscNumber The telephone number of the SMS Centre
	 * @param mpRefNo The multipart reference number to be set in each PDU's UDH
	 * @return Ordered list of PDUs that this message should be sent as.
	 */
	protected String[] generatePdus(String smscNumber, int mpRefNo) {
		if(messageEncoding == SmsMessageEncoding.GSM_7BIT) {
			return TpduUtils.generatePdus_gsm7bit(messageText, smscNumber, this.getRecipient(), mpRefNo, sourcePort, destinationPort, this.statusReportRequest, validityPeriod, protocolIdentifier, this.getDcsByte());
		} else if(messageEncoding == SmsMessageEncoding.UCS2) {
			return TpduUtils.generatePdus_ucs2(messageText, smscNumber, this.getRecipient(), mpRefNo, sourcePort, destinationPort, this.statusReportRequest, validityPeriod, protocolIdentifier, this.getDcsByte());
		} else {
			// Treat custom encoding as 8-bit binary
			return TpduUtils.generatePdus_8bit(messageBinary, smscNumber, this.getRecipient(), mpRefNo, this.sourcePort, this.destinationPort, this.statusReportRequest, this.validityPeriod, this.protocolIdentifier, this.getDcsByte());	
		}
	}
}
