package mecs.iot.proj.om2m.asn.actuator;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.asn.Action;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Tag;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

class RemoteInterface extends Client {
	
	private String context;
	private Tag tag;
	private int location;
	private long end;
	
	private Action[] actions;
	private String address;
	
	private long start;

	RemoteInterface(Tag tag, int location, String uri, String context, boolean debug, Action[] actions, String address, int port, long end) throws URISyntaxException {
		super(tag.id, uri, debug);
		this.context = context;
		this.tag = tag;
		this.location = location;
		this.end = end;
		this.actions = actions;
		this.address = address + ":" + Integer.toString(port);
		createSubscriptionServer(tag.attributes,actions,port);
	}
	
	private class Watchdog extends Thread {
		
		Client lock;
		private long start_;
		
		Watchdog (Client lock) {
			super(Services.joinIdHost("watchdog",Constants.getComputerName()));
			this.lock = lock;
			setDaemon(true);
		}
		
		@Override
		
	    public void run() {
			start_ = start;
			while (true) {
				while(System.currentTimeMillis()-start_<end);
				synchronized(lock) {
					lock.setNotification("terminated",getName());
					lock.notify();
				}
				start_ = System.currentTimeMillis();
			}
	    }
		
	}
	
	@Override
	
	public void run() {
		outStream.out("Starting interface", i);
		outStream.out1("Registering to IN", i);
		CoapResponse response = register(tag,this.address,location);
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
		String[] mnData = response.getResponseText().split(","); 													// MN address and id
		String address = mnData[1]; 																				// MN address
		outStream.out1_2("done, received " + address + " as MN address, connecting");
		try {
			connect(Constants.adnProtocol + address + Constants._mnADNPort + "/" + context);
		} catch (URISyntaxException e) {
			outStream.out2("failed");
			errStream.out(e,i,Severity.MEDIUM);
			return;
		}
		outStream.out1_2("done, registering to MN");
		response = register(tag,this.address);
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
		response = services.postAE(Services.normalizeName(tag.id),i);
		outStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:ae", //
				new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class}), i);
		i++;
		if (end>0) {
			Thread wd = new Watchdog(this);
			//wd.setDaemon(true);
			start = System.currentTimeMillis();
			wd.start();
		} else {
			start = System.currentTimeMillis();
		}
		while(System.currentTimeMillis()-start<end || end==0) {
			outStream.out("Waiting for subscriptions...", i);
			waitForSubscriptions();
			outStream.out("...received: \"" + getNotification() + "\" (by \"" + getNotifier() + "\")", i);
			i++;
		}
		// TODO, delete AE
		outStream.out("Terminating interface", i);
		// TODO, solve termination issues (due to wd this JVM doesn't terminate)
	}

}