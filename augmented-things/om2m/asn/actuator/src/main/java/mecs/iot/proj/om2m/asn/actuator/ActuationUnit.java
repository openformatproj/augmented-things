package mecs.iot.proj.om2m.asn.actuator;

import java.util.HashMap;

import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

import mecs.iot.proj.om2m.asn.Action;
import mecs.iot.proj.om2m.asn.Unit;
import mecs.iot.proj.om2m.asn.actuator.exceptions.ActionNumberMismatchException;

class ActuationUnit implements Unit {
	
	private String name;
	HashMap<String,Action> actionMap;
	
	ActuationUnit(String name, String[] attributes, Action[] actions) throws ActionNumberMismatchException {
		this.name = name;
		int l = attributes.length;
		if (l!=actions.length)
			throw new ActionNumberMismatchException();
		actionMap = new HashMap<String,Action>();
		for (int i=0; i<l; i++) {
			actionMap.put(attributes[i],actions[i]);
		}
	}
	
	@Override
	
	public Response send(String str) {
		if (actionMap.containsKey(str)) {
			actionMap.get(str).action();
			return new Response(ResponseCode.CHANGED);
		} else {
			return new Response(ResponseCode.BAD_REQUEST);
		}
	}
	
	@Override
	
	public String getName() {
		return name;
	}
	
}
