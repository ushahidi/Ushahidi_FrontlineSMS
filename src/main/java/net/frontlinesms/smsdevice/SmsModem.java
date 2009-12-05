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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import serial.*;

import net.frontlinesms.Utils;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.listener.SmsListener;
import net.frontlinesms.resources.ResourceUtils;

import org.apache.log4j.Logger;
import org.smslib.*;
import org.smslib.CService.MessageClass;

/**
 * Class for handling the serial connection to an individual SMS device.
 * 
 * @author Ben Whitaker ben(at)masabi(dot)com
 * @author Alex Anderson alex(at)masabi(dot)com
 * @author Carlos Eduardo Genz kadu(at)masabi(dot)com
 */
public class SmsModem extends Thread implements SmsDevice {
	
//> CONSTANTS
	private static final boolean SEND_BULK = true;
	private static final int SMS_BULK_LIMIT = 10;

	/** The time, in millis, that this phone handler must have been unresponsive for before it is deemed TIMED OUT
	 * As far as I know there is no basis for the time chosen for this timeout. */
	private static final int TIMEOUT = 80 * 1000; // = 80 seconds;

	/** The different baud rates that a PhoneHandler may connect at. */
	private static final int[] COMM_SPEEDS = new int[] {
		9600,
		19200,
		38400,
		57600,
		115200,
		230400,
		460800,
		921600
	};

	/** Logging object */
	private static Logger LOG = Utils.getLogger(SmsModem.class);
		
//> PROPERTIES
	private static HashMap<String, String> cathandlerAliases = initAliasesFromFile(ResourceUtils.getConfigDirectoryPath() + "conf/CATHandlerAliases.txt");
	private static HashMap<String, String> manufacturerAliases = initAliasesFromFile(ResourceUtils.getConfigDirectoryPath() + "conf/manufacturerAliases.txt");
	private static HashMap<String, String> modelAliases = initAliasesFromFile(ResourceUtils.getConfigDirectoryPath() + "conf/modelAliases.txt");

	/**
	 * Watchdog to monitor when a phone handler has lost communication with the phone
	 */
	private long timeOfLastResponseFromPhone;

	private final LinkedList<CIncomingMessage> inbox = new LinkedList<CIncomingMessage>();
	private final ConcurrentLinkedQueue<Message> outbox = new ConcurrentLinkedQueue<Message>();
	/** The SmsListener to which this phone handler should report SMS Message events. */
	private SmsListener smsListener;

	/** The name of the COM port that this PhoneHandler controls. */
	private String portName;
	/**
	 * Indicates whether this PhoneHandler's serial communication
	 * thread is running.  This should *only* be set false in run()
	 * when certain other conditions are fulfilled - when
	 * smsLibConnected is false AND autoReconnect is false.
	 */ 
	private boolean running;
	private boolean phonePresent;
	private boolean smsLibConnected;
	private boolean tryToConnect;
	/** The baud rate, in bps, that this phone handler will connect at. */
	private int baudRate;
	private CService cService;
	private boolean autoDetect;
	private boolean autoReconnect;
	/** true if this is another port into a phone that is already discovered */
	private boolean duplicate;
	/** true when this PhoneHandler has been disconnected using disconnect() */
	private boolean disconnected;
	/** true if this phone is or will be used for sending SMS messages */ 
	private boolean useForSending;
	/** true if this phone is or will be used for receiving SMS messages */
	private boolean useForReceiving;
	/** true if this thread has timed out */
	private boolean timedOut;

	private boolean detecting;
	private boolean disconnecting;

	private boolean deleteMessagesAfterReceiving;
	private boolean useDeliveryReports;

	private String manufacturer = "";
	private String model = "";
	private String preferredCATHandler = "";
	private String serialNumber = "";
	private String imsiNumber;
	private int batteryPercent;
	private int signalPercent;
	private String msisdn;
	
