/**
 * 
 */
package org.smslib;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Data for testing decoding of an incoming SMS.
 * @author Alex
 */
abstract class IncomingSmsTestData {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	/** The PDUs of the message */
	private final String[] messagePdus;

//> CONSTRUCTORS
	/**
	 * Create a new instance of this class.
	 * @param messagePdus
	 */
	protected IncomingSmsTestData(String... messagePdus) {
		this.messagePdus = messagePdus;
	}
	
//> ACCESSORS
	/**
	 * @return the pdu of this sms 
	 * @throws IllegalStateException if this sms comprises more than one pdu 
	 */
	public String getMessagePdu() throws IllegalStateException {
		if(this.messagePdus.length != 1) {
			throw new IllegalStateException("Cannot get single PDU of a multipart message.");
		}
		return this.messagePdus[0];
	}
	
	/** @return collection of the PDUs that make up this message */
	public Collection<String> getMessagePdus() {
		return Collections.unmodifiableCollection(Arrays.asList(this.messagePdus));
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
