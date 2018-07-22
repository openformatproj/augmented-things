package mecs.iot.proj.om2m.adn.in;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.Command;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.web.Globals;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapServer;

public class App
{
	
	final private static String id = Constants.inId;
	final private static String host = Constants.computerName();
	final private static boolean debug = true;
	
	final private static ErrStream errStream = new ErrStream(Services.joinIdHost(id+"/main",host));
	final private static OutStream outStream = new OutStream(Services.joinIdHost(id+"/main",host));
	
    public static void main( String[] args )
    {
    	/* Replace true with your interface implementation */
    	final Console console = new Console(id,host,true,debug);
		try {
			final ADN_IN adn = new ADN_IN(id,host,debug,console);
			CoapServer server = new CoapServer(Constants.inADNPort);
			outStream.out1("Adding ADN on " + Constants.protocol + "localhost" + Constants.inADNRoot, 0);
			server.add(adn);
			outStream.out1_2("done. Starting server");
			server.start();
			outStream.out2("done");
			Command exit = (s) -> {console.terminate(); server.destroy(); adn.cseClient.destroy(); return "Exiting";};
			console.add("exit",exit,0,"Terminate this adn","exit",false);
			console.start();
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		}
    }
    
}
