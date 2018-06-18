package mecs.iot.proj.om2m.asn.sensor;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.asn.Physics;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Tag;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class RemoteInterface extends Client {
	
	private double value;
	private String measureUnit;
	private double fluctuation;
	private String context;
	private Tag tag;
	private int location;
	private long end;
	
	private long start;

	public RemoteInterface(Tag tag, int location, String uri, String context, boolean debug, double value, String measureUnit, double fluctuation, long end) throws URISyntaxException {
		super(tag.id, uri, debug);
		this.value = value;
		this.measureUnit = measureUnit;
		this.fluctuation = fluctuation;
		this.context = context;
		this.tag = tag;
		this.location = location;
		this.end = end;
	}
	
	@Override
	
	public void run() {
		outStream.out("Starting interface", i);
		outStream.out1("Registering to IN", i);
		CoapResponse response = register(tag,location);
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
		String id = mnData[0];
		String address = mnData[1];
		outStream.out1_2("done, received " + address + " as MN address, connecting");
		try {
			connect(Constants.adnProtocol + address + Constants._mnADNPort + "/" + context);
		} catch (URISyntaxException e) {
			outStream.out2("failed");
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		outStream.out1_2("done, registering to MN");
		response = register(tag);
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
		outStream.out("Posting Container", i);
		try {
			response = services.postContainer(id,Services.normalizeName(tag.id),i);
		} catch (URISyntaxException e) {
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		outStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:cnt", //
				new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class}), i);
		outStream.out("Posting Content Instance", i);
		try {
			response = services.postContentInstance(value,measureUnit,i);
		} catch (URISyntaxException e) {
			errStream.out(e, i, Severity.MEDIUM);
			return;
		}
		outStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:cin", //
				new String[] {"ty","cnf","con"}, new Class<?>[] {Integer.class,String.class,String.class}), i);
		i++;
		start = System.currentTimeMillis();
		while(System.currentTimeMillis()-start<end || end==0) {
			try        
			{
			    sleep((long)Physics.randomFluctuation(0.5));
			} 
			catch(InterruptedException ex) 
			{
			    Thread.currentThread().interrupt();
			}
			outStream.out("Posting Content Instance", i);
			try {
				response = services.postContentInstance(value*Physics.randomFluctuation(fluctuation),measureUnit,i);
			} catch (URISyntaxException e) {
				errStream.out(e, i, Severity.MEDIUM);
				return;
			}
			outStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:cin", //
					new String[] {"ty","cnf","con"}, new Class<?>[] {Integer.class,String.class,String.class}), i);
			i++;
		}
		// TODO, delete AE
		outStream.out("Terminating interface", i);
	}

}
