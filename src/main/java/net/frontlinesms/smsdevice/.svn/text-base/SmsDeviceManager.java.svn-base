/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms.smsdevice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

//#ifdef COMM_JAVAX
import javax.comm.*;
//#else
//# import gnu.io.*;
//#endif

import net.frontlinesms.CommUtils;
import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.data.domain.Message;
import net.frontlinesms.listener.SmsListener;
import net.frontlinesms.properties.PropertySet;

import org.apache.log4j.Logger;
import org.smslib.CIncomingMessage;

/**
 * SmsHandler should be run as a separate thread.
 * 
 * It handles the discovery of phones available on the system's COM ports, 
 * and also manages a pool of threads that handle the communication with as many phones as are found 
 * attached to the system.
 * 
 * Autodetection should take 30 seconds.
 * 
 * OUTGOING MESSAGES
 * When you send a new outgoing message through SmsHandler it is added to a stack of waiting messages, 
 * which it will then send to the waiting phones by turn, unless the messages are marked as being 
 * for a specific phone.
 * 
 * INCOMING MESSAGES
 * If you create SmsHandler and pass it an SmsListener, incoming messages will be reported as events 
 * to that listener. If you create the SmsHandler without the listener, the messages will just appear 
 * on the linked list of IncomingMessages, and the calling program must poll it for new messages.
 * 
 * Incoming messages are immediately removed from active phones, so if you close the program without 
 * storing the message, then you will have lost the message.
 * 
 * PHONE STATE:
 * When a phone handler is created on a port, it will attempt AT commands until it gets an OK from a modem.
 * A valid OK will make the phoneHandler set phonePresent to TRUE.
 * The PhoneHanler will then attempt to connect the full SMSLIB tools to it, to take it into 
 * connected=true state, from which you can actually send and recieve messages.
 * 
 * HTTP services:
 * In the future this will be extended to be able to interface with 
 * internet based SMS services via HTTP, to handle bulk messaging.
 * 
 * @author Ben Whitaker ben(at)masabi(dot)com
 * @author Alex Anderson alex(at)masabi(dot)com
 * 
 * FIXME need to add checks for UCS-2 support before sending UCS-2 messages
 */
public class SmsDeviceManager extends Thread implements SmsListener {
	/** List of messages queued to be sent. */
	private final ConcurrentLinkedQueue<Message> outbox = new ConcurrentLinkedQueue<Message>();
	/** List of binary messages queued to be sent. */
	private final ConcurrentLinkedQueue<Message> binOutbox = new ConcurrentLinkedQueue<Message>();
	/** List of phone handlers that this manager is currently looking after. */
	private final Map<String, SmsModem> phoneHandlers = new HashMap<String, SmsModem>();
	/** Set of SMS internet services */
	private Set<SmsInternetService> smsInternetServices = new HashSet<SmsInternetService>();

	/** Listener to be passed SMS Listener events from this */
	private SmsListener smsListener;
	/** Flag indicating that the thread should continue running. */
	private boolean running;	
	/** If set TRUE, then thread will automatically try to connect to newly-detected devices. */ 
	private boolean autoConnectToNewPhones;
	/** Signals to thread that it should search the serial ports for connected devices. */
	private boolean refreshPhoneList;

	/**
	 * Set containing all serial numbers of discovered phones.  Necessary because bluetooth/USB
	 * devices may present multiple virtual COM ports to the app. 
	 */
	private final HashSet<String> connectedSerials = new HashSet<String>();
	/**
	 * incoming messages available here if you are not using the 
	 * incoming event notification system.
	 * 
	 * TODO: might change this to be more threadsafe, and use accessors
	 */
	private final LinkedList<CIncomingMessage> receivedMessages = new LinkedList<CIncomingMessage>();
	private String[] portIgnoreList;

	private static Logger LOG = Utils.getLogger(SmsDeviceManager.class);

