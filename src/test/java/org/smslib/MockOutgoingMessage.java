/**
 * 
 */
package org.smslib;

/**
 * @author Alex
 *
 */
class MockOutgoingMessage extends COutgoingMessage {
	private final String[] pdus;
	private final String smscNumber;
	
	/**
	 * @param pdus
	 * @param recipient
	 * @param smscNumber
	 */
	public MockOutgoingMessage(String smscNumber, String[] pdus) {
		this.pdus = pdus;
		this.smscNumber = smscNumber;
	}
	
	@Override
	public String[] generatePdus(String smscNumber, int concatReferenceNumber) {
		if(this.smscNumber != smscNumber) throw new IllegalArgumentException("Incorrect SMSC number provided.");
		return this.pdus;
	}
}
