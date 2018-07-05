package mecs.iot.proj.om2m.adn.in;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.adn.ADN;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.MN;
import mecs.iot.proj.om2m.structures.Severity;

class ADN_IN extends ADN {
	
	private HashMap<String,MN> mnMap;																					// serial -> MN

	ADN_IN(String id, String host, boolean debug, Console console) throws URISyntaxException {
		super(id,host,debug,console);
		cseClient = new Client(Services.joinIdHost(id+"/CSEclient",host), Constants.cseProtocol + "localhost" + Constants.inCSERoot, debug);
		mnMap = new HashMap<String,MN>();
	}
	
	@Override
	
	synchronized public void handleGET(CoapExchange exchange) {
		Response response = null;
		String mode = getUriValue(exchange,"mode",0);
		if (mode!=null) {
			int sw;
			try {
				sw = Integer.parseInt(mode);
			} catch (NumberFormatException e) {
				debugStream.out("Bad request, mode=" + mode, i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
			String serial = getUriValue(exchange,"ser",1);
			if (serial==null || !isValidSerial(serial)) {
				if (serial!=null)
					debugStream.out("Bad request, ser=" + serial, i);
				else
					debugStream.out("Bad request, ser", i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
			MN mn = mnMap.get(serial);
			switch (sw) {
				case 0:
					// user localization (mode=0&ser=<SERIAL>)
					if (mn==null) {
						debugStream.out("Serial \"" + serial + "\" is not registered on any MN", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out1("Handling user localization for serial \"" + serial + "\"", i);
					response = new Response(ResponseCode.CONTENT);
					response.setPayload(mn.address);
					break;
				default:
					debugStream.out("Bad request, mode=" + mode, i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
			}
		} else {
			debugStream.out("Bad request, mode not specified", i);
			response = new Response(ResponseCode.BAD_REQUEST);
		}
		exchange.respond(response);
		outStream.out2("done");
		i++;
	}
	
	@Override
	
	synchronized public void handlePOST(CoapExchange exchange) {
		Response response = null;
		String id = getUriValue(exchange,"id",0);
		if (id==null || !isValidId(id)) {
			if (id!=null)
				debugStream.out("Bad request, id=" + id, i);
			else
				debugStream.out("Bad request, id", i);
			response = new Response(ResponseCode.BAD_REQUEST);
			exchange.respond(response);
			i++;
			return;
		}
		String serial = getUriValue(exchange,"ser",1);
		if (serial!=null) {
			// node IN registration and localization (id=<ID>&ser=<SERIAL>&loc=<LOC>)
			if (!isValidSerial(serial)) {
				debugStream.out("Bad request, ser=" + serial, i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
			String location = getUriValue(exchange,"loc",2);
			if (location==null || !isValidLocation(location)) {
				if (location!=null)
					debugStream.out("Bad request, loc=" + location, i);
				else
					debugStream.out("Bad request, loc", i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
			MN mn = Db.mnMap.get(Integer.parseInt(location));
			if (mn==null || !mn.active) {
				debugStream.out("MN with location \"" + location + "\" is not registered", i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
			outStream.out1("Associating node \"" + id + "\" with serial \"" + serial + "\" to MN \"" + mn.id + "\"", i);
			mnMap.put(serial,mn);
			response = new Response(ResponseCode.CREATED);
			response.setPayload(mn.id + "," + mn.address);
		} else {
			// MN registration (id=<ID>)
			MN[] mns = Db.mnMap.values().toArray(new MN[] {});
			int index = 0;
			boolean found = false;
			for (int i=0; i<mns.length; i++) {
				if (id.equals(mns[i].id)) {
					index = i;
					found = true;
					break;
				}	
			}
			if (!found) {
				debugStream.out("MN with id \"" + id + "\" doesn't exist", i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
			outStream.out1("Registering MN \"" + id + "\"", i);
			mns[index].active = true;
			String[] uri = new String[] {mns[index].id,"state"};
			cseClient.stepCount();
			try {
				cseClient.services.postSubscription(Constants.adnProtocol+"localhost"+Constants.inADNRoot,"subscription",uri,cseClient.getCount());
			} catch (URISyntaxException e) {
				outStream.out2("failed");
				errStream.out(e,i,Severity.MEDIUM);
				response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
				exchange.respond(response);
				i++;
				return;
			}
			response = new Response(ResponseCode.CREATED);
		}
		exchange.respond(response);
		outStream.out2("done");
		i++;
	}
	
	// TODO: middle-node removal

}
