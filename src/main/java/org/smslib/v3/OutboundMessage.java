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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.smslib.util.GsmAlphabet;

/**
 * Class representing an outbound sms message.
 */
public class OutboundMessage extends Message
{
	private static final long serialVersionUID = -5726955467519470176L;

	private String recipient;

	private Date dispatchDate;

	private int validityPeriod;

	private boolean statusReport;

	private boolean flashSms;

	private int srcPort;

	private int dstPort;

	private String from;

	private MessageStatuses messageStatus;

	private FailureCauses failureCause;

	private int retryCount;

	private MessagePriorities priority;

	private String refNo;

	private String UDH;
	private String UD;

	/**
	 * Outbound message constructor. This parameterless constructor creates an
	 * empty outbound message.
	 * 
	 * @see #OutboundMessage(String, String)
	 */
	public OutboundMessage()
	{
		super(MessageTypes.OUTBOUND, null, null);
		recipient = "";
		validityPeriod = -1;
		statusReport = false;
		flashSms = false;
		srcPort = -1;
		dstPort = -1;
		from = "";
		pid = 0;
		dcs = 0;
		dispatchDate = null;
		setDate(new Date());
		setEncoding(MessageEncodings.ENC7BIT);
		messageStatus = MessageStatuses.UNSENT;
		failureCause = FailureCauses.NO_ERROR;
		retryCount = 0;
		priority = MessagePriorities.NORMAL;
		refNo = "";
	}

	/**
	 * Outbound message constructor.
	 * 
	 * @param recipient
	 *            The recipient of the message.
	 * @param text
	 *            The text of the message.
	 */
	public OutboundMessage(String recipient, String text)
	{
		super(MessageTypes.OUTBOUND, new Date(), text);
		this.recipient = recipient;
		validityPeriod = -1;
		statusReport = false;
		flashSms = false;
		srcPort = -1;
		dstPort = -1;
		from = "";
		pid = 0;
		dcs = 0;
		dispatchDate = null;
		setDate(new Date());
		if(GsmAlphabet.areAllCharactersValidGSM(text)) {
			setEncoding(MessageEncodings.ENC7BIT);
		} else {
			setEncoding(MessageEncodings.ENCUCS2);
		}
		messageStatus = MessageStatuses.UNSENT;
		failureCause = FailureCauses.NO_ERROR;
		retryCount = 0;
		priority = MessagePriorities.NORMAL;
		refNo = "";
	}

	public OutboundMessage(String recipient, byte[] binary)
	{
		super(MessageTypes.OUTBOUND, new Date(), "");
		this.recipient = recipient;
		this.binary = binary;
		validityPeriod = -1;
		statusReport = false;
		flashSms = false;
		srcPort = -1;
		dstPort = -1;
		from = "";
		pid = 0;
		dcs = 0;
		dispatchDate = null;
		setDate(new Date());
		setEncoding(MessageEncodings.ENC8BIT);
		messageStatus = MessageStatuses.UNSENT;
		failureCause = FailureCauses.NO_ERROR;
		retryCount = 0;
		priority = MessagePriorities.NORMAL;
		refNo = "";
	}
	
	public boolean isBig()
	{
		int messageLength;
		messageLength = getEncodedText().length() / 2;
		return (messageLength > maxSize() ? true : false);
	}

	public int getNoOfParts()
	{
		int noOfParts = 0;
		int partSize;
		int messageLength;
		partSize = maxSize() - 8;
		messageLength = getEncodedText().length() / 2;
		noOfParts = messageLength / partSize;
		if ((noOfParts * partSize) < (messageLength)) noOfParts++;
		return noOfParts;
	}

	int maxSize()
	{
		return 140;
	}

	private String getPart(int partNo, int udhLength)
	{
		int partSize;

		partSize = maxSize() - udhLength;
		partSize *= 2;
		if (((partSize * (partNo - 1)) + partSize) > getEncodedText().length()) return getEncodedText().substring(partSize * (partNo - 1));
		else return getEncodedText().substring(partSize * (partNo - 1), (partSize * (partNo - 1)) + partSize);
	}

