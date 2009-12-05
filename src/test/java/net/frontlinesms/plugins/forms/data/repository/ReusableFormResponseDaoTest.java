/**
 * 
 */
package net.frontlinesms.plugins.forms.data.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.frontlinesms.junit.ReusableTestCase;
import net.frontlinesms.plugins.forms.data.domain.*;

/**
 * Class for testing different implementations of {@link FormResponse}.
 * @author Alex
 */
public abstract class ReusableFormResponseDaoTest extends ReusableTestCase<FormResponse> {
	/** DAO which we are testing */
	protected FormResponseDao dao;
	/** Logging object */
	protected final Log log = LogFactory.getLog(getClass());

	/** @param dao new value for {@link #dao} */
	public void setDao(FormResponseDao dao) {
		this.dao = dao;
	}

	/** @see junit.framework.TestCase#tearDown() */
	@Override
	public void tearDown() throws Exception {
		this.dao = null;
	}
	
	/** Test everything all at once! */
	public void test() throws Throwable {
	}
}
