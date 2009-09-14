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

package org.smslib.handler;

import java.io.IOException;
import java.util.*;
import org.smslib.*;
import org.apache.log4j.*;

public class CATHandler extends AbstractATHandler {
	/**
	 * Exceptionally fine logging level used when troubleshooting low-level problems with AT devices.
	 * FIXME remove this and disable it
	 */
	private static final boolean TRACE = false;
	
	/** Character used to terminate a line after an AT command */
	protected static final String END_OF_LINE = "\r";
	/** AT Command for switching echo off */
	private static final String AT_ECHO_OFF = "ATE0";
	/** AT Command for retrieving the IMSI number of the connected device */
	private static final String AT_IMSI = "AT+CIMI";
	/** AT Command for retrieving the network registration status */
	private static final String AT_NETWORK_REGISTRATION = "AT+CREG?";
	/** AT Command for retrieving the MSISDN of the connected device */
	private static final String AT_GET_MSISDN = "CNUM";
	/** AT Command to retrieve the GPRS status */
	private static final String AT_GPRS_STATUS = "AT+CGATT?";
	/** AT Command to retrieve the battery level */
	private static final String AT_BATTERY = "AT+CBC";

	public CATHandler(CSerialDriver serialDriver, Logger log, CService srv) {
		super(serialDriver, log, srv);
	}

	protected void setStorageLocations(String loc) {
		storageLocations = loc;
	}

	protected boolean dataAvailable() throws IOException {
		return serialDriver.dataAvailable();
	}

	protected void sync() throws IOException {
		for(int i=0; i<4; ++i) {
			sleepWithoutInterruption(DELAY_AT);
			serialDriver.send("AT\r");
		}
		sleepWithoutInterruption(DELAY_AT);
	}

	protected void reset() throws IOException {}

	protected void echoOff() throws IOException {
		serialSendReceive(AT_ECHO_OFF);
	}

	protected void init() throws IOException {
		serialSendReceive("AT+CLIP=1");
		serialSendReceive("AT+COPS=0");
		// No need for the following, until full GPRS functionality is implemented.
		//serialSendReceive("AT+CGATT=1");
		serialDriver.emptyBuffer();
	}

	protected boolean isAlive() throws IOException {
		String response = serialSendReceive("AT");
		return response.matches("\\s*[\\p{ASCII}]*\\s+OK\\s");
	}

	protected boolean waitingForPin() throws IOException {
		String response = serialSendReceive("AT+CPIN?");
		return response.contains("SIM PIN");
	}

	protected boolean enterPin(String pin) throws IOException {
		serialDriver.send(CUtils.replace("AT+CPIN=\"{1}\"\r", "{1}", pin));
		sleepWithoutInterruption(DELAY_PIN);
		if(serialDriver.getResponse().contains("OK")) {
			sleepWithoutInterruption(DELAY_PIN);
			return true;
		} else return false;
	}

	protected boolean setVerboseErrors() throws IOException {
		String response = serialSendReceive("AT+CMEE=1");
		return response.matches("\\s+OK\\s+");
	}

	protected boolean setPduMode() throws IOException {
		String response = serialSendReceive("AT+CMGF=0");
		return response.matches("\\s+OK\\s+");
	}

	protected boolean setTextMode() throws IOException {
		String response = serialSendReceive("AT+CMGF=1");
		if(response.matches("\\s+OK\\s+")) {
			response = serialSendReceive("AT+CSCS=\"HEX\"");
			return response.matches("\\s+OK\\s+");
		} else return false;
	}

	protected boolean enableIndications() throws IOException {
		String response = serialSendReceive("AT+CNMI=1,1,0,0,0");
		return response.matches("\\s+OK\\s+");
	}

	protected boolean disableIndications() throws IOException {
		String response = serialSendReceive("AT+CNMI=0,0,0,0,0");
		return response.matches("\\s+OK\\s+");
	}

	protected String getManufacturer() throws IOException {
		return executeATCommand("CGMI", true);
	}

	protected String getModel() throws IOException {
		return executeATCommand("CGMM", true);
	}
	
	protected String getMsisdn() throws IOException {
		return executeATCommand(AT_GET_MSISDN, true);
	}

	protected String getSerialNo() throws IOException {
		return executeATCommand("CGSN", true);
	}

	protected String getImsi() throws IOException {
		return serialSendReceive(AT_IMSI);
	}

	protected String getSwVersion() throws IOException {
		return serialSendReceive("AT+CGMR");
	}

