package mecs.iot.proj.om2m.adn.mn;

import java.util.HashMap;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.json.JSONException;
import org.json.JSONObject;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.adn.mn.exceptions.StateCreationException;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.dashboard.Severity;
import mecs.iot.proj.om2m.exceptions.InvalidRuleException;
import mecs.iot.proj.om2m.exceptions.NoTypeException;
import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.Tag;

import java.net.URISyntaxException;
import java.util.ArrayList;

class Subscriber {
	
	private HashMap<String,ArrayList<Subscription>> subscriptionMap;										// resource id -> list of subscriptions
	
	private String cseBaseName;
	
	private DebugStream debugStream;
	private ErrStream errStream;
	private Client cseClient;
	
	Subscriber(OutStream outStream, DebugStream debugStream, ErrStream errStream, Client cseClient, String cseBaseName, int k) throws URISyntaxException, StateCreationException {
		this.debugStream = debugStream;
		this.errStream = errStream;
		this.cseClient = cseClient;
		this.cseBaseName = cseBaseName;
		subscriptionMap = new HashMap<String,ArrayList<Subscription>>();
		String json = null;
		debugStream.out("Posting subscriptionMap",k);
		cseClient.stepCount();
		CoapResponse response = cseClient.services.postContainer(cseBaseName,"state","subscriptionMap",cseClient.getCount());
		if (response==null) {
			errStream.out("Unable to post Container to " + cseClient.services.uri() + ", timeout expired", k, Severity.HIGH);
			outStream.out2("failed");
			throw new StateCreationException();
		} else if (response.getCode()!=ResponseCode.CREATED/* && response.getCode()!=ResponseCode.FORBIDDEN*/) {
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response.getCode() +
						", reason: " + response.getResponseText(),
						k, Severity.HIGH);
			else
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response.getCode(),
					k, Severity.HIGH);
			outStream.out2("failed");
			throw new StateCreationException();
		}
		try {
			json = Services.parseJSON(response.getResponseText(), "m2m:cnt",
					new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
		} catch (JSONException e) {
			errStream.out(e,k,Severity.MEDIUM);
			outStream.out2("failed");
			throw e;
		}
		debugStream.out("Received JSON: " + json, k);
		debugStream.out("Posting resourceMap",k);
		cseClient.stepCount();
		response = cseClient.services.postContainer(cseBaseName,"state","resourceMap",cseClient.getCount());
		if (response==null) {
			errStream.out("Unable to post Container to " + cseClient.services.uri() + ", timeout expired", k, Severity.HIGH);
			outStream.out2("failed");
			throw new StateCreationException();
		} else if (response.getCode()!=ResponseCode.CREATED/* && response.getCode()!=ResponseCode.FORBIDDEN */) {
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response.getCode() +
						", reason: " + response.getResponseText(),
						k, Severity.HIGH);
			else
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response.getCode(),
					k, Severity.HIGH);
			outStream.out2("failed");
			throw new StateCreationException();
		}
		try {
			json = Services.parseJSON(response.getResponseText(), "m2m:cnt",
					new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
		} catch (JSONException e) {
			errStream.out(e,k,Severity.MEDIUM);
			outStream.out2("failed");
			throw e;
		}
		debugStream.out("Received JSON: " + json, 0);
	}
	
