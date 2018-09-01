package mecs.iot.proj.om2m;

import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.Console;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;

/** A CoAP client thread, used to connect an activity to a CoAP server.
 * 
 * @author Alessandro Trifoglio
 * @version 0.0.1-SNAPSHOT
 * @since 0.0.1-SNAPSHOT
*/
public class Client extends Thread {
	
	/** The set of oneM2M services this class provides.
	*/
	public Services services;
	
	protected OutStream outStream;
	protected DebugStream debugStream;
	protected ErrStream errStream;
	
	protected int i;
	
	private CoapClient connection;
	private URI uri;
	
	/** Create a client.
	 * 
	 * @param name the client name
	 * @param debug specifies whether enabling debugging or not
	 */
	public Client(String name, boolean debug) {
		super(name);
		debugStream = new DebugStream(name,debug);
		i = 0;
	}
	
	/** Create a client and connect it to an URI.
	 * 
	 * @param name the client name
	 * @param uri the server URI
	 * @param debug specifies whether enabling debugging or not
	 * @throws URISyntaxException URI is not valid
	 */
	public Client(String name, String uri, boolean debug) throws URISyntaxException {
		super(name);
		outStream = new OutStream(name);
		debugStream = new DebugStream(name,debug);
		errStream = new ErrStream(name);
		i = 0;
		connect(uri);
	}
	
	 /** Establish a connection with a server.
	 * Connects to a server by specifying its URI and creates a new instance of oneM2M services.
	 * Examples:
	 * <ul>
	 * <li>
	 * <code>connect("coap://192.168.0.104:5685/augmented-things");</code>
	 * connects the client to the infrastructure ADN at the address 192.168.0.104
	 * </li>
	 * <li>
	 * <code>connect("coap://192.168.0.105:5684/~/augmented-things-MN-cse");</code>
	 * connects the client to the middle-node CSE at the address 192.168.0.105:5684
	 * </li>
	 * </ul>
	 *
	 * @param uri the server URI
	 * @throws URISyntaxException URI is not valid
	 */
	public void connect(String uri) throws URISyntaxException {
		URI uri_ = new URI(uri);
		if (!uri_.equals(this.uri)) {
			this.uri = uri_;
			connection = new CoapClient(uri_);
		}
		services = new Services(this,uri);
		debugStream.out("Connected to \"" + uri + "\"", i);
	}
	
	/** Establish a connection with a server.
	 * Connects to a server by specifying its URI.
	 * Examples:
	 * <ul>
	 * <li>
	 * <code>connect("coap://192.168.0.104:5685/augmented-things");</code>
	 * connects the client to the infrastructure ADN at the address 192.168.0.104
	 * </li>
	 * <li>
	 * <code>connect("coap://192.168.0.105:5684/~/augmented-things-MN-cse");</code>
	 * connects the client to the middle-node CSE at the address 192.168.0.105:5684
	 * </li>
	 * </ul>
	 *
	 * @param uri the server URI
	 * @param createService specifies whether creating a new instance of oneM2M services or not
	 * @throws URISyntaxException URI is not valid
	 */
	public void connect(String uri, boolean createService) throws URISyntaxException {
		URI uri_ = new URI(uri);
		if (!uri_.equals(this.uri)) {
			this.uri = uri_;
			connection = new CoapClient(uri_);
		}
		if (createService)
			services = new Services(this,uri);
		debugStream.out("Connected to \"" + uri + "\"", i);
	}
	
//	public boolean ping() {
//		debugStream.out("Sent ping to Coap server  + \"" + uri.getHost() + ":" + uri.getPort() + uri.getPath() + "\"", i);
//		return connection.ping();
//	}
	
	/** Disable the CoAP connection.
	 * 
	 */
	@Override
	public void destroy() {
		connection.shutdown();
	}
	
	/** Send a synchronous CoAP request.
	 * 
	 * @param request the request to issue
	 * @param method the REST method to issue
	 * @return the server response
	 */
	public CoapResponse send(Request request, Code method) {
		debugStream.out("Sent " + method + " request to \"" + uri.getScheme() + "://" + uri.getHost()
		+ ":" + uri.getPort() + uri.getPath() + "?" + Services.parseCoapRequest(request) + "\" with payload <" + request.getPayloadString() + ">", i);
		return connection.advanced(request);
	}
	
	/** Send a synchronous CoAP request and prints the answer on a console.
	 * 
	 * @param request the request to issue
	 * @param method the REST method to issue
	 * @param console a console object
	 * @return the server response
	 */
	public CoapResponse send(Request request, Code method, Console console) {
		console.out("Sent " + method + " request to \"" + uri.getScheme() + "://" + uri.getHost()
		+ ":" + uri.getPort() + uri.getPath() + "?" + Services.parseCoapRequest(request) + "\" with payload <" + request.getPayloadString() + ">");
		return connection.advanced(request);
	}
	
	/** Send an asynchronous CoAP request.
	 * 
	 * @param request the request to issue
	 * @param method the REST method to issue
	 */
	public void sendAsync(Request request, Code method) {
		debugStream.out("Sent asynchronous " + method + " request to \"" + uri.getScheme() + "://" + uri.getHost()
		+ ":" + uri.getPort() + uri.getPath() + "?" + Services.parseCoapRequest(request) + "\" with payload <" + request.getPayloadString() + ">", i);
		connection.advanced(null,request);
	}
	
//	String getUriValue(CoapResponse response, String attribute, int index)
//	{
//		List<String> query = response.getOptions().getUriQuery();
//		return query.get(index).substring(attribute.length()+1);
//	}
	
	/** Get the number of operations this client has performed.
	 * 
	 * @return the number of elementary operations performed between start-up and the call point
	 */
	public int getCount() {
		return this.i;
	}
	
//	public void setCount(int i) {
//		this.i = i;
//	}
	
	/** Step the number of operations of this client by one.
	 * 
	 */
	public void stepCount() {
		this.i = this.i+1;
	}
	
}