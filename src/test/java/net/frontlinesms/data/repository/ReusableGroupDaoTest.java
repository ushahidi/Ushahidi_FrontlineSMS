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
	
//> STATIC PROPERTIES 
	private static final String MY_GROUP_NAME = "My Group";
	
	private static final String CHILD_GROUP_NAME = "Child Group";
	
//> INSTANCE PROPERTIES
	/** Instance of this DAO implementation we are testing. */
	private GroupDao dao;
	/** Logging object */
	private final Log log = LogFactory.getLog(getClass());

	public void setDao(GroupDao dao) {
		this.dao = dao;
	}
	
	@Override
	public void tearDown() throws Exception {
		for(Group g : this.dao.getAllGroups()) {
			this.dao.deleteGroup(g, false);
		}
		this.dao = null;
	}
	
//> JUNIT TEST METHODS	
	/**
	 * Test everything all at once!
	 * @throws DuplicateKeyException 
	 */
	public void test() throws DuplicateKeyException {
		confirmSanity();
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
		
		group = new Group(null, MY_GROUP_NAME);
		dao.saveGroup(group);
		
		assertEquals(1, dao.getGroupCount());
		
		Group childGroup = new Group(group, CHILD_GROUP_NAME);
		dao.saveGroup(childGroup);
		
		assertEquals(2, dao.getGroupCount());
		
		confirmSanity();

		assertEquals(group, dao.getGroupByName(MY_GROUP_NAME));
		assertEquals(childGroup, dao.getGroupByName(CHILD_GROUP_NAME));

		dao.deleteGroup(childGroup, false);
		dao.deleteGroup(group, false);
		
		assertEquals(0, dao.getGroupCount());
		
		confirmSanity();
	}
	
	public void testChildDelete() throws DuplicateKeyException {
		Group group = new Group(null, MY_GROUP_NAME);
		confirmSanity();
		dao.saveGroup(group);
		confirmSanity();

		Group childGroup = new Group(group, CHILD_GROUP_NAME);
		dao.saveGroup(childGroup);

		assertEquals(group, dao.getGroupByName(MY_GROUP_NAME));
		assertEquals(childGroup, dao.getGroupByName(CHILD_GROUP_NAME));
		
		assertEquals(2, dao.getGroupCount());
		
		confirmSanity();
		
		dao.deleteGroup(childGroup, false);
		assertEquals(1, dao.getGroupCount());
		confirmSanity();
		
		dao.deleteGroup(group, false);
		assertEquals(0, dao.getGroupCount());
		confirmSanity();
	}
	
	public void testCascadingDelete() throws DuplicateKeyException {
		Group group = new Group(null, MY_GROUP_NAME);
		confirmSanity();
		dao.saveGroup(group);
		confirmSanity();

		Group childGroup = new Group(group, CHILD_GROUP_NAME);
		dao.saveGroup(childGroup);

		assertEquals(group, dao.getGroupByName(MY_GROUP_NAME));
		assertEquals(childGroup, dao.getGroupByName(CHILD_GROUP_NAME));
		
		assertEquals(2, dao.getGroupCount());
		
		confirmSanity();
		
		dao.deleteGroup(group, false);
		
		assertEquals(0, dao.getGroupCount());
		
		confirmSanity();
	}

	/** Checks some basic facts about the state of the DAO */
	private void confirmSanity() {
		assertEquals(dao.getAllGroups(), dao.getAllGroups(0, Integer.MAX_VALUE));
		assertEquals(dao.getGroupCount(), dao.getAllGroups().size());
		assertNull("Group should not exist.", dao.getGroupByName("I just invented this name"));
	}
}
