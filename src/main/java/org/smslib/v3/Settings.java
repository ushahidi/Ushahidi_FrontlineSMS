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

/**
 * Configuration/settings class. This class holds information about all the
 * parameters which affect SMSLib operation.
 */
public class Settings
{
	/**
	 * Specifies whether debug is enabled.
	 */
	public boolean DEBUG = false;

	/**
	 * Specifies whether queue traffic should be included in the debug
	 * logging.
	 */
	public boolean DEBUG_QUEUE = false;

	/**
	 * Specifies whether the serial port flashing is disabled or enabled.
	 */
	public boolean SERIAL_NOFLUSH = false;

	/**
	 * Specifies whether the serial port will be polled or SMSLib will be
	 * notified by port interrupts.
	 */
	public boolean SERIAL_POLLING = false;

	/**
	 * Specifies the polling interval (milliseconds).
	 */
	public int SERIAL_POLLING_INTERVAL = 200;

	/**
	 * Specifies the serial ports' timeout (milliseconds).
	 */
	public int SERIAL_TIMEOUT = 30000;

	/**
	 * Specifies the serial ports' keep-alive interval (milliseconds).
	 */
	public int SERIAL_KEEPALIVE_INTERVAL = 60000;

	/**
	 * Specifies the buffer size (bytes).
	 */
	public int SERIAL_BUFFER_SIZE = 16384;

	/**
	 * Wait time before clearing the queues (milliseconds).
	 */
	public int SERIAL_CLEAR_WAIT = 1000;

	/**
	 * Specifies whether hardware handshake should be enabled
	 * for outbound port traffic.
	 */
	public boolean SERIAL_RTSCTS_OUT = false;

	/**
	 * Specifies the interval for the awakening of the background sending queue (milliseconds).
	 */
	public int QUEUE_INTERVAL = 1000;

	/**
	 * Specifies the number of retries the background queue should give to an outbound message befrore it classifies it as failed.
	 */
	public int QUEUE_RETRIES = 3;

	/**
	 * Wait time for generic AT commands (milliseconds).
	 */
	public int AT_WAIT = 200;

	/**
	 * Wait time after issuing a RESET command (milliseconds).
	 */
	public int AT_WAIT_AFTER_RESET = 10000;

	/**
	 * Wait time to give the modem after entring COMMAND mode (milliseconds).
	 */
	public int AT_WAIT_CMD = 1100;

	/**
	 * Wait time after issuing a SEND command (milliseconds).
	 */
	public int AT_WAIT_CGMS = 200;

	/**
	 * Wait time before retrying the network status command (milliseconds).
	 */
	public int AT_WAIT_NETWORK = 5000;

	/**
	 * Wait time after entering the PIN (milliseconds).
	 */
	public int AT_WAIT_SIMPIN = 5000;

	/**
	 * Wait time before retrying a failed CNMI command (milliseconds).
	 */
	public int AT_WAIT_CNMI = 3000;

	/**
	 * Number of retries for sending a message.
	 */
	public int OUTBOUND_RETRIES = 3;

	/**
	 * Wait time between retries for sending a message (milliseconds).
	 */
	public int OUTBOUND_RETRY_WAIT = 3000;

	/**
	 * Watchdog - SMSLib background monitoring thread interval (milliseconds).
	 */
	public int WATCHDOG_INTERVAL = 15000;

	/**
	 * Mask or show the IMSI string. 
	 */
	public boolean MASK_IMSI = true;

	public boolean DISABLE_CMTI = false;

	Settings()
	{
		if (System.getProperty("smslib.debug") != null) DEBUG = true;
		if (System.getProperty("smslib.debug_queue") != null)
		{
			DEBUG = true;
			DEBUG_QUEUE = true;
		}
		if (System.getProperty("smslib.serial.noflush") != null) SERIAL_NOFLUSH = true;
		if (System.getProperty("smslib.serial.polling") != null) SERIAL_POLLING = true;
		if (System.getProperty("smslib.serial.pollinginterval") != null) SERIAL_POLLING_INTERVAL = Integer.parseInt(System.getProperty("smslib.serial.pollinginterval"));
		if (System.getProperty("smslib.serial.timeout") != null) SERIAL_TIMEOUT = Integer.parseInt(System.getProperty("smslib.serial.timeout"));
		if (System.getProperty("smslib.serial.keepalive") != null) SERIAL_KEEPALIVE_INTERVAL = Integer.parseInt(System.getProperty("smslib.serial.keepalive"));
		if (System.getProperty("smslib.serial.buffer") != null) SERIAL_BUFFER_SIZE = Integer.parseInt(System.getProperty("smslib.serial.buffer"));
		if (System.getProperty("smslib.serial.clearwait") != null) SERIAL_CLEAR_WAIT = Integer.parseInt(System.getProperty("smslib.serial.clearwait"));
		if (System.getProperty("smslib.queue.interval") != null) QUEUE_INTERVAL = Integer.parseInt(System.getProperty("smslib.queue.interval"));
		if (System.getProperty("smslib.queue.retries") != null) QUEUE_RETRIES = Integer.parseInt(System.getProperty("smslib.queue.retries"));
		if (System.getProperty("smslib.outbound.retries") != null) OUTBOUND_RETRIES = Integer.parseInt(System.getProperty("smslib.outbound.retries"));
		if (System.getProperty("smslib.outbound.retrywait") != null) OUTBOUND_RETRY_WAIT = Integer.parseInt(System.getProperty("smslib.outbound.retrywait"));
		if (System.getProperty("smslib.at.wait") != null) AT_WAIT = Integer.parseInt(System.getProperty("smslib.at.wait"));
		if (System.getProperty("smslib.at.resetwait") != null) AT_WAIT_AFTER_RESET = Integer.parseInt(System.getProperty("smslib.at.resetwait"));
		if (System.getProperty("smslib.at.cmdwait") != null) AT_WAIT_CMD = Integer.parseInt(System.getProperty("smslib.at.cmdwait"));
		if (System.getProperty("smslib.at.cmgswait") != null) AT_WAIT_CGMS = Integer.parseInt(System.getProperty("smslib.at.cmgswait"));
		if (System.getProperty("smslib.at.networkwait") != null) AT_WAIT_NETWORK = Integer.parseInt(System.getProperty("smslib.at.networkwait"));
		if (System.getProperty("smslib.at.simpinwait") != null) AT_WAIT_SIMPIN = Integer.parseInt(System.getProperty("smslib.at.simpinwait"));
		if (System.getProperty("smslib.at.cnmiwait") != null) AT_WAIT_CNMI = Integer.parseInt(System.getProperty("smslib.at.cnmiwait"));
		if (System.getProperty("smslib.watchdog") != null) WATCHDOG_INTERVAL = Integer.parseInt(System.getProperty("smslib.watchdog"));
		
		// Temporary Switch - disable inbound message unsolicited notifications (CMTI).
		if (System.getProperty("smslib.nocmti") != null) DISABLE_CMTI = true;
	}
}
