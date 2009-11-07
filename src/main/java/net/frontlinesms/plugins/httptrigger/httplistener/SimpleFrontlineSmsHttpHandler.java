/**
 * 
 */
package net.frontlinesms.plugins.httptrigger.httplistener;

import java.io.IOException;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.frontlinesms.Utils;
import net.frontlinesms.plugins.httptrigger.HttpTriggerEventListener;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpURI;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 * Handler for simple queries for FrontlineSMS to send messages.
 * @author Alex
 */
class SimpleFrontlineSmsHttpHandler extends AbstractHandler {
	
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	private final HttpTriggerEventListener eventListener;
	private final LinkedList<SimpleUrlRequestHandler> handlers = new LinkedList<SimpleUrlRequestHandler>();

//> CONSTRUCTORS
	SimpleFrontlineSmsHttpHandler(HttpTriggerEventListener eventListener) {
		this.eventListener = eventListener;
		
		// Add handler for SEND SMS commands
		this.handlers.add(new SimpleUrlRequestHandler() {
			final String REQUEST_START = "send/sms/";
			public boolean handle(String requestUri) {
				assert shouldHandle(requestUri) : "This URI should not be handled here.";
				String[] requestParts = requestUri.substring(REQUEST_START.length()).split("\\/", 2);
				if(requestParts.length < 2) {
					SimpleFrontlineSmsHttpHandler.this.eventListener.log("Not enough params in SEND SMS request: " + requestUri);
					return false;
				}
				String toPhoneNumber = requestParts[0];
				String message = requestParts[1];
				
				SimpleFrontlineSmsHttpHandler.this.eventListener.sendSms(Utils.urlDecode(toPhoneNumber), Utils.urlDecode(message));
				
				return true;
			}
			public boolean shouldHandle(String requestUri) {
				return requestUri.startsWith(REQUEST_START);
			}
		});
	}

//> ACCESSORS

//> INSTANCE METHODS
	/** @see org.mortbay.jetty.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int) */
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
		Request baseRequest = (request instanceof Request) ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
		baseRequest.setHandled(true);
		
		response.setContentType("text/html");
		
		boolean success = processRequestFromUrl(baseRequest.getUri());

		final int httpStatusCode;
		final String responseContent;
		if(success) {
			httpStatusCode = HttpServletResponse.SC_OK;
			responseContent = "OK";
		} else {
			httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			responseContent = "ERROR";
		}
		response.setStatus(httpStatusCode);
		response.getWriter().println(responseContent);
	}
	
//> INSTANCE HELPER METHODS
	/**
	 * Process request from the URL.
	 * @param requestUri 
	 * @return 
	 */
	private boolean processRequestFromUrl(final HttpURI requestUri) {
		eventListener.log("Processing request: " + requestUri);

		// Get this URI string, stripping leading '/' character
		String requestUriString = requestUri.toString().substring(1);
		
		for(SimpleUrlRequestHandler handler : this.handlers) {
			if(handler.shouldHandle(requestUriString)) {
				return handler.handle(requestUriString);
			}
		}
		
		this.eventListener.log("No handler found for request: " + requestUriString);
		return false;
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}