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
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.*;

/**
 * Test object to simulate the {@link BodyPart} class.
 *
 * @author Carlos Eduardo Endler Genz
 * @date 21/02/2009
 */
public class MockBodyPart extends BodyPart {
	private Object content;
	private String contentType;

	public MockBodyPart(Object content, String contentType) {
		super();
		this.content = content;
		this.contentType = contentType;
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
		return contentType;
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
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.mail.Part#setContent(java.lang.Object, java.lang.String)
	 */
	public void setContent(Object arg0, String arg1) throws MessagingException {
		// TODO Auto-generated method stub

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

}
