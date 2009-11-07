/**
 * 
 */
package org.smslib.sms.header;

import java.io.IOException;


import org.smslib.sms.PduInputStream;
import org.smslib.sms.UserDataHeaderPart;
import org.smslib.util.HexUtils;

/**
 * A UDH part that we don't understand.
 * @author Alex
 */
public class UnknownUdhPart implements UserDataHeaderPart {
	
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	/** Information-Element-Identifier for this header part */
	private final int ieId;
	/** The binary content of this {@link UserDataHeaderPart} */
	private final byte[] binaryContent;

//> CONSTRUCTORS
	/**
	 * Create a new instance of this class
	 * @param ieId the 
	 * @param binaryContent value for {@link #binaryContent}
	 */
	private UnknownUdhPart(int ieId, byte[] binaryContent) {
		if(ieId != (ieId & 0xFF)) throw new IllegalArgumentException("IE-Identifier is more than one octet of data: " + ieId);
		this.ieId = ieId;
		this.binaryContent = binaryContent;
	}
	
	/** @see UserDataHeaderPart#getIEId() */
	public int getIEId() {
		return this.ieId;
	}

//> ACCESSORS
	/** @see org.smslib.sms.UserDataHeaderPart#getLength() */
	public int getLength() {
		return this.binaryContent.length;
	}

	/** @see org.smslib.sms.PduComponent#toBinary() */
	public byte[] toBinary() {
		return this.binaryContent;
	}
	
	/** Converts this object to a string suitable for logging. */
	@Override
	public String toString() {
		return this.getClass() + "[" +
				"binaryContent:'" + (this.binaryContent==null?null:HexUtils.encode(this.binaryContent)) + "'" +
				"]";
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES
	/**
	 * Create a new instance of this class from the supplied {@link PduInputStream}.
	 * @param in stream to read the {@link UserDataHeaderPart} from
	 * @param id value for {@link #ieId}
	 * @param length length in octets of the {@link UserDataHeaderPart}
	 * @return a new instance of {@link IeAppPorting16bit}
	 * @throws IOException
	 */
	public static UserDataHeaderPart getFromStream(PduInputStream in, int id, int length) throws IOException {
		byte[] binaryContent = new byte[length];
		for(int byteIndex = 0; byteIndex < length; ++byteIndex) {
			binaryContent[byteIndex] = in.readByte();
		}
		return new UnknownUdhPart(id, binaryContent);
	}

//> STATIC HELPER METHODS
}