	String getValidityPeriodBits()
	{
		String bits;
		int value;
		if (validityPeriod == -1) bits = "FF";
		else
		{
			if (validityPeriod <= 12) value = (validityPeriod * 12) - 1;
			else if (validityPeriod <= 24) value = (((validityPeriod - 12) * 2) + 143);
			else if (validityPeriod <= 720) value = (validityPeriod / 24) + 166;
			else value = (validityPeriod / 168) + 192;
			bits = Integer.toHexString(value);
			if (bits.length() != 2) bits = "0" + bits;
			if (bits.length() > 2) bits = "FF";
		}
		return bits;
	}

	String toBCDFormat(String s)
	{
		String bcd, ss;
		int i;
		ss = s;
		if ((ss.length() % 2) != 0) ss = ss + "F";
		bcd = "";
		for (i = 0; i < ss.length(); i += 2)
			bcd = bcd + ss.charAt(i + 1) + ss.charAt(i);
		return bcd;
	}

	public String getPDU(String smscNumber, int mpRefNo, int partNo)
	{
		String pdu, udh, ud = "", dataLen = "";
		String str1, str2;
		int ud_length;
		pdu = "";
		udh = "";
		if ((smscNumber != null) && (smscNumber.length() != 0))
		{
			str1 = "91" + toBCDFormat(smscNumber.substring(1));
			str2 = Integer.toHexString(str1.length() / 2);
			if (str2.length() != 2) str2 = "0" + str2;
			pdu = pdu + str2 + str1;
		}
		else if ((smscNumber != null) && (smscNumber.length() == 0)) pdu = pdu + "00";
		if (((srcPort != -1) && (dstPort != -1)) || (isBig()))
		{
			if (statusReport) pdu = pdu + "71";
			else pdu = pdu + "51";
		}
		else
		{
			if (statusReport) pdu = pdu + "31";
			else pdu = pdu + "11";
		}
		pdu = pdu + "00";
		str1 = getRecipient();
		if (str1.charAt(0) == '+')
		{
			str1 = toBCDFormat(str1.substring(1));
			str2 = Integer.toHexString(getRecipient().length() - 1);
			str1 = "91" + str1;
		}
		else
		{
			str1 = toBCDFormat(str1);
			str2 = Integer.toHexString(getRecipient().length());
			str1 = "81" + str1;
		}
		if (str2.length() != 2) str2 = "0" + str2;
		pdu = pdu + str2 + str1;
		{
			String s;
			s = Integer.toHexString(pid);
			while (s.length() < 2)
				s = "0" + s;
			pdu = pdu + s;
		}
		if (getEncoding() == MessageEncodings.ENC7BIT)
		{
			if (flashSms) pdu = pdu + "10";
			else pdu = pdu + "00";
		}
		else if (getEncoding() == MessageEncodings.ENC8BIT)
		{
			if (flashSms) pdu = pdu + "14";
			else pdu = pdu + "04";
		}
		else if (getEncoding() == MessageEncodings.ENCUCS2)
		{
			if (flashSms) pdu = pdu + "18";
			else
			{
				if (getType() == MessageTypes.WAPSI) pdu = pdu + "F5";
				else pdu = pdu + "08";
			}
		}
		else if (getEncoding() == MessageEncodings.ENCCUSTOM)
		{
			String s = Integer.toHexString(dcs);
			while (s.length() < 2)
				s = "0" + s;
			pdu = pdu + s;
		}
		pdu = pdu + getValidityPeriodBits();
		if ((srcPort != -1) && (dstPort != -1))
		{
			String s;
			udh += "060504";
			s = Integer.toHexString(dstPort);
			while (s.length() < 4)
				s = "0" + s;
			udh += s;
			s = Integer.toHexString(srcPort);
			while (s.length() < 4)
				s = "0" + s;
			udh += s;
		}
		if (isBig())
		{
			String s;
			if ((srcPort != -1) && (dstPort != -1)) udh = "0C" + udh.substring(2) + "0804";
			else udh += "060804";
			s = Integer.toHexString(mpRefNo);
			while (s.length() < 4)
				s = "0" + s;
			udh += s;
			s = Integer.toHexString(getNoOfParts());
			while (s.length() < 2)
				s = "0" + s;
			udh += s;
			s = Integer.toHexString(partNo);
			while (s.length() < 2)
				s = "0" + s;
			udh += s;
		}
		if (getEncoding() == MessageEncodings.ENC7BIT)
		{
			if (isBig())
			{
				ud = getPart(partNo, udh.length());
				ud_length = getText().length() % 8 == 7 ? ud.length() - 1 : ud.length();
				dataLen = Integer.toHexString(((ud_length + udh.length()) * 8 / 7) / 2);
			}
			else
			{
				ud = getPart(partNo, udh.length());
				dataLen = Integer.toHexString(messageCharCount + (udh.length() / 2));
			}
		}
		else if (getEncoding() == MessageEncodings.ENC8BIT)
		{
			ud = getPart(partNo, udh.length());
			dataLen = Integer.toHexString((ud.length() + udh.length()) / 2);
		}
		else if (getEncoding() == MessageEncodings.ENCUCS2)
		{
			ud = getPart(partNo, udh.length());
			dataLen = Integer.toHexString((ud.length() + udh.length()) / 2);
		}
		else if (getEncoding() == MessageEncodings.ENCCUSTOM)
		{
			if ((dcs & 0x04) == 0)
			{
				ud = getPart(partNo, udh.length());
				ud_length = getText().length() % 8 == 7 ? ud.length() - 1 : ud.length();
				dataLen = Integer.toHexString(((ud_length + udh.length()) * 8 / 7) / 2);
			}
			else
			{
				ud = getPart(partNo, udh.length());
				dataLen = Integer.toHexString((ud.length() + udh.length()) / 2);
			}
		}
		if (dataLen.length() != 2) dataLen = "0" + dataLen;
		if (udh.length() != 0) pdu = pdu + dataLen + udh + ud;
		else pdu = pdu + dataLen + ud;
		UDH = udh;
		UD = ud;
		return pdu.toUpperCase();
	}

