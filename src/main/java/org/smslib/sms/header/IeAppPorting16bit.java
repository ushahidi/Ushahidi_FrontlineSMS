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
public class IeAppPorting16bit implements UserDataHeaderPart {

//> INSTANCE PROPERTIES
	/** The source port of a message */
	private final int sourcePort;
	/** The destination port of a message */
	private final int destinationPort;
	
//> CONSTRUCTOR
	/**
	 * Create a new instance of this class
	 * @param sourcePort value for {@link #sourcePort}
	 * @param destinationPort value for {@link #destinationPort}
	 */
	private IeAppPorting16bit(int sourcePort, int destinationPort) {
		if(sourcePort != (sourcePort & 0xFFFF)) {
			throw new IllegalArgumentException("Port value out of range: "  + sourcePort);
		}
		this.sourcePort = sourcePort;

		if(sourcePort != (sourcePort & 0xFFFF)) {
			throw new IllegalArgumentException("Port value out of range: "  + sourcePort);
		}
		this.destinationPort = destinationPort;
	}
	
//> ACCESSORS
	/** @return {@link #sourcePort} */
	public int getSourcePort() {
		return sourcePort;
	}

	/** @return {@link #destinationPort} */
	public int getDestinationPort() {
		return destinationPort;
	}
	
	/** @see UserDataHeaderPart#getLength() */
	public int getLength() {
		return TpduUtils.TP_UDH_IEI_APP_PORTING_16BIT_LENGTH;
	}

	/** @see UserDataHeaderPart#getIEId() */
	public int getIEId() {
		return TpduUtils.TP_UDH_IEI_APP_PORTING_16BIT;
	}
	
	/** Converts this object to a string suitable for logging. */
	@Override
	public String toString() {
		return this.getClass() + "[" +
				"sourcePort:'" + this.sourcePort + "', " +
				"destinationPort:'" + this.destinationPort+ "'" + 
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
	 * @return a new instance of {@link IeAppPorting16bit}
	 * @throws IOException
	 */
	public static IeAppPorting16bit getFromStream(PduInputStream in) throws IOException {
		int destinationPort = in.readUnsignedShort();
		int sourcePort = in.readUnsignedShort();
		
		return new IeAppPorting16bit(sourcePort, destinationPort);
	}

}
