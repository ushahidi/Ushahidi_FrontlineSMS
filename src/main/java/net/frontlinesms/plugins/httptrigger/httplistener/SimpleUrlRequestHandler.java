/**
 * 
 */
package net.frontlinesms.plugins.httptrigger.httplistener;

/**
 * @author Alex
 *
 */
public interface SimpleUrlRequestHandler {
	/**
	 * Check if this handler should handle the supplied URI.
	 * @param requestUri The request URI, without leading / character
	 * @return <code>true</code> if this should process the supplied URI, <code>false</code> otherwise
	 */
	public boolean shouldHandle(String requestUri);
	
	/**
	 * Process the supplied URI.
	 * @param requestUri
	 * @return <code>true</code> if the request was processed successfully, <code>false</code> if there was a problem
	 */
	public boolean handle(String requestUri);
}
