package mecs.iot.proj.om2m.dashboard;

import java.util.ArrayList;
import java.util.HashMap;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.Interface;

public class Console extends Thread {

	private boolean enabled;
	private boolean executing;
	private HashMap<String,CommandContainer> commandMap;
	public Interface interf;
	private OutStream outStream;
	private DebugStream debugStream;
	private int i;
	
	public Console(String id, String host, boolean enabled, boolean debug) {
		super(Services.joinIdHost(id+"/console",host));
		this.enabled = enabled;
		if (enabled) {
			interf = new Shell(this);
		}
		// scan = new Scanner(System.in);
		executing = true;
		commandMap = new HashMap<String,CommandContainer>();
		outStream = new OutStream(Services.joinIdHost(id+"/console",host));
		debugStream = new DebugStream(Services.joinIdHost(id+"/console",host),debug);
		i = 0;
	}
	
	public Console(String id, String host, Interface interf, boolean debug) {
		super(Services.joinIdHost(id+"/console",host));
		// scan = new Scanner(System.in);
		if (interf!=null) {
			this.enabled = true;
			this.interf = interf;
		} else {
			this.enabled = false;
		}
		executing = true;
		commandMap = new HashMap<String,CommandContainer>();
		outStream = new OutStream(Services.joinIdHost(id+"/console",host));
		debugStream = new DebugStream(Services.joinIdHost(id+"/console",host),debug);
		i = 0;
	}
	
	public void add (String name, Command command, int numOptions, String help, String syntax, boolean isJSON) {
		CommandContainer container = new CommandContainer(command, numOptions, help + ". Syntax: " + syntax, isJSON);
		commandMap.put(name,container);
	}
	
	public String getSerial() {
		return interf.getSerial();
	}
	
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
					CommandContainer cnt = commandMap.get(name);
					int optsFound = sections.length-1;
					if (optsFound>0 && sections[1].equals("help")) {
						interf.out(cnt.help, false);
					} else {
						int opts = cnt.numOptions;
						if (optsFound<opts) {
							interf.out(name + " has a mandatory number of " + opts + " options to specify", false);
						} else {
							String ans = "";
							if (optsFound>opts)
								ans += "Warning: only the first " + opts + " options are considered\r\n";
							if (opts>0) {
								String[] options = new String[opts];
								for (int i=0; i<opts; i++) {
									options[i] = sections[i+1];
								}
								ans += cnt.command.execute(options);
								interf.out(ans, cnt.isJSON && str.matches("\\{.+\\}"));
							} else {
								ans += cnt.command.execute(null);
								interf.out(ans, cnt.isJSON && str.matches("\\{.+\\}"));
							}
						}
					}
				} else if (name.equals("ls /commands")) {
					for (int i=0; i<commandMap.size(); i++) {
						ArrayList<String> list = new ArrayList<String>(commandMap.keySet());
						String ans = "";
						for (String c: list) {
							ans += c + ": " + commandMap.get(c).help + "\r\n";
						}
						interf.out(ans,false);
					}
				} else {
					interf.out(name + " is not a valid command", false);
				}
			}
			interf.terminate();
			outStream.out("Terminating console", i);
		} else {
			outStream.out("Terminating console (no interface provided)", i);
		}
	}
	
	private class CommandContainer {
		
		Command command;
		int numOptions;
		String help;
		boolean isJSON;
		
		CommandContainer(Command command, int numOptions, String help, boolean isJSON) {
			this.command = command;
			this.numOptions = numOptions;
			this.help = help;
			this.isJSON = isJSON;
		}
		
	}

}