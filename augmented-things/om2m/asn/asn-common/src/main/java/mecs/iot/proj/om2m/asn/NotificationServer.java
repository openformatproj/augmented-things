package mecs.iot.proj.om2m.asn;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.structures.Severity;

class NotificationServer extends CoapResource {
	
	private OutStream outStream;
	private DebugStream debugStream;
	private ErrStream errStream;
	
	private Unit unit;
	
	private int i;
	
	NotificationServer(String name, String uri, boolean debug, Unit unit) {
		super(uri);
		outStream = new OutStream(name);
		debugStream = new DebugStream(name,debug);
		errStream = new ErrStream(name);
		this.unit = unit;
		i = 1;
	}
	
	@Override
	
	synchronized public void handlePUT(CoapExchange exchange) {
		String str = exchange.getRequestText();
		outStream.out("Handling notification \"" + str + "\"", i);
		Response response = unit.send(str);
		if (response==null || response.getCode()==ResponseCode.BAD_REQUEST) {
			debugStream.out("Bad request, \"" + str + "\" is not a valid notification", i);
			response = new Response(ResponseCode.BAD_REQUEST);
			exchange.respond(response);
			i++;
			return;
		} else if (response.getCode()!=ResponseCode.CHANGED) {
			errStream.out("Unable to write on the unit \"" + unit.getName() + "\", response: " + response.getCode(), //
					i, Severity.LOW);
			response = new Response(response.getCode());
			exchange.respond(response);
			i++;
			return;
		}
		response = new Response(ResponseCode.CHANGED);
		exchange.respond(response);
		i++;
	}

}
