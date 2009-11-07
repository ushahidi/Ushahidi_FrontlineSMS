// SMSLib for Java
// An open-source API Library for sending and receiving SMS via a GSM modem.
// Copyright (C) 2002-2007, Thanasis Delenikas, Athens/GREECE
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.smslib.CService.MessageClass;
import org.smslib.handler.*;

abstract public class AbstractATHandler {
	protected CSerialDriver serialDriver;

	protected Logger log;

	protected String storageLocations = "";

	protected CService srv;

	protected static final int DELAY_AT = 200;

	protected static final int DELAY_RESET = 20000;
	
	protected static final int DELAY_PIN = 12000;

	protected static final int DELAY_CMD_MODE = 1000;

	protected static final int DELAY_CMGS = 300;

	public AbstractATHandler(CSerialDriver serialDriver, Logger log, CService srv) {
		super();
		this.serialDriver = serialDriver;
		this.log = log;
		this.srv = srv;
		storageLocations = "";
	}

	abstract protected void setStorageLocations(String loc);

	abstract protected boolean dataAvailable() throws IOException;

	abstract protected void sync() throws IOException;

	abstract protected void reset() throws IOException;

	abstract protected void echoOff() throws IOException;

	abstract protected void init() throws IOException;

	abstract protected boolean isAlive() throws IOException;

	/**
	 * Issues the AT Command to check if the device is waiting for a PIN or PUK
	 * @return The response from the AT Handler, verbatim
	 * @throws IOException
	 */
	protected abstract String getPinResponse() throws IOException;

	/**
	 * Check the supplied response to the PIN AT command to see if a PIN is required.
	 * @param commandResponse
	 * @return <code>true</code> if a PIN is being waited for; <code>false</code> otherwise
	 */
	protected abstract boolean isWaitingForPin(String commandResponse);

	/**
	 * Check the supplied response to the PIN AT command to see if a PUK is required.
	 * @param commandResponse
	 * @return <code>true</code> if a PIN is being waited for; <code>false</code> otherwise
	 */	
	protected abstract boolean isWaitingForPuk(String commandResponse);

	abstract protected boolean enterPin(String pin) throws IOException;

	abstract protected boolean setVerboseErrors() throws IOException;

	abstract protected boolean setPduMode() throws IOException;

	abstract protected boolean setTextMode() throws IOException;

	abstract protected boolean enableIndications() throws IOException;

	abstract protected boolean disableIndications() throws IOException;

	abstract protected String getManufacturer() throws IOException;

	abstract protected String getModel() throws IOException;
	
	abstract protected String getMsisdn() throws IOException;

	abstract protected String getSerialNo() throws IOException;

	abstract protected String getImsi() throws IOException;

	abstract protected String getSwVersion() throws IOException;

	abstract protected String getBatteryLevel() throws IOException;

	abstract protected String getSignalLevel() throws IOException;

	abstract protected boolean setMemoryLocation(String mem) throws IOException;

	/**
	 * Switches the serial communication mode from data mode to command mode.  Command
	 * mode allows the sending of AT commands.
	 * @throws IOException if there was a problem with the serial connection
	 */
	abstract protected void switchToCmdMode() throws IOException;

	abstract protected boolean keepGsmLinkOpen() throws IOException;

	abstract protected int sendMessage(int size, String pdu, String phone, String text) throws IOException, NoResponseException, UnrecognizedHandlerProtocolException;

	abstract protected String listMessages(MessageClass messageClass) throws IOException, UnrecognizedHandlerProtocolException, SMSLibDeviceException;

	abstract protected boolean deleteMessage(int memIndex, String memLocation) throws IOException;

	abstract protected String getGprsStatus() throws IOException;

	abstract protected String getNetworkRegistration() throws IOException;

	abstract protected void getStorageLocations() throws IOException;

	/**
	 * Checks whether this AT Handler has support for receiving SMS messages
	 * @return true if this AT handler supports receiving of SMS messages.
	 */
	abstract protected boolean supportsReceive();

	/**
	 * Checks whether this AT Handler has support for sending SMS binary messages
	 * @return true if this AT handler supports sending of SMS binary message.
	 */
	protected boolean supportsBinarySmsSending() {
		return true;
	}

