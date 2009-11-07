/**
 * 
 */
package org.smslib.sms.header;

import java.io.IOException;

import org.smslib.sms.PduInputStream;
import org.smslib.sms.UserDataHeaderPart;
import org.smslib.util.TpduUtils;

/**
 * @author Alex
 *
 */
public class IeConcat16bit implements IeConcat {
	
//> INSTANCE PROPERTIES
	/** The concat reference number */
	private final int reference;
	/** The part number of this message part */
	private final int partSequence;
	/** The total number of parts in this concat message */
	private final int totalParts;
	
//> CONSTRUCTOR
	/**
	 * Create a new instance of this class
	 * @param reference value for {@link #reference}
	 * @param partSequence value for {@link #partSequence}
	 * @param totalParts value for {@link #totalParts}
	 */
	private IeConcat16bit(int reference, int partSequence, int totalParts) {
		if(reference != (reference & 0xFFFF)) {
			throw new IllegalArgumentException("Concat reference out of range: " + reference);
		}
		this.reference = reference;
		this.partSequence = partSequence;
		this.totalParts = totalParts;
	}
	
//> ACCESSORS
	/** @return {@link #reference} */
	public int getReference() {
		return reference;
	}
	/** @return {@link #partSequence} */
	public int getPartSequence() {
		return partSequence;
	}
	/** @return {@link #totalParts} */
	public int getTotalParts() {
		return totalParts;
	}
	/** @see UserDataHeaderPart#getLength() */
	public int getLength() {
		return TpduUtils.TP_UDH_IEI_CONCAT_SMS_16BIT_LENGTH;
	}
	/** @see UserDataHeaderPart#getIEId() */
	public int getIEId() {
		return TpduUtils.TP_UDH_IEI_CONCAT_SMS_16BIT;
	}
	
	/** Converts this object to a string suitable for logging. */
	@Override
	public String toString() {
		return this.getClass() + "[" +
				"reference:'" + this.reference + "', " +
				"partSequence:'" + this.partSequence + "', " +
				"totalParts:'" + this.totalParts+ "'" + 
				"]";
	}

// SmsComponent METHODS
	/** @see org.smslib.sms.PduComponent#toBinary() */
	public byte[] toBinary() {
		throw new IllegalStateException("NYI");
	}

//> STATIC FACTORY METHODS
	/**
	 * Create a new instance of this class from the supplied {@link PduInputStream}.
	 * @param in
	 * @return a new instance of {@link IeConcat16bit}
	 * @throws IOException
	 */
	public static IeConcat16bit getFromStream(PduInputStream in) throws IOException {
		int reference = in.readUnsignedShort();
		int totalParts = in.read();
		int partSequence = in.read();
		
		return new IeConcat16bit(reference, partSequence, totalParts);
	}

}