	protected String getBatteryLevel() throws IOException {
		return serialSendReceive(AT_BATTERY);
	}

	protected String getSignalLevel() throws IOException {
		return serialSendReceive("AT+CSQ");
	}

	protected boolean setMemoryLocation(String mem) throws IOException {
		String response = serialSendReceive("AT+CPMS=\"" + mem + "\"");
		return response.matches("\\s*[\\p{ASCII}]*\\s+OK\\s");
	}

	protected void switchToCmdMode() throws IOException {
		serialDriver.send("+++");
		sleepWithoutInterruption(DELAY_CMD_MODE);
	}

	protected boolean keepGsmLinkOpen() throws IOException {
		String response = serialSendReceive("AT+CMMS=1");
		return response.matches("\\s+OK\\s+");
	}

	/** Sends an SMS message and retrieves the SMSC reference number assigned to it. */
	protected int sendMessage(int size, String pdu, String phone, String text) throws IOException, NoResponseException, UnrecognizedHandlerProtocolException {
		int smscReferenceNumber;
		int messageProtocol = srv.getProtocol();
		switch(messageProtocol) {
			case CService.Protocol.PDU:
				int errorRetries = 0;
				while (true) {
					int responseRetries = 0;
					serialDriver.send(CUtils.replace("AT+CMGS=\"{1}\"\r", "\"{1}\"", "" + size));
					sleepWithoutInterruption(DELAY_CMGS);
					while (!serialDriver.dataAvailable()) {
						responseRetries++;
						if (responseRetries == srv.getRetriesNoResponse()) throw new NoResponseException();
						if (log != null) log.warn("ATHandler().SendMessage(): Still waiting for response (I) (" + responseRetries + ")...");
						sleepWithoutInterruption(srv.getDelayNoResponse());
					}
					responseRetries = 0;
					serialDriver.clearBuffer();
					serialDriver.send(pdu);
					serialDriver.send((char) 26);
					String response = serialDriver.getResponse();
					while(response.length() == 0) {
						responseRetries++;
						if (responseRetries == srv.getRetriesNoResponse()) throw new NoResponseException();
						if (log != null) log.warn("ATHandler().SendMessage(): Still waiting for response (II) (" + responseRetries + ")...");
						sleepWithoutInterruption(srv.getDelayNoResponse());
						response = serialDriver.getResponse();
					}
					if (response.indexOf("OK\r") >= 0) {
						smscReferenceNumber = getMessageReferenceNumberFromResponse(response);
						break;
					} else if (response.indexOf("ERROR") >= 0) {
						String err = response.replaceAll("\\s+", "");
						++errorRetries;
						AtCmsError.log(log, err, pdu);
						if (errorRetries == srv.getRetriesCmsErrors()) {
							if (log != null) log.error("Quit retrying, message lost...");
							smscReferenceNumber = -1;
							break;
						} else {
							if (log != null) log.warn("Retrying...");
							sleepWithoutInterruption(srv.getDelayCmsErrors());
						}
					} else smscReferenceNumber = -1;
				}
				break;
			case CService.Protocol.TEXT:
				String cmd1 = CUtils.replace("AT+CMGS=\"{1}\"\r", "{1}", phone);
				serialDriver.send(cmd1);
				serialDriver.emptyBuffer();
				serialDriver.send(text);
				sleepWithoutInterruption(DELAY_CMGS);
				serialDriver.send((byte) 26);
				String response = serialDriver.getResponse();
				if (response.indexOf("OK\r") >= 0) smscReferenceNumber = getMessageReferenceNumberFromResponse(response);
				else smscReferenceNumber = -1;
				break;
			default:
				throw new UnrecognizedHandlerProtocolException(messageProtocol);
		}
		return smscReferenceNumber;
	}
	
	/**
	 * Helper method for retrieving the SMSC reference number of a message from a serial response from the phone after sending.
	 * @param response
	 * @return
	 */
	private static int getMessageReferenceNumberFromResponse(String response) {
		StringBuilder bob = new StringBuilder(4);
		int i = response.indexOf(":");
		while (!Character.isDigit(response.charAt(i)))
			++i;
		while (Character.isDigit(response.charAt(i))) {
			bob.append(response.charAt(i));
			++i;
		}
		return Integer.parseInt(bob.toString());		
	}

