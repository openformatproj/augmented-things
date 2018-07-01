package mecs.iot.proj.om2m;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.structures.Constants;

import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;

import org.json.JSONException;
import org.json.JSONObject;

public class Services {
	
	private Client client;
	private Path path;
	
	public Services (Client client, String uri) {
		this.client = client;
		path = new Path(client,uri,4);
	}
	
	private static String parseJSONObject(JSONObject obj, String attr, Class<?> Type) throws JSONException {
		Object attribute = null;
		attribute = obj.get(attr);
		if (Type==Integer.class) {
			return attr + "=" + Integer.toString((Integer)attribute);
		} else if (Type==String.class) {
			return attr + "=" + (String)attribute;
		} else
			return null;
	}
	
	public static String parseJSON(String json, String type, String[] attr, Class<?>[] Type) {
		JSONObject root = null;
		try {
			root = new JSONObject(json);
		} catch (JSONException e) {
			return "Invalid JSON: " + json;
		}
		JSONObject obj = null;
		try {
			obj = (JSONObject) root.get(type);
		} catch (JSONException e) {
			return "Invalid JSON type \"" + type + "\": " + json;
		}
		String parse = "";
		for (int i=0; i<attr.length; i++) {
			try {
				parse += parseJSONObject(obj,attr[i],Type[i]);
			} catch (JSONException e) {
				return "Invalid JSON attribute \"" + attr[i] + "\": " + json;
			}
			if (i<attr.length-1)
				parse += ", ";
		}
		return parse;
	}
	
	public static String parseJSON(String json, String[] type, String[] attr, Class<?>[] Type) {
		JSONObject root = null;
		try {
			root = new JSONObject(json);
		} catch (JSONException e) {
			return "Invalid JSON: " + json;
		}
		JSONObject obj = root;
		for (int i=0; i<type.length; i++) {
			try {
				obj = (JSONObject) obj.get(type[i]);
			} catch (JSONException e) {
				return "Invalid JSON type \"" + type[i] + "\": " + json;
			}
		}
		String parse = "";
		for (int i=0; i<attr.length; i++) {
			try {
				parse += parseJSONObject(obj,attr[i],Type[i]);
			} catch (JSONException e) {
				return "Invalid JSON attribute \"" + attr[i] + "\": " + json;
			}
			if (i<attr.length-1)
				parse += ", ";
		}
		return parse;
	}
	
	public static String parseJSON(String json, String type, String[] attr, Class<?>[] Type, boolean debug) throws JSONException {
		JSONObject root = null;
		try {
			root = new JSONObject(json);
		} catch (JSONException e) {
			throw e;
		}
		JSONObject obj = null;
		try {
			obj = (JSONObject) root.get(type);
		} catch (JSONException e) {
			throw e;
		}
		String parse = "";
		for (int i=0; i<attr.length; i++) {
			try {
				parse += parseJSONObject(obj,attr[i],Type[i]);
			} catch (JSONException e) {
				throw e;
			}
			if (i<attr.length-1)
				parse += ", ";
		}
		return parse;
	}
	
	public static String parseJSON(String json, String[] type, String[] attr, Class<?>[] Type, boolean debug) throws JSONException {
		JSONObject root = null;
		try {
			root = new JSONObject(json);
		} catch (JSONException e) {
			throw e;
		}
		JSONObject obj = root;
		for (int i=0; i<type.length; i++) {
			try {
				obj = (JSONObject) obj.get(type[i]);
			} catch (JSONException e) {
				throw e;
			}
		}
		String parse = "";
		for (int i=0; i<attr.length; i++) {
			try {
				parse += parseJSONObject(obj,attr[i],Type[i]);
			} catch (JSONException e) {
				throw e;
			}
			if (i<attr.length-1)
				parse += ", ";
		}
		return parse;
	}
	
	public static String parseCoapRequest(Request request) {
		List<String> list = request.getOptions().getUriQuery();
		String str = "";
		for (int i=0; i<list.size(); i++) {
			if (i<list.size()-1)
				str += list.get(i) + "&";
			else
				str += list.get(i);
		}
		return str;
	}
	
	public static String normalizeName(String name) {
		return name.replace('@','.');
	}
	
	public static String joinIdHost(String id, String host) {
		return id + "@" + host;
	}
	
	public CoapResponse getResource(String[] uri, int i) throws URISyntaxException {
		path.change(uri);
		Request request = new Request(Code.GET); 										// Create a GET request
		request.getOptions().addOption(new Option(267,1));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		client.debugStream.out("Sent access request to " + path.uri(), i);
		return client.send(request, Code.GET);
	}
	
	public CoapResponse postAE(String name, int i) {
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267,2));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("api","TempApp-ID");
		obj.put("rr","true");
		obj.put("rn",name);
		JSONObject root = new JSONObject();
		root.put("m2m:ae",obj);
		request.setPayload(root.toString());
		client.debugStream.out("Sent AE creation with JSON: " + root.toString() + " to " + path.uri(), i);
		return client.send(request, Code.POST);
	}
	
	public CoapResponse postContainer(String name1, String name2, int i) throws URISyntaxException {
		if (path.level==0) {
			path.down(name1);
			path.down(name2);
		}
		if (path.level==1) {
			path.down(name2);
		}
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267,3));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("rn","data");
		JSONObject root = new JSONObject();
		root.put("m2m:cnt",obj);
		request.setPayload(root.toString());
		client.debugStream.out("Sent Container creation with JSON: " + root.toString() + " to " + path.uri(), i);
		return client.send(request, Code.POST);
	}
	
	public CoapResponse postContentInstance(String content, int i) throws URISyntaxException {
		if (path.level==2)
			path.down("data");
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267,4));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("cnf","text/plain:0");
//		obj.put("con",String.format("%.3f",value)+" "+ measureUnit);
		obj.put("con",content);
		JSONObject root = new JSONObject();
		root.put("m2m:cin",obj);
		request.setPayload(root.toString());
		client.debugStream.out("Sent Content Instance creation with JSON: " + root.toString() + " to " + path.uri(), i);
		return client.send(request, Code.POST);
	}
	
	public void postSubscription(String observer, String id, String[] uri, int i) throws URISyntaxException {
		path.change(uri);
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267,23));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("rn",id);
		obj.put("nu",Constants.adnProtocol+"localhost"+observer);
		// obj.put("nct",1);
		obj.put("nct",2);
		JSONObject root = new JSONObject();
		root.put("m2m:sub",obj);
		request.setPayload(root.toString());
		client.debugStream.out("Sent Subscription creation with JSON: " + root.toString() + " to " + path.uri(), i);
		client.sendAsync(request, Code.POST);
	}
	
	public CoapResponse deleteSubscription(String[] uri, int i) throws URISyntaxException {
		path.change(uri);
		Request request = new Request(Code.DELETE);
		request.getOptions().addOption(new Option(267,23));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		client.debugStream.out("Sent Subscription deletion to " + path.uri(), i);
		return client.send(request, Code.DELETE);
	}
	
	public String uri() {
		return path.uri();
	}
	
	public static String getKey(String id) {
		return id.split("cnt-")[1];
	}

}
