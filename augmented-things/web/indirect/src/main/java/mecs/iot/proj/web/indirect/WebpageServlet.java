package mecs.iot.proj.web.indirect;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import mecs.iot.proj.om2m.adn.in.OM2MIndirectEngine;


/**
 * The servlet manages in mutual exclusion the shell, with which it can send notifications
 * to the in App. 
 * @author ilaria
 */

@WebServlet(asyncSupported = true, 
			description = "indirect interaction", 
			urlPatterns = { "/webpage" })
public class WebpageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SERV_LOG = "[WEBPAGE] ";
	private static final IndirectShell is = new IndirectShell();
	private static final OM2MIndirectEngine engine;

	static {
    	engine = new OM2MIndirectEngine(is);
    }
	
	/** @see HttpServlet#HttpServlet() */
    public WebpageServlet() {
    	super();
    	engine.start();
    	
    }

	/**
	 * REMEMBER: GET does dot receive any data by itself. Then, we shall use POST
	 * Accepted jsons:
	 * {"command":"mns"}
	 * {"command":"nodes", "mn":"<name>"}
	 * {"command":"users", "mn":"<name>"}
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// We have received a json. Let's check: data of ajax is into QUERY		
		String command = request.getQueryString();
		if (command == null) 
			command = request.getReader().readLine();
		if (command == null) {
			System.out.println(SERV_LOG+"No request to fulfill.");
			response.getWriter().println("{\"error\":\"bad_query\"}");
			return;
		}
		System.out.println(SERV_LOG+"Received ajax request: "+command);
			
		String ans = null;
		try {
			response.setContentType("application/json");			
			JSONObject jsonRequest = new JSONObject(command);
			if (!jsonRequest.has("command")) {
				System.out.println(SERV_LOG+"No commands");
				response.getWriter().println("{\"error\":\"no_commands\"}");
				return;
			}
			if (jsonRequest.getString("command").equals("mns")) {
				is.callMNS();
				ans = is.getOut();
				System.out.println(SERV_LOG+"Answer to mns:\n"+ans);
				response.getWriter().println(ans);
				return;
			}
			if (jsonRequest.getString("command").equals("nodes")) {
				is.callNODES("nodes -"+jsonRequest.getString("mn"));
				ans = is.getOut();
				System.out.println(SERV_LOG+"Answer to nodes:\n"+ans);
				response.getWriter().println(ans);
				return;
			}
			if (jsonRequest.getString("command").equals("users")) {
				is.callNODES("users -"+jsonRequest.getString("mn"));
				ans = is.getOut();
				System.out.println(SERV_LOG+"Answer to users:\n"+ans);
				response.getWriter().println(ans);
				return;
			}
			// if we reached here, no valid command is recognized
			System.out.println(SERV_LOG+"Unrecognized command.");
			response.getWriter().println("{\"error\":\"unrecognized_commandd\"}");
		}
		catch (JSONException e) {
			System.out.println(SERV_LOG+e.getMessage());
			response.getWriter().println("{\"error\":\"POST_error_JSON\"}");
		}
		
//		if (ans == null) {
//			System.out.println(SERV_LOG+"Errors occured while executing command...");
//			response.getWriter().println("{\"error\":\"command_failed\"}");
//			return;
//		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println(SERV_LOG+"Redirecting POST...");
		doGet(request, response);
	}

}