/**
 * 
 */
package org.smslib.sms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.smslib.util.HexUtils;


/**
 * {@link InputStream} implementation used for reading PDUs.
 * This class behaves the same as the basic {@link InputStream}
 * @author Alex
 */
public class PduInputStream extends DataInputStream {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/**
	 * Creates a new {@link PduInputStream} wrapping the supplied hexadecimal data.
	 * @param hexData The data to wrap, supplied as a hexadecimal {@link String}.
	 */
	public PduInputStream(String hexData) {
		this(HexUtils.decode(hexData));
	}
	
	/**
	 * Creates a new {@link PduInputStream} wrapping the supplied data.
	 * @param data The data to wrap
	 */
	public PduInputStream(byte[] data) {
		super(new ByteArrayInputStream(data));
	}

//> INSTANCE METHODS
	/**
	 * This wraps the {@link FilterInputStream#read()} method.  When the end of the stream is
	 * reached, an {@link EOFException} is thrown.
	 * @return the next octet value read from the stream
	 * @throws EOFException when the end of the stream is reached
	 */
	@Override
	public int read() throws IOException {
		int read = super.read();
		if(read == -1) {
			throw new EOFException("Unexpected end of stream.");
		} else {
			return read;
		}
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
