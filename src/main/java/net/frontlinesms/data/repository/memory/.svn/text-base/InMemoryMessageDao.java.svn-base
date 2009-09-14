/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import net.frontlinesms.data.Order;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.domain.Message.Field;
import net.frontlinesms.data.repository.MessageDao;

/**
 * In-memory implementation of {@link MessageDao}.
 * @author Alex
 */
public class InMemoryMessageDao implements MessageDao {
	/** All the saved messages */
	private final HashSet<Message> messages = new HashSet<Message>();
	
	/** @see MessageDao#deleteMessage(Message) */
	public void deleteMessage(Message message) {
		this.messages.remove(message);
	}

	/** @see MessageDao#getAllMessages() */
	public Collection<Message> getAllMessages() {
		return this.messages;
	}

	/** @see MessageDao#getAllMessages(int, Field, Order, Long, Long, int, int) */
	public List<Message> getAllMessages(int type, Field field, Order order, Long start, Long end, int startIndex, int limit) {
		List<Message> messages = this.getMessages(type, field, order);
		for(Message m : messages.toArray(new Message[0])) {
			if(m.getDate() < start || m.getDate() > end) {
				messages.remove(m);
			}
		}
		return messages.subList(startIndex, Math.min(messages.size(), startIndex+limit));
	}

	/** @see MessageDao#getMessageCount(int, Integer[]) */
	public int getMessageCount(int messageType, Integer[] messageStati) {
		return getMessages(messageType, messageStati).size();
	}

	/** @see MessageDao#getMessageCount(int, Long, Long) */
	public int getMessageCount(int type, Long start, Long end) {
		int count = 0;
		for(Message m : this.messages.toArray(new Message[0])) {
			if(m.getType() == type
					&& m.getDate() >= start
					&& m.getDate() <= end) {
				++count;
			}
		}
		return count;
	}

	/** @see MessageDao#getMessageCount(int, Keyword, Long, Long) */
	public int getMessageCount(int messageType, Keyword keyword, Long start, Long end) {
		return this.getMessagesForKeyword(messageType, keyword, Field.MESSAGE_CONTENT, Order.ASCENDING, start, end, 0, Integer.MAX_VALUE).size();
	}

	/** @see MessageDao#getMessageCountForGroups(int, List, Long, Long) */
	public int getMessageCountForGroups(int messageType, List<Group> groups, Long start, Long end) {
		return getMessagesForGroups(messageType, groups, Field.MESSAGE_CONTENT, Order.ASCENDING, start, end, 0, Integer.MAX_VALUE).size();
	}

	/** @see MessageDao#getMessageCountForMsisdn(int, String, Long, Long) */
	public int getMessageCountForMsisdn(int type, String number, Long start, Long end) {
		return this.getMessagesForMsisdn(type, number, Field.MESSAGE_CONTENT, Order.ASCENDING, start, end).size();
	}

	/** @see MessageDao#getMessageForStatusUpdate(String, int) */
	public Message getMessageForStatusUpdate(String targetMsisdnSuffix, int smscReference) {
		for(Message m : getAllMessages()) {
			if(m.getRecipientMsisdn().endsWith(targetMsisdnSuffix) && m.getSmscReference() == smscReference) {
				return m;
			}
		}
		return null;
	}

	/** @see MessageDao#getMessages(int, Keyword, Message.Field, Order) */
	public List<Message> getMessages(int messageType, Keyword keyword, Field sortBy, Order order) {
		TreeMap<Object, Message> sortedMessages = new TreeMap<Object, Message>();
		
		for(Message message : this.getMessagesForKeyword(messageType, keyword)) {
			sortedMessages.put(getSortObject(sortBy, message), message);
		}
		
		ArrayList<Message> sortedList = new ArrayList<Message>();
		sortedList.addAll(sortedMessages.values());
		if(order == Order.ASCENDING) {
			Collections.reverse(sortedList);
		}
		return sortedList;
	}

