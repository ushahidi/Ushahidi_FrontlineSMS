/**
 * 
 */
package net.frontlinesms.junit;

import java.util.Collection;

import junit.framework.TestCase;

/**
 * TODO this class may only be necessary because .equals() needs to be implemented on @Entities
 * @author Alex
 * @param <T> 
 */
public abstract class ReusableTestCase<T> extends TestCase {
	/**
	 * Compares two entities of type T.
	 * @param one
	 * @param two
	 */
	public void assertEquals(T one, T two) {
		assertNotNull(one);
		assertNotNull(two);
		assertTrue(one.equals(two));
	}
	
	/**
	 * Compares two collections of type T.
	 * @param one
	 * @param two
	 */
	@SuppressWarnings("unchecked")
	public void assertEquals(Collection<T> one, Collection<T> two) {
		assertNotNull(one);
		assertNotNull("Other " + one.getClass() + " is null.", two);
		assertEquals(one.size(), two.size());
		Object[] arrayOne = one.toArray();
		Object[] arrayTwo = two.toArray();
		for(int i=0; i<one.size(); ++i) {
			assertEquals((T)arrayOne[i], (T)arrayTwo[i]);
		}
	}

	/** Increase the visibility of {@link TestCase#tearDown()} */
	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * The method that does the tests.
	 * @throws Throwable if there was an unexpected problem during the tests 
	 */
	public abstract void test() throws Throwable;
}
