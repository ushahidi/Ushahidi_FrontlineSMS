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

import static net.frontlinesms.data.domain.SmsInternetServiceSettings.getValueFromString;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.listener.SmsListener;
import net.frontlinesms.smsdevice.properties.OptionalRadioSection;
import net.frontlinesms.smsdevice.properties.OptionalSection;

import org.apache.log4j.Logger;

/**
 * Abstract class containing all default information needed by 
 * Sms Internet Services (e.g Clickatell, IntelliSMS, etc).
 * 
 * @author Carlos Eduardo Endler Genz
 * @date 26/01/2009
 */
public abstract class AbstractSmsInternetService implements SmsInternetService {
	private static Logger LOG = Utils.getLogger(AbstractSmsInternetService.class);
	
	private SmsInternetServiceThread thread;
	
	protected static final String PROPERTY_USE_FOR_SENDING = "common.use.for.sending";
	protected static final String PROPERTY_USE_FOR_RECEIVING = "common.use.for.receiving";
	
	protected ConcurrentLinkedQueue<Message> outbox = new ConcurrentLinkedQueue<Message>();
	/** The SmsListener to which this phone handler should report SMS Message events. */
	protected SmsListener smsListener;
	/** Settings for this service */
	protected SmsInternetServiceSettings settings;
	// TODO status string should not be stored inside this object.  Should get a status code from this object that the UI layer interprets.
	protected String statusString;
	
	protected Map<String, String> persistedProperties;
	

	/** 
	 * Adds the supplied message to the outbox. 
	 */
	public void sendSMS(Message outgoingMessage) {
		LOG.trace("ENTER");
		outgoingMessage.setStatus(Message.STATUS_PENDING);
		outgoingMessage.setSenderMsisdn(getMsisdn());
		
		outbox.add(outgoingMessage);
		if (smsListener != null) {
			smsListener.outgoingMessageEvent(this, outgoingMessage);
		}
		LOG.debug("Message added to outbox. Size is [" + outbox.size() + "]");
		LOG.trace("EXIT");
	}

	// ABSTRACT METHODS
	/**
	 * Send an SMS message using this phone handler.
	 * @param message The message to be sent.
	 */
	protected abstract void sendSmsDirect(Message message);
	
	/**
	 * Attempt to receive SMS messages from this service.
	 */
	protected abstract void receiveSms() throws SmsInternetServiceReceiveException;
	
	/**
	 * @return The property value, either the one stored on db (if any)
	 * or the default value.
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Object> T getPropertyValue(String key, Class<T> clazz) {
		T defaultValue = (T) getValue(key, getPropertiesStructure());
		if (defaultValue == null) throw new IllegalArgumentException("No default value could be found for key: " + key);
		
		if (persistedProperties.containsKey(key)) {
			return (T) getValueFromString(defaultValue, persistedProperties.get(key));
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * Deep-searches nested maps for a propertt's value.  Maps may be nested as values
	 * inside other maps by wrapping them in either an {@link OptionalSection} or an
	 * {@link OptionalRadioSection}.
	 * @param key
	 * @param map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static Object getValue(String key, Map<String, Object> map) {
		if (map == null) {
			// TODO when would map be null?  perhaps we should just be clear that the result is undefined when this is the case?
			return null;
		} else if (map.containsKey(key)) {
			return map.get(key);
		} else {
			for(Object mapValue : map.values()) {
				if(mapValue instanceof OptionalSection) {
					Object value = getValue(key, ((OptionalSection)mapValue).getDependencies());
					if(value != null) return value;
				} else if(mapValue instanceof OptionalRadioSection) {
					Collection<LinkedHashMap<String, Object>> dependencies = ((OptionalRadioSection)mapValue).getAllDependencies();
					for(LinkedHashMap<String, Object> dependencyMap : dependencies) {
						Object value = getValue(key, dependencyMap);
						if(value != null) return value;
					}
				}
			}
		}
		return null;
	}
	
	// TODO javadoc please
	// TODO i think this should be renamed.  presumably this is an event notification rather than trigger
	protected void fireEvent(int code) {
		if (smsListener != null) {
			smsListener.smsDeviceEvent(this, code);
		}
	}
	
	// TODO JAVADOC PLEASE
	// TODO i think this should be renamed.  presumably this is an event notification rather than trigger
	protected void fireEvent() {
		// FIXME what is this magic value -1, and where does it come from
		fireEvent(-1);
	}
	
	/** Stop this service from running */
	public void stopRunning() {
		LOG.trace("ENTER");
		statusString = FrontlineSMSConstants.Dependants.STATUS_CODE_MESSAGES[STATUS_DISCONNECTED] + ".";
		fireEvent();
		this.thread.running = false;
		LOG.trace("EXIT");
	}
	
