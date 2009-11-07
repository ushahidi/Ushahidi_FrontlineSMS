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

import java.io.Serializable;

/**
 * Class representing the different types of messages.
 */
public class MessageTypes implements Serializable
{
	private static final long serialVersionUID = -5031471839787113248L;

	private final String s;

	private MessageTypes(String s) { this.s = s; }
	public String toString() { return s; }

	/**
	 * Inbound message.
	 */
	public static final MessageTypes INBOUND = new MessageTypes("INBOUND");

	/**
	 * Outbound message.
	 */
	public static final MessageTypes OUTBOUND = new MessageTypes("OUTBOUND");

	/**
	 * Status (delivery) report message
	 */
	public static final MessageTypes STATUSREPORT = new MessageTypes("STATUSREPORT");

	/**
	 * Outbound WAP SI message.
	 */
	public static final MessageTypes WAPSI = new MessageTypes("WAPSI");

	/**
	 * Unhandled / unknown message.
	 */
	public static final MessageTypes UNKNOWN = new MessageTypes("UNKNOWN");
}
