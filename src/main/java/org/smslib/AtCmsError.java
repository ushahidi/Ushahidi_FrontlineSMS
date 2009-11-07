/**
 * 
 */
package org.smslib;

import org.apache.log4j.Logger;

/**
 * Class used for interpretation of CMS Errors read from AT Handlers.
 * 
 * Error descriptions taken from http://www.dreamfabric.com/sms/cms_error.html.  TODO find a more suitable source and check these error messages are definitely not device or manufacturer specific.
 * @author Alex
 */
public class AtCmsError {
	/** Description returned by {@link #getDescription(int)} when the error code was not recognized. */
	private static final String DESCRIPTION_UNRECOGNIZED = "Unrecognised error code.";
	
	/**
	 * Logs the CMS errors in the reply from an AT Command Handler.  This method should only be called when
	 * a CMS error has been caused.
	 * @param log The logger to log the CMS error to.  If this object is <code>null</code>, this method will do nothing. 
	 * @param atReply The text of the AT reply which contains the CMS error's details.  Usually, this should be a string representation of a single integer in the range 0-512. 
	 * @param pdu the pdu that caused the error, or <code>null</code> if it is not available or applicable
	 */
	public static void log(Logger log, String atReply, String pdu) {
		if (log != null) {
			String errorString = "CMS Errors [" + atReply + "]";
			// If we can get a description for the error message, we add this to the log output.
			String errorDescription = AtCmsError.getDescription(atReply);
			if(errorDescription != null) {
				errorString += ": (" + errorDescription + ")";
			}
			if(pdu != null) {
				errorString += " for PDU " + pdu;
			}
			log.error(errorString);
		}
	}
	
	/**
	 * Attempts to parse an error code from the supplied string, and get the description for this code.
	 * If an integer error code cannot be decoded from the supplied string, null will be returned.
	 * @param errorCodeAsString A string read from an AT device containing the error code. 
	 * @return A text description of the error code, or <code>null</code> if the supplied string could not be parsed as an integer.
	 */
	private static String getDescription(String errorCodeAsString) {
		try {
			int errorCode = Integer.parseInt(errorCodeAsString);
			return getDescription(errorCode);
		} catch(NumberFormatException ex) {
			return null;
		}
	}
	
	/**
	 * Gets a description of the numeric CMS error code supplied.  If no standard description
	 * for an error code can be found, {@value #DESCRIPTION_UNRECOGNIZED} will be returned.
	 * @param errorCode
	 * @return
	 */
	private static String getDescription(int errorCode) {
		if(errorCode >= 0 && errorCode <= 127) {
			// TODO interpret error codes in this region from the spec indicated
			return "See GSM 04.11 Annex E-2 values.";
		}

		if(errorCode >= 128 && errorCode <= 255) {
			// TODO interpret error codes in this region from the spec indicated
			return "See GSM 03.40 section 9.2.3.22 values";
		}
		 
		switch(errorCode) {
			case 300:
				return "Phone failure";
			case 301:
				return "SMS service of phone reserved";
			case 302:
				return "Operation not allowed";
			case 303:
				return "Operation not supported";
			case 304:
				return "Invalid PDU mode parameter";
			case 305:
				return "Invalid text mode parameter";
			case 310:
				return "SIM not inserted";
			case 311:
				return "SIM PIN necessary";
			case 312:
				return "PH-SIM PIN necessary";
			case 313:
				return "SIM failure";
			case 314:
				return "SIM busy";
			case 315:
				return "SIM wrong";
			case 320:
				return "Memory failure";
			case 321:
				return "Invalid memory index";
			case 322:
				return "Memory full";
			case 330:
				return "SMSC (message service center) address unknown";
			case 331:
				return "No network service";
			case 332:
				return "Network timeout";
			case 500:
				return "Unknown error";
			case 512:
				return "Manufacturer specific";
		}
		return DESCRIPTION_UNRECOGNIZED;
	}
}
