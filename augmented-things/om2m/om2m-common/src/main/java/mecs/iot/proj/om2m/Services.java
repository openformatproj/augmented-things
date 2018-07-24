package mecs.iot.proj.om2m;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.JSONSerializable;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Services {
	
	private Client client;
	private PathManager pathManager;
	
	public Services (Client client, String uri) {
		this.client = client;
		pathManager = new PathManager(client,uri);
	}
	
	private static String parseJSONObject(JSONObject obj, String attr, Class<?> attrType) throws JSONException {
		Object attribute = obj.get(attr);
		if (attrType==Integer.class) {
			return attr + "=" + Integer.toString((Integer)attribute);
		} else if (attrType==String.class) {
			return attr + "=" + (String)attribute;
		} else if (attrType==Boolean.class) {
			return attr + "=" + (boolean)attribute;
		} else
			return null;
	}
	
	private static String parseJSONObject_(JSONObject obj, String attr, Class<?> attrType) throws JSONException {
		Object attribute = obj.get(attr);
		if (attrType==Integer.class) {
			return Integer.toString((Integer)attribute);
		} else if (attrType==String.class) {
			return (String)attribute;
		} else if (attrType==Boolean.class) {
			return "" + (boolean)attribute;
		} else
			return null;
	}
	
	private static List<Object> parseJSONArray_(JSONObject obj, String attr) throws JSONException {
		JSONArray jsonArray = obj.getJSONArray(attr);
		return jsonArray.toList();
	}
	
	public static String parseJSONObject(String json, String attr, Class<?> attrType) throws JSONException {
		JSONObject obj = null;
		try {
			obj = new JSONObject(json);
		} catch (JSONException e) {
			throw e;
		}
		String parse = null;
		try {
			parse = parseJSONObject_(obj,attr,attrType);
		} catch (JSONException e) {
			throw e;
		}
		return parse;
	}
	
	public static String parseJSONObject(String json, String outerAttr, String attr, Class<?> attrType) throws JSONException {
		JSONObject root = null;
		try {
			root = new JSONObject(json);
		} catch (JSONException e) {
			throw e;
		}
		JSONObject obj = null;
		try {
			obj = (JSONObject) root.get(outerAttr);
		} catch (JSONException e) {
			throw e;
		}
		String parse = null;
		try {
			parse = parseJSONObject_(obj,attr,attrType);
		} catch (JSONException e) {
			throw e;
		}
		return parse;
	}
	
	@SuppressWarnings("unchecked")
	
	public static String[] parseJSONArray(String json, String[] outerAttr, String attr) throws JSONException, IndexOutOfBoundsException {
		JSONObject root = null;
		try {
			root = new JSONObject(json);
		} catch (JSONException e) {
			throw e;
		}
		List<Object> jsonList = null;
		try {
			jsonList = parseJSONArray_(root,outerAttr[0]);
		} catch (JSONException | IndexOutOfBoundsException e) {
			throw e;
		}
		ArrayList<String> attributes = new ArrayList<String>();
		HashMap<String,Object> map = null;
		for (int j=0; j<jsonList.size(); j++) {
			if (attr==null) {
				if (outerAttr.length==1) {
					attributes.add((String)jsonList.get(j));
				} else {
					map = (HashMap<String,Object>)jsonList.get(j);
					for (int i=1; i<outerAttr.length-1; i++) {
						try {
							map = (HashMap<String,Object>)map.get(outerAttr[i]);
						} catch (JSONException e) {
							throw e;
						}
					}
					attributes.add((String)map.get(outerAttr[outerAttr.length-1]));
				}
			} else {
				map = (HashMap<String,Object>)jsonList.get(j);
				for (int i=1; i<outerAttr.length; i++) {
					try {
						map = (HashMap<String,Object>)map.get(outerAttr[i]);
					} catch (JSONException e) {
						throw e;
					}
				}
				attributes.add((String)map.get(attr));
			}
		}
		return attributes.toArray(new String[] {});
	}
	
	public static String parseJSON(String json, String outerAttr, String[] attr, Class<?>[] attrType) throws JSONException {
		JSONObject root = null;
		try {
			root = new JSONObject(json);
		} catch (JSONException e) {
			throw e;
		}
		JSONObject obj = null;
		try {
			obj = (JSONObject) root.get(outerAttr);
		} catch (JSONException e) {
			throw e;
		}
		String parse = "";
		for (int i=0; i<attr.length; i++) {
			try {
				parse += parseJSONObject(obj,attr[i],attrType[i]);
			} catch (JSONException e) {
				throw e;
			}
			if (i<attr.length-1)
				parse += ", ";
		}
		return parse;
	}
	
	public static String parseJSON(String json, String[] outerAttr, String[] attr, Class<?>[] attrType) throws JSONException {
		JSONObject root = null;
		try {
			root = new JSONObject(json);
		} catch (JSONException e) {
			throw e;
		}
		JSONObject obj = root;
		for (int i=0; i<outerAttr.length; i++) {
			try {
				obj = (JSONObject) obj.get(outerAttr[i]);
			} catch (JSONException e) {
				throw e;
			}
		}
		String parse = "";
		for (int i=0; i<attr.length; i++) {
			try {
				parse += parseJSONObject(obj,attr[i],attrType[i]);
			} catch (JSONException e) {
				throw e;
			}
			if (i<attr.length-1)
				parse += ", ";
		}
		return parse;
	}
	
	public static JSONObject toJSONArray(JSONSerializable[] json, String attribute) {
		JSONObject obj = new JSONObject();
		for (int i=0; i<json.length; i++)
			obj.append(attribute,json[i].toJSON());
		return obj;
	}
	
	public static JSONObject toJSONArray(String[] value, String attribute) {
		JSONObject obj = new JSONObject();
		for (int i=0; i<value.length; i++)
			obj.append(attribute,value[i]);
		return obj;
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
		pathManager.change(uri);
		Request request = new Request(Code.GET); 										// Create a GET request
		request.getOptions().addOption(new Option(267,1));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		client.debugStream.out("Sent reading request to CSE", i);
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
		client.debugStream.out("Sent AE creation to CSE", i);
		return client.send(request, Code.POST);
	}
	
	public CoapResponse postContainer(String name1, String name2, int i) throws URISyntaxException {
		if (pathManager.level==0) {
			pathManager.down(name1,false);
			pathManager.down(name2,true);
		}
		if (pathManager.level==1) {
			pathManager.down(name2,true);
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
		client.debugStream.out("Sent Container creation to CSE", i);
		return client.send(request, Code.POST);
	}
	
	public CoapResponse postContainer(String name1, String name2, String name3, int i) throws URISyntaxException {
		if (pathManager.level==0) {
			pathManager.down(name1,false);
			pathManager.down(name2,true);
		}
		if (pathManager.level==1) {
			pathManager.down(name2,true);
		}
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267,3));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("rn",name3);
		JSONObject root = new JSONObject();
		root.put("m2m:cnt",obj);
		request.setPayload(root.toString());
		client.debugStream.out("Sent Container creation to CSE", i);
		return client.send(request, Code.POST);
	}
	
	public CoapResponse postContentInstance(String content, int i) throws URISyntaxException {
		if (pathManager.level==2)
			pathManager.down("data",true);
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267,4));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("cnf","text/plain:0");
		obj.put("con",content);
		JSONObject root = new JSONObject();
		root.put("m2m:cin",obj);
		request.setPayload(root.toString());
		client.debugStream.out("Sent Content Instance creation to CSE", i);
		return client.send(request, Code.POST);
	}
	
	public CoapResponse postContentInstance(String content, String[] uri, int i) throws URISyntaxException {
		pathManager.change(uri);
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267,4));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("cnf","text/plain:0");
		obj.put("con",content);
		JSONObject root = new JSONObject();
		root.put("m2m:cin",obj);
		request.setPayload(root.toString());
		client.debugStream.out("Sent Content Instance creation to CSE", i);
		return client.send(request, Code.POST);
	}
	
	public void postSubscription(String observer, String id, String[] uri, int i) throws URISyntaxException {
		pathManager.change(uri);
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267,23));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("rn",id);
		obj.put("nu",observer);
		// obj.put("nct",1);
		obj.put("nct",2);
		JSONObject root = new JSONObject();
		root.put("m2m:sub",obj);
		request.setPayload(root.toString());
		client.debugStream.out("Sent Subscription creation to CSE", i);
		client.sendAsync(request, Code.POST);
	}
	
	public CoapResponse deleteSubscription(String[] uri, int i) throws URISyntaxException {
		pathManager.change(uri);
		Request request = new Request(Code.DELETE);
		request.getOptions().addOption(new Option(267,23));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		client.debugStream.out("Sent Subscription deletion to CSE", i);
		return client.send(request, Code.DELETE);
	}
	
	public CoapResponse oM2Mput(String key, JSONSerializable content, String[] uri, boolean createContainer, int i) throws URISyntaxException {
		pathManager.change(uri);
		Request request = null;
		JSONObject obj = null;
		JSONObject root = null;
		if (createContainer) {
			request = new Request(Code.POST);
			request.getOptions().addOption(new Option(267,3));
			request.getOptions().addOption(new Option(256,"admin:admin"));
			request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
			request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
			obj = new JSONObject();
			obj.put("rn",key);
			root = new JSONObject();
			root.put("m2m:cnt",obj);
			request.setPayload(root.toString());
			client.debugStream.out("Sent Container creation to CSE", i);
			CoapResponse response = client.send(request, Code.POST);
			if (response==null || (response.getCode()!=ResponseCode.CREATED && response.getCode()!=ResponseCode.FORBIDDEN)) {
				return response;
			}
			pathManager.down(key,true);
		}
		request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267,4));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		obj = new JSONObject();
		obj.put("cnf","text/plain:0");
		obj.put("con",packJSON(content.toJSON().toString()));
		root = new JSONObject();
		root.put("m2m:cin",obj);
		request.setPayload(root.toString());
		client.debugStream.out("Sent Content Instance creation to CSE", i);
		return client.send(request, Code.POST);
	}
	
	public CoapResponse oM2Mput(String key, JSONObject content, String[] uri, boolean createContainer, int i) throws URISyntaxException {
		pathManager.change(uri);
		Request request = null;
		JSONObject obj = null;
		JSONObject root = null;
		if (createContainer) {
			request = new Request(Code.POST);
			request.getOptions().addOption(new Option(267,3));
			request.getOptions().addOption(new Option(256,"admin:admin"));
			request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
			request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
			obj = new JSONObject();
			obj.put("rn",key);
			root = new JSONObject();
			root.put("m2m:cnt",obj);
			request.setPayload(root.toString());
			client.debugStream.out("Sent Container creation to CSE", i);
			CoapResponse response = client.send(request, Code.POST);
			if (response==null || (response.getCode()!=ResponseCode.CREATED && response.getCode()!=ResponseCode.FORBIDDEN)) {
				return response;
			}
			pathManager.down(key,true);
		}
		request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267,4));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		obj = new JSONObject();
		obj.put("cnf","text/plain:0");
		obj.put("con",packJSON(content.toString()));
		root = new JSONObject();
		root.put("m2m:cin",obj);
		request.setPayload(root.toString());
		client.debugStream.out("Sent Content Instance creation to CSE", i);
		return client.send(request, Code.POST);
	}
	
	public static String getKeyFromAttribute(String attr) {
		return attr.split("cnt-")[1];
	}
	
	public static String getPathFromKey(String key) {
		return "cnt-" + key;
	}
	
	public String uri() {
		return pathManager.uri();
	}
	
	private static ArrayList<String[]> packTable = new ArrayList<String[]>();
	private static ArrayList<String[]> unpackTable = new ArrayList<String[]>();
	
	static {
		
		// Replace curly brackets with round brackets
		packTable.add(new String[] {"{","("});
		packTable.add(new String[] {"}",")"});
		
		// Replace round brackets with curly brackets
		unpackTable.add(new String[] {"(","{"});
		unpackTable.add(new String[] {")","}"});
		
	}
	
	public static String packJSON (String json) {
		String[] pair = new String[2];
		for (int i=0; i<packTable.size(); i++) {
			pair = packTable.get(i);
			json = json.replace(pair[0],pair[1]);
		}
		return json;
	}
	
	public static String unpackJSON (String json) {
		String[] pair = new String[2];
		for (int i=0; i<unpackTable.size(); i++) {
			pair = unpackTable.get(i);
			json = json.replace(pair[0],pair[1]);
		}
		return json;
	}
	
	public static String formatJSON (String json) {
		int level = 0;
		char[] chars;
		String str = json.replace(" ","");
		int characters = 1;
		char character;
		for (int i=0; i<characters; i++) {
			chars = str.toCharArray();
			character = chars[i];
			characters = chars.length;
			if (isOpeningParenthesis(character)) {
				str = insertStringAfter(str,Constants.newLine,i);
				level++;
				for (int j=0; j<level; j++) {
					str = insertStringAfter(str,Constants.tab,i+Constants.newLine.length());
				}
			} else if (character==',') {
				for (int j=0; j<level; j++) {
					str = insertStringAfter(str,Constants.tab,i);
				}
				str = insertStringAfter(str,Constants.newLine,i);
			} else if (isClosingParenthesis(character)) {
				str = insertStringBefore(str,Constants.newLine,i);
				i += Constants.newLine.length();
				level--;
				for (int j=0; j<level; j++) {
					str = insertStringBefore(str,Constants.tab,i);
					i += Constants.tab.length();
				}
			}
		}
		return str;
	}
	
	private static boolean isOpeningParenthesis(char c) {
		return c=='{' || c=='[';
	}
	
	private static boolean isClosingParenthesis(char c) {
		return c=='}' || c==']';
	}
	
	private static String insertStringBefore(String str, String c, int position) {
		return str.substring(0,position) + c + str.substring(position,str.length());
	}
	
	private static String insertStringAfter(String str, String c, int position) {
		return str.substring(0,position+1) + c + str.substring(position+1,str.length());
	}
	
	public static void main (String[] args) {
		String json = "{" + 
				"   \"mn\":\"augmented-things-MN\"," + 
				"   \"subs\":[" + 
				"      {" + 
				"         \"receiver\":{" + 
				"            \"node\":\"ACTUATOR\"," + 
				"            \"address\":\"coap://127.0.0.1:5690/augmented-things\"," + 
				"            \"id\":\"actuator.alessandro\"" + 
				"         }," + 
				"         \"sender\":{" + 
				"            \"node\":\"SENSOR\"," + 
				"            \"id\":\"sensor.alessandro\"," + 
				"            \"type\":\"tempC\"" + 
				"         }," + 
				"         \"action\":\"action1\"," + 
				"         \"event\":\"event\"" + 
				"      }," + 
				"      {" + 
				"         \"receiver\":{" + 
				"            \"node\":\"USER\"," + 
				"            \"address\":\"coap://192.168.0.107:5691/augmented-things\"," + 
				"            \"id\":\"user.ALESSANDRO-K7NR\"" + 
				"         },\r\n" + 
				"         \"sender\":{" + 
				"            \"node\":\"SENSOR\"," + 
				"            \"id\":\"sensor.alessandro\"," + 
				"            \"type\":\"tempC\"" + 
				"         }" + 
				"      }" + 
				"   ]," + 
				"   \"id\":\"sensor.alessandro\"" + 
				"}";
		String[] v1 = parseJSONArray(json,new String[] {"subs","receiver"},"id");
		String[] v2 = parseJSONArray(json,new String[] {"subs"},"event");
		for (int i=0;i<v1.length; i++)
			System.out.println(v1[i]);
		for (int i=0;i<v2.length; i++)
			System.out.println(v2[i]);
		json = "{" + 
				"   \"mn\":\"augmented-things-MN\"," + 
				"   \"active\":true," + 
				"   \"attributes\":[" + 
				"      \"action1\"," + 
				"      \"action2\"" + 
				"   ]," + 
				"   \"id\":\"actuator.alessandro\"," + 
				"   \"type\":\"act\"" + 
				"}";
		String[] v3 = parseJSONArray(json,new String[] {"attributes"},null);
		for (int i=0;i<v3.length; i++)
			System.out.println(v3[i]);
	}

}
