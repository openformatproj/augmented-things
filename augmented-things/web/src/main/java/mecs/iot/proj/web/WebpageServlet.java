package mecs.iot.proj.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


/**
 * The servlet manages in mutual exclusion the shell, with which it can send notifications
 * to the in App. 
 * @author ilaria
 */
@WebServlet(name = "WebpageServlet", 
			urlPatterns = { "/webpage" },
			loadOnStartup = 1)
public class WebpageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/** @see HttpServlet#HttpServlet() */
    public WebpageServlet() {
    	super();
    }

	/** @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// We have received a json. Let's check: data of ajax is into QUERY
		System.out.println("[OM2M] I received a request.");
		System.out.println("[OM2M] Content type: "+request.getContentType());
		System.out.println("[OM2M] Query: "+request.getQueryString());
		
		String command = request.getQueryString();
		Boolean success = false;
		if (command.equals("mns")) {
			Globals.is.callMNS();
			success = true;
		}
		else {
			String[] commands = command.split(",");
			if (commands.length == 2) { 
				if (commands[0].equals("nodes")) { 
					Globals.is.callNODES(commands[1]);
					success = true;
				}
				if (commands[0].equals("users")) {
					Globals.is.callUSERS(commands[1]);
					success = true;
				}
			}	
		}
		
		// prepare a json example to test
		String json = "{"
				+ "\"nodes\" :["
				+ "{"
					+ "\"mn\":\"augmented-things-MN\","
					+ "\"active\":true,"
					+ "\"attributes\":["
						+ "\"event\""
					+ "],"
					+ "\"id\":\"sensor.alessandro\","
					+ "\"type\":\"tempC\""
				+ "},"
				+ "{"
				+ "\"mn\":\"augmented-things-MN\","
				+ "\"active\":true,"
				+ "\"attributes\":["
					+ "\"action1\","
					+ "\"action2\""
				+ "],"
				+ "\"id\":\"actuator.alessandro\","
				+ "\"type\":\"act\""
				+ "}"
				+ "]}";

		response.setContentType("application/json");
//		response.getWriter().println(Globals.is.getOutString());
		response.getWriter().println(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
