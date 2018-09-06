package mecs.iot.proj.web.direct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mecs.iot.proj.RegistrationState;
import mecs.iot.proj.om2m.asn.user_direct.OM2MDirectEngine;

/**
 * Servlet implementation class AndroidServlet
 */
@WebServlet(asyncSupported = true, 
			description = "direct interaction", 
			urlPatterns = { "/android" })
public class AndroidServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String SERV_LOG = "[ANDROID] "; 
    private static final DirectShell ds;
    private static final OM2MDirectEngine engine;
    private static final ErrorMessage em;
    static {
    	ds = new DirectShell();
    	em = new ErrorMessage();
    	
    	engine = new OM2MDirectEngine(ds);
    }
	
   
    
    public AndroidServlet() {
        super();
        System.out.println(SERV_LOG+"Servlet is online");
        engine.start();
    }

    // NOTA BENE: COSE CHE MANCANO
    // - corretto shut down (quando il servlet trova un errore irreparabile, cade eccetera) bisognerebbe
    //   terminare correttamente anche l'engine. Quando parte il waitForNotifications rimane bloccato e 
    //   il timeout per la mutua esclusione crea qualche problema con il servlet;
    //	 NOTA: qunando tomcat aggiorna online va in exception: non puo' aggiornare se c'e' un altro thread
    //	 attivo sotto che sta facendo altre cose
    // - gestione delle notifiche -> servlet asincrono + gestione parallela android
    // - retry con la registrazione se fallisce
    // - quell'handling notifications ROMPE I COGLIONI
    // - DOMANDA: un sensore puo' essere collegato a piu' azioni contemporaneamente?
    
	/**
	 * The GET action contains the serial about which we have to give back infos about. It always considers
	 * JSON requests that are redirected from POST, PUT and DEL. That's because GET is never called directly, 
	 * since such request implicitly does never accept meaningful payload.
	 * If the response from server to any command is negative, the JSON object in the response is valid but 
	 * any value is recognizable as 'null' string.
	 * The GET can also process classic "serial: 0xsomeserial" commands, and examines them as POST does in 
	 * order to retrieve general information about new serial found into the MN.
	 * 
	 * json req1: {"command": "query", "serial": "<serial>"} 	
	 * json req2: {"command": "read", "serial": "<serial>"}		
	 * json req3: {"command": "lookout", "serial": "<serial>"}	
	 * json req1: {"command": "link", "sensor": <serial>, "actuator": <serial>, "event": <event>, "action": <action>}
	 * json req2: {"command": "write", "actuator": <serial>, "action": <action>}
	 * json req1: {"command": "rm link", "sensor": <serial>, "actuator": <serial>, "event": <event>, "action": <action>}
	 * json req2: {"command": "rm lookout", "sensor": <serial>}
	 * 
	 * more json: {"command": "mn"}
	 * more json: {"command": "read lk", "serial": "<serial>"}
	 * more json: {"command": "name", "serial": "<serial>"}
	 * more json: {"command": "serial", "serial": "<serial>"} // same post stuff for simple new serials
	 * 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// meaningful content has to be searched inside the request query or body 
		// (it depends on applications: android uses body, while ajax uses query).
		String jsonQuery = request.getQueryString();
		if (jsonQuery == null)
			jsonQuery = request.getReader().readLine();
		if (jsonQuery == null) {
			System.out.println(SERV_LOG+"Bad query.");
			response.getWriter().println(em.BAD_QUERY);
			return;
		}
		try { 
			JSONObject jsonRequest = new JSONObject(jsonQuery);
			System.out.println(SERV_LOG+"Received correct json query: "+jsonRequest.toString());
			response.setContentType("application/json");
			
			// check correctness
			if (!jsonRequest.has("command")) {
				if (jsonRequest.has("serial") && jsonRequest.names().length() == 1) { // relogging
					System.out.println(SERV_LOG+"Accepting client already registered....");
					response.getWriter().println(PostStuff(jsonRequest.getString("serial")));
					return;
				}
				System.out.println(SERV_LOG+"No commands.");
				response.getWriter().println(em.NO_CMD);
				return;
			}
			// prepare json command to issue to the shell
			if (jsonRequest.getString("command").equals("query")) {
				String [] options = new String [] {
					jsonRequest.getString("serial")	
				};
				ds.setCommand("query", options);
				String ans = ds.getOut();
				System.out.println(SERV_LOG+"Answer for query: "+ans);
				response.getWriter().println("{\"attributes\":\""+ans+"\"}");
				return;
			}
			if (jsonRequest.getString("command").equals("read")) {
				String [] options = new String [] {
						jsonRequest.getString("serial")
				};
				ds.setCommand("read", options);
				String ans = ds.getOut();
				System.out.println(SERV_LOG+"Answer for read: "+ans);
				response.getWriter().println("{\"value\":\""+ans+"\"}");
				return;
			}
			if (jsonRequest.getString("command").equals("lookout")) {
				String [] options = new String [] {
						jsonRequest.getString("serial")
				};
				ds.setCommand("lookout", options);
				String ans = ds.getOut();
				System.out.println(SERV_LOG+"Answer for lookout: "+ans);
				response.getWriter().println("{\"lookout\":\""+ans+"\"}");
				return;
			}
			if (jsonRequest.getString("command").equals("link")) {
				String [] options = new String [] {
						jsonRequest.getString("sensor"),
						jsonRequest.getString("actuator"),
						jsonRequest.getString("event"),
						jsonRequest.getString("action")
				};
				ds.setCommand("link", options);
				String ans = ds.getOut();
				System.out.println(SERV_LOG+"Answer for link creation: "+ans);
				response.getWriter().println("{\"linked\":\""+ans+"\"}");
				return;
			}
			if (jsonRequest.getString("command").equals("write")) {
				String [] options = new String [] {
						jsonRequest.getString("actuator"),
						jsonRequest.getString("action")
				};
				ds.setCommand("write", options);
				String ans = ds.getOut();
				System.out.println(SERV_LOG+"Answer for write action: "+ans);
				response.getWriter().println("{\"written\":\""+ans+"\"}");
				return;
			}
			if (jsonRequest.getString("command").equals("rm link")) {
				String [] options = new String [] {
						jsonRequest.getString("sensor"),
						jsonRequest.getString("actuator"),
						jsonRequest.getString("event"),
						jsonRequest.getString("action")
				};
				ds.setCommand("rm link", options);
				String ans = ds.getOut();
				System.out.println(SERV_LOG+"Answer for link removal: "+ans);
				response.getWriter().println("{\"unlinked\":\""+ans+"\"}");
				return;
			}
			if (jsonRequest.getString("command").equals("rm lookout")) {
				String [] options = new String [] {
						jsonRequest.getString("serial")
				};
				ds.setCommand("rm lookout", options);
				String ans = ds.getOut();
				System.out.println(SERV_LOG+"Answer for lookout removal: "+ans);
				response.getWriter().println("{\"unlooked\":\""+ans+"\"}");
				return;
			}
			if (jsonRequest.getString("command").equals("mn")) {
				ds.setCommand("mn", null);
				String ans = ds.getOut().split(": ")[1];
				System.out.println(SERV_LOG+"Answer for mn: "+ans);
				response.getWriter().println("{\"mn\":\""+ans+"\"}");
				return;
			}
			if (jsonRequest.getString("command").equals("name")) {
				String [] options = new String [] {
						jsonRequest.getString("serial")
				};
				ds.setCommand("name", options);
				String ans = ds.getOut().split(": ")[1];
				System.out.println(SERV_LOG+"Answer for name: "+ans);
				response.getWriter().println("{\"name\":\""+ans+"\"}");
				return;
			}
			if (jsonRequest.getString("command").equals("serial")) {
				response.getWriter().println(PostStuff(jsonRequest.getString("serial")).toString());
				return;
			}
			if (jsonRequest.getString("command").equals("read lk")) {
				String ans = ds.getOutAsync();
				System.out.println(SERV_LOG+"Answer for async read: "+ans);
				response.getWriter().println("{\"async\":\""+ans+"\"}");
				return;
			}
			// if we are here, the command has not been recognized
			System.out.println(SERV_LOG+"Unrecognized command.");
			response.getWriter().println(em.UNREC_CMD);
		}
		catch (Exception e) {
			System.out.println(SERV_LOG+"Error during casting: "+e.getMessage());
			response.getWriter().println(em.GET_ERR);
		}
	}

	/**
	 * The POST receives 2 types of requests:
	 * 1) if the direct engine is still waiting for valid registration, only json like:
	 * 		{"serial":"0xsomeserial"}
	 *    are accepted. The servlet asks the server to find onto om2m the middle node to which it talks. 
	 *    In order to do that, it leaves in the direct shell the serial that the remote client sent. As a response, 
	 *    it returns the name of the connected middle node, and first information about the serial issued;
	 * 2) after om2m connection, actions are passed to GET and rendered in a aggregated way.
	 * 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String serial; // this it is what I actually use in POST stuff
		
		// 1. if the engine is registered apply "GET stuff", else "POST stuff"
		if (engine.registrationState() == RegistrationState.REGISTERED) {
			// 1a. GET stuff
			doGet(request, response);
			return;
		}	
		// 1b. POST stuff
		// NOTE: the POST procedure, without the registration, can be called more than
		// once to retrieve general information about a simple serial that is given.
		try {
			response.setContentType("application/json");
			
			serial = request.getQueryString();
			if (serial == null)
				serial = request.getReader().readLine();
			if (serial == null) {
				System.out.println(SERV_LOG+"Invalid query.");
				// invalidate response
				response.getWriter().println(em.INVALID_QUERY);
				return;
			}
			JSONObject jsonQuery = new JSONObject(serial);
			if (jsonQuery.has("command")) {
				System.out.println(SERV_LOG+"Server unregistered.");
				response.getWriter().println(em.UNREG);
				return;
			}
			
			serial = (new JSONObject(serial).getString("serial"));
			System.out.println(SERV_LOG+"Received serial to locate: "+serial);
			// 2a. register to om2m
			ds.setSerial(serial);
			// 2b. if the registration ended well, then the state is REGISTERED.
			// We then have to wait until the engine is no more in WAITING state
			if (engine.registrationState() == RegistrationState.UNREGISTERED) {
				System.out.println(SERV_LOG+"Registration failed. Retry...");
				response.getWriter().println(em.REG_FAIL);
				return;
			}
			if (engine.registrationState() == RegistrationState.WAITING) {
				System.out.println(SERV_LOG+"Waiting for registration...");
				response.getWriter().println(em.REG_WAIT);
				return;
			}
			System.out.println(SERV_LOG+"Engine registered.");
			// 2c. call POST stuff
			response.getWriter().println(PostStuff(serial));		
		}
		catch (Exception e) {
			System.out.println(SERV_LOG+"POST error: "+e.getMessage());
			// invalidate response
			response.getWriter().println(em.POST_ERR);
		}		
	}
		
	/***
	 * PUT: receives jsons about any kind of command and passes them to the doGet
	 * response from om2m is sync
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(SERV_LOG+"Received PUT request");
		doPost(request, response);
	}

	/**
	 * DEL: remove links between 2 signaled nodes or removes a lookout
	 * It passes the casting to doGet
	 * type of answer: sync
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(SERV_LOG+"Received DEL request");
		// check query correctness
		if ( !request.getReader().readLine().split("\"")[1].startsWith("rm")) {
			System.out.println(SERV_LOG+"Not a remove request.");
			response.getWriter().println(em.DEL_ERR);
		}
		else 
			doPost(request, response);
	}
	
	/**
	 * app utility for POST stuff
	 */
	protected JSONObject PostStuff(String serial) throws JSONException {
		String node_mn, node_name, node_attrs;
		JSONObject json;
		// 3. ask name 
		ds.setCommand("mn", null);
		node_mn = ds.getOut();
		// 4. ask infos about the node: its name/type and its attributes
		ds.setCommand("name", new String[] {serial});
		node_name = ds.getOut();
		ds.setCommand("query", new String[] {serial});
		node_attrs = ds.getOut();
		
		// 4a. check that no errors occurred 			
		if (node_mn == null || node_name == null || node_attrs == null) {
			System.out.println(SERV_LOG+"OM2M engine failed.");
			return new JSONObject(em.OM2M_FAIL);
		}
		// 4b. prepare json response 
		json = new JSONObject();
		
		json.put("mn", node_mn.split(": ")[1]); 	// node_mn is like MN: <mn_name>
		json.put("name", node_name.split(": ")[1]);	// node_name is like <TYPE>: <ID>
		json.put("type", node_name.split(": ")[0]);
		String [] attrs = node_attrs.split(", ");	// node_attr is splittable using ','
		for (int i = 0; i < attrs.length; i++)
			json.accumulate("attributes", attrs[i]);
		System.out.println(SERV_LOG+"Resulting json:\n\t"+json.toString());
			
		// 4c. prepare example response
//					String json2 = "{"
//							+ "\"mn\": \"greenhouse-MN\","
//							+ "\"name\": \"sensor.ilaria\","
//							+ "\"type\": \"SENSOR\","
//							+ "\"attributes\": ["
//							+ "\"event\"]"
//							+ "}";
//					System.out.println(SERV_LOG+"Should be like: "+json2);

		// 5. send response
		return json;
	}
	
	
}