package mecs.iot.proj.web.direct;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import mecs.iot.proj.om2m.asn.user_direct.OM2MDirectEngine;

@WebServlet(
		asyncSupported = true, 
		urlPatterns = { 
				"/android",
		})
public class AndroidServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String SERV_LOG = "[ANDROID] "; 
    private static final String FAILED_LOG = "Failed to issue command due to: ";
    private static final DirectShell ds = new DirectShell();
    static {
    	new OM2MDirectEngine(ds);
    }
	
    public AndroidServlet() {
        super();
    }

    // NOTA BENE: ogni tanto va controllato l'outasync. Si puo' scegliere di fare un refresh manuale da app
    // (quindi sync) mentre il servlet ogni tanto deve guardare la shell...si fa dopo.
	/**
	 * The GET action contains the serial about which we have to give back infos about. It always considers
	 * JSON requests and some of them are redirected from PUT and DEL
	 * json req1: {"command": "query", "serial": "<serial>"} 	-> questa basta farla una volta
	 * json req2: {"command": "read", "serial": "<serial>"}		-> the most typical
	 * json req3: {"command": "lookout", "serial": "<serial>"}	
	 * json req1: {"command": "link", "sensor": <serial>, "actuator": <serial>, "event": <event>, "action": <action>}
	 * json req2: {"command": "write", "actuator": <serial>, "action": <action>}
	 * json req1: {"command": "rm link", "sensor": <serial>, "actuator": <serial>, "event": <event>, "action": <action>}
	 * json req2: {"command": "rm lookout", "sensor": <serial>}
	 * 
	 * In realta' tutte si possono fare con la get. Tranne le rm se assumiamo che rm sia aggiunto a link e lookout
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String jsonQuery = request.getQueryString();
		if (jsonQuery == null)
			jsonQuery = request.getReader().readLine();
		if (jsonQuery != null) {
			try { 
				JSONObject jsonRequest = new JSONObject(request.getQueryString());
				System.out.println(SERV_LOG+"GET: Received correct json query.");
				System.out.println(SERV_LOG+jsonRequest.toString());
				response.setContentType("application/json");
				
				// 1. prepare json to issue to the shell
				if (jsonRequest.getString("command").equals("query")) {
					String [] options = new String [] {
						jsonRequest.getString("serial")	
					};
					ds.setCommand("query", options);
					String ans = ds.getOut();
					System.out.println(SERV_LOG+"Answer for query: "+ans);
					response.getWriter().println("\"attributes\":\""+ans+"\"");
				}
				if (jsonRequest.getString("command").equals("read")) {
					String [] options = new String [] {
							jsonRequest.getString("serial")
					};
					ds.setCommand("read", options);
					String ans = ds.getOut();
					System.out.println(SERV_LOG+"Answer for read: "+ans);
					response.getWriter().println("\"value\":\""+ans+"\"");
				}
				if (jsonRequest.getString("command").equals("lookout")) {
					String [] options = new String [] {
							jsonRequest.getString("serial")
					};
					ds.setCommand("lookout", options);
					String ans = ds.getOut();
					System.out.println(SERV_LOG+"Answer for lookout: "+ans);
					response.getWriter().println("\"lookout\":\""+ans+"\"");
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
					response.getWriter().println("\"linked\":\""+ans+"\"");
				}
				if (jsonRequest.getString("command").equals("write")) {
					String [] options = new String [] {
							jsonRequest.getString("actuator"),
							jsonRequest.getString("action")
					};
					ds.setCommand("write", options);
					String ans = ds.getOut();
					System.out.println(SERV_LOG+"Answer for write action: "+ans);
					response.getWriter().println("\"written\":\""+ans+"\"");
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
					response.getWriter().println("\"unlinked\":\""+ans+"\"");
				}
				if (jsonRequest.getString("command").equals("rm lookoout")) {
					String [] options = new String [] {
							jsonRequest.getString("serial")
					};
					ds.setCommand("rm lookout", options);
					String ans = ds.getOut();
					System.out.println(SERV_LOG+"Answer for lookout removal: "+ans);
					response.getWriter().println("\"unlooked\":\""+ans+"\"");
				}
			}
			catch (Exception e) {
				System.out.println(SERV_LOG+"Error during json casting: "+e.getMessage());
				response.getWriter().println("{\"error\": \"parser_error\"}");
			}
		}
		else {
			System.out.println(SERV_LOG+"Bad query!!");
			response.getWriter().println("{\"error\": \"bad_query\"}");
		}
		
		// 2. experimental response
//		response.setContentType("application/json");
//		response.getWriter().println("{\"test\": \"received\"}");
	}

	/**
	 * The POST action asks the server to find onto om2m the middle node to which it talks. In order to do that, the
	 * servlet leaves in the shell the serial that android sent. As a response, it returns the name of the connected 
	 * middle node.
	 * 
	 * POST waits for a json like: {"serial":"0xsomeserial"}
	 * Serial parsing is not interesting for us. 
	 * After om2m connection, some actions are pretty similar to GET, but rendered in a aggregated way.
	 */
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject json = null;
		String node_mn, node_name, node_attrs;
		String serial = request.getReader().readLine();
		// NOTA: la query e' usata da ajax. Il contenuto da android. CHE PALLE
