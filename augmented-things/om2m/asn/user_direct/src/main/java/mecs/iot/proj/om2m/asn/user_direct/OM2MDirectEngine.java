package mecs.iot.proj.om2m.asn.user_direct;

import java.net.URISyntaxException;

import mecs.iot.proj.Interface;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Command;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Severity;

public class OM2MDirectEngine {
	
	private RemoteInterface remote;
	
	public OM2MDirectEngine(Interface interf) {
	
		final String id = "user";
		final String host = Constants.computerName();
		final boolean debug = true;
		
		final DebugStream debugStream = new DebugStream(Services.joinIdHost(id+"/main",host),debug);
		final ErrStream errStream = new ErrStream(Services.joinIdHost(id+"/main",host));
		
		final String context = Constants.context;
		final String address = Constants.protocol + Constants.inAddressASN(debugStream,0) + Constants.inADNRoot;
		final String ip = Constants.ip(debugStream,0);
		
		final Console console = new Console(id,host,interf,debug);
		try {
			remote = new RemoteInterface(id,host,address,context,debug,console,ip,5691);
			Command exit = (s) -> {console.terminate(); remote.terminate(); return "Exiting";};
			console.add("exit",exit,0,"Terminate this asn","exit",false);
		} catch (URISyntaxException e) {
			errStream.out(e,0,Severity.MEDIUM);
		}
	
	}
	
	public void start() {
		remote.start();
	}

}
