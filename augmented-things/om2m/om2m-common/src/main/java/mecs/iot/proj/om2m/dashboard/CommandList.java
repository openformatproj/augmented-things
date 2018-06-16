package mecs.iot.proj.om2m.dashboard;

public class CommandList {
	
	protected Command[] commands;
	public int numCommands;
	public String[][] text;
	
	public Command getCommand(int i) {
		if (i<commands.length)
			return commands[i];
		else
			return null;
	}

}
