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
	
	public void sendAck(String str) {
		console.interf.out(str);
	}
	
	@Override
	
	public Response sendContent(String str) {
		if (str.contains("con=")) {
			console.interf.outAsync(str);
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