	/** @see MessageDao#getMessages(int, Integer[]) */
	public Collection<Message> getMessages(int type, Integer[] status) {
		HashSet<Message> messages = new HashSet<Message>();
		for(Message m : this.messages) {
			if(m.getType() == type) {
				for(int s : status) {
					if(s == m.getStatus()) {
						messages.add(m);
					}
				}
			}
		}
		return messages;
	}

	/** @see MessageDao#getMessages(int, Message.Field, Order) */
	public List<Message> getMessages(int messageType, Field sortBy, Order order) {
			TreeMap<Object, Message> sortedMessages = new TreeMap<Object, Message>();
		
		for(Message message : this.getAllMessages()) {
			if(message.getType() == messageType) {
				sortedMessages.put(getSortObject(sortBy, message), message);
			}
		}
		
		ArrayList<Message> sortedList = new ArrayList<Message>();
		sortedList.addAll(sortedMessages.values());
		if(order == Order.ASCENDING) {
			Collections.reverse(sortedList);
		}
		return sortedList;
	}

	/* (non-Javadoc)
	 * @see MessageDao#getMessagesForAction(KeywordAction)
	 */
	public List<Message> getMessagesForAction(KeywordAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	/** @see MessageDao#getMessagesForGroups(int, List, Message.Field, Order, Long, Long, int, int) */
	public List<Message> getMessagesForGroups(int messageType,
			List<Group> groups, Field sortBy, Order order, Long start,
			Long end, int startIndex, int limit) {
		TreeMap<Object, Message> sortedMessages = new TreeMap<Object, Message>();
		
		for(Message message : this.getAllMessages()) {
			if(message.getType() == messageType
					&& message.getDate() >= start
					&& message.getDate() <= end) {
				for(Group g : groups) {
					for(Contact c : g.getDirectMembers()) {
						if(c.getMsisdn().equals(message.getSenderMsisdn())
								|| c.getMsisdn().equals(message.getRecipientMsisdn())) {
							sortedMessages.put(getSortObject(sortBy, message), message);
						}
					}
				}
			}
		}
		
		ArrayList<Message> sortedList = new ArrayList<Message>();
		sortedList.addAll(sortedMessages.values());
		if(order == Order.ASCENDING) {
			Collections.reverse(sortedList);
		}
		return sortedList.subList(startIndex, Math.min(sortedList.size(), startIndex+limit));
	}

	/** @see MessageDao#getMessagesForKeyword(int, Keyword, Message.Field, Order, Long, Long, int, int) */
	public List<Message> getMessagesForKeyword(int messageType,
			Keyword keyword, Field sortBy, Order order, Long start, Long end,
			int startIndex, int limit) {
		List<Message> messages = getMessages(messageType, keyword, sortBy, order);
		for(Message m : messages) {
			if(m.getDate() < start || m.getDate() > end) {
				messages.remove(m);
			}
		}
		return messages.subList(startIndex, Math.min(messages.size(), startIndex + limit));
	}

	/** @see MessageDao#getMessagesForKeyword(int, Keyword) */
	public List<Message> getMessagesForKeyword(int messageType, Keyword keyword) {
		ArrayList<Message> messages = new ArrayList<Message>();
		
		for(Message message : this.getAllMessages()) {
			if(message.getType() == messageType &&
					keyword.matches(message.getTextContent())) {
				messages.add(message);
			}
		}
		
		return messages;
	}

	/** @see MessageDao#getMessagesForMsisdn(int, java.lang.String, Message.Field, Order, Long, Long, int, int) */
	public List<Message> getMessagesForMsisdn(int type,
			String number, Field sortBy, Order order, Long start, Long end,
			int startIndex, int limit) {
		List<Message> messages = getMessagesForMsisdn(type, number, sortBy, order, start, end);
		return messages.subList(startIndex, Math.min(messages.size(), startIndex + limit));
	}

	/** @see MessageDao#getMessagesForMsisdn(int, String, Message.Field, Order, Long, Long) */
	public List<Message> getMessagesForMsisdn(int type, String number, Field sortBy, Order order, Long start, Long end) {
		List<Message> messages = getMessages(type, sortBy, order);
		for(Message m : messages.toArray(new Message[0])) {
			if(m.getDate() < start || m.getDate() > end) {
				// TODO should actually match MSISDNs a little more subtley than this
				if(!m.getRecipientMsisdn().equals(number) && !m.getSenderMsisdn().equals(number)) {
					messages.remove(m);
				}
			}
		}
		return messages;
	}

	/** @see MessageDao#getMessagesForStati(int, Integer[], Message.Field, Order, int, int) */
	public List<Message> getMessagesForStati(int messageType,
			Integer[] messageStati, Field sortBy, Order order, int startIndex,
			int limit) {
		Collection<Message> messages = this.getMessages(messageType, messageStati);

		TreeMap<Object, Message> sortedMessages = new TreeMap<Object, Message>();
		for(Message m : messages) {
			sortedMessages.put(getSortObject(sortBy, m), m);
		}
		ArrayList<Message> sortedList = new ArrayList<Message>();
		sortedList.addAll(sortedMessages.values());
		if(order == Order.ASCENDING) {
			Collections.reverse(sortedList);
		}
		
		return sortedList.subList(startIndex, Math.min(sortedList.size(), startIndex + limit));
	}

	/** @see MessageDao#getSMSCount(Long, Long) */
	public int getSMSCount(Long start, Long end) {
		return getSms(start, end).size();
	}
	
	/**
	 * Gets all SMS messages sent or received between two specific dates.
	 * @param start Start date for messages to be included
	 * @param end End date for messages to be included
	 * @return All sms messages in the system sent or received between the specified dates
	 */
	private Collection<Message> getSms(Long start, Long end) {
		HashSet<Message> messages = new HashSet<Message>();
		for(Message m : this.messages) {
			long date = m.getDate();
			if(date >= start && date <= end) {
				messages.add(m);
			}
		}
		return messages;
	}

	/** @see MessageDao#getSMSCountForGroups(java.util.List, Long, Long) */
	public int getSMSCountForGroups(List<Group> groups, Long start, Long end) {
		HashSet<String> memberPhoneNumbers = new HashSet<String>();
		for(Group g : groups) {
			for(Contact c : g.getAllMembers()) {
				memberPhoneNumbers.add(c.getMsisdn());
			}
		}
		int count = 0;
		for(String phoneNumber : memberPhoneNumbers) {
			count += getSMSCountForMsisdn(phoneNumber, 0l, Long.MAX_VALUE);
		}
		return count;
	}

	/** @see MessageDao#getSMSCountForKeyword(Keyword, Long, Long) */
	public int getSMSCountForKeyword(Keyword keyword, Long start, Long end) {
		int count = 0;
		for(Message m : this.messages.toArray(new Message[0])) {
			if(keyword.matches(m.getTextContent())) {
				++count;
			}
		}
		return count;
	}

	/** @see MessageDao#getSMSCountForMsisdn(java.lang.String, Long, Long) */
	public int getSMSCountForMsisdn(String number, Long start, Long end) {
		int count = 0;
		for(Message m : getSms(start, end)) {
			String mNum = m.getSenderMsisdn();
			if(number.equals(mNum)) {
				++count;
			}
		}
		return count;
	}

	/** Save a message to the data source */
	public void saveMessage(Message message) {
		this.messages.add(message);
	}
	
	/**
	 * Get the field to sort this message by.
	 * @param sortBy
	 * @param message
	 * @return the message parameter that we should be sorting by
	 */
	private Object getSortObject(Field sortBy, Message message) {
		if(sortBy == Field.DATE) {
			return message.getDate();
		} else if(sortBy == Field.MESSAGE_CONTENT) {
			return message.getTextContent();
		} else if(sortBy == Field.RECIPIENT_MSISDN) {
			return message.getRecipientMsisdn();
		} else if(sortBy == Field.SENDER_MSISDN) {
			return message.getSenderMsisdn();
		} else if(sortBy == Field.STATUS) {
			return message.getStatus();
		}
		return null;
	}
}
