package mecs.iot.proj.om2m.asn.user_direct;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.dashboard.Command;
import mecs.iot.proj.om2m.dashboard.Console;

class CommandList extends mecs.iot.proj.om2m.dashboard.CommandList {
	
	CommandList(Client client, Console console, String id) {
		super(client);
		numCommands = 6;
		commands = new Command[numCommands];
		commands[0] = (options) -> client.getAttributes(options[0],console).getResponseText();
		commands[1] = (options) -> client.getResource(options[0],console).getResponseText();
		commands[2] = (options) -> client.postSubscription(id,options[0],console).getResponseText();
		commands[3] = (options) -> client.removeSubscription(id,options[0],console).getResponseText();
		commands[4] = (options) -> client.putResource(options[0],options[1],console).getResponseText();
		commands[5] = (options) -> client.postSubscription(options[0],options[1],options[2],options[3],console).getResponseText();
		text = new String[numCommands][3];
		text[0] = new String[] {"query","Query the attributes of a node","query -<serial>"};
		text[1] = new String[] {"read","Read the value of a node","read -<serial>"};
		text[2] = new String[] {"lookout","Adds a subscription to a node","lookout -<serial>"};
		text[3] = new String[] {"rm lookout","Removes a subscription from a node","rm lookout -<serial>"};
		text[4] = new String[] {"write","Write an action to a node","write -<serial> -<action>"};
		text[5] = new String[] {"link","Adds a subscription between two nodes","link -<sensor_serial> -<actuator_serial> -<event> -<action>"};
	}

}
