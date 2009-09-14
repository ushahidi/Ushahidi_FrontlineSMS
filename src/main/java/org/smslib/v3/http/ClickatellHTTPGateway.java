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

package org.smslib.v3.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.smslib.v3.AGateway;
import org.smslib.v3.DeliveryStatuses;
import org.smslib.v3.FailureCauses;
import org.smslib.v3.GatewayException;
import org.smslib.v3.MessageEncodings;
import org.smslib.v3.MessagePriorities;
import org.smslib.v3.MessageStatuses;
import org.smslib.v3.MessageTypes;
import org.smslib.v3.OutboundMessage;
import org.smslib.v3.OutboundWapSIMessage;
import org.smslib.v3.TimeoutException;
import org.smslib.v3.WapSISignals;

/**
 * Gateway for Clickatell bulk operator (http://www.clickatell.com) Outbound
 * only - implements HTTP & HTTPS interface.
 */
public class ClickatellHTTPGateway extends HTTPGateway
{
	private String apiId, username, password;

	private String sessionId;

	private KeepAlive keepAlive;

	private boolean secure;

	Object SYNC_Commander;

	private String HTTP = "http://";

	private String HTTPS = "https://";

	private String URL_BALANCE = "api.clickatell.com/http/getbalance";

	private String URL_COVERAGE = "api.clickatell.com/utils/routeCoverage.php";

	private String URL_QUERYMSG = "api.clickatell.com/http/querymsg";

	private String URL_AUTH = "api.clickatell.com/http/auth";

	private String URL_PING = "api.clickatell.com/http/ping";

	private String URL_SENDMSG = "api.clickatell.com/http/sendmsg";

	private String URL_SENDWAPSI = "api.clickatell.com/mms/si_push";

	public ClickatellHTTPGateway(String id, String apiId, String username, String password)
	{
		super(id);
		started = false;
		this.apiId = apiId;
		this.username = username;
		this.password = password;
		this.sessionId = null;
		this.from = "";
		this.secure = false;
		SYNC_Commander = new Object();
		attributes = AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.WAPSI | AGateway.GatewayAttributes.CUSTOMFROM | AGateway.GatewayAttributes.BIGMESSAGES | AGateway.GatewayAttributes.FLASHSMS;
	}

	/**
	 * Sets whether the gateway works in unsecured (HTTP) or secured (HTTPS)
	 * mode. False denotes unsecured.
	 * 
	 * @param secure
	 *            True for HTTPS, false for plain HTTP.
	 */
	public void setSecure(boolean secure)
	{
		this.secure = secure;
	}

	/**
	 * Return the operation mode (HTTP or HTTPS).
	 * 
	 * @return True for HTTPS, false for HTTP.
	 * @see #setSecure(boolean)
	 */
	public boolean getSecure()
	{
		return secure;
	}

