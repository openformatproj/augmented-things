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

public class Client extends Thread
{
	
	public Services services;
	
	private CoapClient connection;
	private URI uri;
	
	protected OutStream outStream;
	protected DebugStream debugStream;
	protected ErrStream errStream;
	
	protected int i;
	
	public Client(String name, boolean debug) {
		super(name);
		debugStream = new DebugStream(name,debug);
		i = 0;
	}
	
	public Client(String name, String uri, boolean debug) throws URISyntaxException {
		super(name);
		outStream = new OutStream(name);
		debugStream = new DebugStream(name,debug);
		errStream = new ErrStream(name);
		i = 0;
		connect(uri);
	}
	
	 /** 
	 * Connects to a server by specifying its URI. For instance,
	 * <code>connect("coap://192.168.0.104:5685/augmented-things");</code>
	 * can be used to connect the client to the infrastructure
	 * ADN at address 192.168.0.104, while
	 * <code>connect("coap://192.168.0.105:5684/~/augmented-things-MN-cse")</code>
	 * connects the client to the middle-node CSE.
	 *
	 * @param uri       the server URI
	 * @throws URISyntaxException
	 */
	public void connect(String uri) throws URISyntaxException {
		URI uri_ = new URI(uri);
		if (!uri_.equals(this.uri)) {
			this.uri = uri_;
			connection = new CoapClient(uri_);
		}
		services = new Services(this,uri);
		debugStream.out("Connected to " + uri, i);
	}
	
	public void connect(String uri, boolean createService) throws URISyntaxException {
		URI uri_ = new URI(uri);
		if (!uri_.equals(this.uri)) {
			this.uri = uri_;
			connection = new CoapClient(uri_);
		}
		if (createService)
			services = new Services(this,uri);
		debugStream.out("Connected to " + uri, i);
	}
	
	public boolean ping() {
		debugStream.out("Sent ping to Coap server " + uri.getHost() + ":" + uri.getPort() + uri.getPath(), i);
		return connection.ping();
	}
	
	@Override
	
	public void destroy() {
		connection.shutdown();
	}
	
	public CoapResponse send(Request request, Code method) {
		debugStream.out("Sent " + method + " request to " + uri.getScheme() + "://" + uri.getHost()
		+ ":" + uri.getPort() + uri.getPath() + "?" + Services.parseCoapRequest(request) + " with payload <" + request.getPayloadString() + ">", i);
		return connection.advanced(request);
	}
	
	public CoapResponse send(Request request, Code method, Console console) {
		console.out("Sent " + method + " request to " + uri.getScheme() + "://" + uri.getHost()
		+ ":" + uri.getPort() + uri.getPath() + "?" + Services.parseCoapRequest(request) + " with payload <" + request.getPayloadString() + ">");
		return connection.advanced(request);
	}
	
	public void sendAsync(Request request, Code method) {
		debugStream.out("Sent asynchronous " + method + " request to " + uri.getScheme() + "://" + uri.getHost()
		+ ":" + uri.getPort() + uri.getPath() + "?" + Services.parseCoapRequest(request) + " with payload <" + request.getPayloadString() + ">", i);
		connection.advanced(null,request);
	}
	
//	String getUriValue(CoapResponse response, String attribute, int index)
//	{
//		List<String> query = response.getOptions().getUriQuery();
//		return query.get(index).substring(attribute.length()+1);
//	}
	
	public int getCount() {
		return this.i;
	}
	
	public void setCount(int i) {
		this.i = i;
	}
	
	public void stepCount() {
		this.i = this.i+1;
	}
	
}