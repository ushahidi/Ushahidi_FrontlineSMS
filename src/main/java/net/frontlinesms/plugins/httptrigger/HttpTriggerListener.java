/**
 * 
 */
package net.frontlinesms.plugins.httptrigger;

/**
 * @author Alex
 */
public interface HttpTriggerListener {
	/** Non-blocking call to request that the listener stops. */
	void pleaseStop();

	/** Non-blocking call to make the listener start. */
	void start();
}

//
//import net.frontlinesms.plugins.httptrigger.httplistener.HttpTriggerServer;
//
//
///**
// * @author Alex
// *
// */
//public class HttpTriggerListener extends Thread {
//
////> STATIC CONSTANTS
//
////> INSTANCE PROPERTIES
//	private final HttpTriggerServer server;
//	private HttpTriggerEventListener eventListener;
//
////> CONSTRUCTORS
//	public HttpTriggerListener(int portNumber) {
//		this.server = new HttpTriggerServer(portNumber);
//	}
//
////> ACCESSORS
//	public void pleaseStop() {
//		this.eventListener.log("Terminating listener on port: " + this.server.getPort());
//		this.server.stop();
//	}
//
////> INSTANCE HELPER METHODS
//	public void run() {
//		this.eventListener.log("Starting on port: " + this.server.getPort());
//
//		this.server.runService();
//		
//		this.eventListener.log("Listener terminated on port: " + this.server.getPort());
//	}
//
////> STATIC FACTORIES
//
////> STATIC HELPER METHODS
//}
