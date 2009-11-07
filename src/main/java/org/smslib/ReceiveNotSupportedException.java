package org.smslib;

/**
 * Thrown if SMS Receive is attempted on a device which does not support it.
 * @author Alex Anderson
 * <li>alex(at)masabi(dot)com
 */
@SuppressWarnings("serial")
public class ReceiveNotSupportedException extends RuntimeException {}
