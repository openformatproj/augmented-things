package mecs.iot.proj.om2m;

import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
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
	 * <code>connect("coap://192.168.0.104:5685");</code>
	 * can be used to connect the client to the infrastructure ADN
	 * at address 192.168.0.104, while
	 * <code>connect("coap://192.168.0.105:5684/~/augmented-things-MN-cse")</code>
	 * connects the client to the middle-node CSE.
	 *
	 * @param uri       the server URI
	 * @throws URISyntaxException
	 */
	public void connect(String uri) throws URISyntaxException {
		this.uri = new URI(uri);
		connection = new CoapClient(this.uri);
		services = new Services(this,uri);
		debugStream.out("Connected to " + uri, i);
	}
	
	void simpleConnect(String uri) throws URISyntaxException {
		this.uri = new URI(uri);
		connection = new CoapClient(this.uri);
	}
	
	public void destroy() {
		connection.shutdown();
	}
	
	public CoapResponse send(Request request) {
		return connection.advanced(request);
	}
	
//	String getUriValue(CoapResponse response, String attribute, int index)
//	{
//		List<String> query = response.getOptions().getUriQuery();
//		return query.get(index).substring(attribute.length()+1);
//	}
	
	public int getCount() {
		return i;
	}
	
}