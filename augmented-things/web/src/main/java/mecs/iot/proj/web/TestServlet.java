package mecs.iot.proj.web;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.tomcat.util.http.parser.MediaType;
import org.json.JSONObject;


@WebServlet(name = "TestServlet", 
			urlPatterns = { "/test" }, 
			loadOnStartup = 1) // startup order
public class TestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /** @see HttpServlet#HttpServlet() */
    public TestServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/** @see Servlet#init(ServletConfig) */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String json = "{\"name\": \"jsontest\",\"type\":\"jsonobject\"}";
		System.out.println("[TEST] I received a request.");
		response.setContentType("application/json");
		response.getWriter().append(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
