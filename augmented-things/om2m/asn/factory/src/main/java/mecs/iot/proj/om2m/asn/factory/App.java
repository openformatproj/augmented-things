package mecs.iot.proj.om2m.asn.factory;

import java.net.URISyntaxException;
import java.util.ArrayList;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;
import mecs.iot.proj.om2m.asn.factory.dashboard.Viewer;

public class App
{
	
	final private static String id = "factory";
	final private static String host = Constants.computerName();
	final private static boolean debug = true;
	
	final private static DebugStream debugStream = new DebugStream(Services.joinIdHost(id+"/main",host),debug);
	final private static ErrStream errStream = new ErrStream(Services.joinIdHost(id+"/main",host));
	
	final private static String context = Constants.context;
	final private static String address = Constants.adnProtocol + Constants.inAddressASN(debugStream,0) + Constants.inADNRoot;
	final private static String ip = Constants.ip(debugStream,0);
	
    public static void main( String[] args )
    {
    	Client remote = null;
    	try {
    		Viewer viewer = new Viewer();
	        ArrayList<Client> remotes = Remotes.load(host,address,context,debug,ip,viewer);
	        viewer.start();
	        for (int i=0; i<remotes.size(); i++) {
	        	remote = remotes.get(i);
	        	remote.start();
	        	viewer.show(i);
	        }
    	} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		}
    }
    
}