	/**
	 * Returns the recipient of this outbound message.
	 * 
	 * @return The recipient of the message.
	 * @see #setRecipient(String)
	 */
	public String getRecipient()
	{
		return recipient;
	}

	/**
	 * Set the recipient of the message.
	 * 
	 * @param recipient
	 *            The recipient of the message.
	 * @see #getRecipient()
	 */
	public void setRecipient(String recipient)
	{
		this.recipient = recipient;
	}

	/**
	 * Returns the dispatch date of this message. If the message has not been
	 * sent yet, the dispatch date is null.
	 * 
	 * @return The message dispatch date.
	 */
	public Date getDispatchDate()
	{
		if (dispatchDate != null) return new java.util.Date(dispatchDate.getTime());
		else return null;
	}

	public void setDispatchDate(Date dispatchDate)
	{
		this.dispatchDate = dispatchDate;
	}

	/**
	 * Returns the destination port of the message. Source and Destination ports
	 * are used when messages are targeting a midlet application. For standard
	 * SMS messages, the Source and Destination ports should <b>both</b> be set
	 * to -1 (which is their default value anyway).
	 * 
	 * @return The destination port.
	 * @see #getDstPort()
	 * @see #setSrcPort(int)
	 * @see #getSrcPort()
	 */
	public int getDstPort()
	{
		return dstPort;
	}

	/**
	 * Sets the destination port of the message. Source and Destination ports
	 * are used when messages are targeting a midlet application. For standard
	 * SMS messages, the Source and Destination ports should <b>both</b> be set
	 * to -1 (which is their default value anyway).
	 * <p>
	 * The default is (-1).
	 * 
	 * @param dstPort
	 *            The destination port.
	 * @see #setDstPort(int)
	 * @see #setSrcPort(int)
	 * @see #getSrcPort()
	 */
	public void setDstPort(int dstPort)
	{
		this.dstPort = dstPort;
	}

	/**
	 * Returns true if this message is to be sent out as a flash SMS. Otherwise,
	 * it returns false.
	 * 
	 * @return True for a Flash message.
	 * @see #setFlashSms(boolean)
	 */
	public boolean isFlashSms()
	{
		return flashSms;
	}

