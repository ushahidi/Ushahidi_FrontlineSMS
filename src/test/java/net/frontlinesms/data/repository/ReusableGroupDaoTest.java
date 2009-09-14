/**
 * 
 */
package net.frontlinesms.data.repository;


import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Group;
import net.frontlinesms.junit.ReusableTestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Base test class for testing {@link GroupDao}
 * @author Alex
 */
public abstract class ReusableGroupDaoTest extends ReusableTestCase<Group> {
	/** Instance of this DAO implementation we are testing. */
	private GroupDao dao;
	/** Logging object */
	private final Log log = LogFactory.getLog(getClass());

	public void setDao(GroupDao dao) {
		this.dao = dao;
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.dao = null;
	}
	
	/**
	 * Test everything all at once!
	 * @throws DuplicateKeyException 
	 */
	public void test() throws DuplicateKeyException {
		confirmSanity();
		final String MY_GROUP_NAME = "My Group";
		Group group = new Group(null, MY_GROUP_NAME);
		confirmSanity();
		dao.saveGroup(group);
		confirmSanity();
		
		Group duplicateGroup = new Group(null, MY_GROUP_NAME);
		try {
			dao.saveGroup(duplicateGroup);
			fail("Saving duplicate group should have failed.");
		} catch(DuplicateKeyException ex) { /* expected */ }
		
		confirmSanity();
		
		assertEquals(group, dao.getGroupByName(MY_GROUP_NAME));

		dao.deleteGroup(group, false);
		
		confirmSanity();
		
		assertNull("My group should have been deleted.", dao.getGroupByName(MY_GROUP_NAME));
		
		dao.saveGroup(group);
		
		assertEquals(1, dao.getGroupCount());
		
		final String CHILD_GROUP_NAME = "Child Group";
		Group childGroup = new Group(group, CHILD_GROUP_NAME);
		dao.saveGroup(childGroup);
		
		assertEquals(2, dao.getGroupCount());
		
		confirmSanity();

		assertEquals(group, dao.getGroupByName(MY_GROUP_NAME));
		assertEquals(childGroup, dao.getGroupByName(CHILD_GROUP_NAME));
		
		dao.deleteGroup(group, false);
		
		assertEquals(0, dao.getGroupCount());
	}

	/** Checks some basic facts about the state of the DAO */
	private void confirmSanity() {
		assertEquals(dao.getAllGroups(), dao.getAllGroups(0, Integer.MAX_VALUE));
		assertEquals(dao.getGroupCount(), dao.getAllGroups().size());
		assertNull("Group should not exist.", dao.getGroupByName("I just invented this name"));
	}
}
