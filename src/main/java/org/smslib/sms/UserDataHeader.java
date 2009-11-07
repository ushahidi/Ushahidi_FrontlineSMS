/**
 * 
 */
package org.smslib.sms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.smslib.sms.header.*;
import org.smslib.util.TpduUtils;

/**
 * The User Data Header (UDH) of an SMS message.
 * @author Alex
 */
public class UserDataHeader implements PduComponent {
	
//> PROPERTIES
	/** The parts of this header */
	private final List<UserDataHeaderPart> parts = new ArrayList<UserDataHeaderPart>();
	
//> ACCESSOR METHODS
	/**
	 * Adds another part to this header.
	 * @param part Part of this UD-Header to add
	 */
	public void addPart(UserDataHeaderPart part) {
		this.parts.add(part);
	}
	
	/** @return {@link #parts}, in unmodifiable form. */
	public List<UserDataHeaderPart> getParts() {
		return Collections.unmodifiableList(this.parts);
	}
	
	/** @return the length, in octets, of this header */
	public int getLength() {
		// Always include 1 byte for the UDH-Length itself
		int length = 1;
		// Now add the length of each part that this header is made of
		for(UserDataHeaderPart part : this.parts) {
			// Add length for the ID and length octets
			length += 2;
			// Add length of the actual contents
			length += part.getLength();
		}
		
		return length;
	}

	/** Converts this object to a string suitable for logging. */
	@Override
	public String toString() {
		return this.getClass() + "[" +
				"parts:'" + this.parts + "'" +
				"]";
	}

//> SmsComponent METHODS
	/** @see PduComponent#toBinary() */
	public byte[] toBinary() {
		// TODO Auto-generated method stub
		return null;
	}

//> STATIC FACTORY METHODS
	/**
	 * @param in the input stream to read from
	 * @param id the Information Element Identifier of this header part
	 * @param length the length of this part, in octets 
	 * @return the {@link UserDataHeaderPart} loaded from the stream, or <code>null</code> if the part was an unrecognized type
	 * @throws IOException 
	 */
	private static UserDataHeaderPart getFromStream(PduInputStream in, int id, int length) throws IOException {
		switch(id) {
		case TpduUtils.TP_UDH_IEI_APP_PORTING_16BIT:
			// Message ports have been set
			return IeAppPorting16bit.getFromStream(in);
		case TpduUtils.TP_UDH_IEI_CONCAT_SMS_8BIT:
			// This is part of a multipart/concatenated message
			return IeConcat8bit.getFromStream(in);
			// This is part of a multipart/concatenated message
		case TpduUtils.TP_UDH_IEI_CONCAT_SMS_16BIT:
			return IeConcat16bit.getFromStream(in);
		case TpduUtils.TP_UDH_IEI_WIRELESS_MESSAGE_CONTROL_PROTOCOL:
			// N.B. No devices we have used will allow access to the WAP SI inbox using AT commands, so this is much of a muchness right now
			/* Drop through to unknown handling */
		default:
			return UnknownUdhPart.getFromStream(in, id, length);
		}
	}
	
	/**
	 * @param in The stream to read the {@link UserDataHeader} from.
	 * @return the {@link UserDataHeader} represented in the supplied {@link PduInputStream}
	 * @throws PduDecodeException if there was a problem decoding the stream or a problem with the integrity of the data read
	 * @throws IOException if there was a problem reading from the stream
	 */
	public static UserDataHeader getFromStream(PduInputStream in) throws PduDecodeException, IOException {
		UserDataHeader header = new UserDataHeader();
		
		int udhLength = in.read();
		int headerOctetsRemaining = udhLength;
		while(headerOctetsRemaining > 0) {
			int informationElementIdentifier = in.read();
			int informationElementLength = in.read();
			headerOctetsRemaining -= 2 + informationElementLength;
			
			UserDataHeaderPart headerPart = getFromStream(in, informationElementIdentifier, informationElementLength);
			if(headerPart != null) {
				header.addPart(headerPart);
			}
		}
		
		if(headerOctetsRemaining != 0) {
			// The UDH was a different length to that expected.
			throw new PduDecodeException("UDH Length was different to that expected - there are " + headerOctetsRemaining + " octets remaining after UDH processing.");
		}
		
		++udhLength; // Add 1 to the UDH length - now it includes the length octet itself
		
		// Validate that the UDH we were provided with is the same as the UDH our parts calculate
		if(udhLength != header.getLength()) {
			throw new PduDecodeException("UDH Length was different: udhLength read from stream=" + udhLength + "; header.getLength()=" + header.getLength());
		}
		
		return header;
	}
}