	/**
	 * Set the flash message indication. Set this to true for this message to be
	 * sent as a flash message. Flash messages appear directly on the handset,
	 * so use this feature with care, because it may be a bit annoying.
	 * Furthermore, keep in mind that flash messaging is not supported on all
	 * phones.
	 * <p>
	 * The default is non-flash (false).
	 * 
	 * @param flashSms
	 *            True for a flash sms.
	 */
	public void setFlashSms(boolean flashSms)
	{
		this.flashSms = flashSms;
	}

	/**
	 * Returns the source port of the message. Source and Destination ports are
	 * used when messages are targeting a midlet application. For standard SMS
	 * messages, the Source and Destination ports should <b>both</b> be set to
	 * -1 (which is their default value anyway).
	 * 
	 * @return The source port.
	 * @see #setSrcPort(int)
	 * @see #setDstPort(int)
	 * @see #getDstPort()
	 */
	public int getSrcPort()
	{
		return srcPort;
	}

	/**
	 * Sets the source port of the message. Source and Destination ports are
	 * used when messages are targeting a midlet application. For standard SMS
	 * messages, the Source and Destination ports should <b>both</b> be set to
	 * -1 (which is their default value anyway).
	 * <p>
	 * The default is (-1).
	 * 
	 * @param srcPort
	 *            The source port.
	 * @see #setDstPort(int)
	 * @see #setSrcPort(int)
	 * @see #getSrcPort()
	 */
	public void setSrcPort(int srcPort)
	{
		this.srcPort = srcPort;
	}

	/**
	 * Returns true if a status/delivery report will be asked for this message.
	 * 
	 * @return True if a status report will be generated.
	 */
	public boolean getStatusReport()
	{
		return statusReport;
	}

	/**
	 * Sets the status report request. If you set it to true, a status report
	 * message will be generated, otherwise no status report message will be
	 * generated.
	 * <p>
	 * The default is (false).
	 * 
	 * @param statusReport
	 *            The status report request status.
	 */
	public void setStatusReport(boolean statusReport)
	{
		this.statusReport = statusReport;
	}

	/**
	 * Returns the message validity period (in hours).
	 * 
	 * @return The message validity period.
	 * @see #setValidityPeriod(int)
	 */
	public int getValidityPeriod()
	{
		return validityPeriod;
	}

	/**
	 * Sets the message validity period.
	 * 
	 * @param validityPeriod
	 *            The message validity period in hours.
	 * @see #getValidityPeriod()
	 */
	public void setValidityPeriod(int validityPeriod)
	{
		this.validityPeriod = validityPeriod;
	}

	/**
	 * Receives the custom originator string. Set it to empty string to leave
	 * the default behavior.
	 * 
	 * @return The custom originator string.
	 * @see #setFrom(String)
	 */
	public String getFrom()
	{
		return from;
	}

	/**
	 * Sets the custom originator string. Some gateways allow you to define a
	 * custom string as the originator. When the message arrives at the
	 * recipient, the latter will not see your number but this string.
	 * <p>
	 * Note that this functionality is not supported on GSM modems / phones. It
	 * is supported on most bulk sms operators.
	 * 
	 * @param from
	 *            The custom originator string.
	 * @see #getFrom()
	 */
	public void setFrom(String from)
	{
		this.from = from;
	}

	int getPid()
	{
		return pid;
	}

	void setPid(int pid)
	{
		this.pid = pid;
	}

	int getDcs()
	{
		return dcs;
	}

	void setDcs(int dcs)
	{
		this.dcs = dcs;
	}

	/**
	 * Returns the message status.
	 * 
	 * @return The message status.
	 * @see MessageStatuses
	 */
	public MessageStatuses getMessageStatus()
	{
		return messageStatus;
	}

	public void setMessageStatus(MessageStatuses messageStatus)
	{
		this.messageStatus = messageStatus;
	}

	public FailureCauses getFailureCause()
	{
		return failureCause;
	}

	/**
	 * Mark message as failed and set cause of failure.
	 * 
	 * @param failureCause
	 *            Cause of failure
	 */
	public void setFailureCause(FailureCauses failureCause)
	{
		if (failureCause != FailureCauses.NO_ERROR) this.messageStatus = MessageStatuses.FAILED;
		this.failureCause = failureCause;
	}

