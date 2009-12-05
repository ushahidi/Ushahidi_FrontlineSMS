/**
 * 
 */
package net.frontlinesms.junit;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * Base class for transactional tests.
 * @author Alex
 */
public abstract class HibernateTestCase extends AbstractTransactionalDataSourceSpringContextTests {

//> TEST METHODS
	/**
	 * Run tests for this class 
	 * @throws Throwable if there was an unhandled problem running the test
	 */
	public abstract void test() throws Throwable;

//> TEST SETUP/TEARDOWN
	/**
	 * Implement cleaning of the DAO being tested, as otherwise it may end up with junk in it.
	 * @throws Exception If there is a problem tearing down
	 */
	public abstract void doTearDown() throws Exception;

	/** @see org.springframework.test.AbstractTransactionalSpringContextTests#onTearDown() */
	@Override
	protected void onTearDown() throws Exception {
		super.onTearDown();
		doTearDown();
	}

//> SETUP
	/**
	 * Gets a Spring resource location from the classpath, derived from the package
	 * in which the extending implementation of this class exists. 
	 * @return classpath location of XML database config
	 */
	@Override
	protected String[] getConfigLocations() {
		String resourcePath = "classpath:" + getClass().getName().replace('.', '/') + '.' + "xml";
		return new String[] { resourcePath };
	}
}
