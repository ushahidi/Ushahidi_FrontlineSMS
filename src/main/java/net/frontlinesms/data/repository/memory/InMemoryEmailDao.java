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
import net.frontlinesms.data.domain.Email;
import net.frontlinesms.data.domain.Email.Field;
import net.frontlinesms.data.repository.EmailDao;

/**
 * In-memory implementation of {@link EmailDao}
 * @author Alex
 */
public class InMemoryEmailDao implements EmailDao {
	/** All emails in the system */
	private final HashSet<Email> emails = new HashSet<Email>();

	/** @see EmailDao#saveEmail(Email) */
	public void saveEmail(Email email) {
		this.emails.add(email);
	}

	/** @see EmailDao#updateEmail(Email) */
	public void updateEmail(Email email) {
		// No need to do anything for in-memory DAO
	}
	
	/** @see EmailDao#deleteEmail(Email) */
	public void deleteEmail(Email email) {
		emails.remove(email);
	}

	/** @see EmailDao#getAllEmails() */
	public Collection<Email> getAllEmails() {
		return Collections.unmodifiableCollection(this.emails);
	}

	/** @see EmailDao#getEmailCount() */
	public int getEmailCount() {
		return this.emails.size();
	}

	/** @see EmailDao#getEmailsForStatus(Integer[]) */
	public Collection<Email> getEmailsForStatus(Integer[] status) {
		HashSet<Email> selectedEmails = new HashSet<Email>();
		for(int s : status) {
			for(Email e : emails) {
				if(e.getStatus() == s) {
					selectedEmails.add(e);
				}
			}
		}
		return selectedEmails;
	}

	/** @see EmailDao#getEmailsWithLimit(Email.Field, Order, int, int) */
	public List<Email> getEmailsWithLimit(Field sortBy, Order order, int startIndex, int limit) {
		TreeMap<Object, Email> map = new TreeMap<Object, Email>();
		
		for(Email email : this.emails) {
			Object key;
			if(sortBy == Field.DATE) {
				key = email.getDate();
			} else if(sortBy == Field.EMAIL_CONTENT) {
				key = email.getEmailContent();
			} else if(sortBy == Field.FROM) {
				key = email.getEmailFrom();
			} else if(sortBy == Field.STATUS) {
				key = email.getStatus();
			} else if(sortBy == Field.SUBJECT) {
				key = email.getEmailSubject();
			} else if(sortBy == Field.TO) {
				key = email.getEmailRecipients();
			} else {
				key = null;
			}
			map.put(key, email);
		}
		
		ArrayList<Email> list = new ArrayList<Email>();
		list.addAll(map.values());
		return list.subList(startIndex, Math.min(list.size(), startIndex+limit));
	}

	/** @see EmailDao#getEmailsWithLimitWithoutSorting(int, int) */
	public List<Email> getEmailsWithLimitWithoutSorting(int startIndex, int limit) {
		ArrayList<Email> emails = new ArrayList<Email>();
		emails.addAll(this.emails);
		return emails.subList(startIndex, Math.min(emails.size(), startIndex + limit));
	}

}
