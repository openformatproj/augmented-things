package mecs.iot.proj.om2m.adn.in;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.Command;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapServer;

public class App
{
	
	final private static String id = Constants.inId;
	final private static String host = Constants.getComputerName();
	
	final private static ErrStream errStream = new ErrStream(Services.joinIdHost("main",host));
	final private static OutStream outStream = new OutStream(Services.joinIdHost("main",host));
	
    public static void main( String[] args )
    {
    	final Console console = new Console(id,host,false);
		try {
			final ADN_IN adn = new ADN_IN(id,host,Constants.context,Constants.context,true,console);
			CoapServer server = new CoapServer(Constants.inADNPort);
			outStream.out1("Adding ADN on " + Constants.adnProtocol + "localhost" + Constants._inADNPort + "/" + adn.getName(), 0);
			server.add(adn);
			outStream.out1_2("done. Starting server");
			server.start();
			outStream.out2("done");
			Command exit = (s) -> {console.terminate(); server.destroy(); adn.client.destroy(); return "Exiting";};
			console.add("exit",exit,"Terminate this adn","exit");
			console.start();
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		}
    }
    
}
