package mecs.iot.proj.om2m.structures;

public class Tag {
	
	public String id;
	public String serial;
	public String type;
	public String[] attributes;
	
	public Tag (String id, String serial, String type, String[] attributes) {
		this.id = id;
		this.serial = serial;
		this.type = type;
		this.attributes = attributes;
	}
	
	public Tag (String id, String serial, String[] attributes) {
		this.id = id;
		this.serial = serial;
		this.type = null;
		this.attributes = attributes;
	}
	
	/*
	 * Used to store information in tagMap<serial,tag> 
	 */
//	public Tag (String[] attributes, String type, String id) {
//		this.id = id;
//		this.serial = null;
//		this.type = type;
//		this.attributes = attributes;
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
