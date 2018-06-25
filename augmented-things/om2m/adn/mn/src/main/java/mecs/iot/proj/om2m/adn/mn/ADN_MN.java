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
		subscriber = new Subscriber(debugStream,errStream,cseClient,context);
		tagMap = new HashMap<String,Tag_>();
		userMap = new HashMap<String,String>();
		subscriptionsEnabled = true;
		notificationId = "";
		notificationAddress = "";
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
			if (sw!=1 || sw!=2) {
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
			Tag_ tag = tagMap.get(serial);
			switch (sw) {
				case 1:
					// attributes query (mode=1&ser=<SERIAL>)		
					if (tag==null || tag.node==Node.USER) {
						debugStream.out("Serial \"" + serial + "\" is not registered on this MN as an endpoint node", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out1("Handling attributes querying for serial \"" + serial + "\"", i);
					String[] attributes = tag.attributes;
					String payload = "";
					for (int j=0; j<attributes.length; j++) {
						if (j!=attributes.length-1)
							payload += attributes[j] + ",";
						else
							payload += attributes[j];
					}
					response = new Response(ResponseCode.CONTENT);
					response.setPayload(payload);
					break;
				case 2:
					// node read (mode=2&ser=<SERIAL>)
					if (tag==null || tag.node!=Node.SENSOR) {
						debugStream.out("Serial \"" + serial + "\" is not registered on this MN as a sensor", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out1("Handling reading on sensor with serial \"" + serial + "\"", i);
					String id = tag.id;
					String[] uri = new String[] {context + Constants.mnPostfix, id, "data", "la"};
					CoapResponse response_ = null;
					try {
						cseClient.stepCount();
						response_ = cseClient.services.getResource(uri,cseClient.getCount());
					} catch (URISyntaxException e) {
						outStream.out2("failed");
						errStream.out(e,0,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					if (response_==null) {
						outStream.out2("failed");
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
			outStream.out1("Handling MN name request", i);
			response = new Response(ResponseCode.CONTENT);
			response.setPayload("MN: " + name);
		} else {
			debugStream.out("Bad request, mode not specified", i);
			response = new Response(ResponseCode.BAD_REQUEST);
			exchange.respond(response);
			i++;
			return;
		}
		exchange.respond(response);
		outStream.out2("done");
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
					outStream.out1("Registering node \"" + id + "\" with serial \"" + serial + "\"", i);
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
						outStream.out1("Subscribing user \"" + id + "\" to resource with serial \"" + serial + "\"", i);
						String[] uri = new String[] {context + Constants.mnPostfix, tag.id, "data"};
						cseClient.stepCount();
						try {
							cseClient.services.postSubscription(Constants._mnADNPort+"/"+getName(),"subscription",uri,cseClient.getCount());
						} catch (URISyntaxException e) {
							outStream.out2("failed");
							errStream.out(e,0,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							i++;
							return;
						}
						subscriber.insert(tag.id,id,address);
						if (!subscriber.containsResource(tag.id))
							subscriptionsEnabled = false;								// Disable subscription service to determine the pi identifier associated to this subscription
						response = new Response(ResponseCode.CONTINUE);
					} else {
						outStream.out1("Subscription service temporarily disabled", i);
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
				outStream.out1("Registering user \"" + id + "\" with address \"" + address + "\"", i);
				userMap.put(id,address);
				response = new Response(ResponseCode.CREATED);
			}
		} else {
			String serial0 = getUriValue(exchange,"ser",0);
			String serial1 = getUriValue(exchange,"ser",1);
			String label0 = getUriValue(exchange,"lab",2);
			String label1 = getUriValue(exchange,"lab",3);
			notificationId = getUriValue(exchange,"id",4);
			if (serial0!=null && serial1!=null && label0!=null && label1!=null && notificationId!=null) {
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
					outStream.out1("Linking sensor with serial \"" + serial0 + "\" to actuator with serial \"" + serial1 + "\"", i);
					String[] uri = new String[] {context + Constants.mnPostfix, tag0.id, "data"};
					cseClient.stepCount();
					try {
						cseClient.services.postSubscription(Constants._mnADNPort+"/"+getName(),"subscription",uri,cseClient.getCount());
					} catch (URISyntaxException e) {
						outStream.out2("failed");
						errStream.out(e,0,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					subscriber.insert(tag0.id,label0,tag1.id,tag1.address,label1);
					if (!subscriber.containsResource(tag0.id))
						subscriptionsEnabled = false;								// Disable subscription service to determine the pi identifier associated to this subscription
					response = new Response(ResponseCode.CONTINUE);
				} else {
					outStream.out1("Subscription service temporarily disabled", i);
					response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
				}
			} else {
				// notifications from subscriptions
				String notification = exchange.getRequestText();
				if (notification.contains("m2m:vrq")) {
					outStream.out1("Handling subscription confirmation", i);
					CoapResponse response_ = null;
					try {
						response_ = forwardNotification(notificationId,notificationAddress,"OK");	// Warn the requester about completed subscription
					} catch (URISyntaxException e) {
						outStream.out2("failed");
						errStream.out(e,0,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					if (response_==null || response_.getCode()!=ResponseCode.CHANGED) {
						outStream.out2("failed");
						errStream.out("Unable to send data to \"" + id + "\", response: " + response_.getCode(), //
								i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					}
				} else {
					String pi = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cin"}, // Example: "pi=/augmented-things-MN-cse/cnt-67185819"
							new String[] {"pi"}, new Class<?>[] {String.class});
					String con = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cin"}, // Example: "con=36,404 °C"
							new String[] {"con"}, new Class<?>[] {String.class});
					// String sur = Services.parseJSON(notification, "m2m:sgn", // "Example: sur=/augmented-things-MN-cse/sub-730903481"
					//		new String[] {"sur"}, new Class<?>[] {String.class});
					outStream.out1("Handling notification with JSON: " + pi + ", " + con, i);
					String key = getKey(pi);
					if (!subscriber.containsKey(key)) {
						subscriber.bindToLastResource(key);							// If this is a brand new notification, associated this pi to the last subscription
						subscriptionsEnabled = true;								// Re-enable subscription service
					}
					ArrayList<Reference> refs = subscriber.get(key);
					if (refs!=null && refs.size()>0) {
						CoapResponse response_ = null;
						for (int j=0; j<refs.size(); j++) {
							switch (refs.get(j).node) {
								case SENSOR:
									break;
								case ACTUATOR:
									try {
										response_ = forwardNotification(refs.get(j).receiver,refs.get(j).address,refs.get(j).action);
									} catch (URISyntaxException e) {
										outStream.out2("failed");
										errStream.out(e,0,Severity.MEDIUM);
										response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
										exchange.respond(response);
										i++;
										return;
									} // TODO: check events
									break;
								case USER:
									try {
										response_ = forwardNotification(refs.get(j).receiver,refs.get(j).address,subscriber.getName(key)+": "+con);
									} catch (URISyntaxException e) {
										outStream.out2("failed");
										errStream.out(e,0,Severity.MEDIUM);
										response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
										exchange.respond(response);
										i++;
										return;
									}
									break;
							}
							if (response_==null || response_.getCode()!=ResponseCode.CHANGED) {
								outStream.out2("failed");
								errStream.out("Unable to send data to \"" + id + "\", response: " + response_.getCode(), //
										i, Severity.LOW);
								response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
								exchange.respond(response);
								i++;
								return;
							}
						}
					}
				}
				response = new Response(ResponseCode.CREATED);
			}
		}
		exchange.respond(response);
		outStream.out2("done");
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
		outStream.out1("Handling writing on actuator with serial \"" + serial + "\"", i);
		notificationClient.stepCount();
		try {
			notificationClient.connect(tag.address,false);
		} catch (URISyntaxException e) {
			outStream.out2("failed");
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
		CoapResponse response_ = notificationClient.send(request, Code.PUT);
		if (response_==null || response_.getCode()!=ResponseCode.CHANGED) {
			outStream.out2("failed");
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
		outStream.out2("done");
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
			String address = userMap.get(id);
			if (address==null) {
				debugStream.out("User \"" + id + "\" is not registered on this MN", i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
			String serial = getUriValue(exchange,"ser",1);
			if (serial!=null) {
				// lookout removal (id=<ID>&ser=<SERIAL>)
				if (!isValidSerial(serial)) {
					debugStream.out("Bad request, ser=" + serial, i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				Tag_ tag = tagMap.get(serial);
				if (tag==null || tag.node!=Node.SENSOR) {
					debugStream.out("Serial \"" + serial + "\" is not registered on this MN as a sensor", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				outStream.out1("Handling removal of lookout between user \"" + id + "\" and serial \"" + serial + "\"", i);
				try {
					subscriber.remove(tag.id,id,i);
				} catch (URISyntaxException e) {
					outStream.out2("failed");
					errStream.out(e,0,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					i++;
					return;
				}
			} else {
				// user removal (id=<ID>)
				outStream.out1("Handling removal of user \"" + id + "\"", i);
				try {
					subscriber.remove(id,Node.USER,i);
				} catch (URISyntaxException e) {
					outStream.out2("failed");
					errStream.out(e,0,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					i++;
					return;
				}
				userMap.remove(id);
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
				Tag_ tag0 = tagMap.get(serial0);
				String serial1 = getUriValue(exchange,"ser",1);
				if (serial1!=null) {
					// link removal (ser=<SERIAL>&ser=<SERIAL>&lab=<EVENT_LABEL>&lab=<ACTION_LABEL>&id=<ID>)
					if (tag0==null || tag0.node!=Node.SENSOR) {
						debugStream.out("Serial \"" + serial0 + "\" is not registered on this MN as a sensor", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					if (!isValidSerial(serial1)) {
						debugStream.out("Bad request, ser=" + serial1, i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					Tag_ tag1 = tagMap.get(serial1);
					if (tag1==null || tag1.node!=Node.ACTUATOR) {
						debugStream.out("Serial \"" + serial1 + "\" is not registered on this MN as an actuator", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					String label0 = getUriValue(exchange,"lab",2);
					String label1 = getUriValue(exchange,"lab",3);
//					String notificationId = getUriValue(exchange,"id",4);
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
//					notificationAddress = userMap.get(notificationId);
//					if (notificationAddress==null) {
//						debugStream.out("User \"" + notificationId + "\" is not registered on this MN", i);
//						response = new Response(ResponseCode.BAD_REQUEST);
//						exchange.respond(response);
//						i++;
//						return;
//					}
					outStream.out1("Handling removal of link between serial \"" + serial0 + "\" and serial \"" + serial1 + "\"", i);
					try {
						subscriber.remove(tag0.id,label0,tag1.id,label1,i);
					} catch (URISyntaxException e) {
						outStream.out2("failed");
						errStream.out(e,0,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
				} else {
					// node removal (ser=<SERIAL>)
					if (tag0==null || tag0.node==Node.USER) {
						debugStream.out("Serial \"" + serial0 + "\" is not registered on this MN as an endpoint node", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out1("Handling removal of node with serial \"" + serial0 + "\"", i);
					String[] uri = null;
					CoapResponse response_ = null;
					switch (tag0.node) {
						case SENSOR:
							try {
								subscriber.remove(tag0.id,Node.SENSOR,i);
							} catch (URISyntaxException e) {
								outStream.out2("failed");
								errStream.out(e,0,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								i++;
								return;
							}
							outStream.out1_2("deleting subscription on \"" + tag0.id + "\"");
							uri = new String[] {context + Constants.mnPostfix, tag0.id, "data", "subscription"};
							cseClient.stepCount();
							try {
								response_ = cseClient.services.deleteSubscription(uri,cseClient.getCount());
							} catch (URISyntaxException e) {
								outStream.out2("failed");
								errStream.out(e,0,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								i++;
								return;
							}
							if (response_==null || response_.getCode()!=ResponseCode.DELETED) {
								outStream.out2("failed");
								errStream.out("Unable to delete subscription on \"" + tag0.id + "\", response: " + response_.getCode(), //
										i, Severity.LOW);
								response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
								exchange.respond(response);
								i++;
								return;
							}
							break;
						case ACTUATOR:
							try {
								subscriber.remove(tag0.id,Node.ACTUATOR,i);
							} catch (URISyntaxException e) {
								outStream.out2("failed");
								errStream.out(e,0,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								i++;
								return;
							}
							break;
						case USER:
							break;
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
		outStream.out2("done");
		i++;
	}
	
	private CoapResponse forwardNotification(String id, String address, String content) throws URISyntaxException {
		outStream.out1_2("forwarding notification to \"" + id + "\"");
		notificationClient.stepCount();
		notificationClient.connect(address,false);
		Request request = new Request(Code.PUT);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.setPayload(content);
		return notificationClient.send(request, Code.PUT);
	}
	
	private String getKey(String pi) {
		return pi.split("cnt-")[0];
	}
}
