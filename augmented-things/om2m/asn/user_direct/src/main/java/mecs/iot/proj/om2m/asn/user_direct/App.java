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
	final private static String id = "user";
	final private static String host = Constants.getComputerName();
	final private static String address = Constants.adnProtocol + Constants.inAddress + Constants._inADNPort + "/" + Constants.context;
	
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
			remote_ = new RemoteInterface(id,host,address,Constants.context,true,console,getSerial(),Constants.getIp(),5690);
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		} finally {
			remote = remote_;
		}
		Command exit = (s) -> {console.terminate(); remote.destroyConnections(); remote.terminate(); return "Exiting";};
		console.add("exit",exit,"Terminate this asn","exit");
		remote.start();
    }
    
}