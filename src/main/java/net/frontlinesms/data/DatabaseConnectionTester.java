/**
 * 
 */
package net.frontlinesms.data;

import java.awt.event.ActionEvent;

import net.frontlinesms.data.repository.ContactDao;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * 
 * @author Alex
 */
public class DatabaseConnectionTester {

//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	/** The DAO we will use to test the database connection. */
	private ContactDao contactDao;
	/** Handler for test results. */
	private DatabaseConnectionTestHandler testHandler;
	/** Listener for events on this class. */
	private DatabaseConnectionTesterEventListener eventListener;

//> CONSTRUCTORS
	/**
	 * Create a new {@link DatabaseConnectionTester}.
	 */
	public DatabaseConnectionTester() {}

//> ACCESSORS
	/** @param eventListener value for {@link #eventListener} */
	public void setEventListener(DatabaseConnectionTesterEventListener eventListener) {
		this.eventListener = eventListener;
	}
	/** @param contactDao value for {@link #contactDao} */
	public void setContactDao(ContactDao contactDao) {
		this.contactDao = contactDao;
	}
	/** @param testHandler value for {@link #testHandler} */
	@Required
	public void setTestHandler(DatabaseConnectionTestHandler testHandler) {
		this.testHandler = testHandler;
	}

//> INSTANCE HELPER METHODS
	/**
	 * Checks if the database can connect successfully.
	 * @return <code>true</code> if database connection is successful, <code>false</code> otherwise
	 */
	public boolean checkConnection() {
		if(this.eventListener != null) this.eventListener.actionPerformed(new ActionEvent(this, DatabaseConnectionTesterEventListener.ACTION_STARTING_CHECK, null));
		try {
			// Attempt to get a contact from the database.  Whether they exist or not
			// isn't so important - really we are testing the connection.
			this.contactDao.getContactByName("test");
			if(this.eventListener != null) this.eventListener.actionPerformed(new ActionEvent(this, DatabaseConnectionTesterEventListener.ACTION_CHECK_SUCCEEDED, null));
			return true;
		} catch(DataAccessResourceFailureException ex) {
			ex.printStackTrace(); // FIXME remove stacktrace
			// TODO log this properly
			if(this.eventListener != null) this.eventListener.actionPerformed(new ActionEvent(this, DatabaseConnectionTesterEventListener.ACTION_CHECK_FAILED, ex.getMessage()));
			return false;
		}
	}

	/**
	 * Blocking method which will check that the database can connect.  If connection fails, this will be conveyed
	 * to the user, who can then re-attempt connection, or modify their settings.
	 */
	public void ensureConnected() {
		this.testHandler.ensureConnected(this);
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	/**
	 * Sleeps the thread for the requested number of millis.  If the thread is interrupted, it is ignored.
	 * @param millis passed to {@link Thread#sleep(long)}
	 */
	private void sleep(long millis) {
		try {
			if(this.eventListener != null) this.eventListener.actionPerformed(new ActionEvent(this, DatabaseConnectionTesterEventListener.ACTION_SLEEPING, Long.toString(millis)));
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			/* ignore this */
		}	
	}
}