/**
 * 
 */
package org.smslib;

/**
 * Mock implementation of {@link COutgoingMessage}.
 * @author Alex
 */
class MockOutgoingMessage extends COutgoingMessage {
	/** The PDUs that make up this message */
	private final String[] pdus;
	/** The SMSC number that will be used for these messages */
	private final String smscNumber;
	
	/**
	 * @param pdus
	 * @param smscNumber
	 */
	public MockOutgoingMessage(String smscNumber, String[] pdus) {
		this.pdus = pdus;
		this.smscNumber = smscNumber;
	}

	/**
	 * Override of {@link COutgoingMessage#generatePdus(String, int)} which returns the preset value
	 * stored in {@link #pdus}.
	 */
	@Override
	public String[] generatePdus(String smscNumber, int concatReferenceNumber) {
		if(this.smscNumber != smscNumber) throw new IllegalArgumentException("Incorrect SMSC number provided.");
		return this.pdus;
	}
}
