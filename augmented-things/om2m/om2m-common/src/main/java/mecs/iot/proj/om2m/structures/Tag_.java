package mecs.iot.proj.om2m.structures;

public class Tag_ {
	
	public Node node;
	public String id;
	public String description; 										// type for sensors, address for actuators
	public String[] attributes;
	
	public Tag_ (Node node, String id, String description, String[] attributes) {
		this.node = node;
		this.id = id;
		this.description = description;
		this.attributes = attributes;
	}

}