	public void startGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		logInfo("Starting gateway.");
		connect();
		super.startGateway();
	}

	public void stopGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		logInfo("Stopping gateway.");
		super.stopGateway();
		sessionId = null;
		if (keepAlive != null)
		{
			keepAlive.interrupt();
			keepAlive.join();
			keepAlive = null;
		}
	}

	@SuppressWarnings("unchecked")
	public float queryBalance() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List request = new ArrayList();
		List response;
		if (sessionId == null) throw new GatewayException("Internal Clickatell Gateway error.");
		url = new URL((secure ? HTTPS : HTTP) + URL_BALANCE);
		request.add(new HttpHeader("session_id", sessionId, false));
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		if (((String) response.get(0)).indexOf("Credit:") == 0) return Float.parseFloat(((String) response.get(0)).substring(((String) response.get(0)).indexOf(':') + 1));
		else return -1;
	}

	@SuppressWarnings("unchecked")
	public boolean queryCoverage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List request = new ArrayList();
		List response;
		if (sessionId == null) throw new GatewayException("Internal Clickatell Gateway error.");
		url = new URL((secure ? HTTPS : HTTP) + URL_COVERAGE);
		request.add(new HttpHeader("session_id", sessionId, false));
		request.add(new HttpHeader("msisdn", msg.getRecipient().substring(1), false));
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		if (((String) response.get(0)).indexOf("OK") == 0) return true;
		else return false;
	}

	@SuppressWarnings("unchecked")
	public DeliveryStatuses queryMessage(String refNo) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List request = new ArrayList();
		List response;
		int pos;
		if (sessionId == null) throw new GatewayException("Internal Clickatell Gateway error.");
		url = new URL((secure ? HTTPS : HTTP) + URL_QUERYMSG);
		request.add(new HttpHeader("session_id", sessionId, false));
		request.add(new HttpHeader("apimsgid", refNo, false));
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		pos = ((String) response.get(0)).indexOf("Status:");
		deliveryErrorCode = Integer.parseInt(((String) response.get(0)).substring(pos + 7).trim());
		switch (deliveryErrorCode)
		{
			case 1:
				return DeliveryStatuses.UNKNOWN;
			case 2:
			case 3:
			case 8:
			case 11:
				return DeliveryStatuses.KEEPTRYING;
			case 4:
				return DeliveryStatuses.DELIVERED;
			case 5:
			case 6:
			case 7:
				return DeliveryStatuses.ABORTED;
			case 9:
			case 10:
				return DeliveryStatuses.ABORTED;
			case 12:
				return DeliveryStatuses.ABORTED;
			default:
				return DeliveryStatuses.UNKNOWN;
		}
	}

	void connect() throws GatewayException, IOException
	{
		try
		{
			if (!authenticate()) throw new GatewayException("Cannot authenticate to Clickatell.");
			keepAlive = new KeepAlive();
		}
		catch (MalformedURLException e)
		{
			throw new GatewayException("Internal Clickatell Gateway error.");
		}
	}

	@SuppressWarnings("unchecked")
	private boolean authenticate() throws IOException, MalformedURLException
	{
		URL url;
		List request = new ArrayList();
		List response;
		logDebug("Authenticate().");
		url = new URL((secure ? HTTPS : HTTP) + URL_AUTH);
		request.add(new HttpHeader("api_id", apiId, false));
		request.add(new HttpHeader("user", username, false));
		request.add(new HttpHeader("password", password, false));
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		if (((String) response.get(0)).indexOf("ERR:") == 0)
		{
			sessionId = null;
			return false;
		}
		else
		{
			sessionId = ((String) response.get(0)).substring(4);
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean ping() throws IOException, MalformedURLException
	{
		URL url;
		List request = new ArrayList();
		List response;
		logDebug("Ping()");
		url = new URL((secure ? HTTPS : HTTP) + URL_PING);
		request.add(new HttpHeader("session_id", sessionId, false));
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		if (((String) response.get(0)).indexOf("ERR:") == 0) return false;
		else return true;
	}

	@SuppressWarnings("unchecked")
	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List request = new ArrayList();
		boolean ok = false;
		if (sessionId == null)
		{
			logError("No session defined.");
			msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
			return false;
		}
		logDebug("sendMessage()");
		try
		{
			if (msg.getType() == MessageTypes.OUTBOUND) url = new URL((secure ? HTTPS : HTTP) + URL_SENDMSG);
			else if (msg.getType() == MessageTypes.WAPSI) url = new URL((secure ? HTTPS : HTTP) + URL_SENDWAPSI);
			else
			{
				msg.setFailureCause(FailureCauses.BAD_FORMAT);
				logError("Incorrect message format.");
				return false;
			}
			request.add(new HttpHeader("session_id", sessionId, false));
			request.add(new HttpHeader("to", msg.getRecipient().substring(1), false));
			request.add(new HttpHeader("concat", "3", false));
			
			String from = msg.getFrom();
			if (from == null || from.trim().equals("")) from = this.from;
			if (from != null) {
				if(from.length() > 0 && from.charAt(0) == '+') from = from.substring(1);
				if(from.length() > 0) request.add(new HttpHeader("from", from, false));
			}
			if (msg.getPriority() == MessagePriorities.LOW) request.add(new HttpHeader("queue", "3", false));
			else if (msg.getPriority() == MessagePriorities.NORMAL) request.add(new HttpHeader("queue", "2", false));
			else if (msg.getPriority() == MessagePriorities.HIGH) request.add(new HttpHeader("queue", "1", false));
			if ((msg.getSrcPort() != -1) || (msg.getDstPort() != -1)) {
				if (msg.getEncoding() == MessageEncodings.ENC8BIT) {
					msg.getPDUs("", 0);
					request.add(new HttpHeader("udh", msg.getUDH(), false));
					request.add(new HttpHeader("text", msg.getEncodedText(), false));
				} else {
					msg.getPDU("", 0, 0);
					request.add(new HttpHeader("udh", msg.getUDH(), false));
					request.add(new HttpHeader("text", msg.getText(), false));
				}
			} else {
				if (msg.isFlashSms()) request.add(new HttpHeader("msg_type", "SMS_FLASH", false));
				if (msg.getType() == MessageTypes.OUTBOUND) {
					if (msg.getEncoding() == MessageEncodings.ENC7BIT) request.add(new HttpHeader("text", msg.getText(), false));
					else if (msg.getEncoding() == MessageEncodings.ENCUCS2) {
						request.add(new HttpHeader("unicode", "1", false));
						request.add(new HttpHeader("text", msg.getText(), true));
					}
				} else if (msg.getType() == MessageTypes.WAPSI) {
					request.add(new HttpHeader("si_id", msg.getId(), false));
					if (((OutboundWapSIMessage) msg).getCreateDate() != null) request.add(new HttpHeader("si_created", formatDateUTC(((OutboundWapSIMessage) msg).getCreateDate()), false));
					if (((OutboundWapSIMessage) msg).getExpireDate() != null) request.add(new HttpHeader("si_expires", formatDateUTC(((OutboundWapSIMessage) msg).getExpireDate()), false));
					request.add(new HttpHeader("si_action", formatSignal(((OutboundWapSIMessage) msg).getSignal()), false));
					request.add(new HttpHeader("si_url", ((OutboundWapSIMessage) msg).getUrl().toString(), false));
					request.add(new HttpHeader("si_text", msg.getText(), false));
				}
				int requestFeatures = 0;
				if (msg.getStatusReport()) request.add(new HttpHeader("deliv_ack", "1", false));
				if (from != null && from.length() != 0) requestFeatures += 16 + 32;
				if (msg.isFlashSms()) requestFeatures += 512;
				if (msg.getStatusReport()) requestFeatures += 8192;
				request.add(new HttpHeader("req_feat", "" + requestFeatures, false));
			}
			ok = sendRequest(msg, url, request, ok);
		}
		catch (MalformedURLException e)
		{
			logError("Malformed URL.", e);
		}
		catch (IOException e)
		{
			logError("I/O error.", e);
		}
		return ok;
	}

	/**
	 * @param msg
	 * @param url
	 * @param request
	 * @param ok
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private boolean sendRequest(OutboundMessage msg, URL url, List request,
			boolean ok) throws IOException {
		List response;
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		if (((String) response.get(0)).indexOf("ID:") == 0)
		{
			msg.setRefNo(((String) response.get(0)).substring(4));
			msg.setDispatchDate(new Date());
			msg.setGatewayId(gtwId);
			msg.setMessageStatus(MessageStatuses.SENT);
			incOutboundMessageCount();
			ok = true;
		}
		else if (((String) response.get(0)).indexOf("ERR:") == 0)
		{
			switch (Integer.parseInt(((String) response.get(0)).substring(5, 8)))
			{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					msg.setFailureCause(FailureCauses.GATEWAY_AUTH);
					break;
				case 101:
				case 102:
				case 105:
				case 106:
				case 107:
				case 112:
				case 116:
				case 120:
					msg.setFailureCause(FailureCauses.BAD_FORMAT);
					break;
				case 114:
					msg.setFailureCause(FailureCauses.NO_ROUTE);
					break;
				case 301:
				case 302:
					msg.setFailureCause(FailureCauses.NO_CREDIT);
					break;
				default:
					msg.setFailureCause(FailureCauses.UNKNOWN);
					break;
			}
			msg.setRefNo(null);
			msg.setDispatchDate(null);
			msg.setMessageStatus(MessageStatuses.FAILED);
			ok = false;
		}
		return ok;
	}

	private String formatDateUTC(Date d)
	{
		String strDate = "", tmp = "";
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		strDate = String.valueOf(cal.get(Calendar.YEAR));
		tmp = String.valueOf(cal.get(Calendar.MONTH) + 1);
		if (tmp.length() != 2) tmp = "0" + tmp;
		strDate += "-" + tmp;
		tmp = String.valueOf(cal.get(Calendar.DATE));
		if (tmp.length() != 2) tmp = "0" + tmp;
		strDate += "-" + tmp;
		tmp = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		if (tmp.length() != 2) tmp = "0" + tmp;
		strDate += "T" + tmp;
		tmp = String.valueOf(cal.get(Calendar.MINUTE));
		if (tmp.length() != 2) tmp = "0" + tmp;
		strDate += ":" + tmp;
		tmp = String.valueOf(cal.get(Calendar.SECOND));
		if (tmp.length() != 2) tmp = "0" + tmp;
		strDate += ":" + tmp + "Z";
		return strDate;
	}

	private String formatSignal(WapSISignals signal)
	{
		if (signal == WapSISignals.NONE) return "signal-none";
		else if (signal == WapSISignals.LOW) return "signal-low";
		else if (signal == WapSISignals.MEDIUM) return "signal-medium";
		else if (signal == WapSISignals.HIGH) return "signal-high";
		else if (signal == WapSISignals.DELETE) return "signal-delete";
		else return "signal-none";
	}

	private class KeepAlive extends Thread
	{
		public KeepAlive()
		{
			setPriority(MIN_PRIORITY);
			start();
		}

		public void run()
		{
			logDebug("KeepAlive thread started.");
			while (true)
			{
				try
				{
					sleep(10 * 60 * 1000);
					if (sessionId == null) break;
					logDebug("** KeepAlive START **");
					synchronized (SYNC_Commander)
					{
						ping();
					}
					logDebug("** KeepAlive END **");
				}
				catch (InterruptedException e)
				{
					if (sessionId == null) break;
				}
				catch (Exception e)
				{
				}
			}
			logDebug("KeepAlive thread ended.");
		}
	}
}
