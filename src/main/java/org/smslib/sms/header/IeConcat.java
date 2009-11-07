package org.smslib.sms.header;

import org.smslib.sms.UserDataHeaderPart;

/**
 * Header part describing a multipart message and its place in the full message.
 * @author Alex
 */
public interface IeConcat extends UserDataHeaderPart {
	/** @return the reference number of the list of concat messages */
	public int getReference();
	/** @return the 1-based index into the concat messages list */
	public int getPartSequence();
	/** @return the total number of concat messages make up the whole message */
	public int getTotalParts();
}
