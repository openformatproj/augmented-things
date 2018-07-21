package mecs.iot.proj.om2m.asn.sensor;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.structures.Physics;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Tag;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class RemoteInterface extends Client {
	
	private double value;
	private double fluctuation;
	private Tag tag;
	private int location;
	private long duration;
	private long period;
	
	private long start;

	public RemoteInterface(Tag tag, int location, String uri, String context, boolean debug, double value, double fluctuation, long duration, long period) throws URISyntaxException {
		super(tag.id, uri, debug);
		this.value = value;
		this.fluctuation = fluctuation;
		this.tag = tag;
		this.location = location;
		this.duration = duration;
		this.period = period;
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
		response = register(tag);
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
		start = System.currentTimeMillis();
		long timer;
		while(System.currentTimeMillis()-start<duration || duration==0) {
			outStream.out1("Posting Content Instance", i);
			publish(tag.id,Format.pack(value*Physics.randomGaussianFluctuation(fluctuation),tag.type));
			outStream.out2("done");
			i++;
			timer = System.currentTimeMillis();
			while (System.currentTimeMillis()-timer<period);
		}
		deleteNodeAsync(tag.serial);
		outStream.out("Terminating interface", i);
	}

}
