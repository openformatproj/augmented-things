package mecs.iot.proj.om2m.adn.mn;

import org.json.JSONObject;

import mecs.iot.proj.om2m.exceptions.InvalidRuleException;
import mecs.iot.proj.om2m.exceptions.NoTypeException;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.JSONSerializable;
import mecs.iot.proj.om2m.structures.Tag;

class Subscription implements JSONSerializable, Cloneable {
	
	Tag sender;
	String event;
	Checker checker;
	Tag receiver;
	String action;
	
	Subscription(Tag sender, Tag receiver) {
		this.sender = sender;
		this.event = null;
		this.checker = null;
		this.receiver = receiver;
		this.action = null;
	}
	
	Subscription(Tag sender, String event, String rule, Tag receiver, String action) throws InvalidRuleException, NoTypeException {
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
	
	private Subscription(Tag sender, String event, Tag receiver, String action) {
		this.sender = sender;
		this.event = event;
		this.checker = null;
		this.receiver = receiver;
		this.action = action;
	}
	
	@Override
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
//		obj.put("sender",sender.toJSON()); TODO: maybe not needed
		obj.put("event",event);
		obj.put("receiver",receiver.toJSON());
		obj.put("action",action);
		return obj;
	}
	
	@Override
	
	public Object clone() {
		Tag sender = (Tag)this.sender.clone();
		Tag receiver = (Tag)this.receiver.clone();
		return new Subscription(sender,event,receiver,action);
	}
	
}