//	void insert(String sender, String type, String receiver, String address, int k) throws URISyntaxException, StateCreationException {
//		Subscription ref = new Subscription(sender,type,receiver,address);
//		if (subscriptionMap.containsKey(sender)) {
//			ArrayList<Subscription> subs = subscriptionMap.get(sender);
//			ArrayList<Subscription> subs_ = cloneList(subs);					// Clone before passing to oM2Mput in order to prevent inconsistencies between local cache and CSE
////			subs.add(ref);
////			oM2Mput(sender,subs,false,k);
//			subs_.add(ref);
//			oM2Mput(sender,subs_,false,k);
//			debugStream.out("Creating subscription on \"" + sender + "\"", k);
//			subs.add(ref);
//		} else {
//			ArrayList<Subscription> subs = new ArrayList<Subscription>();
//			subs.add(ref);
//			oM2Mput(sender,subs,true,k);
//			debugStream.out("Creating subscription on \"" + sender + "\"", k);
//			subscriptionMap.put(sender,subs);
//		}
//	}
//	
//	void insert(String sender, String type, String event, String rule, String receiver, String address, String action, int k) throws URISyntaxException, StateCreationException, InvalidRuleException, NoTypeException {
//		Subscription ref = new Subscription(sender,type,event,rule,receiver,address,action);
//		if (subscriptionMap.containsKey(sender)) {
//			ArrayList<Subscription> subs = subscriptionMap.get(sender);
//			ArrayList<Subscription> subs_ = cloneList(subs);					// Clone before passing to oM2Mput in order to prevent inconsistencies between local cache and CSE
////			subs.add(ref);
////			oM2Mput(sender,subs,false,k);
//			subs_.add(ref);
//			oM2Mput(sender,subs_,false,k);
//			debugStream.out("Creating subscription on \"" + sender + "\"", k);
//			subs.add(ref);
//		} else {
//			ArrayList<Subscription> subs = new ArrayList<Subscription>();
//			subs.add(ref);
//			oM2Mput(sender,subs,true,k);
//			debugStream.out("Creating subscription on \"" + sender + "\"", k);
//			subscriptionMap.put(sender,subs);
//		}
//	}
	
	void insert(String senderSerial, Tag senderTag, String receiverSerial, Tag receiverTag, int k) throws URISyntaxException, StateCreationException {
		Subscription ref = new Subscription(senderSerial,senderTag,receiverSerial,receiverTag);
		if (subscriptionMap.containsKey(senderTag.id)) {
			ArrayList<Subscription> subs = subscriptionMap.get(senderTag.id);
			ArrayList<Subscription> subs_ = cloneList(subs);												// Clone before passing to oM2Mput in order to prevent inconsistencies between local cache and CSE
			subs_.add(ref);
			oM2Mput(senderTag.id,subs_,false,k);
			debugStream.out("Creating subscription on \"" + senderTag.id + "\"", k);
			subs.add(ref);
		} else {
			ArrayList<Subscription> subs = new ArrayList<Subscription>();
			subs.add(ref);
			oM2Mput(senderTag.id,subs,true,k);
			debugStream.out("Creating subscription on \"" + senderTag.id + "\"", k);
			subscriptionMap.put(senderTag.id,subs);
		}
	}
	
	void insert(String senderSerial, Tag senderTag, String event, String rule, String receiverSerial, Tag receiverTag, String action, int k) throws URISyntaxException, StateCreationException, InvalidRuleException, NoTypeException {
		Subscription ref = new Subscription(senderSerial,senderTag,event,rule,receiverSerial,receiverTag,action);
		if (subscriptionMap.containsKey(senderTag.id)) {
			ArrayList<Subscription> subs = subscriptionMap.get(senderTag.id);
			ArrayList<Subscription> subs_ = cloneList(subs);												// Clone before passing to oM2Mput in order to prevent inconsistencies between local cache and CSE
			subs_.add(ref);
			oM2Mput(senderTag.id,subs_,false,k);
			debugStream.out("Creating subscription on \"" + senderTag.id + "\"", k);
			subs.add(ref);
		} else {
			ArrayList<Subscription> subs = new ArrayList<Subscription>();
			subs.add(ref);
			oM2Mput(senderTag.id,subs,true,k);
			debugStream.out("Creating subscription on \"" + senderTag.id + "\"", k);
			subscriptionMap.put(senderTag.id,subs);
		}
	}
	
	ArrayList<Subscription> get(String id) {
		return subscriptionMap.get(id);
	}
	
	void remove(String id, Node node, int k) throws URISyntaxException, StateCreationException {
		switch(node) {
			case SENSOR:
				oM2Mput(id,new ArrayList<Subscription>(),false,k);
				debugStream.out("Deleting subscription on \"" + id + "\"", k);
				subscriptionMap.remove(id);
				break;
			case ACTUATOR:
			case USER:
				String[] resources = subscriptionMap.keySet().toArray(new String[]{});
				ArrayList<Subscription> subs;
				for (int i=0; i<resources.length; i++) {
					subs = subscriptionMap.get(resources[i]);
					for (int j=0; j<subs.size(); j++) {
						if (subs.get(j).receiver.tag.id.equals(id))
							subs.remove(j);																	// Remove all subscriptions containing the receiver
					}
					oM2Mput(resources[i],subs,false,k);
					if (subs.size()==0) {																	// If there are no subscriptions anymore, remove the subscription to the corresponding resource
						debugStream.out("Deleting subscription on \"" + resources[i] + "\"", k);
						subscriptionMap.remove(resources[i]);
					}
				}
				break;
		}
	}
	
	void remove(String sender, String receiver, int k) throws URISyntaxException, StateCreationException {
		ArrayList<Subscription> subs = subscriptionMap.get(sender);
		for (int j=0; j<subs.size(); j++) {
			if (subs.get(j).receiver.tag.id.equals(receiver))
				subs.remove(j);																				// Remove all subscriptions containing the receiver
		}
		oM2Mput(sender,subs,false,k);
		if (subs.size()==0) {
			debugStream.out("Deleting subscription on \"" + sender + "\"", k);
			subscriptionMap.remove(sender);
		}
	}
	
	void remove(String sender, String event, String receiver, String action, int k) throws URISyntaxException, StateCreationException {
		ArrayList<Subscription> subs = subscriptionMap.get(sender);
		Subscription ref;
		for (int j=0; j<subs.size(); j++) {
			ref = subs.get(j);
			if (ref.receiver.tag.id.equals(receiver) && ref.event.equals(event) && ref.action.equals(action))
				subs.remove(j);																				// Remove all subscriptions both containing the receiver and matching the pair event/action
		}
		oM2Mput(sender,subs,false,k);
		if (subs.size()==0) {
			debugStream.out("Deleting subscription on \"" + sender + "\"", k);
			subscriptionMap.remove(sender);
		}
	}
	
	private void oM2Mput(String sender, ArrayList<Subscription> subs, boolean createContainer, int k) throws URISyntaxException, StateCreationException {
		CoapResponse response;
		debugStream.out("Posting subscriptionMap",k);
		JSONObject obj = Services.toJSONArray(subs.toArray(new Subscription[] {}),"subs");
		obj.put("id",sender);
		obj.put("mn",cseBaseName);
		cseClient.stepCount();
		if (createContainer) {
			String[] uri = new String[] {cseBaseName, "state", "subscriptionMap"};
			response = cseClient.services.oM2Mput(sender,obj,uri,true,cseClient.getCount());
		} else {
			String[] uri = new String[] {cseBaseName, "state", "subscriptionMap", sender};
			response = cseClient.services.oM2Mput(sender,obj,uri,false,cseClient.getCount());
		}	
		if (response==null) {
			errStream.out("Unable to register subscription on CSE, timeout expired", k, Severity.MEDIUM);
			throw new StateCreationException();
		} else if (response.getCode()!=ResponseCode.CREATED) {
			errStream.out("Unable to register subscription on CSE, response: " + response.getCode(),
					k, Severity.MEDIUM);
			throw new StateCreationException();
		}
	}
	
	private static ArrayList<Subscription> cloneList(ArrayList<Subscription> in) {
		ArrayList<Subscription> out = new ArrayList<Subscription>(in.size());
	    for (Subscription item : in)
	    	out.add((Subscription)item.clone());
	    return out;
	}

}
