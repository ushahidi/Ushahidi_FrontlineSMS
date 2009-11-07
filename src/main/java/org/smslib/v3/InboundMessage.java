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

import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;

import org.smslib.v3.helper.GSMAlphabet;

/**
 * Class representing an inbound sms message.
 */
public class InboundMessage extends Message
{
	private static final long serialVersionUID = -1961987130095782409L;

	private String originator;

	private int memIndex;

	private String memLocation;

	private int mpRefNo;

	private int mpMaxNo;

	private int mpSeqNo;

	private String mpMemIndex;

	private String pduUserData;

	public InboundMessage(Date date, String originator, String text, int memIndex, String memLocation)
	{
		super(MessageTypes.INBOUND, date, text);
		this.originator = originator;
		this.memIndex = memIndex;
		this.memLocation = memLocation;
		mpRefNo = 0;
		mpMaxNo = 0;
		mpSeqNo = 0;
		mpMemIndex = "";
	}

	public InboundMessage(MessageTypes type, int memIndex, String memLocation)
	{
		super(type, null, null);
		this.originator = "";
		this.memIndex = memIndex;
		this.memLocation = memLocation;
		mpRefNo = 0;
		mpMaxNo = 0;
		mpSeqNo = 0;
		mpMemIndex = "";
	}

