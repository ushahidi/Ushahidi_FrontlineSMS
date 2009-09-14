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

import java.io.*;
import java.net.*;

import net.frontlinesms.*;

/**
 * This tests Intelli SMS sending using a java implementation.
 *
 * @author Carlos Eduardo Endler Genz
 * @date 19/01/2009
 */
public class TestIntelliSMSHttpSending extends AbstractTestCase {
	
	/**
	 * Sends a SMS text message, using IntelliSMS settings stored in
	 * the test properties file.
	 * 
	 * @throws IOException
	 */
	public void testSendSMS() throws IOException {
		// FIXME Do we need to encode everything?
		long start = System.currentTimeMillis();
		String username = PROPERTIES.getProperty("intellisms.username");
		try { username = URLEncoder.encode(username, "UTF-8"); } catch (UnsupportedEncodingException e) {}
		String password = PROPERTIES.getProperty("intellisms.password");
		try { password = URLEncoder.encode(password, "UTF-8"); } catch (UnsupportedEncodingException e) {}
		String sendTo = PROPERTIES.getProperty("intellisms.send.to");
		String msg = PROPERTIES.getProperty("intellisms.msg");
		// FIXME I think we do need to encode the msg, but if you do so, you get the message encoded in your phone.
		//try { msg = URLEncoder.encode(msg, "UTF-8"); } catch (UnsupportedEncodingException e) {}
		String sender = PROPERTIES.getProperty("intellisms.sender") == null ? "" : PROPERTIES.getProperty("intellisms.sender");
		if (sender != null)
			try { msg = URLEncoder.encode(msg, "UTF-8"); } catch (UnsupportedEncodingException e) {}
		String sendUrl = PROPERTIES.getProperty("intellisms.http.send.url");
		if (!sendUrl.endsWith("?")) sendUrl += "?";
		
		StringBuilder sb = new StringBuilder(sendUrl);
		sb.append("username=").append(username).append("&");
		sb.append("password=").append(password).append("&");
		sb.append("to=").append(sendTo).append("&");
		sb.append("text=").append(msg);
		if (sender != null)
			sb.append("&from=").append(sender);
		
		URL hp = new URL(sb.toString());
		HttpURLConnection conn = (HttpURLConnection) hp.openConnection();
		int rc = conn.getResponseCode();
		assertEquals(rc, HttpURLConnection.HTTP_OK);
		if (rc == HttpURLConnection.HTTP_OK) {
			InputStream input = conn.getInputStream();
			// Don't check the MIME type here - we don't want to confuse anybody
			// Get response data.
			BufferedReader inputData = new BufferedReader(new InputStreamReader(input));
			sb = new StringBuilder();
			String str;
			while (null != (str = inputData.readLine())) {
				sb.append(str + "\n");
			}
			System.out.println("Response: " + sb.toString());
			assertTrue(sb.toString().startsWith("ID:"));
		}
		
		System.out.println("Time to send: " + (System.currentTimeMillis() - start));
	}
	
	/**
	 * Sends a SMS text message, using IntelliSMS settings stored in
	 * the test properties file, expecting an error.
	 * 
	 * @throws IOException 
	 */
	public void testSendSMSError() throws IOException {
		String username = "dummy";
		String password = "dummyPass";
		String sendTo = PROPERTIES.getProperty("intellisms.send.to");
		String msg = PROPERTIES.getProperty("intellisms.msg");
		// FIXME I think we do need to encode the msg, but if you do so, you get the message encoded in your phone.
		//try { msg = URLEncoder.encode(msg, "UTF-8"); } catch (UnsupportedEncodingException e) {}
		String sender = PROPERTIES.getProperty("intellisms.sender") == null ? "" : PROPERTIES.getProperty("intellisms.sender");
		if (sender != null)
			try { msg = URLEncoder.encode(msg, "UTF-8"); } catch (UnsupportedEncodingException e) {}
		String sendUrl = PROPERTIES.getProperty("intellisms.http.send.url");
		if (!sendUrl.endsWith("?")) sendUrl += "?";
		
		StringBuilder sb = new StringBuilder(sendUrl);
		sb.append("username=").append(username).append("&");
		sb.append("password=").append(password).append("&");
		sb.append("to=").append(sendTo).append("&");
		sb.append("text=").append(msg);
		if (sender != null)
			sb.append("&from=").append(sender);
		
		URL hp = new URL(sb.toString());
		HttpURLConnection conn = (HttpURLConnection) hp.openConnection();
		int rc = conn.getResponseCode();
		assertEquals(rc, HttpURLConnection.HTTP_OK);
		if (rc == HttpURLConnection.HTTP_OK) {
			InputStream input = conn.getInputStream();
			// Don't check the MIME type here - we don't want to confuse anybody
			// Get response data.
			BufferedReader inputData = new BufferedReader(new InputStreamReader(input));
			sb = new StringBuilder();
			String str;
			while (null != (str = inputData.readLine())) {
				sb.append(str + "\n");
			}
			System.out.println("Response: " + sb.toString());
			assertTrue(sb.toString().startsWith("ERR:"));
		}
	}
	
	/**
	 * Gets the account balance value, in credits.
	 * 
	 * @throws IOException
	 */
	public void testGetBalance() throws IOException {
//		 FIXME Do we need to encode everything?
		String username = PROPERTIES.getProperty("intellisms.username");
		try { username = URLEncoder.encode(username, "UTF-8"); } catch (UnsupportedEncodingException e) {}
		String password = Utils.decodeBase64(PROPERTIES.getProperty("intellisms.password"));
		try { password = URLEncoder.encode(password, "UTF-8"); } catch (UnsupportedEncodingException e) {}

		String balanceUrl = PROPERTIES.getProperty("intellisms.http.balance.url");
		if (!balanceUrl.endsWith("?")) balanceUrl += "?";
		
		StringBuilder sb = new StringBuilder(balanceUrl);
		sb.append("username=").append(username).append("&");
		sb.append("password=").append(password).append("&");
		
		URL hp = new URL(sb.toString());
		HttpURLConnection conn = (HttpURLConnection) hp.openConnection();
		int rc = conn.getResponseCode();
		assertEquals(rc, HttpURLConnection.HTTP_OK);
		if (rc == HttpURLConnection.HTTP_OK) {
			InputStream input = conn.getInputStream();
			// Don't check the MIME type here - we don't want to confuse anybody
			// Get response data.
			BufferedReader inputData = new BufferedReader(new InputStreamReader(input));
			sb = new StringBuilder();
			String str;
			while (null != (str = inputData.readLine())) {
				sb.append(str + "\n");
			}
			try {
				int value = Integer.parseInt(sb.toString());
				System.out.println(value);
			} catch (NumberFormatException e) {
				// Error
				fail("Balance must be a number.");
			}
		}
	}
	
}
