package mecs.iot.proj.om2m.asn.actuator;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.asn.actuator.exceptions.ActionNumberMismatchException;
import mecs.iot.proj.om2m.dashboard.FactoryInterface;
import mecs.iot.proj.om2m.dashboard.Severity;
import mecs.iot.proj.om2m.asn.Action;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.ASN;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class RemoteInterface extends Client {
	
	private ASN tag;
	private int location;
	private long duration;
	
	private String address;
	
	private long start;
	
	private FactoryInterface viewer;
	private int viewerIndex;

	public RemoteInterface(ASN tag, int location, String uri, String context, boolean debug, Action[] actions, String ip, int port, String id, String host, long duration) throws URISyntaxException, ActionNumberMismatchException {
		super(tag.id, uri, debug);
		this.tag = tag;
		this.location = location;
		this.duration = duration;
		this.address = Constants.protocol + ip + ":" + Integer.toString(port) + "/" + context;
		ActuationUnit unit = new ActuationUnit(Format.joinIdHost(id+"/unit",host),tag.attributes,actions);
		createNotificationServer(Format.joinIdHost(id+"/ATserver",host),context,debug,unit,port);
	}
	
	public void add(FactoryInterface viewer, int index) {
		this.viewer = viewer;
		viewerIndex = index;
	}
	
	private class Watchdog extends Thread {
		
		Client lock;
		private long start_;
		
		Watchdog (Client lock) {
			super(Format.joinIdHost("watchdog",Constants.computerName()));
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
		outStream.out("Starting remote interface", i);
		outStream.out1("Locating node", i);
		CoapResponse response = locate(tag.id,tag.serial,location);
		if (response==null) {
			errStream.out("Unable to register to \"" + services.uri() + "\", timeout expired", i, Severity.LOW);
			destroy();
			outStream.out2("failed. Terminating remote interface");
			return;
		} else if (response.getCode()!=ResponseCode.CREATED) {
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to register to \"" + services.uri() + "\", response: " + response.getCode() +
						", reason: " + response.getResponseText(),
						i, Severity.LOW);
			else
				errStream.out("Unable to register to \"" + services.uri() + "\", response: " + response.getCode(),
						i, Severity.LOW);
			destroy();
			outStream.out2("failed. Terminating remote interface");
			return;
		}
		if (viewer!=null)
			viewer.touch(viewerIndex);
		String[] mnData = response.getResponseText().split(", "); 													// MN id and address
		String name = mnData[0];
		String address = mnData[1];
		outStream.out1_2("done, received \"" + name + "\" and \"" + address + "\" as MN id and address, connecting to ADN");
		try {
			connect(Constants.protocol + address + Constants.mnADNRoot);
		} catch (URISyntaxException e) {
			errStream.out(e,i,Severity.MEDIUM);
			destroy();
			outStream.out2("failed. Terminating remote interface");
			return;
		}
		outStream.out1_2("done, registering");
		response = register(tag,this.address);
		if (response==null) {
			errStream.out("Unable to register to \"" + services.uri() + "\", timeout expired", i, Severity.LOW);
			destroy();
			outStream.out2("failed. Terminating remote interface");
			return;
		} else if (response.getCode()!=ResponseCode.CREATED) {
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to register to \"" + services.uri() + "\", response: " + response.getCode() +
						", reason: " + response.getResponseText(),
						i, Severity.LOW);
			else
				errStream.out("Unable to register to \"" + services.uri() + "\", response: " + response.getCode(),
					i, Severity.LOW);
			destroy();
			outStream.out2("failed. Terminating remote interface");
			return;
		}
		if (viewer!=null)
			viewer.touch(viewerIndex);
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
		deleteNode(tag.serial);
		if (viewer!=null)
			viewer.touch(viewerIndex);
		destroy();
		outStream.out("Terminating remote interface", i);
	}

}