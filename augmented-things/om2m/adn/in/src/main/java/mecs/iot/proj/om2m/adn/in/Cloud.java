package mecs.iot.proj.om2m.adn.in;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.adn.in.exceptions.NotFoundMNException;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.structures.JSONSerializable;

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
			Services.parseJSONObject(json,"type",String.class);
			Services.parseJSONObject(json,"active",Boolean.class);
			Services.parseJSONArray(json,new String[] {"attributes"},null);
			root.addTag(id,json,k);
		} catch (JSONException e1) {
			try {
				Services.parseJSONObject(json,"address",String.class);
				Services.parseJSONObject(json,"active",Boolean.class);
				root.addUser(id,json,k);
			} catch (JSONException e2) {
				try {
					Services.parseJSONArray(json,new String[] {"subs","receiver"},"id");
					Services.parseJSONArray(json,new String[] {"subs"},"event");
					Services.parseJSONArray(json,new String[] {"subs"},"action");
					root.addSubscription(id,json,k);
				} catch (JSONException e3) {
					root.addSubscription(id,json,k);
					return;
				}
				return;
			}
			return;
		}
	}
	
	String getJSONMNs() {
		String[] mns = mnMap.keySet().toArray(new String[] {});
		return Services.toJSONArray(mns,"mns").toString();
	}
	
	String getJSONNodes(String mn) {
		JSON[] nodes = mnMap.get(mn).tagMap.values().toArray(new JSON[] {});
		return Services.toJSONArray(nodes,"nodes").toString();
	}
	
	String getJSONUsers(String mn) {
		JSON[] users = mnMap.get(mn).userMap.values().toArray(new JSON[] {});
		return Services.toJSONArray(users,"users").toString();
	}
	
	String getJSONSubscriptions(String mn, String id) {
		JSON[] subs = mnMap.get(mn).subscriptionMap.values().toArray(new JSON[] {});
		return Services.toJSONArray(subs,"subs").toString();
	}

}

class MN {
	
	private String id;
	
	HashMap<String,JSON> tagMap;																	// serial -> tag
	HashMap<String,JSON> userMap;																	// user id -> user
	HashMap<String,JSON> subscriptionMap;															// resource id -> list of subscriptions
	
	private DebugStream debugStream;
	
	MN(String id, DebugStream debugStream) {
		this.id = id;
		tagMap = new HashMap<String,JSON>();
		userMap = new HashMap<String,JSON>();
		subscriptionMap = new HashMap<String,JSON>();
		this.debugStream = debugStream;
	}
	
	void addTag(String id, String json, int k) {
		debugStream.out("Adding endpoint node (sensor) \"" + id + "\" to MN \"" + this.id + "\"", k);
		tagMap.put(id,new JSON(json));
	}
	
	void addUser(String id, String json, int k) {
		debugStream.out("Adding user \"" + id + "\" to MN \"" + this.id + "\"", k);
		userMap.put(id,new JSON(json));
	}
	
	void addSubscription(String id, String json, int k) {
		debugStream.out("Adding subscription on \"" + id + "\" to MN \"" + this.id + "\"", k);
		subscriptionMap.put(id,new JSON(json));
	}
	
}

class JSON implements JSONSerializable {
	
	String content;
	
	JSON(String content) {
		this.content = content;
	}
	
	@Override
	
	public JSONObject toJSON() {
		return new JSONObject(content);
	}
	
}
