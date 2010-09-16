package com.ushahidi.plugins.mapping.sync;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * SynchronizationPost
 * @author dalezak
 *
 */
public class SynchronizationPost {

	/**
	 * Internal storage for post body
	 */
	private final StringBuilder post = new StringBuilder();

	/**
	 * Add param and value to post body
	 * @param key param key
	 * @param value param value
	 */
	public void add(String key, double value) {
		if (value != 0) {
			add(key, Double.toString(value));	
		}
		else {
			add(key, "");
		}
		
	}
	
	/**
	 * Add param and value to post body
	 * @param key param key
	 * @param value param value
	 */
	public void add(String key, String value) {
		if (post.length() > 0) {
			post.append("&");
		}
		post.append(key);
		post.append("=");
		try {
			if (value != null) {
				post.append(URLEncoder.encode(value, "UTF-8"));
			}
			else {
				post.append("");
			}
		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Output post body string
	 */
	@Override
	public String toString() {
		return post.toString();
	}
}