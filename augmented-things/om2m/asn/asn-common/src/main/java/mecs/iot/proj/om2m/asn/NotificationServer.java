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
		Response response = null;
		String str = exchange.getRequestText();
		if (str.equals("OK")) {
			outStream.out1("Handling successful subscription",i);
			unit.sendAck(str);
			response = new Response(ResponseCode.CHANGED);
		} else {
			outStream.out1("Handling notification \"" + str + "\"", i);
			response = unit.sendContent(str);
			if (response==null || response.getCode()==ResponseCode.BAD_REQUEST) {
				outStream.out2("failed");
				debugStream.out("Bad request, \"" + str + "\" is not a valid notification", i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			} else if (response.getCode()!=ResponseCode.CHANGED) {
				outStream.out2("failed");
				errStream.out("Unable to write on the unit \"" + unit.getName() + "\", response: " + response.getCode(),
						i, Severity.LOW);
				response = new Response(response.getCode());
				exchange.respond(response);
				i++;
				return;
			}
		}
		exchange.respond(response);
		outStream.out2("done");
		i++;
	}

}
