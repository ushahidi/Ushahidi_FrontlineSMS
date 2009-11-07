/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms.message.processor;

import net.frontlinesms.data.domain.Message;
import net.frontlinesms.listener.SmsListener;
import net.frontlinesms.smsdevice.SmsDevice;
import net.frontlinesms.smsdevice.SmsDeviceStatus;

/**
 * 
 *
 * @author Carlos Eduardo Endler Genz
 * @date 11/03/2009
 */
public class FakeSmsDevice implements SmsDevice {

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#getMsisdn()
	 */
	public String getMsisdn() {
		return "Test Number";
	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#getStatusString()
	 */
	public String getStatusString() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#isBinarySendingSupported()
	 */
	public boolean isBinarySendingSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#isConnected()
	 */
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#isUcs2SendingSupported()
	 */
	public boolean isUcs2SendingSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#isUseForReceiving()
	 */
	public boolean isUseForReceiving() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#isUseForSending()
	 */
	public boolean isUseForSending() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#sendSMS(net.frontlinesms.data.Message)
	 */
	public void sendSMS(Message outgoingMessage) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#setSmsListener(net.frontlinesms.listener.SmsListener)
	 */
	public void setSmsListener(SmsListener smsListener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#setUseForReceiving(boolean)
	 */
	public void setUseForReceiving(boolean use) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#setUseForSending(boolean)
	 */
	public void setUseForSending(boolean use) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.frontlinesms.smsdevice.SmsDevice#supportsReceive()
	 */
	public boolean supportsReceive() {
		// TODO Auto-generated method stub
		return false;
	}

	public SmsDeviceStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStatusDetail() {
		// TODO Auto-generated method stub
		return null;
	}

}
