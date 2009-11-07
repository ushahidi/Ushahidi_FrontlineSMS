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

import java.io.IOException;
import java.util.List;

import org.smslib.v3.helper.Queue;

/**
 * Abstract class representing a Gateway, i.e. an interface capable of sending
 * and/or receiving SMS messages.
 */
public abstract class AGateway
{
	public static class GatewayAttributes
	{
		public static final int SEND = 0x0001;

		public static final int RECEIVE = 0x0002;

		public static final int CUSTOMFROM = 0x0004;

		public static final int BIGMESSAGES = 0x0008;

		public static final int WAPSI = 0x0010;

		public static final int PORTADDRESSING = 0x0020;

		public static final int FLASHSMS = 0x0040;

		public static final int DELIVERYREPORTS = 0x0080;
	}

	protected boolean started;

	protected String gtwId;

	protected int attributes;

	protected boolean inbound;

	protected boolean outbound;

	protected Service srv;

	protected MessageProtocols protocol;

	protected IInboundMessageNotification inboundNotification;

	protected IOutboundMessageNotification outboundNotification;

	protected ICallNotification callNotification;

	protected Statistics statistics;

	protected String from;

	protected int deliveryErrorCode;

	protected Thread queueManagerThread;

	protected Queue lowQ, normalQ, highQ;

	protected GatewayStatuses gatewayStatus;

	public AGateway(String id)
	{
		this.gtwId = id;
		srv = null;
		started = false;
		inbound = false;
		outbound = false;
		attributes = 0;
		protocol = MessageProtocols.PDU;
		inboundNotification = null;
		outboundNotification = null;
		callNotification = null;
		from = "";
		lowQ = new Queue();
		normalQ = new Queue();
		highQ = new Queue();
		statistics = new Statistics();
		from = "";
		deliveryErrorCode = -1;
		gatewayStatus = GatewayStatuses.OK;
	}

	boolean isStarted()
	{
		return started;
	}

	int getAttributes()
	{
		return attributes;
	}

	public boolean getStarted()
	{
		return started;
	}

	public Service getService()
	{
		return srv;
	}

	public void setService(Service srv)
	{
		this.srv = srv;
	}

	/**
	 * Returns true if the the gateway is set for inbound messaging.
	 * 
	 * @return True if this gateway is set for inbound messaging.
	 */
	public boolean isInbound()
	{
		return inbound;
	}

	/**
	 * Enables or disables the gateway for inbound messaging. The command is
	 * accepted only if the gateway supports inbound messaging.
	 * 
	 * @param value
	 *            True to enable the gateway for inbound messaging.
	 */
	public void setInbound(boolean value)
	{
		if ((attributes & GatewayAttributes.RECEIVE) != 0) inbound = value;
	}

	/**
	 * Returns true if the the gateway is set for outbound messaging.
	 * 
	 * @return True if this gateway is set for outbound messaging.
	 */
	public boolean isOutbound()
	{
		return outbound;
	}

	/**
	 * Enables or disables the gateway for outbound messaging. The command is
	 * accepted only if the gateway supports outbound messaging.
	 * 
	 * @param value
	 *            True to enable the gateway for outbound messaging.
	 */
	public void setOutbound(boolean value)
	{
		if ((attributes & GatewayAttributes.SEND) != 0) outbound = value;
	}

	/**
	 * Sets the communication protocol of the gateway. The call is applicable
	 * only for modem gateways, in other cases it is ignored.
	 * 
	 * @param protocol
	 * @see MessageProtocols
	 * @see #getProtocol
	 */
	public void setProtocol(MessageProtocols protocol)
	{
		this.protocol = protocol;
	}

	/**
	 * Returns the communication protocol current in use by the gateway.
	 * 
	 * @return The communication protocol.
	 * @see MessageProtocols
	 * @see #setProtocol(MessageProtocols)
	 */
	public MessageProtocols getProtocol()
	{
		return protocol;
	}

	/**
	 * Returns the gateway id assigned to this gateway during initialization.
	 * 
	 * @return The gateway id.
	 */
	public String getGatewayId()
	{
		return gtwId;
	}

	/**
	 * Returns the gateway status.
	 * 
	 * @return The gateway status
	 * @see GatewayStatuses
	 */
	public GatewayStatuses getGatewayStatus()
	{
		return gatewayStatus;
	}

	/**
	 * Sets the gateway status to a new value.
	 * 
	 * @param status
	 *            The new gateway status.
	 * @see GatewayStatuses
	 */
	public void setGatewayStatus(GatewayStatuses status)
	{
		gatewayStatus = status;
	}

	/**
	 * Returns the notification method set for inbound messages. Returns null if
	 * no such method is set.
	 * 
	 * @return The notification method.
	 * @see #setInboundNotification(IInboundMessageNotification)
	 */
	public IInboundMessageNotification getInboundNotification()
	{
		return inboundNotification;
	}

