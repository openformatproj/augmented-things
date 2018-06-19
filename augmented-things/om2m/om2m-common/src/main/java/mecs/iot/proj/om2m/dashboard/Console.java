package mecs.iot.proj.om2m.dashboard;

import java.util.ArrayList;
import java.util.HashMap;

import mecs.iot.proj.om2m.Services;

public class Console extends Thread {

	private boolean enabled;
	private boolean executing;
	private HashMap<String,CommandContainer> commandMap;
	public Interface interf;
	private OutStream outStream;
	private DebugStream debugStream;
	private int i;
	
	public Console(String id, String host, boolean enabled, boolean debug) {
		super(Services.joinIdHost(id+"_console",host));
		this.enabled = enabled;
		if (enabled) {
			interf = new Shell(this);
		}
		// scan = new Scanner(System.in);
		executing = true;
		commandMap = new HashMap<String,CommandContainer>();
		outStream = new OutStream(Services.joinIdHost(id+"_console",host));
		debugStream = new DebugStream(Services.joinIdHost(id+"_console",host),debug);
		i = 0;
	}
	
	public Console(String id, String host, Interface interf, boolean debug) {
		super(Services.joinIdHost(id+"_console",host));
		// scan = new Scanner(System.in);
		if (interf!=null) {
			this.enabled = true;
			this.interf = interf;
		} else {
			this.enabled = false;
		}
		executing = true;
		commandMap = new HashMap<String,CommandContainer>();
		outStream = new OutStream(Services.joinIdHost(id+"_console",host));
		debugStream = new DebugStream(Services.joinIdHost(id+"_console",host),debug);
		i = 0;
	}
	
	public void add (String name, Command command, String help, String syntax) {
		CommandContainer container = new CommandContainer(command, help + ". Syntax: " + syntax);
		commandMap.put(name,container);
	}
	
//	public boolean isExecuting() {
//		return executing;
//	}
	
	public void out(String str) {
		debugStream.out(str,i);
	}
	
	public void terminate() {
		executing = false;
	}
	
	@Override
	
	public void run() {
		outStream.out("Starting console", i);
		if (enabled) {
			interf.start();
			while (executing) {
				String str = interf.in();
				i++;
				String[] sections = str.split(" -");
				String name = sections[0];
				if (commandMap.containsKey(name)) {
					int n = sections.length-1;
					if (n>0) {
						String[] options = new String[n];
						for (int i=0; i<n; i++) {
							options[i] = sections[i+1];
						}
						if (options[0].equals("help")) {
							interf.out(commandMap.get(name).help);
						} else {
							interf.out(commandMap.get(name).command.execute(options));
						}
					} else {
						interf.out(commandMap.get(name).command.execute(null));
					}
				} else if (name.equals("ls")) {
					for (int i=0; i<commandMap.size(); i++) {
						ArrayList<String> list = new ArrayList<String>(commandMap.keySet());
						String o = "";
						for (String c: list) {
							o += c + ": " + commandMap.get(c).help + "\r\n";
						}
						interf.out(o);
					}
				} else {
					interf.out(name + " is not a valid command");
				}
			}
			interf.terminate();
			outStream.out("Terminating console", i);
		} else {
			outStream.out("Terminating console (no interface provided)", i);
		}
	}
	
	//TODO (GUI,commands)
//	public void _run() {
//		while (executing) {
//			System.out.print(name+"> ");
//			String command = scan.nextLine();
//			String[] sections = command.split(" ");
//			switch (commandMap.get(sections[0])) {
//			case EXIT:
//				executing = false;
//				System.out.println("exiting");
//				client.destroy();
//				server.destroy();
//				break;
//			case LS:
//				Tag_[] tags = tagMap.values().toArray(new Tag_[0]);
//				String str = "Tags:\n";
//				if (tags!=null && tags.length>0) {
//					for (int i=0; i<tags.length; i++) {
//						str += tags[i].toString() + "\n";
//					}
//				} else {
//					str += "no registered tags yet\n";
//				}
//				if(mnMap!=null) {
//					MN[] mn = mnMap.values().toArray(new MN[0]);
//					str += "MNs:\n";
//					if (mn!=null && mn.length>0) {
//						for (int i=0; i<mn.length; i++) {
//							str += mn[i].toString() + "\n";
//						}
//					} else {
//						str += "no registered MNs\n";
//					}
//				}
//				System.out.print(str);
//				break;
//			default:
//				System.out.println("not valid command");
//				break;
//			}
//		}
//	}

}

class CommandContainer {
	
	Command command;
	String help;
	
	CommandContainer(Command command, String help) {
		this.command = command;
		this.help = help;
	}
	
}