	/**
	 * Create a polling-variant SMS Handler.
	 * 
	 * To add a message listener, setSmsListener() should be called.
	 */
	public SmsDeviceManager() {
		super("SmsDeviceManager");
		LOG.trace("ENTER");
		// Load the COMM properties file, and extract the IGNORE list from
		// it - this is a list of COM ports that should be ignored.
		PropertySet properties = PropertySet.load(FrontlineSMSConstants.PROPERTIES_COMM);
		String ignore = properties.getProperty(FrontlineSMSConstants.PROPERTIES_COMM_IGNORE);
		if (ignore == null) portIgnoreList = new String[0];
		else portIgnoreList = ignore.toUpperCase().split(",");

		listComPortsAndOwners(false);
		LOG.trace("EXIT");
	}

	public void setSmsListener(SmsListener smsListener) {
		this.smsListener = smsListener;
	}

	public void run() {
		LOG.trace("ENTER");
		running = true;
		while (running) {
			if (refreshPhoneList) {
				LOG.debug("Refreshing phone list...");
				// N.B. why is this not using the value from autoConnectToNewPhones? 
				listComPortsAndOwners(autoConnectToNewPhones);
				refreshPhoneList = false;
			} else {
				// FIXME why are these 2 separate lists?
				if (outbox.size() > 0) {
					sendSmsToPhones();
				}
				if (binOutbox.size() > 0) {
					sendBinarySmsToPhones();
				}
				// Individual phones should sleep, so there's no need to do this here!(?)  Here's
				// a token pause in case things lock up / to stop this thread eating the CPU for
				// breakfast.
				Utils.sleep_ignoreInterrupts(10);
			}
		}
		LOG.trace("EXIT");
	}

	/**
	 * FIXME this has a lot of code duplicated from sendBinarySmsToPhones()  Can they be combined?
	 */
	private void sendSmsToPhones() {
		// When there are HTTP Handlers set up for sending, we ONLY use these for sending.
		// Otherwise, we split the messages between all connected (sending) phones.
		boolean hasFoundWorkingHttpHandler = false;
		if (!smsInternetServices.isEmpty()) {
			// Here we scan through the list of connected phones and determine how many
			// of them are currently being used for SENDING.  Once we've done this, we 
			// can assign messages to the sending phones, and poll receiving phones for
			// incoming messages.
			Iterator<SmsInternetService> httpHandlers = this.smsInternetServices.iterator();

			List<SmsInternetService> toSend = new ArrayList<SmsInternetService>();

			while (httpHandlers.hasNext()) {
				SmsInternetService httpService = httpHandlers.next();
				if (httpService.isConnected() && httpService.isUseForSending()) {
					toSend.add(httpService);
				}
			}

			hasFoundWorkingHttpHandler = !toSend.isEmpty();

			//how many messages per batch?
			int numberOfMessagesToSendPerPhone = 1;
			if (toSend.size() != 0) {
				numberOfMessagesToSendPerPhone = ((outbox.size()%toSend.size() != 0) ? 1:0) +
				(outbox.size() / toSend.size());
			}

			for (SmsInternetService serv : toSend) {
				Message m;
				//grab a load of messages, try for the number suggested in the numberOfMessagesToSendPerPhone
				for (int i = 0; i < numberOfMessagesToSendPerPhone; ++i) {
					if (( m = outbox.poll()) != null) {
						serv.sendSMS(m);
						outgoingMessageEvent(serv, m);
					}
				}
			}
		}
		if (!hasFoundWorkingHttpHandler) {
			// Here we scan through the list of connected phones and determine how many
			// of them are currently being used for SENDING.  Once we've done this, we 
			// can assign messages to the sending phones, and poll receiving phones for
			// incoming messages.
			Iterator<SmsModem> phoneHandlers = this.phoneHandlers.values().iterator();

			//how many messages should be sent on each phone?
			//how many phones available for sending?
			int numberOfPhonesForSending = 0;

			while (phoneHandlers.hasNext()) {
				SmsModem phoneHandler = phoneHandlers.next();
				if (phoneHandler.isConnected() && phoneHandler.isUseForSending()) numberOfPhonesForSending++;
			}
			//how many messages per batch?
			int numberOfMessagesToSendPerPhone = 1;
			if (numberOfPhonesForSending != 0) {
				numberOfMessagesToSendPerPhone = ((outbox.size()%numberOfPhonesForSending != 0) ? 1:0) +
				(outbox.size() / numberOfPhonesForSending);
			}
			//reset iterator
			phoneHandlers = this.phoneHandlers.values().iterator();

			while (phoneHandlers.hasNext()) {
				SmsModem phoneHandler = phoneHandlers.next();
				//check for timeout
				if(phoneHandler.isRunning() && phoneHandler.isTimedOut()) {
					LOG.debug("Watchdog from phone [" + phoneHandler.getPort() + "] has timed out! Disconnecting...");
					// The phone's being unresponsive.  Attempt to disconnect from the phone, remove the serial
					// number from the duplicates list and then add the phone to the reconnect list so we can
					// reconnect to it later.  We should also remove the unresponsive phone from the phoneHandlers
					// list.
					phoneHandler.disconnect();
					connectedSerials.remove(phoneHandler.getSerial());
				} else if(phoneHandler.isConnected()) {
					// If this handset is connected, we can now use it for sending and/or receiving
					// messages as appropriate.

					// If this handset is being used to receive, check for incoming messages.
					if (phoneHandler.isUseForReceiving()) {
						while (phoneHandler.incomingMessageWaiting()) {
							incomingMessageEvent(phoneHandler, phoneHandler.nextIncomingMessage());
						}
					}
					// If this handset is being user for sending, and there are messages in the
					// outbox, then pass the determined number for sending.
					if (phoneHandler.isUseForSending()) {
						Message m;
						//grab a load of messages, try for the number suggested in the numberOfMessagesToSendPerPhone
						for (int i = 0; i < numberOfMessagesToSendPerPhone; ++i) {
							if (( m = outbox.poll()) != null) {
								phoneHandler.sendSMS(m);
								outgoingMessageEvent(phoneHandler, m);
							}
						}

					}
				}
			}
		}
	}

