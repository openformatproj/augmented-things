package mecs.iot.proj.om2m.adn.mn;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.adn.ADN;
import mecs.iot.proj.om2m.adn.mn.exceptions.*;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.Severity;
import mecs.iot.proj.om2m.exceptions.InvalidRuleException;
import mecs.iot.proj.om2m.exceptions.NoTypeException;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.Tag;

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

class ADN_MN extends ADN {

	Client notificationClient;

	private HashMap<String,Tag> tagMap;																					// serial -> node tag
	private HashMap<String,Tag> userMap;																				// user id -> user tag

	private Subscriber subscriber;
	
	private PeriodicityTracker tracker;

	ADN_MN(String id, String host, boolean debug, Console console) throws URISyntaxException, StateCreationException, RegistrationException {
		super(id,host,debug,console);
		cseClient = new Client(Format.joinIdHost(id+"/CSEclient",host), Constants.protocol + "localhost" + Constants.mnCSERoot(id), debug);
		notificationClient = new Client(Format.joinIdHost(id+"/ATclient",host),debug);
		tagMap = new HashMap<String,Tag>();
		userMap = new HashMap<String,Tag>();
		if (false) {
			// TODO: pull from OM2M
		} else {
			String json = null;
			outStream.out1("Creating OM2M state",i);
			outStream.out1_2("posting root AE");
			cseClient.stepCount();
			CoapResponse response_ = cseClient.services.postAE("state",cseClient.getCount());
			if (response_==null) {
				errStream.out("Unable to post AE to \"" + cseClient.services.uri() + "\", timeout expired", i, Severity.HIGH);
				outStream.out2("failed");
				throw new StateCreationException();
			} else if (response_.getCode()!=ResponseCode.CREATED/* && response_.getCode()!=ResponseCode.FORBIDDEN*/) {
				if (!response_.getResponseText().isEmpty())
					errStream.out("Unable to post AE to \"" + cseClient.services.uri() + "\", response: " + response_.getCode() +
							", reason: " + response_.getResponseText(),
							i, Severity.HIGH);
				else
					errStream.out("Unable to post AE to \"" + cseClient.services.uri() + "\", response: " + response_.getCode(),
						i, Severity.HIGH);
				outStream.out2("failed");
				throw new StateCreationException();
			}
			try {
				json = Services.parseJSON(response_.getResponseText(), "m2m:ae",
						new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
			} catch (JSONException e) {
				errStream.out(e,i,Severity.MEDIUM);
				outStream.out2("failed");
				throw e;
			}
			debugStream.out("Received JSON: " + json, i);
			outStream.out1_2("done, posting tagMap");
			cseClient.stepCount();
			response_ = cseClient.services.postContainer(cseBaseName,"state","tagMap",cseClient.getCount());
			if (response_==null) {
				errStream.out("Unable to post Container to \"" + cseClient.services.uri() + "\", timeout expired", i, Severity.HIGH);
				outStream.out2("failed");
				throw new StateCreationException();
			} else if (response_.getCode()!=ResponseCode.CREATED/* && response_.getCode()!=ResponseCode.FORBIDDEN */) {
				if (!response_.getResponseText().isEmpty())
					errStream.out("Unable to post Container to \"" + cseClient.services.uri() + "\", response: " + response_.getCode() +
							", reason: " + response_.getResponseText(),
							i, Severity.HIGH);
				else
					errStream.out("Unable to post Container to \"" + cseClient.services.uri() + "\", response: " + response_.getCode(),
						i, Severity.HIGH);
				outStream.out2("failed");
				throw new StateCreationException();
			}
			try {
				json = Services.parseJSON(response_.getResponseText(), "m2m:cnt",
						new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
			} catch (JSONException e) {
				errStream.out(e,i,Severity.MEDIUM);
				outStream.out2("failed");
				throw e;
			}
			debugStream.out("Received JSON: " + json, i);
			outStream.out1_2("done, posting userMap");
			cseClient.stepCount();
			response_ = cseClient.services.postContainer(cseBaseName,"state","userMap",cseClient.getCount());
			if (response_==null) {
				errStream.out("Unable to post Container to \"" + cseClient.services.uri() + "\", timeout expired", i, Severity.HIGH);
				outStream.out2("failed");
				throw new StateCreationException();
			} else if (response_.getCode()!=ResponseCode.CREATED/* && response_.getCode()!=ResponseCode.FORBIDDEN */) {
				if (!response_.getResponseText().isEmpty())
					errStream.out("Unable to post Container to \"" + cseClient.services.uri() + "\", response: " + response_.getCode() +
							", reason: " + response_.getResponseText(),
							i, Severity.HIGH);
				else
					errStream.out("Unable to post Container to \"" + cseClient.services.uri() + "\", response: " + response_.getCode(),
						i, Severity.HIGH);
				outStream.out2("failed");
				throw new StateCreationException();
			}
			try {
				json = Services.parseJSON(response_.getResponseText(), "m2m:cnt",
						new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
			} catch (JSONException e) {
				errStream.out(e,i,Severity.MEDIUM);
				outStream.out2("failed");
				throw e;
			}
			debugStream.out("Received JSON: " + json, i);
			outStream.out1_2("done, posting subscription state");
			subscriber = new Subscriber(outStream,debugStream,errStream,cseClient,cseBaseName,i);
		}
		outStream.out1("Registering to IN",i);
		register(id,host,debug);
		outStream.out2("done");
		tracker = new PeriodicityTracker(Format.joinIdHost(cseBaseName+"/tracker",host),cseClient,subscriber,cseBaseName);
		tracker.start();
		i++;
	}

	@Override
	synchronized public void handleGET(CoapExchange exchange) {
		outStream.out1("Received GET request", i);
		Response response = null;
		String mode = getUriValue(exchange,"mode",0);
		if (mode!=null) {
			int sw;
			try {
				sw = Integer.parseInt(mode);
			} catch (NumberFormatException e) {
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				outStream.out2("bad request, \"mode=" + mode + "\"");
				i++;
				return;
			}
			String serial = getUriValue(exchange,"ser",1);
			if (serial==null || !isValidSerial(serial)) {
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				if (serial!=null)
					outStream.out2("bad request, \"ser=" + serial + "\"");
				else
					outStream.out2("bad request, \"ser\" is empty");
				i++;
				return;
			}
			Tag tag = tagMap.get(serial);
			switch (sw) {
				case 1:
					// attributes query (mode=1&ser=<SERIAL>)
					outStream.out2("detected attributes query");
					if (tag==null) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out("Serial \"" + serial + "\" is not registered on this MN as an endpoint node", i);
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
					outStream.out2("detected node read");
					if (tag==null || tag.node!=Node.SENSOR) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out("Serial \"" + serial + "\" is not registered on this MN as a sensor", i);
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
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					if (response_==null) {
						errStream.out("Unable to read from \"" + cseClient.services.uri() + "\", timeout expired", i, Severity.LOW);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					} else if (response_.getCode()!=ResponseCode.CONTENT) {
						errStream.out("Unable to read from \"" + cseClient.services.uri() + "\", response: " + response_.getCode(),
								i, Severity.LOW);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					String con = null;
					try {
						con = Services.parseJSON(response_.getResponseText(), "m2m:cin",
								new String[] {"con"}, new Class<?>[] {String.class});
					} catch (JSONException e) {
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					response = new Response(ResponseCode.CONTENT);
					response.setPayload(id + ": " + con);
					break;
				default:
					outStream.out2("bad request, \"mode\" is not valid");
					outStream.out1("Handling incorrect request", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("done");
					i++;
					return;
			}
		} else if (exchange.getRequestOptions().getUriQuery().size()==0) {
			// mn name query ()
			outStream.out2("detected mn query");
			outStream.out1("Handling MN name request", i);
			response = new Response(ResponseCode.CONTENT);
			response.setPayload("MN: " + cseBaseName);
		} else {
			String serial = getUriValue(exchange,"ser",0);
			if (serial==null || !isValidSerial(serial)) {
				if (serial!=null)
					outStream.out2("bad request, \"ser=" + serial + "\"");
				else
					outStream.out2("bad request, \"ser\" is empty");
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				i++;
				return;
			}
			Tag tag = tagMap.get(serial);
			// node name query (ser=<SERIAL>)
			outStream.out2("detected node name query");
			if (tag==null) {
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				outStream.out("Serial \"" + serial + "\" is not registered on this MN", i);
				i++;
				return;
			}
			outStream.out1("Handling node name request for serial \"" + serial + "\"", i);
			response = new Response(ResponseCode.CONTENT);
			response.setPayload(tag.node + ": " + tag.id);
		}
		exchange.respond(response);
		outStream.out2("done");
		i++;
	}

	@Override
	synchronized public void handlePOST(CoapExchange exchange) {
		outStream.out1("Received POST request", i);
		Response response = null;
		String id = getUriValue(exchange,"id",0);
		if (id!=null) {
			if (!isValidId(id)) {
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				outStream.out2("bad request, \"id=" + id + "\"");
				i++;
				return;
			}
			String serial = getUriValue(exchange,"ser",1);
			if (serial!=null) {
				if (!isValidSerial(serial)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("bad request, \"ser=" + serial + "\"");
					i++;
					return;
				}
				String type = getUriValue(exchange,"type",2);
				if (type!=null) {
					if (!isValidType(type)) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out2("bad request, \"type=" + type + "\"");
						i++;
						return;
					}
					String payload = exchange.getRequestText();
					String[] attributes = payload.split(", ");
					Integer k = new Integer(0);
					if (!areValidAttributes(attributes,k)) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out2("bad request, attribute=\"" + attributes[k] + "\"");
						i++;
						return;
					}
					Tag tag = null;
					if (type.equals("act")) {
						String address = getUriValue(exchange,"addr",3);
						if (address==null || !isValidAddress(address)) {
							response = new Response(ResponseCode.BAD_REQUEST);
							exchange.respond(response);
							if (address!=null)
								outStream.out2("bad request, \"addr=" + address + "\"");
							else
								outStream.out2("bad request, \"addr\" is empty");
							i++;
							return;
						}
						tag = new Tag(Node.ACTUATOR,id,serial,address,attributes,cseBaseName);
					} else {
						tag = new Tag(Node.SENSOR,id,serial,type,attributes,cseBaseName);
					}
					// node MN registration (id=<ID>&ser=<SERIAL>&type=<TYPE>{&addr=<URI>}, PAYLOAD [<ATTRIBUTE>])
					outStream.out2("detected node MN registration");
					outStream.out1("Registering node \"" + id + "\" with serial \"" + serial + "\"", i);
					boolean createState;
					String[] uri_;
					if (tagMap.containsKey(serial)) {
						createState = false;
						uri_ = new String[] {cseBaseName, "state", "tagMap", serial};
					} else {
						createState = true;
						uri_ = new String[] {cseBaseName, "state", "tagMap"};
					}
					CoapResponse response_ = null;
					cseClient.stepCount();
					try {
						response_ = cseClient.services.oM2Mput(serial,tag,uri_,createState,cseClient.getCount());
					} catch (URISyntaxException e) {
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					if (response_==null) {
						errStream.out("Unable to register node \"" + id + "\" on CSE, timeout expired", i, Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					} else if (response_.getCode()!=ResponseCode.CREATED) {
						errStream.out("Unable to register node \"" + id + "\" on CSE, response: " + response_.getCode(),
								i, Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					if (createState) {
						cseClient.stepCount();
						try {
							cseClient.connect(Constants.protocol + "localhost" + Constants.mnCSERoot(cseBaseName));
						} catch (URISyntaxException e) {
							errStream.out(e,i,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						response_ = cseClient.services.postAE(id,cseClient.getCount());
						if (response_==null) {
							errStream.out("Unable to register node \"" + id + "\" on CSE, timeout expired", i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						} else if (response_.getCode()!=ResponseCode.CREATED) {
							errStream.out("Unable to register node \"" + id + "\" on CSE, response: " + response_.getCode(),
									i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						String json = null;
						try {
							json = Services.parseJSON(response_.getResponseText(), "m2m:ae",
								new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
						} catch (JSONException e) {
							errStream.out(e,i,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						debugStream.out("Received JSON: " + json, i);
						try {
							response_ = cseClient.services.postContainer(cseBaseName,id,cseClient.getCount());
						} catch (URISyntaxException e) {
							errStream.out(e,i,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						if (response_==null) {
							errStream.out("Unable to register node \"" + id + "\" on CSE, timeout expired", i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						} else if (response_.getCode()!=ResponseCode.CREATED) {
							errStream.out("Unable to register node \"" + id + "\" on CSE, response: " + response_.getCode(),
									i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						try {
							json = Services.parseJSON(response_.getResponseText(), "m2m:cnt",
								new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
						} catch (JSONException e) {
							errStream.out(e,i,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						debugStream.out("Received JSON: " + json, i);
					}
					tagMap.put(serial,tag);
					if (tag.node==Node.SENSOR)
						tracker.insert(id,tag,serial);
					response = new Response(ResponseCode.CREATED);
				} else {
					String address = getUriValue(exchange,"addr",2);
					if (address!=null) {
						if (!isValidEUI64Address(address)) {
							response = new Response(ResponseCode.BAD_REQUEST);
							exchange.respond(response);
							outStream.out2("bad request, \"addr=" + address + "\"");
							i++;
							return;
						}
						// actuator MN registration (id=<ID>&ser=<SERIAL>&addr=<EUI-64>), shortcut for highly constrained devices on an IPv6 network
						outStream.out2("actuator MN registration");
						Tag tag = new Tag(Node.ACTUATOR,id,serial,resolveEUI64Address(address),new String[] {"on","off"},cseBaseName);
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
							errStream.out(e,i,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						if (response_==null) {
							errStream.out("Unable to register node \"" + id + "\" on CSE, timeout expired", i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						} else if (response_.getCode()!=ResponseCode.CREATED) {
							errStream.out("Unable to register node \"" + id + "\" on CSE, response: " + response_.getCode(),
									i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						if (createState) {
							cseClient.stepCount();
							try {
								cseClient.connect(Constants.protocol + "localhost" + Constants.mnCSERoot(cseBaseName));
							} catch (URISyntaxException e) {
								errStream.out(e,i,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							}
							response_ = cseClient.services.postAE(id,cseClient.getCount());
							if (response_==null) {
								errStream.out("Unable to register node \"" + id + "\" on CSE, timeout expired", i, Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							} else if (response_.getCode()!=ResponseCode.CREATED) {
								errStream.out("Unable to register node \"" + id + "\" on CSE, response: " + response_.getCode(),
										i, Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							}
							String json = null;
							try {
								json = Services.parseJSON(response_.getResponseText(), "m2m:ae",
									new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
							} catch (JSONException e) {
								errStream.out(e,i,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							}
							debugStream.out("Received JSON: " + json, i);
							try {
								response_ = cseClient.services.postContainer(cseBaseName,id,cseClient.getCount());
							} catch (URISyntaxException e) {
								errStream.out(e,i,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							}
							if (response_==null) {
								errStream.out("Unable to register node \"" + id + "\" on CSE, timeout expired", i, Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							} else if (response_.getCode()!=ResponseCode.CREATED) {
								errStream.out("Unable to register node \"" + id + "\" on CSE, response: " + response_.getCode(),
										i, Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							}
							try {
								json = Services.parseJSON(response_.getResponseText(), "m2m:cnt",
									new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
							} catch (JSONException e) {
								errStream.out(e,i,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							}
							debugStream.out("Received JSON: " + json, i);
						}
						tagMap.put(serial,tag);
						response = new Response(ResponseCode.CREATED);
					} else {
						String content = getUriValue(exchange,"con",2);
						if (content!=null) {
							if (!isValidContent(content)) {
								response = new Response(ResponseCode.BAD_REQUEST);
								exchange.respond(response);
								outStream.out2("bad request, \"con=" + content + "\"");
								i++;
								return;
							}
							Tag tag = tagMap.get(serial);
							// content instance posting (id=<ID>&ser=<SERIAL>&con=<CON>)
							outStream.out2("detected content instance posting");
							if (tag==null || tag.node!=Node.SENSOR) {
								response = new Response(ResponseCode.BAD_REQUEST);
								exchange.respond(response);
								outStream.out("Serial \"" + serial + "\" is not registered on this MN as a sensor", i);
								i++;
								return;
							}
							if (!isRecognizedContent(content,tag)) {
								response = new Response(ResponseCode.BAD_REQUEST);
								exchange.respond(response);
								outStream.out("Content \"" + content + "\" is not a valid content for \"" + serial + "\"", i);
								i++;
								return;
							}
							tracker.track(tag.id);
							if (content=="\\*") {
								try {
									content = Format.getRandomValue(tag.type);
								} catch (NoTypeException e) {
									response = new Response(ResponseCode.BAD_REQUEST);
									exchange.respond(response);
									outStream.out("Sensor \"" + tag.id + "\" hasn't a valid type", i);
									i++;
									return;
								}
							}
							outStream.out1("Posting Content Instance \"" + content + "\" on node \"" + id + "\"", i);
							String[] uri = new String[] {cseBaseName, id, "data"};
							CoapResponse response_ = null;
							cseClient.stepCount();
							try {
								response_ = cseClient.services.postContentInstance(content,uri,cseClient.getCount());
							} catch (URISyntaxException e) {
								errStream.out(e,i,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							}
							if (response_==null) {
								errStream.out("Unable to post Content Instance on node \"" + id + "\", timeout expired", i, Severity.LOW);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							} else if (response_.getCode()!=ResponseCode.CREATED) {
								errStream.out("Unable to post Content Instance on node \"" + id + "\", response: " + response_.getCode(),
										i, Severity.LOW);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							}
							String json = null;
							try {
								json = Services.parseJSON(response_.getResponseText(), "m2m:cin",
										new String[] {"ty","cnf","con"}, new Class<?>[] {Integer.class,String.class,String.class});
							} catch (JSONException e) {
								errStream.out(e,i,Severity.MEDIUM);
								response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							}
							debugStream.out("Received JSON: " + json, i);
							response = new Response(ResponseCode.CREATED);
							// forward notifications towards observers
							outStream.out1_2("forwarding notification to observers");
							ArrayList<Subscription> subs = subscriber.get(id);
							if (subs!=null && subs.size()>0) {
								response_ = null;
								boolean hasBeenForwarded;
								boolean exceptionOccurred;
								Tag receiver;
								String key = null;
								for (int j=0; j<subs.size(); j++) {
									hasBeenForwarded = false;
									exceptionOccurred = false;
									receiver = subs.get(j).receiver;
									switch (receiver.node) {
										case SENSOR:
											errStream.out("Unexpected error", i, Severity.MEDIUM);
											response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
											exchange.respond(response);
											outStream.out2("failed");
											i++;
											return;
										case ACTUATOR:
											Object value;
											try {
												value = Format.unpack(content,subs.get(j).sender.type);
											} catch (ParseException e) {
												errStream.out("Unable to send data to \"" + receiver.id + "\", reason: " + e.getMessage(), i, Severity.MEDIUM);
												exceptionOccurred = true;
												break;
											} catch (NoTypeException e) {
												errStream.out("Unable to send data to \"" + receiver.id + "\", reason: " + e.getMessage(), i, Severity.LOW);
												exceptionOccurred = true;
												break;
											}
											Checker checker = subs.get(j).checker;
											checker.insert(value);
											if (checker==null || checker.check()) {
												try {
													response_ = forwardNotification(receiver.id,receiver.address,subs.get(j).action,i);
												} catch (URISyntaxException e) {
													errStream.out(e,i,Severity.MEDIUM);
													break;
												}
												hasBeenForwarded = true;
												key = receiver.serial;
												debugStream.out("Forwarded notification to \"" + receiver.id + "\"", i);
											}
											break;
										case USER:
											try {
												response_ = forwardNotification(receiver.id,receiver.address,id+": con="+content,i);
											} catch (URISyntaxException e) {
												errStream.out(e,i,Severity.MEDIUM);
												break;
											}
											hasBeenForwarded = true;
											key = receiver.id;
											debugStream.out("Forwarded notification to \"" + receiver.id + "\"", i);
											break;
									}
									if (exceptionOccurred) {
										;
									} else if (hasBeenForwarded && response_==null) {
										errStream.out("Unable to send data to \"" + receiver.id + "\", timeout expired", i, Severity.LOW);
										// remove listener if it doesn't answer
										receiver.active = false;
										try {
											subscriber.remove(receiver.id,receiver.node,i);
											String[] uri_ = new String[] {cseBaseName, "state", "tagMap", key};
											cseClient.stepCount();
												response_ = cseClient.services.oM2Mput(receiver,uri_,cseClient.getCount());
											if (response_==null) {
												errStream.out("Unable to remove node from CSE, timeout expired", i, Severity.HIGH);
											} else if (response_.getCode()!=ResponseCode.CREATED) {
												errStream.out("Unable to remove node from CSE, response: " + response_.getCode(),
														i, Severity.HIGH);
											}
										} catch (URISyntaxException | StateCreationException e) {
											errStream.out("Unable to remove node from CSE, reason: " + e.getMessage(), i, Severity.HIGH);
										}
									} else if (response_!=null && response_.getCode()!=ResponseCode.CHANGED) {
										errStream.out("Unable to send data to \"" + receiver.id + "\", response: " + response_.getCode(), i, Severity.MEDIUM);
									}
								}
							}
						} else {
							Tag tag = tagMap.get(serial);
							// node lookout (id=<ID>&ser=<SERIAL>)
							outStream.out2("detected node lookout");
							if (tag==null || tag.node!=Node.SENSOR) {
								response = new Response(ResponseCode.BAD_REQUEST);
								exchange.respond(response);
								outStream.out("Serial \"" + serial + "\" is not registered on this MN as a sensor", i);
								i++;
								return;
							}
							Tag user  = userMap.get(id);
							if (user==null) {
								response = new Response(ResponseCode.BAD_REQUEST);
								exchange.respond(response);
								outStream.out("User \"" + id + "\" is not registered on this MN", i);
								i++;
								return;
							}
							outStream.out1("Subscribing user \"" + id + "\" to resource with serial \"" + serial + "\"", i);
							try {
								subscriber.insert(tag,user,i);
							} catch (URISyntaxException | StateCreationException e) {
								errStream.out(e,i,Severity.MEDIUM);
								response = new Response(ResponseCode.BAD_REQUEST);
								exchange.respond(response);
								outStream.out2("failed");
								i++;
								return;
							}
							response = new Response(ResponseCode.CREATED);
						}
					}
				}
			} else {
				String address = getUriValue(exchange,"addr",1);
				if (address!=null) {
					if (!isValidAddress(address)) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out2("bad request, \"addr=" + address + "\"");
						i++;
						return;
					}
					// user MN registration (id=<ID>&addr=<URI>)
					outStream.out2("user MN registration");
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
					Tag user = new Tag(id,address,cseBaseName);
					CoapResponse response_ = null;
					cseClient.stepCount();
					try {
						response_ = cseClient.services.oM2Mput(id,user,uri_,createState,cseClient.getCount());
					} catch (URISyntaxException e) {
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					if (response_==null) {
						errStream.out("Unable to register user \"" + id + "\" on CSE, timeout expired", i, Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					} else if (response_.getCode()!=ResponseCode.CREATED) {
						errStream.out("Unable to register user \"" + id + "\" on CSE, response: " + response_.getCode(),
								i, Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					if (createState) {
						cseClient.stepCount();
						try {
							cseClient.connect(Constants.protocol + "localhost" + Constants.mnCSERoot(cseBaseName));
						} catch (URISyntaxException e) {
							errStream.out(e,i,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						response_ = cseClient.services.postAE(id,cseClient.getCount());
						if (response_==null) {
							errStream.out("Unable to register user \"" + id + "\" on CSE, timeout expired", i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						} else if (response_.getCode()!=ResponseCode.CREATED) {
							errStream.out("Unable to register user \"" + id + "\" on CSE, response: " + response_.getCode(),
									i, Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						String json = null;
						try {
							json = Services.parseJSON(response_.getResponseText(), "m2m:ae",
								new String[] {"rn","ty"}, new Class<?>[] {String.class,Integer.class});
						} catch (JSONException e) {
							errStream.out(e,i,Severity.MEDIUM);
							response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
							exchange.respond(response);
							outStream.out2("failed");
							i++;
							return;
						}
						debugStream.out("Received JSON: " + json, i);
					}
					userMap.put(id,user);
					response = new Response(ResponseCode.CREATED);
				}
			}
		} else {
			String serial0 = getUriValue(exchange,"ser",0);
			String serial1 = getUriValue(exchange,"ser",1);
			String label0 = getUriValue(exchange,"lab",2);
			String label1 = getUriValue(exchange,"lab",3);
			String notificationId = getUriValue(exchange,"id",4);
			if (serial0!=null && serial1!=null && label0!=null && label1!=null && notificationId!=null) {
				if (!isValidSerial(serial0) && isValidSerial(serial1)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("bad request, \"ser=" + serial0 + "\"");
					i++;
					return;
				}
				if (isValidSerial(serial0) && !isValidSerial(serial1)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("bad request, \"ser=" + serial1 + "\"");
					i++;
					return;
				}
				if (!isValidSerial(serial0) && !isValidSerial(serial1)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("bad request, \"ser=" + serial0 + "&ser=" + serial1 + "\"");
					i++;
					return;
				}
				if (!isValidLabel(label0)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("bad request, \"lab=" + label0 + "\"");
					i++;
					return;
				}
				if (!isValidLabel(label1)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("bad request, \"lab=" + label1 + "\"");
					i++;
					return;
				}
				Tag tag0 = tagMap.get(serial0);
				Tag tag1 = tagMap.get(serial1);
				// nodes link (ser=<SERIAL>&ser=<SERIAL>&lab=<EVENT_LABEL>&lab=<ACTION_LABEL>&id=<ID>)
				outStream.out2("detected nodes link");
				if (tag0==null || tag0.node!=Node.SENSOR) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("Serial \"" + serial0 + "\" is not registered on this MN as a sensor", i);
					i++;
					return;
				}
				if (tag1==null || tag1.node!=Node.ACTUATOR) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("Serial \"" + serial1 + "\" is not registered on this MN as an actuator", i);
					i++;
					return;
				}
				if (!isRecognizedLabel(label0,tag0)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("Label \"" + label0 + "\" is not a valid label for \"" + serial0 + "\"", i);
					i++;
					return;
				}
				if (!isRecognizedLabel(label1,tag1)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("Label \"" + label1 + "\" is not a valid label for \"" + serial1 + "\"", i);
					i++;
					return;
				}
				Tag user = userMap.get(notificationId);
				if (user==null) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("User \"" + notificationId + "\" is not registered on this MN", i);
					i++;
					return;
				}
				outStream.out1("Linking sensor with serial \"" + serial0 + "\" to actuator with serial \"" + serial1 + "\"", i);
				try {
					subscriber.insert(tag0,label0,tag0.ruleMap.get(label0),tag1,label1,i);
				} catch (URISyntaxException | StateCreationException e) {
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				} catch (InvalidRuleException | NoTypeException e) {
					errStream.out(e,i,Severity.LOW);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("failed");
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
		outStream.out1("Received PUT request", i);
		Response response = null;
		String serial = getUriValue(exchange,"ser",0);
		if (serial==null || !isValidSerial(serial)) {
			response = new Response(ResponseCode.BAD_REQUEST);
			exchange.respond(response);
			if (serial!=null)
				outStream.out2("bad request, \"ser=" + serial + "\"");
			else
				outStream.out2("bad request, \"ser\" is empty");
			i++;
			return;
		}
		String label = getUriValue(exchange,"lab",1);
		if (label==null || !isValidLabel(label)) {
			response = new Response(ResponseCode.BAD_REQUEST);
			exchange.respond(response);
			if (label!=null)
				outStream.out2("bad request, \"lab=" + label + "\"");
			else
				outStream.out2("bad request, \"lab\" is empty");
			i++;
			return;
		}
		Tag tag = tagMap.get(serial);
		// node write (ser=<SERIAL>&lab=<ACTION_LABEL>)
		outStream.out2("detected node write");
		if (tag==null || tag.node!=Node.ACTUATOR) {
			response = new Response(ResponseCode.BAD_REQUEST);
			exchange.respond(response);
			outStream.out("Serial \"" + serial + "\" is not registered on this MN as an actuator", i);
			i++;
			return;
		}
		if (!isRecognizedLabel(label,tag)) {
			response = new Response(ResponseCode.BAD_REQUEST);
			exchange.respond(response);
			outStream.out("Label \"" + label + "\" is not a valid label for \"" + serial + "\"", i);
			i++;
			return;
		}
		outStream.out1("Handling writing on actuator with serial \"" + serial + "\"", i);
		notificationClient.stepCount();
		try {
			notificationClient.connect(tag.address,false);
		} catch (URISyntaxException e) {
			errStream.out(e,i,Severity.MEDIUM);
			response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
			exchange.respond(response);
			outStream.out2("failed");
			i++;
			return;
		}
		CoapResponse response_ = write(label);
		if (response_==null) {
			errStream.out("Unable to write on actuator \"" + tag.id + "\", timeout expired", i, Severity.LOW);
			// remove listener if it doesn't answer
			tag.active = false;
			try {
				subscriber.remove(tag.id,Node.ACTUATOR,i);
				String[] uri_ = new String[] {cseBaseName, "state", "tagMap", serial};
				cseClient.stepCount();
					response_ = cseClient.services.oM2Mput(tag,uri_,cseClient.getCount());
				if (response_==null) {
					errStream.out("Unable to remove node from CSE, timeout expired", i, Severity.HIGH);
				} else if (response_.getCode()!=ResponseCode.CREATED) {
					errStream.out("Unable to remove node from CSE, response: " + response_.getCode(),
							i, Severity.HIGH);
				}
			} catch (URISyntaxException | StateCreationException e) {
				errStream.out("Unable to remove node from CSE, reason: " + e.getMessage(), i, Severity.HIGH);
			}
			response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
			exchange.respond(response);
			outStream.out2("failed");
			i++;
			return;
		} else if (response_.getCode()!=ResponseCode.CHANGED) {
			errStream.out("Unable to write on actuator \"" + tag.id + "\", response: " + response_.getCode(),
					i, Severity.LOW);
			response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
			exchange.respond(response);
			outStream.out2("failed");
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
		outStream.out1("Received DELETE request", i);
		Response response = null;
		String id = getUriValue(exchange,"id",0);
		if (id!=null) {
			if (!isValidId(id)) {
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				outStream.out2("bad request, \"id=" + id + "\"");
				i++;
				return;
			}
			String serial = getUriValue(exchange,"ser",1);
			if (serial!=null) {
				if (!isValidSerial(serial)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("bad request, \"ser=" + serial + "\"");
					i++;
					return;
				}
				Tag tag = tagMap.get(serial);
				Tag user = userMap.get(id);
				// lookout removal (id=<ID>&ser=<SERIAL>)
				outStream.out2("detected lookout removal");
				if (tag==null || tag.node!=Node.SENSOR) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("Serial \"" + serial + "\" is not registered on this MN as a sensor", i);
					i++;
					return;
				}
				if (user==null) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("User \"" + id + "\" is not registered on this MN", i);
					i++;
					return;
				}
				outStream.out1("Handling removal of lookout between user \"" + id + "\" and serial \"" + serial + "\"", i);
				try {
					subscriber.remove(tag.id,id,i);
				} catch (URISyntaxException | StateCreationException e) {
					errStream.out(e,i,Severity.HIGH);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				response = new Response(ResponseCode.DELETED);
				response.setPayload("OK");
			} else {
				Tag user = userMap.get(id);
				// user removal (id=<ID>)
				outStream.out2("detected user removal");
				if (user==null) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("User \"" + id + "\" is not registered on this MN", i);
					i++;
					return;
				}
				outStream.out1("Handling removal of user \"" + id + "\"", i);
				user.active = false;
				try {
					subscriber.remove(id,Node.USER,i);
				} catch (URISyntaxException | StateCreationException e) {
					errStream.out(e,i,Severity.HIGH);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				String[] uri_ = new String[] {cseBaseName, "state", "userMap", id};
				CoapResponse response_ = null;
				cseClient.stepCount();
				try {
					response_ = cseClient.services.oM2Mput(id,user,uri_,false,cseClient.getCount());
				} catch (URISyntaxException e) {
					errStream.out(e,i,Severity.HIGH);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				if (response_==null) {
					errStream.out("Unable to remove user from CSE, timeout expired", i, Severity.HIGH);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				} else if (response_.getCode()!=ResponseCode.CREATED) {
					errStream.out("Unable to remove user from CSE, response: " + response_.getCode(),
							i, Severity.HIGH);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				response = new Response(ResponseCode.DELETED);
			}
		} else {
			String serial0 = getUriValue(exchange,"ser",0);
			if (serial0!=null) {
				if (!isValidSerial(serial0)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("bad request, \"ser=" + serial0 + "\"");
					i++;
					return;
				}
				String serial1 = getUriValue(exchange,"ser",1);
				if (serial1!=null) {
					if (!isValidSerial(serial1)) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out2("bad request, \"ser=" + serial1 + "\"");
						i++;
						return;
					}
					String label0 = getUriValue(exchange,"lab",2);
					String label1 = getUriValue(exchange,"lab",3);
					if (label0==null || !isValidLabel(label0)) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						if (label0!=null)
							outStream.out2("bad request, \"lab=" + label0 + "\"");
						else
							outStream.out2("bad request, \"lab\" is empty");
						i++;
						return;
					}
					if (label1==null || !isValidLabel(label1)) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						if (label1!=null)
							outStream.out2("bad request, \"lab=" + label1 + "\"");
						else
							outStream.out2("bad request, \"lab\" is empty");
						i++;
						return;
					}
					Tag tag0 = tagMap.get(serial0);
					Tag tag1 = tagMap.get(serial1);
					// link removal (ser=<SERIAL>&ser=<SERIAL>&lab=<EVENT_LABEL>&lab=<ACTION_LABEL>&id=<ID>)
					outStream.out2("detected link removal");
					if (tag0==null || tag0.node!=Node.SENSOR) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out("Serial \"" + serial0 + "\" is not registered on this MN as a sensor", i);
						i++;
						return;
					}
					if (tag1==null || tag1.node!=Node.ACTUATOR) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out("Serial \"" + serial1 + "\" is not registered on this MN as an actuator", i);
						i++;
						return;
					}
					if (!isRecognizedLabel(label0,tag0)) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out("Label \"" + label0 + "\" is not a valid label for \"" + serial0 + "\"", i);
						i++;
						return;
					}
					if (!isRecognizedLabel(label1,tag1)) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out("Label \"" + label1 + "\" is not a valid label for \"" + serial1 + "\"", i);
						i++;
						return;
					}
					outStream.out1("Handling removal of link between sensor with serial \"" + serial0 + "\" and actuator serial \"" + serial1 + "\"", i);
					try {
						subscriber.remove(tag0.id,label0,tag1.id,label1,i);
					} catch (URISyntaxException | StateCreationException e) {
						errStream.out(e,i,Severity.HIGH);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					response = new Response(ResponseCode.DELETED);
					response.setPayload("OK");
				} else {
					Tag tag0 = tagMap.get(serial0);
					// node removal (ser=<SERIAL>)
					outStream.out2("detected node removal");
					if (tag0==null) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out("Serial \"" + serial0 + "\" is not registered on this MN as an endpoint node", i);
						i++;
						return;
					}
					outStream.out1("Handling removal of node with serial \"" + serial0 + "\"", i);
					CoapResponse response_ = null;
					tag0.active = false;
					try {
						subscriber.remove(tag0.id,tag0.node,i);
					} catch (URISyntaxException | StateCreationException e) {
						errStream.out(e,i,Severity.HIGH);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					if (tag0.node==Node.SENSOR)
						tracker.remove(tag0.id);
					String[] uri_ = new String[] {cseBaseName, "state", "tagMap", serial0};
					cseClient.stepCount();
					try {
						response_ = cseClient.services.oM2Mput(serial0,tag0,uri_,false,cseClient.getCount());
					} catch (URISyntaxException e) {
						errStream.out(e,i,Severity.HIGH);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					if (response_==null) {
						errStream.out("Unable to remove node from CSE, timeout expired", i, Severity.HIGH);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					} else if (response_.getCode()!=ResponseCode.CREATED) {
						errStream.out("Unable to remove node from CSE, response: " + response_.getCode(),
								i, Severity.HIGH);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					response = new Response(ResponseCode.DELETED);
				}
			} else {
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				outStream.out2("bad request, \"ser\" is empty");
				i++;
				return;
			}
		}
		exchange.respond(response);
		outStream.out2("done");
		i++;
	}

	private CoapResponse forwardNotification(String id, String address, String content, int i) throws URISyntaxException {
		notificationClient.stepCount();
		notificationClient.connect(address,false);
		Request request = new Request(Code.PUT);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.setPayload(content);
		return notificationClient.send(request, Code.PUT);
	}
	
	private CoapResponse write(String label) {
		Request request = new Request(Code.PUT);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.setPayload(label);
		return notificationClient.send(request, Code.PUT);
	}
	
	private void register(String id, String host, boolean debug) throws URISyntaxException, RegistrationException {
		Client tempClient = new Client(Format.joinIdHost(id+"/TEMPclient",host), Constants.protocol + Constants.inAddressADN(debugStream,i) + Constants.inADNRoot, debug);
		Request request = new Request(Code.POST);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().addUriQuery("id" + "=" + Format.normalizeName(cseBaseName));
		CoapResponse response = tempClient.send(request, Code.POST);
		if (response==null) {
			errStream.out("Unable to register to IN at address \"" + tempClient.services.uri() + "\", timeout expired", i, Severity.HIGH);
			outStream.out2("failed");
			throw new RegistrationException();
		} else if (response.getCode()!=ResponseCode.CREATED) {
			if (!response.getResponseText().isEmpty())
				errStream.out("Unable to register to IN at address \"" + tempClient.services.uri() + "\", response: " + response.getCode() +
						", reason: " + response.getResponseText(),
						i, Severity.HIGH);
			else
				errStream.out("Unable to register to IN at address \"" + tempClient.services.uri() + "\", response: " + response.getCode(),
					i, Severity.HIGH);
			outStream.out2("failed");
			throw new RegistrationException();
		}
	}

	private boolean isValidEUI64Address(String address) {
		// TODO
		return !address.isEmpty();
	}

	private String resolveEUI64Address(String address) {
		return Constants.protocol + "[" + Constants.networkAddress + "::" + address + "]:" + Constants.stdActuatorServerPort + "/" + Constants.context;
	}

}