package mecs.iot.proj.om2m.asn.user_direct;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Command;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;

import java.net.URISyntaxException;

public class App 
{
	
	final private static String baseAddress = Constants.inAddress + Constants._inADNPort; 							// IN address (ADN)
	final private static String context = Constants.context;
	final private static String relAddress = context;
	final private static String address = Constants.adnProtocol + baseAddress + "/" + relAddress;
	final private static String host = Constants.getComputerName();
	
	final private static String id = "user";
	
	final private static ErrStream errStream = new ErrStream(Services.joinIdHost("main",host));
	
	final private static String getSerial() {	// TODO
		return "0x0001";
	}
	
    public static void main( String[] args )
    {
    	final Console console = new Console(id,host,true);
    	final RemoteInterface remote;
    	RemoteInterface remote_ = null;
		try {
			remote_ = new RemoteInterface(getSerial(),id,host,address,context,true,Constants.getIp(),5690,console);
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		} finally {
			remote = remote_;
		}
		remote.start();
		Command exit = (s) -> {console.terminate(); remote.destroyConnections(); remote.terminate(); return "Exiting";};
		console.add("exit",exit,"Terminate this asn. Syntax: exit");
		console.start();
    }
    
}