	/**
	 * Sets the inbound message notification method. The method must adhere to
	 * the IInboundMessageNotification interface. If set, SMSLib will call this
	 * method upon arrival of a new inbound message.
	 * 
	 * @param inboundNotification
	 *            The method to be called.
	 * @see #getInboundNotification()
	 * @see IInboundMessageNotification
	 */
	public void setInboundNotification(IInboundMessageNotification inboundNotification)
	{
		this.inboundNotification = inboundNotification;
	}

	/**
	 * Returns the notification method set for outbound messages. Returns null
	 * if no such method is set.
	 * 
	 * @return The notification method.
	 * @see #setOutboundNotification(IOutboundMessageNotification)
	 */
	public IOutboundMessageNotification getOutboundNotification()
	{
		return outboundNotification;
	}

	/**
	 * Sets the outbound notification method. The method must adhere to the
	 * IOutboundMessageNotification interface. If set, SMSLib will call this
	 * method upon dispatch of a message through the queueing (asyncronous)
	 * calls.
	 * 
	 * @param outboundNotification
	 * @see #getOutboundNotification()
	 * @see IOutboundMessageNotification
	 */
	public void setOutboundNotification(IOutboundMessageNotification outboundNotification)
	{
		this.outboundNotification = outboundNotification;
	}

	/**
	 * Returns the call notification method. Returns null if no such method is
	 * set.
	 * 
	 * @return The notification method.
	 * @see #setCallNotification(ICallNotification)
	 */
	public ICallNotification getCallNotification()
	{
		return callNotification;
	}

	/**
	 * Returns the call notification method. The method must adhere to the
	 * ICallNotification interface. If set, SMSLib will call this method upon
	 * detection of an inbound call.
	 * 
	 * @param callNotification
	 * @see #getCallNotification()
	 * @see ICallNotification
	 */
	public void setCallNotification(ICallNotification callNotification)
	{
		this.callNotification = callNotification;
	}

	/**
	 * Returns the total number of messages received by this gateway.
	 * 
	 * @return The number of received messages.
	 */
	public int getInboundMessageCount()
	{
		return statistics.inbound;
	}

	public void incInboundMessageCount()
	{
		statistics.inbound++;
	}

	/**
	 * Returns the total number of messages sent via this gateway.
	 * 
	 * @return The number of sent messages.
	 */
	public int getOutboundMessageCount()
	{
		return statistics.outbound;
	}

	public void incOutboundMessageCount()
	{
		statistics.outbound++;
	}

	/**
	 * Returns the string that will appear on recipient's phone as the
	 * originator. Not all gateways support this.
	 * 
	 * @return The originator string.
	 * @see #setFrom(String)
	 */
	public String getFrom()
	{
		return from;
	}

	/**
	 * Sets the string that will appear on recipient's phone as the originator.
	 * Not all gateways support this.
	 * 
	 * @param from
	 *            The originator string.
	 * @see #getFrom()
	 */
	public void setFrom(String from)
	{
		this.from = from;
	}

	public boolean queueMessage(OutboundMessage msg)
	{
		if (msg.getPriority() == MessagePriorities.LOW) lowQ.add(msg);
		else if (msg.getPriority() == MessagePriorities.NORMAL) normalQ.add(msg);
		else if (msg.getPriority() == MessagePriorities.HIGH) highQ.add(msg);
		return true;
	}

	@SuppressWarnings("unchecked")
	public int queueMessages(List msgList)
	{
		int count = 0;
		for (int i = 0, n = msgList.size(); i < n; i++)
			if (queueMessage((OutboundMessage) msgList.get(i))) count++;
		return count;
	}

