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

import java.io.Serializable;
import java.util.Date;

import org.smslib.v3.helper.GSMAlphabet;

/**
 * The parent of all message-related classes. Most of common fields and
 * attributes of both inbound and outbound messages are placed in this class.
 */
public class Message implements Serializable
{
	private static final long serialVersionUID = 6283844156557099044L;

	private String gtwId;

	private MessageTypes type;

	private Date date;

	private String id;

	private String text;

	private String encodedText;

	private MessageEncodings encoding;

	protected int pid;

	protected int dcs;

	protected int messageCharCount;

	protected byte[] binary;
	
	public Message(MessageTypes type, Date date, String text)
	{
		this.gtwId = "";
		this.type = type;
		this.id = "";
		setDate(date);
		this.text = text;
		this.encoding = MessageEncodings.ENC7BIT;
		messageCharCount = 0;
	}

	/**
	 * Returns the creation date. For outbound messages, this is the object's
	 * creation date. For inbound messages, this is the date when the originator
	 * has sent the message.
	 * 
	 * @return the creation date.
	 * @see #setDate(Date)
	 */
	public Date getDate()
	{
		return date == null ? null : new java.util.Date(date.getTime());
	}

	/**
	 * Sets the creation date to a specific date.
	 * 
	 * @param date
	 *            A custom date.
	 * @see #getDate()
	 */
	public void setDate(Date date)
	{
		this.date = (date != null ? new java.util.Date(date.getTime()) : null);
	}

	/**
	 * Returns the message's text encoded using the specified encoding.
	 * 
	 * @return The encoded message text.
	 */
	public String getEncodedText()
	{
		return encodedText;
	}

	void setEncodedText(String encodedText)
	{
		this.encodedText = encodedText;
	}

	/**
	 * Returns the message encoding.
	 * 
	 * @return The message encoding.
	 * @see #setEncoding(MessageEncodings)
	 * @see MessageEncodings
	 */
	public MessageEncodings getEncoding()
	{
		return encoding;
	}

	/**
	 * Sets the message encoding to the specified one.
	 * 
	 * @param encoding
	 *            The message encoding.
	 * @see #getEncoding()
	 * @see MessageEncodings
	 */
	public void setEncoding(MessageEncodings encoding)
	{
		this.encoding = encoding;
		encodedText = "";
		if ( (text == null || text.length() == 0) && encoding != MessageEncodings.ENC8BIT) return;
		if (binary == null && encoding == MessageEncodings.ENC8BIT) return;
		if (this.encoding == MessageEncodings.ENC7BIT)
		{
			encodedText = GSMAlphabet.textToPDU(text);
			messageCharCount = GSMAlphabet.noOfChars(text);
		}
		else if (this.encoding == MessageEncodings.ENC8BIT)
		{
			encodedText = toHexString(binary);
		}
		else if (this.encoding == MessageEncodings.ENCUCS2)
		{
			for (int i = 0; i < text.length(); i++)
			{
				char c = text.charAt(i);
				int high = (int) (c / 256);
				int low = c % 256;
				encodedText += ((Integer.toHexString(high).length() < 2) ? "0" + Integer.toHexString(high) : Integer.toHexString(high));
				encodedText += ((Integer.toHexString(low).length() < 2) ? "0" + Integer.toHexString(low) : Integer.toHexString(low));
			}
		}
		else if (this.encoding == MessageEncodings.ENCCUSTOM)
		{
			if ((dcs & 0x04) == 0) encodedText = GSMAlphabet.textToPDU(text);
			else
			{
				for (int i = 0; i < text.length(); i++)
				{
					char c = text.charAt(i);
					encodedText += ((Integer.toHexString(c).length() < 2) ? "0" + Integer.toHexString(c) : Integer.toHexString(c));
				}
			}
		}
	}

	/**
	 * Returns the ID of the gateway which the message was received from (for
	 * inbound messages) or the message was dispatched from (outbound messages).
	 * 
	 * @return The Gateway ID.
	 * @see #setGatewayId(String)
	 */
	public String getGatewayId()
	{
		return gtwId;
	}

	/**
	 * Sets the message's Gateway ID to a specific value.
	 * 
	 * @param gtwId
	 *            The Gateway ID.
	 * @see #getGatewayId()
	 */
	public void setGatewayId(String gtwId)
	{
		this.gtwId = gtwId;
	}

	/**
	 * Returns the message ID. This field can be used for your own purposes.
	 * 
	 * @return The message ID.
	 * @see #setId(String)
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Sets the message ID to a specific value.
	 * 
	 * @param id
	 *            The new message ID.
	 * @see #getId()
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Returns the message text.
	 * 
	 * @return The message text.
	 * @see #setText(String)
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * Sets the message text.
	 * 
	 * @param text
	 *            The message text.
	 * @see #getText()
	 */
	protected void setText(String text)
	{
		this.text = text;
	}

	public void addText(String text)
	{
		this.text += text;
	}

	/**
	 * Returns the message type.
	 * 
	 * @return The message type.
	 * @see MessageTypes
	 * @see #setType(MessageTypes)
	 */
	public MessageTypes getType()
	{
		return type;
	}

	void setType(MessageTypes type)
	{
		this.type = type;
	}
	
	static char[] hexChar = {
		'0' , '1' , '2' , '3' ,
		'4' , '5' , '6' , '7' ,
		'8' , '9' , 'A' , 'B' ,
		'C' , 'D' , 'E' , 'F' 
	};

	private String toHexString ( byte[] b ) {
		StringBuilder sb = new StringBuilder( b.length * 2 );
		for ( int i=0; i< b.length; i++ ) {
			sb.append(hexChar[(b[i] >> 4) & 0xF]);
			sb.append(hexChar[b[i] & 0xF]);
		}
		return sb.toString();
	}
}
