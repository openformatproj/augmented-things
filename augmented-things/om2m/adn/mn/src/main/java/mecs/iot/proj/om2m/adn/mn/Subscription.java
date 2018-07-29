package mecs.iot.proj.om2m.adn.mn;

import mecs.iot.proj.om2m.exceptions.InvalidRuleException;
import mecs.iot.proj.om2m.exceptions.NoTypeException;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.JSONSerializable;
import mecs.iot.proj.om2m.structures.ASN;

import org.json.JSONObject;

class Subscription implements JSONSerializable, Cloneable {
	
	ASN sender;
	String event;
	Checker checker;
	ASN receiver;
	String action;
	
	Subscription(ASN sender, ASN receiver) {
		this.sender = sender;
		this.event = null;
		this.checker = null;
		this.receiver = receiver;
		this.action = null;
	}
	
	Subscription(ASN sender, String event, String rule, ASN receiver, String action) throws InvalidRuleException, NoTypeException {
		this.sender = sender;
		this.event = event;
		String cl = null;
		try {
			cl = Format.getClassName(sender.type);
		} catch (NoTypeException e) {
			throw e;
		}
		switch(cl) {
			case "Double":
				this.checker = new Controller(rule);
				break;
			default:
				this.checker = null;
				break;
		}
		this.receiver = receiver;
		this.action = action;
	}
	
	private Subscription(ASN sender, String event, ASN receiver, String action) {
		this.sender = sender;
		this.event = event;
		this.checker = null;
		this.receiver = receiver;
		this.action = action;
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("event",event);
		obj.put("receiver",receiver.toJSONReduced());
		obj.put("action",action);
		return obj;
	}
	
	@Override
	public Object clone() {
		ASN sender = (ASN)this.sender.clone();
		ASN receiver = (ASN)this.receiver.clone();
		return new Subscription(sender,event,receiver,action);
	}
	
}
