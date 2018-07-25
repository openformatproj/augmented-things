package mecs.iot.proj.web.indirect;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import mecs.iot.proj.om2m.adn.in.OM2MIndirectEngine;


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

	/** @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// We have received a json. Let's check: data of ajax is into QUERY
		System.out.println(SERV_LOG	+"I received a request.");
		System.out.println(SERV_LOG	+"Content type: "+request.getContentType());
		System.out.println(SERV_LOG	+"Query: "+request.getQueryString());
		
		String command = request.getQueryString();
		Boolean success = false;
		if (command.equals("mns")) {
			is.callMNS();
			success = true;
		}
		else {
			String[] commands = command.split(",");
			if (commands.length == 2) { 
				if (commands[0].equals("nodes")) { 
					is.callNODES(commands[1]);
					success = true;
				}
				if (commands[0].equals("users")) {
					is.callUSERS(commands[1]);
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
//		response.getWriter().println(is.getOutString());
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
