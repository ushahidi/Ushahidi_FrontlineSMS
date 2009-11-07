/**
 * 
 */
package org.smslib;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Mock implementation of {@link CSerialDriver}.
 * @author Alex
 */
public class MockSerialDriver extends CSerialDriver {
	/** Log of all string sent to the device */
	private ArrayList<String> sentStrings = new ArrayList<String>();
	
//> CONSTRUCTORS
	/**
	 * Constructs a new {@link MockSerialDriver} with the supplied attributes.
	 * @param port
	 * @param baud
	 * @param srv
	 */
	public MockSerialDriver(String port, int baud, CService srv) {
		super(port, baud, srv);
	}
	
//> OVERRIDE METHODS
	/** Overrides {@link CSerialDriver#send(String)}, storing all 'sent' strings in {@link #sentStrings}. */
	@Override
	public void send(String s) throws IOException {
		this.sentStrings.add(s);
	}
}