	/**
	 * FIXME this has a lot of code duplicated from sendSmsToPhones()  Can they be combined?
	 */
	private void sendBinarySmsToPhones() {
		// When there are HTTP Handlers set up for sending, we ONLY use these for sending.
		// Otherwise, we split the messages between all connected (sending) phones.
		boolean hasFoundWorkingSmsInternetServiceHandler = false;
		if (!smsInternetServices.isEmpty()) {
			// Here we scan through the list of connected phones and determine how many
			// of them are currently being used for SENDING.  Once we've done this, we 
			// can assign messages to the sending phones, and poll receiving phones for
			// incoming messages.
			Iterator<SmsInternetService> smsInternetServicesHandlers = this.smsInternetServices.iterator();

			List<SmsInternetService> toSend = new ArrayList<SmsInternetService>();

			while (smsInternetServicesHandlers.hasNext()) {
				SmsInternetService smsInternetService = smsInternetServicesHandlers.next();
				if (smsInternetService.isConnected() && smsInternetService.isUseForSending() && smsInternetService.isBinarySendingSupported()) {
					toSend.add(smsInternetService);
				}
			}

			hasFoundWorkingSmsInternetServiceHandler = !toSend.isEmpty();

			//how many messages per batch?
			int numberOfMessagesToSendPerPhone = 1;
			if (toSend.size() != 0) {
				numberOfMessagesToSendPerPhone = ((binOutbox.size()%toSend.size() != 0) ? 1:0) +
				(binOutbox.size() / toSend.size());
			}

			for (SmsInternetService serv : toSend) {
				Message m;
				//grab a load of messages, try for the number suggested in the numberOfMessagesToSendPerPhone
				for (int i = 0; i < numberOfMessagesToSendPerPhone; ++i) {
					if (( m = binOutbox.poll()) != null) {
						serv.sendSMS(m);
						outgoingMessageEvent(serv, m);
					}
				}
			}
		}
		if (!hasFoundWorkingSmsInternetServiceHandler) {
			// Here we scan through the list of connected phones and determine how many
			// of them are currently being used for SENDING.  Once we've done this, we 
			// can assign messages to the sending phones, and poll receiving phones for
			// incoming messages.
			Iterator<SmsModem> phoneHandlers = this.phoneHandlers.values().iterator();

			//how many messages should be sent on each phone?
			//how many phones available for sending?
			int numberOfPhonesForSending = 0;

			while (phoneHandlers.hasNext()) {
				SmsModem phoneHandler = phoneHandlers.next();
				if (phoneHandler.isConnected() && phoneHandler.isUseForSending() && phoneHandler.isBinarySendingSupported()) 
					numberOfPhonesForSending++;
			}
			//how many messages per batch?
			int numberOfMessagesToSendPerPhone = 1;
			if (numberOfPhonesForSending != 0) {
				numberOfMessagesToSendPerPhone = ((binOutbox.size()%numberOfPhonesForSending != 0) ? 1:0) +
				(binOutbox.size() / numberOfPhonesForSending);
			}
			//reset iterator
			phoneHandlers = this.phoneHandlers.values().iterator();

			while (phoneHandlers.hasNext()) {
				SmsModem phoneHandler = phoneHandlers.next();
				//check for timeout
				if(phoneHandler.isRunning() && phoneHandler.isTimedOut()) {
					LOG.debug("Watchdog from phone [" + phoneHandler.getPort() + "] has timed out! Disconnecting...");
					// The phone's being unresponsive.  Attempt to disconnect from the phone, remove the serial
					// number from the duplicates list and then add the phone to the reconnect list so we can
					// reconnect to it later.  We should also remove the unresponsive phone from the phoneHandlers
					// list.
					phoneHandler.disconnect();
					connectedSerials.remove(phoneHandler.getSerial());
				} else if(phoneHandler.isConnected()) {
					// If this handset is connected, we can now use it for sending and/or receiving
					// messages as appropriate.

					// If this handset is being used to receive, check for incoming messages.
					if (phoneHandler.isUseForReceiving()) {
						while (phoneHandler.incomingMessageWaiting()) {
							incomingMessageEvent(phoneHandler, phoneHandler.nextIncomingMessage());
						}
					}
					// If this handset is being user for sending, and there are messages in the
					// outbox, then pass the determined number for sending.
					if (phoneHandler.isUseForSending()) {
						Message m;
						//grab a load of messages, try for the number suggested in the numberOfMessagesToSendPerPhone
						for (int i = 0; i < numberOfMessagesToSendPerPhone; ++i) {
							if (( m = binOutbox.poll()) != null) {
								phoneHandler.sendSMS(m);
								outgoingMessageEvent(phoneHandler, m);
							}
						}

					}
				}
			}
		}
	}
	
