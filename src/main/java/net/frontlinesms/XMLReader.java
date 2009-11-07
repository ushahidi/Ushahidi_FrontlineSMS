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
package net.frontlinesms;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import net.frontlinesms.data.XMLMessage;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * This class is used to read XML files, which contains frontlineSMS commands, for instance,
 * sending messages to numbers, contacts or groups.
 * 
 * @author Carlos Eduardo Genz
 */
public class XMLReader {

	private static final String ATTRIBUTE_GROUP = "group";
	private static final String ATTRIBUTE_CONTACT = "contact";
	private static final String ATTRIBUTE_NUMBER = "number";
	private static final String ELEMENT_TO = "to";
	private static final String ELEMENT_TEXT = "text";
	private static final String ELEMENT_DATA = "data";
	private static final String TYPE_BINARY = "binary";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String ELEMENT_SMS = "sms";

	private Document doc;
	
	private static Logger LOG = Utils.getLogger(XMLReader.class);
	
	public XMLReader(InputStream toRead) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		doc = builder.build(toRead);
	}

	/**
	 * This method reads the XML file looking for instances of message (<sms/>).
	 * 
	 * @return A list with all message instances.
	 */
	public LinkedList<XMLMessage> readMessages() {
		Element root = doc.getRootElement();
		LinkedList<XMLMessage> messages = new LinkedList<XMLMessage>();
		for (Object o : root.getChildren()) {
			Element child = (Element) o;
			if (ELEMENT_SMS.equalsIgnoreCase(child.getName())) {
				try {
					//new message
					XMLMessage current = new XMLMessage();
					getMessageType(child, current);
					getData(child, current);
					getRecipients(child, current);
					messages.add(current);
				} catch (RuntimeException e) {
					LOG.debug("Error reading message", e);
				}
			}
		}
		return messages;
	}

	/**
	 * This method searches for <to/> elements into the supplied element and
	 * add the useful information to the supplied XMLMessage instance.
	 * 
	 * @param child
	 * @param current
	 */
	private static void getRecipients(Element child, XMLMessage current) {
		if (child.getChildren(ELEMENT_TO).size() == 0) {
			throw new RuntimeException("No [to] elements were found inside [" + ELEMENT_SMS + "]. Discarding message.");
		}
		for (Object to : child.getChildren(ELEMENT_TO)) {
			Element toElement = (Element) to;
			if (toElement.getAttributes().size() == 1) {
				Attribute recipient = (Attribute) toElement.getAttributes().get(0);
				if (!recipient.getValue().equals("")) {
					if (recipient.getName().equalsIgnoreCase(ATTRIBUTE_NUMBER)) {
						current.addNumber(recipient.getValue());
					} else if (recipient.getName().equalsIgnoreCase(ATTRIBUTE_CONTACT)) {
						current.addContact(recipient.getValue());
					} else if (recipient.getName().equalsIgnoreCase(ATTRIBUTE_GROUP)) {
						current.addGroup(recipient.getValue());
					} 
				}
			}
		}
	}

	/**
	 * This method searches for <data/> or <text/> elements into the supplied element and
	 * sets the data into the supplied XMLMessage instance.
	 * 
	 * @param child
	 * @param current
	 */
	private static void getData(Element child, XMLMessage current) {
		if (current.getType() == XMLMessage.TYPE_BINARY) {
			Attribute dataAttr = child.getAttribute(ELEMENT_DATA);
			if (dataAttr == null || dataAttr.getValue().equalsIgnoreCase("")) {
				Element data = child.getChild(ELEMENT_DATA);
				if (data == null || data.getValue().equalsIgnoreCase("")) {
					//PROBLEM.. data not found.
					throw new RuntimeException("[data] inside [" + ELEMENT_SMS + "] is missing or blank.");
				} 
				current.setData(data.getValue().trim());
			} else {
				current.setData(dataAttr.getValue().trim());
			}
		} else {
			Attribute textAttr = child.getAttribute(ELEMENT_TEXT);
			if (textAttr == null || textAttr.getValue().equalsIgnoreCase("")) {
				Element text = child.getChild(ELEMENT_TEXT);
				if (text == null || text.getValue().equalsIgnoreCase("")) {
					//PROBLEM.. text not found.
					throw new RuntimeException("[text] inside [" + ELEMENT_SMS + "] is missing or blank.");
				} 
				current.setData(text.getValue().trim());
			} else {
				current.setData(textAttr.getValue().trim());
			}
		}
	}

	/**
	 * This method searches for a <code>type</code> attribute into the supplied element and
	 * set the type of the supplied XMLMessage instance.
	 * 
	 * @param child
	 * @param current
	 */
	private static void getMessageType(Element child, XMLMessage current) {
		//Verify if type attribute is defined.
		if (child.getAttribute(ATTRIBUTE_TYPE) != null) {
			String type = child.getAttributeValue(ATTRIBUTE_TYPE);
			if (TYPE_BINARY.equalsIgnoreCase(type)) {
				current.setType(XMLMessage.TYPE_BINARY);
			}
		}
	}

}
