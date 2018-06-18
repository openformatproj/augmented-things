package mecs.iot.proj.om2m.asn.sensor;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.structures.Tag;

import java.net.URISyntaxException;

public class App 
{
	
	final private static String id = "sensor";
	final private static String host = Constants.getComputerName();
	final private static String context = Constants.context;
	final private static String address = Constants.adnProtocol + Constants.getInAddress() + Constants._inADNPort + "/" + context;
	final private static boolean debug = true;
	
	final private static Tag tag = new Tag(Services.joinIdHost(id,host),"0x0001","tempC",new String[]{"event"});
	final private static int location = 0;
	
	final private static ErrStream errStream = new ErrStream(Services.joinIdHost("main",host));
	
    public static void main( String[] args )
    {
		try {
			final RemoteInterface remote = new RemoteInterface(tag,location,address,context,debug,36.0,0.05,2000);
			remote.start();
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		}
    }
    
}
