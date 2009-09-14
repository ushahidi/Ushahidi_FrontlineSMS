/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Group;
import net.frontlinesms.data.repository.GroupDao;

/**
 * Hibernate implementation of {@link GroupDao}.
 * @author Alex
 */
public class HibernateGroupDao extends BaseHibernateDao<Group> implements GroupDao {
	/** Create instance of this class */
	public HibernateGroupDao() {
		super(Group.class);
	}

	/** @see GroupDao#deleteGroup(Group, boolean) */
	public void deleteGroup(Group group, boolean destroyContacts) {
		for(Group child : group.getDirectSubGroups()) {
			this.deleteGroup(child, destroyContacts);
		}
		if(destroyContacts) {
			// TODO delete contacts here
		}
		super.delete(group);
	}
	
	/** @see GroupDao#getAllGroups() */
	public List<Group> getAllGroups() {
		return super.getAll();
	}

	/** @see GroupDao#getAllGroups(int, int) */
	public List<Group> getAllGroups(int startIndex, int limit) {
		return super.getAll(startIndex, limit);
	}

	/** @see GroupDao#getGroupByName(String) */
	public Group getGroupByName(String name) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(Group.Field.NAME.getFieldName(), name));
		return super.getUnique(criteria);
	}

	/** @see GroupDao#getGroupCount() */
	public int getGroupCount() {
		return super.countAll();
	}

	/** @see GroupDao#getPageNumber(Group, int) */
	public int getPageNumber(Group group, int groupsPerPage) {
		// TODO Do this better
		return super.getAll().indexOf(group) / groupsPerPage;
	}

	/** @see GroupDao#saveGroup(Group) */
	public void saveGroup(Group group) throws DuplicateKeyException {
		super.save(group);
	}

	/** @see GroupDao#updateGroup(Group) */
	public void updateGroup(Group group) {
		super.updateWithoutDuplicateHandling(group);
	}

}