	// FIXME unused method, can remove.
	public InboundMessage(String pdu, int memIndex, String memLocation)
	{
		super(MessageTypes.INBOUND, null, null);
		Date date;
		String originator;
		String str1, str2;
		int index, i, j, k, protocol, addr, year, month, day, hour, min, sec, skipBytes;
		boolean hasUDH;
		int UDHLength;
		String UDHData;
		byte[] bytes;
		this.memIndex = memIndex;
		this.memLocation = memLocation;
		mpRefNo = 0;
		mpMaxNo = 0;
		mpSeqNo = 0;
		mpMemIndex = "";
		skipBytes = 0;
		i = Integer.parseInt(pdu.substring(0, 2), 16);
		index = (i + 1) * 2;
		hasUDH = ((Integer.parseInt(pdu.substring(index, index + 2), 16) & 0x40) != 0) ? true : false;
		index += 2;
		i = Integer.parseInt(pdu.substring(index, index + 2), 16);
		j = index + 4;
		originator = "";
		for (k = 0; k < i; k += 2)
			originator = originator + pdu.charAt(j + k + 1) + pdu.charAt(j + k);
		originator = "+" + originator;
		if (originator.charAt(originator.length() - 1) == 'F') originator = originator.substring(0, originator.length() - 1);
		addr = Integer.parseInt(pdu.substring(j - 2, j), 16);
		if ((addr & (1 << 6)) != 0 && (addr & (1 << 5)) == 0 && (addr & (1 << 4)) != 0)
		{
			str1 = pduToText(pdu.substring(j, j + k));
			bytes = new byte[str1.length()];
			for (i = 0; i < str1.length(); i++)
				bytes[i] = (byte) str1.charAt(i);
			originator = GSMAlphabet.bytesToString(bytes);
		}
		index = j + k + 2;
		str1 = "" + pdu.charAt(index) + pdu.charAt(index + 1);
		protocol = Integer.parseInt(str1, 16);
		index += 2;
		year = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index));
		index += 2;
		month = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index));
		index += 2;
		day = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index));
		index += 2;
		hour = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index));
		index += 2;
		min = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index));
		index += 2;
		sec = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index));
		index += 4;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year + 2000);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, sec);
		date = cal.getTime();
		if (hasUDH)
		{
			UDHLength = Integer.parseInt(pdu.substring(index + 2, index + 2 + 2), 16);
			UDHData = pdu.substring(index + 2 + 2, index + 2 + 2 + (UDHLength * 2));
			if (UDHData.substring(0, 2).equalsIgnoreCase("00"))
			{
				mpRefNo = Integer.parseInt(UDHData.substring(4, 6), 16);
				mpMaxNo = Integer.parseInt(UDHData.substring(6, 8), 16);
				mpSeqNo = Integer.parseInt(UDHData.substring(8, 10), 16);
				skipBytes = 7;
			}
			else if (UDHData.substring(0, 2).equalsIgnoreCase("08"))
			{
				mpRefNo = Integer.parseInt(UDHData.substring(4, 8), 16);
				mpMaxNo = Integer.parseInt(UDHData.substring(8, 10), 16);
				mpSeqNo = Integer.parseInt(UDHData.substring(10, 12), 16);
				skipBytes = 8;
			}
		}
		else
		{
			UDHLength = 0;
			UDHData = "";
		}
		switch (protocol & 0x0C)
		{
			case 0:
				setEncoding(MessageEncodings.ENC7BIT);
				str1 = pduToText(pdu.substring(index + 2));
				pduUserData = pdu.substring(index + 2 + UDHLength);
				bytes = new byte[str1.length()];
				for (i = 0; i < str1.length(); i++)
					bytes[i] = (byte) str1.charAt(i);
				str2 = GSMAlphabet.bytesToString(bytes);
				if (hasUDH) str1 = str2.substring(UDHLength + 2);
				else str1 = str2;
				break;
			case 4:
				setEncoding(MessageEncodings.ENC8BIT);
				index += 2;
				if (hasUDH) index += UDHLength + skipBytes;
				pduUserData = pdu.substring(index);
				str1 = "";
				while (index < pdu.length())
				{
					i = Integer.parseInt("" + pdu.charAt(index) + pdu.charAt(index + 1), 16);
					str1 = str1 + (char) i;
					index += 2;
				}
				break;
			case 8:
				setEncoding(MessageEncodings.ENCUCS2);
				index += 2;
				if (hasUDH) index += UDHLength + skipBytes;
				pduUserData = pdu.substring(index);
				str1 = "";
				while (index < pdu.length())
				{
					i = Integer.parseInt("" + pdu.charAt(index) + pdu.charAt(index + 1), 16);
					j = Integer.parseInt("" + pdu.charAt(index + 2) + pdu.charAt(index + 3), 16);
					str1 = str1 + (char) ((i * 256) + j);
					index += 4;
				}
				break;
		}
		this.setOriginator(originator);
		this.setDate(date);
		this.setText(str1);
	}

	private String pduToText(String pdu)
	{
		String text;
		byte oldBytes[], newBytes[];
		BitSet bitSet;
		int i, j, value1, value2;
		oldBytes = new byte[pdu.length() / 2];
		for (i = 0; i < pdu.length() / 2; i++)
		{
			oldBytes[i] = (byte) (Integer.parseInt(pdu.substring(i * 2, (i * 2) + 1), 16) * 16);
			oldBytes[i] += (byte) Integer.parseInt(pdu.substring((i * 2) + 1, (i * 2) + 2), 16);
		}
		bitSet = new BitSet(pdu.length() / 2 * 8);
		value1 = 0;
		for (i = 0; i < pdu.length() / 2; i++)
			for (j = 0; j < 8; j++)
			{
				value1 = (i * 8) + j;
				if ((oldBytes[i] & (1 << j)) != 0) bitSet.set(value1);
			}
		value1++;
		value2 = value1 / 7;
		if (value2 == 0) value2++;
		newBytes = new byte[value2];
		for (i = 0; i < value2; i++)
			for (j = 0; j < 7; j++)
				if ((value1 + 1) > (i * 7 + j)) if (bitSet.get(i * 7 + j)) newBytes[i] |= (byte) (1 << j);
		if (newBytes[value2 - 1] == 0) text = new String(newBytes, 0, value2 - 1);
		else text = new String(newBytes);
		return text;
	}

	/**
	 * Returns the originator of this message.
	 * 
	 * @return The originator of this message.
	 */
	public String getOriginator()
	{
		return originator;
	}

	void setOriginator(String originator)
	{
		this.originator = originator;
	}

	/**
	 * Returns the GSM Modem/Phone memory index from which this message was
	 * read.
	 * 
	 * @return The memory index.
	 * @see #getMemLocation()
	 */
	public int getMemIndex()
	{
		return memIndex;
	}

	public void setMemIndex(int memIndex)
	{
		this.memIndex = memIndex;
	}

	/**
	 * Returns the GSM Modem/Phone memory location from which this message was
	 * read.
	 * 
	 * @return The memory location identifier.
	 * @see #getMemIndex()
	 */
	public String getMemLocation()
	{
		return memLocation;
	}

	public void setMemLocation(String memLocation)
	{
		this.memLocation = memLocation;
	}

	public int getMpMaxNo()
	{
		return mpMaxNo;
	}

	public void setMpMaxNo(int mpMaxNo)
	{
		this.mpMaxNo = mpMaxNo;
	}

	public String getMpMemIndex()
	{
		return mpMemIndex;
	}

	public void setMpMemIndex(int mpMemIndex)
	{
		this.mpMemIndex += (this.mpMemIndex.length() == 0 ? "" : ",") + mpMemIndex;
	}

	public int getMpRefNo()
	{
		return mpRefNo;
	}

	public void setMpRefNo(int mpRefNo)
	{
		this.mpRefNo = mpRefNo;
	}

	public int getMpSeqNo()
	{
		return mpSeqNo;
	}

	public void setMpSeqNo(int mpSeqNo)
	{
		this.mpSeqNo = mpSeqNo;
	}

	/**
	 * Returns the raw PDU data block of this message.
	 * 
	 * @return The PDU data of this message.
	 */
	public String getPduUserData()
	{
		return pduUserData;
	}

	public void setPduUserData(String pduUserData)
	{
		this.pduUserData = pduUserData;
	}

	public void addPduUserData(String pduUserData)
	{
		this.pduUserData += pduUserData;
	}

	public String toString()
	{
		String str = "";
		str += "===============================================================================";
		str += "\n";
		str += "<< INBOUND MESSAGE DUMP >>";
		str += "\n";
		str += "-------------------------------------------------------------------------------";
		str += "\n";
		str += " Gateway Id: " + getGatewayId();
		str += "\n";
		str += " Encoding: " + (getEncoding() == MessageEncodings.ENC7BIT ? "7-bit" : (getEncoding() == MessageEncodings.ENC8BIT ? "8-bit" : "UCS2 (Unicode)"));
		str += "\n";
		str += " Date: " + getDate();
		str += "\n";
		str += " Text: " + getText();
		str += "\n";
		str += " PDU data: " + getPduUserData();
		str += "\n";
		str += " Originator: " + originator;
		str += "\n";
		str += " Memory Index: " + getMemIndex();
		str += "\n";
		str += " Multi-part Memory Index: " + getMpMemIndex();
		str += "\n";
		str += " Memory Location: " + getMemLocation();
		str += "\n";
		str += "===============================================================================";
		str += "\n";
		return str;
	}
}
