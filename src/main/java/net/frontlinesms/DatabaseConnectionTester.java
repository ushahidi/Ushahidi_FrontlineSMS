/**
 * 
 */
package net.frontlinesms;

import java.awt.event.ActionEvent;

import net.frontlinesms.data.repository.ContactDao;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * 
 * @author Alex
 */
public class DatabaseConnectionTester {

//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	/** The DAO we will use to test the database connection. */
	private final ContactDao contactDao;
	/** Listener for events on this class. */
	private DatabaseConnectionTesterEventListener eventListener;

//> CONSTRUCTORS
	/**
	 * Create a new {@link DatabaseConnectionTester}.
	 * @param frontlineSms 
	 */
	public DatabaseConnectionTester(FrontlineSMS frontlineSms) {
		// Cache the contact DAO so we can run tests using it.
		this.contactDao = frontlineSms.getContactDao();
		assert(this.contactDao != null) : "The " + this.getClass() + " requires a " + ContactDao.class.getSimpleName() + " to function correctly.";
	}

//> ACCESSORS
	/** @param eventListener value for {@link #eventListener} */
	public void setEventListener(DatabaseConnectionTesterEventListener eventListener) {
		this.eventListener = eventListener;
	}

//> INSTANCE HELPER METHODS
	/**
	 * Checks if the database can connect successfully.
	 * @return <code>true</code> if database connection is successful, <code>false</code> otherwise
	 */
	private boolean checkConnection() {
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
		int retryInMillis = 1000;
		while(!checkConnection()) {
			System.out.println("Connection failed.  Will retry in " + retryInMillis + "ms"); // FIXME remove sysout
			// TODO we should display a UI to the user with the following
			// 1. details of the connection problem
			// 2. the option to change database settings
			// 3. a button to attempt reconnection
			sleep(retryInMillis);
			// Double the time til the next retry.
			retryInMillis <<= 1;
		}
		// Connected successfully, so it's now safe to return
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