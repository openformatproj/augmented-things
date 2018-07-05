package mecs.iot.proj.om2m.asn.actuator;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.asn.Action;
import mecs.iot.proj.om2m.asn.actuator.exceptions.ActionNumberMismatchException;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Tag;

import java.net.URISyntaxException;

public class App 
{
	
	final private static String id = "actuator";
	final private static String host = Constants.computerName();
	final private static String context = Constants.context;
	final private static String address = Constants.adnProtocol + Constants.inAddress() + Constants.inADNRoot;
	final private static String ip = Constants.ip();
	final private static boolean debug = true;
	
	final private static Tag tag = new Tag(Services.joinIdHost(id,host),"0x0002",new String[]{"action1","action2"});
	final private static int location = 0;
	
	final private static Action action1 = () -> {/* System.out.println("*************************"); An action */};
	final private static Action action2 = () -> {};
	
	final private static ErrStream errStream = new ErrStream(Services.joinIdHost(id+"/main",host));
	
    public static void main( String[] args )
    {
		try {
			final RemoteInterface remote = new RemoteInterface(tag,location,address,context,debug,new Action[]{action1,action2},ip,5690,id,host,0);
			remote.start();
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		} catch (ActionNumberMismatchException e) {
			errStream.out(e,0,Severity.MEDIUM);
		}
    }
    
}
