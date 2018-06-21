package mecs.iot.proj.om2m.adn;

import mecs.iot.proj.om2m.structures.Node;

public class Reference {
	
	public String resource;
	public String event;
	public String id;
	public String address;
	public String action;
	public Node node;
	
	Reference(String resource, String id, String address) {
		this.resource = resource;
		this.event = null;
		this.id = id;
		this.address = address;
		this.action = null;
		node = Node.USER;
	}
	
	Reference(String resource, String event, String id, String address, String action) {
		this.resource = resource;
		this.event = event;
		this.id = id;
		this.address = address;
		this.action = action;
		node = Node.ACTUATOR;
	}
	
}
