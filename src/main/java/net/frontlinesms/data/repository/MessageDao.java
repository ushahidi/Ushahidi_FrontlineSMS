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
package net.frontlinesms.data.repository;

import java.util.Collection;
import java.util.List;

import net.frontlinesms.data.Order;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.domain.Message.Field;

/**
 * Factory for creating instances of net.frontlinesms.data.Message
 * @author Alex
 */
public interface MessageDao {
	/**
	 * Gets all messages for the specified number. 
	 * @param type 
	 * @param number
	 * @param sortBy Message Field to sort the results by
	 * @param order direction to order results in
	 * @param start TODO
	 * @param end TODO
	 * @param startIndex 
	 * @param limit the maximum number of messages to recover
	 * @param index the result index of the messages to recover
	 * @return
	 */
	public List<Message> getMessagesForMsisdn(int type, String number, Field sortBy, Order order, Long start, Long end, int startIndex, int limit);

	/**
	 * Gets all messages for the specified number. 
	 * @param number
	 * @param sortBy Message Field to sort the results by
	 * @param order direction to order results in
	 * @param start TODO
	 * @param end TODO
	 * @param limit the maximum number of messages to recover
	 * @param index the result index of the messages to recover
	 * @return
	 */
	public List<Message> getMessagesForMsisdn(int type, String number, Field sortBy, Order order, Long start, Long end);
	
	/**
	 * Gets message count for the specified number. 
	 * @param number
	 * @param start TODO
	 * @param end TODO
	 * @return
	 */
	public int getMessageCountForMsisdn(int type, String number, Long start, Long end);
	
	/**
	 * Gets count of SMS sent for the specified number. 
	 * @param number
	 * @param start TODO
	 * @param end TODO
	 * @return
	 */
	public int getSMSCountForMsisdn(String number, Long start, Long end);
	
	/**
	 * Gets count of SMS sent. 
	 * @param start TODO
	 * @param end TODO
	 * @return
	 */
	public int getSMSCount(Long start, Long end);
	
	/**
	 * Gets count of SMS sent for the specified keyword. 
	 * @param keyword
	 * @param start TODO
	 * @param end TODO
	 * @return
	 */
	public int getSMSCountForKeyword(Keyword keyword, Long start, Long end);
	
	/**
	 * Gets all messages of a particular type (SENT, RECEIVED, ALL) which begin with the specified keyword.  If
	 * the supplied keyword is NULL, it will be ignored (i.e. all messages of requested type will be returned).
	 * @param messageType message type(s) to be retrieved, or Message.TYPE_ALL for all messages
	 * @param keyword word messages should start with, or NULL to retrieve all messages
	 * @param sortBy Message Field to sort the results by
	 * @param order direction to order results in
	 * @param start TODO
	 * @param end TODO
	 * @param limit the maximum number of messages to recover
	 * @param index the result index of the messages to recover
	 * @return
	 * FIXME keyword should never be null for this method, and messageType should be understood to
	 * be TYPE_RECEIVED always.  If other functionality is required, the method should be renamed
	 * or new methods created.
	 */
	public List<Message> getMessagesForKeyword(int messageType, Keyword keyword, Field sortBy, Order order, Long start, Long end, int startIndex, int limit);
	
	/**
	 * Gets all messages of a particular type (SENT, RECEIVED, ALL).
	 * @param messageType message type(s) to be retrieved, or Message.TYPE_ALL for all messages
	 * @param sortBy Message Field to sort the results by
	 * @param order direction to order results in
	 * @return
	 */
	public List<Message> getMessages(int messageType, Field sortBy, Order order);
	
	/**
	 * Gets all messages of a particular type (SENT, RECEIVED, ALL) which begin with the specified keyword.
	 * @param messageType message type(s) to be retrieved, or Message.TYPE_ALL for all messages
	 * @param keyword word messages should start with
	 * @return
	 */
	public List<Message> getMessagesForKeyword(int messageType, Keyword keyword);
	
	// TODO could greatly speed this by defining status ranges e.g. STATUS_DRAFT=1, _OUTBOX=2, _PENDING=3 -> status <=3 => message not yet sent
	public List<Message> getMessagesForStati(int messageType, Integer[] messageStati, Field sortBy, Order order, int startIndex, int limit);
	
