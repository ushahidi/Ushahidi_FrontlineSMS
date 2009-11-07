/**
 * 
 */
package net.frontlinesms.smsdevice;

/**
 * @author Alex
 *
 */
public interface SmsDeviceEventListener {
	/**
	 * Event fired when there is a change in status of an {@link SmsDevice}
	 * TODO why is it necessary to pass the status?  Surely it can be got from device.getStatus()?  Or does this provide opportunity for  concurrency issues?  If that's the case, it should be documented.
	 * @param phone The device whose status has changed.
	 * @param status The new status.
	 */
	public void smsDeviceEvent(SmsDevice phone, SmsDeviceStatus status);
}
