package mecs.iot.proj.om2m.adn.mn;

import org.json.JSONObject;

import mecs.iot.proj.om2m.structures.JSONSerializable;
import mecs.iot.proj.om2m.structures.Node;

class Terminal implements JSONSerializable {
	
	String id;
	String type;
	String address;
	Node node;
	
	Terminal(String id, String type, String address, Node node) {
		this.id = id;
		this.type = type;
		this.address = address;
		this.node = node;
	}
	
	@Override
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("id",id);
		obj.put("type",type);
		obj.put("address",address);
		obj.put("node",node);
		return obj;
	}
	
}
