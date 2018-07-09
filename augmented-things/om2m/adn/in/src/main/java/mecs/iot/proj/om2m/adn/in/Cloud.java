package mecs.iot.proj.om2m.adn.in;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.adn.in.exceptions.NotFoundMNException;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.structures.Node;

class Cloud {
	
	private HashMap<String,MN> mnMap;
	private DebugStream debugStream;
	
	Cloud(DebugStream debugStream) {
		mnMap = new HashMap<String,MN>();
		this.debugStream = debugStream;
	}
	
	void addMN(String id) {
		mnMap.put(id,new MN(id,debugStream));
	}
	
	void add(String json, int k) throws JSONException, NotFoundMNException {
		String mn = null;
		String id = null;
		String type = null;
		String address = null;
		String active = null;
		String[] attributes = null;
		try {
			mn = Services.parseJSONObject(json,"mn",String.class);
			id = Services.parseJSONObject(json,"id",String.class);
		} catch (JSONException e) {
			throw new JSONException("Invalid JSON");
		}
		MN root = mnMap.get(mn);
		if (root==null)
			throw new NotFoundMNException();
		try {
			type = Services.parseJSONObject(json,"type",String.class);
			active = Services.parseJSONObject(json,"active",Boolean.class);
			attributes = Services.parseJSONArray(json,"attributes");
			if (Boolean.parseBoolean(active)) {
				if (type.equals("act"))
					root.addTag(id,attributes,k);
				else
					root.addTag(id,type,attributes,k);
			}
			else
				root.removeTag(id,k);
		} catch (JSONException e1) {
			try {
				address = Services.parseJSONObject(json,"address",String.class);
				active = Services.parseJSONObject(json,"active",Boolean.class);
				if (Boolean.parseBoolean(active))
					root.addUser(id,address,k);
				else
					root.removeUser(id,k);
			} catch (JSONException e2) {
				ArrayList<String> actuators = new ArrayList<String>();
				ArrayList<String> users = new ArrayList<String>();
				ArrayList<String> events = new ArrayList<String>();
				ArrayList<String> actions = new ArrayList<String>();
				try {
					String[] subs = Services.parseJSONArray(json,"subs");
					String receiverType;
					String receiverId;
					for (int i=0; i<subs.length; i++) {
						receiverType = Services.parseJSONObject(subs[i],"receiver","node",String.class); // TODO: check type
						receiverId = Services.parseJSONObject(subs[i],"receiver","id",String.class);
						if (receiverType.equals(Node.ACTUATOR)) {
							actuators.add(receiverId);
							events.add(Services.parseJSONObject(subs[i],"event",String.class));
							actions.add(Services.parseJSONObject(subs[i],"action",String.class));
						} else if (receiverType.equals(Node.USER)) {
							users.add(receiverId);
						}
					}
					root.removeSubscriptions(id,k,false);
					for (int i=0; i<actuators.size(); i++)
						root.addSubscription(id,events.get(i),actuators.get(i),actions.get(i),k);
					for (int i=0; i<users.size(); i++)
						root.addSubscription(id,users.get(i),k);
				} catch (JSONException e3) {
					root.removeSubscriptions(id,k,true);
					return;
				}
				return;
			}
			return;
		}
	}

}

class MN {
	
	public String id;
	
	private HashMap<String,Tag> tagMap;																		// serial -> tag
	private HashMap<String,User> userMap;																	// user id -> user
	private HashMap<String,ArrayList<Subscription>> subscriptionMap;										// resource id -> list of subscriptions
	
	private DebugStream debugStream;
	
	MN(String id, DebugStream debugStream) {
		this.id = id;
		tagMap = new HashMap<String,Tag>();
		userMap = new HashMap<String,User>();
		subscriptionMap = new HashMap<String,ArrayList<Subscription>>();
		this.debugStream = debugStream;
	}
	
	void addTag(String id, String type, String[] attributes, int k) {
		debugStream.out("Adding endpoint node \"" + id + "\" to the cloud", k);
		tagMap.put(id,new Tag(id,type,attributes));
	}
	
	void addTag(String id, String[] attributes, int k) {
		debugStream.out("Adding endpoint node \"" + id + "\" to the cloud", k);
		tagMap.put(id,new Tag(id,attributes));
	}
	
	void removeTag(String id, int k) {
		debugStream.out("Removing endpoint node \"" + id + "\" from the cloud", k);
		tagMap.remove(id);
	}
	
	void addUser(String id, String address, int k) {
		debugStream.out("Adding user \"" + id + "\" to the cloud", k);
		userMap.put(id,new User(id,address));
	}
	
	void removeUser(String id, int k) {
		debugStream.out("Removing user \"" + id + "\" from the cloud", k);
		userMap.remove(id);
	}
	
	void addSubscription(String sender, String receiver, int k) {
		debugStream.out("Adding subscription between \"" + sender + "\" and \"" + receiver + "\" to the cloud", k);
		Subscription ref = new Subscription(sender,receiver);
		if (subscriptionMap.containsKey(sender)) {
			ArrayList<Subscription> subs = subscriptionMap.get(sender);
			subs.add(ref);
		} else {
			ArrayList<Subscription> subs = new ArrayList<Subscription>();
			subs.add(ref);
			subscriptionMap.put(sender,subs);
		}
	}
	
	void addSubscription(String sender, String event, String receiver, String action, int k) {
		debugStream.out("Adding subscription between \"" + sender + "\" and \"" + receiver + "\" to the cloud", k);
		Subscription ref = new Subscription(sender,event,receiver,action);
		if (subscriptionMap.containsKey(sender)) {
			ArrayList<Subscription> subs = subscriptionMap.get(sender);
			subs.add(ref);
		} else {
			ArrayList<Subscription> subs = new ArrayList<Subscription>();
			subs.add(ref);
			subscriptionMap.put(sender,subs);
		}
	}
	
	void removeSubscriptions(String sender, int k, boolean show) {
		if (show)
			debugStream.out("Deleting subscriptions on \"" + id + "\"", k);
		subscriptionMap.remove(sender);
	}
	
}

class Tag {
	
	private Node node;
	private String id;
	private String type;
	private String[] attributes;
	
	Tag(String id, String type, String[] attributes) {
		node = Node.SENSOR;
		this.id = id;
		this.type = type;
		this.attributes = attributes;
	}
	
	Tag(String id, String[] attributes) {
		node = Node.ACTUATOR;
		this.id = id;
		this.attributes = attributes;
	}
	
}

class User {
	
	private String id;
	private String address;
	
	User(String id, String address) {
		this.id = id;
		this.address = address;
	}
	
}

class Subscription {
	
	private String sender;
	private String event;
	private String receiver;
	private String action;
	
	private Node receiverNode;
	
	Subscription(String sender, String receiver) {
		this.sender = sender;
		this.event = null;
		this.receiver = receiver;
		this.action = null;
		receiverNode = Node.USER;
	}
	
	Subscription(String sender, String event, String receiver, String action) {
		this.sender = sender;
		this.event = event;
		this.receiver = receiver;
		this.action = action;
		receiverNode = Node.ACTUATOR;
	}
	
}
