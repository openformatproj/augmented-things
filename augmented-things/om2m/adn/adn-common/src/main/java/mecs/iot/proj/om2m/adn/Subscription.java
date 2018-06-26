package mecs.iot.proj.om2m.adn;

import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.exceptions.InvalidRuleException;

public class Subscription {
	
	public Terminal sender;
	public String event;
	public Controller controller;
	public Terminal receiver;
	public String action;
	
	Subscription(String sender, String receiver, String address) {
		this.sender = new Terminal(sender,null,Node.SENSOR);
		this.action = null;
		this.controller = null;
		this.receiver = new Terminal(receiver,address,Node.USER);
		this.action = null;
	}
	
	Subscription(String sender, String event, String rule, String receiver, String address, String action) throws InvalidRuleException {
		this.sender = new Terminal(sender,null,Node.SENSOR);
		this.event = event;
		this.controller = new Controller(rule);
		this.receiver = new Terminal(receiver,address,Node.ACTUATOR);
		this.action = action;
	}
	
}
