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
package net.frontlinesms.data.domain;

import javax.persistence.*;

import net.frontlinesms.data.EntityField;

/**
 * Object representing an email in our data structure.
 * 
 * @author Carlos Eduardo Genz
 * @author Alex Anderson
 */
@Entity
public class Email {
	
//> COLUMN_CONSTANTS
	/** Column name for {@link #date} */
	private static final String COLUMN_DATE = "date";
	/** Column name for {@link #status} */
	private static final String COLUMN_STATUS = "status";
	/** Column name for {@link #sender} */
	private static final String COLUMN_SENDER = "sender";
	/** Column name for {@link #recipients} */
	private static final String COLUMN_RECIPIENTS = "recipients";
	/** Column name for {@link #subject} */
	private static final String COLUMN_SUBJECT = "subject";
	/** Column name for {@link #content} */
	private static final String COLUMN_CONTENT = "content";
	
//> ENTITY FIELDS
	/** Details of the fields that this class has. */
	public enum Field implements EntityField<Email> {
		/** field mapping for {@link Email#date} */
		DATE(COLUMN_DATE),
		/** field mapping for {@link Email#status} */
		STATUS(COLUMN_STATUS),
		/** field mapping for {@link Email#sender} */
		FROM(COLUMN_SENDER),
		/** field mapping for {@link Email#recipients} */
		TO(COLUMN_RECIPIENTS),
		/** field mapping for {@link Email#subject} */
		SUBJECT(COLUMN_SUBJECT),
		/** field mapping for {@link Email#content} */
		EMAIL_CONTENT(COLUMN_CONTENT);
		/** name of a field */
		private final String fieldName;
		/**
		 * Creates a new {@link Field}
		 * @param fieldName name of the field
		 */
		Field(String fieldName) { this.fieldName = fieldName; }
		/** @see EntityField#getFieldName() */
		public String getFieldName() { return this.fieldName; }
	}
	
//> CONSTANTS
	/** Unique id for this entity.  This is for hibernate usage. */
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true,nullable=false,updatable=false)
	private long id;
	/** outgoing email that is created, and will be sent when the server is available */
	public static final int STATUS_OUTBOX = 2;
	/** outgoing email successfully delivered*/
	public static final int STATUS_SENT = 4;	
	/** outgoing email failed*/
	public static final int STATUS_FAILED = 9;	
	/** outgoing email pending*/
	public static final int STATUS_PENDING = 3;
	/** outgoing email re-trying*/
	public static final int STATUS_RETRYING = 6;

//> INSTANCE PROPERTIES
	/** Status of this email */
	@Column(name=COLUMN_STATUS)
	private int status;
	
	/** Subject line of the email */
	@Column(name=COLUMN_SUBJECT)
	private String subject;

	/** Content of the email */
	@Column(name=COLUMN_CONTENT)
	private String content;

	/** Date of the email */
	@Column(name=COLUMN_DATE)
	private Long date;

	/** Sender of the email */
	@ManyToOne(targetEntity=EmailAccount.class)
	private EmailAccount sender;

	/** Recipient of the email */
	@Column(name=COLUMN_RECIPIENTS)
	private String recipients;

//> CONSTRUCTORS
	/** Empty constructor required for hibernate. */
	Email() {}
	
	/**
	 * Creates an email with the supplied properties.
	 * @param from The account to send the email
	 * @param recipients The email recipients
	 * @param subject The email subject
	 * @param content The email content
	 */
	public Email(EmailAccount from, String recipients, String subject, String content) {
		this.sender = from;
		this.recipients = recipients;
		this.subject = subject;
		this.content = content;
	}
	
//> ACCESSOR METHODS
	/**
	 * @param subject new value for {@link #subject} 
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/** @param content new value for {@link #content} */
	public void setContent(String content) {
		this.content = content;
	}

	/** @param date new value for {@link #date} */
	public void setDate(Long date) {
		this.date = date;
	}

	/** @param sender new value for {@link #sender} */
	public void setSender(EmailAccount sender) {
		this.sender = sender;
	}

	/** @param recipients new value for {@link #recipients} */
	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}
	
	/**
	 * Gets the status of this Email.  Should be one of the Email.STATUS_ constants.
	 * @return {@link #status}
	 */
	public int getStatus() {
		return this.status;
	}
	
	/**
	 * sets the type of this Email.  Should be one of the Email.STATUS_ constants.
	 * @param messageStatus new value for {@link #status}
	 */
	public void setStatus(int messageStatus) {
		this.status = messageStatus;
	}

	/**
	 * Gets the text content of this email.
	 * @return {@link #content}
	 */
	public String getEmailContent() {
		return this.content;
	}
	
	/**
	 * Sets the date for this email.
	 * @param date
	 */
	public void setDate(long date) {
		this.date = date;
	}
	
	/**
	 * Gets this action email recipients.
	 * @return {@link #recipients}
	 */
	public String getEmailRecipients() {
		return this.recipients;
	}

	/**
	 * Gets the email account related to this email. This is the sender.
	 * @return {@link #sender}
	 */
	public EmailAccount getEmailFrom() {
		return this.sender;
	}

	/**
	 * Gets this action email subject.
	 * @return  {@link #subject}
	 */
	public String getEmailSubject() {
		return this.subject;
	}

	/**
	 * Gets the date at which this email was sent.
	 * @return {@link #date}
	 */
	public long getDate() {
		return this.date;
	}

//> GENERATED CODE
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result
				+ ((recipients == null) ? 0 : recipients.hashCode());
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result + status;
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		return result;
	}

	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Email other = (Email) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (id != other.id)
			return false;
		if (recipients == null) {
			if (other.recipients != null)
				return false;
		} else if (!recipients.equals(other.recipients))
			return false;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		if (status != other.status)
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		return true;
	}
}