	/**
	 * Return value of internal sending retry counter.
	 * 
	 * @return Number of sending message retries
	 */
	public int getRetryCount()
	{
		return retryCount;
	}

	void incrementRetryCount()
	{
		retryCount++;
	}

	/**
	 * Returns the priority of the message.
	 * 
	 * @return The priority of the message.
	 * @see MessagePriorities
	 */
	public MessagePriorities getPriority()
	{
		return priority;
	}

	/**
	 * Sets the priority of the message.
	 * 
	 * @param priority
	 *            The new priority.
	 * @see MessagePriorities
	 */
	public void setPriority(MessagePriorities priority)
	{
		this.priority = priority;
	}

	/**
	 * Returns the message Reference Number. The Reference Number comes into
	 * existence when the message is sent. Its format depends on the gateway:
	 * For modems, its a number. For bulk sms operators, this is a hex string.
	 * If the message has not been sent yet, the Reference number is blank.
	 * 
	 * @return The message reference number.
	 */
	public String getRefNo()
	{
		return refNo;
	}

	public void setRefNo(String refNo)
	{
		this.refNo = refNo;
	}

	public String getUDH()
	{
		return UDH;
	}

	public String getUD()
	{
		return UD;
	}

	public String toString()
	{
		String str = "";
		str += "===============================================================================";
		str += "\n";
		str += "<< OUTBOUND MESSAGE DUMP >>";
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
		str += " SMSC Ref No: " + refNo;
		str += "\n";
		str += " Recipient: " + recipient;
		str += "\n";
		str += " Dispatch Date: " + getDispatchDate();
		str += "\n";
		str += " Message Status: " + getMessageStatus();
		str += "\n";
		str += " Validity Period (Hours): " + getValidityPeriod();
		str += "\n";
		str += " Status Report: " + getStatusReport();
		str += "\n";
		str += " Source / Destination Ports: " + getSrcPort() + " / " + getDstPort();
		str += "\n";
		str += " Flash SMS: " + isFlashSms();
		str += "\n";
		str += "===============================================================================";
		str += "\n";
		return str;
	}

