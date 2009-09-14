// SMSLib for Java
// An open-source API Library for sending and receiving SMS via a GSM modem.
// Copyright (C) 2002-2007, Thanasis Delenikas, Athens/GREECE
// Copyright (C) 2008-2009, Alex Anderson, Masabi Ltd.
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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.frontlinesms.hex.HexUtils;


public class CIncomingMessage extends CMessage {
	/**
	 * Value of {@link #mpRefNo} when it has not been explicitly set. 
	 * This should be the value of {@link #mpRefNo} in <em>all</em> single part messages,
	 * and <em>never</em> for concatenated message parts.
	 */
	public static final int CONCAT_REFERENCE_NUMBER_NOT_SET = -1;
	
	private int memIndex;
	private String memLocation;
	private int mpRefNo = CONCAT_REFERENCE_NUMBER_NOT_SET;
	private int mpMaxNo;
	private int mpSeqNo;
	/** List of memory indexes that a multipart message's parts are located at.  This is not used for single-part messages. */
	private final List<Integer> mpMemIndex = new ArrayList<Integer>();
	private String pduUserData;

	public CIncomingMessage(Date date, String originator, String text) {
		this(date, originator, text, 0, "");
	}

	public CIncomingMessage(Date date, String originator, String text, int memIndex, String memLocation) {
		super(MessageType.Incoming, date, originator, null, text);

		this.memIndex = memIndex;
		this.memLocation = memLocation;
	}

	protected CIncomingMessage(int messageType, int memIndex, String memLocation) {
		super(messageType, null, null, null, "");

		this.memIndex = memIndex;
		this.memLocation = memLocation;
	}
	
