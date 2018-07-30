package mecs.iot.proj.om2m.asn;

import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.ASN;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

/** An ASN Client.
 * 
 * @author Alessandro Trifoglio
 * @version 0.0.1-SNAPSHOT
 * @since 0.0.1-SNAPSHOT
*/
public class Client extends mecs.iot.proj.om2m.Client {
	
	private CoapServer server;
	private String notification;
	private String notifier;
	
	protected boolean waiting;

	protected Client(String id, String uri, boolean debug) throws URISyntaxException {
		super(id,uri,debug);
		server = null;
		notification = null;
		waiting = false;
	}
	
	/*
	 * Locate node
	 */
	
	protected CoapResponse locate(String id, String serial, int location) {
		Request request = new Request(Code.POST);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + Format.normalizeName(id));
		request.getOptions().addUriQuery("ser" + "=" + serial);
		request.getOptions().addUriQuery("loc" + "=" + Integer.toString(location));
		//request.setTimedOut(true);
		debugStream.out("Sent location request to \"" + services.uri() + "\"", i);
		return send(request, Code.POST);
	}
	
	/*
	 * Locate user
	 */
	
	protected CoapResponse locate(String serial) {
		Request request = new Request(Code.GET);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("mode" + "=" + Integer.toString(0));
		request.getOptions().addUriQuery("ser" + "=" + serial);
		//request.setTimedOut(true);
		debugStream.out("Sent location request to \"" + services.uri() + "\"", i);
		return send(request, Code.GET);
	}
	
	/*
	 * MN registration (sensor)
	 */
	
	protected CoapResponse register(ASN tag) {
		Request request = new Request(Code.POST);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + Format.normalizeName(tag.id));
		request.getOptions().addUriQuery("ser" + "=" + tag.serial);
		request.getOptions().addUriQuery("type" + "=" + tag.type);
		String payload = "";
		for (int i=0; i<tag.attributes.length; i++) {
			if (i!=tag.attributes.length-1)
				payload += tag.attributes[i] + ", ";
			else
				payload += tag.attributes[i];
		}
		request.setPayload(payload);
		//request.setTimedOut(true);
		debugStream.out("Sent registration request to \"" + services.uri() + "\"", i);
		return send(request, Code.POST);
	}
	
	/*
	 * MN registration (actuator)
	 */
	
	protected CoapResponse register(ASN tag, String address) {
		Request request = new Request(Code.POST);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + Format.normalizeName(tag.id));
		request.getOptions().addUriQuery("ser" + "=" + tag.serial);
		request.getOptions().addUriQuery("type" + "=" + tag.type);
		request.getOptions().addUriQuery("addr" + "=" + address);
		String payload = "";
		for (int i=0; i<tag.attributes.length; i++) {
			if (i!=tag.attributes.length-1)
				payload += tag.attributes[i] + ", ";
			else
				payload += tag.attributes[i];
		}
		request.setPayload(payload);
		//request.setTimedOut(true);
		debugStream.out("Sent registration request to " + services.uri() + "\"", i);
		return send(request, Code.POST);
	}
	
	/*
	 * MN registration (user direct)
	 */
	
	protected CoapResponse register(String id, String address) {
		Request request = new Request(Code.POST);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + Format.normalizeName(id));
		request.getOptions().addUriQuery("addr" + "=" + address);
		//request.setTimedOut(true);
		debugStream.out("Sent registration request to \"" + services.uri() + "\"", i);
		return send(request, Code.POST);
	}
	
	/*
	 * Delete user and node
	 */
	
	protected void deleteNode(String serial) {
		Request request = new Request(Code.DELETE);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("ser" + "=" + serial);
		//request.setTimedOut(true);
		debugStream.out("Sent deletion request to \"" + services.uri() + "\"", i);
		send(request, Code.DELETE);
	}
	
	protected void deleteUser(String id) {
		Request request = new Request(Code.DELETE);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + id);
		//request.setTimedOut(true);
		debugStream.out("Sent deletion request to \"" + services.uri() + "\"", i);
		send(request, Code.DELETE);
	}
	
	protected void deleteNodeAsync(String serial) {
		Request request = new Request(Code.DELETE);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("ser" + "=" + serial);
		//request.setTimedOut(true);
		debugStream.out("Sent deletion request to \"" + services.uri() + "\"", i);
		sendAsync(request, Code.DELETE);
	}
	
	protected void deleteUserAsync(String id) {
		Request request = new Request(Code.DELETE);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + id);
		//request.setTimedOut(true);
		debugStream.out("Sent deletion request to \"" + services.uri() + "\"", i);
		sendAsync(request, Code.DELETE);
	}
	
	/*
	 * Other functions
	 */
	
	protected void createNotificationServer(String name, String uri, boolean debug, Unit unit, int port) {
		server = new CoapServer(port);
		server.add(new NotificationServer(name,uri,debug,unit));
		server.start();
	}
	
	protected synchronized void waitForNotifications() {
		waiting = true;
		notify();
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		waiting = false;
	}
	
	// Public methods: used by console commands
	
	/*
	 * Content instance posting (sensor)
	 */
	
	public void publish(String id, String serial, String con) {
		Request request = new Request(Code.POST);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + Format.normalizeName(id));
		request.getOptions().addUriQuery("ser" + "=" + serial);
		request.getOptions().addUriQuery("con" + "=" + con);
		//request.setTimedOut(true);
		debugStream.out("Sent content instance request to \"" + services.uri() + "\"", i);
		sendAsync(request, Code.POST);
	}
	
	/*
	 * Attributes query
	 */
	
	public String getAttributes(String serial, Console console) {
		Request request = new Request(Code.GET);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("mode" + "=" + "1");
		request.getOptions().addUriQuery("ser" + "=" + serial);
		//request.setTimedOut(true);
		console.out("Sent attributes request to \"" + services.uri() + "\"");
		CoapResponse response = send(request, Code.GET, console);
		if (response==null)
			return "Error: timeout expired";
		if (response.getCode()==ResponseCode.CONTENT)
			return response.getResponseText();
		else
			return "Error: " + response.getCode().toString();
	}
	
	/*
	 * Node read
	 */
	
	public String getResource(String serial, Console console) {
		Request request = new Request(Code.GET);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("mode" + "=" + "2");
		request.getOptions().addUriQuery("ser" + "=" + serial);
		//request.setTimedOut(true);
		console.out("Sent reading request to \"" + services.uri() + "\"");
		CoapResponse response = send(request, Code.GET, console);
		if (response==null)
			return "Error: timeout expired";
		if (response.getCode()==ResponseCode.CONTENT)
			return response.getResponseText();
		else
			return "Error: " + response.getCode().toString();
	}
	
	/*
	 * Node lookout
	 */
	
	public String postSubscription(String observer, String serial, Console console) {
		Request request = new Request(Code.POST);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + Format.normalizeName(observer));
		request.getOptions().addUriQuery("ser" + "=" + serial);
		//request.setTimedOut(true);
		console.out("Sent lookout request to \"" + services.uri() + "\"");
		CoapResponse response = send(request, Code.POST, console);
		if (response==null)
			return "Error: timeout expired";
		if (response.getCode()==ResponseCode.CREATED)
			return "OK";
		else
			return "Error: " + response.getCode().toString();
	}
	
	/*
	 * Lookout removal
	 */
	
	public String removeSubscription(String observer, String serial, Console console) {
		Request request = new Request(Code.DELETE);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + Format.normalizeName(observer));
		request.getOptions().addUriQuery("ser" + "=" + serial);
		//request.setTimedOut(true);
		console.out("Sent lookout removal request to \"" + services.uri() + "\"");
		CoapResponse response = send(request, Code.DELETE, console);
		if (response==null)
			return "Error: timeout expired";
		if (response.getCode()==ResponseCode.DELETED)
			return response.getResponseText();
		else
			return "Error: " + response.getCode().toString();
	}
	
	/*
	 * Node write
	 */
	
	public String putResource(String serial, String label, Console console) {
		Request request = new Request(Code.PUT);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("ser" + "=" + serial);
		request.getOptions().addUriQuery("lab" + "=" + label);
		//request.setTimedOut(true);
		console.out("Sent write request to \"" + services.uri() + "\"");
		CoapResponse response = send(request, Code.PUT, console);
		if (response==null)
			return "Error: timeout expired";
		if (response.getCode()==ResponseCode.CHANGED)
			return response.getResponseText();
		else
			return "Error: " + response.getCode().toString();
	}
	
	/*
	 * Nodes link (serial0=sensor, serial1=actuator, lab0=event, lab1=action, id=notificationId)
	 */
	
	public String postSubscription(String id, String serial0, String serial1, String label0, String label1, Console console) {
		Request request = new Request(Code.POST);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("ser" + "=" + serial0);
		request.getOptions().addUriQuery("ser" + "=" + serial1);
		request.getOptions().addUriQuery("lab" + "=" + label0);
		request.getOptions().addUriQuery("lab" + "=" + label1);
		request.getOptions().addUriQuery("id" + "=" + Format.normalizeName(id));
		//request.setTimedOut(true);
		console.out("Sent link request to \"" + services.uri() + "\"");
		CoapResponse response = send(request, Code.POST, console);
		if (response==null)
			return "Error: timeout expired";
		if (response.getCode()==ResponseCode.CREATED)
			return "OK";
		else
			return "Error: " + response.getCode().toString();
	}
	
	/*
	 * Link removal (serial0=sensor, serial1=actuator, lab0=event, lab1=action, id=notificationId)
	 */
	
	public String removeSubscription(String id, String serial0, String serial1, String label0, String label1, Console console) {
		Request request = new Request(Code.DELETE);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("ser" + "=" + serial0);
		request.getOptions().addUriQuery("ser" + "=" + serial1);
		request.getOptions().addUriQuery("lab" + "=" + label0);
		request.getOptions().addUriQuery("lab" + "=" + label1);
		request.getOptions().addUriQuery("id" + "=" + Format.normalizeName(id));
		//request.setTimedOut(true);
		console.out("Sent link removal request to \"" + services.uri() + "\"");
		CoapResponse response = send(request, Code.DELETE, console);
		if (response==null)
			return "Error: timeout expired";
		if (response.getCode()==ResponseCode.DELETED)
			return response.getResponseText();
		else
			return "Error: " + response.getCode().toString();
	}
	
	/*
	 * MN name query
	 */
	
	public String getMN(Console console) {
		Request request = new Request(Code.GET);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		//request.setTimedOut(true);
		console.out("Sent MN name request to \"" + services.uri() + "\"");
		CoapResponse response = send(request, Code.GET, console);
		if (response==null)
			return "Error: timeout expired";
		if (response.getCode()==ResponseCode.CONTENT)
			return response.getResponseText();
		else
			return "Error: " + response.getCode().toString();
	}
	
	/*
	 * Node name query
	 */
	
	public String getNode(String serial, Console console) {
		Request request = new Request(Code.GET);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("ser" + "=" + serial);
		//request.setTimedOut(true);
		console.out("Sent node name request to \"" + services.uri() + "\"");
		CoapResponse response = send(request, Code.GET, console);
		if (response==null)
			return "Error: timeout expired";
		if (response.getCode()==ResponseCode.CONTENT)
			return response.getResponseText();
		else
			return "Error: " + response.getCode().toString();
	}
	
	public void setNotification(String notification, String notifier) {
		this.notification = notification;
		this.notifier = notifier;
	}
	
	public String getNotification() {
		return notification;
	}
	
	public String getNotifier() {
		return notifier;
	}
	
	@Override
	public void destroy() {
		if (server!=null)
			server.destroy();
		super.destroy();
	}

}
