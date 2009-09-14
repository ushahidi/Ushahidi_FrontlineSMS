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

import org.apache.log4j.Logger;
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

	abstract protected boolean waitingForPin() throws IOException;

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

	abstract protected void switchToCmdMode() throws IOException;

	abstract protected boolean keepGsmLinkOpen() throws IOException;

	abstract protected int sendMessage(int size, String pdu, String phone, String text) throws IOException, NoResponseException, UnrecognizedHandlerProtocolException;

	abstract protected String listMessages(int messageClass) throws IOException, UnrecognizedHandlerProtocolException;

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
	
	@SuppressWarnings("unchecked")
	static AbstractATHandler load(CSerialDriver serialDriver, Logger log, CService srv, String gsmDeviceManufacturer, String gsmDeviceModel, String catHandlerAlias) {
		log.trace("ENTRY");
		String BASE_HANDLER = org.smslib.handler.CATHandler.class.getName();
		String[] handlerClassNames = { null, null, null, BASE_HANDLER };

		StringBuffer handlerClassName = new StringBuffer(BASE_HANDLER);
		
		if (gsmDeviceManufacturer != null && !gsmDeviceManufacturer.equals("")) {
			if (catHandlerAlias != null && !catHandlerAlias.equals("")) {
				handlerClassNames[0] = handlerClassName.toString() + "_" + catHandlerAlias;
			}
			handlerClassName.append("_").append(gsmDeviceManufacturer);
			handlerClassNames[2] = handlerClassName.toString();
			if (gsmDeviceModel != null && !gsmDeviceModel.equals("")) {
				handlerClassName.append("_").append(gsmDeviceModel);
				handlerClassNames[1] = handlerClassName.toString();
			}
		}

		AbstractATHandler atHandler = null;
		for (int i = 0; i < handlerClassNames.length; ++i) {
			try {
				if (handlerClassNames[i] != null) {
					Class<AbstractATHandler> handlerClass = (Class<AbstractATHandler>)Class.forName(handlerClassNames[i]);

					java.lang.reflect.Constructor<AbstractATHandler> handlerConstructor = handlerClass.getConstructor(new Class[]
					                                                                                         { CSerialDriver.class, Logger.class, CService.class });
					atHandler = handlerConstructor.newInstance(new Object[]{serialDriver, log, srv});
					break;
				}
			} catch (Exception ex) {
				if (i == handlerClassNames.length - 1) throw new RuntimeException("Class AbstractATHandler: Cannot initialize handler '" + handlerClassNames[i] + "'!");
			}
		}
		log.info("Loaded AT Handler: " + atHandler.getClass());
		log.trace("EXIT");
		return atHandler;
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
		CATHandler_Wavecom_M1306B.class,
		CATHandler_Wavecom.class,
	};

	@SuppressWarnings("unchecked")
	/**
	 * Gets a list containing all available AT Handlers.
	 */
	public static <T extends AbstractATHandler> Class<T>[] getHandlers() throws IOException {
		return HANDLERS;
	}
	
	
	
	protected int getProtocol() {
		return CService.Protocol.PDU;
	}
}
