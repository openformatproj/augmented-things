package mecs.iot.proj.om2m.adn.in;

import mecs.iot.proj.om2m.dashboard.Command;
import mecs.iot.proj.om2m.dashboard.Console;

class CommandList extends mecs.iot.proj.om2m.dashboard.CommandList {
	
	CommandList(ADN_IN adn, Console console) {
		numCommands = 4;
		commands = new Command[numCommands];
		numOptions = new int[]{0,1,1,2};
		text = new String[numCommands][3];
		commands[0] = (options) -> adn.cloud.getJSONMNs();
		commands[1] = (options) -> adn.cloud.getJSONNodes(options[0]);
		commands[2] = (options) -> adn.cloud.getJSONUsers(options[0]);
		commands[3] = (options) -> adn.cloud.getJSONSubscriptions(options[0],options[1]);
		text[0] = new String[] {"mns","Query the active mn","mns"};
		text[1] = new String[] {"nodes","Query the nodes attached to a mn","nodes -<MN>"};
		text[2] = new String[] {"users","Query the users attached to a mn","users -<MN>"};
		text[3] = new String[] {"subs","Query the subscriptions active on a given resource","subs -<MN> -<ID>"};
	}

}
