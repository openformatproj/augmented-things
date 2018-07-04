package mecs.iot.proj.om2m.adn.mn;

import org.json.JSONObject;

import mecs.iot.proj.om2m.structures.JSONSerializable;
import mecs.iot.proj.om2m.structures.Node;

public class Terminal implements JSONSerializable {
	
	public String id;
	public String type;
	public String address;
	public Node node;
	
	Terminal(String id, String type, String address, Node node) {
		this.id = id;
		this.type = type;
		this.address = address;
		this.node = node;
	}
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("id",id);
		obj.put("type",type);
		obj.put("address",address);
		obj.put("node",node);
		return obj;
	}
	
}
