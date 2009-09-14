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

import java.io.*;
import java.util.*;

import javax.activation.DataHandler;
import javax.mail.*;

/**
 * Test object to simulate the {@link javax.mail.Message} class.
 *
 * @author Carlos Eduardo Endler Genz
 * @date 21/02/2009
 */
public class MockMailMessage extends Message {
	private List<Address> from = new LinkedList<Address>();
	private List<Address> replyTo = new LinkedList<Address>();
	private Object content;
	/* (non-Javadoc)
	 * @see javax.mail.Message#addFrom(javax.mail.Address[])
	 */
	@Override
	public void addFrom(Address[] arg0) throws MessagingException {
		for (Address a : arg0) {
			from.add(a);
		}
	}

	@Override
	public void setReplyTo(Address[] arg0) throws MessagingException {
		replyTo.clear();
		for (Address a : arg0) {
			replyTo.add(a);
		}
	}
	
	@Override
	public Address[] getReplyTo() throws MessagingException {
		if (replyTo == null) return null;
		return replyTo.toArray(new Address[0]);
	}
	
	/* (non-Javadoc)
	 * @see javax.mail.Message#addRecipients(javax.mail.Message.RecipientType, javax.mail.Address[])
	 */
	@Override
	public void addRecipients(RecipientType arg0, Address[] arg1)
			throws MessagingException {}

	/* (non-Javadoc)
	 * @see javax.mail.Message#getFlags()
	 */
	@Override
	public Flags getFlags() throws MessagingException { return null; }

	/* (non-Javadoc)
	 * @see javax.mail.Message#getFrom()
	 */
	@Override
	public Address[] getFrom() throws MessagingException {
		if (from == null) return null;
		return from.toArray(new Address[0]);
	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#getReceivedDate()
	 */
	@Override
	public Date getReceivedDate() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#getRecipients(javax.mail.Message.RecipientType)
	 */
	@Override
	public Address[] getRecipients(RecipientType arg0)
			throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#getSentDate()
	 */
	@Override
	public Date getSentDate() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#getSubject()
	 */
	@Override
	public String getSubject() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#reply(boolean)
	 */
	@Override
	public Message reply(boolean arg0) throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#saveChanges()
	 */
	@Override
	public void saveChanges() throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#setFlags(javax.mail.Flags, boolean)
	 */
	@Override
	public void setFlags(Flags arg0, boolean arg1) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#setFrom()
	 */
	@Override
	public void setFrom() throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#setFrom(javax.mail.Address)
	 */
	@Override
	public void setFrom(Address arg0) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#setRecipients(javax.mail.Message.RecipientType, javax.mail.Address[])
	 */
	@Override
	public void setRecipients(RecipientType arg0, Address[] arg1)
			throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#setSentDate(java.util.Date)
	 */
	@Override
	public void setSentDate(Date arg0) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Message#setSubject(java.lang.String)
	 */
	@Override
	public void setSubject(String arg0) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String arg0, String arg1) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getAllHeaders()
	 */
	public Enumeration getAllHeaders() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getContent()
	 */
	public Object getContent() throws IOException, MessagingException {
		return content;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getContentType()
	 */
	public String getContentType() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getDataHandler()
	 */
	public DataHandler getDataHandler() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getDescription()
	 */
	public String getDescription() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getDisposition()
	 */
	public String getDisposition() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getFileName()
	 */
	public String getFileName() throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getHeader(java.lang.String)
	 */
	public String[] getHeader(String arg0) throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getInputStream()
	 */
	public InputStream getInputStream() throws IOException, MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getLineCount()
	 */
	public int getLineCount() throws MessagingException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getMatchingHeaders(java.lang.String[])
	 */
	public Enumeration getMatchingHeaders(String[] arg0)
			throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getNonMatchingHeaders(java.lang.String[])
	 */
	public Enumeration getNonMatchingHeaders(String[] arg0)
			throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#getSize()
	 */
	public int getSize() throws MessagingException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#isMimeType(java.lang.String)
	 */
	public boolean isMimeType(String arg0) throws MessagingException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#removeHeader(java.lang.String)
	 */
	public void removeHeader(String arg0) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#setContent(javax.mail.Multipart)
	 */
	public void setContent(Multipart arg0) throws MessagingException {
		this.content = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#setContent(java.lang.Object, java.lang.String)
	 */
	public void setContent(Object arg0, String arg1) throws MessagingException {
		this.content = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#setDataHandler(javax.activation.DataHandler)
	 */
	public void setDataHandler(DataHandler arg0) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#setDescription(java.lang.String)
	 */
	public void setDescription(String arg0) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#setDisposition(java.lang.String)
	 */
	public void setDisposition(String arg0) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#setFileName(java.lang.String)
	 */
	public void setFileName(String arg0) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String arg0, String arg1) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#setText(java.lang.String)
	 */
	public void setText(String arg0) throws MessagingException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#writeTo(java.io.OutputStream)
	 */
	public void writeTo(OutputStream arg0) throws IOException,
			MessagingException {
		// TODO Auto-generated method stub

	}

	/**
	 * @param from the from to set
	 */
	public void setFromList(List<Address> from) {
		this.from = from;
	}

	/**
	 * @param replyTo the replyTo to set
	 */
	public void setReplyToList(List<Address> replyTo) {
		this.replyTo = replyTo;
	}

}
