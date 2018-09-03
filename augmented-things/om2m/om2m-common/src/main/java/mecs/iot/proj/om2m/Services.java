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

/** A collection of oneM2M services.
 * Static methods allow manipulation of JSON objects,
 * while instance methods are meant to be used on a
 * {@link mecs.iot.proj.om2m.Client Client} object (Client.services)
 * 
 * @author Alessandro Trifoglio
 * @version 0.0.1-SNAPSHOT
 * @since 0.0.1-SNAPSHOT
 */
public class Services {
	
	private Client client;
	private PathManager pathManager;
	
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
	
	Services (Client client, String uri) {
		this.client = client;
		pathManager = new PathManager(client,uri);
	}
	
	/** Pack a JSON string.
	 * Makes a JSON string suited to be sent towards
	 * an oneM2M service.
	 * 
	 * @param json the JSON string to pack
	 * @return the packed string
	 */
	public static String packJSON (String json) {
		String[] pair = new String[2];
		for (int i=0; i<packTable.size(); i++) {
			pair = packTable.get(i);
			json = json.replace(pair[0],pair[1]);
		}
		return json;
	}
	
	/** Unpack a JSON string.
	 * Converts a JSON coming from an oneM2M service
	 * into a well-formed JSON string.
	 * 
	 * @param json the JSON string to unpack
	 * @return the unpacked string
	 */
	public static String unpackJSON (String json) {
		String[] pair = new String[2];
		for (int i=0; i<unpackTable.size(); i++) {
			pair = unpackTable.get(i);
			json = json.replace(pair[0],pair[1]);
		}
		return json;
	}
	
