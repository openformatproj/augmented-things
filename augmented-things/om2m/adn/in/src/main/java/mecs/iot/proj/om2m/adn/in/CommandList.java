package mecs.iot.proj.om2m.adn.in;

import mecs.iot.proj.om2m.dashboard.Command;
import mecs.iot.proj.om2m.dashboard.Console;

class CommandList extends mecs.iot.proj.om2m.dashboard.CommandList {
	
	CommandList(ADN_IN adn, Console console) {
		numCommands = 4;
		commands = new Command[numCommands];
		numOptions = new int[]{0,1,1,1};
		text = new String[numCommands][3];
		commands[0] = (options) -> adn.cloud.getJSONMN();
		commands[1] = (options) -> adn.cloud.getJSONTag(options[0]);
		commands[2] = (options) -> adn.cloud.getJSONUser(options[0]);
		commands[3] = (options) -> adn.cloud.getJSONSubscriptions(options[0]);
		text[0] = new String[] {"mns","Query for the active middle-nodes","mns"};
		text[1] = new String[] {"nodes","Query for the nodes attached to a mn","nodes -<mn>"};
		text[2] = new String[] {"users","Query for the users attached to a mn","users -<mn>"};
		text[3] = new String[] {"subs","Query for the subscriptions active on a mn","subs -<mn>"};
	}

}
