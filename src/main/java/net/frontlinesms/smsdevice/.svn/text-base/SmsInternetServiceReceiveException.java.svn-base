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
package net.frontlinesms.smsdevice;

import net.frontlinesms.email.pop.PopReceiveException;

/**
 * Encapsulates all exceptions thrown during the receive method.
 *
 * @author Carlos Eduardo Endler Genz
 * @date 19/02/2009
 */
@SuppressWarnings("serial")
class SmsInternetServiceReceiveException extends Exception {
	public SmsInternetServiceReceiveException(String string, Throwable t) {
		super(string, t);
	}

	public SmsInternetServiceReceiveException(PopReceiveException cause) {
		super(cause);
	}
}
