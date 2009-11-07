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

import org.smslib.sms.SmsMessageEncoding;
import org.smslib.util.TpduUtils;

/**
 * This is the parent class defining a generic SMS message. In almost all cases, you will not work with this class, except for calling some methods for accessing info fields common for all types of messages.
 * 
 * @see CIncomingMessage
 * @see COutgoingMessage
 * @see CStatusReportMessage
 */
public abstract class CMessage {
	/** Holds values representing message types. */
	public static class MessageType {
		/** Incoming (inbound) message. */
		public static final int Incoming = 1;
		/** Outgoing (outbound) message. */
		public static final int Outgoing = 2;
		/** Delivery status report message. */
		public static final int StatusReport = 3;
	}
	
//> INSTANCE PROPERTIES
	/** The type of message, from {@link MessageType}.  TODO {@link MessageType} should probably be an enum. */
	protected final int type;
	protected int refNo;
	protected String id;
	/**
	 * The date this message was created as a java timestamp.  This time is in UTC.
	 * This value should only be changed by incoming messages.
	 */
	protected long date = System.currentTimeMillis();
	/** The originator of the message */
	protected String originator;
	/** The recipient of the message */
	protected String recipient;
	/** message content of a text message */
	protected String messageText;
	/** data content of a binary message */
	protected byte[] messageBinary;
	/** The encoding of the message, from {@link SmsMessageEncoding}. */
	protected SmsMessageEncoding messageEncoding;
	/** Port to show this SMS as sent from */
	protected int sourcePort;
	/** Port to show this SMS as sent to */
	protected int destinationPort;
	/**
	 * [MANDATORY: TP-PID] TP-Protocol-Identifier. 
	 * Parameter identifying the above layer protocol, if any.
	 */
	protected int protocolIdentifier;
	/**
	 * [MANDATORY: TP-DCS] TP-Data-Coding-Scheme
	 * Parameter identifying the coding scheme within the TP-User-Data.
	 * @deprecated DCS should never be explicitly set - should be generated from other properties, especially {@link #getDcsByte()}.
	 */
	protected int dataCodingScheme;

//> CONSTRUCTORS
	/**
	 * Creates a new {@link CMessage} with unspecified content and encoding.
	 * @param type the {@link MessageType} of the message
	 * @param originator the originator
	 * @param recipient the recipient
	 */
	protected CMessage(int type, String originator, String recipient) {
		this.type = type;
		this.originator = originator;
		this.recipient = recipient;
	}

	/**
	 * Creates a new {@link CMessage} with {@link SmsMessageEncoding#GSM_7BIT}.
	 * @param type the {@link MessageType} of the message
	 * @param originator the originator
	 * @param recipient the recipient
	 * @param text text content of the message
	 */
	protected CMessage(int type, String originator, String recipient, String text) {
		this(type, originator, recipient);
		this.messageText = text;
		this.messageEncoding = SmsMessageEncoding.GSM_7BIT;
	}

	/**
	 * Creates a new {@link CMessage} with {@link SmsMessageEncoding#BINARY_8BIT}.
	 * @param type the {@link MessageType} of the message
	 * @param originator the originator
	 * @param recipient the recipient
	 * @param binary binary content of the message
	 */
	protected CMessage(int type, String originator, String recipient, byte[] binary) {
		this(type, originator, recipient);
		this.messageBinary = binary;
		this.messageEncoding = SmsMessageEncoding.BINARY_8BIT;
	}
	
//> ACCESSORS
	/**
	 * Returns the message type.
	 * @return The message type.
	 * @see MessageType
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the message SMSC Reference Number.
	 * @return The SMSC Ref Number.
	 */
	public int getRefNo() {
		return refNo;
	}

	/**
	 * Returns the message id.
	 * @return The message id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the message text.
	 * @return The message text.
	 */
	public String getText() {
		return messageText;
	}

	/**
	 * Returns the message encoding.
	 * @return The message encoding.
	 * @see SmsMessageEncoding
	 */
	public SmsMessageEncoding getMessageEncoding() {
		return messageEncoding;
	}
	
	/** @return {@link #date} */
	public Long getDate() {
		return date;
	}

	/**
	 * TODO not sure why it's useful to set the date to <code>null</code>; maybe this should be prevented.
	 * @param date */
	public void setDate(Long date) {
		this.date = date;
	}

	/**
	 * Sets the id of the message.
	 * <p>
	 * The message id is a user-defined id field. It is not used in actual
	 * sending or receiving of a message. So, even if you don't define it,
	 * SMSLib will work normally. Use it if you need to differentiate messages
	 * in some way.
	 * @param id The message id.
	 * @see #getId()
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the message text.
	 * @param text The message text.
	 */
	public void setText(String text) {
		this.messageText = text;
	}

	/**
	 * Sets the message encoding.
	 * @param messageEncoding The message encoding, one of {@link SmsMessageEncoding}.
	 */
	public void setMessageEncoding(SmsMessageEncoding messageEncoding) {
		this.messageEncoding = messageEncoding;
	}

