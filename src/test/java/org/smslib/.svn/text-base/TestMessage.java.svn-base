/**
 * 
 */
package org.smslib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.frontlinesms.hex.HexUtils;

/**
 * @author Alex
 *
 */
public class TestMessage {
	
//> Property Keys
	private static final String PROP_TEXT = "message.text";
	private static final String PROP_PDU = "message.pdu.";
	private static final String PROP_BINARY = "message.binary";
	private static final String PROP_PORT_DESTINATION = "message.destination.port";
	private static final String PROP_PORT_SOURCE = "message.source.port";
	private static final String PROP_RECIPIENT = "message.recipient";
	private static final String PROP_ENCODING = "message.encoding";
	private static final String PROP_TYPE = "message.type";
	private static final String PROP_PARTS = "message.parts";
	private static final String PROP_SMSCNUMBER = "smsc.msisdn";
	private static final String PROP_MPREFNO = "mp.ref.no";

//> Instance properties
	private final int destinationPort;
	private final int sourcePort;
	private final int encoding;
	private final String text;
	private final String[] pdus;
	private final String recipient;
	private final int type;
	private final byte[] binaryContent;
	private final String smscNumber;
	private final int mpRefNo;
	
	/**
	 * Create a new {@link TestMessage} from the supplied {@link COutgoingMessage}.
	 * @param message
	 * @param smscNumber SMS centre number
	 * @param mpRefNo message reference number
	 */
	public TestMessage(COutgoingMessage message, String smscNumber, int mpRefNo) {
		this.destinationPort = message.getDestinationPort();
		this.sourcePort = message.getSourcePort();
		this.encoding = message.getMessageEncoding();
		this.text = message.getText();
		this.pdus = message.generatePdus(smscNumber, mpRefNo);
		this.binaryContent = message.getBinary();
		this.type = message.getType();
		this.recipient = message.getRecipient();
		this.smscNumber = smscNumber;
		this.mpRefNo = mpRefNo;
	}
	
	
	
	/**
	 * @param destinationPort
	 * @param sourcePort
	 * @param encoding
	 * @param text
	 * @param pdus
	 * @param recipient
	 * @param type
	 * @param binaryContent
	 */
	private TestMessage(int destinationPort, int sourcePort, int encoding,
			String text, String[] pdus, String recipient, int type,
			byte[] binaryContent, String smscNumber, int mpRefNo) {
		this.destinationPort = destinationPort;
		this.sourcePort = sourcePort;
		this.encoding = encoding;
		this.text = text;
		this.pdus = pdus;
		this.recipient = recipient;
		this.type = type;
		this.binaryContent = binaryContent;
		this.smscNumber = smscNumber;
		this.mpRefNo = mpRefNo;
	}

	/**
	 * Create a new {@link TestMessage} from the supplied file.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static TestMessage getFromFile(File file) throws IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));

		int destinationPort = Integer.parseInt((String)prop.getProperty(PROP_PORT_DESTINATION));
		int sourcePort = Integer.parseInt((String)prop.getProperty(PROP_PORT_SOURCE));
		String text = prop.getProperty(PROP_TEXT);
		String binary = prop.getProperty(PROP_BINARY);
		byte[] binaryContent = null;
		if(binary != null) binaryContent = HexUtils.decode(binary);
		int encoding = Integer.parseInt(prop.getProperty(PROP_ENCODING));
		String recipient = prop.getProperty(PROP_RECIPIENT);
		int type = Integer.parseInt(prop.getProperty(PROP_TYPE));
		
		int partCount = Integer.parseInt(prop.getProperty(PROP_PARTS));
		String[] pdus = new String[partCount];
		for (int i = 0; i < partCount; i++) {
			pdus[i] = prop.getProperty(PROP_PDU + i);
		}
		
		// N.B this can be null
		String smscNumber = prop.getProperty(PROP_SMSCNUMBER);
		int mpRefNo = Integer.parseInt(prop.getProperty(PROP_MPREFNO));
		
		return new TestMessage(destinationPort, sourcePort, encoding,
				text, pdus, recipient, type,
				binaryContent, smscNumber, mpRefNo);
	}
	
	/**
	 * Save this test message to the supplied file location.
	 * @param file
	 * @throws IOException
	 * @throws OopsException
	 */
	public void saveToFile(File file) throws IOException {
		Properties messageDetails = new Properties();
		
		if(this.text != null) messageDetails.setProperty(PROP_TEXT, this.text);
		
		if(this.binaryContent != null) messageDetails.setProperty(PROP_BINARY, HexUtils.encode(this.binaryContent));
		
		messageDetails.setProperty(PROP_PORT_DESTINATION, Integer.toString(this.destinationPort));
		messageDetails.setProperty(PROP_PORT_SOURCE, Integer.toString(this.sourcePort));
		
		messageDetails.setProperty(PROP_RECIPIENT, this.recipient);
		
		messageDetails.setProperty(PROP_ENCODING, Integer.toString(this.encoding));

		messageDetails.setProperty(PROP_TYPE, Integer.toString(this.type));
		int messageParts = this.pdus.length;
		messageDetails.setProperty(PROP_PARTS, Integer.toString(messageParts));
		
		if(this.smscNumber != null) messageDetails.setProperty(PROP_SMSCNUMBER, this.smscNumber);
		messageDetails.setProperty(PROP_MPREFNO, Integer.toString(this.mpRefNo));
		
		for(int i=0; i<messageParts; ++i) {
			messageDetails.setProperty(PROP_PDU + i, this.pdus[i]);
		}
		
		messageDetails.store(new FileOutputStream(file), "no comment yet");
	}



	/**
	 * @return the destinationPort
	 */
	public int getDestinationPort() {
		return destinationPort;
	}



	/**
	 * @return the sourcePort
	 */
	public int getSourcePort() {
		return sourcePort;
	}



	/**
	 * @return the encoding
	 */
	public int getEncoding() {
		return encoding;
	}



	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}



	/**
	 * @return the pdus
	 */
	public String[] getPdus() {
		return pdus;
	}



	/**
	 * @return the recipient
	 */
	public String getRecipient() {
		return recipient;
	}



	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}



	/**
	 * @return the binaryContent
	 */
	public byte[] getBinaryContent() {
		return binaryContent;
	}



	public int getParts() {
		return this.pdus.length;
	}

	public String getSmscNumber() {
		return this.smscNumber;
	}

	public int getMpRefNo() {
		return this.mpRefNo;
	}
}
