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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This is main library class. Your primary interface with SMSLib is via methods
 * defined in this class.
 */
public class Service
{
	private Logger logger;

	@SuppressWarnings("unchecked")
	private List gtwList;

	private Router router;

	private LoadBalancer loadBalancer;

	private WatchDog watchDog;

	/**
	 * Configuration settings.
	 * 
	 * @see Settings
	 */
	public Settings S;

	/**
	 * Default Service constructor. Will set SMSLib to use its own logger.
	 */
	@SuppressWarnings("unchecked")
	public Service()
	{
		S = new Settings();
		try
		{
			logger = Logger.getLogger("org.smslib");
			logger.info(Library.getLibraryDescription());
			logger.info("Version: " + Library.getLibraryVersion());
			logger.info("JRE Version: " + System.getProperty("java.version"));
			logger.info("JRE Impl Version: " + System.getProperty("java.vm.version"));
			logger.info("O/S: " + System.getProperty("os.name") + " / " + System.getProperty("os.arch") + " / " + System.getProperty("os.version"));
			gtwList = new ArrayList();
			setRouter(new Router(this));
			setLoadBalancer(new RoundRobinLoadBalancer(this));
		}
		catch (Exception e)
		{
			logger.fatal(e);
		}
	}

	/**
	 * Service constructor. Will set SMSLib to use the provided log4j logger.
	 * 
	 * @param logger
	 *            A ready log4j logger to use.
	 */
	@SuppressWarnings("unchecked")
	public Service(Logger logger)
	{
		S = new Settings();
		this.logger = logger;
		logger.info(Library.getLibraryDescription());
		logger.info("Version: " + Library.getLibraryVersion());
		logger.info("JRE Version: " + System.getProperty("java.version"));
		logger.info("JRE Impl Version: " + System.getProperty("java.vm.version"));
		logger.info("O/S: " + System.getProperty("os.name") + " / " + System.getProperty("os.arch") + " / " + System.getProperty("os.version"));
		gtwList = new ArrayList();
		setRouter(new Router(this));
		setLoadBalancer(new RoundRobinLoadBalancer(this));
	}

	/**
	 * Returns the logger used by SMSLib.
	 * 
	 * @return The logger in use.
	 */
	public Logger getLogger()
	{
		return logger;
	}

	/**
	 * Adds a gateway to the list of gateways managed by the Service class.
	 * 
	 * @param gtw
	 *            The gateway to be added.
	 * @see #getGatewayList()
	 */
	@SuppressWarnings("unchecked")
	public void addGateway(AGateway gtw)
	{
		synchronized (gtwList)
		{
			gtw.setService(this);
			gtwList.add(gtw);
		}
	}

	/**
	 * Initializes all gateways. This should be the first call before you use
	 * the Service class for sending/receiving messages. The call will try to
	 * start all defined gateways.
	 * 
	 * @throws SMSLibException
	 *             No Gateways are defined.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #stopService()
	 */
	public synchronized void startService() throws SMSLibException, TimeoutException, GatewayException, IOException, InterruptedException
	{
		watchDog = new WatchDog();
		synchronized (gtwList)
		{
			if (gtwList.size() == 0) throw new SMSLibException("No gateways are defined.");
			for (int i = 0, n = gtwList.size(); i < n; i++)
				((AGateway) gtwList.get(i)).startGateway();
		}
	}

