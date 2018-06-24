package mecs.iot.proj.om2m.adn;

import java.util.HashMap;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.Severity;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class Subscriber {
	
	private HashMap<String,ArrayList<Reference>> referenceMap;												// resource -> list of references
	private HashMap<String,String> piMap;
	private String lastResource;
	
	private DebugStream debugStream;
	private ErrStream errStream;
	private Client cseClient;
	private String context;
	
	public Subscriber(DebugStream debugStream, ErrStream errStream, Client cseClient, String context) {
		referenceMap = new HashMap<String,ArrayList<Reference>>();
		piMap = new HashMap<String,String>();
		this.debugStream = debugStream;
		this.errStream = errStream;
		this.cseClient = cseClient;
		this.context = context;
	}
	
	public void insert(String sender, String receiver, String address) {
		Reference ref = new Reference(sender,receiver,address);
		if (referenceMap.containsKey(sender)) {
			referenceMap.get(sender).add(ref);
		} else {
			ArrayList<Reference> refs = new ArrayList<Reference>();
			refs.add(ref);
			referenceMap.put(sender,refs);
		}
		lastResource = sender;
	}
	
	public void insert(String sender, String event, String receiver, String address, String action) {
		Reference ref = new Reference(sender,event,receiver,address,action);
		if (referenceMap.containsKey(sender)) {
			referenceMap.get(sender).add(ref);
		} else {
			ArrayList<Reference> refs = new ArrayList<Reference>();
			refs.add(ref);
			referenceMap.put(sender,refs);
		}
		lastResource = sender;
	}
	
	// TODO: push referenceMap pair (sensor,refs) into CSE for IN-MN synchronization
	
	public boolean containsResource(String sender) {
		return piMap.containsValue(sender);
	}
	
	public boolean containsKey(String pi) {
		return piMap.containsKey(pi);
	}
	
	public void bindToLastResource(String pi) {
		piMap.put(pi,lastResource);
	}
	
	public ArrayList<Reference> get(String pi) {
		return referenceMap.get(piMap.get(pi));
	}
	
	public String getName(String pi) {
		return piMap.get(pi);
	}
	
	public void remove(String id, Node node, int k) throws URISyntaxException {
		switch(node) {
			case SENSOR:
				referenceMap.remove(id);
				break;
			case ACTUATOR:
			case USER:
				String[] resources = referenceMap.keySet().toArray(new String[]{});
				ArrayList<Reference> refs;
				for (int i=0; i<resources.length; i++) {
					refs = referenceMap.get(resources[i]);
					for (int j=0; j<refs.size(); j++) {
						if (refs.get(j).receiver.equals(id))
							refs.remove(j);																	// Remove all references containing the receiver
					}
					if (refs.size()==0) {																	// If there are no references anymore, remove the subscription to the corresponding resource
						debugStream.out("Deleting subscription on \"" + resources[i] + "\"", k);
						String[] uri = new String[] {context + Constants.mnPostfix, resources[i], "data", "subscription"};
						CoapResponse response_ = null;
						cseClient.stepCount();
						response_ = cseClient.services.deleteSubscription(uri,cseClient.getCount());
						if (response_==null || response_.getCode()!=ResponseCode.DELETED) {
							errStream.out("Unable to delete subscription on \"" + resources[i] + "\", response: " + response_.getCode(), //
									i, Severity.LOW);
						}
						referenceMap.remove(resources[i]);
					}
				}
				break;
		}
	}

}
