/**
 * 
 */
package net.frontlinesms.plugins.forms.data.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.frontlinesms.junit.ReusableTestCase;
import net.frontlinesms.plugins.forms.data.domain.*;

/**
 * Class for testing different implementations of {@link FormDao}.
 * @author Alex
 */
public abstract class ReusableFormDaoTest extends ReusableTestCase<Form> {
	/** DAO which we are testing */
	protected FormDao dao;
	/** Logging object */
	protected final Log log = LogFactory.getLog(getClass());

	/** @param dao new value for {@link #dao} */
	public void setDao(FormDao dao) {
		this.dao = dao;
	}
	
	/** @see junit.framework.TestCase#tearDown() */
	@Override
	protected void tearDown() throws Exception {
		this.dao = null;
	}
	
	/** Test everything all at once! */
	public void test() throws Throwable {
		assertEquals(0, dao.getAllForms().size());
		
		Form myFirstForm = new Form("My First Form");
		myFirstForm.addField(new FormField(myFirstForm, FormFieldType.WRAPPED_TEXT, "Here is the introductory text to my first form."));
		this.dao.saveForm(myFirstForm);
		
		assertEquals(1, dao.getAllForms().size());
	}
}
