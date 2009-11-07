/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.Collection;
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
		if(destroyContacts) {
			// TODO delete contacts here
		}
		
		// Remove the group from its parent, as hibernate does not seem to deal with this
		Group parent = group.getParent();
		if(parent != null) {
			parent.removeChild(group);
		}
		
		super.delete(group);
	}
	
	/** @see GroupDao#getAllGroups() */
	public List<Group> getAllGroups() {
		return super.getAll();
	}
	
	/** @see GroupDao#getChildGroups(Group) */
	public Collection<Group> getChildGroups(Group parent) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(getEqualsOrNull(Group.Field.PARENT, parent));
		return super.getList(criteria);
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
		// TODO Do this better - this isn't hugely efficient
		return super.getAll().indexOf(group) / groupsPerPage;
	}

	/** @see GroupDao#saveGroup(Group) */
	public void saveGroup(Group group) throws DuplicateKeyException {
		if(group.getParent() == null) {
			// Check there is not already a top-level group with this name.  We do this here as SQL does not
			// consider NULL == NULL, so top-level groups with the same name are allowed in the database.
			// TODO this check/save operation would ideally be atomic - what if someone snuck in and created
			// this group between the check and the creation?
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.isNull(Group.Field.PARENT.getFieldName()));
			criteria.add(Restrictions.eq(Group.Field.NAME.getFieldName(), group.getName()));
			if(super.getUnique(criteria) != null) {
				throw new DuplicateKeyException();
			}
		}
		super.save(group);
	}

	/** @see GroupDao#updateGroup(Group) */
	public void updateGroup(Group group) {
		super.updateWithoutDuplicateHandling(group);
	}

}
