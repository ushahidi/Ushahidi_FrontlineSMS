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

import java.util.LinkedHashMap;

import net.frontlinesms.*;
import net.frontlinesms.data.domain.Message;
import net.frontlinesms.smsdevice.properties.*;

import org.apache.log4j.Logger;
import org.smslib.ReceiveNotSupportedException;
import org.smslib.v3.*;
import org.smslib.v3.http.ClickatellHTTPGateway;

/**
 * Implements the Clickatell Internet service.
 * 
 * @author Carlos Eduardo Endler Genz
 * @date 26/01/2009
 */
@Provider(name = "Clickatell", icon = "/icons/sms_http.png")
public class ClickatellInternetService extends AbstractSmsInternetService {
	/** Prefix attached to every property name. */
	private static final String PROPERTY_PREFIX = "smsdevice.internet.clickatell.";

	protected static final String PROPERTY_USERNAME = PROPERTY_PREFIX + "username";
	protected static final String PROPERTY_PASSWORD = PROPERTY_PREFIX + "password";
	protected static final String PROPERTY_API = PROPERTY_PREFIX + "api";
	protected static final String PROPERTY_FROM_MSISDN = PROPERTY_PREFIX + "from.msisdn";
	protected static final String PROPERTY_SSL = PROPERTY_PREFIX + "ssl";

	private static Logger LOG = Utils.getLogger(ClickatellInternetService.class);
	private ClickatellHTTPGateway gateway;
	private boolean connected;
	private Service service;

	/**
	 * Initialises the clickatell http gateway.
	 */
	private void initGateway() {
		gateway = new ClickatellHTTPGateway(getIdentifier(), getApiId(), getUsername(), getPassword());
		gateway.setOutbound(true);
		gateway.setSecure(isEncrypted());

		service = new Service();
		service.addGateway(gateway);
	}

	/**
	 * Verify if we this services has received messages.
	 * @return the number of read messages.
	 */
	protected void receiveSms() {
		throw new ReceiveNotSupportedException();
	}

