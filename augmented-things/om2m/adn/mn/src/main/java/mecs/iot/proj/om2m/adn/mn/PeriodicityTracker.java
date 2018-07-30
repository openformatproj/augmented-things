package mecs.iot.proj.om2m.adn.mn;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.adn.mn.exceptions.StateCreationException;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.dashboard.Severity;
import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.ASN;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

class PeriodicityTracker extends Thread {
	
	private Client cseClient;
	
	private OutStream outStream;
	private ErrStream errStream;
	
	private HashMap<String,ASN> tagMap;																					// id -> tag
	private HashMap<String,String> serialMap;																			// id -> serial
	
	private Subscriber subscriber;
	
	private String cseBaseName;
	private int i;
	
	PeriodicityTracker(String name, Client cseClient, Subscriber subscriber, String cseBaseName, HashMap<String,ASN> tagMap) {
		super(name);
		this.cseClient = cseClient;
		this.subscriber = subscriber;
		this.cseBaseName = cseBaseName;
		this.tagMap = tagMap;
		serialMap = new HashMap<String,String>();
		outStream = new OutStream(name);
		errStream = new ErrStream(name);
		i = 0;
	}
	
	void insert(String id, String serial) {
		serialMap.put(id,serial);
	}
	
	void track(String id) {
		// TODO
	}
	
	@Override
	public void run() {
		// TODO
	}
	
	private void delete(String id) {
		ASN tag = tagMap.get(id);
		String serial = serialMap.get(id);
		// TODO: handle not existing id
		outStream.out1("Handling removal of node with serial \"" + serial + "\"", i);
		CoapResponse response_ = null;
		try {
			subscriber.remove(tag.id,Node.SENSOR,i);
		} catch (URISyntaxException | StateCreationException e) {
			errStream.out(e,i,Severity.HIGH);
			outStream.out2("failed");
			i++;
			return;
		}
		tag.active = false;
		String[] uri_ = new String[] {cseBaseName, "state", "tagMap", serial};
		cseClient.stepCount();
		try {
			response_ = cseClient.services.oM2Mput(serial,tag,uri_,false,cseClient.getCount());
		} catch (URISyntaxException e) {
			errStream.out(e,i,Severity.HIGH);
			outStream.out2("failed");
			i++;
			return;
		}
		if (response_==null) {
			errStream.out("Unable to remove node from CSE, timeout expired", i, Severity.HIGH);
			outStream.out2("failed");
			i++;
			return;
		} else if (response_.getCode()!=ResponseCode.CREATED) {
			errStream.out("Unable to remove node from CSE, response: " + response_.getCode(),
					i, Severity.HIGH);
			outStream.out2("failed");
			i++;
			return;
		}
		outStream.out2("done");
		i++;
	}

}
