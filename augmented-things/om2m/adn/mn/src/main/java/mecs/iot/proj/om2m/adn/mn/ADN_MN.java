package mecs.iot.proj.om2m.adn.mn;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.adn.ADN;
import mecs.iot.proj.om2m.adn.Reference;
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
	private boolean subscriptionsEnabled;
	private String notificationId;
	private String notificationAddress;

	ADN_MN(String id, String host, String uri, String context, boolean debug, Console console) throws URISyntaxException {
		super(Services.joinIdHost(id+"_server",host), uri, context, debug, console);
		cseClient = new Client(Services.joinIdHost(id+"_CSEclient",host), Constants.cseProtocol + "localhost" + Constants.mnRoot + context + Constants.mnCSEPostfix, debug);
		notificationClient = new Client(Services.joinIdHost(id+"_ATclient",host),debug);
		subscriber = new Subscriber();
		tagMap = new HashMap<String,Tag_>();
		userMap = new HashMap<String,String>();
		subscriptionsEnabled = true;
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
				Tag_ tag1 = tagMap.get(serial);
				if (tag1==null) {
					debugStream.out("Serial \"" + serial + "\" is not registered on this MN", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				outStream.out("Handling attributes querying for serial \"" + serial + "\"", i);
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
				Tag_ tag2 = tagMap.get(serial);
				if (tag2==null || tag2.node!=Node.SENSOR) {
					debugStream.out("Serial \"" + serial + "\" is not registered on this MN as a sensor", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				outStream.out("Handling sensor reading for serial \"" + serial + "\"", i);
				String id = tag2.id;
				String[] uri = new String[] {context + Constants.mnPostfix, id, "data", "la"};
				CoapResponse response_ = null;
				try {
					cseClient.stepCount();
					response_ = cseClient.services.getResource(uri,cseClient.getCount());
				} catch (URISyntaxException e) {
					errStream.out(e,0,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					i++;
					return;
				}
				if (response_==null) {
					errStream.out("Unable to read from " + cseClient.services.uri(), //
							i, Severity.LOW);
					response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
					exchange.respond(response);
					i++;
					return;
				}
				response = new Response(ResponseCode.CONTENT);
				String con = Services.parseJSON(response_.getResponseText(), "m2m:cin", //
						new String[] {"con"}, new Class<?>[] {String.class});
				response.setPayload(id + ": " + con);
				break;
			default:
				debugStream.out("Bad request, mode=" + mode, i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
		} else if (exchange.getRequestOptions().getUriQuery().size()==0) {
			response = new Response(ResponseCode.CONTENT);
			response.setPayload("MN: " + name);
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
					// node lookout (id=<ID>&ser=<SERIAL>)
					if (subscriptionsEnabled) {
						Tag_ tag = tagMap.get(serial);
						if (tag==null || tag.node!=Node.SENSOR) {
							debugStream.out("Serial \"" + serial + "\" is not registered on this MN as a sensor", i);
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
						notificationId = id;
						notificationAddress = address;
						outStream.out("Subscribing user \"" + id + "\" (address \"" + address + "\") to resource \"" + tag.id + "\" (serial \"" + serial + "\")", i);
						String[] uri = new String[] {context + Constants.mnPostfix, tag.id, "data"};
						cseClient.stepCount();
						try {
							cseClient.services.postSubscription(Constants._mnADNPort+"/"+getName(),"subscription",uri,cseClient.getCount());
						} catch (URISyntaxException e) {
							errStream.out(e,0,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							i++;
							return;
						}
						subscriber.insert(tag.id,id,address);
						if (!subscriber.containsResource(tag.id))
							subscriptionsEnabled = false;
						response = new Response(ResponseCode.CONTINUE);
					} else {
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
					}
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
			String label0 = getUriValue(exchange,"lab",2);
			String label1 = getUriValue(exchange,"lab",3);
			notificationId = getUriValue(exchange,"id",4);
			if (serial0!=null && serial1!=null && label0!=null && label1!=null) {
				if (subscriptionsEnabled) {
					// nodes link (ser=<SERIAL>&ser=<SERIAL>&lab=<EVENT_LABEL>&lab=<ACTION_LABEL>&id=<ID>)
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
					if (tag0==null || tag0.node!=Node.SENSOR) {
						debugStream.out("Serial \"" + serial0 + "\" is not registered on this MN as a sensor", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					if (tag1==null || tag1.node!=Node.ACTUATOR) {
						debugStream.out("Serial \"" + serial1 + "\" is not registered on this MN as an actuator", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					if (!isValidLabel(label0,tag0)) {
						debugStream.out("Bad request, lab=" + label0, i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					if (!isValidLabel(label1,tag1)) {
						debugStream.out("Bad request, lab=" + label1, i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					notificationAddress = userMap.get(notificationId);
					if (notificationAddress==null) {
						debugStream.out("User \"" + notificationId + "\" is not registered on this MN", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out("Linking sensor \"" + tag0.id + "\" (serial \"" + serial0 + //
							"\") to actuator \"" + tag1.id + "\" (serial \"" + serial1 + "\")", i);
					String[] uri = new String[] {context + Constants.mnPostfix, tag0.id, "data"};
					cseClient.stepCount();
					try {
						cseClient.services.postSubscription(Constants._mnADNPort+"/"+getName(),"subscription",uri,cseClient.getCount());
					} catch (URISyntaxException e) {
						errStream.out(e,0,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					subscriber.insert(tag0.id,label0,tag1.id,tag1.address,label1);
					if (!subscriber.containsResource(tag0.id))
						subscriptionsEnabled = false;
					response = new Response(ResponseCode.CONTINUE);
				} else {
					response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
				}
			} else {
				// notifications from subscriptions
				String notification = exchange.getRequestText();
				if (notification.contains("m2m:vrq")) {
					forwardNotification(notificationId,notificationAddress,"OK");
				} else {
					String pi = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cin"}, // Example: "pi=/augmented-things-MN-cse/cnt-67185819"
							new String[] {"pi"}, new Class<?>[] {String.class});
					String con = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cin"}, // Example: "con=36,404 °C"
							new String[] {"con"}, new Class<?>[] {String.class});
					// String sur = Services.parseJSON(notification, "m2m:sgn", // "Example: sur=/augmented-things-MN-cse/sub-730903481"
					//		new String[] {"sur"}, new Class<?>[] {String.class});
					outStream.out("Received JSON: " + pi + ", " + con, i);
					String key = getKey(pi);
					if (!subscriber.containsKey(key)) {
						subscriber.bindToLastResource(key);
						subscriptionsEnabled = true;
					}
					ArrayList<Reference> refs = subscriber.get(key);
					if (refs!=null && refs.size()>0) {
						for (int i=0; i<refs.size(); i++) {
							switch (refs.get(i).node) {
								case SENSOR:
									break;
								case ACTUATOR:
									forwardNotification(refs.get(i).receiver,refs.get(i).address,refs.get(i).action); // TODO: check events
									break;
								case USER:
									forwardNotification(refs.get(i).receiver,refs.get(i).address,subscriber.getName(key)+": "+con);
									break;
							}
						}
					}
				}
				response = new Response(ResponseCode.CREATED);
			}
		}
		exchange.respond(response);
		i++;
	}
	
	@Override
	
	synchronized public void handlePUT(CoapExchange exchange) {
		Response response = null;
		// node write (ser=<SERIAL>&lab=<ACTION_LABEL>)
		String serial = getUriValue(exchange,"ser",0);
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
		Tag_ tag = tagMap.get(serial);
		if (tag==null || tag.node!=Node.ACTUATOR) {
			debugStream.out("Serial \"" + serial + "\" is not registered on this MN as an actuator", i);
			response = new Response(ResponseCode.BAD_REQUEST);
			exchange.respond(response);
			i++;
			return;
		}
		String label = getUriValue(exchange,"lab",1);
		if (label==null || !isValidLabel(label,tag)) {
			if (label!=null)
				debugStream.out("Bad request, lab=" + label, i);
			else
				debugStream.out("Bad request, lab", i);
			response = new Response(ResponseCode.BAD_REQUEST);
			exchange.respond(response);
			i++;
			return;
		}
		outStream.out("Handling actuator writing for serial \"" + serial + "\"", i);
		notificationClient.stepCount();
		try {
			notificationClient.connect(tag.address,false);
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
			response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
			exchange.respond(response);
			i++;
			return;
		}
		Request request = new Request(Code.PUT);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.setPayload(label);
		CoapResponse response_ = notificationClient.send(request);
		if (response_==null || response_.getCode()!=ResponseCode.CHANGED) {
			errStream.out("Unable to write on actuator \"" + tag.id + "\", response: " + response_.getCode(), //
					i, Severity.LOW);
			response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
			exchange.respond(response);
			i++;
			return;
		}
		response = new Response(ResponseCode.CHANGED);
		response.setPayload(tag.id + ": " + label);
		exchange.respond(response);
		i++;
	}
	
	@Override
	
	synchronized public void handleDELETE(CoapExchange exchange) {
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
				// subscription removal (id=<ID>&ser=<SERIAL>), TODO
			} else {
				// user removal (id=<ID>)
				if (userMap.containsKey(id)) {
					subscriber.remove(id);
					ArrayList<String> resources = subscriber.emptyRefs();
					for (int i=0; i<resources.size(); i++) {
						String[] uri = new String[] {context + Constants.mnPostfix, resources.get(i), "data", "subscription"};
						cseClient.stepCount();
						try {
							cseClient.services.deleteSubscription(uri,cseClient.getCount());
						} catch (URISyntaxException e) {
							errStream.out(e,0,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							i++;
							return;
						}
					}
					userMap.remove(id);
				} else {
					debugStream.out("User \"" + id + "\" is not registered on this MN", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
			}
		} else {
			String serial0 = getUriValue(exchange,"ser",0);
			if (serial0!=null) {
				if (!isValidSerial(serial0)) {
					debugStream.out("Bad request, ser=" + serial0, i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				String serial1 = getUriValue(exchange,"ser",1);
				if (serial1!=null) {
					if (!isValidSerial(serial1)) {
						debugStream.out("Bad request, ser=" + serial1, i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					if (!tagMap.containsKey(serial0) || tagMap.get(serial0).node!=Node.SENSOR) {
						debugStream.out("Serial \"" + serial0 + "\" is not registered on this MN as a sensor", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					if (!tagMap.containsKey(serial1) || tagMap.get(serial1).node!=Node.ACTUATOR) {
						debugStream.out("Serial \"" + serial1 + "\" is not registered on this MN as an actuator", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					// link removal (ser=<SERIAL>&ser=<SERIAL>&lab=<EVENT_LABEL>&lab=<ACTION_LABEL>&id=<ID>), TODO
				} else {
					// node removal (ser=<SERIAL>)
					if (!tagMap.containsKey(serial0)) {
						debugStream.out("Serial \"" + serial0 + "\" is not registered on this MN", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					subscriber.remove(tagMap.get(serial0).id);
					ArrayList<String> resources = subscriber.emptyRefs();
					for (int i=0; i<resources.size(); i++) {
						String[] uri = new String[] {context + Constants.mnPostfix, resources.get(i), "data", "subscription"};
						cseClient.stepCount();
						try {
							cseClient.services.deleteSubscription(uri,cseClient.getCount());
						} catch (URISyntaxException e) {
							errStream.out(e,0,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							i++;
							return;
						}
					}
					tagMap.remove(serial0);
				}
			} else {
				debugStream.out("Bad request, ser", i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
		}
		response = new Response(ResponseCode.DELETED);
		exchange.respond(response);
		i++;
	}
	
	private Response forwardNotification(String id, String address, String content) {
		notificationClient.stepCount();
		try {
			notificationClient.connect(address,false);
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
			return new Response(ResponseCode.INTERNAL_SERVER_ERROR);
		}
		Request request = new Request(Code.PUT);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.setPayload(content);
		CoapResponse response_ = notificationClient.send(request);
		if (response_==null || response_.getCode()!=ResponseCode.CHANGED) {
			errStream.out("Unable to send data to \"" + id + "\", response: " + response_.getCode(), //
					i, Severity.LOW);
			return new Response(ResponseCode.SERVICE_UNAVAILABLE);
		} else {
			return new Response(ResponseCode.CHANGED);
		}
	}
	
	private String getKey(String pi) {
		return pi.split("cnt-")[0];
	}
}
