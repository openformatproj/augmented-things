package mecs.iot.proj.om2m.asn.sensor;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.asn.Physics;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Tag;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class RemoteInterface extends Client {
	
	private double value;
//	private String metadata;
	private double fluctuation;
	private String context;
	private Tag tag;
	private int location;
	private long duration;
	private long period;
	
	private long start;

	public RemoteInterface(Tag tag, int location, String uri, String context, boolean debug, double value, double fluctuation, long duration, long period) throws URISyntaxException {
		super(tag.id, uri, debug);
		this.value = value;
//		this.metadata = Format.get(tag.type);
		this.fluctuation = fluctuation;
		this.context = context;
		this.tag = tag;
		this.location = location;
		this.duration = duration;
		this.period = period;
	}
	
	@Override
	
	public void run() {
		outStream.out("Starting interface", i);
		outStream.out1("Registering to IN", i);
		CoapResponse response = register(tag,location);
		if (response==null) {
			outStream.out2("failed. Terminating interface");
			errStream.out("Unable to register to " + services.uri() + ", timeout expired", i, Severity.LOW);
			return;
		} else if (response.getCode()!=ResponseCode.CREATED) {
			outStream.out2("failed. Terminating interface");
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
		String id = mnData[0];
		String address = mnData[1];
		outStream.out1_2("done, received " + address + " as MN address, connecting");
		try {
			connect(Constants.adnProtocol + address + Constants._mnADNPort + "/" + context);
		} catch (URISyntaxException e) {
			outStream.out2("failed. Terminating interface");
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		outStream.out1_2("done, registering to MN");
		response = register(tag);
		if (response==null) {
			outStream.out2("failed. Terminating interface");
			errStream.out("Unable to register to " + services.uri() + ", timeout expired", i, Severity.LOW);
			return;
		} else if (response.getCode()!=ResponseCode.CREATED) {
			outStream.out2("failed. Terminating interface");
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
			deleteNodeAsync(tag.serial);
			outStream.out2("failed. Terminating interface");
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		outStream.out2("done, posting AE");
		response = services.postAE(Services.normalizeName(tag.id),i);
		if (response==null) {
			deleteNodeAsync(tag.serial);
			outStream.out("failed. Terminating interface",i);
			errStream.out("Unable to post AE to " + services.uri() + ", timeout expired", i, Severity.LOW);
			return;
		} else if (response.getCode()!=ResponseCode.CREATED && response.getCode()!=ResponseCode.FORBIDDEN) {
			deleteNodeAsync(tag.serial);
			outStream.out("failed. Terminating interface",i);
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to post AE to " + services.uri() + ", response: " + response.getCode() + //
						", reason: " + response.getResponseText(), //
						i, Severity.LOW);
			else
				errStream.out("Unable to post AE to " + services.uri() + ", response: " + response.getCode(), //
					i, Severity.LOW);
			return;
		}
		outStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:ae", //
				new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class}), i);
		outStream.out("Posting Container", i);
		try {
			response = services.postContainer(id,Services.normalizeName(tag.id),i);
		} catch (URISyntaxException e) {
			deleteNodeAsync(tag.serial);
			outStream.out("failed. Terminating interface",i);
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		if (response==null) {
			deleteNodeAsync(tag.serial);
			outStream.out("failed. Terminating interface",i);
			errStream.out("Unable to post container to " + services.uri() + ", timeout expired", i, Severity.LOW);
			return;
		} else if (response.getCode()!=ResponseCode.CREATED && response.getCode()!=ResponseCode.FORBIDDEN) {
			deleteNodeAsync(tag.serial);
			outStream.out("failed. Terminating interface",i);
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to post AE to " + services.uri() + ", response: " + response.getCode() + //
						", reason: " + response.getResponseText(), //
						i, Severity.LOW);
			else
				errStream.out("Unable to post AE to " + services.uri() + ", response: " + response.getCode(), //
					i, Severity.LOW);
			return;
		}
		outStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:cnt", //
				new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class}), i);
		outStream.out("Posting Content Instance", i);
		try {
			response = services.postContentInstance(Format.pack(value,tag.type),i);
		} catch (URISyntaxException e) {
			deleteNodeAsync(tag.serial);
			outStream.out("failed. Terminating interface",i);
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		if (response==null) {
			deleteNodeAsync(tag.serial);
			outStream.out("failed. Terminating interface",i);
			errStream.out("Unable to post content instance to " + services.uri() + ", timeout expired", i, Severity.LOW);
			return;
		}
		outStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:cin", //
				new String[] {"ty","cnf","con"}, new Class<?>[] {Integer.class,String.class,String.class}), i);
		i++;
		start = System.currentTimeMillis();
		long timer;
		while(System.currentTimeMillis()-start<duration || duration==0) {
			outStream.out("Posting Content Instance", i);
			try {
				response = services.postContentInstance(Format.pack(value*Physics.randomFluctuation(fluctuation),tag.type),i);
			} catch (URISyntaxException e) {
				deleteNodeAsync(tag.serial);
				outStream.out("failed. Terminating interface",i);
				errStream.out(e, i, Severity.MEDIUM);
				return;
			}
			if (response==null) {
				deleteNodeAsync(tag.serial);
				outStream.out("failed. Terminating interface",i);
				errStream.out("Unable to post content instance to " + services.uri() + ", timeout expired", i, Severity.LOW);
				return;
			}
			outStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:cin", //
					new String[] {"ty","cnf","con"}, new Class<?>[] {Integer.class,String.class,String.class}), i);
			i++;
			timer = System.currentTimeMillis();
			while (System.currentTimeMillis()-timer<period);
		}
		deleteNodeAsync(tag.serial);
		outStream.out("Terminating interface", i);
	}

}