	/**
	 * Stops all gateways - does not remove them from Service's internal list.
	 * Once stopped, all SMSLib operations will fail. You need to start the
	 * gateways again before proceeding.
	 * 
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #startService()
	 */
	public synchronized void stopService() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (watchDog != null)
		{
			watchDog.interrupt();
			watchDog.join();
		}
		synchronized (gtwList)
		{
			for (int i = 0, n = gtwList.size(); i < n; i++)
				((AGateway) gtwList.get(i)).stopGateway();
		}
	}

	/**
	 * Reads inbound messages from ALL gateways with the Inbound attribute set.
	 * When successful, the message list will contain all messages read.
	 * 
	 * @param msgList
	 *            A (probably empty) list that will be populated with Inbound
	 *            messages read.
	 * @param msgClass
	 *            Filtering: Class of messages that need to be read.
	 * @return The number of messages read.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see MessageClasses
	 */
	@SuppressWarnings("unchecked")
	public int readMessages(List msgList, MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		synchronized (gtwList)
		{
			for (int i = 0, n = gtwList.size(); i < n; i++)
			{
				AGateway gtw = (AGateway) gtwList.get(i);
				if (gtw.isInbound())
				{
					try
					{
						readMessages(msgList, msgClass, gtw);
					}
					catch (TimeoutException e)
					{
						logWarn("readMessages(): Gateway " + gtw.getGatewayId() + " does not respond, marking for restart.");
						gtw.setGatewayStatus(GatewayStatuses.RESTART);
					}
					catch (IOException e)
					{
						logWarn("readMessages(): Gateway " + gtw.getGatewayId() + " throws IO errors, marking for restart.");
						gtw.setGatewayStatus(GatewayStatuses.RESTART);
					}
				}
			}
		}
		return msgList.size();
	}

	/**
	 * Reads inbound messages from the SPECIFIC gateway. When successful, the
	 * message list will contain all messages read.
	 * 
	 * @param msgList
	 *            A (probably empty) list that will be populated with Inbound
	 *            messages read.
	 * @param msgClass
	 *            Filtering: Class of messages that need to be read.
	 * @param gtwId
	 *            The identifier of the gateway from which to read messages.
	 * @return The number of messages read.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see MessageClasses
	 * @see AGateway
	 */
	@SuppressWarnings("unchecked")
	public int readMessages(List msgList, MessageClasses msgClass, String gtwId) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		synchronized (gtwList)
		{
			AGateway gtw = findGateway(gtwId);
			if ((gtw != null) && (gtw.isInbound()))
			{
				try
				{
					readMessages(msgList, msgClass, gtw);
				}
				catch (TimeoutException e)
				{
					logWarn("readMessages(): Gateway " + gtw.getGatewayId() + " does not respond, marking for restart.");
					gtw.setGatewayStatus(GatewayStatuses.RESTART);
				}
				catch (IOException e)
				{
					logWarn("readMessages(): Gateway " + gtw.getGatewayId() + " throws IO errors, marking for restart.");
					gtw.setGatewayStatus(GatewayStatuses.RESTART);
				}
			}
		}
		return msgList.size();
	}

	/**
	 * Reads inbound messages from the SPECIFIC gateway. When successful, the
	 * message list will contain all messages read.
	 * 
	 * @param msgList
	 *            A (probably empty) list that will be populated with inbound
	 *            messages read.
	 * @param msgClass
	 *            Filtering: Class of messages that need to be read.
	 * @param gtw
	 *            The gateway object from which to read messages.
	 * @return The number of messages read.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see MessageClasses
	 * @see AGateway
	 */
	@SuppressWarnings("unchecked")
	public int readMessages(List msgList, MessageClasses msgClass, AGateway gtw) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		synchronized (gtwList)
		{
			try
			{
				gtw.readMessages(msgList, msgClass);
			}
			catch (TimeoutException e)
			{
				logWarn("readMessages(): Gateway " + gtw.getGatewayId() + " does not respond, marking for restart.");
				gtw.setGatewayStatus(GatewayStatuses.RESTART);
			}
			catch (IOException e)
			{
				logWarn("readMessages(): Gateway " + gtw.getGatewayId() + " throws IO errors, marking for restart.");
				gtw.setGatewayStatus(GatewayStatuses.RESTART);
			}
		}
		return msgList.size();
	}

	/**
	 * Reads a specific gateway for a message matching the given Memory Location
	 * and Memory Index.
	 * <p>
	 * This is a "dummy" approach. It does not implement the CGMR command,
	 * rather it reads all messages and searches for a match.
	 * 
	 * @param gtwId
	 *            The Gateway ID of the gateway to read from.
	 * @param memLoc
	 *            The memory location string.
	 * @param memIndex
	 *            The memory index.
	 * @return The message read. Null if no relevant message is found or if the
	 *         Gateway ID given is invalid.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public InboundMessage readMessage(String gtwId, String memLoc, int memIndex) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		InboundMessage msg = null;
		synchronized (gtwList)
		{
			AGateway gtw = findGateway(gtwId);
			if ((gtw != null) && (gtw.isInbound()))
			{
				try
				{
					msg = gtw.readMessage(memLoc, memIndex);
				}
				catch (TimeoutException e)
				{
					logWarn("readMessages(): Gateway " + gtw.getGatewayId() + " does not respond, marking for restart.");
					gtw.setGatewayStatus(GatewayStatuses.RESTART);
				}
				catch (IOException e)
				{
					logWarn("readMessages(): Gateway " + gtw.getGatewayId() + " throws IO errors, marking for restart.");
					gtw.setGatewayStatus(GatewayStatuses.RESTART);
				}
			}
		}
		return msg;
	}

	/**
	 * Sends a single message. The following logic is applied in order for
	 * SMSLib to decide from which gateway it will send the message:<br>
	 * 1. If the message holds gateway information (member field "gatewayId"),
	 * SMSLib will try to send it from that gateway.<br>
	 * 2. If the message does not hold gateway information (member field
	 * "gatewayId" is empty or "*") then if router and load balancer is
	 * defined, then message is processed by these classes.<br>
	 * 3. Otherwise the method selects the first outbound-capable gateway
	 * defined and sends the message from it.<br>
	 * The method blocks until the message is actually sent (synchronous
	 * operation).
	 * 
	 * @param msg
	 *            An OutboundMessage object.
	 * @return True if the message is sent.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #queueMessage(OutboundMessage)
	 */
	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		synchronized (gtwList)
		{
			AGateway gtw = routeMessage(msg);
			if (gtw != null)
			{
				try
				{
					return gtw.sendMessage(msg);
				}
				catch (TimeoutException e)
				{
					logWarn("sendMessage(): Gateway " + gtw.getGatewayId() + " does not respond, marking for restart.");
					gtw.setGatewayStatus(GatewayStatuses.RESTART);
					msg.setMessageStatus(MessageStatuses.FAILED);
					msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
					return false;
				}
				catch (IOException e)
				{
					logWarn("sendMessage(): Gateway " + gtw.getGatewayId() + " throws IO errors, marking for restart.");
					gtw.setGatewayStatus(GatewayStatuses.RESTART);
					msg.setMessageStatus(MessageStatuses.FAILED);
					msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
					return false;
				}
			}
			else return false;
		}
	}

	/**
	 * Sends a single message from the specified gateway.
	 * 
	 * @param msg
	 *            An OutboundMessage object.
	 * @param gtwId
	 *            The id of the gateway that will be used for sending.
	 * @return True if the message is sent.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #sendMessage(OutboundMessage)
	 */
	public boolean sendMessage(OutboundMessage msg, String gtwId) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		msg.setGatewayId(gtwId);
		return sendMessage(msg);
	}

	/**
	 * Sends a list of messages.
	 * 
	 * @param msgList
	 *            A list of OutboundMessage objects.
	 * @return The number of messages sent.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #sendMessage(OutboundMessage)
	 */
	@SuppressWarnings("unchecked")
	public int sendMessages(List msgList) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int counter = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			OutboundMessage msg = (OutboundMessage) msgList.get(i);
			if (sendMessage(msg)) counter++;
		}
		return counter;
	}

	/**
	 * Sends a list of messages from the specified gateway.
	 * 
	 * @param msgList
	 *            A list of OutboundMessage objects.
	 * @param gtwId
	 *            The id of the gateway that will be used for sending.
	 * @return The number of messages sent.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #sendMessage(OutboundMessage)
	 */
	@SuppressWarnings("unchecked")
	public int sendMessages(List msgList, String gtwId) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int counter = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			OutboundMessage msg = (OutboundMessage) msgList.get(i);
			msg.setGatewayId(gtwId);
			if (sendMessage(msg)) counter++;
		}
		return counter;
	}

	/**
	 * Queues a message for sending. The gateway selection logic is the same as
	 * for sendMessage(). The method does not block - returns immediately. If
	 * you wish to be alerted about the fate of the message, you may implement a
	 * IOutboundMessageNotification listener.
	 * 
	 * @param msg
	 *            Message to be sent
	 * @return True if the message is accepted in the Queue.
	 * @see #sendMessage(OutboundMessage)
	 * @see IOutboundMessageNotification
	 */
	public boolean queueMessage(OutboundMessage msg)
	{
		synchronized (gtwList)
		{
			AGateway gtw = routeMessage(msg);
			if (gtw != null) return gtw.queueMessage(msg);
			else return false;
		}
	}

	/**
	 * Queues a message for sending from the specific gateway.
	 * 
	 * @param msg
	 *            A OutboundMessage object.
	 * @param gtwId
	 *            The id of the gateway that will be used for sending.
	 * @return True if the message is accepted in the Queue.
	 * @see #queueMessage(OutboundMessage)
	 */
	public boolean queueMessage(OutboundMessage msg, String gtwId)
	{
		synchronized (gtwList)
		{
			msg.setGatewayId(gtwId);
			return queueMessage(msg);
		}
	}

	/**
	 * Queues a list of messages for sending.
	 * 
	 * @param msgList
	 *            A list of OutboundMessage objects.
	 * @return The number of messages accepted in the Queue.
	 * @see #queueMessage(OutboundMessage)
	 */
	@SuppressWarnings("unchecked")
	public int queueMessages(List msgList)
	{
		int counter = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			OutboundMessage msg = (OutboundMessage) msgList.get(i);
			if (queueMessage(msg)) counter++;
		}
		return counter;
	}

	/**
	 * Queues a list of messages for sending from the specific gateway.
	 * 
	 * @param msgList
	 *            A list of OutboundMessage objects.
	 * @param gtwId
	 *            The id of the gateway to be used for sending.
	 * @return The number of messages accepted in the Queue.
	 * @see #queueMessage(OutboundMessage)
	 */
	@SuppressWarnings("unchecked")
	public int queueMessages(List msgList, String gtwId)
	{
		int counter = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			OutboundMessage msg = (OutboundMessage) msgList.get(i);
			msg.setGatewayId(gtwId);
			if (queueMessage(msg)) counter++;
		}
		return counter;
	}

	/**
	 * Deletes the specified message. The operation is not supported by all
	 * gateways.
	 * 
	 * @param msg
	 *            The message to be deleted. It must be a valid InboundMessage
	 *            object. <b>DO NOT PASS invalid objects to the method!</b>
	 * @return True if the message is deleted.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		synchronized (gtwList)
		{
			AGateway gtw = findGateway(msg.getGatewayId());
			if (gtw != null)
			{
				try
				{
					return gtw.deleteMessage(msg);
				}
				catch (TimeoutException e)
				{
					logWarn("deleteMessage(): Gateway " + gtw.getGatewayId() + " does not respond, marking for restart.");
					gtw.setGatewayStatus(GatewayStatuses.RESTART);
					return false;
				}
				catch (IOException e)
				{
					logWarn("deleteMessage(): Gateway " + gtw.getGatewayId() + " throws IO errors, marking for restart.");
					gtw.setGatewayStatus(GatewayStatuses.RESTART);
					return false;
				}
			}
			else return false;
		}
	}

	/**
	 * Returns the total number of messages received by the specified gateway.
	 * 
	 * @param gtwId
	 *            The id of the gateway to query.
	 * @return The number of received messages or -1 on error.
	 */
	public int getInboundMessageCount(String gtwId)
	{
		return getInboundMessageCount(findGateway(gtwId));
	}

	/**
	 * Returns the total number of messages received by the specified gateway.
	 * 
	 * @param gtw
	 *            The AGateway object to query.
	 * @return The number of received messages or -1 on error.
	 */
	public int getInboundMessageCount(AGateway gtw)
	{
		return (gtw != null ? gtw.getInboundMessageCount() : -1);
	}

	/**
	 * Returns the total number of messages sent via the specified gateway.
	 * 
	 * @param gtwId
	 *            The id of the gateway to query.
	 * @return The number of sent messages or -1 on error.
	 */
	public int getOutboundMessageCount(String gtwId)
	{
		return getOutboundMessageCount(findGateway(gtwId));
	}

	/**
	 * Returns the total number of messages sent via the specified gateway.
	 * 
	 * @param gtw
	 *            The AGateway object to query.
	 * @return The number of sent messages or -1 on error.
	 */
	public int getOutboundMessageCount(AGateway gtw)
	{
		return (gtw != null ? gtw.getOutboundMessageCount() : -1);
	}

	/**
	 * Returns the total number of messages received by all gateways.
	 * 
	 * @return The number of received messages.
	 */
	public int getInboundMessageCount()
	{
		int total = 0;
		for (int i = 0, n = gtwList.size(); i < n; i++)
			total = total + getInboundMessageCount((AGateway) gtwList.get(i));
		return total;
	}

	/**
	 * Returns the total number of messages sent via all gateways.
	 * 
	 * @return The number of sent messages.
	 */
	public int getOutboundMessageCount()
	{
		int total = 0;
		for (int i = 0, n = gtwList.size(); i < n; i++)
			total = total + getOutboundMessageCount((AGateway) gtwList.get(i));
		return total;
	}

	/**
	 * Find and return a gateway by its ID.
	 * 
	 * @param gtwId
	 *            The ID of gateway to find.
	 * @return Gateway object bearing given name, or NULL if not found.
	 */
	public AGateway findGateway(String gtwId)
	{
		for (int i = 0, n = gtwList.size(); i < n; i++)
			if (((AGateway) gtwList.get(i)).getGatewayId().equals(gtwId)) return (AGateway) gtwList.get(i);
		return null;
	}

	/**
	 * Returns the list of defined gateways.
	 * 
	 * @return The list of gateways.
	 */
	@SuppressWarnings("unchecked")
	public List getGatewayList()
	{
		return gtwList;
	}

	/**
	 * Retrieves the Queue load (i.e. pending messages) from all gateways and
	 * for all priorities.
	 * 
	 * @return The number of pending messages to be send.
	 * @see #getGatewayQueueLoad(MessagePriorities)
	 * @see #getGatewayQueueLoad(String)
	 * @see #getGatewayQueueLoad(String, MessagePriorities)
	 */
	public int getGatewayQueueLoad()
	{
		int total = 0;
		for (int i = 0, n = gtwList.size(); i < n; i++)
			total += ((AGateway) gtwList.get(i)).getQueueLoad();
		return total;
	}

	/**
	 * Retrieves the Queue load (i.e. pending messages) from all gateways and
	 * for a specific priority.
	 * 
	 * @param priority
	 *            The priority looked for.
	 * @return The number of pending messages to be send.
	 * @see #getGatewayQueueLoad()
	 * @see #getGatewayQueueLoad(String)
	 * @see #getGatewayQueueLoad(String, MessagePriorities)
	 */
	public int getGatewayQueueLoad(MessagePriorities priority)
	{
		int total = 0;
		for (int i = 0, n = gtwList.size(); i < n; i++)
			total += ((AGateway) gtwList.get(i)).getQueueLoad(priority);
		return total;
	}

	/**
	 * Retrieves the Queue load (i.e. pending messages) from a specific gateway
	 * and for all priorities.
	 * 
	 * @param gtwId
	 *            The Gateway ID for which information is to be retrieved.
	 * @return The number of pending messages to be send.
	 * @see #getGatewayQueueLoad()
	 * @see #getGatewayQueueLoad(MessagePriorities)
	 * @see #getGatewayQueueLoad(String, MessagePriorities)
	 */
	public int getGatewayQueueLoad(String gtwId)
	{
		AGateway gtw = findGateway(gtwId);
		return (gtw == null ? 0 : gtw.getQueueLoad());
	}

	/**
	 * Retrieves the Queue load (i.e. pending messages) from a specific gateway
	 * and for a specific priority.
	 * 
	 * @param gtwId
	 *            The Gateway ID for which information is to be retrieved.
	 * @param priority
	 *            The priority looked for.
	 * @return The number of pending messages to be send.
	 * @see #getGatewayQueueLoad()
	 * @see #getGatewayQueueLoad(MessagePriorities)
	 * @see #getGatewayQueueLoad(String)
	 */
	public int getGatewayQueueLoad(String gtwId, MessagePriorities priority)
	{
		AGateway gtw = findGateway(gtwId);
		return (gtw == null ? 0 : gtw.getQueueLoad(priority));
	}

	/**
	 * Returns the active Load Balancer class.
	 * 
	 * @return The active LoadBalancer class.
	 * @see LoadBalancer
	 */
	public LoadBalancer getLoadBalancer()
	{
		return loadBalancer;
	}

	/**
	 * Sets a new Load Balancer.
	 * 
	 * @param loadBalancer
	 *            The Load Balancer that will take effect.
	 * @see LoadBalancer
	 */
	public void setLoadBalancer(LoadBalancer loadBalancer)
	{
		this.loadBalancer = loadBalancer;
	}

	/**
	 * Returns the active Router class.
	 * 
	 * @return The active Router class.
	 * @see Router
	 */
	public Router getRouter()
	{
		return router;
	}

	/**
	 * Sets a new Router.
	 * 
	 * @param router
	 *            The Router that will take effect.
	 * @see Router
	 */
	public void setRouter(Router router)
	{
		this.router = router;
	}

	/**
	 * Find best suitable gateway to send specific message through Router and
	 * Load Balancer.
	 * 
	 * @param msg
	 *            Message to be routed
	 * @return Reference to gateway or <code>null</code> if no suitable
	 *         gateway is found.
	 */
	AGateway routeMessage(OutboundMessage msg)
	{
		synchronized (gtwList)
		{
			return router.route(msg);
		}
	}

	public void logError(String message)
	{
		logError(message, null);
	}

	public void logError(String message, Exception e)
	{
		logger.error(message + (e == null ? "" : (" (" + e.getMessage() + ")")));
	}

	public void logDebug(String message)
	{
		logDebug(message, null);
	}

	public void logDebug(String message, Exception e)
	{
		logger.debug(message + (e == null ? "" : (" (" + e.getMessage() + ")")));
	}

	public void logWarn(String message)
	{
		logWarn(message, null);
	}

	public void logWarn(String message, Exception e)
	{
		logger.warn(message + (e == null ? "" : (" (" + e.getMessage() + ")")));
	}

	public void logInfo(String message)
	{
		logInfo(message, null);
	}

	public void logInfo(String message, Exception e)
	{
		logger.info(message + (e == null ? "" : (" (" + e.getMessage() + ")")));
	}

	private class WatchDog extends Thread
	{
		public WatchDog()
		{
			start();
		}

		public void run()
		{
			logDebug("WatchDog started.");
			while (true)
			{
				try
				{
					logInfo("WatchDog running...");
					synchronized (gtwList)
					{
						for (int i = 0, n = gtwList.size(); i < n; i++)
						{
							AGateway gtw = (AGateway) gtwList.get(i);
							if (gtw.getGatewayStatus() == GatewayStatuses.RESTART)
							{
								logWarn("WatchDog: Gateway: " + gtw.getGatewayId() + " restarting.");
								try
								{
									gtw.stopGateway();
								}
								catch (Exception e)
								{
									logWarn("WatchDog: error while shutting down Gateway: " + gtw.getGatewayId(), e);
								}
								try
								{
									gtw.startGateway();
								}
								catch (Exception e)
								{
									logError("WatchDog: error while starting Gateway: " + gtw.getGatewayId(), e);
								}
							}
						}
					}
					Thread.sleep(S.WATCHDOG_INTERVAL);
				}
				catch (InterruptedException e)
				{
					break;
				}
				catch (Exception e)
				{
					logError("WatchDog error. ", e);
				}
			}
			logDebug("WatchDog stopped.");
		}
	}

	public static void main(String[] args)
	{
		System.out.println(Library.getLibraryDescription());
		System.out.println("\nSMSLib API Version: " + Library.getLibraryVersion());
	}
}
