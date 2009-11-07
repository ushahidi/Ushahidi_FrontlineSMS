/**
 * 
 */
package net.frontlinesms;

import java.awt.event.ActionListener;

/**
 * Event listener for events in the {@link DatabaseConnectionTester}.
 * @author Alex
 */
public interface DatabaseConnectionTesterEventListener extends ActionListener {
//> STATIC CONSTANTS
	/** Action: beginning database connection test. */
	int ACTION_STARTING_CHECK = 1;
	/** Action: database connection test was successful. */
	int ACTION_CHECK_SUCCEEDED = 2;
	/** Action: database connection test failed. */
	int ACTION_CHECK_FAILED = 3;
	/** Action: going to sleep before performing test again.  Check command for length of time in millis until next try */
	int ACTION_SLEEPING = 4;

//> INSTANCE PROPERTIES

//> CONSTRUCTORS

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
