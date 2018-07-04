package mecs.iot.proj.om2m.adn.mn;

import org.json.JSONObject;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.structures.JSONSerializable;
import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.exceptions.InvalidRuleException;

public class Subscription implements JSONSerializable {
	
	public Terminal sender;
	public String event;
	public Controller controller;
	public Terminal receiver;
	public String action;
	
	Subscription(String sender, String type, String receiver, String address) {
		this.sender = new Terminal(sender,type,null,Node.SENSOR);
		this.event = null;
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
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("sender",sender.toJSON());
		obj.put("event",event);
		obj.put("receiver",receiver.toJSON());
		obj.put("action",action);
		return obj;
	}
	
	public static void main(String[] args) {
		Subscription[] subs = new Subscription[2];
		try {
			subs[0] = new Subscription("sensor.alessandro", "tempC", "event", "", "actuator.alessandro", "coap://127.0.0.1:5690/augmented-things", "action1");
		} catch (InvalidRuleException e) {
			e.printStackTrace();
		}
		subs[1] = new Subscription("sensor.alessandro", "tempC", "user.ALESSANDRO-K7NR", "coap://192.168.0.107:5691/augmented-things");
		System.out.println(Services.vectorizeJSON(subs).toString());
		System.out.println(Services.packJSON(Services.vectorizeJSON(subs).toString()));
		System.out.println(Services.unpackJSON(Services.packJSON(Services.vectorizeJSON(subs).toString())));
	}
	
}
