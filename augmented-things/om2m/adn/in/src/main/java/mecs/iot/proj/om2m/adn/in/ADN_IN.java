package mecs.iot.proj.om2m.adn.in;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

//import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.adn.ADN;
//import mecs.iot.proj.om2m.adn.Subscriber;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Console;
//import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.MN;
import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.Tag_;

class ADN_IN extends ADN {
	
	private HashMap<String,Tag_> tagMap;
	// private HashMap<String,String> userMap;
	private HashMap<String,MN> mnMap;

	ADN_IN(String id, String host, String uri, String context, boolean debug, Console console) throws URISyntaxException {
		super(Services.joinIdHost(id+"_server",host), uri, context, debug, console);
		//cseClient = new Client(Services.joinIdHost(id+"_CSEclient",host), Constants.cseProtocol + "localhost" + Constants.inRoot + context + Constants.inCSEPostfix, debug);
		//subscriber = new Subscriber();
		tagMap = new HashMap<String,Tag_>();
		// userMap = new HashMap<String,String>();
		mnMap = new HashMap<String,MN>();
	}
	
	@Override
	
	synchronized public void handleGET(CoapExchange exchange) {
		Response response = null;
		String mode = getUriValue(exchange,"mode",0);
		if (mode!=null) {
			int sw;
			MN mn = null;
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
			switch (sw) {
			case 0:
				// user localization (mode=0&ser=<SERIAL>)
				outStream.out("Handling user localization for serial \"" + serial + "\"", i);
				mn = mnMap.get(serial);
				if (mn==null) {
					debugStream.out("Serial \"" + serial + "\" is not registered on any MN", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
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
		i++;
	}
	
	@Override
	
	synchronized public void handlePOST(CoapExchange exchange) {
		Response response = null;
		String id = getUriValue(exchange,"id",0);
		if (id!=null) {
			if (!isValidId(id)) {
				debugStream.out("Bad request, id=" + id, i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
			String serial = getUriValue(exchange,"ser",1);
			if (serial!=null) {
				if (!isValidSerial(serial)) {
					debugStream.out("Bad request, ser=" + serial, i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				String type = getUriValue(exchange,"type",2);
				if (type!=null) {
					// node IN registration and localization (id=<ID>&ser=<SERIAL>&type=<TYPE>&loc=<LOC>{&addr=<URI>}, PAYLOAD [<ATTRIBUTE>])
					if (!isValidType(type)) {
						debugStream.out("Bad request, type=" + type, i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					String location = getUriValue(exchange,"loc",3);
					if (!isValidLocation(location)) {
						debugStream.out("Bad request, loc=" + location, i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					String payload = exchange.getRequestText();
					String[] attributes = payload.split(",");
					Integer k = new Integer(0);
					if (!areValidAttributes(attributes,k)) {
						debugStream.out("Bad request, attribute=" + attributes[k], i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					Tag_ tag = null;
					if (type.equals("act")) {
						String address = getUriValue(exchange,"addr",4);
						if (address==null || !isValidAddress(address)) {
							if (address!=null)
								debugStream.out("Bad request, addr=" + address, i);
							else
								debugStream.out("Bad request, addr", i);
							response = new Response(ResponseCode.BAD_REQUEST);
							exchange.respond(response);
							i++;
							return;
						}
						tag = new Tag_(Node.ACTUATOR,id,address,attributes);
					} else {
						tag = new Tag_(Node.SENSOR,id,type,attributes);
					}
					MN mn = Db.mnMap.get(Integer.parseInt(location));
					if (mn==null) {
						debugStream.out("MN with location \"" + location + "\" is not registered", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out("Registering node \"" + id + "\" (serial \"" + serial + "\") on MN \"" + mn.id + "\"", i);
					tagMap.put(serial,tag);
					mnMap.put(serial,mn);
					response = new Response(ResponseCode.CREATED);
					response.setPayload(mn.id + "," + mn.address);
				} else {
					debugStream.out("Bad request, type not specified", i);
					response = new Response(ResponseCode.BAD_REQUEST);
				}
			} else {
//				// user IN registration (id=<ID>&addr=<URI>)
//				String address = getUriValue(exchange,"addr",1);
//				if (address==null || !isValidAddress(address)) {
//					if (address!=null)
//						debugStream.out("Bad request, addr=" + address, i);
//					else
//						debugStream.out("Bad request, addr", i);
//					response = new Response(ResponseCode.BAD_REQUEST);
//					exchange.respond(response);
//					i++;
//					return;
//				}
//				outStream.out("Registering user \"" + id + "\" (address \"" + address + "\")", i);
//				userMap.put(id,address);
//				response = new Response(ResponseCode.CREATED);
			}
		} else {
			debugStream.out("Bad request, id not specified", i);
			response = new Response(ResponseCode.BAD_REQUEST);
		}
		exchange.respond(response);
		i++;
	}

}
