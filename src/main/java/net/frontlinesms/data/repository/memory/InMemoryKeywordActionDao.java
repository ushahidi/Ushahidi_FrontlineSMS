/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import java.util.*;

import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.repository.*;

/**
 * In-memory implementation of {@link KeywordActionDao}
 * @author Alex
 */
public class InMemoryKeywordActionDao implements KeywordActionDao {
	/** All keyword actions in this data source */
	private HashSet<KeywordAction> allActions = new HashSet<KeywordAction>();
	
	/** @see KeywordActionDao#deleteKeywordAction(KeywordAction) */
	public void deleteKeywordAction(KeywordAction action) {
		this.allActions.remove(action);
	}

	/** @see KeywordActionDao#getReplyActions() */
	public Collection<KeywordAction> getReplyActions() {
		HashSet<KeywordAction> replies = new HashSet<KeywordAction>();
		for(KeywordAction action : this.allActions.toArray(new KeywordAction[0])) {
			if(action.getType() == KeywordAction.TYPE_REPLY) {
				replies.add(action);
			}
		}
		return replies;
	}

	/** @see KeywordActionDao#getSurveysActions() */
	public Collection<KeywordAction> getSurveysActions() {
		HashSet<KeywordAction> replies = new HashSet<KeywordAction>();
		for(KeywordAction action : this.allActions.toArray(new KeywordAction[0])) {
			if(action.getType() == KeywordAction.TYPE_SURVEY) {
				replies.add(action);
			}
		}
		return replies;
	}

	/** @see KeywordActionDao#saveKeywordAction(KeywordAction) */
	public void saveKeywordAction(KeywordAction action) {
		this.allActions.add(action);
	}

}
