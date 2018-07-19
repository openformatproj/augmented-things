package mecs.iot.proj.om2m.adn.mn;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONException;
import org.json.JSONObject;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.adn.ADN;
import mecs.iot.proj.om2m.adn.mn.exceptions.*;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.exceptions.InvalidRuleException;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.JSONSerializable;
import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Tag;

class ADN_MN extends ADN {

	public Client notificationClient;
	
	private HashMap<String,Tag> tagMap;																					// serial -> tag
	private HashMap<String,User> userMap;																				// user id -> user
	
	private Subscriber subscriber;

	ADN_MN(String id, String host, boolean debug, Console console) throws URISyntaxException, StateCreationException, RegistrationException {
		super(id,host,debug,console);
		cseClient = new Client(Services.joinIdHost(id+"/CSEclient",host), Constants.protocol + "localhost" + Constants.mnCSERoot(id), debug);
		notificationClient = new Client(Services.joinIdHost(id+"/ATclient",host),debug);
		// TODO: pull from OM2M
		tagMap = new HashMap<String,Tag>();
		userMap = new HashMap<String,User>();
		outStream.out("Posting state",i);
		outStream.out1("Posting main AE",i);
		cseClient.stepCount();
		CoapResponse response_ = cseClient.services.postAE("state",cseClient.getCount());
		if (response_==null) {
			outStream.out2("failed");
			errStream.out("Unable to post AE to " + cseClient.services.uri() + ", timeout expired", i, Severity.LOW);
			throw new StateCreationException();
		} else if (response_.getCode()!=ResponseCode.CREATED && response_.getCode()!=ResponseCode.FORBIDDEN) {
			outStream.out2("failed. Terminating interface");
			if (!response_.getResponseText().isEmpty())
				errStream.out("Unable to post AE to " + cseClient.services.uri() + ", response: " + response_.getCode() +
						", reason: " + response_.getResponseText(),
						i, Severity.LOW);
			else
				errStream.out("Unable to post AE to " + cseClient.services.uri() + ", response: " + response_.getCode(),
					i, Severity.LOW);
			throw new StateCreationException();
		}
		if (response_.getCode()==ResponseCode.FORBIDDEN) {
			debugStream.out(response_.getResponseText(), i);
		} else {
			String json = null;
			try {
				json = Services.parseJSON(response_.getResponseText(), "m2m:ae",
						new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
			} catch (JSONException e) {
				outStream.out2("failed");
				errStream.out(e, i, Severity.MEDIUM);
				throw e;
			}
			debugStream.out("Received JSON: " + json, i);
		}
		outStream.out1_2("done, posting tagMap");
		cseClient.stepCount();
		response_ = cseClient.services.postContainer(cseBaseName,"state","tagMap",cseClient.getCount());
		if (response_==null) {
			outStream.out2("failed");
			errStream.out("Unable to post Container to " + cseClient.services.uri() + ", timeout expired", i, Severity.LOW);
			throw new StateCreationException();
		} else if (response_.getCode()!=ResponseCode.CREATED && response_.getCode()!=ResponseCode.FORBIDDEN) {
			outStream.out2("failed");
			if (!response_.getResponseText().isEmpty())
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response_.getCode() +
						", reason: " + response_.getResponseText(),
						i, Severity.LOW);
			else
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response_.getCode(),
					i, Severity.LOW);
			throw new StateCreationException();
		}
		if (response_.getCode()==ResponseCode.FORBIDDEN) {
			debugStream.out(response_.getResponseText(), i);
		} else {
			String json = null;
			try {
				json = Services.parseJSON(response_.getResponseText(), "m2m:cnt",
						new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
			} catch (JSONException e) {
				outStream.out2("failed");
				errStream.out(e, i, Severity.MEDIUM);
				throw e;
			}
			debugStream.out("Received JSON: " + json, i);
		}
		outStream.out1_2("done, posting userMap");
		cseClient.stepCount();
		response_ = cseClient.services.postContainer(cseBaseName,"state","userMap",cseClient.getCount());
		if (response_==null) {
			outStream.out2("failed");
			errStream.out("Unable to post Container to " + cseClient.services.uri() + ", timeout expired", i, Severity.LOW);
			throw new StateCreationException();
		} else if (response_.getCode()!=ResponseCode.CREATED && response_.getCode()!=ResponseCode.FORBIDDEN) {
			outStream.out2("failed");
			if (!response_.getResponseText().isEmpty())
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response_.getCode() +
						", reason: " + response_.getResponseText(),
						i, Severity.LOW);
			else
				errStream.out("Unable to post Container to " + cseClient.services.uri() + ", response: " + response_.getCode(),
					i, Severity.LOW);
			throw new StateCreationException();
		}
		if (response_.getCode()==ResponseCode.FORBIDDEN) {
			debugStream.out(response_.getResponseText(), i);
		} else {
			String json = null;
			try {
				json = Services.parseJSON(response_.getResponseText(), "m2m:cnt",
						new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
			} catch (JSONException e) {
				outStream.out2("failed");
				errStream.out(e, i, Severity.MEDIUM);
				throw e;
			}
			debugStream.out("Received JSON: " + json, i);
		}
		outStream.out1_2("done, posting subscription state");
		subscriber = new Subscriber(debugStream,errStream,cseClient,cseBaseName);
		outStream.out1_2("done, registering to IN");
		Client tempClient = new Client(Services.joinIdHost(id+"/TEMPclient",host), Constants.protocol + Constants.inAddressADN(debugStream,0) + Constants.inADNRoot, debug);
		Request request = new Request(Code.POST);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + Services.normalizeName(cseBaseName));
		response_ = tempClient.send(request, Code.POST);
		if (response_==null) {
			outStream.out2("failed");
			errStream.out("Unable to register to IN at address " + tempClient.services.uri() + ", timeout expired", i, Severity.LOW);
			throw new RegistrationException();
		} else if (response_.getCode()!=ResponseCode.CREATED) {
			outStream.out2("failed");
			if (!response_.getResponseText().isEmpty())
				errStream.out("Unable to register to IN at address " + tempClient.services.uri() + ", response: " + response_.getCode() +
						", reason: " + response_.getResponseText(),
						i, Severity.LOW);
			else
				errStream.out("Unable to register to IN at address " + tempClient.services.uri() + ", response: " + response_.getCode(),
					i, Severity.LOW);
			throw new RegistrationException();
		}
		outStream.out2("done");
		i++;
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
			Tag tag = tagMap.get(serial);
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
							payload += attributes[j] + ", ";
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
					String[] uri = new String[] {cseBaseName, id, "data", "la"};
					CoapResponse response_ = null;
					cseClient.stepCount();
					try {
						response_ = cseClient.services.getResource(uri,cseClient.getCount());
					} catch (URISyntaxException e) {
						outStream.out2("failed");
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					if (response_==null) {
						outStream.out2("failed");
						errStream.out("Unable to read from " + cseClient.services.uri() + ", timeout expired", i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					} else if (response_.getCode()!=ResponseCode.CONTENT) {
						outStream.out2("failed");
						errStream.out("Unable to read from " + cseClient.services.uri() + ", response: " + response_.getCode(),
								i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					}
					response = new Response(ResponseCode.CONTENT);
					String con = null;
					try {
						con = Services.parseJSON(response_.getResponseText(), "m2m:cin",
								new String[] {"con"}, new Class<?>[] {String.class});
					} catch (JSONException e) {
						outStream.out2("failed");
						errStream.out(e, i, Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
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
			response.setPayload("mn: " + name);
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
					String[] attributes = payload.split(", ");
					Integer k = new Integer(0);
					if (!areValidAttributes(attributes,k)) {
						debugStream.out("Bad request, attribute=" + attributes[k], i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					Tag tag = null;
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
						tag = new Tag(Node.ACTUATOR,id,address,attributes,cseBaseName);
					} else {
						tag = new Tag(Node.SENSOR,id,type,attributes,cseBaseName);
					}
					outStream.out1("Registering node \"" + id + "\" with serial \"" + serial + "\"", i);
					boolean createState;
					String[] uri_;
					if (tagMap.containsKey(serial)) {
						createState = false;
						uri_ = new String[] {cseBaseName, "state", "tagMap", serial};
					}
					else {
						createState = true;
						uri_ = new String[] {cseBaseName, "state", "tagMap"};
					}
					CoapResponse response_ = null;
					cseClient.stepCount();
					try {
						response_ = cseClient.services.oM2Mput(serial,tag,uri_,createState,cseClient.getCount());
					} catch (URISyntaxException e) {
						outStream.out2("failed");
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					if (response_==null) {
						outStream.out2("failed");
						errStream.out("Unable to register node \"" + id + "\" on CSE, timeout expired", i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					} else if (response_.getCode()!=ResponseCode.CREATED) {
						outStream.out2("failed");
						errStream.out("Unable to register node \"" + id + "\" on CSE, response: " + response_.getCode(),
								i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					}
					if (createState) {
						cseClient.stepCount();
						try {
							cseClient.connect(Constants.protocol + "localhost" + Constants.mnCSERoot(cseBaseName));
						} catch (URISyntaxException e) {
							outStream.out2("failed");
							errStream.out(e,i,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							i++;
							return;
						}
						response_ = cseClient.services.postAE(id,cseClient.getCount());
						if (response_==null) {
							outStream.out2("failed");
							errStream.out("Unable to register node \"" + id + "\" on CSE, timeout expired", i, Severity.LOW);
							response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
							exchange.respond(response);
							i++;
							return;
						} else if (response_.getCode()!=ResponseCode.CREATED) {
							outStream.out2("failed");
							errStream.out("Unable to register node \"" + id + "\" on CSE, response: " + response_.getCode(),
									i, Severity.LOW);
							response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
							exchange.respond(response);
							i++;
							return;
						}
						String json = null;
						try {
							json = Services.parseJSON(response_.getResponseText(), "m2m:ae",
								new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
						} catch (JSONException e) {
							outStream.out2("failed");
							errStream.out(e, i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							i++;
							return;
						}
						debugStream.out("Received JSON: " + json, i);
						try {
							response_ = cseClient.services.postContainer(cseBaseName,id,cseClient.getCount());
						} catch (URISyntaxException e) {
							outStream.out2("failed");
							errStream.out(e,i,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							i++;
							return;
						}
						if (response_==null) {
							outStream.out2("failed");
							errStream.out("Unable to register node \"" + id + "\" on CSE, timeout expired", i, Severity.LOW);
							response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
							exchange.respond(response);
							i++;
							return;
						} else if (response_.getCode()!=ResponseCode.CREATED) {
							outStream.out2("failed");
							errStream.out("Unable to register node \"" + id + "\" on CSE, response: " + response_.getCode(),
									i, Severity.LOW);
							response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
							exchange.respond(response);
							i++;
							return;
						}
						try {
							json = Services.parseJSON(response_.getResponseText(), "m2m:cnt",
								new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
						} catch (JSONException e) {
							outStream.out2("failed");
							errStream.out(e, i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							i++;
							return;
						}
						debugStream.out("Received JSON: " + json, i);
						// TODO: check if the following section is safe to remove
						if (tag.node==Node.SENSOR) {
							String ri = null;
							try {
								ri = Services.parseJSON(response_.getResponseText(), "m2m:cnt",
										new String[] {"ri"}, new Class<?>[] {String.class});							// Example: "/augmented-things-MN-cse/cnt-67185819"
							} catch (JSONException e) {
								outStream.out2("failed");
								errStream.out(e, i, Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								i++;
								return;
							}
							String key = Services.getKeyFromAttribute(ri);												// Example: "67185819"
							subscriber.bind(id,key);
						}
					}
					tagMap.put(serial,tag);
					response = new Response(ResponseCode.CREATED);
				} else {
					// node lookout (id=<ID>&ser=<SERIAL>)
					Tag tag = tagMap.get(serial);
					if (tag==null || tag.node!=Node.SENSOR) {
						debugStream.out("Serial \"" + serial + "\" is not registered on this MN as a sensor", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					User user = userMap.get(id);
					if (user==null) {
						debugStream.out("User \"" + id + "\" is not registered on this MN", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out1("Subscribing user \"" + id + "\" to resource with serial \"" + serial + "\"", i);
					try {
						subscriber.insert(tag.id,tag.type,id,user.address,i);
					} catch (URISyntaxException | StateCreationException e) {
						outStream.out2("failed");
						errStream.out(e,i,Severity.LOW);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					response = new Response(ResponseCode.CREATED);
				}
			} else {
				String address = getUriValue(exchange,"addr",1);
				if (address!=null) {
					// user MN registration (id=<ID>&addr=<URI>)
					if (!isValidAddress(address)) {
						debugStream.out("Bad request, addr=" + address, i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out1("Registering user \"" + id + "\" with address \"" + address + "\"", i);
					boolean createState;
					String[] uri_;
					if (userMap.containsKey(id)) {
						createState = false;
						uri_ = new String[] {cseBaseName, "state", "userMap", id};
					}
					else {
						createState = true;
						uri_ = new String[] {cseBaseName, "state", "userMap"};
					}
					User user = new User(id,address,cseBaseName);
					CoapResponse response_ = null;
					cseClient.stepCount();
					try {
						response_ = cseClient.services.oM2Mput(id,user,uri_,createState,cseClient.getCount());
					} catch (URISyntaxException e) {
						outStream.out2("failed");
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					if (response_==null) {
						outStream.out2("failed");
						errStream.out("Unable to register user \"" + id + "\" on CSE, timeout expired", i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					} else if (response_.getCode()!=ResponseCode.CREATED) {
						outStream.out2("failed");
						errStream.out("Unable to register user \"" + id + "\" on CSE, response: " + response_.getCode(),
								i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					}
					if (createState) {
						cseClient.stepCount();
						try {
							cseClient.connect(Constants.protocol + "localhost" + Constants.mnCSERoot(cseBaseName));
						} catch (URISyntaxException e) {
							outStream.out2("failed");
							errStream.out(e,i,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							i++;
							return;
						}
						response_ = cseClient.services.postAE(id,cseClient.getCount());
						if (response_==null) {
							outStream.out2("failed");
							errStream.out("Unable to register user \"" + id + "\" on CSE, timeout expired", i, Severity.LOW);
							response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
							exchange.respond(response);
							i++;
							return;
						} else if (response_.getCode()!=ResponseCode.CREATED) {
							outStream.out2("failed");
							errStream.out("Unable to register user \"" + id + "\" on CSE, response: " + response_.getCode(),
									i, Severity.LOW);
							response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
							exchange.respond(response);
							i++;
							return;
						}
						String json = null;
						try {
							json = Services.parseJSON(response_.getResponseText(), "m2m:ae",
								new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
						} catch (JSONException e) {
							outStream.out2("failed");
							errStream.out(e, i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							i++;
							return;
						}
						debugStream.out("Received JSON: " + json, i);
					}
					userMap.put(id,user);
					response = new Response(ResponseCode.CREATED);
				} else {
					// Content Instance posting (id=<ID>&con=<CON>)
					String content = getUriValue(exchange,"con",1);
					if (content==null || !isValidContent(content)) {
						if (content!=null)
							debugStream.out("Bad request, con=" + content, i);
						else
							debugStream.out("Bad request, con", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out1("Posting Content Instance \"" + content + "\" on node \"" + id + "\"", i);
					String[] uri = new String[] {cseBaseName, id, "data"};
					CoapResponse response_ = null;
					cseClient.stepCount();
					try {
						response_ = cseClient.services.postContentInstance(content,uri,cseClient.getCount());
					} catch (URISyntaxException e) {
						outStream.out2("failed");
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					if (response_==null) {
						outStream.out2("failed");
						errStream.out("Unable to post Content Instance on node \"" + id + "\", timeout expired", i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					} else if (response_.getCode()!=ResponseCode.CREATED) {
						outStream.out2("failed");
						errStream.out("Unable to post Content Instance on node \"" + id + "\", response: " + response_.getCode(),
								i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					}
					String json = null;
					try {
						json = Services.parseJSON(response_.getResponseText(), "m2m:cin",
								new String[] {"ty","cnf","con"}, new Class<?>[] {Integer.class,String.class,String.class});
					} catch (JSONException e) {
						outStream.out2("failed");
						errStream.out(e, i, Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					debugStream.out("Received JSON: " + json, i);
					response = new Response(ResponseCode.CREATED);
					// Forward notifications towards observers
					ArrayList<Subscription> subs = subscriber.getFromId(id);
					if (subs!=null && subs.size()>0) {
						response_ = null;
						for (int j=0; j<subs.size(); j++) {
							switch (subs.get(j).receiver.node) {
								case SENSOR:
									break;
								case ACTUATOR:
									double value;
									try {
										value = Format.unpack(content,subs.get(j).sender.type);
									} catch (ParseException e) {
										outStream.out2("failed");
										errStream.out(e,i,Severity.MEDIUM);
										response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
										exchange.respond(response);
										i++;
										return;
									}
									subs.get(j).controller.insert(value);
									if (subs.get(j).controller.check()) {
										try {
											response_ = forwardNotification(subs.get(j).receiver.id,subs.get(j).receiver.address,subs.get(j).action);
										} catch (URISyntaxException e) {
											outStream.out2("failed");
											errStream.out(e,i,Severity.MEDIUM);
											response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
											exchange.respond(response);
											i++;
											return;
										}
									}
									break;
								case USER:
									try {
										response_ = forwardNotification(subs.get(j).receiver.id,subs.get(j).receiver.address,id+": con="+content);
									} catch (URISyntaxException e) {
										outStream.out2("failed");
										errStream.out(e,i,Severity.MEDIUM);
										response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
										exchange.respond(response);
										i++;
										return;
									}
									break;
							}
							if (response_==null) {
								outStream.out2("failed");
								errStream.out("Unable to send data to \"" + id + "\", timeout expired", i, Severity.LOW);
								response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
								exchange.respond(response);
								i++;
								return;
							} else if (response_.getCode()!=ResponseCode.CHANGED) {
								outStream.out2("failed");
								errStream.out("Unable to send data to \"" + id + "\", response: " + response_.getCode(),
										i, Severity.LOW);
								response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
								exchange.respond(response);
								i++;
								return;
							}
						}
					}
				}
			}
		} else {
			String serial0 = getUriValue(exchange,"ser",0);
			String serial1 = getUriValue(exchange,"ser",1);
			String label0 = getUriValue(exchange,"lab",2);
			String label1 = getUriValue(exchange,"lab",3);
			String notificationId = getUriValue(exchange,"id",4);
			if (serial0!=null && serial1!=null && label0!=null && label1!=null && notificationId!=null) {
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
				Tag tag0 = tagMap.get(serial0);
				Tag tag1 = tagMap.get(serial1);
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
				User user = userMap.get(notificationId);
				if (user==null) {
					debugStream.out("User \"" + notificationId + "\" is not registered on this MN", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				outStream.out1("Linking sensor with serial \"" + serial0 + "\" to actuator with serial \"" + serial1 + "\"", i);
				try {
					subscriber.insert(tag0.id,tag0.type,label0,tag0.ruleMap.get(label0),tag1.id,tag1.address,label1,i);
				} catch (URISyntaxException | StateCreationException | InvalidRuleException e) {
					outStream.out2("failed");
					errStream.out(e,i,Severity.LOW);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
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
		Tag tag = tagMap.get(serial);
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
			errStream.out(e,i,Severity.MEDIUM);
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
		if (response_==null) {
			outStream.out2("failed");
			errStream.out("Unable to write on actuator \"" + tag.id + "\", timeout expired", i, Severity.LOW);
			response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
			exchange.respond(response);
			i++;
			return;
		} else if (response_.getCode()!=ResponseCode.CHANGED) {
			outStream.out2("failed");
			errStream.out("Unable to write on actuator \"" + tag.id + "\", response: " + response_.getCode(),
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
			User user = userMap.get(id);
			if (user==null) {
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
				Tag tag = tagMap.get(serial);
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
				} catch (URISyntaxException | StateCreationException e) {
					outStream.out2("failed");
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					i++;
					return;
				}
				response = new Response(ResponseCode.DELETED);
				response.setPayload("OK");
			} else {
				// user removal (id=<ID>)
				outStream.out1("Handling removal of user \"" + id + "\"", i);
				try {
					subscriber.remove(id,Node.USER,i);
				} catch (URISyntaxException | StateCreationException e) {
					outStream.out2("failed");
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					i++;
					return;
				}
				user = userMap.get(id);
				user.active = false;
				String[] uri_ = new String[] {cseBaseName, "state", "userMap", id};
				CoapResponse response_ = null;
				cseClient.stepCount();
				try {
					response_ = cseClient.services.oM2Mput(id,user,uri_,false,cseClient.getCount());
				} catch (URISyntaxException e) {
					outStream.out2("failed");
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					i++;
					return;
				}
				if (response_==null) {
					outStream.out2("failed");
					errStream.out("Unable to remove user from CSE, timeout expired", i, Severity.LOW);
					response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
					exchange.respond(response);
					i++;
					return;
				} else if (response_.getCode()!=ResponseCode.CREATED) {
					outStream.out2("failed");
					errStream.out("Unable to remove user from CSE, response: " + response_.getCode(),
							i, Severity.LOW);
					response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
					exchange.respond(response);
					i++;
					return;
				}
				response = new Response(ResponseCode.DELETED);
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
				Tag tag0 = tagMap.get(serial0);
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
					Tag tag1 = tagMap.get(serial1);
					if (tag1==null || tag1.node!=Node.ACTUATOR) {
						debugStream.out("Serial \"" + serial1 + "\" is not registered on this MN as an actuator", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					String label0 = getUriValue(exchange,"lab",2);
					String label1 = getUriValue(exchange,"lab",3);
					if (label0==null || !isValidLabel(label0,tag0)) {
						if (label0!=null)
							debugStream.out("Bad request, lab=" + label0, i);
						else
							debugStream.out("Bad request, lab", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					if (label1==null || !isValidLabel(label1,tag1)) {
						if (label1!=null)
							debugStream.out("Bad request, lab=" + label1, i);
						else
							debugStream.out("Bad request, lab", i);
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						i++;
						return;
					}
					outStream.out1("Handling removal of link between serial \"" + serial0 + "\" and serial \"" + serial1 + "\"", i);
					try {
						subscriber.remove(tag0.id,label0,tag1.id,label1,i);
					} catch (URISyntaxException | StateCreationException e) {
						outStream.out2("failed");
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					response = new Response(ResponseCode.DELETED);
					response.setPayload("OK");
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
					CoapResponse response_ = null;
					switch (tag0.node) {
						case SENSOR:
							try {
								subscriber.remove(tag0.id,Node.SENSOR,i);
							} catch (URISyntaxException | StateCreationException e) {
								outStream.out2("failed");
								errStream.out(e,i,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								i++;
								return;
							}
							break;
						case ACTUATOR:
							try {
								subscriber.remove(tag0.id,Node.ACTUATOR,i);
							} catch (URISyntaxException | StateCreationException e) {
								outStream.out2("failed");
								errStream.out(e,i,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								i++;
								return;
							}
							break;
						case USER:
							break;
					}
					tag0.active = false;
					String[] uri_ = new String[] {cseBaseName, "state", "tagMap", serial0};
					cseClient.stepCount();
					try {
						response_ = cseClient.services.oM2Mput(serial0,tag0,uri_,false,cseClient.getCount());
					} catch (URISyntaxException e) {
						outStream.out2("failed");
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						i++;
						return;
					}
					if (response_==null) {
						outStream.out2("failed");
						errStream.out("Unable to remove node from CSE, timeout expired", i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					} else if (response_.getCode()!=ResponseCode.CREATED) {
						outStream.out2("failed");
						errStream.out("Unable to remove node from CSE, response: " + response_.getCode(),
								i, Severity.LOW);
						response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
						exchange.respond(response);
						i++;
						return;
					}
					response = new Response(ResponseCode.DELETED);
				}
			} else {
				debugStream.out("Bad request, ser", i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
		}
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

}

class User implements JSONSerializable {
	
	String address;
	
	private String id;
	private String cseBaseName;
	
	boolean active;
	
	User(String id, String address, String cseBaseName) {
		this.id = id;
		this.address = address;
		this.active = true;
		this.cseBaseName = cseBaseName;
	}
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("id",id);
		obj.put("address",address);
		obj.put("mn",cseBaseName);
		obj.put("active",active);
		return obj;
	}
	
}