	/**
	 * list com ports, optionally find phones, and optionally connect to them
	 * @param autoDiscoverPhones - if false, the call will only enumerate COM ports and find the owners - not try to auto-detect phones
	 * @param connectToAllDiscoveredPhones - only works if findPhoneNames is true, and will try to not connect to duplicate connections to the same phone.
	 */
	public void refreshPhoneList(boolean autoConnectToNewPhones) {
		this.autoConnectToNewPhones = autoConnectToNewPhones;
		refreshPhoneList = true;
	}

	/**
	 * Scan through the COM ports this computer is displaying.
	 * When an unowned port is found, we initiate a PhoneHandler
	 * detect an AT device on this port. Ignore all non-serial
	 * ports and all ports whose names' appear on our "ignore"
	 * list.
	 * @param findPhoneNames
	 * @param connectToAllDiscoveredPhones
	 */
	public void listComPortsAndOwners(boolean connectToAllDiscoveredPhones) {
		LOG.trace("ENTER");
		Enumeration<CommPortIdentifier> portIdentifiers = CommUtils.getPortIdentifiers();
		LOG.debug("Getting ports...");
		while (portIdentifiers.hasMoreElements()) {
			requestConnect(portIdentifiers.nextElement(), connectToAllDiscoveredPhones);
		}
		LOG.trace("EXIT");
	}

	/**
	 * Checks if a COM port should be ignored (rather than connected to).
	 * @param comPortName
	 * @return
	 */
	private boolean shouldIgnore(String comPortName) {
		for (String ig : portIgnoreList) {
			if (ig.equalsIgnoreCase(comPortName)) return true;
		}
		return false;
	}


