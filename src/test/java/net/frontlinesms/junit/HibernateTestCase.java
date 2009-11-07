/**
 * 
 */
package net.frontlinesms.junit;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Base class for transactional tests.
 * @author Alex
 */
public abstract class HibernateTestCase extends AbstractDependencyInjectionSpringContextTests {

//> TEST METHODS
	/**
	 * Run tests for this class 
	 * @throws Throwable if there was an unhandled problem running the test
	 */
	public abstract void test() throws Throwable;

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
