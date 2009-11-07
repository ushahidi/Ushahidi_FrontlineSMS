/**
 * 
 */
package net.frontlinesms.plugins.httptrigger.httplistener;

import net.frontlinesms.plugins.httptrigger.HttpTriggerEventListener;
import net.frontlinesms.plugins.httptrigger.HttpTriggerListener;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;

/**
 * @author Alex
 *
 */
public class HttpTriggerServer extends Thread implements HttpTriggerListener {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	private final HttpTriggerEventListener eventListener;
	private final int port;
	private final Server server;

//> CONSTRUCTORS
	public HttpTriggerServer(HttpTriggerEventListener eventListener, int port) {
		this.port = port;
		this.eventListener = eventListener;
		
		this.server = new Server();
		Connector connector = new SocketConnector();
		connector.setPort(port);
		server.setConnectors(new Connector[]{connector});
		
		Handler handler = new SimpleFrontlineSmsHttpHandler(this.eventListener);
		server.setHandler(handler);
	}

//> ACCESSORS

	public int getPort() {
		return this.port;
	}

//> INSTANCE METHODS
	public void run() {
		this.eventListener.log("Starting on port: " + this.getPort());
		try {
			server.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				this.server.stop();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			server.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.eventListener.log("Listener terminated on port: " + this.getPort());
	}

	public void pleaseStop() {
		this.eventListener.log("Terminating listener on port: " + this.getPort());
		try {
			this.server.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS

	public static void main(String[] args) {
		HttpTriggerServer serv = new HttpTriggerServer(new CommandLineTriggerEventListener(), 1440);
		serv.start();
		
		boolean running = true;
		while(running) {
			System.out.println("sleeping...");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				running = false;
			}
		}
	}
}

/**
 * Implementation of {@link HttpTriggerEventListener} which logs all messages to {@link System#out}
 * @author Alex
 */
class CommandLineTriggerEventListener implements HttpTriggerEventListener {
	/** @see net.frontlinesms.plugins.httptrigger.HttpTriggerEventListener#log(java.lang.String) */
	public void log(String message) {
		System.out.println("LOG: " + message);
	}

	public void sendSms(String toPhoneNumber, String message) {
		System.out.println("SEND SMS: to=" + toPhoneNumber + "; message=" + message);
	}
}
