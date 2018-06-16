package mecs.iot.proj.om2m.adn.mn;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.adn.ADN;
import mecs.iot.proj.om2m.adn.Subscriber;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Tag_;

class ADN_MN extends ADN {

	private HashMap<String,Tag_> tagMap;
	private HashMap<String,String> userMap;

	ADN_MN(String id, String host, String uri, String context, boolean debug, Console console) throws URISyntaxException {
		super(Services.joinIdHost(id+"_server",host), uri, context, debug, console);
		client = new Client(name, Constants.cseProtocol + "localhost" + Constants.mnRoot + context + Constants.mnCSEPostfix, debug);
		subscriber = new Subscriber(client);
		tagMap = new HashMap<String,Tag_>();
		userMap = new HashMap<String,String>();
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
			switch (sw) {
			case 1:
				// attributes query (mode=1&ser=<SERIAL>)
				outStream.out("Handling attributes querying for serial \"" + serial + "\"", i);
				Tag_ tag1 = tagMap.get(serial);
				if (tag1==null) {
					debugStream.out("Serial \"" + serial + "\" is not registered on this MN", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				String[] attributes = tag1.attributes;
				String payload = "";
				for (int i=0; i<attributes.length; i++) {
					if (i!=attributes.length-1)
						payload += attributes[i] + ",";
					else
						payload += attributes[i];
				}
				response = new Response(ResponseCode.CONTENT);
				response.setPayload(payload);
				break;
			case 2:
				// node read (mode=2&ser=<SERIAL>)
				outStream.out("Handling sensor reading for serial \"" + serial + "\"", i);
				Tag_ tag2 = tagMap.get(serial);
				if (tag2==null) {
					debugStream.out("Serial \"" + serial + "\" is not registered on this MN", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				String id = tag2.id;
				String[] uri = new String[] {context + Constants.mnPostfix, id, "data", "la"};
				CoapResponse cin = null;
				try {
					cin = client.services.getResource(uri,i);
				} catch (URISyntaxException e) {
					errStream.out(e,0,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					i++;
					return;
				}
				response = new Response(ResponseCode.CONTENT);
				response.setPayload(Services.parseJSON(cin.getResponseText(), "m2m:cin", //
						new String[] {"con"}, new Class<?>[] {String.class}));
				break;
			}
		} else {
			response = new Response(ResponseCode.CONTENT);
			response.setPayload("MN " + name);
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
					// node MN registration (id=<ID>&ser=<SERIAL>&type=<TYPE>{&addr=<URI>}, PAYLOAD [<ATTRIBUTE>])
					if (!isValidType(type)) {
						debugStream.out("Bad request, type=" + type, i);
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
						String address = getUriValue(exchange,"addr",3);
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
					outStream.out("Registering node \"" + id + "\" (serial \"" + serial + "\")", i);
					tagMap.put(serial,tag);
					response = new Response(ResponseCode.CREATED);
				} else {
					// node subscription (id=<ID>&ser=<SERIAL>)
					Tag_ tag = tagMap.get(serial);
					if (tag==null) {
						debugStream.out("Serial \"" + serial + "\" is not registered on this MN", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					String address = userMap.get(id);
					if (address==null) {
						debugStream.out("User \"" + id + "\" is not registered on this MN", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out("Subscribing user \"" + id + "\" (address \"" + address + "\") to resource \"" + tag.id + "\" (serial \"" + serial + "\")", i);
					String[] uri = new String[] {context + Constants.mnPostfix, tag.id, "data"};
					CoapResponse response_;
					try {
						response_ = subscriber.insert(Constants._mnADNPort+"/"+getName(),tag.id,uri,address,i);
					} catch (URISyntaxException e) {
						errStream.out(e,0,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					if (response_==null || response_.getCode()!=ResponseCode.CREATED) {
						errStream.out("Unable to subscribe user to " + client.services.uri() + ", response: " + response_.getCode(), //
								i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					}
					response = new Response(ResponseCode.CREATED);
				}
			} else {
				// user MN registration (id=<ID>&addr=<URI>)
				String address = getUriValue(exchange,"addr",1);
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
				outStream.out("Registering user \"" + id + "\" (address \"" + address + "\")", i);
				userMap.put(id,address);
				response = new Response(ResponseCode.CREATED);
			}
		} else {
			String serial0 = getUriValue(exchange,"ser",0);
			String serial1 = getUriValue(exchange,"ser",1);
			String id0 = getUriValue(exchange,"id",2);
			String id1 = getUriValue(exchange,"id",3);
			if (serial0!=null && serial1!=null && id0!=null && id1!=null) {
				// nodes link (ser=<SERIAL>&ser=<SERIAL>&id=<EVENT_ID>&id=<ACTION_ID>)
				if (!isValidSerial(serial0) && isValidSerial(serial1)) {
					debugStream.out("Bad request, ser0=" + serial0, i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				if (isValidSerial(serial0) && !isValidSerial(serial1)) {
					debugStream.out("Bad request, ser1=" + serial1, i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				if (!isValidSerial(serial0) && !isValidSerial(serial1)) {
					debugStream.out("Bad request, ser0=" + serial0 + ", ser1=" + serial1, i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				Tag_ tag0 = tagMap.get(serial0);
				Tag_ tag1 = tagMap.get(serial1);
				if (tag0==null) {
					debugStream.out("Serial \"" + serial0 + "\" is not registered on this MN", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				if (tag1==null) {
					debugStream.out("Serial \"" + serial1 + "\" is not registered on this MN", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				int i0;
				int i1;
				try {
					i0 = Integer.parseInt(id0);
				} catch (NumberFormatException e) {
					debugStream.out("Bad request, id0=" + id0, i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				if (i0>=tag0.attributes.length) {
					debugStream.out("Bad request, id0=" + id0 + " (out of bounds)", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				try {
					i1 = Integer.parseInt(id1);
				} catch (NumberFormatException e) {
					debugStream.out("Bad request, id1=" + id1, i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				if (i1>=tag1.attributes.length) {
					debugStream.out("Bad request, id1=" + id1 + " (out of bounds)", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				outStream.out("Linking sensor \"" + tag0.id + "\" (serial \"" + serial0 + //
						"\") to actuator \"" + tag1.id + "\" (serial \"" + serial1 + "\")", i);
				String[] uri = new String[] {context + Constants.mnPostfix, tag0.id, "data"};
				CoapResponse response_;
				try {
					response_ = subscriber.insert(Constants._mnADNPort+"/"+getName(),tag0.id,uri,tag0.attributes[i0],tag1.description,tag1.attributes[i1],i);
				} catch (URISyntaxException e) {
					errStream.out(e,0,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					i++;
					return;
				}
				if (response_==null || response_.getCode()!=ResponseCode.CREATED) {
					errStream.out("Unable to register link on " + client.services.uri() + ", response: " + response_.getCode(), //
							i, Severity.LOW);
					response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
					exchange.respond(response);
					i++;
					return;
				}
				response = new Response(ResponseCode.CREATED);
			} else {
				// notification, TODO
				System.out.println("Got something");
				System.out.println(exchange.getRequestText());
				response = new Response(ResponseCode.VALID);
			}
		}
		exchange.respond(response);
		i++;
	}
	
	@Override
	
	synchronized public void handlePUT(CoapExchange exchange) {
		// node write (ser=<SERIAL>&id=<ACTION_ID>), TODO
	}
	
	@Override
	
	synchronized public void handleDELETE(CoapExchange exchange) {
		// subscription removal (id=<ID>&ser=<SERIAL>), TODO
		// link removal (ser=<SERIAL>&ser=<SERIAL>&id=<EVENT_ID>&id=<ACTION_ID>), TODO
		// node/user removal (id=<ID>), TODO
	}

}
