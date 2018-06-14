package mecs.iot.proj.om2m.asn.user_direct;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

class RemoteInterface extends Client {
	
	private String serial;
	private String context;
	private String id;
	
	private String address;
	
	private boolean executing;

	RemoteInterface(String id, String host, String uri, String context, boolean debug, Console console, String serial, String ip, int port) throws URISyntaxException {
		super(Services.joinIdHost(id+"_remote",host), uri, debug);
		this.serial = serial;
		this.context = context;
		this.id = Services.joinIdHost(id,host);
		this.address = ip + ":" + Integer.toString(port);
		CommandList list = new CommandList(this,this.id);
		console.add("query",list.getCommand(0),"Query the attributes of a node. Syntax: query serial");
		console.add("read",list.getCommand(1),"Read the value of a node. Syntax: read serial");
		console.add("lookout",list.getCommand(2),"Adds a subscription to a node. Syntax: lookout serial");
		console.add("write",list.getCommand(3),"Write an action to a node. Syntax: write serial action");
		console.add("link",list.getCommand(4),"Adds a subscription between two nodes. Syntax: link sensor actuator event action");
		//console.add("rm",list.getCommand(5)); TODO
		executing = true;
		createSubscriptionServer(null,null,port);
	}
	
	@Override
	
	public void run() {
		outStream.out("Starting interface", i);
		outStream.out1("Locating serial \"" + serial + "\"", i);
		CoapResponse response = locate(serial);
		if (response==null) {
			outStream.out2("failed");
			errStream.out("Unable to register to " + services.uri() + ", timeout expired", i, Severity.LOW);
			return;
		} else if (response.getCode()!=ResponseCode.CONTENT) {
			outStream.out2("failed");
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to locate the user on " + services.uri() + ", response: " + response.getCode() + //
						", reason: " + response.getResponseText(), //
						i, Severity.LOW);
			else
				errStream.out("Unable to locate the user on " + services.uri() + ", response: " + response.getCode(), //
						i, Severity.LOW);
			return;
		}
		String address = response.getResponseText(); 																	// MN address
		outStream.out1_2("done, received " + address + " as MN address, connecting");
		try {
			connect(Constants.adnProtocol + address + Constants._mnADNPort + "/" + context);
		} catch (URISyntaxException e) {
			outStream.out2("failed");
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		outStream.out1_2("done, registering to MN");
		response = register(id,this.address);
		// response = ping();
		if (response==null) {
			outStream.out2("failed");
			errStream.out("Unable to register to " + services.uri() + ", timeout expired", i, Severity.LOW);
			return;
		} else if (response.getCode()!=ResponseCode.CREATED) {
			outStream.out2("failed");
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to register to " + services.uri() + ", response: " + response.getCode() + //
						", reason: " + response.getResponseText(), //
						i, Severity.LOW);
			else
				errStream.out("Unable to register to " + services.uri() + ", response: " + response.getCode(), //
					i, Severity.LOW);
			return;
		}
		outStream.out1_2("done, connecting to CSE");
		try {
			connect(Constants.cseProtocol + address + Constants.mnRoot + context + Constants.mnCSEPostfix);
		} catch (URISyntaxException e) {
			outStream.out2("failed");
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		outStream.out2("done, posting AE");
		response = services.postAE(Services.normalizeName(id),i);
		outStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:ae", //
				new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class}), i);
		outStream.out1("Connecting to MN", i);
		try {
			connect(Constants.adnProtocol + address + Constants._mnADNPort + "/" + context);
		} catch (URISyntaxException e) {
			outStream.out2("failed");
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		outStream.out2("done");
		i++;
		while(executing) {
			outStream.out1("Waiting for subscriptions", i);
			waitForSubscriptions();
			outStream.out2("received: \"" + getNotification() + "\" (by \"" + getNotifier() + "\")");
			i++;
		}
		// TODO, delete AE
		outStream.out("Terminating interface", i);
	}
	
	public synchronized void terminate() {
		while (!waiting) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		executing = false;
		setNotification("terminated",Thread.currentThread().getName());
		notify();
	}

}