	/** 
	 * Gets a String describing the status of this device.  TODO shouldn't this be i18n'd somehow? 
	 */
	public String getStatusString() {
		return statusString;
	}
	
	/** 
	 * Sets the {@link SmsListener} attached to this {@link SmsDevice}. 
	 */
	public void setSmsListener(SmsListener smsListener) {
		this.smsListener = smsListener;
	}
	
	/**
	 * Initialize the service using the supplied properties.
	 * TODO If this service is currently running, we should handle that, possibly by restarting it,
	 * or throwing an exception to notify the user.
	 * @param newProperties
	 */
	public void init(SmsInternetServiceSettings settings) {
		this.settings = settings;
		// Load the persisted properties into memory.
		persistedProperties = settings.getProperties();
	}
	
	public void reconnect() {
		// Re-load the persisted properties into memory.
		persistedProperties = settings.getProperties();
	}
	
	/**
	 * @return This internet service outbox.
	 */
	public ConcurrentLinkedQueue<Message> getOutbox() {
		return outbox;
	}
	
	/**
	 * Gets the settings attached to this {@link SmsInternetService} instance.
	 * @return
	 */
	public SmsInternetServiceSettings getSettings() {
		return settings;
	}

	/** Starts this service. */
	public synchronized void startThisThing() {
		try {
			statusString = FrontlineSMSConstants.Dependants.STATUS_CODE_MESSAGES[STATUS_CONNECTING] + "...";
			fireEvent();
			init();
			SmsInternetServiceThread newThread = new SmsInternetServiceThread(this);
			this.thread = newThread;
			newThread.start();
		} catch(SmsInternetServiceInitialisationException ex) {
			LOG.error("There was an error starting SMS Internet Service.", ex);
			this.stopThisThing();
		}
	}
	
	/** Re-connects this service. */
	public void restartThisThing() {
		this.stopThisThing();
		this.startThisThing();
	}
	
	/** Stop this service from running */
	public void stopThisThing() {
		deinit();
		if(this.thread != null) this.thread.running = false;
	}
	
	/**
	 * Initialize all settings related to this service's {@link SmsInternetServiceThread}
	 * @throws SmsInternetServiceInitialisationException
	 */
	protected abstract void init() throws SmsInternetServiceInitialisationException;

	/**
	 * Deinitialize all settings related to this service's {@link SmsInternetServiceThread}
	 * @throws SmsInternetServiceInitialisationException
	 */
	protected abstract void deinit();
	
	private class SmsInternetServiceThread extends Thread {
		/** Indicates whether this {@link SmsInternetServiceThread} is running. */ 
		protected boolean running;
		
		SmsInternetServiceThread(AbstractSmsInternetService owner) {
			super(owner.getClass().getSimpleName() + " :: " + owner.getIdentifier());
		}
		
		/**
		 * Sends and receives SMS messages.
		 */
		public void run() {
			LOG.trace("ENTER");
			running = true;
			while (running) {
				boolean sleep = true;
				if (isConnected() && isUseForSending()) {
					Message m = outbox.poll();
					if (m != null) {
						LOG.debug("Sending message [" + m.toString() + "]");
						long startTime = System.currentTimeMillis();
						sendSmsDirect(m);
						LOG.debug("Send messages took [" + (System.currentTimeMillis() - startTime) + "]");
						sleep = false;
					}
				}
				if (running && isConnected() && isUseForReceiving()) {
					LOG.debug("Receiving messages...");
					try {
						long startTime = System.currentTimeMillis();
						receiveSms();
						LOG.debug("Receiving messages took " + (System.currentTimeMillis() - startTime) + "ms");
					} catch (SmsInternetServiceReceiveException e) {
						LOG.error("Failed to receive messages.", e);
						fireEvent(STATUS_RECEIVING_FAILED);
					}
				}
				// TODO verify delivery reports?
				// If this thread is still running, we should have a little snooze
				if (running) {
					if (sleep) Utils.sleep_ignoreInterrupts(5000); /* 5 seconds */
					else Utils.sleep_ignoreInterrupts(100); /* 0.1 seconds */
				}
			}
			LOG.trace("EXIT");
		}
	}
}
