package mecs.iot.proj.om2m.adn;

import mecs.iot.proj.om2m.structures.Node;

public class Subscription {
	
	public Terminal sender;
	public String event;
	private String rule;
	public Terminal receiver;
	public String action;
	
	Subscription(String sender, String receiver, String address) {
		this.sender = new Terminal(sender,null,Node.SENSOR);
		this.action = null;
		this.rule = null;
		this.receiver = new Terminal(receiver,address,Node.USER);
		this.action = null;
	}
	
	Subscription(String sender, String event, String rule, String receiver, String address, String action) {
		this.sender = new Terminal(sender,null,Node.SENSOR);
		this.event = event;
		this.rule = rule;
		this.receiver = new Terminal(receiver,address,Node.ACTUATOR);
		this.action = action;
	}
	
}
