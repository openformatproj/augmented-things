package mecs.iot.proj.om2m.adn;

import java.io.Serializable;

import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.exceptions.InvalidRuleException;

public class Subscription implements Serializable {
	
	public Terminal sender;
	public String event;
	public transient Controller controller;
	public Terminal receiver;
	public String action;
	
	private static final long serialVersionUID = 1L;
	
	Subscription(String sender, String type, String receiver, String address) {
		this.sender = new Terminal(sender,type,null,Node.SENSOR);
		this.action = null;
		this.controller = null;
		this.receiver = new Terminal(receiver,null,address,Node.USER);
		this.action = null;
	}
	
	Subscription(String sender, String type, String event, String rule, String receiver, String address, String action) throws InvalidRuleException {
		this.sender = new Terminal(sender,type,null,Node.SENSOR);
		this.event = event;
		this.controller = new Controller(rule);
		this.receiver = new Terminal(receiver,null,address,Node.ACTUATOR);
		this.action = action;
	}
	
}