	/**
	 * Request that an SMS with the specified text be sent to the requested
	 * number.
	 * @param targetNumber
	 * @param smsMessage
	 * @return the Message object 
	 */
	public void sendSMS(Message outgoingMessage) {
		LOG.trace("ENTER");
		outgoingMessage.setStatus(Message.STATUS_OUTBOX);
		if (outgoingMessage.isBinaryMessage()) {
			binOutbox.add(outgoingMessage);
			LOG.debug("Message added to binOutbox. Size is [" + binOutbox.size() + "]");
		} else {
			outbox.add(outgoingMessage);
			LOG.debug("Message added to outbox. Size is [" + outbox.size() + "]");
		}
		if (smsListener != null) smsListener.outgoingMessageEvent(null, outgoingMessage);
		LOG.trace("EXIT");
	}

	/**
	 * Remove the supplied message from outbox.
	 * 
	 * @param deleted
	 */
	public void removeFromOutbox(Message deleted) {
		LOG.trace("ENTER");
		outbox.remove(deleted);
		LOG.debug("Message [" + deleted + "] removed from outbox. Size is [" + outbox.size() + "]");
		LOG.trace("EXIT");
	}

	/**
	 * Flags the internal thread to stop running.
	 */
	public void stopRunning() {
		this.running = false;

		// Disconnect all phones.
		for(SmsModem p : phoneHandlers.values()) {
			p.setAutoReconnect(false);
			p.disconnect();
			connectedSerials.remove(p.getSerial());
		}
	}

	public void incomingMessageEvent(SmsDevice receiver, CIncomingMessage msg) {
		// If we've got a higher-level listener attached to this, pass the message 
		// up to there.  Otherwise, add it to our internal list
		if (smsListener != null) smsListener.incomingMessageEvent(receiver, msg);
		else receivedMessages.add(msg);
	}

	public void outgoingMessageEvent(SmsDevice sender, Message msg) {
		if (smsListener != null) smsListener.outgoingMessageEvent(sender, msg);
		if (msg.getStatus() == Message.STATUS_FAILED) {
			if (msg.getRetriesRemaining() > 0) {
				msg.setRetriesRemaining(msg.getRetriesRemaining() - 1);
				msg.setSenderMsisdn("");
				sendSMS(msg);
			}
		}
	}

	public boolean hasPhoneConnected(String port) {
		SmsDevice phoneHandler = phoneHandlers.get(port);
		return phoneHandler != null && phoneHandler.isConnected();
	}

	/**
	 * called when one of the SMS devices (phones or http senders) has a change in status,
	 * such as detection, connection, disconnecting, running out of batteries, etc.
	 * see PhoneHandler.STATUS_CODE_MESSAGES[smsDeviceEventCode] to get the relevant messages
	 *  
	 * @param activeDevice
	 * @param smsDeviceEventCode
	 */
	public void smsDeviceEvent(SmsDevice device, int smsDeviceEventCode) {
		LOG.trace("ENTER");
		if (device instanceof SmsModem) {
			LOG.debug("Event [" + FrontlineSMSConstants.Dependants.STATUS_CODE_MESSAGES[smsDeviceEventCode] + "]");
			SmsModem activeDevice = (SmsModem) device;
			switch (smsDeviceEventCode) {
			case SmsModem.STATUS_DISCONNECTED:
				// A device has just disconnected.  If we aren't using the device for sending or receiving,
				// then we should just ditch it.  However, if we *are* actively using the device, then we
				// would probably want to attempt to reconnect.  Also, if we were previously connected to 
				// this device then we should now remove its serial number from the list of connected serials.
				if(!activeDevice.isDuplicate()) connectedSerials.remove(activeDevice.getSerial());
				break;
			case SmsModem.STATUS_CONNECTING:
				// The max speed for this connection has been found.  If this connection
				// is a duplicate, we should set the duplicate flag to true.  Otherwise,
				// we may wish to reconnect.
				if (autoConnectToNewPhones) {
					boolean isDuplicate = !connectedSerials.add(activeDevice.getSerial());
					activeDevice.setDuplicate(isDuplicate);
					if(!isDuplicate) activeDevice.connect();
				}
				break;
			}
		}
		if (smsListener != null) smsListener.smsDeviceEvent(device, smsDeviceEventCode);
		LOG.trace("EXIT");
	}