	/** Format a JSON string.
	 * Converts an inline JSON string into an expanded
	 * and better readable one. Ideal for GUIs.
	 * 
	 * @param json the JSON string to format
	 * @return the formatted string
	 */
	public static String formatJSON (String json) {
		int level = 0;
		char[] charArray;
		boolean parse = true;
		String str = json.replace(" ","");
		charArray = str.toCharArray();
		char character;
		for (int i=0; i<charArray.length; i++) {
			character = charArray[i];
			if (character=='"')
				parse = !parse;
			if (parse) {
				if (isOpeningParenthesis(character)) {
					str = insertStringAfter(str,Constants.newLine,i);
					level++;
					for (int j=0; j<level; j++) {
						str = insertStringAfter(str,Constants.tab,i+Constants.newLine.length());
					}
				} else if (character==','/* || character==';'*/) {
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
			charArray = str.toCharArray();
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
	
	/** Parse a JSON string.
	 * Gets a JSON string and retrieves a string containing
	 * the content of an arbitrary number of attributes inside
	 * a main outer JSON container.
	 * Example:
	 * <ul>
	 * <li>
	 * <code>Services.parseJSON("{"m2m:ae":{"rn":"state","ty":2}}", "m2m:ae", new String[] {"rn","ty"}, new Class&lt;?&gt;[] {String.class,Integer.class});</code>
	 * retrieves <code>"rn=state, ty=2"</code>
	 * </li>
	 * </ul>
	 * 
	 * @param json the JSON string to parse
	 * @param outerAttr the outer attribute
	 * @param attribute the array of attributes to extract content from
	 * @param attrType an array of types describing the class of contents
	 * @return the parsed string of attributes and their content
	 */
	public static String parseJSON(String json, String outerAttr, String[] attribute, Class<?>[] attrType) throws JSONException {
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
		for (int i=0; i<attribute.length; i++) {
			try {
				parse += parseJSONObject(obj,attribute[i],attrType[i]);
			} catch (JSONException e) {
				throw e;
			}
			if (i<attribute.length-1)
				parse += ", ";
		}
		return parse;
	}
	
	/** Parse a JSON string.
	 * Gets a JSON string and retrieves a string containing
	 * the content of an arbitrary number of attributes inside
	 * a main outer JSON container.
	 * Example:
	 * <ul>
	 * <li>
	 * <code>con = Services.parseJSON(notification, new String[] {"m2m:sgn","m2m:nev","m2m:rep","m2m:cin"}, new String[] {"con"}, new Class&lt;?&gt;[] {String.class});</code>
	 * may retrieve for instance <code>"con=(\"mn\":\"augmented-things-MN\",\"id\":\"user.ALESSANDRO-K7NR\")"</code>
	 * </li>
	 * </ul>
	 * 
	 * @param json the JSON string to parse
	 * @param outerAttr an array of attributes identifying the outer container
	 * @param attribute the array of attributes to extract content from
	 * @param attrType an array of types describing the class of contents
	 * @return the parsed string of attributes and their content
	 */
	public static String parseJSON(String json, String[] outerAttr, String[] attribute, Class<?>[] attrType) throws JSONException {
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
		for (int i=0; i<attribute.length; i++) {
			try {
				parse += parseJSONObject(obj,attribute[i],attrType[i]);
			} catch (JSONException e) {
				throw e;
			}
			if (i<attribute.length-1)
				parse += ", ";
		}
		return parse;
	}
	
	/** Parse a JSON string.
	 * Gets a JSON string and retrieves a string containing
	 * the content of an inner attribute.
	 * 
	 * @param json the JSON string to parse
	 * @param attribute the attribute to extract content from
	 * @param attrType a type describing the class of content
	 * @return the content of requested attribute
	 */
	public static String parseJSONObject(String json, String attribute, Class<?> attrType) throws JSONException {
		JSONObject obj = null;
		try {
			obj = new JSONObject(json);
		} catch (JSONException e) {
			throw e;
		}
		String parse = null;
		try {
			parse = parseJSONObjectSilent(obj,attribute,attrType);
		} catch (JSONException e) {
			throw e;
		}
		return parse;
	}
	
	/** Parse a JSON string.
	 * Gets a JSON string and retrieves a string containing
	 * the content of an attribute inside a main outer JSON
	 * container.
	 * 
	 * @param json the JSON string to parse
	 * @param outerAttr the outer attribute
	 * @param attribute the attribute to extract content from
	 * @param attrType a type describing the class of content
	 * @return the content of requested attribute
	 */
	public static String parseJSONObject(String json, String outerAttr, String attribute, Class<?> attrType) throws JSONException {
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
			parse = parseJSONObjectSilent(obj,attribute,attrType);
		} catch (JSONException e) {
			throw e;
		}
		return parse;
	}
	
	/** Parse a JSON string.
	 * Gets a JSON string and retrieves a string containing
	 * the content of an attribute embedded multiple times
	 * inside a JSON array.
	 * Example:
	 * <ul>
	 * <li>
	 * <code>id = parseJSONArray(json,new String[] {"subs","receiver"},"id");</code>
	 * </li>
	 * </ul>
	 * 
	 * @param json the JSON string to parse
	 * @param outerAttr an array of attributes identifying the array and the outer container
	 * @param attribute the attribute to extract content from (assumed of type String)
	 * @return an array containing all instances of the requested attribute inside the array
	 */
	@SuppressWarnings("unchecked")
	public static String[] parseJSONArray(String json, String[] outerAttr, String attribute) throws JSONException, IndexOutOfBoundsException {
		JSONObject root = null;
		try {
			root = new JSONObject(json);
		} catch (JSONException e) {
			throw e;
		}
		List<Object> jsonList = null;
		try {
			jsonList = parseJSONArray(root,outerAttr[0]);
		} catch (JSONException | IndexOutOfBoundsException e) {
			throw e;
		}
		ArrayList<String> attributes = new ArrayList<String>();
		HashMap<String,Object> map = null;
		for (int j=0; j<jsonList.size(); j++) {
			if (attribute==null) {
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
				attributes.add((String)map.get(attribute));
			}
		}
		return attributes.toArray(new String[] {});
	}
	
	private static String parseJSONObject(JSONObject obj, String attribute, Class<?> attrType) throws JSONException {
		Object attr = obj.get(attribute);
		if (attrType==Integer.class) {
			return attribute + "=" + Integer.toString((Integer)attr);
		} else if (attrType==String.class) {
			return attribute + "=" + (String)attr;
		} else if (attrType==Boolean.class) {
			return attribute + "=" + (boolean)attr;
		} else
			return null;
	}
	
	private static String parseJSONObjectSilent(JSONObject obj, String attribute, Class<?> attrType) throws JSONException {
		Object attr = obj.get(attribute);
		if (attrType==Integer.class) {
			return Integer.toString((Integer)attr);
		} else if (attrType==String.class) {
			return (String)attr;
		} else if (attrType==Boolean.class) {
			return "" + (boolean)attr;
		} else
			return null;
	}
	
	private static List<Object> parseJSONArray(JSONObject obj, String attribute) throws JSONException {
		JSONArray jsonArray = obj.getJSONArray(attribute);
		return jsonArray.toList();
	}
	
	/** Convert an array into a JSON array.
	 * Converts an array of JSONSerializable objects into a JSON array.
	 * 
	 * @param jsonArray the JSONSerializable array
	 * @param attribute the attribute under which the array is created
	 * @return a JSON object containing the array
	 */
	public static JSONObject toJSONArray(JSONSerializable[] jsonArray, String attribute) {
		JSONObject obj = new JSONObject();
		for (int i=0; i<jsonArray.length; i++)
			obj.append(attribute,jsonArray[i].toJSON());
		return obj;
	}
	
	/** Convert an array into a JSON array.
	 * Converts an array of strings into a JSON array.
	 * 
	 * @param array the String array
	 * @param attribute the attribute under which the array is created
	 * @return a JSON object containing the array
	 */
	public static JSONObject toJSONArray(String[] array, String attribute) {
		JSONObject obj = new JSONObject();
		for (int i=0; i<array.length; i++)
			obj.append(attribute,array[i]);
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
	
	// Instance methods: associated to a Client instance
	
	public CoapResponse getResource(String[] uri, int i) throws URISyntaxException {
		pathManager.change(uri);
		Request request = new Request(Code.GET);
		request.getOptions().addOption(new Option(267,1));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		client.debugStream.out("Sent reading request to CSE", i);
		return client.send(request, Code.GET);
	}
	
	public CoapResponse deleteResource(String[] uri, int i) throws URISyntaxException {
		pathManager.change(uri);
		Request request = new Request(Code.DELETE);
		request.getOptions().addOption(new Option(267,1));
		request.getOptions().addOption(new Option(256,"admin:admin"));
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		client.debugStream.out("Sent reading request to CSE", i);
		return client.send(request, Code.DELETE);
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
	
	public CoapResponse oM2Mput(JSONSerializable content, String[] uri, int i) throws URISyntaxException {
		pathManager.change(uri);
		Request request = null;
		JSONObject obj = null;
		JSONObject root = null;
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
	
	public CoapResponse oM2Mput(JSONObject content, String[] uri, int i) throws URISyntaxException {
		pathManager.change(uri);
		Request request = null;
		JSONObject obj = null;
		JSONObject root = null;
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
	
	public String uri() {
		return pathManager.uri();
	}
	
	public void up() throws URISyntaxException {
		pathManager.up();
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
