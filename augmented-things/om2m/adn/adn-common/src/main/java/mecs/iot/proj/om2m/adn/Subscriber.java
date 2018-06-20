package mecs.iot.proj.om2m.adn;

import java.util.HashMap;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.californium.core.CoapResponse;

import mecs.iot.proj.om2m.Client;

public class Subscriber {
	
	private Client client;
	
	private HashMap<String,ArrayList<Addr>> addressMap;
	
	public Subscriber(Client client) {
		this.client = client;
	}
	
	// TODO
	
	public CoapResponse insert(String adn, String id, String[] uri, String address, int i) throws URISyntaxException {
		if (addressMap.containsKey(id)) {
			addressMap.get(id).add(new Addr(address));
		} else {
			ArrayList<Addr> array = new ArrayList<Addr>();
			array.add(new Addr(address));
			addressMap.put(id,array);
		}
		client.stepCount();
		return client.services.postSubscription(adn,id,uri,client.getCount());
	}
	
	public CoapResponse insert(String adn, String id, String[] uri, String event, String address, String action, int i) throws URISyntaxException {
		if (addressMap.containsKey(id)) {
			addressMap.get(id).add(new Addr(address,action,event));
		} else {
			ArrayList<Addr> array = new ArrayList<Addr>();
			array.add(new Addr(address,action,event));
			addressMap.put(id,array);
		}
		client.stepCount();
		return client.services.postSubscription(adn,id,uri,client.getCount());
	}

}

class Addr {
	
	String address;
	String action;
	String event;
	
	Addr(String address) {
		this.address = address;
		this.action = null;
		this.event = null;
	}
	
	Addr(String address, String action, String event) {
		this.address = address;
		this.action = action;
		this.event = event;
	}
	
}
