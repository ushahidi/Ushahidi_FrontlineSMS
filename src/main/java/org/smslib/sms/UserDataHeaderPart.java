/**
 * 
 */
package org.smslib.sms;

/**
 * Interface implemented by different UDH part types
 * @author Alex
 */
public interface UserDataHeaderPart extends PduComponent {
	/**
	 * Gets the length, in octets, of this {@link UserDataHeaderPart}.
	 * This length does not include IE-Identifier or IE-Length
	 * @return the length, in octets, of this {@link UserDataHeaderPart}
	 */
	public int getLength();
	
	/**
	 * Gets the Information-Element-Identifier for this header part.
	 * @return the Information-Element-Identifier, a single octet.
	 */
	public int getIEId();
}
