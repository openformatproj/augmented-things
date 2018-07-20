package mecs.iot.proj.web;

import java.io.IOException;
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
		jsonRequest = new JSONObject(request.getQueryString());
		
//		catch (Exception e) {
//			System.out.println(SERV_LOG+"Bad GET request");
//			response.setStatus(response.SC_BAD_REQUEST);
//		}
		System.out.println("Received: "+jsonRequest.toString());
		
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
		// TODO Auto-generated method stub
		String serial = request.getQueryString();
//		System.out.println(SERV_LOG+"Received query: "+serial);
//		if (serial.split("x")[0].equals("0") == false) {
//			// wrong query
//			response.setStatus(response.SC_BAD_REQUEST);
//		}
//		else {
//			// good query: 1. get registered
//			Globals.ds.setSerial(serial);
//			// 2. ask the name issuing 'mn'
//			Globals.ds.setCommand("mn", null);
//			// 3. then get response: sync
//			Globals.ds.out(null, false); // JUST FOR TEST
//			response.setContentType("application/json");
//			response.getWriter().println(Globals.ds.getOut()); // THE MN NAME
//		}
		response.setContentType("application/json");
		response.getWriter().println("{\"mn\": \"greenhouse-MN\"");
		
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


	
}
