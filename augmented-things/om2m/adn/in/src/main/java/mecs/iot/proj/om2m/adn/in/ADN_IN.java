package mecs.iot.proj.om2m.adn.in;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONException;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.adn.ADN;
import mecs.iot.proj.om2m.adn.in.exceptions.NotFoundMNException;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.Severity;
import mecs.iot.proj.om2m.structures.Configuration;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.ConfigurationDirectory;
import mecs.iot.proj.om2m.structures.ConfigurationType;

class ADN_IN extends ADN {
	
	private HashMap<String,MN> serialMap;																					// serial -> MN
	private String[] subscriptions;
	private HashMap<Integer,MN> locationMap;
	
	Cloud cloud;

	ADN_IN(String id, String host, boolean debug, Console console) throws URISyntaxException {
		super(id,host,debug,console);
		cseClient = new Client(Services.joinIdHost(id+"/CSEclient",host), Constants.protocol + "localhost" + Constants.inCSERoot(id), debug);
		// TODO: create AE, pull from OM2M (serialMap)
		serialMap = new HashMap<String,MN>();
		subscriptions = new String[] {"tagMap","userMap","subscriptionMap"};
		locationMap = new HashMap<Integer,MN>();
		Configuration db = null;
		String[][] mn = null;
		try {
			db = new Configuration ("/configuration/db.ini",ConfigurationDirectory.JAR,ConfigurationType.INI);
			debugStream.out("Found local configuration file (IN)",i);
		} catch (Exception e0) {
			try {
				db = new Configuration ("src/main/resources/configuration/db.ini",ConfigurationDirectory.MAVEN,ConfigurationType.INI);
				debugStream.out("Found local configuration file (IN)",i);
			} catch (Exception e1) {
				try {
					db = new Configuration (Constants.remotePath+"/db.ini",ConfigurationDirectory.REMOTE,ConfigurationType.INI);
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
		cloud = new Cloud(debugStream);
		CommandList list = new CommandList(this,console);
		for (int i=0; i<list.numCommands; i++) {
			console.add(list.text[i][0],list.getCommand(i),list.numOptions[i],list.text[i][1],list.text[i][2],list.isJSON[i]);
		}
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
				exchange.respond(response);
				if (serial!=null)
					outStream.out2("bad request, \"ser=" + serial + "\"");
				else
					outStream.out2("bad request, \"ser\" is empty");
				i++;
				return;
			}
			MN mn = serialMap.get(serial);
			switch (sw) {
				case 0:
					// user localization (mode=0&ser=<SERIAL>)
					outStream.out2("detected user localization");
					if (mn==null) {
						response = new Response(ResponseCode.BAD_REQUEST);
						exchange.respond(response);
						outStream.out("Serial \"" + serial + "\" is not registered on any MN", i);
						i++;
						return;
					}
					outStream.out1("Handling user localization for serial \"" + serial + "\"", i);
					response = new Response(ResponseCode.CONTENT);
					response.setPayload(mn.id + ", " + mn.address);
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
		} else {
			outStream.out2("bad request, \"mode\" is not specified");
			outStream.out1("Handling incorrect request", i);
			response = new Response(ResponseCode.BAD_REQUEST);
			exchange.respond(response);
			outStream.out2("done");
			i++;
			return;
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
				String location = getUriValue(exchange,"loc",2);
				if (location==null || !isValidLocation(location)) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					if (location!=null)
						outStream.out2("bad request, \"loc=" + location + "\"");
					else
						outStream.out2("bad request, \"loc\" is empty");
					i++;
					return;
				}
				int loc;
				try {
					loc = Integer.parseInt(location);
				} catch (NumberFormatException e) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out2("bad request, \"loc=" + location + "\"");
					i++;
					return;
				}
				MN mn = locationMap.get(loc);
				// node IN registration and localization (id=<ID>&ser=<SERIAL>&loc=<LOC>)
				outStream.out2("detected node IN registration and localization");
				if (mn==null || !mn.active) {
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("MN with location \"" + location + "\" is not registered", i);
					i++;
					return;
				}
				outStream.out1("Associating node \"" + id + "\" with serial \"" + serial + "\" to MN \"" + mn.id + "\"", i);
				serialMap.put(serial,mn);
				// TODO: push to OM2M
				response = new Response(ResponseCode.CREATED);
				response.setPayload(mn.id + ", " + mn.address);
			} else {
				// MN registration (id=<ID>)
				outStream.out2("detected MN registration");
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
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("MN with id \"" + id + "\" doesn't exist", i);
					i++;
					return;
				}
				outStream.out1("Registering MN \"" + id + "\"", i);
				mns[index].active = true;
				cseClient.stepCount();
				try {
					cseClient.connect(Constants.protocol + mns[index].address + Constants.mnCSERoot(mns[index].id));
				} catch (URISyntaxException e) {
					errStream.out(e,i,Severity.MEDIUM);
					outStream.out2("failed");
					return;
				}
				String[] uri;
				for (int j=0; j<subscriptions.length; j++) {
					uri = new String[] {mns[index].id,"state",subscriptions[j]};
					try {
						cseClient.services.postSubscription(Constants.protocol+"localhost"+Constants.inADNRoot,"subscription",uri,cseClient.getCount());
					} catch (URISyntaxException e) {
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
				}
				cloud.addMN(id);
				String json = null;
				try {
					json = cloud.getJSONMN(id);
				} catch (NotFoundMNException e) {
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				console.interf.outAsync(json,true);
				response = new Response(ResponseCode.CREATED);
			}
		} else {
			// notification from subscriptions
			outStream.out2("detected notification from subscriptions");
			String notification = exchange.getRequestText();
			if (notification.contains("m2m:vrq")) {
				outStream.out1("Handling subscription confirmation", i);
			} else if (notification.contains("m2m:cnt")) {
				String ri = null;
				String rn = null;
				String la;
				try {
					ri = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cnt"},
							new String[] {"ri"}, new Class<?>[] {String.class});
					rn = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cnt"},
							new String[] {"rn"}, new Class<?>[] {String.class});
					la = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cnt"},
							new String[] {"la"}, new Class<?>[] {String.class});
				} catch (JSONException e) {
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("Received invalid notification", i);
					i++;
					return;
				}
				outStream.out1("Handling Container notification with JSON: " + ri + ", " + rn + ", " + la, i);
				cseClient.stepCount();
				try {
					cseClient.connect(Constants.protocol+"localhost"+Constants.mnCSERoot()+ri.substring(3));
				} catch (URISyntaxException e) {
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				try {
					cseClient.services.postSubscription(Constants.protocol+"localhost"+Constants.inADNRoot,"subscription",new String[]{},cseClient.getCount());
				} catch (URISyntaxException e) {
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				cseClient.stepCount();
				try {
					cseClient.connect(Constants.protocol+"localhost"+Constants.mnCSERoot()+la.substring(3));
				} catch (URISyntaxException e) {
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				CoapResponse response_ = null;
				do {
					try {
						response_ = cseClient.services.getResource(new String[]{},cseClient.getCount());
					} catch (URISyntaxException e) {
						errStream.out(e,i,Severity.MEDIUM);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
					if (response_==null) {
						errStream.out("Unable to read from " + cseClient.services.uri() + ", timeout expired", i, Severity.LOW);
						response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
						exchange.respond(response);
						outStream.out2("failed");
						i++;
						return;
					}
				} while(!hasBeenFound(response_));
				if (response_.getCode()!=ResponseCode.CONTENT) {
					errStream.out("Unable to read from " + cseClient.services.uri() + ", response: " + response_.getCode(),
							i, Severity.LOW);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				String con = null;
				try {
					con = Services.parseJSON(response_.getResponseText(), "m2m:cin",									// Example: "con=("mn":"augmented-things-MN","address":"coap://192.168.0.107:5691/augmented-things","active":true,"id":"user.ALESSANDRO-K7NR")"
							new String[] {"con"}, new Class<?>[] {String.class});
				} catch (JSONException e) {
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				outStream.out1_2("done, getting initial state with JSON: " + con);
				String json = Services.unpackJSON(con.substring(4));
				try {
					cloud.add(json,i);
				} catch (JSONException | NotFoundMNException e) {
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				console.interf.outAsync(json,true);
			} else if (notification.contains("m2m:cin")) {
				String con = null;																						// Example: "con=("mn":"augmented-things-MN","address":"coap://192.168.0.107:5691/augmented-things","active":true,"id":"user.ALESSANDRO-K7NR")"
				try {
					con = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cin"},
							new String[] {"con"}, new Class<?>[] {String.class});
				} catch (JSONException e) {
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.BAD_REQUEST);
					exchange.respond(response);
					outStream.out("Received invalid notification", i);
					i++;
					return;
				}
				outStream.out1("Handling Content Instance notification with JSON: " + con, i);
				String json = Services.unpackJSON(con.substring(4));
				try {
					cloud.add(json,i);
				} catch (JSONException | NotFoundMNException e) {
					errStream.out(e,i,Severity.MEDIUM);
					response = new Response(ResponseCode.INTERNAL_SERVER_ERROR);
					exchange.respond(response);
					outStream.out2("failed");
					i++;
					return;
				}
				console.interf.outAsync(json,true);
			} else {
				outStream.out1("Handling unexpected notification", i);
				response = new Response(ResponseCode.BAD_REQUEST);
				exchange.respond(response);
				outStream.out2("done");
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
	
	private boolean hasBeenFound(CoapResponse response) {
		if (response.getCode().equals(ResponseCode.NOT_FOUND)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return false;
		}
		else
			return true;
	}
	
	private class MN {
		
		String id;
		String address;
		boolean active;
		
		MN(String id, String address) {
			this.id = id;
			this.address = address;
			this.active = false;
		}
		
		@Override
		
		public String toString() {
			return "id=" + id + ", address=" + address + ", active=" + active;
		}
		
		@Override
		
		public boolean equals(Object obj) {
			MN mn = (MN)obj;
			return id.equals(mn.id);
		}
		
	}

}
