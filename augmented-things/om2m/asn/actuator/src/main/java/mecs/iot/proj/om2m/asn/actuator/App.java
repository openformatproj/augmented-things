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
	final private static String id = "actuator";
	final private static String host = Constants.getComputerName();
	final private static String address = Constants.adnProtocol + Constants.inAddress + Constants._inADNPort + "/" + Constants.context;
	
	final private static Tag tag = new Tag(Services.joinIdHost(id,host),"0x0002",new String[]{"action1","action2"});
	final private static int location = 0;
	
	final private static Action action1 = () -> {};
	final private static Action action2 = () -> {};
	
	final private static ErrStream errStream = new ErrStream(Services.joinIdHost("main",host));
	
    public static void main( String[] args )
    {
    	RemoteInterface remote = null;
		try {
			remote = new RemoteInterface(tag,location,address,Constants.context,true,new Action[]{action1,action2},Constants.getIp(),5690,2000);
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		}
		remote.start();
    }
    
}
