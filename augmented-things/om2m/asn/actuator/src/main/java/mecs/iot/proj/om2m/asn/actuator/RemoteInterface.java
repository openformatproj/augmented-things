package mecs.iot.proj.om2m.asn.actuator;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.asn.actuator.exceptions.ActionNumberMismatchException;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.asn.Action;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Tag;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class RemoteInterface extends Client {
	
	private Tag tag;
	private int location;
	private long duration;
	
	private String address;
	
	private long start;

	public RemoteInterface(Tag tag, int location, String uri, String context, boolean debug, Action[] actions, String ip, int port, String id, String host, long duration) throws URISyntaxException, ActionNumberMismatchException {
		super(tag.id, uri, debug);
		this.tag = tag;
		this.location = location;
		this.duration = duration;
		this.address = Constants.protocol + ip + ":" + Integer.toString(port) + "/" + context;
		ActuationUnit unit = new ActuationUnit(Services.joinIdHost(id+"/unit",host),tag.attributes,actions);
		createNotificationServer(Services.joinIdHost(id+"/ATserver",host),context,debug,unit,port);
	}
	
	private class Watchdog extends Thread {
		
		Client lock;
		private long start_;
		
		Watchdog (Client lock) {
			super(Services.joinIdHost("watchdog",Constants.computerName()));
			this.lock = lock;
			setDaemon(true);
		}
		
		@Override
		
	    public void run() {
			start_ = start;
			while (true) {
				while(System.currentTimeMillis()-start_<duration);
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
		outStream.out1("Locating node", i);
		CoapResponse response = locate(tag.id,tag.serial,location);
		if (response==null) {
			outStream.out2("failed. Terminating interface");
			errStream.out("Unable to register to " + services.uri() + ", timeout expired", i, Severity.LOW);
			return;
		} else if (response.getCode()!=ResponseCode.CREATED) {
			outStream.out2("failed. Terminating interface");
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to register to " + services.uri() + ", response: " + response.getCode() +
						", reason: " + response.getResponseText(),
						i, Severity.LOW);
			else
				errStream.out("Unable to register to " + services.uri() + ", response: " + response.getCode(),
						i, Severity.LOW);
			return;
		}
		String[] mnData = response.getResponseText().split(", "); 													// MN id and address
		String name = mnData[0];
		String address = mnData[1];
		outStream.out1_2("done, received \"" + name + "\" and \"" + address + "\" as MN id and address, connecting to ADN");
		try {
			connect(Constants.protocol + address + Constants.mnADNRoot);
		} catch (URISyntaxException e) {
			outStream.out2("failed. Terminating interface");
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		outStream.out1_2("done, registering");
		response = register(tag,this.address);
		if (response==null) {
			outStream.out2("failed. Terminating interface");
			errStream.out("Unable to register to " + services.uri() + ", timeout expired", i, Severity.LOW);
			return;
		} else if (response.getCode()!=ResponseCode.CREATED) {
			outStream.out2("failed. Terminating interface");
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to register to " + services.uri() + ", response: " + response.getCode() +
						", reason: " + response.getResponseText(),
						i, Severity.LOW);
			else
				errStream.out("Unable to register to " + services.uri() + ", response: " + response.getCode(),
					i, Severity.LOW);
			return;
		}
		outStream.out2("done");
		i++;
		if (duration>0) {
			Thread wd = new Watchdog(this);
			start = System.currentTimeMillis();
			wd.start();
		} else {
			start = System.currentTimeMillis();
		}
		while(System.currentTimeMillis()-start<duration || duration==0) {
			outStream.out1("Waiting for notifications", i);
			waitForNotifications();
			outStream.out2("received: \"" + getNotification() + "\" (by \"" + getNotifier() + "\")");
			i++;
		}
		deleteNodeAsync(tag.serial);
		outStream.out("Terminating interface", i);
	}

}