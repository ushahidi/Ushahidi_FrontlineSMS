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
 * Class representing the failure reasons of a failed outbound message.
 */
public class FailureCauses
{
	private final String s;

	private FailureCauses(String s) { this.s = s; }
	public String toString() { return s; }

	/**
	 * No error, everything OK.
	 */
	public static final FailureCauses NO_ERROR = new FailureCauses("NO_ERROR");

	/**
	 * Bad destination number - fatal error.
	 */
	public static final FailureCauses BAD_NUMBER = new FailureCauses("BAD_NUMBER");

	/**
	 * Bad message format - fatal error.
	 */
	public static final FailureCauses BAD_FORMAT = new FailureCauses("BAD_FORMAT");

	/**
	 * Generic gateway failure - transient error, retry later.
	 */
	public static final FailureCauses GATEWAY_FAILURE = new FailureCauses("GATEWAY_FAILURE");

	/**
	 * No credit left - fatal error.
	 */
	public static final FailureCauses NO_CREDIT = new FailureCauses("NO_CREDIT");

	/**
	 * Authentication problem (pin, passwords, etc) - fatal error.
	 */
	public static final FailureCauses GATEWAY_AUTH = new FailureCauses("GATEWAY_AUTH");

	/**
	 * Unable to route message - transient error.
	 */
	public static final FailureCauses NO_ROUTE = new FailureCauses("NO_ROUTE");

	/**
	 * Unknown generic problems encountered.
	 */
	public static final FailureCauses UNKNOWN = new FailureCauses("UNKNOWN");
}
