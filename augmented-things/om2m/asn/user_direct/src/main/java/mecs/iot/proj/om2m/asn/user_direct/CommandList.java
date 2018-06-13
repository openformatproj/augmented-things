package mecs.iot.proj.om2m.asn.user_direct;

import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.dashboard.Command;

class CommandList extends mecs.iot.proj.om2m.dashboard.CommandList {
	
	CommandList(Client client, String id) {
		super(client);
		commands = new Command[6];
		commands[0] = (options) -> client.getAttributes(options[0],client.getCount()).getResponseText();
		commands[1] = (options) -> client.getResource(options[0],client.getCount()).getResponseText();
		commands[2] = (options) -> client.postSubscription(id,options[0],client.getCount()).getResponseText();
		commands[3] = (options) -> client.putResource(options[0],options[1],client.getCount()).getResponseText();
		commands[4] = (options) -> client.postSubscription(options[0],options[1],options[2],options[3],client.getCount()).getResponseText();
		//commands[5] = remove; TODO
	}

}
