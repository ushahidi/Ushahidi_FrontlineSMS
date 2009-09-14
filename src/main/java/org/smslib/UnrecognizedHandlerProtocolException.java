package org.smslib;

@SuppressWarnings("serial")
public class UnrecognizedHandlerProtocolException extends SMSLibDeviceException {
	public UnrecognizedHandlerProtocolException(int protocol) {
		super("Unrecognized message protocol: " + protocol);
	}
}