	protected CIncomingMessage(String pdu, int memIndex, String memLocation) throws MessageDecodeException {
		super(MessageType.Incoming, null, null, null, "");

		this.memIndex = memIndex;
		this.memLocation = memLocation;
		
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(HexUtils.decode(pdu));
			DataInputStream in = new DataInputStream(bais);
			
			String smscNumber = TpduUtils.decodeMsisdnFromAddressField(in, true);
			
			// get the front byte, which identifies message content
			byte byteZero = (byte)in.read();
			boolean hasUdh = (byteZero & TpduUtils.TP_UDHI) != 0;
	
			/** [TP-OA: TP-Originating-Address] Address of the originating SME. */
			String originator = TpduUtils.decodeMsisdnFromAddressField(in, false);
			this.originator = originator;
	
			/** [TP-PID: TP-Protocol-Identifier] Parameter identifying the above layer protocol, if any. */
			int protocolIdentifier = in.read();
			this.protocolIdentifier = protocolIdentifier;
	
			/** [TP-DCS: TP-Data-CodingScheme] Parameter identifying the coding scheme within the TP-User-Data. */
			int dataCodingScheme = in.read();
			this.messageEncoding = TpduUtils.getMessageEncoding(dataCodingScheme);
			
			/** [TP-SCTS: TP-Service-Centre-Time-Stamp] Parameter identifying time when the SC received the message. */
			long serviceCentreTimeStamp = TpduUtils.decodeServiceCentreTimeStamp(in);
			this.date = new Date(serviceCentreTimeStamp);
			
			/** [TP-UDL: TP-User-Data-Length] Length of the UD, specific to the encoding. */
			int userDataLength = in.read();
			
			int udhLength;
			if (hasUdh) {
				udhLength = in.read();
				int headerOctetsRemaining = udhLength;
				while(headerOctetsRemaining > 0) {
					int informationElementIdentifier = in.read();
					int informationElementLength = in.read();
					headerOctetsRemaining -= 2 + informationElementLength;
					switch(informationElementIdentifier) {
					case TpduUtils.TP_UDH_IEI_APP_PORTING_16BIT:
						// Message ports have been set
						this.setDestinationPort(in.readUnsignedShort());
						this.setSourcePort(in.readUnsignedShort());
						break;
					case TpduUtils.TP_UDH_IEI_CONCAT_SMS_8BIT:
						// This is part of a multipart/concatenated message
						this.mpRefNo = in.read();
						this.mpMaxNo = in.read();
						this.mpSeqNo = in.read();
						break;
					case TpduUtils.TP_UDH_IEI_CONCAT_SMS_16BIT:
						// This is part of a multipart/concatenated message
						this.mpRefNo = in.readUnsignedShort();
						this.mpMaxNo = in.read();
						this.mpSeqNo = in.read();
						break;
					case TpduUtils.TP_UDH_IEI_WIRELESS_MESSAGE_CONTROL_PROTOCOL:
						// N.B. No devices we have used will allow access to the WAP SI inbox using AT commands, so this is much of a muchness right now
						/* Drop through to unknown handling */
					default:
						// If an IE is not recognised, it should be ignored.
						while(--informationElementLength >= 0) in.read();
					}
				}
				
				if(headerOctetsRemaining != 0) {
					// The UDH was a different length to that expected.
					throw new MessageDecodeException("UDH Length was different to that expected - there are " + headerOctetsRemaining + " octets remaining after UDH processing.");
				}
				
				++udhLength; // Add 1 to the UDH length - now it includes the length octet itself
			} else {
				udhLength = 0;
			}

			int skipBit = GsmAlphabet.calculateBitSkip(udhLength);
			
			/** For GSM 7-bit encoded messages, we need to know the number of septets comprising the actual message. */
			int msSeptetCount = 0; 
			/** The length in octets of the data contained in the MS */
			int payloadLength;
			if(messageEncoding == MessageEncoding.Enc7Bit) {
				// For 7-bit GSM alphabet messages, the TP-UDL is the number of characters in the MS
				// plus the number of octets in the UDH, including the UDH's length octet.
				int totalOctets = (int)Math.ceil((userDataLength * 7) / 8.0);
				payloadLength = totalOctets - udhLength;
				int udhSeptets = (int)Math.ceil((udhLength * 8) / 7.0);
				msSeptetCount = userDataLength - udhSeptets;
			} else {
				// For binary and UCS-2 messages, the TP-UDL is the total number of octets in the UD
				payloadLength = userDataLength - udhLength;
			}
			byte[] udWithoutHeader = new byte[payloadLength];
			in.readFully(udWithoutHeader);
			
			if(in.read() != -1) {
				throw new MessageDecodeException("There were unexpected bytes at the end of this message.");
			}
			
			switch(messageEncoding) {
			case MessageEncoding.Enc7Bit:
				// The position of the MS data actually depends on the length of the UDH, for some reason
				String messageText = GsmAlphabet.bytesToString(GsmAlphabet.octetStream2septetStream(udWithoutHeader, skipBit, msSeptetCount));
				this.messageText = messageText;
				break;
			default:
				/* Custom message encoding - treat as binary */ 
			case MessageEncoding.Enc8Bit:
				this.messageBinary = udWithoutHeader;
				break;
			case MessageEncoding.EncUcs2:
				this.messageText = TpduUtils.decodeUcs2Text(udWithoutHeader);
				break;
			}
		} catch(MessageDecodeException ex) {
			// This exception is already handled
			throw ex;
		} catch(Throwable t) {
			// Unhandled Throwables are wrapped in an SmsDecodeException so they can be handled
			// properly at the next level.
			throw new MessageDecodeException("Error decoding PDU", t);
		}
	}

	protected void setMpSeqNo(int mpSeqNo) {
		this.mpSeqNo = mpSeqNo;
	}

	protected void setMemIndex(int memIndex) {
		this.memIndex = memIndex;
	}

	protected void addMpMemIndex(int memIndex) {
		this.mpMemIndex.add(memIndex);
	}

	/**
	 * Returns the Originator's number. Number is in international format or in text format.
	 * @return The Originator's number.
	 */
	public String getOriginator() {
		return originator;
	}

	/**
	 * @return The memory index of the message.
	 * @see #getMemLocation()
	 */
	public int getMemIndex() {
		return memIndex;
	}

	/**
	 * Returns the memory location of the message.
	 * <p>
	 * Memory location is a two-char identifier (i.e. SM, SR, etc) which denotes the memory storage of the phone.
	 * @return The memory location.
	 */
	public String getMemLocation() {
		return memLocation;
	}

	/**
	 * Return the raw PDU data (excluding the pdu user data header, if available)
	 * @return The PDU user data.
	 */
	public String getPDUUserData() {
		return pduUserData;
	}

	protected int getMpRefNo() {
		return mpRefNo;
	}

	protected int getMpMaxNo() {
		return mpMaxNo;
	}

	protected int getMpSeqNo() {
		return mpSeqNo;
	}

	protected Integer[] getMpMemIndex() {
		return mpMemIndex.toArray(new Integer[mpMemIndex.size()]);
	}
}
