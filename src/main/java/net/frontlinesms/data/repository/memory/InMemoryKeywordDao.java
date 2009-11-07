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
	
	/** @see KeywordDao#deleteKeyword(Keyword) */
	public void deleteKeyword(Keyword keyword) {
		for(Keyword child : getAllSubWords(keyword)) {
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
				currentChildren = getDirectSubWords(current);
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
	
	/**
	 * Gets all direct children of a keyword.
	 * @param keyword
	 * @return all direct children of the supplied keyword
	 */
	private Collection<Keyword> getDirectSubWords(Keyword keyword) {
		HashSet<Keyword> subwords = new HashSet<Keyword>();
		for(Keyword k : this.getAllKeywords()) {
			if(keyword.equals(k.getParent())) {
				subwords.add(k);
			}
		}
		return subwords;
	}

	/**
	 * Gets all subwords of the supplied keyword.
	 * @param keyword
	 * @return all subwords of the supplied keyword.
	 */
	private Collection<Keyword> getAllSubWords(Keyword keyword) {
		HashSet<Keyword> subwords = new HashSet<Keyword>();
		getAllSubwords(subwords, keyword);
		return subwords;
	}

	/**
	 * Gets all subwords of the supplied keyword, and inserts them into the supplied set.
	 * @param subwords
	 * @param keyword
	 */
	private void getAllSubwords(HashSet<Keyword> subwords, Keyword keyword) {
		for(Keyword k : this.getAllKeywords()) {
			if(k.getParent().equals(keyword)) {
				subwords.add(k);
				getAllSubwords(subwords, k);
			}
		}
	}
}
