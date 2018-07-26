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
	private String id;
	
	private String address;
	
	private Console console;
	private boolean executing;

	RemoteInterface(String id, String host, String uri, String context, boolean debug, Console console, String ip, int port) throws URISyntaxException {
		super(Services.joinIdHost(id+"/remote",host), uri, debug);
		this.id = Services.joinIdHost(id,host);
		this.address = Constants.protocol + ip + ":" + Integer.toString(port) + "/" + context;
		this.console = console;
		CommandList list = new CommandList(this,console,this.id);
		for (int i=0; i<list.numCommands; i++) {
			console.add(list.text[i][0],list.getCommand(i),list.numOptions[i],list.text[i][1],list.text[i][2],list.isJSON[i]);
		}
		executing = true;
		ConsoleWrapper unit = new ConsoleWrapper(Services.joinIdHost(id+"/unit",host),console);
		createNotificationServer(Services.joinIdHost(id+"/ATserver",host),context,debug,unit,port);
	}
	
	@Override
	
	public void run() {
		outStream.out("Starting remote interface", i);
		boolean foundSerial = false;
		CoapResponse response = null;
		while (!foundSerial) {
			serial = console.getSerial();
			outStream.out1("Locating serial \"" + serial + "\"", i);
			response = locate(serial);
			if (response==null) {
				errStream.out("Unable to locate the user on \"" + services.uri() + "\", timeout expired", i, Severity.LOW);
				outStream.out2("failed. Retrying");
			} else if (response.getCode()!=ResponseCode.CONTENT) {
				if (!response.getResponseText().isEmpty())
					errStream.out("Unable to locate the user on \"" + services.uri() + "\", response: " + response.getCode() +
							", reason: " + response.getResponseText(),
							i, Severity.LOW);
				else
					errStream.out("Unable to locate the user on \"" + services.uri() + "\", response: " + response.getCode(),
							i, Severity.LOW);
				outStream.out2("failed. Retrying");
			} else {
				foundSerial = true;
			}
		}
		String[] mnData = response.getResponseText().split(", "); 													// MN id and address
		String name = mnData[0];
		String address = mnData[1];
		outStream.out1_2("done, received \"" + name + "\" and \"" + address + "\" as MN id and address, connecting to ADN");
		try {
			connect(Constants.protocol + address + Constants.mnADNRoot);
		} catch (URISyntaxException e) {
			errStream.out(e,i,Severity.MEDIUM);
			outStream.out2("failed. Terminating remote interface");
			return;
		}
		outStream.out1_2("done, registering");
		response = register(id,this.address);
		if (response==null) {
			errStream.out("Unable to register to \"" + services.uri() + "\", timeout expired", i, Severity.LOW);
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
			outStream.out2("failed. Terminating remote interface");
			return;
		}
		outStream.out2("done");
		i++;
		console.start();
		while(executing) {
			outStream.out1("Waiting for notifications", i);
			waitForNotifications();
			outStream.out2("received: \"" + getNotification() + "\" (by \"" + getNotifier() + "\")");
			i++;
		}
		deleteUser(Services.normalizeName(id));
		destroy();
		outStream.out("Terminating remote interface", i);
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
