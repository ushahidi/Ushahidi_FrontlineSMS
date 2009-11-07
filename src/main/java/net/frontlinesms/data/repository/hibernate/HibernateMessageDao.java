/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;

import net.frontlinesms.data.Order;
import net.frontlinesms.data.domain.Email;
import net.frontlinesms.data.domain.Group;
import net.frontlinesms.data.domain.Keyword;
import net.frontlinesms.data.domain.KeywordAction;
import net.frontlinesms.data.domain.Message;
import net.frontlinesms.data.domain.Message.Field;
import net.frontlinesms.data.repository.MessageDao;

/**
 * Hibernate implementation of {@link MessageDao}.
 * @author Alex
 */
public class HibernateMessageDao extends BaseHibernateDao<Message> implements MessageDao {
	/** Create instance of this class */
	public HibernateMessageDao() {
		super(Message.class);
	}

	/** @see MessageDao#deleteMessage(Message) */
	public void deleteMessage(Message message) {
		super.delete(message);
	}

	/** @see MessageDao#getAllMessages() */
	public List<Message> getAllMessages() {
		return super.getAll();
	}

	/** @see MessageDao#getAllMessages(int, Field, Order, Long, Long, int, int) */
	public List<Message> getAllMessages(int messageType, Field sortBy, Order order, Long start, Long end, int startIndex, int limit) {
		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
		addTypeCriteria(criteria, messageType);
		addDateCriteria(criteria, start, end);
		return super.getList(criteria, startIndex, limit);
	}

