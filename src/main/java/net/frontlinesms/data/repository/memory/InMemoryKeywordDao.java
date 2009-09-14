/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Keyword;
import net.frontlinesms.data.repository.KeywordDao;

/**
 * In-memory implementation of {@link KeywordDao}.
 * @author Alex
 */
public class InMemoryKeywordDao implements KeywordDao {
	/** All the keywords that we have saved. */
	private HashSet<Keyword> allKeywords = new HashSet<Keyword>();
	
	/** @see KeywordDao#createKeywordsHierarchically(String[], String, boolean) */
	public Keyword createKeywordsHierarchically(String[] keywordHierarchy, String description, boolean classicMode) throws DuplicateKeyException {
		/** Bottom keyword already created */
		Keyword root = null;
		/** number of words in the hierarchy to skip */
		int skip = 0;
		for(Keyword k : getRootKeywords()) {
			if(k.getKeyword().equals(keywordHierarchy[0].toUpperCase())) {
				root = k;
				skip = 1;
			}
		}
		
		if(root != null) {
			for(; skip < keywordHierarchy.length; skip++) {
				check:for(Keyword k : root.getAllSubWords()) {
					if(k.getKeyword().equals(keywordHierarchy[skip+1].toUpperCase())) {
						root = k;
						break check;
					}
				}
			}
		
			if(skip == keywordHierarchy.length - 1) {
				for(Keyword k : root.getAllSubWords()) {
					if(k.getKeyword().equals(keywordHierarchy[skip].toUpperCase())) {
						return k;
					}
				}
			}
		}
		
		String[] subHierarchy = new String[keywordHierarchy.length - skip];
		for (int i = skip; i < keywordHierarchy.length; i++) {
			subHierarchy[i-skip] = keywordHierarchy[i];
		}
		for(String s : subHierarchy) {
			root = new Keyword(root, s, description);
			this.saveKeyword(root);
		}
		
		return root;
	}

	/** @see KeywordDao#deleteKeyword(Keyword) */
	public void deleteKeyword(Keyword keyword) {
		for(Keyword child : keyword.getAllSubWords()) {
			this.deleteKeyword(child);
		}
		this.allKeywords.remove(keyword);
	}

	/** @see KeywordDao#getAllKeywords() */
	public List<Keyword> getAllKeywords() {
		TreeMap<String, Keyword> sortedKeywords = new TreeMap<String, Keyword>();
		for(Keyword k : allKeywords) {
			sortedKeywords.put(k.getKeywordString(), k);
		}
		ArrayList<Keyword> keywordList = new ArrayList<Keyword>();
		keywordList.addAll(sortedKeywords.values());
		return keywordList;
	}

	/** @see net.frontlinesms.data.repository.KeywordDao#getAllKeywords(int, int) */
	public List<Keyword> getAllKeywords(int startIndex, int limit) {
		List<Keyword> allKeywords = this.getAllKeywords();
		return allKeywords.subList(startIndex, Math.min(allKeywords.size(), startIndex+limit));
	}

	/** @see net.frontlinesms.data.repository.KeywordDao#getFromMessageText(java.lang.String) */
	public Keyword getFromMessageText(String messageText) {
		Keyword last = null;
		Keyword current = null;
		Collection<Keyword> currentChildren = getRootKeywords();
		while(true) {
			current = getFromMessageText(messageText, currentChildren);
			if(current == null) {
				return last;
			} else {
				last = current;
				currentChildren = current.getDirectSubWords();
			}
		}
	}
	
	/**
	 * Gets the keyword from a supplied group who matches a message's text.  This method
	 * returns the first match, so should only be called on keywords of the same level
	 * as each other, rather than e.g. {@link #allKeywords}
	 * @param messageText
	 * @param possibleKeywords
	 * @return first keyword that matches the message text
	 */
	private Keyword getFromMessageText(String messageText, Collection<Keyword> possibleKeywords) {
		for(Keyword k : possibleKeywords) {
			if(k.matches(messageText)) {
				return k;
			}
		}
		return null;
	}

	/** @see KeywordDao#getPageNumber(Keyword, int) */
	public int getPageNumber(Keyword keyword, int keywordsPerPage) {
		return getAllKeywords().indexOf(keyword) / keywordsPerPage;
	}

	/** @see net.frontlinesms.data.repository.KeywordDao#getRootKeywords() */
	public List<Keyword> getRootKeywords() {
		List<Keyword> keywords = getAllKeywords();
		for(Keyword k : keywords.toArray(new Keyword[0])) {
			if(k.getParent() != null) {
				keywords.remove(k);
			}
		}
		return keywords;
	}

	/** @see net.frontlinesms.data.repository.KeywordDao#getTotalKeywordCount() */
	public int getTotalKeywordCount() {
		return this.allKeywords.size();
	}

	/** @see KeywordDao#saveKeyword(Keyword) */
	public void saveKeyword(Keyword keyword) throws DuplicateKeyException {
		if(!this.allKeywords.add(keyword)) {
			throw new DuplicateKeyException();
		}
	}

}
