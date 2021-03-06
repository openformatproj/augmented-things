package mecs.iot.proj.om2m.adn.mn;

import mecs.iot.proj.om2m.adn.mn.exceptions.*;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.dashboard.Severity;
import mecs.iot.proj.om2m.dashboard.Command;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.structures.Configuration;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.ConfigurationDirectory;
import mecs.iot.proj.om2m.structures.ConfigurationType;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapServer;

public class App
{
	
	final private static String id;
	
	final private static String host = Constants.computerName();
	final private static boolean debug = true;
	
	static {
		DebugStream debugStream = new DebugStream(Format.joinIdHost("configurator/main",host),debug);
		Configuration name = null;
		String str = null;
		try {
			name = new Configuration ("/configuration/name.ini",ConfigurationDirectory.JAR,ConfigurationType.INI);
			debugStream.out("Found local configuration file (MN)",0);
		} catch (Exception e0) {
			try {
				name = new Configuration ("src/main/resources/configuration/name.ini",ConfigurationDirectory.MAVEN,ConfigurationType.INI);
				debugStream.out("Found local configuration file (MN)",0);
			} catch (Exception e1) {
				try {
					name = new Configuration (Constants.remotePath+"/name.ini",ConfigurationDirectory.REMOTE,ConfigurationType.INI);
					debugStream.out("Found remote configuration file (MN)",0);
				} catch (Exception e2) {
					debugStream.out("No configuration files (MN) found, using default values",0);
				}
			}
		}
		try {
			str = name.getAttribute("mecs.iot.proj.om2m.mnId");
		} catch (Exception e) {
			str = "augmented-things-MN";
		} finally {
			id = str;
		}
	}
	
	final private static ErrStream errStream = new ErrStream(Format.joinIdHost(id+"/main",host));
	final private static OutStream outStream = new OutStream(Format.joinIdHost(id+"/main",host));
	
    public static void main( String[] args )
    {
    	final Console console = new Console(id,host,false,debug);
    	try {
    		final ADN_MN adn = new ADN_MN(id,host,debug,console);
    		CoapServer server = new CoapServer(Constants.mnADNPort);
        	outStream.out1("Adding ADN on \"" + Constants.protocol + "localhost" + Constants.mnADNRoot + "\"", 0);
        	server.add(adn);
        	outStream.out1_2("done. Starting server");
        	server.start();
        	outStream.out2("done");
        	Command exit = (s) -> {console.terminate(); server.destroy(); adn.notificationClient.destroy(); adn.cseClient.destroy(); return "Exiting";};
    		console.add("exit",exit,0,"Terminate this adn","exit",false);
        	console.start();
    	} catch (URISyntaxException | StateCreationException | RegistrationException e) {
    		errStream.out(e,0,Severity.MEDIUM);
		}
    }
    
}
