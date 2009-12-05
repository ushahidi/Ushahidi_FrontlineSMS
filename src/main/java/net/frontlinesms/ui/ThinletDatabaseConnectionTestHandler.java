/**
 * 
 */
package net.frontlinesms.ui;

import thinlet.FrameLauncher;
import net.frontlinesms.data.DatabaseConnectionFailedException;
import net.frontlinesms.data.DatabaseConnectionTestHandler;
import net.frontlinesms.data.DatabaseConnectionTester;

/**
 * UI Controller used when there is a problem 
 * @author Alex
 */
@SuppressWarnings("serial")
public class ThinletDatabaseConnectionTestHandler extends FrontlineUI implements DatabaseConnectionTestHandler {
	
//> STATIC CONSTANTS
	/** Thinlet UI File: the dialog for the user to attempt reconnection or modify database settings */
	private static final String UI_FILE_CONNECTION_PROBLEM_DIALOG = "/ui/core/database/dgConnectionProblem.xml";

//> INSTANCE PROPERTIES
	/** Lock for {@link #ensureConnected(DatabaseConnectionTester)} while the UI is handling connection. */
	private final Object CONNECTING_LOCK = new Object();
	/** For {@link #ensureConnected(DatabaseConnectionTester)} to check if it should keep blocking. */
	private boolean keepBlocking;
	/** Conneciton tester. */
	private DatabaseConnectionTester connectionTester;

//> CONSTRUCTORS
	/** Create a new instance of this class. */
	public ThinletDatabaseConnectionTestHandler() {}
	
//> TEST METHODS
	/** @see DatabaseConnectionTestHandler#ensureConnected(DatabaseConnectionTester) */
	public void ensureConnected(DatabaseConnectionTester connectionTester) throws DatabaseConnectionFailedException {
		if(this.connectionTester != null) throw new IllegalStateException("DatabaseConnectionTestHandler.ensureConnected should only be called once.");
		this.connectionTester = connectionTester;
		
		// Attempt to connect three times, waiting longer between each one
		if(this.connectionTester.checkConnection()) return;
		sleep(1000);
		if(this.connectionTester.checkConnection()) return;
		sleep(2000);
		if(this.connectionTester.checkConnection()) return;
		sleep(4000);
		if(this.connectionTester.checkConnection()) {
			// The connection is working fine.
			return;
		} else {
			// Display the UI for re-attempting connection
			Object problemDialog = loadComponentFromFile(UI_FILE_CONNECTION_PROBLEM_DIALOG);
			this.add(problemDialog);
			
			frameLauncher = new FrameLauncher("Database Connection Problem", this, 510, 380, getIcon(Icon.FRONTLINE_ICON));
			frameLauncher.setResizable(false);
			
			// Wait until the UI has triggered a successful connection attempt
			synchronized(CONNECTING_LOCK) {
				keepBlocking = true;
				while(keepBlocking) {
					try {
						CONNECTING_LOCK.wait();
					} catch (InterruptedException ex) {}
				}
			}
		}
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS
	
//> PUBLIC UI METHODS
	/** Attempt to reconnect with the current settings. */
	public void reconnect() {
		System.out.println("ThinletDatabaseConnectionTestHandler.reconnect() : ENTRY");
		if(this.connectionTester.checkConnection()) {
			// We've connected successfully, so wake up the sleeping thread and give control back to it
			synchronized (CONNECTING_LOCK) {
				this.keepBlocking = false;
				CONNECTING_LOCK.notify();
			}
			this.frameLauncher.dispose();
		}
		System.out.println("ThinletDatabaseConnectionTestHandler.reconnect() : EXIT");
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	/**
	 * Put the thread to sleep.
	 * @param millis number of milliseconds to sleep for 
	 */
	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