	public void startGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		started = true;
		queueManagerThread = new Thread(new QueueManager());
		queueManagerThread.start();
		gatewayStatus = GatewayStatuses.OK;
	}

	public void stopGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		started = false;
		if (queueManagerThread != null)
		{
			queueManagerThread.interrupt();
			try
			{
				queueManagerThread.join();
			}
			catch (InterruptedException e)
			{
				logInfo("Interrupted while waiting for gateway to stop.", e);
			}
			finally
			{
				queueManagerThread = null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void readMessages(List msgList, MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	public InboundMessage readMessage(String memLoc, int memIndex) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	@SuppressWarnings("unchecked")
	public int sendMessages(List msgList) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int cnt = 0;
		for (int i = 0; i < msgList.size(); i++)
			if (sendMessage((OutboundMessage) msgList.get(i))) cnt++;
		return cnt;
	}

	public boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	/**
	 * Queries the gateway for remaining credit.
	 * 
	 * @return Remaining credit.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public float queryBalance() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	/**
	 * Queries the gateway to see if a specific message and its recipient are
	 * covered. The given message is not sent out - it is just tested.
	 * 
	 * @param msg
	 *            The message to test.
	 * @return True is the recipient is covered by the network.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public boolean queryCoverage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	/**
	 * Query the gateway for message delivery status.
	 * 
	 * @param msg
	 *            The OutboundMessage object to be checked.
	 * @return The delivery status. This is interpreted and mapped to the
	 *         standard SMSLib status codes. For detailed information, check
	 *         method getDeliveryErrorCode().
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see DeliveryStatuses
	 * @see #getDeliveryErrorCode()
	 */
	public DeliveryStatuses queryMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		return queryMessage(msg.getRefNo());
	}

	/**
	 * Query the gateway for message delivery status.
	 * 
	 * @param refNo
	 *            The reference number of a previously sent message to be
	 *            checked.
	 * @return The delivery status. This is interpreted and mapped to the
	 *         standard SMSLib status codes. For detailed information, check
	 *         method getDeliveryErrorCode().
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see DeliveryStatuses
	 * @see #getDeliveryErrorCode()
	 */
	public DeliveryStatuses queryMessage(String refNo) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	/**
	 * Returns the gateway-specific error code from the last queryMessage()
	 * call. Note that each call to queryMessage() resets this error.
	 * 
	 * @return The error code - actual values depend on gateway used.
	 * @see #queryMessage(OutboundMessage)
	 */
	public int getDeliveryErrorCode()
	{
		return deliveryErrorCode;
	}

	boolean isCapableOf(int att)
	{
		return ((att & attributes) == att);
	}

	boolean conformsTo(int attrib, boolean required)
	{
		if (required && !isCapableOf(attrib)) return false;
		else return true;
	}

	static class Statistics
	{
		public int inbound;

		public int outbound;

		public Statistics()
		{
			inbound = 0;
			outbound = 0;
		}
	}

	int getQueueLoad()
	{
		return (getQueueLoad(MessagePriorities.LOW) + getQueueLoad(MessagePriorities.NORMAL) + getQueueLoad(MessagePriorities.HIGH));
	}

	int getQueueLoad(MessagePriorities priority)
	{
		if (priority == MessagePriorities.LOW) return lowQ.size();
		else if (priority == MessagePriorities.NORMAL) return normalQ.size();
		else if (priority == MessagePriorities.HIGH) return highQ.size();
		else return 0;
	}

	public void logError(String message)
	{
		srv.logError("GTW: " + gtwId + ": " + message, null);
	}

	public void logError(String message, Exception e)
	{
		srv.logError("GTW: " + gtwId + ": " + message, e);
	}

	public void logDebug(String message)
	{
		srv.logDebug("GTW: " + gtwId + ": " + message, null);
	}

	public void logDebug(String message, Exception e)
	{
		srv.logDebug("GTW: " + gtwId + ": " + message, e);
	}

	public void logWarn(String message)
	{
		srv.logWarn("GTW: " + gtwId + ": " + message, null);
	}

	public void logWarn(String message, Exception e)
	{
		srv.logWarn("GTW: " + gtwId + ": " + message, e);
	}

	public void logInfo(String message)
	{
		srv.logInfo("GTW: " + gtwId + ": " + message, null);
	}

	public void logInfo(String message, Exception e)
	{
		srv.logInfo("GTW: " + gtwId + ": " + message, e);
	}

	private class QueueManager implements Runnable
	{
		public QueueManager()
		{
			super();
		}

		public Object get()
		{
			if (highQ.size() > 0) return highQ.get();
			else if (normalQ.size() > 0) return normalQ.get();
			else if (lowQ.size() > 0) return lowQ.get();
			else return null;
		}

		public void run()
		{
			OutboundMessage msg = null;
			logInfo("Starting Queue Manager.", null);
			try
			{
				if (started)
				{
					while (true)
					{
						while (true)
						{
							msg = (OutboundMessage) get();
							if (msg == null) Thread.sleep(srv.S.QUEUE_INTERVAL);
							else break;
						}
						if ((!started) || (gatewayStatus != GatewayStatuses.OK)) break;
						if (msg != null)
						{
							if (!sendMessage(msg))
							{
								if (msg.getRetryCount() < srv.S.QUEUE_RETRIES)
								{
									logInfo("Reinserting message to queue.", null);
									msg.incrementRetryCount();
									queueMessage(msg);
								}
								else
								{
									logWarn("Maximum number of queue retries exceeded, message lost.", null);
									msg.setFailureCause(FailureCauses.UNKNOWN);
									if (getOutboundNotification() != null) getOutboundNotification().process(gtwId, msg);
								}
							}
							else if (getOutboundNotification() != null) getOutboundNotification().process(gtwId, msg);
						}
						msg = null;
						try
						{
							Thread.sleep(srv.S.QUEUE_INTERVAL);
						}
						catch (Exception e)
						{
						}
						if (!started) break;
					}
				}
			}
			catch (InterruptedException e)
			{
				if ((msg != null) && (msg.getMessageStatus() != MessageStatuses.SENT)) queueMessage(msg);
				logInfo("Interrupting queue.", e);
			}
			catch (Exception e)
			{
				logWarn("Queue exception, marking gateway for reset.", e);
				gatewayStatus = GatewayStatuses.RESTART;
				try
				{
					if ((msg != null) && (msg.getMessageStatus() != MessageStatuses.SENT)) queueMessage(msg);
				}
				catch (Exception e1)
				{
					logError("Fatal error during restart of the queue.", e1);
				}
			}
			logInfo("QueueManager stopped.", null);
		}
	}
}
