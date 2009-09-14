/**
 * 
 */
package org.smslib;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Alex
 *
 */
public class MockSerialDriver extends CSerialDriver {
	private ArrayList<String> sentStrings = new ArrayList<String>();
	
	public MockSerialDriver(String port, int baud, CService srv) {
		super(port, baud, srv);
	}
	
	@Override
	public void send(String s) throws IOException {
		this.sentStrings.add(s);
	}
}
