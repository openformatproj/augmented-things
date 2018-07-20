package mecs.iot.proj.om2m.asn.user_direct;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.dashboard.Command;
import mecs.iot.proj.om2m.dashboard.Console;

class CommandList extends mecs.iot.proj.om2m.dashboard.CommandList {
	
	CommandList(Client client, Console console, String id) {
		numCommands = 9;
		commands = new Command[numCommands];
		numOptions = new int[]{1,1,1,1,2,4,4,0,1};
		text = new String[numCommands][3];
		isJSON = new boolean[]{false,false,false,false,false,false,false,false,false};
		commands[0] = (options) -> client.getAttributes(options[0],console);
		commands[1] = (options) -> client.getResource(options[0],console);
		commands[2] = (options) -> client.postSubscription(id,options[0],console);
		commands[3] = (options) -> client.removeSubscription(id,options[0],console);
		commands[4] = (options) -> client.putResource(options[0],options[1],console);
		commands[5] = (options) -> client.postSubscription(id,options[0],options[1],options[2],options[3],console);
		commands[6] = (options) -> client.removeSubscription(id,options[0],options[1],options[2],options[3],console);
		commands[7] = (options) -> client.getMN(console);
		commands[8] = (options) -> client.getNode(options[0],console);
		text[0] = new String[] {"query","Query the attributes of a node","query -<SERIAL>"};
		text[1] = new String[] {"read","Read the value of a node","read -<SENSOR_SERIAL>"};
		text[2] = new String[] {"lookout","Adds a subscription to a node","lookout -<SENSOR_SERIAL>"};
		text[3] = new String[] {"rm lookout","Removes a subscription from a node","rm lookout -<SENSOR_SERIAL>"};
		text[4] = new String[] {"write","Write an action to a node","write -<ACTUATOR_SERIAL> -<ACTION_LABEL>"};
		text[5] = new String[] {"link","Adds a subscription between two nodes","link -<SENSOR_SERIAL> -<ACTUATOR_SERIAL> -<EVENT_LABEL> -<ACTION_LABEL>"};
		text[6] = new String[] {"rm link","Removes a subscription between two nodes","rm link -<SENSOR_SERIAL> -<ACTUATOR_SERIAL> -<EVENT_LABEL> -<ACTION_LABEL>"};
		text[7] = new String[] {"mn","Query the name of the current middle-node","mn"};
		text[8] = new String[] {"name","Query the name of a node","name -<SERIAL>"};
	}

}
