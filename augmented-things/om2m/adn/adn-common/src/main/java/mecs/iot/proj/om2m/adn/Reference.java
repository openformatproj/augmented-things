package mecs.iot.proj.om2m.adn;

import mecs.iot.proj.om2m.structures.Node;

public class Reference {
	
	private String sender;
	public String event;
	public String receiver;
	public String address;
	public String action;
	public Node node;
	
	Reference(String sender, String receiver, String address) {
		this.sender = sender;
		this.event = null;
		this.receiver = receiver;
		this.address = address;
		this.action = null;
		node = Node.USER;
	}
	
	Reference(String sender, String event, String receiver, String address, String action) {
		this.sender = sender;
		this.event = event;
		this.receiver = receiver;
		this.address = address;
		this.action = action;
		node = Node.ACTUATOR;
	}
	
}