	protected void setRefNo(int refNo) {
		this.refNo = refNo;
	}
	
	/**
	 * Sets the message Protocol Id. Normally, you should not care about this value - by default it is set to 0 which is appropriate for normal SMS messages.
	 * @param protocolIdentifier The Protocol Id.
	 * @see #getPid()
	 */
	public void setPid(int pid) {
		this.protocolIdentifier = pid;
	}

	/**
	 * Returns the message's Protocol id.
	 * @return The Protocol id.
	 * @see #setPid(int)
	 */
	public int getPid() {
		return protocolIdentifier;
	}

	/**
	 * A message-to-string mapping function. Used for debugging and for easy viewing of of message object's info fields.
	 * 
	 * TODO refactor this into the subclasses
	 */
	public String toString()
	{
		String str = "";

		str += "===============================================================================";
		str += "\n";
		str += "<< MESSAGE DUMP >>";
		str += "\n";
		str += "-------------------------------------------------------------------------------";
		str += "\n";
		str += " Type: " + (type == MessageType.Incoming ? "Incoming" : (type == MessageType.Outgoing ? "Outgoing" : "Status Report"));
		str += "\n";
		str += " Encoding: " + messageEncoding.toString();
		str += "\n";
		str += " Date: " + date;
		str += "\n";
		str += " Originator: " + originator;
		str += "\n";
		str += " Recipient: " + recipient;
		str += "\n";
		str += " Message Text: " + messageText;
		str += "\n";
		str += " SMSC Ref No: " + refNo;
		str += "\n";
		if (type == MessageType.Incoming)
		{
			CIncomingMessage msg = (CIncomingMessage) this;
			str += " Memory Index: " + msg.getMemIndex();
			str += "\n";
			str += " Multi-part Memory Index: " + msg.getMpMemIndex();
			str += "\n";
			str += " Memory Location: " + msg.getMemLocation();
			str += "\n";
		}
		if (type == MessageType.Outgoing)
		{
			COutgoingMessage msg = (COutgoingMessage) this;
			str += " Dispatch Date: " + msg.getDispatchDate();
			str += "\n";
			str += " Validity Period (Hours): " + msg.getValidityPeriod();
			str += "\n";
			str += " Status Report: " + msg.getStatusReport();
			str += "\n";
			str += " Source / Destination Ports: " + msg.getSourcePort() + " / " + msg.getDestinationPort();
			str += "\n";
		}
		if (type == MessageType.StatusReport)
		{
			CStatusReportMessage msg = (CStatusReportMessage) this;
			str += " Date Sent: " + msg.getDateOriginal();
			str += "\n";
			str += " Date Received (by recipient): " + msg.getDateReceived();
			str += "\n";
			str += " Delivery Status: ";
			if (msg.getDeliveryStatus() == CStatusReportMessage.DeliveryStatus.Delivered) str += "Delivered";
			else if (msg.getDeliveryStatus() == CStatusReportMessage.DeliveryStatus.KeepTrying) str += "Not delivered yet - will keep trying.";
			else if (msg.getDeliveryStatus() == CStatusReportMessage.DeliveryStatus.Aborted) str += "Not delivered - aborted.";
			str += "\n";
		}

		str += "===============================================================================";
		str += "\n";
		return str;
	}

	public byte[] getBinary() {
		return messageBinary;
	}

	public void setBinary(byte[] messageBinary) {
		this.messageBinary = messageBinary;
	}
	
	/**
	 * Generates the DCS octet for this message.
	 * [TP-DCS: TP-Data-CodingScheme] Parameter identifying the coding scheme within the TP-User-Data.
	 * @return
	 */
	protected final int getDcsByte() {
		if(this.messageEncoding == SmsMessageEncoding.EncCustom) {
			if(this.dataCodingScheme == 0) throw new IllegalSmsEncodingException("TP-DCS (Data-Coding-Scheme) must not be zero for custom encoding.");
			return this.dataCodingScheme;
		} else {
			return TpduUtils.getDcsByte(this.messageEncoding);
		}
	}
	


	/**
	 * Sets the source port for this message.
	 * @param port The Source Port.
	 */
	public void setSourcePort(int port) {
		// Check the port fits within 16 bits
		if(port != (port & 0xFFFF)) throw new IllegalArgumentException("Illegal source port value: " + port);
		this.sourcePort = port;
	}

	/** The source port for this message. */
	public int getSourcePort() {
		return this.sourcePort;
	}

	/**
	 * Sets the destination port for this message.
	 * @param port The Destination Port.
	 */
	public void setDestinationPort(int port) {
		// Check the port fits within 16 bits
		if(port != (port & 0xFFFF)) throw new IllegalArgumentException("Illegal destination port value: " + port);
		this.destinationPort = port;
	}

	/** The destination port for this message. */
	public int getDestinationPort() {
		return this.destinationPort;
	}
}