	/** The status of this device */
	private SmsModemStatus status = SmsModemStatus.DORMANT;
	/** Extra info relating to the current status. */
	private String statusDetail;

//> CONSTRUCTORS
	/**
	 * Create a new instance {@link SmsModem}
	 * @param portName the name of the port which this modem is found on.  Value for {@link #portName}
	 * @param smsListener the value for {@link #smsListener} 
	 */
	public SmsModem(String portName, SmsListener smsListener) {
		super("SmsModem :: " + portName);
		/*
		 * Make this into a daemon thread - we never know when it may get blocked in native code.  Indeed this can be
		 * witnessed by connecting to a port without a device attached, which can then block as such (I think this one 
		 * was a bluetooth port):
		 *	Win32SerialPort.nwrite(byte[], int, int) line: not available [native method]	
		 *	Win32SerialPort.write(byte[], int, int) line: 672	
		 *	Win32SerialPort.write(int) line: 664	
		 *	Win32SerialOutputStream.write(int) line: 34
		 *  ...
		 */	
		super.setDaemon(true);

		this.smsListener = smsListener;
		this.portName = portName;

		resetWatchdog();

		//sets up the phone handler on a certain port, it will attempt to auto-detect the phone
		try {
			String currentOwner = CommPortIdentifier.getPortIdentifier(this.portName).getCurrentOwner();
			if(currentOwner == null) currentOwner = "";
			
			this.setStatus(SmsModemStatus.OWNED_BY_SOMEONE_ELSE, currentOwner);
		} catch (NoSuchPortException e) {
			LOG.debug("Error getting owner from port", e);
			//doesn't matter if it doesn't get this message
			// TODO surely this NoSuchPortException should be thrown
		}
	}
	
//> ACCESSOR METHODS
	/** @return {@link #status} */
	public SmsDeviceStatus getStatus() {
		return this.status;
	}
	
	/**
	 * Set the status of this {@link SmsModem}, and fires an event to {@link #smsListener}
	 * @param status the status
	 * @param detail detail relating to the status
	 */
	private void setStatus(SmsModemStatus status, String detail) {
		this.status = status;
		this.statusDetail = detail;
		LOG.debug("Status [" + status.name()
				+ (detail == null?"":": "+detail)
				+ "]");
		
		if (smsListener != null) {
			smsListener.smsDeviceEvent(this, this.status);
		}
	}
	
	/** @return {@link #statusDetail} */
	public String getStatusDetail() {
		return this.statusDetail;
	}

	/**
	 * @return true if there are waiting messages, false otherwise.
	 */
	public boolean incomingMessageWaiting() {
		return !inbox.isEmpty();
	}
	/**
	 * @return the next incoming message, waiting to be read.
	 */
	public CIncomingMessage nextIncomingMessage() {
		return inbox.poll();
	}

	public String getPort() {
		return portName;
	}

	/**
	 * Checks if this PhoneHandler is currently active.  Returns true if either the thread
	 * has stopped running OR the handler has been forcibly disconnected.  This extra check
	 * is necessary as PhoneHandler threads can sometimes go to sleep and never wake up
	 * (it seems).
	 * @return true if this phone is currently active, false otherwise.
	 */
	public boolean isRunning() {
		return !disconnected && running;
	}