	public Map<String, String> getPDUs(String smscNumber, int mpRefNo)
	{
		Map<String, String> ret = new HashMap<String, String>();
		String pdu, ud = "", dataLen = "";
		String str1, str2;
	
		StringBuilder udh;
	
		int toSend = getEncodedText().length();
	
		int partNo = 0;
		
		int udhSize = getUDHSize();
		int maxUD = maxSize() - udhSize;
		int messagesNeeded = ( (getEncodedText().length() / 2) + maxUD - 1) / maxUD;
		
		while (toSend > 0) {
			pdu = "";
			udh = new StringBuilder("");
			partNo ++;
			if ((smscNumber != null) && (smscNumber.length() != 0))
			{
				str1 = "91" + toBCDFormat(smscNumber.substring(1));
				str2 = Integer.toHexString(str1.length() / 2);
				if (str2.length() != 2) str2 = "0" + str2;
				pdu = pdu + str2 + str1;
			}
			else if ((smscNumber != null) && (smscNumber.length() == 0)) pdu = pdu + "00";
			if (srcPort != -1 || dstPort != -1 || (isBig()))
			{
				if (statusReport) pdu = pdu + "71";
				else pdu = pdu + "51";
			}
			else
			{
				if (statusReport) pdu = pdu + "31";
				else pdu = pdu + "11";
			}
			pdu = pdu + "00";
			str1 = getRecipient();
			if (str1.charAt(0) == '+')
			{
				str1 = toBCDFormat(str1.substring(1));
				str2 = Integer.toHexString(getRecipient().length() - 1);
				str1 = "91" + str1;
			}
			else
			{
				str1 = toBCDFormat(str1);
				str2 = Integer.toHexString(getRecipient().length());
				str1 = "81" + str1;
			}
			if (str2.length() != 2) str2 = "0" + str2;
			pdu = pdu + str2 + str1;
	
			{
				String s;
	
				s = Integer.toHexString(pid);
				while (s.length() < 2)
					s = "0" + s;
				pdu = pdu + s;
			}
			if (getEncoding() == MessageEncodings.ENC7BIT)
			{
				if (flashSms) pdu = pdu + "10";
				else pdu = pdu + "00";
			}
			else if (getEncoding() == MessageEncodings.ENC8BIT)
			{
				if (flashSms) pdu = pdu + "14";
				else pdu = pdu + "04";
			}
			else if (getEncoding() == MessageEncodings.ENCUCS2)
			{
				if (flashSms) pdu = pdu + "18";
				else
				{
					if (getType() == MessageTypes.WAPSI) pdu = pdu + "F5";
					else pdu = pdu + "08";
				}
			}
			else if (getEncoding() == MessageEncodings.ENCCUSTOM)
			{
				String s = Integer.toHexString(dcs);
				while (s.length() < 2)
					s = "0" + s;
				pdu = pdu + s;
			}
			pdu = pdu + getValidityPeriodBits();
			
			// If only one port is set, we set the other to 0000
			if ((srcPort != -1) || (dstPort != -1))
			{
				String s;
				udh.append("050400000000");
				
				if (dstPort != -1) {
					s = Integer.toHexString(dstPort);
					while (s.length() < 4)
						s = "0" + s;
					udh.replace(4, 8, s);
				}
				if (srcPort != -1) {
					s = Integer.toHexString(srcPort);
					while (s.length() < 4)
						s = "0" + s;
					udh.replace(8, 12, s);
				}
				
				int udhL = udh.length() / 2;
				s = Integer.toHexString(udhL);
				if (s.length() < 2) {
					s = "0" + s;
				}
				UDH = s + udh.toString().toUpperCase();
			}
	
			if (isBig())
			{
				String s;
	
				udh.append("0804");
				s = Integer.toHexString(mpRefNo);
				while (s.length() < 4)
					s = "0" + s;
				udh.append(s);
				s = Integer.toHexString(messagesNeeded);
				while (s.length() < 2)
					s = "0" + s;
				udh.append(s);
				s = Integer.toHexString(partNo);
				while (s.length() < 2)
					s = "0" + s;
				udh.append(s);
			}
			
			if (udh.length() > 0) {
				int udhL = udh.length() / 2;
				String s = Integer.toHexString(udhL);
				if (s.length() < 2) {
					s = "0" + s;
				}
	
				udh.insert(0, s);
			}
			
			if (getEncoding() == MessageEncodings.ENC7BIT)
			{
				ud = getPart(partNo, udh.length());
				dataLen = Integer.toHexString(((ud.length() + udh.length()) * 8 / 7) / 2);
			}
			else if (getEncoding() == MessageEncodings.ENC8BIT)
			{
				ud = getPart(partNo, udh.length());
				dataLen = Integer.toHexString((ud.length() + udh.length()) / 2);
			}
			else if (getEncoding() == MessageEncodings.ENCUCS2)
			{
				ud = getPart(partNo, udh.length());
				dataLen = Integer.toHexString((ud.length() + udh.length()) / 2);
			}
			else if (getEncoding() == MessageEncodings.ENCCUSTOM)
			{
				if ((dcs & 0x04) == 0)
				{
					ud = getPart(partNo, udh.length());
					dataLen = Integer.toHexString(((ud.length() + udh.length()) * 8 / 7) / 2);
				}
				else
				{
					ud = getPart(partNo, udh.length());
					dataLen = Integer.toHexString((ud.length() + udh.length()) / 2);
				}
			}
			
			if (dataLen.length() != 2) dataLen = "0" + dataLen;
			if (udh.length() != 0) pdu = pdu + dataLen + udh + ud;
			else pdu = pdu + dataLen + ud;
	
			ret.put(ud.toUpperCase(), udh.toString().toUpperCase());
			toSend -= ud.length();
		}
		return ret;
	}

	private int getUDHSize() {
		int ret = 0; 
		if (srcPort != -1 || dstPort != -1 || (isBig())) {
			ret += 2; // UDH Length
			ret += 4; 
			if (srcPort != -1 || dstPort != -1) {
				ret += 8;
			}
			if (isBig()) {
				ret += 8;
			}
		}
		return ret;
	}
}
