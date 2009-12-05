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

	/** @see KeywordActionDao#updateKeywordAction(KeywordAction) */
	public void updateKeywordAction(KeywordAction action) {
		// Do nothing!
	}

	/** @see net.frontlinesms.data.repository.KeywordActionDao#getAction(net.frontlinesms.data.domain.Keyword, int) */
	public KeywordAction getAction(Keyword keyword, int actionType) {
		for(KeywordAction action : this.allActions) {
			if(action.getType() == actionType && action.getKeyword().equals(keyword)) {
				return action;
			}
		}
		return null;
	}

	/** @see net.frontlinesms.data.repository.KeywordActionDao#getActions(net.frontlinesms.data.domain.Keyword) */
	public List<KeywordAction> getActions(Keyword keyword) {
		ArrayList<KeywordAction> actions = new ArrayList<KeywordAction>();
		for(KeywordAction action : this.allActions) {
			if(action.getKeyword().equals(keyword)) {
				actions.add(action);
			}
		}
		return actions;
	}

}