	/**
	 * Checks if this instance of PhoneHandler has timed out.  Once a PhoneHandler has
	 * timed out once, it becomes unusable.
	 * @return true if this phone has timeout, false otherwise.
	 */
	public boolean isTimedOut() {
		if (!timedOut) timedOut = (System.currentTimeMillis() - timeOfLastResponseFromPhone) > TIMEOUT;
		return timedOut;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public int getBatteryPercent() {
		if (smsLibConnected) return cService.getDeviceInfo().getBatteryLevel();
		else return batteryPercent;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public int getSignalPercent() {
		if (smsLibConnected) return cService.getDeviceInfo().getSignalLevel();
		else return signalPercent;
	}

	/**
	 * Checks if this instance of PhoneHandler is connected to a device.
	 * @return true if this phone is connected, false otherwise.
	 */
	public boolean isConnected() {
		return smsLibConnected;
	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.SmsDevice#isUseForSending()
	 */
	public boolean isUseForSending() {
		return useForSending;
	}
	public void setUseForSending(boolean useForSend) {
		useForSending = useForSend;
	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.SmsDevice#isUseForReceiving()
	 */
	public boolean isUseForReceiving() {
		return useForReceiving;
	}

	/**
	 * This throws a {@link RuntimeException}, so {@link SmsDevice#supportsReceive()} should be checked before calling this.
	 */
	public void setUseForReceiving(boolean useForReceive) {
		if(!supportsReceive()) throw new ReceiveNotSupportedException();
		useForReceiving = useForReceive;
	}

	public boolean supportsReceive() {
		return cService.supportsReceive();
	}

	public boolean isDuplicate() {
		return duplicate;
	}
	
	/**
	 * Sets the status of this modem.  If the status is {@link SmsModemStatus#DUPLICATE}, an
	 * event will be triggered with {@link #smsListener}.
	 * @param newDuplicate new value for {@link #duplicate}
	 */
	public void setDuplicate(boolean newDuplicate) {
		duplicate = newDuplicate;
		if (duplicate) {
			this.setStatus(SmsModemStatus.DUPLICATE, null);
		}
	}
	
	/**
	 * Checks if this instance of PhoneHandler has a phone present.
	 * @return true if this has a phone present, false otherwise.
	 */
	public boolean isPhonePresent() {
		return phonePresent;
	}

	/** @return {@link #manufacturer} */
	public String getManufacturer() {
		return manufacturer;
	}

	/** @return {@link #model} */
	public String getModel() {
		return model;
	}

	/** @return {@link #serialNumber} */
	public String getSerial() {
		return serialNumber;
	}

	public void connect(){
		if (!phonePresent || duplicate || manufacturer.length() == 0) return;
		tryToConnect = true;
		resetWatchdog();
	}

	/**
	 * Try to connect to the device attached to this port, using the speed informed 
	 * as parameter.
	 * 
	 * @param maxSpeedRequested The max speed for this device.
	 * @param manufacturerName The device's manufacturer name.
	 * @param modelName The device's model name.
	 * @param preferredCATHandler TODO
	 * @return true if the connection was successful, false otherwise.
	 */
	private boolean connect(int maxSpeedRequested, String manufacturerName, String modelName, String preferredCATHandler) {
		LOG.trace("ENTER");
		LOG.debug("Attempting to connect:" 
				+ "\n - Speed [" + maxSpeedRequested + "]" 
				+ "\n - Manufacturer [" + manufacturerName + "]"
				+ "\n - Model [" + modelName + "]"
				+ "\n - CAT Handler Alias [" + preferredCATHandler + "]");

		this.setStatus(SmsModemStatus.TRY_TO_CONNECT, Integer.toString(maxSpeedRequested));
		
		resetWatchdog();
		cService = new CService(this.portName, maxSpeedRequested, manufacturerName, modelName, preferredCATHandler);
		LOG.debug("Created service [" + cService + "]");

		try {
			// If the GSM device is PIN protected, enter the PIN here.
			// PIN information will be used only when the GSM device reports
			// that it needs a PIN in order to continue.
			cService.setSimPin("0000");

			// Some modems may require a SIM PIN 2 to unlock their full functionality.
			// Like the Vodafone 3G/GPRS PCMCIA card.
			// If you have such a modem, you should also define the SIM PIN 2.
			cService.setSimPin2("0000");

			// Normally, you would want to set the SMSC number to blank. GSM
			// devices get the SMSC number information from their SIM card.
			cService.setSmscNumber("");

			Utils.sleep_ignoreInterrupts(500);

			resetWatchdog();
			cService.connect();
			resetWatchdog();

			// Lets get info about the GSM device...
			setManufacturer(cService.getManufacturer());
			setModel(cService.getModel());

			this.msisdn = cService.getMsisdn();
			LOG.debug("Msisdn [" + this.msisdn + "]");

			this.serialNumber = cService.getSerialNo();
			LOG.debug("Serial Number [" + this.serialNumber + "]");

			this.imsiNumber = cService.getImsi();
			LOG.debug("Imsi Number [" + this.imsiNumber + "]");

			LOG.debug("Mobile Device Information: "
					+ "\n - Manufacturer [" + manufacturerName + "]"
					+ "\n - Model [" + modelName + "]"
					+ "\n - Preferred CAT Handler [" + preferredCATHandler + "]"
					+ "\n - Used CAT Handler [" + cService.getAtHandlerName() + "]"
					+ "\n - Serial Number [" + cService.getDeviceInfo().getSerialNo() + "]"
					+ "\n - IMSI [" + cService.getDeviceInfo().getImsi() + "]"
					+ "\n - S/W Version [" + cService.getDeviceInfo().getSwVersion() + "]"
					+ "\n - Battery Level [" + cService.getDeviceInfo().getBatteryLevel() + "%]"
					+ "\n - Signal Level [" + cService.getDeviceInfo().getSignalLevel() + "%]"
					+ "\n - Baud Rate [" + baudRate + "]");

			this.setStatus(SmsModemStatus.CONNECTING, null);
			
			if (isDuplicate()) {
				disconnect(false);
				return false;
			}
			phonePresent = true;
			autoReconnect = true;
			smsLibConnected = true;
			
			this.setStatus(SmsModemStatus.CONNECTED, Integer.toString(maxSpeedRequested));
			
			resetWatchdog();
			LOG.debug("Connection successful!");
			LOG.trace("EXIT");
			return true;
		} catch (GsmNetworkRegistrationException e) {
			this.setStatus(SmsModemStatus.GSM_REG_FAILED, null);
		} catch (SMSLibDeviceException ex) {
			this.setStatus(SmsModemStatus.FAILED_TO_CONNECT, ex.getClass().getCanonicalName() + " : " + ex.getMessage());
		} catch (Exception ex) {
			this.setStatus(SmsModemStatus.DISCONNECTED, ex.getClass().getCanonicalName() + " : " + ex.getMessage());
		}
		LOG.debug("Connection failed!");
		LOG.trace("EXIT");
		return false;
	}

	/**
	 * Start the modem handler listening to the serial port with the requested connection settings.
	 * @param baudRate
	 * @param manufacturer
	 * @param model
	 * @param preferredCATHAndler
	 */
	public void start(int baudRate, String preferredCATHAndler) {
		this.autoDetect = false;
		this.tryToConnect = true;
		this.baudRate = baudRate;
		this.preferredCATHandler = preferredCATHAndler;
		super.start();
	}

	/**
	 * Start the sms modem listening to the serial port, and autodetect the settings to use.
	 */
	@Override
	public synchronized void start() {
		this.autoDetect = true;
		super.start();
	}


	public void run() {
		LOG.trace("ENTER");
		if(autoDetect) running = _doDetection();
		else running = true;
		while (running) {
			boolean noActivity = true;

			resetWatchdog();

			if(tryToConnect) {
				smsLibConnected = connect(baudRate, manufacturer, model, preferredCATHandler);
				tryToConnect = false;
			}

			if(smsLibConnected) {
				try {
					//check for incoming messages
					if (useForReceiving) {
						long startTime = System.currentTimeMillis();
						LOG.debug("Checking for received messages...");
						int newMessages = checkForMessages();
						if(newMessages > 0) noActivity = false;
						LOG.debug("Check for messages took [" + (System.currentTimeMillis() - startTime) + "]");
					}
					// If there are any messages waiting to be sent, send them now.
					if (useForSending) {
						LOG.debug("Sending some pending messages. Outbox size is [" + outbox.size() + "]");
						resetWatchdog();
						long startTime = System.currentTimeMillis();
						if(SEND_BULK) {
							//create SMS list
							LinkedList<Message> messageList = new LinkedList<Message>();
							Message m;
							while(messageList.size() < SMS_BULK_LIMIT && (m = outbox.poll()) != null) messageList.add(m);
							if(messageList.size() > 0) {
								LOG.debug("Sending bulk of [" + messageList.size() + "] message(s)");
								sendSmsListDirect(messageList);
							}
						} else {
							Message m = outbox.poll();
							if(m != null) {
								LOG.debug("Sending message [" + m.toString() + "]");
								sendSmsDirect(m);
								noActivity = false;
							}
						}
						LOG.debug("Send messages took [" + (System.currentTimeMillis() - startTime) + "]");
						resetWatchdog();
					}
				} catch(UnrecognizedHandlerProtocolException ex) {
					LOG.debug("Invalid protocol", ex);
				} catch(UnableToReconnectException ex) {
					LOG.debug("Fatal exception in device communication.", ex);
					this.setAutoReconnect(false);
					setStatus(SmsModemStatus.DISCONNECT_FORCED, ex.getMessage());
					disconnect(false);
				} catch(SMSLibDeviceException ex) {
					LOG.debug("Phone not connected", ex);
					disconnect(true);
				} catch(IOException ex) {
					LOG.error("Communication failed", ex);
					disconnect(true);
				}
			} else if (autoReconnect) {
				LOG.debug("Trying to reconnect...");
				// Disconnect and relinquish ownership of the COM port.
				disconnect(true);
				// Reconnect on the next loop.
				tryToConnect = true;
			} else {
				running = false;
			}
			// If this thread is still running, we should have a little snooze as
			// checking the phone continuously for messages will:
			//  - waste a lot of processor cycles on PC
			//  - waste phone battery
			//  - be unnecessary as phones are unlikely to be able to receive messages quicker than ~every 3s
			if(running) {
				if (noActivity) {
					try {
						if(smsLibConnected) cService.keepGsmLinkOpen();
						Utils.sleep_ignoreInterrupts(5000); /* 5 seconds */
					} catch (Throwable t) {
						LOG.debug("", t);
						tryToConnect = false;
						disconnect(true);
					}
				} else {
					Utils.sleep_ignoreInterrupts(100); /* 0.1 seconds */
				}
			}
		}
		LOG.trace("EXIT");
	}

	private final void setManufacturer(String manufacturer) {
		LOG.debug("Manufacturer before translation [" + manufacturer + "]");
		this.manufacturer = translateManufacturer(manufacturer);
		LOG.debug("Manufacturer after translation [" + this.manufacturer + "]");
	}

	private final void setModel(String model) {
		LOG.debug("Model before translation [" + model + "]");
		this.model = translateModel(manufacturer, model);
		LOG.debug("Model after translation [" + this.model + "]");
		setPreferredCatHandler();
	}

	private final void setPreferredCatHandler() {
		this.preferredCATHandler = translateCATHandlerModel(manufacturer, model);
		LOG.debug("Preferred CAT Handler [" + this.preferredCATHandler + "]");
	}

	/**
	 * Discover the fastest speed at which we can connect to the AT device on this port.
	 * 
	 * N.B. THIS SHOULD ONLY BE CALLED FROM WITHIN run() - it is put here for readability.
	 * 
	 * FIXME this needs to fire the correct events.
	 * 
	 * @return true if a phone was found, or false otherwise
	 */
	private boolean _doDetection() {
		LOG.trace("ENTER");
		detecting = true;
		int maxBaudRate = 0;
		boolean phoneFound = false;
		
		this.setStatus(SmsModemStatus.SEARCHING, null);
		
		for (int currentBaudRate : COMM_SPEEDS) {
			if (!isDetecting()) {
				disconnect(true);
				return false;
			}
			LOG.debug("Testing baud rate [" + currentBaudRate + "]");
			if (maxBaudRate == 0) {
				this.setStatus(SmsModemStatus.SEARCHING, Integer.toString(currentBaudRate));
			}

			resetWatchdog();
			cService = new CService(portName, currentBaudRate, "", "", "");
			try {
				cService.serialDriver.open();
				// wait for port to open and AT handler to awake
				Utils.sleep_ignoreInterrupts(500);
				cService.serialDriver.send("AT\r");
				Utils.sleep_ignoreInterrupts(500); // Wait here just in case the phone does not respond very quickly, e.g Nokia 6310i throws IOException without this line.
				String response = cService.serialDriver.getResponse();

				// If the phone returns an OK, then it looks like it works at this baud
				// rate.  Save this as the fastest speed, and then search at the next speed.
				if (response.contains("OK")) {
					phoneFound = true;
					maxBaudRate = currentBaudRate;
					setStatus(SmsModemStatus.DETECTED, Integer.toString(currentBaudRate));
				} 
			} catch(IOException ex) {
				// TODO Here, we've caught an IOException.  This could be something
				// quite fatal, like the port disappearing, or perhaps it could be
				// something less bad?  Let's assume it's fatal.
				LOG.error("Communication failed", ex);
				phoneFound = false;
				break;
			} catch(TooManyListenersException ex) {
				LOG.debug("Too Many Listeners", ex);
			} catch(UnsupportedCommOperationException ex) {
				LOG.debug("Unsupported Operation", ex);
			} catch(NoSuchPortException ex) {
				LOG.debug("Port does not exist", ex);
			} catch(PortInUseException ex) {
				LOG.debug("Port already in use", ex);
			} finally {
				disconnect(!phoneFound);
			}
		}

		if (!phoneFound) {
			disconnect(false);
			setStatus(SmsModemStatus.NO_PHONE_DETECTED, null);
		} else {
			try {
				baudRate = maxBaudRate;
				phonePresent = true;
				LOG.debug("Phone found, max speed is [" + baudRate + "]");
				cService = new CService(portName, maxBaudRate, "", "", "");
				try {
					cService.serialDriver.open();
					// wait for port to open and AT handler to awake
					Utils.sleep_ignoreInterrupts(500);
				} catch(TooManyListenersException ex) {
					LOG.debug("Too Many Listeners", ex);
				} catch(UnsupportedCommOperationException ex) {
					LOG.debug("Unsupported Operation", ex);
				} catch(NoSuchPortException ex) {
					LOG.debug("Port does not exist", ex);
				} catch(PortInUseException ex) {
					LOG.debug("Port already in use", ex);
				} 
				setManufacturer(cService.getManufacturer());
				setModel(cService.getModel());
				
				this.msisdn = cService.getMsisdn();
				LOG.debug("Msisdn [" + this.msisdn + "]");

				this.serialNumber = cService.getSerialNo();
				LOG.debug("Serial Number [" + this.serialNumber + "]");

				this.imsiNumber = cService.getImsi();
				LOG.debug("Imsi Number [" + this.imsiNumber + "]");

				try { 
					this.batteryPercent = cService.getBatteryLevel();
					LOG.debug("Battery Percent [" + this.batteryPercent + "]");
				} catch(NumberFormatException ex) {
					LOG.debug("Invalid Battery value [" + this.batteryPercent + "]", ex);
				}

				this.setStatus(SmsModemStatus.MAX_SPEED_FOUND, Integer.toString(maxBaudRate));
				if(!duplicate) {
					tryToConnect = true;
				}
			} catch (IOException ex) {
				LOG.error("Communication error", ex);
				baudRate = 0;
				phonePresent = false;
				tryToConnect = false;
			} catch (Exception ex) {
				LOG.error("Unexpected error while detecting phone", ex);
				baudRate = 0;
				phonePresent = false;
				tryToConnect = false;
			} finally {
				disconnect(false);				
			}
		}
		LOG.trace("EXIT");
		detecting = false;
		return phoneFound;
	}

	/** NB. Currently resets LAST ACTIVE time rather than the time left, or time to die */
	/** Resets the watchdog timer - used for calculating timeouts. */
	private final void resetWatchdog() {
		timeOfLastResponseFromPhone = System.currentTimeMillis();
	}

	/**
	 * Checks if there is new messages ready to be read from the attached device.
	 * 
	 * @throws IOException
	 * @return The number of new messages retrieved
	 * @throws SMSLibDeviceException 
	 */
	@SuppressWarnings("unchecked")
	public int checkForMessages() throws IOException, SMSLibDeviceException {	
		LOG.trace("ENTER");
		LinkedList messageList = new LinkedList();
		resetWatchdog();
		cService.readMessages(messageList, MessageClass.UNREAD); 
		resetWatchdog();

		int messagesRead = messageList.size();

		LOG.debug("[" + messagesRead + "] message(s) received.");
		while (messageList.size() > 0) {
			resetWatchdog();
			CIncomingMessage msg = (CIncomingMessage) messageList.removeFirst();
			try {
				LOG.debug("- From [" + msg.getOriginator() + "]"
						+ "\n -Message [" + msg.getText() + "]"
						+ "\n -ID [" + msg.getId() + "]"
						+ "\n -Mem Index [" + msg.getMemIndex() + "]"
						+ "\n -Mem Location [" + msg.getMemLocation() + "]"
						+ "\n -Message Encoding [" + msg.getMessageEncoding() + "]"
						+ "\n -Protocol ID [" + msg.getPid() + "]"
						+ "\n -Reference Number [" + msg.getRefNo() + "]"
						+ "\n -Type [" + msg.getType() + "]"
						+ "\n -Date [" + msg.getDate() + "]");

				if (msisdn != null && msisdn.length() != 0) {
					msg.setId(msisdn);
				} else if (serialNumber != null && serialNumber.length() != 0) {
					msg.setId(serialNumber);
				} else if (imsiNumber != null) {
					msg.setId(imsiNumber);
				}
				LOG.debug("Changed ID [" + msg.getId() + "]");

				if (smsListener != null) {
					if (useDeliveryReports || msg.getType() != CIncomingMessage.MessageType.StatusReport) {
						smsListener.incomingMessageEvent(this, msg);
					}
				} else inbox.add(msg);

				if (isDeleteMessagesAfterReceiving() || msg.getType() == CIncomingMessage.MessageType.StatusReport) {
					//delete msg if is supposed to do it, or if it is a delivery report.
					LOG.debug("Removing message [" + msg.getId() + "] from phone.");
					cService.deleteMessage(msg);
				}
			} catch (Exception e) {
				//couldn't process message
				LOG.info("Unable to process message.", e);
			}
			resetWatchdog();
		}

		LOG.trace("EXIT");
		return messagesRead;
	}


	/* (non-Javadoc)
	 * @see net.frontlinesms.SmsDevice#sendSMS(net.frontlinesms.data.Message)
	 */
	public void sendSMS(Message outgoingMessage) {
		LOG.trace("ENTER");
		outgoingMessage.setStatus(Message.STATUS_PENDING);

		if (msisdn != null && !msisdn.equals("")) {
			outgoingMessage.setSenderMsisdn(msisdn);
		} else if (serialNumber != null && !serialNumber.equals("")) {
			outgoingMessage.setSenderMsisdn(serialNumber);
		} // Otherwise it will go with blank sender.

		outbox.add(outgoingMessage);
		if (smsListener != null) {
			smsListener.outgoingMessageEvent(this, outgoingMessage);
		}
		LOG.debug("Message added to outbox. Size is [" + outbox.size() + "]");

		LOG.trace("EXIT");
	}

	/**
	 * Send an SMS message using this phone handler.
	 * @param message The message to be sent.
	 */
	private void sendSmsDirect(Message message) throws IOException {
		LOG.trace("ENTER");
		LOG.debug("Sending [" + message.getTextContent() + "] to [" + message.getRecipientMsisdn() + "]");
		COutgoingMessage cMessage = new COutgoingMessage(message.getRecipientMsisdn(), message.getTextContent());

		// Do we require a Delivery Status Report?
		cMessage.setStatusReport(this.useDeliveryReports);

		// Ok, finished with the message parameters, now send it!
		try {
			cService.sendMessage(cMessage);
			if (cMessage.getRefNo() != -1) {
				message.setDispatchDate(cMessage.getDispatchDate());
				message.setSmscReference(cMessage.getRefNo());
				message.setStatus(Message.STATUS_SENT);
				LOG.debug("Message [" + message + "] was sent!");
			} else {
				//message not sent
				//failed to send
				message.setStatus(Message.STATUS_FAILED);
				LOG.debug("Message [" + message + "] was not sent!");
			}
		} catch(Exception ex) {
			message.setStatus(Message.STATUS_FAILED);
			LOG.debug("Failed to send message [" + message + "]", ex);
			LOG.info("Failed to send message");
			throw new IOException();
		} finally {
			if (smsListener != null) {
				smsListener.outgoingMessageEvent(this, message);
			}
		}
	}

	/** @param msisdn new value for {@link #msisdn} */
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public boolean isDeleteMessagesAfterReceiving() {
		return deleteMessagesAfterReceiving;
	}

	public void setDeleteMessagesAfterReceiving(
			boolean deleteMessagesAfterReceiving) {
		this.deleteMessagesAfterReceiving = deleteMessagesAfterReceiving;
	}

	public boolean isUseDeliveryReports() {
		return useDeliveryReports;
	}

	public void setUseDeliveryReports(boolean useDeliveryReports) {
		this.useDeliveryReports = useDeliveryReports;
	}

	public void setAutoReconnect(boolean autoReconnect) {
		this.autoReconnect = autoReconnect;
	}

	public boolean isDetecting() {
		return detecting;
	}

	public void setDetecting(boolean detecting) {
		this.detecting = detecting;
	}

	public boolean isTryToConnect() {
		return tryToConnect;
	}

	/**
	 * Set the baud rate that this phone should connect at.
	 * @param baudRate new value for {@link #baudRate}
	 */
	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	/** @see SmsDevice#setSmsListener(SmsListener) */
	public void setSmsListener(SmsListener smsListener) {
		this.smsListener = smsListener;
	}

	/** @see SmsDevice#isBinarySendingSupported() */
	public boolean isBinarySendingSupported() {
		return cService.supportsBinarySmsSending();
	}
	
	/** @see SmsDevice#isUcs2SendingSupported() */
	public boolean isUcs2SendingSupported() {
		return cService.supportsUcs2SmsSending();
	}
	
	public boolean isDisconnecting() {
		return disconnecting;
	}
	
//> OTHER METHODS
	
	protected void disconnecting() {
		disconnecting = true;
		if(this.supportsReceive())
			this.setUseForReceiving(false);
		setUseForSending(false);
		
		this.setStatus(SmsModemStatus.DISCONNECTING, null);
		
		disconnect(true);
		disconnecting = false;
	}

	/**
	 * Forces the phone to disconnect from the COM port and close all listeners.
	 */
	public synchronized void disconnect() {
		if (isConnected()) {
			// Actually disconnecting the phone!
			new Thread("Disconnecting [" + this.getName() + "]") {
				public void run() {
					disconnecting();
				}
			}.start();
		} else {
			disconnect(true);
		}
	}

	/**
	 * Forces the phone to disconnect from the COM port and close all listeners.
	 * @param setStatus set <code>true</code> if status should be updated.  This will likely only be set <code>false</code> when doing phone detection. 
	 */
	private void disconnect(boolean setStatus) {
		LOG.trace("ENTER");
		try {
			cService.disconnect();
		} catch(Throwable t) {
			// If anything goes wrong in the disconnect, we want to make
			// sure we still kill the serialDriver.  Any throwables can
			// be ignored.
			LOG.debug("Error disconnecting", t);
			try { 
				cService.serialDriver.close(); 
			} catch(Throwable th) {
				LOG.debug("Error disconnecting", th);
			}
		}
		timeOfLastResponseFromPhone = 0;
		disconnected = true;
		smsLibConnected = false;
		if(setStatus && !isDuplicate()) {
			this.setStatus(SmsModemStatus.DISCONNECTED, null);
		}
		
		for (Message m : outbox) {
			m.setStatus(Message.STATUS_FAILED);
			if (smsListener != null) smsListener.outgoingMessageEvent(this, m);
		}
		LOG.trace("EXIT");
	}

	/**
	 * Sends directly the informed message list.
	 * 
	 * @param smsMessages
	 * @return true if there was no problem sending messages, false otherwise.
	 * @throws IOException 
	 */
	private void sendSmsListDirect(List<Message> smsMessages) throws IOException {
		LOG.trace("ENTER");

		try {
			cService.keepGsmLinkOpen();
			for (Message message : smsMessages) {
				LOG.debug("Sending [" + message.getTextContent() + "] to [" + message.getRecipientMsisdn() + "]");
				COutgoingMessage cMessage;

				// If it's a binary message, we set the encoding to send it.
				if (message.isBinaryMessage()) {
					cMessage = new COutgoingMessage(message.getRecipientMsisdn(), message.getBinaryContent());
					cMessage.setDestinationPort(message.getRecipientSmsPort());
				} else {
					cMessage = new COutgoingMessage(message.getRecipientMsisdn(), message.getTextContent());
				}

				// Do we require a Delivery Status Report?
				cMessage.setStatusReport(this.useDeliveryReports);

				// Ok, finished with the message parameters, now send it!
				try {
					cService.sendMessage(cMessage);
					if (cMessage.getRefNo() != -1) {
						message.setDispatchDate(cMessage.getDispatchDate());
						message.setSmscReference(cMessage.getRefNo());
						message.setStatus(Message.STATUS_SENT);
						LOG.debug("Message [" + message + "] was sent!");
					} else {
						//message not sent
						//failed to send
						message.setStatus(Message.STATUS_FAILED);
						LOG.debug("Message [" + message + "] was not sent!");
					}
				} catch(Exception ex) {
					message.setStatus(Message.STATUS_FAILED);
					LOG.debug("Failed to send message [" + message + "]", ex);
					LOG.info("Failed to send message");
				} finally {
					if (smsListener != null) {
						smsListener.outgoingMessageEvent(this, message);
					}
				}
			}
		} finally {
			for (Message m : smsMessages) {
				if (m.getStatus() == Message.STATUS_PENDING) {
					outbox.add(m);
				}
			}
		}
		LOG.trace("EXIT");
	}
	
//> STATIC HELPER METHODS
	/**
	 * Loads a translation map from a file of the following format: 
	 *	Split the line.  It should be of the following format:
	 *	<officialName><whiteSpace><alternateName1>,<alternateName2>,...,<alternateNameN>
	 * TODO this kind of thing should probably be done by the manager, or even at the UI layer.
	 * @param fileName name of the file to load the aliases from
	 * @return
	 */
	private static final HashMap<String, String> initAliasesFromFile(String filename) {
		String[] fileContents = ResourceUtils.getUsefulLines(filename);
		
		// map from alternate names to offical names.
		HashMap<String, String> map = new HashMap<String, String>();
		for(String line : fileContents) {
			// Split the line.  It should be of the following format:
			// 	<officialName>		<alternateName1>,<alternateName2>,...,<alternateNameN>
			String[] words = line.split("\\s", 2);
			String officialName = words[0];
			map.put(officialName.toLowerCase(), officialName);
			if (words.length > 1) {
				words = words[1].split(",");
				for (String word : words) {
					map.put(word.trim().toLowerCase(), officialName);
				}
			}
		}
		
		return map;
	}

	/**
	 * Attempts to get a mapping from a particular make and model to a CATHandler
	 * 
	 * @param manufacturer
	 * @param model 
	 * @return
	 */
	private static final String translateCATHandlerModel(String manufacturer, String model) {
		String lookupString = manufacturer.toLowerCase() + "_" + model.toLowerCase();
		String catHandler = cathandlerAliases.get(lookupString);
		return catHandler;
	}

	/**
	 * Translates the manufacture to a user-friendly string.
	 * 
	 * @param manufacturer
	 * @return
	 */
	private static final String translateManufacturer(String manufacturer) {
		manufacturer = manufacturer.trim();
		String alias = manufacturerAliases.get(manufacturer.toLowerCase());
		if(alias == null) return manufacturer;
		else return alias;
	}

	/**
	 * Translates the model to a user-friendly string.
	 * 
	 * @param model
	 * @return
	 */
	private static final String translateModel(String manufacturer, String model) {
		model = model.trim();
		model = model.replace("\\s", "");
		model = model.replace(manufacturer, "");
		String alias = modelAliases.get(model.toLowerCase());
		if (alias == null) return model;
		else return alias;
	}
	
	/**
	 * TODO an emulator should surely be a different type of device???
	 * @param number the phone number of the emulator
	 * @return a new "emulator"
	 */
	public static SmsModem createEmulator(String number) {
		SmsModem emulator = new SmsModem(null, null);
		emulator.setMsisdn(number);
		return emulator;
	}
}
