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
package net.frontlinesms.listener;
import net.frontlinesms.EmailSender;
import net.frontlinesms.data.domain.*;

/**
 * Abstract - implement this one event listener to be passed mail messages.
 * 
 * @author Carlos Eduardo Genz
 * <li> kadu(at)masabi(dot)com
 */
public interface EmailListener {

	/**
	 * Event Handler for outgoing emails
	 * @param sender The email sender object used for sending this email.
	 * @param The email just sent.
	 */
	public void outgoingEmailEvent(EmailSender sender, Email email);
	
}
