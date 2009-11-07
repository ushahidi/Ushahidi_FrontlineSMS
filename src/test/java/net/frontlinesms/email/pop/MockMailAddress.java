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
package net.frontlinesms.email.pop;

import javax.mail.Address;

/**
 * Test object to simulate the {@link Address} class.
 *
 * @author Carlos Eduardo Endler Genz
 * @date 21/02/2009
 */
@SuppressWarnings("serial")
public class MockMailAddress extends Address {
	private String address;

	public MockMailAddress(String address) {
		this.address = address;
	}
	/* (non-Javadoc)
	 * @see javax.mail.Address#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Address#getType()
	 */
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Address#toString()
	 */
	@Override
	public String toString() {
		return address;
	}

}