	protected String listMessages(int messageClass) throws IOException, UnrecognizedHandlerProtocolException
	{
		int messageProtocol = srv.getProtocol();
		switch (messageProtocol)
		{
			case CService.Protocol.PDU:
				switch (messageClass)
				{
					case CService.MessageClass.All:
						serialDriver.send("AT+CMGL=4\r");
						break;
					case CService.MessageClass.Unread:
						serialDriver.send("AT+CMGL=0\r");
						break;
					case CService.MessageClass.Read:
						serialDriver.send("AT+CMGL=1\r");
						break;
					default:
						throw new RuntimeException("Unrecognized PDU message class: " + messageClass);
				}
				break;
			case CService.Protocol.TEXT:
				switch (messageClass)
				{
					case CService.MessageClass.All:
						serialDriver.send("AT+CMGL=\"ALL\"\r");
						break;
					case CService.MessageClass.Unread:
						serialDriver.send("AT+CMGL=\"REC UNREAD\"\r");
						break;
					case CService.MessageClass.Read:
						serialDriver.send("AT+CMGL=\"REC READ\"\r");
						break;
					default:
						throw new RuntimeException("Unrecognized TEXT message class: " + messageClass);
//						throw new OopsException();
				}
				break;
			default:
				throw new UnrecognizedHandlerProtocolException(messageProtocol);
//				throw new OopsException();
		}
		return serialDriver.getResponse();
	}

	protected boolean deleteMessage(int memIndex, String memLocation) throws IOException
	{
		if (!setMemoryLocation(memLocation)) throw new RuntimeException("CATHandler.deleteMessage() : Memory Location not found!!!");
		String response = serialSendReceive(CUtils.replace("AT+CMGD={1}", "{1}", "" + memIndex));
		return response.matches("\\s+OK\\s+");
	}

	protected String getGprsStatus() throws IOException {
		return serialSendReceive(AT_GPRS_STATUS);
	}

	protected String getNetworkRegistration() throws IOException {
		return serialSendReceive(AT_NETWORK_REGISTRATION);
	}

	protected void getStorageLocations() throws IOException {
		String response = serialSendReceive("AT+CPMS?");
		if(response.contains("+CPMS:")) {
			response = response.replaceAll("\\s*\\+CPMS:\\s*", "");
			StringTokenizer tokens = new StringTokenizer(response, ",");
			while(tokens.hasMoreTokens()) {
				String loc = tokens.nextToken().replace("\"", "");
				if(!storageLocations.contains(loc)) storageLocations += loc;
				tokens.nextToken();
				tokens.nextToken();
			}
		}
	}
	
	/**
	 * Writes a command to the serial driver and retrieves the response.
	 * @param command
	 */
	private String serialSendReceive(String command) throws IOException {
		if(TRACE) log.info("ISSUING COMMAND: " + command);
		serialDriver.send(command + END_OF_LINE);
		String response = serialDriver.getResponse();
		if(TRACE) log.info("RECEIVED RESPONSE: " + response);
		return response;
	}
	
	/**
	 * Writes an AT command to the serial driver and retrieves the response.  The supplied
	 * command will be prepended with "AT+" and appended with a \r.  If requested, any
	 * presence of the command in the response will be removed.
	 * @param command
	 * @param removeCommand If set true, the command is removed from the response.
	 */
	private String executeATCommand(String command, boolean removeCommand) throws IOException {
		// Issue the
		if(TRACE) System.out.println("ISSUING COMMAND: " + command);
		serialDriver.send("AT+" + command + '\r');
		String response = serialDriver.getResponse();
		if(TRACE) System.out.println("RECEIVED RESPONSE: " + response);
		// If requested, remove the command we issued from the response string
		if(removeCommand) {
			response = response.replaceAll("\\s*(AT)?\\+" + command + "\\s*", "");
		}
		return response;
	}

	/**
	 * Make the thread sleep until it's slept the requested amount.
	 * TODO this seems a dangerous practice.  If the thread's been interrupted, it should probably be allowed to die.
	 * @param millis
	 */
	public static void sleepWithoutInterruption(long millis) {
		while(millis > 0) {
			long startTime = System.currentTimeMillis();
			try {
				Thread.sleep(millis);
			} catch(InterruptedException ex) {}
			millis -= (System.currentTimeMillis() - startTime);
		}
	}
	
	@Override
	/** This method should be overridden by AT Handlers that only support sending. */
	protected boolean supportsReceive() {
		return true;
	}

	@Override
	public boolean supportsUcs2SmsSending() {
		return true;
	}
}
