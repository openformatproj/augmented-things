package mecs.iot.proj.om2m.dashboard;

import mecs.iot.proj.om2m.Client;

public class CommandList {
	
	protected Client client;
	protected Command[] commands;
	public int numCommands;
	public String[][] text;
	
	public CommandList(Client client) {
		this.client = client;
	}
	
	public Command getCommand(int i) {
		if (i<commands.length)
			return commands[i];
		else
			return null;
	}

}
