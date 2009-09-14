/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.Collection;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.EmailAccount;
import net.frontlinesms.data.repository.EmailAccountDao;

/**
 * In-memory implementation of {@link EmailAccountDao}.
 * @author Alex
 */
public class HibernateEmailAccountDao extends BaseHibernateDao<EmailAccount> implements EmailAccountDao {
	/** Create a new instance of this class */
	public HibernateEmailAccountDao() {
		super(EmailAccount.class);
	}
	
	/** @see EmailAccountDao#deleteEmailAccount(EmailAccount) */
	public void deleteEmailAccount(EmailAccount account) {
		this.getHibernateTemplate().delete(account);
	}

	/** @see EmailAccountDao#getAllEmailAccounts() */
	public Collection<EmailAccount> getAllEmailAccounts() {
		return super.getAll();
	}

	/** @see EmailAccountDao#saveEmailAccount(EmailAccount) */
	public void saveEmailAccount(EmailAccount account) throws DuplicateKeyException {
		super.save(account);
	}

	/** @see EmailAccountDao#updateEmailAccount(EmailAccount) */
	public void updateEmailAccount(EmailAccount account) throws DuplicateKeyException {
		super.update(account);
	}

}
