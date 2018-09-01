package mecs.iot.proj.om2m.asn.sensor;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.dashboard.FactoryInterface;
import mecs.iot.proj.om2m.dashboard.Severity;
import mecs.iot.proj.om2m.exceptions.NoTypeException;
import mecs.iot.proj.om2m.structures.Physics;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.ASN;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class RemoteInterface extends Client {
	
	private double value;
	private double fluctuation;
	private ASN tag;
	private int location;
	private long duration;
	private long period;
	
	private long start;
	
	private FactoryInterface viewer;
	private int viewerIndex;

	public RemoteInterface(ASN tag, int location, String uri, String context, boolean debug, double value, double fluctuation, long duration, long period) throws URISyntaxException {
		super(tag.id, uri, debug);
		this.value = value;
		this.fluctuation = fluctuation;
		this.tag = tag;
		this.location = location;
		this.duration = duration;
		this.period = period;
	}
	
	public void add(FactoryInterface viewer, int index) {
		this.viewer = viewer;
		viewerIndex = index;
	}
	
	@Override
	public void run() {
		outStream.out("Starting remote interface", i);
		outStream.out1("Locating node", i);
		if (viewer!=null)
			viewer.touch(viewerIndex, "Locating");
		CoapResponse response = locate(tag.id,tag.serial,location);
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
		if (viewer!=null)
			viewer.touch(viewerIndex, "Registering");
		response = register(tag,period);
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
		start = System.currentTimeMillis();
		long timer;
		while(System.currentTimeMillis()-start<duration || duration==0) {
			outStream.out1("Posting Content Instance", i);
			String datum = null;
			try {
				datum = Format.pack(value*Physics.randomGaussianFluctuation(fluctuation),tag.type);
			} catch (NoTypeException e) {
				errStream.out(e,i,Severity.MEDIUM);
				deleteNodeAsync(tag.serial);
				outStream.out2("failed. Terminating remote interface");
			}
			publish(tag.id,tag.serial,datum);
			if (viewer!=null)
				viewer.touch(viewerIndex, "Published: " + datum);
			outStream.out2("done");
			i++;
			timer = System.currentTimeMillis();
			while (System.currentTimeMillis()-timer<period);
		}
		if (viewer!=null)
			viewer.touch(viewerIndex, "Unregistering");
		deleteNode(tag.serial);
		outStream.out("Terminating remote interface", i);
	}

}
