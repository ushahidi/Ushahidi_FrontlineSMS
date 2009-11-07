/**
 * 
 */
package org.smslib;

/**
 * @author Alex
 *
 */
public class IncomingBinarySmsTestData extends IncomingSmsTestData {
//> STATIC CONSTANTS
	/** Test binary messages.  These messages all have a single PDU. */
	public static final IncomingBinarySmsTestData[] BINARY_MESSAGES = {
		new IncomingBinarySmsTestData(new byte[]{1, 37, 0, 0, 0, 37, 0, 1, 1, 0, 1, 3, 7, 10, 0, 17, 65, 32, 118, 101, 114, 121, 32, 115, 104, 111, 114, 116, 32, 102, 111, 114, 109, 15, 8, 0, 1, 0, 16, 67, 111, 109, 109, 101, 110, 116, 115, 44, 32, 112, 108, 101, 97, 115, 101, 10, 9, 0, 1, 5},
				"0791449737019037640C91447749318438000490201171434400430605043E870000012500000025000101000103070A00114120766572792073686F727420666F726D0F0800010010436F6D6D656E74732C20706C656173650A09000105"),
		new IncomingBinarySmsTestData(new byte[]{0x00, 0x00, 0x00, 0x02, 0x3C, 0x74, 0x65, 0x78, 0x74, 0x3E},
				"0791361907002004440C9136197773977800048050827112512311060504D9030000000000023C746578743E"),
		new IncomingBinarySmsTestData(new byte[]{0x00, 0x00, 0x00, 0x03, 0x3C, 0x74, 0x65, 0x78, 0x74, 0x3E},
				"0791361907002004040C913619777397780004805082713235230A000000033C746578743E"),
		new IncomingBinarySmsTestData(new byte[]{0x00, 0x00, 0x00, 0x0C, 0x3C, 0x74, 0x65, 0x78, 0x74, 0x3E},
				"0791361907002004040C913609363107320004805082618283230A0000000C3C746578743E"),
		new IncomingBinarySmsTestData(new byte[]{0x00, 0x00, 0x00, 0x0B, 0x3C, 0x74, 0x65, 0x78, 0x74, 0x3E},
				"0791361907002004440C9136093631073200F58050826182902311060504D903FDE80000000B3C746578743E"),
	};

//> INSTANCE PROPERTIES
	/** The text of the decoded message. */
	private final byte[] messageBinary;

//> CONSTRUCTORS
	/**
	 * Create a new instance of this class.
	 * @param messageBinary 
	 * @param messagePdus
	 */
	IncomingBinarySmsTestData(byte[] messageBinary, String... messagePdus) {
		super(messagePdus);
		this.messageBinary = messageBinary;
	}

//> ACCESSORS
	/** @return {@link #messageBinary} */
	public byte[] getMessageBinary() {
		return messageBinary;
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
