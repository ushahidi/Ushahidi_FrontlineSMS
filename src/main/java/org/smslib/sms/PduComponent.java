/**
 * 
 */
package org.smslib.sms;

/**
 * A component of an SMS message.
 * @author Alex
 */
public interface PduComponent {
	/**
	 * Convert this into binary. 
	 * @return binary representation of this {@link PduComponent}, as it would appear in a PDU
	 */
	public byte[] toBinary();
}
