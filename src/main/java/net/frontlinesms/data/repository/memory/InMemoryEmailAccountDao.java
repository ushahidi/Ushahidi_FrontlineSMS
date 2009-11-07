/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import java.util.Collection;
import java.util.HashMap;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.EmailAccount;
import net.frontlinesms.data.repository.EmailAccountDao;

/**
 * In-memory implementation of {@link EmailAccountDao}.
 * @author Alex
 */
public class InMemoryEmailAccountDao implements EmailAccountDao {
	/** all the saved email accounts */
	private final HashMap<String, EmailAccount> accounts = new HashMap<String, EmailAccount>();
	
	/** @see net.frontlinesms.data.repository.EmailAccountDao#deleteEmailAccount(net.frontlinesms.data.domain.EmailAccount) */
	public void deleteEmailAccount(EmailAccount account) {
		this.accounts.remove(account.getAccountName());
	}

	/** @see net.frontlinesms.data.repository.EmailAccountDao#getAllEmailAccounts() */
	public Collection<EmailAccount> getAllEmailAccounts() {
		return this.accounts.values();
	}

	/** @throws DuplicateKeyException 
	 * @see EmailAccountDao#saveEmailAccount(EmailAccount) */
	public void saveEmailAccount(EmailAccount account) throws DuplicateKeyException {
		if(this.accounts.containsKey(account.getAccountName())
				|| this.accounts.put(account.getAccountName(), account) != null) {
			throw new DuplicateKeyException();
		}
	}

	/** @see EmailAccountDao#updateEmailAccount(EmailAccount) */
	public void updateEmailAccount(EmailAccount account) {
		// do nothing
	}

}
