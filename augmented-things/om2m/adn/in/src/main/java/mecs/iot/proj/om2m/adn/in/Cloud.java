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
	
	String getJSONMN(String id) throws NotFoundMNException {
		if (mnMap.containsKey(id))
			return mnMap.get(id).toJSON().toString();
		else
			throw new NotFoundMNException();
	}
	
	void removeMN(String id) throws NotFoundMNException {
		if (mnMap.containsKey(id))
			mnMap.get(id).active = false;
		else
			throw new NotFoundMNException();
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
		MN[] mns = mnMap.values().toArray(new MN[] {});
		return Services.toJSONArray(mns,"mns").toString();
	}
	
	String getJSONNodes(String mn) {
		if (mnMap.containsKey(mn)) {
			JSON[] nodes = mnMap.get(mn).tagMap.values().toArray(new JSON[] {});
			return Services.toJSONArray(nodes,"nodes").toString();
		} else {
			return "MN \"" + mn + "\" is not registered";
		}
	}
	
	String getJSONUsers(String mn) {
		if (mnMap.containsKey(mn)) {
			JSON[] users = mnMap.get(mn).userMap.values().toArray(new JSON[] {});
			return Services.toJSONArray(users,"users").toString();
		} else {
			return "MN \"" + mn + "\" is not registered";
		}
	}
	
	String getJSONSubscriptions(String mn, String id) {
		String id_ = Services.normalizeName(id);
		if (mnMap.containsKey(mn)) {
			if (mnMap.get(mn).subscriptionMap.containsKey(id_)) {
				JSON sub = mnMap.get(mn).subscriptionMap.get(id_);
				return sub.toJSON().toString();
			} else {
				return "Node \"" + id + "\" is not registered on MN \"" + mn + "\" as a sensor or hasn't active subscriptions";
			}
		} else {
			return "MN \"" + mn + "\" is not registered";
		}
	}
	
	private class MN implements JSONSerializable {
		
		private String id;
		boolean active;
		
		HashMap<String,JSON> tagMap;																	// serial -> tag
		HashMap<String,JSON> userMap;																	// user id -> user
		HashMap<String,JSON> subscriptionMap;															// resource id -> list of subscriptions
		
		private DebugStream debugStream;
		
		MN(String id, DebugStream debugStream) {
			this.id = id;
			this.active = true;
			tagMap = new HashMap<String,JSON>();
			userMap = new HashMap<String,JSON>();
			subscriptionMap = new HashMap<String,JSON>();
			this.debugStream = debugStream;
		}
		
		void addTag(String id, String json, int k) {
			if (tagMap.containsKey(id))
				debugStream.out("Adding endpoint node \"" + id + "\" to MN \"" + this.id + "\"", k);
			else
				debugStream.out("Changing endpoint node \"" + id + "\" state on MN \"" + this.id + "\"", k);
			tagMap.put(id,new JSON(json));
		}
		
		void addUser(String id, String json, int k) {
			if (userMap.containsKey(id))
				debugStream.out("Adding user \"" + id + "\" to MN \"" + this.id + "\"", k);
			else
				debugStream.out("Changing user \"" + id + "\" state on MN \"" + this.id + "\"", k);
			userMap.put(id,new JSON(json));
		}
		
		void addSubscription(String id, String json, int k) {
			debugStream.out("Changing subscription state of \"" + id + "\" on MN \"" + this.id + "\"", k);
			subscriptionMap.put(id,new JSON(json));
		}

		@Override
		
		public JSONObject toJSON() {
			JSONObject obj = new JSONObject();
			obj.put("mn",id);
			obj.put("active",active);
			return obj;
		}
		
	}

	private class JSON implements JSONSerializable {
		
		String content;
		
		JSON(String content) {
			this.content = content;
		}
		
		@Override
		
		public JSONObject toJSON() {
			return new JSONObject(content);
		}
		
	}

}
