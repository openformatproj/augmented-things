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
	final private static String address = Constants.adnProtocol + Constants.getInAddress() + Constants._inADNPort + "/" + Constants.context;
	final private static String ip = Constants.getIp();
	final private static boolean debug = true;
	
	final private static ErrStream errStream = new ErrStream(Services.joinIdHost("main",host));
	
	final private static String getSerial() {	// TODO
		return "0x0001";
	}
	
    public static void main( String[] args )
    {
    	final Console console = new Console(id,host,true,debug);
		try {
			final RemoteInterface remote = new RemoteInterface(id,host,address,Constants.context,debug,console,getSerial(),ip,5690);
			Command exit = (s) -> {console.terminate(); remote.destroy(); remote.terminate(); return "Exiting";};
			console.add("exit",exit,"Terminate this asn","exit");
			remote.start();
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		}
    }

}