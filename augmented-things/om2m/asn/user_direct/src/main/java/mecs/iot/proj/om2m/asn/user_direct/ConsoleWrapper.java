package mecs.iot.proj.om2m.asn.user_direct;

import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

import mecs.iot.proj.om2m.asn.Unit;
import mecs.iot.proj.om2m.dashboard.Console;

class ConsoleWrapper implements Unit {
	
	private String name;
	private Console console;
	
	ConsoleWrapper(String name, Console console) {
		this.name = name;
		this.console = console;
	}
	
	@Override
	
	public Response send(String str) {
		if (isValid(str)) {
			console.interf.out(str);
			return new Response(ResponseCode.CHANGED);
		} else {
			return new Response(ResponseCode.BAD_REQUEST);
		}
	}
	
	@Override
	
	public String getName() {
		return name;
	}
	
	private boolean isValid(String str) {
		return str.equals("OK") || str.contains("con=");
	}
	
}
