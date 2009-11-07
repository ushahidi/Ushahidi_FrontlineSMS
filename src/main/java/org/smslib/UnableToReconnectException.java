/**
 * 
 */
package org.smslib;

import org.smslib.handler.CATHandler;

/**
 * Exception thrown when there was an error communicating with a {@link CATHandler}, and re-connection
 * should not be attempted before the fault has been examined.
 * @author Alex
 */
@SuppressWarnings("serial")
public class UnableToReconnectException extends SMSLibDeviceException {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/** @see SMSLibDeviceException#SMSLibDeviceException(String) */
	public UnableToReconnectException(String message) {
		super(message);
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
