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
					int commandsFound = sections.length-1;
					int commands = cnt.numOptions;
					if (commandsFound<commands) {
						interf.out(name + " has " + cnt.numOptions + " mandatory number of options to specify",false);
					} else {
						if (commands>0) {
							String[] options = new String[commands];
							for (int i=0; i<commands; i++) {
								options[i] = sections[i+1];
							}
							if (options[0].equals("help")) {
								interf.out(cnt.help, false);
							} else {
								str = cnt.command.execute(options);
								interf.out(str, cnt.isJSON && str.matches("\\{.+\\}"));
							}
						} else if (commandsFound>0) {
							String firstOption = sections[1];
							if (firstOption.equals("help")) {
								interf.out(cnt.help, false);
							}
						} else {
							str = cnt.command.execute(null);
							interf.out(str, cnt.isJSON && str.matches("\\{.+\\}"));
						}
					}
				} else if (name.equals("ls /commands")) {
					for (int i=0; i<commandMap.size(); i++) {
						ArrayList<String> list = new ArrayList<String>(commandMap.keySet());
						String o = "";
						for (String c: list) {
							o += c + ": " + commandMap.get(c).help + "\r\n";
						}
						interf.out(o,false);
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