	/**
	 * Send an SMS message using this phone handler.
	 * @param message The message to be sent.
	 */
	protected void sendSmsDirect(Message message) {
		LOG.trace("ENTER");
		LOG.debug(Library.getLibraryDescription());
		LOG.debug("Version: " + Library.getLibraryVersion());

		LOG.debug("Sending [" + message.getTextContent() + "] to [" + message.getRecipientMsisdn() + "]");
		OutboundMessage oMessage;

		// FIXME if we are sending a binary message, we should create one of those here instead
		if(message.isBinaryMessage()) {
			oMessage = new OutboundMessage(message.getRecipientMsisdn(), message.getBinaryContent());
		} else {
			oMessage = new OutboundMessage(message.getRecipientMsisdn(), message.getTextContent());
		}
		if(message.getRecipientSmsPort() != -1) {
			oMessage.setDstPort(message.getRecipientSmsPort());
		}
		
		String fromMsisdn = getMsisdn();
		if (fromMsisdn != null && !fromMsisdn.equals("")) {
			oMessage.setFrom(fromMsisdn);
		}
		try {
			service.sendMessage(oMessage);
			if (oMessage.getMessageStatus() == MessageStatuses.SENT) {
				message.setDispatchDate(oMessage.getDispatchDate().getTime());
				message.setStatus(Message.STATUS_SENT);
				LOG.debug("Message [" + message + "] was sent!");
			} else {
				//message not sent
				//failed to send
				if (oMessage.getFailureCause() == FailureCauses.NO_CREDIT) {
					statusString = FrontlineSMSConstants.Dependants.STATUS_CODE_MESSAGES[STATUS_LOW_CREDIT].replace(FrontlineSMSConstants.ARG_VALUE, String.valueOf(gateway.queryBalance())) + ".";
					fireEvent();
					creditLow();
				}
				message.setStatus(Message.STATUS_FAILED);
				LOG.debug("Message [" + message + "] was not sent.  Cause: [" + oMessage.getFailureCause() + "]");
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

	/**
	 * Starts the service. Normally we initialise the gateway.
	 */
	protected void init() throws SmsInternetServiceInitialisationException {
		LOG.trace("ENTER");
		initGateway();
		try{
			service.startService();
			connected = true;
			statusString = FrontlineSMSConstants.Dependants.STATUS_CODE_MESSAGES[STATUS_CONNECTED] + ".";
			fireEvent(STATUS_CONNECTED);
		} catch (Throwable t) {
			LOG.debug("Could not connect", t);
			statusString = FrontlineSMSConstants.Dependants.STATUS_CODE_MESSAGES[STATUS_FAILED_TO_CONNECT] + ".";
			connected = false;
			fireEvent(STATUS_DISCONNECTED);
			throw new SmsInternetServiceInitialisationException(t);
		}
		LOG.trace("EXIT");
	}

	/**
	 * Stops the service showing the remaining credits
	 */
	public void creditLow() {
		stopThisThing();
	}

	/**
	 * Forces the service to stop all gateways.
	 */
	protected void deinit() {
		LOG.trace("ENTER");
		try {
			service.stopService();
		} catch(Throwable t) {
			// If anything goes wrong in the service stopping.
			LOG.debug("Error stopping service", t);
		}
		statusString = FrontlineSMSConstants.Dependants.STATUS_CODE_MESSAGES[STATUS_DISCONNECTED] + ".";
		connected = false;
		fireEvent(STATUS_DISCONNECTED);
		LOG.trace("EXIT");
	}

	/**
	 * We do not currently support receive through Clickatell.
	 * @return <code>false</code>
	 */
	public boolean supportsReceive() {
		return false;
	}

	/**
	 * Gets an identifier for this instance of {@link SmsInternetService}.  Usually this
	 * will be the username used to login with the provider, or a similar identifer on
	 * the service.
	 */
	public String getIdentifier() {
		return this.getUsername() + " (API " + this.getApiId() + ")";
	}

	/** 
	 * Get the default properties for this class.
	 */
	public LinkedHashMap<String, Object> getPropertiesStructure() {
		LinkedHashMap<String, Object> defaultSettings = new LinkedHashMap<String, Object>();
		defaultSettings.put(PROPERTY_USERNAME, "");
		defaultSettings.put(PROPERTY_PASSWORD, new PasswordString(""));
		defaultSettings.put(PROPERTY_API, "");
		defaultSettings.put(PROPERTY_FROM_MSISDN, new PhoneSection(""));
		defaultSettings.put(PROPERTY_SSL, Boolean.FALSE);
		defaultSettings.put(PROPERTY_USE_FOR_SENDING, Boolean.TRUE);
		return defaultSettings;
	}
	
	/** 
	 * Check if this service is encrypted using SSL. 
	 */
	public boolean isEncrypted() {
		return getPropertyValue(PROPERTY_SSL, Boolean.class);
	}

	/** 
	 * Set this device to be used for receiving messages. 
	 */
	public void setUseForReceiving(boolean use) {
		throw new ReceiveNotSupportedException();
	}

	/** 
	 * Set this device to be used for sending SMS messages. 
	 */
	public void setUseForSending(boolean use) {
		this.settings.set(PROPERTY_USE_FOR_SENDING, new Boolean(use));
		persistedProperties.put(PROPERTY_USE_FOR_SENDING, String.valueOf(use));
	}

	/**
	 * @return The property value of {@value #PROPERTY_USERNAME}
	 */
	private String getUsername() {
		return getPropertyValue(PROPERTY_USERNAME, String.class);
	}

	/**
	 * @return The property value of {@value #PROPERTY_PASSWORD}
	 */
	private String getPassword() {
		return getPropertyValue(PROPERTY_PASSWORD, PasswordString.class).getValue();
	}

	/**
	 * @return The property value of {@value #PROPERTY_API}
	 */
	private String getApiId() {
		return getPropertyValue(PROPERTY_API, String.class);
	}


	/**
	 * Check if this device is being used to receive SMS messages.
	 * Our implementation of Clickatell does not support SMS receiving.
	 */
	public boolean isUseForReceiving() {
		return false;
	}

	/** 
	 * Checks if this device is being used to send SMS messages. 
	 */
	public boolean isUseForSending() {
		return getPropertyValue(PROPERTY_USE_FOR_SENDING, Boolean.class);
	}

	/** 
	 * Gets the MSISDN that numbers sent from this service will appear to be from. 
	 */
	public String getMsisdn() {
		return getPropertyValue(PROPERTY_FROM_MSISDN, PhoneSection.class).getValue();
	}

	/**
	 * Checks if the service is currently connected.
	 * TODO could rename this isLive().
	 * @return
	 */
	public boolean isConnected() {
		verifyGatewayConnection();
		return this.connected;
	}

	/**
	 * Verifies if the gateway is running okay or we need to restart it.
	 */
	private void verifyGatewayConnection() {
		// Let's verify it is really connected
		if (gateway.getGatewayStatus() != GatewayStatuses.OK) {
			try {
				if (gateway.getStarted()) {
					statusString = FrontlineSMSConstants.Dependants.STATUS_CODE_MESSAGES[STATUS_TRYING_TO_RECONNECT];
					fireEvent(STATUS_TRYING_TO_RECONNECT);
					service.stopService();
					connected = false;
					service.startService();
					connected = true;
					statusString = FrontlineSMSConstants.Dependants.STATUS_CODE_MESSAGES[STATUS_CONNECTED] + ".";
					fireEvent(STATUS_CONNECTED);
				}
			} catch (Throwable t) {
				// We could not reconnect
				LOG.error("Error reconnecting", t);
				connected = false;
			}
		}
	}

	/** 
	 * Check whether this device actually supports sending binary sms. 
	 */
	public boolean isBinarySendingSupported() {
		return true;
	}

	/**
	 * TODO this needs testing.  If it works, this method can return true.  If it doesn't work,
	 * this comment can be replaced with a more descriptive one.
	 */
	public boolean isUcs2SendingSupported() {
		return false;
	}
}