	/**
	 * Checks whether this AT Handler has support for sending UCS-2 encoded text messages.
	 * @return true if this AT handler supports sending UCS-2 encoded text messages.
	 */
	public abstract boolean supportsUcs2SmsSending();
	
	/**
	 * Attempt to load a particular AT Handler.
	 * @param serialDriver
	 * @param log
	 * @param srv
	 * @param handlerClassName
	 * @return A new instance of the required handler.
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	private static AbstractATHandler load(CSerialDriver serialDriver, Logger log, CService srv, String handlerClassName) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException {
		log.info("Attempting to load handler: " + handlerClassName);
		
		Class<AbstractATHandler> handlerClass = (Class<AbstractATHandler>) Class.forName(handlerClassName);

		java.lang.reflect.Constructor<AbstractATHandler> handlerConstructor = handlerClass.getConstructor(new Class[] { CSerialDriver.class, Logger.class, CService.class });
		AbstractATHandler atHandlerInstance = handlerConstructor.newInstance(new Object[]{serialDriver, log, srv});
		
		log.info("Successfully loaded handler: " + atHandlerInstance.getClass().getName());
		
		return atHandlerInstance;
	}
	
	/**
	 * 
	 * @param serialDriver
	 * @param log
	 * @param srv
	 * @param gsmDeviceManufacturer
	 * @param gsmDeviceModel
	 * @param catHandlerAlias
	 * @return
	 */
	static AbstractATHandler load(CSerialDriver serialDriver, Logger log, CService srv, String gsmDeviceManufacturer, String gsmDeviceModel, String catHandlerAlias) {
		log.trace("ENTRY");
		final String BASE_HANDLER = org.smslib.handler.CATHandler.class.getName();

		if (catHandlerAlias != null && !catHandlerAlias.equals("")) {
			// suggested cat handler from method param
			String requestedHandlerName = BASE_HANDLER + "_" + catHandlerAlias;
			try {
				return load(serialDriver, log, srv, requestedHandlerName);
			} catch(Exception ex) {
				log.info("Could not load requested handler '" + requestedHandlerName + "'; will try more generic version.", ex);
			}
		}

		if (gsmDeviceManufacturer != null && !gsmDeviceManufacturer.equals("")) {
			String manufacturerHandlerName = BASE_HANDLER + "_" + gsmDeviceManufacturer;
			
			if (gsmDeviceModel != null && !gsmDeviceModel.equals("")) {
				String modelHandlerName = manufacturerHandlerName + "_" + gsmDeviceModel;
				try {
					return load(serialDriver, log, srv, modelHandlerName);
				} catch(Exception ex) {
					log.info("Could not load requested handler '" + modelHandlerName + "'; will try more generic version.", ex);
				}
			}

			try {
				return load(serialDriver, log, srv, manufacturerHandlerName);
			} catch(Exception ex) {
				log.info("Could not load requested handler '" + manufacturerHandlerName + "'; will try more generic version.", ex);
			}
		}
		
		return new CATHandler(serialDriver, log, srv);
	}
	
	/** List of all AT handler classes */
	@SuppressWarnings("unchecked")
	private static final Class[] HANDLERS = {
		CATHandler.class,
		CATHandler_Motorola_RAZRV3x.class,
		CATHandler_Nokia_S40_3ed.class,
		CATHandler_Samsung.class,
		CATHandler_Siemens_M55.class,
		CATHandler_Siemens_MC75.class,
		CATHandler_Siemens_S55.class,
		CATHandler_Siemens_TC35.class,
		CATHandler_SonyEricsson_GT48.class,
		CATHandler_SonyEricsson_W550i.class,
		CATHandler_SonyEricsson.class,
		CATHandler_Symbian_PiAccess.class,
		CATHandler_Wavecom_M1306B.class,
		CATHandler_Wavecom.class,
	};

	/**
	 * Gets a list containing all available AT Handlers.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends AbstractATHandler> Class<T>[] getHandlers() {
		return HANDLERS;
	}
	
	
	
	protected int getProtocol() {
		return CService.Protocol.PDU;
	}
}
