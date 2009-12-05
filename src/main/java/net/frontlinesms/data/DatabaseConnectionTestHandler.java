/**
 * 
 */
package net.frontlinesms.data;

/**
 * Class for handling failed database connection tests.
 * @author Alex
 */
public interface DatabaseConnectionTestHandler {
	/**
	 * Ensure that the supplied databaseConnectionTester is working.  This method will block until the database is successfully connected to. 
	 * @param databaseConnectionTester 
	 * @throws DatabaseConnectionFailedException If a successful database connection cannot be made.
	 */
	void ensureConnected(DatabaseConnectionTester databaseConnectionTester) throws DatabaseConnectionFailedException;
}
