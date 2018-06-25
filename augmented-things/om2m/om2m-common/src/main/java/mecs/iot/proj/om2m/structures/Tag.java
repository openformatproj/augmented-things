package mecs.iot.proj.om2m.structures;

import java.util.HashMap;

public class Tag {
	
	public Node node;
	public String id;
	public String serial;
	public String address;
	public String type;
	public String[] attributes;
	
	public HashMap<String,String> labelMap;
	
	public Tag (String id, String serial, String type, String[] attributes) {
		this.node = Node.SENSOR;
		this.id = id;
		this.serial = serial;
		this.address = null;
		this.type = type;
		this.attributes = attributes;
		labelMap = null;
	}
	
	public Tag (String id, String serial, String[] attributes) {
		this.node = Node.ACTUATOR;
		this.id = id;
		this.serial = serial;
		this.address = null;
		this.type = "act";
		this.attributes = attributes;
		labelMap = null;
	}
	
	public Tag (Node node, String id, String description, String[] attributes) {
		this.node = node;
		this.id = id;
		this.serial = null;
		switch (node) {
			case SENSOR:
				this.address = null;
				this.type = description;
				String[] splits;
				for (int i=0; i<attributes.length; i++) {
					splits = attributes[i].split(": ");
					if (splits.length>1)
						labelMap.put(splits[1],splits[0]);
					else
						labelMap.put(splits[0],"");
				}
				break;
			case ACTUATOR:
				this.address = description;
				this.type = "act";
				break;
			case USER:
				break;
		}
		this.attributes = attributes;
	}
	
//	public String[] labels() {
//		if (node==Node.ACTUATOR) {
//			return attributes;
//		} else {
//			int l = attributes.length;
//			String[] labels = new String[l];
//			String[] splits;
//			for (int i=0; i<l; i++) {
//				splits = attributes[i].split(": ");
//				if (splits.length>1)
//					labels[i] = splits[1];
//				else
//					labels[i] = splits[0];
//			}
//			return labels;
//		}
//	}
	
	@Override
	
	public String toString() {
		String str = "id=" + id + ", type=" + type;
		for (int i=0; i<attributes.length; i++) {
			str += ", attributes[" + Integer.toString(i) + "]=" + attributes[i];
		}
		return str;
	}
	
}
