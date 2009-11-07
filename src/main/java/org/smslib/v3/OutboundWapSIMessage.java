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

import java.net.URL;
import java.util.Date;

public class OutboundWapSIMessage extends OutboundMessage
{
	private static final long serialVersionUID = -7736798614962107393L;
	
	// private static final String PDU_PATTERN =
	// "25060803AE81EAAF82B48401056A0045C6_URL1_03_URL2_00080103_TEXT_000101";
	private static final String PDU_PATTERN = "29060603AE81EA8DCA02056A0045C6_URL1_03_URL2_00080103_TEXT_000101";
	private String siPdu = "";
	protected URL url;
	protected Date createDate, expireDate;
	protected WapSISignals signal;
	private static final String[][] protocolBytes = { { "http://www.", "0D" }, { "https://www.", "0F" }, { "http://", "0C" }, { "https://", "0E" } };
	private static final String[][] domainBytes = { { ".com/", "85" }, { ".edu/", "86" }, { ".net/", "87" }, { ".org/", "88" } };

	public OutboundWapSIMessage(String recipient, URL url, Date createDate, Date expireDate, WapSISignals signal, String text) throws SMSLibException
	{
		super();
		this.url = url;
		this.createDate = new java.util.Date(createDate.getTime());
		this.expireDate = new java.util.Date(expireDate.getTime());
		this.signal = signal;
		this.setText(text);
		setEncoding(MessageEncodings.ENCUCS2);
		setSrcPort(9200);
		setDstPort(2948);
		setType(MessageTypes.WAPSI);
		this.setRecipient(recipient);
		try { fixPdu(); } catch (java.io.UnsupportedEncodingException e) { throw new SMSLibException(e.getMessage()); }
	}

	public OutboundWapSIMessage(String recipient, URL url, String text) throws SMSLibException
	{
		this(recipient, url, new java.util.Date(), new java.util.Date(), WapSISignals.NONE, text);
	}

	public Date getCreateDate()
	{
		return new java.util.Date(createDate.getTime());
	}

	public void setCreateDate(Date createDate)
	{
		this.createDate = new java.util.Date(createDate.getTime());
	}

	public Date getExpireDate()
	{
		return new java.util.Date(expireDate.getTime());
	}

	public void setExpireDate(Date expireDate)
	{
		this.expireDate = new java.util.Date(expireDate.getTime());
	}

	public WapSISignals getSignal()
	{
		return signal;
	}

	public void setSignal(WapSISignals signal)
	{
		this.signal = signal;
	}

	public URL getUrl()
	{
		return url;
	}

	public void setUrl(URL url)
	{
		this.url = url;
	}

	protected String getPDUData()
	{
		return siPdu;
	}

	private String bytesToHexStr(byte[] b)
	{
		if (b == null) return "";
		StringBuffer strBuffer = new StringBuffer(b.length * 3);
		for (int i = 0; i < b.length; i++)
		{
			strBuffer.append(Integer.toHexString(b[i] & 0xff));
		}
		return strBuffer.toString();
	}

	private void fixPdu() throws java.io.UnsupportedEncodingException
	{
		String s, urlText;
		int i;
		char c;
		// byte[] utfBytes;
		// byte cc;
		boolean foundProtocol;
		
		siPdu = PDU_PATTERN;
		s = bytesToHexStr(getText().getBytes("UTF-8"));
		siPdu = siPdu.replaceAll("_TEXT_", s);
		foundProtocol = false;
		urlText = url.toString();
		for (i = 0; i < 4; i++)
		{
			if (urlText.indexOf(protocolBytes[i][0]) == 0)
			{
				foundProtocol = true;
				siPdu = siPdu.replaceAll("_URL1_", protocolBytes[i][1]);
				urlText = urlText.replaceAll(protocolBytes[i][0], "");
				break;
			}
		}
		if (!foundProtocol) siPdu = siPdu.replaceAll("_URL1_", "0B");
		s = "";
		for (i = 0; i < urlText.length(); i++)
		{
			String subUrl;
			boolean foundDomain = false;
			subUrl = urlText.substring(i);
			for (int j = 0; j < 4; j++)
			{
				if (subUrl.indexOf(domainBytes[j][0]) == 0)
				{
					foundDomain = true;
					i += 4;
					s += "00";
					s += domainBytes[j][1];
					s += "03";
					break;
				}
			}
			if (!foundDomain)
			{
				c = urlText.charAt(i);
				s += ((Integer.toHexString(c).length() < 2) ? "0" + Integer.toHexString(c) : Integer.toHexString(c));
			}
		}
		siPdu = siPdu.replaceAll("_URL2_", s);
		setEncodedText(siPdu);
	}
}