//		System.out.println(SERV_LOG+"QUERY: "+serial);
//		System.out.println(SERV_LOG+"Other: "+request.getReader().readLine());
		if (serial == null) {
			System.out.println(SERV_LOG+"Received invalid query.");
			// invalidate response
			response.getWriter().println("{\"error\": \"invalid_query\"}");
		}
		else {
			serial = (new JSONObject(serial).getString("serial"));
			// good query: 0. verify the serial is present in database
			if (/*!findSerialInDB(serial)*/false)
			{
				System.out.println(SERV_LOG+"unknown serial.");
				// invalidate response
				response.getWriter().println("{\"error\": \"unknonw_serial\"}");
			}
			else {
				// 1. register: theoretically, once the mn is set, user_direct never blocks again on setSerial
				ds.setSerial(serial);
				// 2. ask name 
				ds.setCommand("mn", null);
				node_mn = ds.getOut();

				// FROM HERE ON, IT COULD BE WORK OF DO_GET
				// 3. ask infos about the node: its name/type and its attributes
				ds.setCommand("name", new String[] {serial});
				node_name = ds.getOut();
				ds.setCommand("query", new String[] {serial});
				node_attrs = ds.getOut();
				
				// 4. prepare json response
				response.setContentType("application/json");
				try {
					json = new JSONObject();
					json.put("mn", node_mn);
					// node_name is like <TYPE>: <ID>
					json.put("name", node_name.split(":")[1]);
					json.put("type", node_name.split(":")[0]);
					// node_attr is splittable using ','
					String [] attrs = node_attrs.split(",");
					for (int i = 0; i < attrs.length; i++)
						json.accumulate("attributes", attrs[i]);
					System.out.println(SERV_LOG+"Resulting json: "+json.toString());
					
					// 4b. prepare example response
					String json2 = "{"
							+ "\"mn\": \"greenhouse-MN\","
							+ "\"name\": \"sensor.ilaria\","
							+ "\"type\": \"SENSOR\","
							+ "\"attributes\": ["
							+ "\"event\"]"
							+ "}";
					System.out.println(SERV_LOG+"Should be like: "+json2);
					
					// 5. send response
					response.getWriter().println(json2);				
				}
				catch (Exception e) {
					System.out.println(SERV_LOG+"FATAL: "+e.getMessage());
					// invalidate response
					response.getWriter().println("{\"error\": \""+e.getMessage()+"\"}");
				}
			}
		}
	}

	
	
	/***
	 * PUT: receives jsons about commands link and write, and passes them to the doGet
	 * response from om2m is sync
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * DEL: remove links between 2 signaled nodes or removes a lookout
	 * It passes the casting to doGet
	 * type of answer: sync
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(SERV_LOG+"Received delete request");
		// check query correctness
		if ( !request.getReader().readLine().split("\"")[1].startsWith("rm")) {
			System.out.println(SERV_LOG+"Not a remove request.");
			response.getWriter().println(SERV_LOG+"BAD REQUEST.");
		}
		else 
			doGet(request, response);
	}

//	private boolean findSerialInDB (String serial) {
//		if (serial.split("x").length != 2 || serial.split("x")[1].length() != 4)
//			return false;
//		for (int i = 0; i< Globals.knownCodes.length; i++)
//			if (Globals.knownCodes[i].equals(serial))
//				return true;
//		return false;
//	}
	
}