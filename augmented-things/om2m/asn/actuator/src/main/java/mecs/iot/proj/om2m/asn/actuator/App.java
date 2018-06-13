package mecs.iot.proj.om2m.asn.actuator;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.asn.Action;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Tag;

import java.net.URISyntaxException;

public class App 
{
	
	final private static String baseAddress = Constants.inAddress + Constants._inADNPort; 							// IN address (ADN)
	final private static String context = Constants.context;
	final private static String relAddress = context;
	final private static String address = Constants.adnProtocol + baseAddress + "/" + relAddress;
	final private static String host = Constants.getComputerName();
	
	final private static Tag tag = new Tag(Services.joinIdHost("actuator",host),"0x0002",new String[]{"action1","action2"});
	final private static int location = 0;
	
	final private static Action action1 = () -> {};
	final private static Action action2 = () -> {};
	
	final private static ErrStream errStream = new ErrStream(Services.joinIdHost("main",host));
	
    public static void main( String[] args )
    {
    	RemoteInterface remote = null;
		try {
			remote = new RemoteInterface(tag,address,context,location,2000,true,new Action[]{action1,action2},Constants.getIp(),5690);
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		}
		remote.start();
    }
    
}
