package mecs.iot.proj.om2m.adn.mn;

import org.json.JSONObject;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.exceptions.InvalidRuleException;
import mecs.iot.proj.om2m.exceptions.NoTypeException;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.JSONSerializable;
import mecs.iot.proj.om2m.structures.Node;

class Subscription implements JSONSerializable, Cloneable {
	
	Terminal sender;
	String event;
	Checker checker;
	Terminal receiver;
	String action;
	
	Subscription(String sender, String type, String receiver, String address) {
		this.sender = new Terminal(sender,type,null,Node.SENSOR);
		this.event = null;
		this.checker = null;
		this.receiver = new Terminal(receiver,null,address,Node.USER);
		this.action = null;
	}
	
	Subscription(String sender, String type, String event, String rule, String receiver, String address, String action) throws InvalidRuleException, NoTypeException {
		this.sender = new Terminal(sender,type,null,Node.SENSOR);
		this.event = event;
		String cl = null;
		try {
			cl = Format.getClassName(type);
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
		this.receiver = new Terminal(receiver,null,address,Node.ACTUATOR);
		this.action = action;
	}
	
	private Subscription(Terminal sender, String event, Terminal receiver, String action) {
		this.sender = sender;
		this.event = event;
		this.checker = null;
		this.receiver = receiver;
		this.action = action;
	}
	
	@Override
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("sender",sender.toJSON());
		obj.put("event",event);
		obj.put("receiver",receiver.toJSON());
		obj.put("action",action);
		return obj;
	}
	
	@Override
	
	public Object clone() {
		Terminal sender = (Terminal)this.sender.clone();
		Terminal receiver = (Terminal)this.receiver.clone();
		return new Subscription(sender,event,receiver,action);
	}
	
	public static void main(String[] args) {
		Subscription[] subs = new Subscription[2];
		try {
			subs[0] = new Subscription("sensor.alessandro", "tempC", "event", "", "actuator.alessandro", "coap://127.0.0.1:5690/augmented-things", "action1");
		} catch (InvalidRuleException | NoTypeException e) {
			e.printStackTrace();
		}
		subs[1] = new Subscription("sensor.alessandro", "tempC", "user.ALESSANDRO-K7NR", "coap://192.168.0.107:5691/augmented-things");
		System.out.println(Services.toJSONArray(subs,"subs").toString());
		System.out.println(Services.packJSON(Services.toJSONArray(subs,"subs").toString()));
		System.out.println(Services.unpackJSON(Services.packJSON(Services.toJSONArray(subs,"subs").toString())));
	}
	
}
