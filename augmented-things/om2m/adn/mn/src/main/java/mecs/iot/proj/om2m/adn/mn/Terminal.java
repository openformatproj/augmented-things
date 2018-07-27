package mecs.iot.proj.om2m.adn.mn;

import org.json.JSONObject;

import mecs.iot.proj.om2m.structures.JSONSerializable;
import mecs.iot.proj.om2m.structures.Tag;

class Terminal implements JSONSerializable, Cloneable {
	
//	String id;
//	String type;
//	String address;
//	Node node;
//	
//	Terminal(String id, String type, String address, Node node) {
//		this.id = id;
//		this.type = type;
//		this.address = address;
//		this.node = node;
//	}
	
	Tag tag;
	String serial;
	
	Terminal(String serial, Tag tag) {
		this.serial = serial;
		this.tag = tag;
	}
	
	@Override
	
//	public JSONObject toJSON() {
//		JSONObject obj = new JSONObject();
//		obj.put("id",id);
//		obj.put("type",type);
//		obj.put("address",address);
//		obj.put("node",node);
//		return obj;
//	}
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("id",tag.id);
		obj.put("type",tag.type);
		obj.put("address",tag.address);
		obj.put("node",tag.node);
		return obj;
	}
	
	@ Override
	
	public Object clone() {
//		return new Terminal(id,type,address,node);
		Tag tag = (Tag)this.tag.clone();
		return new Terminal(serial,tag);
	}
	
}