	/**
	 * Get's all {@link SmsDevice}s that this manager is currently connected to
	 * or investigating.
	 * @return
	 */
	public Collection<SmsDevice> getAllPhones() {
		Set<SmsDevice> ret = new HashSet<SmsDevice>();
		ret.addAll(phoneHandlers.values());
		ret.addAll(smsInternetServices);
		return ret;
	}

	/**
	 * Request the phone manager to attempt a connection to a particular COM port.
	 * @param port
	 */
	public void requestConnect(String port) throws NoSuchPortException {
		requestConnect(CommPortIdentifier.getPortIdentifier(port), true);
	}

	public void requestConnect(String portName, int baudRate, String preferredCATHandler) throws NoSuchPortException {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

		LOG.debug("Port Name [" + portName + "]");
		if(!shouldIgnore(portName) && portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
			LOG.debug("It is a suitable port.");
			if(!portIdentifier.isCurrentlyOwned()) {
				LOG.debug("Connecting to port...");
				SmsModem phoneHandler = new SmsModem(portName, this);
				phoneHandlers.put(portName, phoneHandler);
				phoneHandler.start(baudRate, preferredCATHandler);
			} else {
				if(!phoneHandlers.containsKey(portName)) {
					// If we don't have a handle on this port, but it's owned by someone else,
					// then we add it to the phoneHandlers list anyway so that we can see its
					// status.
					LOG.debug("Port currently owned by another process.");
					phoneHandlers.put(portName, new SmsModem(portName, this));
				}
			}
		}
	}

	public void addSmsInternetService(SmsInternetService smsInternetService) {
		smsInternetService.setSmsListener(smsListener);
		if (smsInternetServices.contains(smsInternetService)) {
			smsInternetService.restartThisThing();
		} else {
			smsInternetServices.add(smsInternetService);
			smsInternetService.startThisThing();
		}
	}

	/**
	 * Remove a service from this {@link SmsDeviceManager}.
	 * @param service
	 */
	public void removeSmsInternetService(SmsInternetService service) {
		smsInternetServices.remove(service);
		disconnectSmsInternetService(service);
	}
	
	public void disconnect(SmsDevice device) {
		if(device instanceof SmsModem) disconnectPhone((SmsModem)device);
		else if(device instanceof SmsInternetService) disconnectSmsInternetService((SmsInternetService)device);
	}

	private void disconnectPhone(SmsModem modem) {
		modem.setAutoReconnect(false);
		modem.disconnect();
		connectedSerials.remove(modem.getSerial());
	}

	public void stopDetection(String port) {
		// FIXME is this thread-safe?  Should really get handler ONCE and then call methods.
		phoneHandlers.get(port).setDetecting(false);
		phoneHandlers.get(port).setAutoReconnect(false);
	}

	private void disconnectSmsInternetService(SmsInternetService device) {
		device.stopThisThing();
	}

	/**
	 * Attempts to connect to the supplied comm port
	 * @param portIdentifier
	 * @param connectToDiscoveredPhone
	 * @param findPhoneName
	 */
	private void requestConnect(CommPortIdentifier portIdentifier, boolean connectToDiscoveredPhones) {
		String portName = portIdentifier.getName();
		LOG.debug("Port Name [" + portName + "]");
		if(!shouldIgnore(portName) && portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
			LOG.debug("It is a suitable port.");
			if(!portIdentifier.isCurrentlyOwned()) {
				LOG.debug("Connecting to port...");
				SmsModem phoneHandler = new SmsModem(portName, this);
				phoneHandlers.put(portName, phoneHandler);
				if(connectToDiscoveredPhones) phoneHandler.start();
			} else if(!phoneHandlers.containsKey(portName)) {
				// If we don't have a handle on this port, but it's owned by someone else,
				// then we add it to the phoneHandlers list anyway so that we can see its
				// status.
				LOG.debug("Port currently owned by another process.");
				phoneHandlers.put(portName, new SmsModem(portName, this));
			}
		}
	}

	public Collection<SmsInternetService> getSmsInternetServices() {
		return this.smsInternetServices;
	}
}
