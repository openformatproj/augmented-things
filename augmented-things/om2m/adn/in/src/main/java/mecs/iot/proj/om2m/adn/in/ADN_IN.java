package mecs.iot.proj.om2m.adn.in;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONException;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.adn.ADN;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.structures.Configuration;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.MN;
import mecs.iot.proj.om2m.structures.Pack;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Type;

class ADN_IN extends ADN {
	
	private HashMap<String,MN> serialMap;																					// serial -> MN
	private String[] subscriptions;
	private HashMap<Integer,MN> locationMap;

	ADN_IN(String id, String host, boolean debug, Console console) throws URISyntaxException {
		super(id,host,debug,console);
		cseClient = new Client(Services.joinIdHost(id+"/CSEclient",host), Constants.cseProtocol + "localhost" + Constants.inCSERoot(id), debug);
		serialMap = new HashMap<String,MN>();
		subscriptions = new String[] {"tagMap","userMap","subscriptionMap"};
		locationMap = new HashMap<Integer,MN>();
		Configuration db = null;
		String[][] mn = null;
		try {
			db = new Configuration ("/configuration/db.ini",Pack.JAR,Type.INI);
			debugStream.out("Found local configuration file (IN)",i);
		} catch (Exception e0) {
			try {
				db = new Configuration ("src/main/resources/configuration/db.ini",Pack.MAVEN,Type.INI);
				debugStream.out("Found local configuration file (IN)",i);
			} catch (Exception e1) {
				try {
					db = new Configuration (Constants.remotePath+"/db.ini",Pack.REMOTE,Type.INI);
					debugStream.out("Found remote configuration file (IN)",i);
				} catch (Exception e2) {
					debugStream.out("No configuration files (IN) found, using default values",i);
				}
			}
		}
		try {
			mn = db.getAttributeList("mecs.iot.proj.om2m.mnList",2);
		} catch (Exception e) {
			mn = new String[1][2];
			mn[0][0] = "augmented-things-MN";
			mn[0][1] = "127.0.0.1";
		}
		for (int j=0; j<mn.length; j++) {
			locationMap.put(j, new MN(mn[j][0],mn[j][1]));
			// debugStream.out("\t"+mn[j][0]+","+mn[j][1],i);
		}
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
			MN mn = serialMap.get(serial);
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
					response.setPayload(mn.id + ", " + mn.address);
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
				MN mn = locationMap.get(Integer.parseInt(location));
				if (mn==null || !mn.active) {
					debugStream.out("MN with location \"" + location + "\" is not registered", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				outStream.out1("Associating node \"" + id + "\" with serial \"" + serial + "\" to MN \"" + mn.id + "\"", i);
				serialMap.put(serial,mn);
				response = new Response(ResponseCode.CREATED);
				response.setPayload(mn.id + ", " + mn.address);
			} else {
				// MN registration (id=<ID>)
				MN[] mns = locationMap.values().toArray(new MN[] {});
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
				cseClient.stepCount();
				try {
					cseClient.connect(Constants.cseProtocol + mns[index].address + Constants.mnCSERoot(mns[index].id));
				} catch (URISyntaxException e) {
					outStream.out2("failed");
					errStream.out(e, i, Severity.MEDIUM);
					return;
				}
				String[] uri;
				for (int j=0; j<subscriptions.length; j++) {
					uri = new String[] {mns[index].id,"state",subscriptions[j]};
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
				}
				response = new Response(ResponseCode.CREATED);
			}
		} else {
			// notifications from subscriptions
			String notification = exchange.getRequestText();
			if (notification.contains("m2m:vrq")) {
				outStream.out1("Handling subscription confirmation", i);
			} else if (notification.contains("m2m:cnt")) {
				String ri = null;
				String rn = null;																						// serial, user id or resource id
				try {
					ri = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cnt"},
							new String[] {"ri"}, new Class<?>[] {String.class});
					rn = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cnt"},
							new String[] {"rn"}, new Class<?>[] {String.class});
				} catch (JSONException e) {
					debugStream.out("Received invalid notification", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				outStream.out1("Handling Container notification with JSON: " + ri + ", " + rn, i);
				cseClient.stepCount();
				try {
					cseClient.connect(Constants.adnProtocol+"localhost"+Constants.mnCSERoot()+ri.substring(3));
				} catch (URISyntaxException e) {
					outStream.out2("failed");
					errStream.out(e, i, Severity.MEDIUM);
					return;
				}
				try {
					cseClient.services.postSubscription(Constants.adnProtocol+"localhost"+Constants.inADNRoot,"subscription",new String[]{},cseClient.getCount());
				} catch (URISyntaxException e) {
					outStream.out2("failed");
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					i++;
					return;
				}
//				String[] uri = new String[] {"la"};
//				CoapResponse response_ = null;
//				cseClient.stepCount();
//				try {
//					response_ = cseClient.services.getResource(uri,cseClient.getCount());
//				} catch (URISyntaxException e) {
//					outStream.out2("failed");
//					errStream.out(e,i,Severity.MEDIUM);
//					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
//					exchange.respond(response);
//					i++;
//					return;
//				}
//				if (response_==null) {
//					outStream.out2("failed");
//					errStream.out("Unable to read from " + cseClient.services.uri() + ", timeout expired", i, Severity.LOW);
//					response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
//					exchange.respond(response);
//					i++;
//					return;
//				} else if (response_.getCode()!=ResponseCode.CONTENT) {
//					outStream.out2("failed");
//					errStream.out("Unable to read from " + cseClient.services.uri() + ", response: " + response_.getCode(),
//							i, Severity.LOW);
//					response = new Response(ResponseCode.SERVICE_UNAVAILABLE);
//					exchange.respond(response);
//					i++;
//					return;
//				}
//				String con = null;																						// Example: "con=("mn":"augmented-things-MN","address":"coap://192.168.0.107:5691/augmented-things","active":true,"id":"user.ALESSANDRO-K7NR")"
//				try {
//					con = Services.parseJSON(response_.getResponseText(), "m2m:cin",
//							new String[] {"con"}, new Class<?>[] {String.class});
//				} catch (JSONException e) {
//					outStream.out2("failed");
//					errStream.out("Content of " + cseClient.services.uri() + " is invalid", i, Severity.MEDIUM);
//					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
//					exchange.respond(response);
//					i++;
//					return;
//				}
//				outStream.out1_2("Getting initial state with JSON: " + con);
//				String json = Services.unpackJSON(con.substring(4));
				// TODO
			} else if (notification.contains("m2m:cin")) {
				String con = null;																						// Example: "con=("mn":"augmented-things-MN","address":"coap://192.168.0.107:5691/augmented-things","active":true,"id":"user.ALESSANDRO-K7NR")"
				try {
					con = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cin"},
							new String[] {"con"}, new Class<?>[] {String.class});
				} catch (JSONException e) {
					debugStream.out("Received invalid notification", i);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					i++;
					return;
				}
				outStream.out1("Handling Content Instance notification with JSON: " + con, i);
//				String json = Services.unpackJSON(con.substring(4));
				// TODO
			} else {
				outStream.out1("Received unexpected notification", i);
			}
			response = new Response(ResponseCode.CREATED);
		}
		exchange.respond(response);
		outStream.out2("done");
		i++;
	}
	
	// TODO: middle-node removal

}