	/**
	 * Get the total number of messages with the supplied statuses.
	 * @param messageType
	 * @param messageStati
	 * @return
	 */
	public int getMessageCount(int messageType, Integer[] messageStati);

	/**
	 * Gets all messages of a particular type (SENT, RECEIVED, ALL) which begin with the specified keyword.  If
	 * the supplied keyword is NULL, it will be ignored (i.e. all messages of requested type will be returned).
	 * 
	 * @param messageType message type(s) to be retrieved, or Message.TYPE_ALL for all messages
	 * @param keyword word messages should start with, or NULL to retrieve all messages
	 * @param sortBy Message Field to sort the results by
	 * @param order direction to order results in
	 * @param index the result index of the messages to recover
	 * @param limit the maximum number of messages to recover
	 * @return
	 */
	public List<Message> getMessages(int messageType, Keyword keyword, Field sortBy, Order order);
	
	/**
	 * Gets all messages.
	 * @return all messages in the system 
	 */
	public List<Message> getAllMessages();
	
	/**
	 * Gets a page of messages.
	 * @param type the type of the message
	 * @param field the field to sort by
	 * @param order the order to sort by
	 * @param start the start date for the messages
	 * @param end the end date for the messages
	 * @param startIndex the index of the first message to get
	 * @param limit the maximum number of messages to get
	 * @return list of all messages conforming to the specified constraints and sorted in a particular way.
	 *
	 */
	public List<Message> getAllMessages(int type, Field field, Order order, Long start, Long end, int startIndex, int limit);
	
	/**
	 * Gets the number of messages of a specific type from between the specified dates
	 * @param type
	 * @param start
	 * @param end
	 * @return count of messages
	 */
	public int getMessageCount(int type, Long start, Long end);
	
	/**
	 * Gets all messages with the supplied status and type.
	 * @param type
	 * @param status
	 * @return 
	 */
	public Collection<Message> getMessages(int type, Integer[] status);
	
	/**
	 * Gets the number of messagesthere are of the given type for the given keyword.
	 * @param messageType
	 * @param keyword
	 * @param start TODO
	 * @param end TODO
	 * @return
	 */
	public int getMessageCount(int messageType, Keyword keyword, Long start, Long end);
	
	/**
	 * Gets the outgoing message with the matching SMSC Reference Number sent to
	 * a number ending with the supplied msisdn suffix.
	 * @param targetMsisdnSuffix last N digits of the target's msisdn
	 * @param smscReference
	 * @return
	 */
	public Message getMessageForStatusUpdate(String targetMsisdnSuffix, int smscReference);
	
	/**
	 * Returns all message associated with these groups.
	 * @param messageType 
	 * @param groups 
	 * @param field TODO
	 * @param order TODO
	 * @param start TODO
	 * @param end TODO
	 * @param startIndex 
	 * @param limit 
	 * @return
	 */
	public List<Message> getMessagesForGroups(int messageType, List<Group> groups, Field field, Order order, Long start, Long end, int startIndex, int limit);
	
	/**
	 * Returns the message count associated to these groups.
	 * @param messageType 
	 * @param groups 
	 * @param start TODO
	 * @param end TODO
	 * @return
	 */
	public int getMessageCountForGroups(int messageType, List<Group> groups, Long start, Long end);
	
	/**
	 * Returns the count of sent SMS associated to these groups.
	 * @param groups 
	 * @param start TODO
	 * @param end TODO
	 * @return
	 */
	public int getSMSCountForGroups(List<Group> groups, Long start, Long end);

	/**
	 * Delete the supplied message to the data source.
	 * @param message the message to be deleted
	 */
	public void deleteMessage(Message message);

	/**
	 * @param action
	 * @return
	 */
	public List<Message> getMessagesForAction(KeywordAction action);

	/**
	 * Save the supplied message to the data source.
	 * @param message the message to be saved
	 */
	public void saveMessage(Message message);

	/**
	 * Update the supplied message in the data source.
	 * @param message the message to be updated
	 */
	public void updateMessage(Message message);
}
