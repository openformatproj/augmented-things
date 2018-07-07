package mecs.iot.proj.om2m.adn.mn;

import java.util.HashMap;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.json.JSONObject;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.adn.mn.exceptions.StateCreationException;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.exceptions.InvalidRuleException;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class Subscriber {
	
	private HashMap<String,ArrayList<Subscription>> subscriptionMap;										// resource id -> list of subscriptions
	private HashMap<String,String> resourceMap;																// cnt key -> resource id
	
	private String cseBaseName;
	
	private DebugStream debugStream;
	private ErrStream errStream;
	private Client cseClient;
	
	public Subscriber(DebugStream debugStream, ErrStream errStream, Client cseClient, String cseBaseName) throws URISyntaxException, StateCreationException {
		// TODO: pull from OM2M
		this.debugStream = debugStream;
		this.errStream = errStream;
		this.cseClient = cseClient;
		subscriptionMap = new HashMap<String,ArrayList<Subscription>>();
		resourceMap = new HashMap<String,String>();
		this.cseBaseName = cseBaseName;
		CoapResponse response;
		debugStream.out("Posting subscriptionMap",0);
		cseClient.stepCount();
		response = cseClient.services.postContainer(cseBaseName,"state","subscriptionMap",cseClient.getCount());
		if (response==null) {
			debugStream.out("failed",0);
			errStream.out("Unable to post Container to " + cseClient.services.uri() + ", timeout expired", 0, Severity.LOW);
			throw new StateCreationException();
		} else if (response.getCode()!=ResponseCode.CREATED && response.getCode()!=ResponseCode.FORBIDDEN) {
			debugStream.out("failed",0);
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response.getCode() +
						", reason: " + response.getResponseText(),
						0, Severity.LOW);
			else
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response.getCode(),
					0, Severity.LOW);
			throw new StateCreationException();
		}
		debugStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:cnt",
				new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class}), 0);
		debugStream.out("Posting resourceMap",0);
		cseClient.stepCount();
		response = cseClient.services.postContainer(cseBaseName,"state","resourceMap",cseClient.getCount());
		if (response==null) {
			debugStream.out("failed",0);
			errStream.out("Unable to post Container to " + cseClient.services.uri() + ", timeout expired", 0, Severity.LOW);
			throw new StateCreationException();
		} else if (response.getCode()!=ResponseCode.CREATED && response.getCode()!=ResponseCode.FORBIDDEN) {
			debugStream.out("failed",0);
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response.getCode() +
						", reason: " + response.getResponseText(),
						0, Severity.LOW);
			else
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response.getCode(),
					0, Severity.LOW);
			throw new StateCreationException();
		}
		debugStream.out("Received JSON: " + Services.parseJSON(response.getResponseText(), "m2m:cnt",
				new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class}), 0);
	}
	
	public void insert(String sender, String type, String receiver, String address, int i) throws URISyntaxException, StateCreationException {
		Subscription ref = new Subscription(sender,type,receiver,address);
		if (subscriptionMap.containsKey(sender)) {
			ArrayList<Subscription> subs = subscriptionMap.get(sender);
			subs.add(ref);
			oM2Mput(sender,subs,false,i);
		} else {
			ArrayList<Subscription> subs = new ArrayList<Subscription>();
			subs.add(ref);
			subscriptionMap.put(sender,subs);
			oM2Mput(sender,subs,true,i);
		}
	}
	
	public void insert(String sender, String type, String event, String rule, String receiver, String address, String action, int i) throws URISyntaxException, StateCreationException, InvalidRuleException {
		Subscription ref = new Subscription(sender,type,event,rule,receiver,address,action);
		if (subscriptionMap.containsKey(sender)) {
			ArrayList<Subscription> subs = subscriptionMap.get(sender);
			subs.add(ref);
			oM2Mput(sender,subs,false,i);
		} else {
			ArrayList<Subscription> subs = new ArrayList<Subscription>();
			subs.add(ref);
			subscriptionMap.put(sender,subs);
			oM2Mput(sender,subs,true,i);
		}
	}
	
	public void bind(String sender, String key) {
		resourceMap.put(key,sender);
		// TODO: push to OM2M
	}
	
	public ArrayList<Subscription> get(String pi) {
		return subscriptionMap.get(resourceMap.get(pi));
	}
	
	public String getResourceId(String key) {
		return resourceMap.get(key);
	}
	
	public void remove(String sender, Node node, int k) throws URISyntaxException, StateCreationException {
		switch(node) {
			case SENSOR:
				oM2Mput(sender,new ArrayList<Subscription>(),false,k);
				deleteSubscription(sender, k);																// ? TODO
				break;
			case ACTUATOR:
			case USER:
				String[] resources = subscriptionMap.keySet().toArray(new String[]{});
				ArrayList<Subscription> subs;
				for (int i=0; i<resources.length; i++) {
					subs = subscriptionMap.get(resources[i]);
					for (int j=0; j<subs.size(); j++) {
						if (subs.get(j).receiver.id.equals(sender))
							subs.remove(j);																	// Remove all subscriptions containing the receiver
							oM2Mput(sender,subs,false,k);
					}
					if (subs.size()==0) {																	// If there are no subscriptions anymore, remove the subscription to the corresponding resource
						deleteSubscription(resources[i], k);
					}
				}
				break;
		}
	}
	
	public void remove(String sender, String receiver, int k) throws URISyntaxException, StateCreationException {
		ArrayList<Subscription> subs = subscriptionMap.get(sender);
		for (int j=0; j<subs.size(); j++) {
			if (subs.get(j).receiver.id.equals(receiver))
				subs.remove(j);																				// Remove all subscriptions containing the receiver
				oM2Mput(sender,subs,false,k);
		}
		if (subs.size()==0) {
			deleteSubscription(sender, k);
		}
	}
	
	public void remove(String sender, String event, String receiver, String action, int k) throws URISyntaxException, StateCreationException {
		ArrayList<Subscription> subs = subscriptionMap.get(sender);
		Subscription ref;
		for (int j=0; j<subs.size(); j++) {
			ref = subs.get(j);
			if (ref.receiver.id.equals(receiver) && ref.event.equals(event) && ref.action.equals(action))
				subs.remove(j);																				// Remove all subscriptions both containing the receiver and matching the pair event/action
				oM2Mput(sender,subs,false,k);
		}
		if (subs.size()==0) {
			deleteSubscription(sender, k);
		}
	}
	
	private void deleteSubscription(String resource, int i) throws URISyntaxException, StateCreationException {
		String[] uri = new String[] {cseBaseName, resource, "data", "subscription"};
		CoapResponse response = null;
		debugStream.out("Deleting subscription on \"" + resource + "\"", i);
		cseClient.stepCount();
		response = cseClient.services.deleteSubscription(uri,cseClient.getCount());
		if (response==null) {
			debugStream.out("failed",i);
			errStream.out("Unable to delete subscription on \"" + resource + "\", timeout expired" , i, Severity.LOW);
			throw new StateCreationException();
		} else if (response.getCode()!=ResponseCode.DELETED) {
			debugStream.out("failed",i);
			errStream.out("Unable to delete subscription on \"" + resource + "\", response: " + response.getCode(),
					i, Severity.LOW);
			throw new StateCreationException();
		}
		subscriptionMap.remove(resource);
		debugStream.out("...done",i);
	}
	
	private void oM2Mput (String sender, ArrayList<Subscription> subs, boolean createContainer, int i) throws URISyntaxException, StateCreationException {
		String[] uri = new String[] {cseBaseName, "state", "subscriptionMap"};
		CoapResponse response;
		debugStream.out("Posting subscriptionMap...",i);
		JSONObject obj = Services.toJSONArray(subs.toArray(new Subscription[] {}),"subs");
		obj.put("id",sender);
		obj.put("mn",cseBaseName);
		cseClient.stepCount();
		response = cseClient.services.oM2Mput(sender,obj,uri,createContainer,cseClient.getCount());
		if (response==null) {
			debugStream.out("failed",i);
			errStream.out("Unable to register subscription on CSE, timeout expired", i, Severity.LOW);
			throw new StateCreationException();
		} else if (response.getCode()!=ResponseCode.CREATED) {
			debugStream.out("failed",i);
			errStream.out("Unable to register subscription on CSE, response: " + response.getCode(),
					i, Severity.LOW);
			throw new StateCreationException();
		}
		debugStream.out("...done",i);
	}

}
