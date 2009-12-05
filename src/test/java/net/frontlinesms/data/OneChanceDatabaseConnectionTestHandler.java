/**
 * 
 */
package net.frontlinesms.data;

import net.frontlinesms.data.DatabaseConnectionFailedException;
import net.frontlinesms.data.DatabaseConnectionTestHandler;
import net.frontlinesms.data.DatabaseConnectionTester;

/**
 * Implementation of {@link DatabaseConnectionTestHandler} which will attempt to connect once and if this
 * does not work, it will fail with no retries.
 * @author Alex
 */
public class OneChanceDatabaseConnectionTestHandler implements DatabaseConnectionTestHandler {

//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS

//> ACCESSORS
	
//> INSTANCE METHODS
	/** @see net.frontlinesms.data.DatabaseConnectionTestHandler#ensureConnected(net.frontlinesms.data.DatabaseConnectionTester) */
	public void ensureConnected(DatabaseConnectionTester databaseConnectionTester) throws DatabaseConnectionFailedException {
		if(databaseConnectionTester.checkConnection()) return;
		else throw new DatabaseConnectionFailedException("Database connection could not be established on the first attempt.");
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