	/** @see MessageDao#getMessageCount(int, Integer[]) */
	public int getMessageCount(int messageType, Integer[] messageStati) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Email.class);
		addStatusCriteria(criteria, messageStati);
		addTypeCriteria(criteria, messageType);
		return getCount(criteria);
	}

	/** @see MessageDao#getMessageCount(int, Long, Long) */
	public int getMessageCount(int messageType, Long start, Long end) {
		DetachedCriteria criteria = super.getCriterion();
		addDateCriteria(criteria, start, end);
		addTypeCriteria(criteria, messageType);
		return getCount(criteria);
	}

	/** @see MessageDao#getMessageCount(int, Keyword, Long, Long) */
	public int getMessageCount(int messageType, Keyword keyword, Long start, Long end) {
		DetachedCriteria criteria = super.getCriterion();
		addDateCriteria(criteria, start, end);
		addTypeCriteria(criteria, messageType);
		addKeywordMatchCriteria(criteria, keyword);
		return getCount(criteria);
	}

	/** @see MessageDao#getMessageCountForGroups(int, List, Long, Long) */
	public int getMessageCountForGroups(int messageType, List<Group> groups, Long start, Long end) {
		DetachedCriteria criteria = super.getCriterion();
		addTypeCriteria(criteria, messageType);
		addGroupsCriteria(criteria, groups);
		addDateCriteria(criteria, start, end);
		return super.getCount(criteria);
	}

	/** @see MessageDao#getMessageCountForMsisdn(int, String, Long, Long) */
	public int getMessageCountForMsisdn(int messageType, String phoneNumber, Long start, Long end) {
		DetachedCriteria criteria = super.getCriterion();
		addTypeCriteria(criteria, messageType);
		addDateCriteria(criteria, start, end);
		addPhoneNumberMatchCriteria(criteria, phoneNumber, true, true);
		return getCount(criteria);
	}

	/** @see MessageDao#getMessageForStatusUpdate(String, int) */
	public Message getMessageForStatusUpdate(String targetMsisdnSuffix, int smscReference) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.like(Field.RECIPIENT_MSISDN.getFieldName(), '%' + targetMsisdnSuffix));
		criteria.add(Restrictions.eq(Field.SMSC_REFERENCE.getFieldName(), smscReference));
		return super.getUnique(criteria);
	}

	/** @see MessageDao#getMessages(int, Field, Order) */
	public List<Message> getMessages(int messageType, Field sortBy, Order order) {
		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
		addTypeCriteria(criteria, messageType);
		return getList(criteria);
	}

	/** @see MessageDao#getMessagesForKeyword(int, Keyword, Field, Order, Long, Long, int, int) */
	public List<Message> getMessages(int messageType, Keyword keyword, Field sortBy, Order order) {
		DetachedCriteria criteria = getSortCriterion(sortBy, order);
		addTypeCriteria(criteria, messageType);
		addKeywordMatchCriteria(criteria, keyword);
		return getList(criteria);
	}

	/** @see MessageDao#getMessages(int, Integer[]) */
	public Collection<Message> getMessages(int messageType, Integer[] status) {
		DetachedCriteria criteria = super.getCriterion();
		addTypeCriteria(criteria, messageType);
		addStatusCriteria(criteria, status);
		return getList(criteria);
	}

	/** @see MessageDao#getMessagesForAction(KeywordAction) */
	public List<Message> getMessagesForAction(KeywordAction action) {
		// TODO need to add action messages
		return new ArrayList<Message>();
	}

	/** @see MessageDao#getMessagesForGroups(int, List, Field, Order, Long, Long, int, int) */
	public List<Message> getMessagesForGroups(int messageType, List<Group> groups, Field sortBy, Order order, Long start, Long end, int startIndex, int limit) {
		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
		addTypeCriteria(criteria, messageType);
		addGroupsCriteria(criteria, groups);
		addDateCriteria(criteria, start, end);
		return super.getList(criteria , startIndex, limit);
	}

	/** @see MessageDao#getMessagesForKeyword(int, Keyword, Field, Order, Long, Long, int, int) */
	public List<Message> getMessagesForKeyword(int messageType, Keyword keyword, Field sortBy, Order order, Long start, Long end, int startIndex, int limit) {
		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
		addTypeCriteria(criteria, messageType);
		addDateCriteria(criteria, start, end);
		addKeywordMatchCriteria(criteria, keyword);
		return getList(criteria);
	}

	/** @see MessageDao#getMessagesForKeyword(int, Keyword) */
	public List<Message> getMessagesForKeyword(int messageType, Keyword keyword) {
		DetachedCriteria criteria = super.getCriterion();
		addTypeCriteria(criteria, messageType);
		addKeywordMatchCriteria(criteria, keyword);
		return getList(criteria);
	}

	/** @see MessageDao#getMessagesForMsisdn(int, String, Field, Order, Long, Long, int, int) */
	public List<Message> getMessagesForMsisdn(int messageType, String phoneNumber, Field sortBy, Order order, Long start, Long end, int startIndex, int limit) {
		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
		addTypeCriteria(criteria, messageType);
		addDateCriteria(criteria, start, end);
		addPhoneNumberMatchCriteria(criteria, phoneNumber, true, true);
		return super.getList(criteria, startIndex, limit);
	}

	/** @see MessageDao#getMessagesForMsisdn(int, String, Field, Order, Long, Long) */
	public List<Message> getMessagesForMsisdn(int messageType, String phoneNumber, Field sortBy, Order order, Long start, Long end) {
		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
		addTypeCriteria(criteria, messageType);
		addDateCriteria(criteria, start, end);
		addPhoneNumberMatchCriteria(criteria, phoneNumber, true, true);
		return super.getList(criteria);
	}

	/** @see MessageDao#getMessagesForStati(int, Integer[], Field, Order, int, int) */
	public List<Message> getMessagesForStati(int messageType, Integer[] messageStati, Field sortBy, Order order, int startIndex, int limit) {
		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
		addTypeCriteria(criteria, messageType);
		addStatusCriteria(criteria, messageStati);
		return super.getList(criteria, startIndex, limit);
	}

	/** @see MessageDao#getSMSCount(Long, Long) */
	public int getSMSCount(Long start, Long end) {
		System.out.println("HibernateMessageDao.getSMSCount()");
		DetachedCriteria criteria = super.getCriterion();
		addDateCriteria(criteria, start, end);
		return super.getCount(criteria);
	}

	/** @see MessageDao#getSMSCountForGroups(List, Long, Long) */
	public int getSMSCountForGroups(List<Group> groups, Long start, Long end) {
		DetachedCriteria criteria = super.getCriterion();
		addGroupsCriteria(criteria, groups);
		addDateCriteria(criteria, start, end);
		return super.getCount(criteria);
	}

	/** @see MessageDao#getSMSCountForKeyword(Keyword, Long, Long) */
	public int getSMSCountForKeyword(Keyword keyword, Long start, Long end) {
		DetachedCriteria criteria = super.getCriterion();
		addDateCriteria(criteria, start, end);
		addKeywordMatchCriteria(criteria, keyword);
		return super.getCount(criteria);
	}

	/** @see MessageDao#getSMSCountForMsisdn(String, Long, Long) */
	public int getSMSCountForMsisdn(String phoneNumber, Long start, Long end) {
		DetachedCriteria criteria = super.getCriterion();
		addDateCriteria(criteria, start, end);
		addPhoneNumberMatchCriteria(criteria, phoneNumber, true, true);
		return super.getCount(criteria);
	}

	/** @see MessageDao#saveMessage(Message) */
	public void saveMessage(Message message) {
		super.saveWithoutDuplicateHandling(message);
	}

	/** @see MessageDao#updateMessage(Message) */
	public void updateMessage(Message message) {
		super.updateWithoutDuplicateHandling(message);
	}
	
	/**
	 * Augments the supplied criteria with that required to match a keyword.
	 * @param criteria
	 * @param keyword 
	 */
	private void addKeywordMatchCriteria(DetachedCriteria criteria, Keyword keyword) {
		String keywordString = keyword.getKeywordString();
		Criterion matchKeyword = Restrictions.or(
				Restrictions.eq(Field.MESSAGE_CONTENT.getFieldName(), keywordString),
				Restrictions.like(Field.MESSAGE_CONTENT.getFieldName(), keywordString + ' '));
		criteria.add(matchKeyword);
	}
	
	/**
	 * Augments the supplied criteria with that required to match an msisdn, either for the sender, the receiver or both.
	 * @param criteria
	 * @param phoneNumber 
	 * @param sender 
	 * @param receiver 
	 */
	private void addPhoneNumberMatchCriteria(DetachedCriteria criteria, String phoneNumber, boolean sender, boolean receiver) {
		if(!sender && !receiver) {
			throw new IllegalStateException("This neither sender nor receiver matching is requested.");
		}
		// TODO make sure that this size is the same as in the old implementation
		String msisdnLike = '%' + phoneNumber.substring(Math.min(5, phoneNumber.length()));
		SimpleExpression likeSender = Restrictions.like(Field.SENDER_MSISDN.getFieldName(), msisdnLike);
		SimpleExpression likeReceiver = Restrictions.like(Field.RECIPIENT_MSISDN.getFieldName(), msisdnLike);
		if(sender && receiver) {
			criteria.add(Restrictions.or(likeSender, likeReceiver));
		} else if(sender) {
			criteria.add(likeSender);
		} else if(receiver) {
			criteria.add(likeReceiver);
		}
	}
	
	/**
	 * Augments the supplied criteria with that required to match a date range.
	 * @param criteria
	 * @param start
	 * @param end
	 */
	private void addDateCriteria(DetachedCriteria criteria, Long start, Long end) {
		if(start != null) {
			criteria.add(Restrictions.ge(Field.DATE.getFieldName(), start));
		}
		if(end != null) {
			criteria.add(Restrictions.le(Field.DATE.getFieldName(), end));
		}
	}
	
	/**
	 * Augments the supplied criteria with that required to match a date range.
	 * @param criteria
	 * @param messageType 
	 */
	private void addTypeCriteria(DetachedCriteria criteria, int messageType) {
		if(messageType != Message.TYPE_ALL) {
			criteria.add(Restrictions.eq(Field.TYPE.getFieldName(), messageType));
		}
	}

	/**
	 * Augments the supplied criteria with that required to match a date range.
	 * @param criteria 
	 * @param statuses 
	 */
	private void addStatusCriteria(DetachedCriteria criteria, Integer[] statuses) {
		criteria.add(Restrictions.in(Field.STATUS.getFieldName(), statuses));
	}

	/**
	 * Augments the supplied criteria with that required to match a list of groups.
	 * @param criteria 
	 * @param groups 
	 */
	private void addGroupsCriteria(DetachedCriteria criteria, Collection<Group> groups) {
		// TODO add a join off contact.message, contact.groups => groups
	}
}
