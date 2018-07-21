package mecs.iot.proj.web;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;



@WebServlet(
		asyncSupported = true, 
		urlPatterns = { 
				"/android",
		})
public class AndroidServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String SERV_LOG = "[ANDROID] "; 
    private static final String FAILED_LOG = "Failed to issue command due to: ";
	
    public AndroidServlet() {
        super();
    }

    // NOTA BENE: ogni tanto va controllato l'outasync. Si puo' scegliere di fare un refresh manuale da app
    // (quindi sync) mentre il servlet ogni tanto deve guardare la shell...si fa dopo.
	/**
	 * The GET action contains the serial about which we have to give back infos about
	 * GET si aspetta sempre due comandi dal json:
	 * json req1: {"command": "query", "serial": "<serial>"}
	 * json req2: {"command": "read", "serial": "<serial>"}
	 * json req3: {"command": "lookout", "serial": "<serial>"}
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject jsonRequest = null;
		String jsonquery = request.getQueryString();
		if (jsonquery != null) {
			jsonRequest = new JSONObject(request.getQueryString());
			System.out.println(SERV_LOG+"Received correct json query.");
			System.out.println(SERV_LOG+jsonRequest.toString());
		}
		else {
			jsonquery = "{}";
			System.out.println(SERV_LOG+"Bad query!!");
		}
		
//		catch (Exception e) {
//			System.out.println(SERV_LOG+"Bad GET request");
//			response.setStatus(response.SC_BAD_REQUEST);
//		}
//		System.out.println("Received: "+jsonRequest.toString());
		
		response.setContentType("application/json");
		response.getWriter().println("{\"test\": \"received\"}");
	}

	/**
	 * The POST action asks the server to find onto om2m the middle node to which it talks. In order to do that, the
	 * servlet leaves in the shell the serial that android sent. As a response, it returns the name of the connected 
	 * middle node.
	 * 
	 * POST si aspetta un json con: {"serial":"0xsomeserial"}
	 * non ce ne frega nulla di parsarlo. 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject json = null;
		String node_mn, node_name, node_attrs;
		String serial = request.getQueryString();
		if (serial != null && serial.split("x").length == 2) {
			// good query: 0. verify the serial is present in database
			if (!findSerialInDB(serial))
			{
				System.out.println(SERV_LOG+"unknown serial.");
				// invalidate response
				response.getWriter().println(SERV_LOG+FAILED_LOG+"UNKNOWN SERIAL");
			}
			else {
				// 1. register
				Globals.ds.setSerial(serial);
				// 2. ask name 
				Globals.ds.setCommand("mn", null);
				node_mn = Globals.ds.getOut();
				// 3. ask infos about the node: its name/type and its attributes
				Globals.ds.setCommand("name", new String[] {serial});
				node_name = Globals.ds.getOut();
				Globals.ds.setCommand("query", new String[] {serial});
				node_attrs = Globals.ds.getOut();
				
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
							+ "\"event\"],"
							+ "}";
					System.out.println(SERV_LOG+"Should be like: "+json2);
					
					// 5. send response
					response.getWriter().println(json2);				
				}
				catch (Exception e) {
					System.out.println(SERV_LOG+"FATAL: "+e.getMessage());
					// invalidate response
					response.getWriter().println(SERV_LOG+FAILED_LOG+e.getMessage());
				}
			}
		}
		else {
			System.out.println(SERV_LOG+"Received invalid query.");
			// invalidate response
			response.getWriter().println(SERV_LOG+FAILED_LOG+"INVALID QUERY");
		}
	}
	
	/**
	 * PUT: create a new link between available nodes OR write new action for actuator
	 * response from om2m is sync
	 * json req1: {"command": <link>, "sensor": <serial>, "actuator": <serial>, "event": <event>, "action": <action>}
	 * json req2: {"command": <write>, "actuator": <serial>, "action": <action>}
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(SERV_LOG+"Received POST: "+request.getQueryString());
		// must be set by android the accept on json!
		JSONObject json = new JSONObject(request.getQueryString());
		if (json.getString("command").equals("link")) {
			String [] options = new String [] {
				json.getString("sensor"),
				json.getString("actuator"),
				json.getString("event"),
				json.getString("action")
			};
			Globals.ds.setCommand("nodes link", options);
		}
		if (json.get("command").equals("write")) {
			String [] options = new String [] {
					json.getString("actuator"),
					json.getString("action")
			};
			Globals.ds.setCommand("nodes write", options);
		}
		response.setContentType("application/json");
		response.getWriter().println(Globals.ds.getOut());
	}

	/**
	 * DEL: remove links between 2 signaled nodes
	 * json req: {"sensor": <serial>, "actuator": <serial>, "event": <event>, "action": <action>}
	 * type of answer: sync
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	private boolean findSerialInDB (String serial) {
		for (int i = 0; i< Globals.knownCodes.length; i++)
			if (Globals.knownCodes[i].equals(serial))
				return true;
		return false;
	}
	
}
