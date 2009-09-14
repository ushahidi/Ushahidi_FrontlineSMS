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

import net.frontlinesms.data.domain.Message;
import net.frontlinesms.listener.SmsListener;

public interface SmsDevice {
	public static final int STATUS_DORMANT = 0;
	public static final int STATUS_SEARCHING = 1;
	public static final int STATUS_DETECTED = 2;
	public static final int STATUS_CONNECTED = 3;
	public static final int STATUS_DISCONNECTED = 4;
	public static final int STATUS_SIM_REFUSED = 5;
	public static final int STATUS_DUPLICATE = 6;
	public static final int STATUS_MAX_SPEED_FOUND = 7;
	public static final int STATUS_TRY_TO_CONNECT = 8;
	public static final int STATUS_NO_PHONE_DETECTED = 9;
	public static final int STATUS_CONNECTING = 10;
	public static final int STATUS_FAILED_TO_CONNECT = 11;
	public static final int STATUS_LOW_CREDIT = 12;
	public static final int STATUS_DISCONNECTING = 13;
	public static final int STATUS_RECEIVING_FAILED = 14;
	public static final int STATUS_TRYING_TO_RECONNECT = 15;
	
	/** Checks if this device is being used to send SMS messages. */
	public boolean isUseForSending();

	/** Set this device to be used for sending SMS messages. */
	public void setUseForSending(boolean use);
	
	/** Check whether this device actually supports SMS receipt. */
	public boolean supportsReceive();

	/** Check if this device is being used to receive SMS messages. */
	public boolean isUseForReceiving();
	
	/** Set this device to be used for receiving messages. */
	public void setUseForReceiving(boolean use);

	/** Adds the supplied message to the outbox. */
	public void sendSMS(Message outgoingMessage);
	
	/** Gets a String describing the status of this device.  TODO shouldn't this be i18n'd somehow? */
	public String getStatusString();

	/** Sets the {@link SmsListener} attached to this {@link SmsDevice}. */
	public void setSmsListener(SmsListener smsListener);
	
	/** Check whether this device is currently connected */
	public boolean isConnected();
	
	/** Check whether this device actually supports sending binary sms. */
	public boolean isBinarySendingSupported();

	/** Gets the MSISDN to be displayed for this device. */
	public String getMsisdn();
	
	/**
	 * Checks whether this device supports sending sms messages in the UCS2 characterset.
	 * FIXME this method is unnecessary as all handsets support UCS-2 so far!
	 */
	public boolean isUcs2SendingSupported();
}
