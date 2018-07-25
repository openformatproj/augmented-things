package mecs.iot.proj.om2m.adn.in;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapServer;

import mecs.iot.proj.Interface;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Command;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;

public class OM2MIndirectEngine {
	
	public OM2MIndirectEngine(Interface interf) {
		
		final String id = Constants.inId;
		final String host = Constants.computerName();
		final boolean debug = true;
		
		final ErrStream errStream = new ErrStream(Services.joinIdHost(id+"/main",host));
		final OutStream outStream = new OutStream(Services.joinIdHost(id+"/main",host));
		
    	final Console console = new Console(id,host,interf,debug);
		try {
			final ADN_IN adn = new ADN_IN(id,host,debug,console);
			CoapServer server = new CoapServer(Constants.inADNPort);
			outStream.out1("Adding ADN on \"" + Constants.protocol + "localhost" + Constants.inADNRoot + "\"", 0);
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
