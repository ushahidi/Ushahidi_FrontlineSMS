package com.ushahidi.plugins.mapping.sync;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ushahidi.plugins.mapping.util.MappingLogger;

/**
 * SynchronizationPost
 * @author dalezak
 *
 */
public class SynchronizationPost {
	
	public static final MappingLogger LOG = MappingLogger.getLogger(SynchronizationPost.class);	
	
	/**
	 * Post body
	 */
	private final Map<String, Object> post = new LinkedHashMap<String, Object>();
	
	/**
	 * Url
	 */
	private final URL url;
	
	/**
	 * The line end characters.  
	 */
	private static final String NEWLINE = "\r\n";
 
	/**
	 * The boundary prefix.  
	 */
	private static final String PREFIX = "--";
	
	/**
	 * The charset
	 */
	private static final String CHARSET = "UTF-8";
	
	/**
	 * is the post multipart/form-data?
	 */
	private boolean requiresMultiPartFormData = false;
	
	/**
	 * SynchronizationPost
	 * @param url post url
	 * @throws MalformedURLException
	 */
	public SynchronizationPost(String url) throws MalformedURLException {
		this.url = new URL(url);
	}
	
	/**
	 * Add Post Body value
	 * @param key key
	 * @param value value
	 */
	public void add(String key, String value) {
		if (value != null) {
			post.put(key, value);
		}
	}
	
	/**
	 * Add Post Body value
	 * @param key key
	 * @param value value
	 */
	public void add(String key, double value) {
		post.put(key, Double.toString(value));
	}
	
	/**
	 * Add Post Body value
	 * @param key key
	 * @param value value
	 */
	public void add(String key, File value) {
		if (value != null) {
			post.put(key, value);
			requiresMultiPartFormData = true;	
		}
	}
	
	/**
	 * Post form data
	 * @return response string
	 * @throws IOException 
	 */
	public String postFormData() throws IOException {
		return requiresMultiPartFormData 
			? postMultiPartFormData() 
			: postUrlEncodedFormData();
	}
	
	/**
	 * Get URLConnection
	 * @param contentType the Content-Type of the connection
	 * */
	protected URLConnection getURLConnection(String contentType) throws IOException {
		URLConnection connection = url.openConnection();
		if (connection instanceof HttpURLConnection) {
			HttpURLConnection httpConnection = (HttpURLConnection)connection;
			httpConnection.setRequestMethod("POST");
		}
		//connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setDefaultUseCaches(false);
		connection.setRequestProperty("Accept","*/*");
		connection.setRequestProperty("Cache-Control", "no-cache");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type", contentType);
		return connection;
	}
	
	/**
	 * Get response string
	 * @param connection URLConnection
	 * @return response string
	 * @throws IOException
	 */
	private String getResponseString(URLConnection connection) throws IOException {
		StringBuffer response = new StringBuffer();
		BufferedReader input = null;
		try {
			HttpURLConnection httpConnection = (HttpURLConnection)connection; 
			if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				LOG.debug("ResponseCode: %d", httpConnection.getResponseCode());
				LOG.debug("ResponseMessage: %s", httpConnection.getResponseMessage());
				input = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
			}
			else {
				LOG.error("ResponseCode: %d", httpConnection.getResponseCode());
				LOG.error("ResponseMessage: %s", httpConnection.getResponseMessage());
				input = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));	
			}
			String line = null;
			while((line = input.readLine()) != null){
				response.append(line);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw new IOException(ex);
		}
		finally {
			if (input != null) {
				input.close();
			}
		}
		LOG.debug("RESPONSE: %s", response.toString());	
		return response.toString();
	}
	
	/**
	 * Post multi-part form data to server
	 * @param requestURL url
	 * @param post post values
	 * @return string response
	 * @throws IOException 
	 */
	protected String postMultiPartFormData() throws IOException {
		String boundary = Long.toString(System.currentTimeMillis(), 16);
		URLConnection connection = getURLConnection("multipart/form-data; boundary=" + boundary);
		DataOutputStream output = new DataOutputStream(connection.getOutputStream());
		try {
		    for(String key : post.keySet()) {
		    	Object value = post.get(key);
		    	if (value instanceof File) {
		    		File file = (File)value;
		    		if (file.exists()) {
			    		LOG.debug("%s = %s", key, file.getName());
			    		output.writeBytes(PREFIX);
			    		output.writeBytes(boundary);
			    		output.writeBytes(NEWLINE);
			    		output.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + file.getName() + "\"");
			    		output.writeBytes(NEWLINE);
			    		String contentType = URLConnection.guessContentTypeFromName(file.getName());
			    		if(contentType != null) {
			    			output.writeBytes("Content-Type: " + contentType);
			    		}
			    		else {
			    			output.writeBytes("Content-Type: application/octet-stream");
			    		}
			    		output.writeBytes(NEWLINE);
			    		output.writeBytes(NEWLINE);
			    		InputStream input = null;
			    	    try {
			    	        input = new FileInputStream(file);
			    	        byte[] data = new byte[1024];
			    			int read = 0;
			    			while((read = input.read(data, 0, data.length)) >= 0) {
			    				output.write(data, 0, read);
			    			}
			    			output.flush();
			    			data = null;
			    	    } 
			    	    finally {
			    	        if (input != null) {
			    	        	try { 
			    	        		input.close(); 
			    	        	} 
			    	        	catch (IOException ex) {}
			    	        }
			    	    }
			    		output.writeBytes(NEWLINE);
			    		output.flush();
		    		}
		    	}
		    	else if (value instanceof String) {
		    		String string = (String)value;
		    		output.writeBytes(PREFIX);
		    		output.writeBytes(boundary);
		    		output.writeBytes(NEWLINE);
		    		output.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"");
		    		output.writeBytes(NEWLINE);
		    		output.writeBytes(NEWLINE);
			    	output.writeBytes(string);	
		    		output.writeBytes(NEWLINE);
		    		output.flush();
		    		LOG.debug("%s = %s", key, string);
		    	}
		    }
		} 
		finally {
			output.writeBytes(PREFIX);
			output.writeBytes(boundary);
			output.writeBytes(PREFIX);
			output.writeBytes(NEWLINE);
			output.flush();
			output.close();	
		}
		return getResponseString(connection);
	}
	
	/**
	 * Post URL-Encoded Form Data to server
	 * @param request url
	 * @param post post body
	 * @return response string
	 * @throws IOException 
	 */
	protected String postUrlEncodedFormData() throws IOException {
		URLConnection connection = getURLConnection("application/x-www-form-urlencoded");
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		try {
			boolean includeAmpersand = false;
			for(String key : post.keySet()) {
				Object value = post.get(key);
				if (value instanceof File) {
					//skip File, since it can't be handled via url-encoded form data
				}
				else {
					if (includeAmpersand) {
						writer.write("&");
					}
					writer.write(key);
					writer.write("=");
					if (value != null) {
						writer.write(URLEncoder.encode((String)value, CHARSET));
					}
					else {
						writer.write("");
					}
					includeAmpersand = true;
				}
			}
			writer.flush();
		}
		finally {
			writer.close();
		}
		return getResponseString(connection);
	}
}