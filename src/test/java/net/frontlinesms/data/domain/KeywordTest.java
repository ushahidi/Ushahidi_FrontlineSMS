/**
 * 
 */
package net.frontlinesms.data.domain;

import net.frontlinesms.junit.BaseTestCase;

/**
 * Unit tests for the {@link Keyword} class.
 * @author Alex
 */
public class KeywordTest extends BaseTestCase {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS

//> ACCESSORS
	
//> TEST METHODS
	/** Test that empty keywords will be rejected. */
	public void testEmptyKeyword() {
		testBadKeyword(" ");
		testBadKeyword("  ");
		testBadKeyword("   ");
		testBadKeyword("\r\n");
		testBadKeyword("\r \n ");
	}
	
	/** Test that the constructor successfully rejects keywords which start with a space or other whitespace character. */
	public void testLeadingWhitespace() {
		testBadKeyword(" bad");
		testBadKeyword("  bad");
		testBadKeyword("\nbad");
		testBadKeyword("\rbad");
	}

	/** Test that the constructor successfully rejects keywords which end with a space or other whitespace character. */
	public void testTrailingWhitespace() {
		testBadKeyword("bad ");
		testBadKeyword("bad  ");
		testBadKeyword("bad\n");
		testBadKeyword("bad\r");
	}

	/** Test that the constructor successfully rejects keywords which have repetitve space characters between words. */	
	public void testMultipleWhitespace() {
		testBadKeyword("very  bad");
		testBadKeyword("very   bad");
		testBadKeyword("very    bad");
		testBadKeyword("very very  bad");
	}

	/** Test that the constructor successfully rejects keywords which have illegal characters. */	
	public void testIllegalCharacters() {
		testBadKeyword("very\nbad");
		testBadKeyword("very\rbad");
	}
	
	/**
	 * Tests creation of keywords which should be legal.  It's important to keep in mind that people may be putting
	 * punctuation or non-latin characters into keywords, so we need to make sure that this is accepted.
	 */
	public void testLegalKeywords() {
		testGoodKeyword("");
		testGoodKeyword("a");
		testGoodKeyword("a b");
		testGoodKeyword("a b c");
		testGoodKeyword("simple");
		testGoodKeyword("less simple");
		testGoodKeyword("works?");
		testGoodKeyword("works too?");
		testGoodKeyword("works? too");
		testGoodKeyword("Buén dia");
		testGoodKeyword("yes!");
		testGoodKeyword("Создать внешнюю команду");
	}

//> INSTANCE HELPER METHODS
	/**
	 * Tests creation of a keyword with a String which should be rejected.
	 * @param keyword 
	 */
	private void testBadKeyword(String keyword) {
		try {
			new Keyword(keyword, "");
			fail("Keyword constructor should have thrown exception for keyword: '" + keyword + "'");
		} catch(IllegalArgumentException ex) { /* expected */ }
	}
	
	/**
	 * Test the creation of a legal keyword.
	 * @param keyword
	 */
	private void testGoodKeyword(String keyword) {
		try {
			new Keyword(keyword, "");
		} catch(IllegalArgumentException ex) {
			fail("Failed to create legal keyword '" + keyword + "'");
		}
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
