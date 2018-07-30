package mecs.iot.proj.om2m.adn.mn;

import java.util.HashMap;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.structures.ASN;

class PeriodicNotifier extends Thread {
	
private Client cseClient;
	
	private OutStream outStream;
	private ErrStream errStream;
	
	private HashMap<String,ASN> tagMap;																					// id -> tag
	private HashMap<String,ASN> userMap;																				// id -> user
	private HashMap<String,String> serialMap;																			// id -> serial
	
	private Subscriber subscriber;
	
	private String cseBaseName;
	private int i;
	
	PeriodicNotifier(String name, Client cseClient, Subscriber subscriber, String cseBaseName, HashMap<String,ASN> tagMap, HashMap<String,ASN> userMap) {
		super(name);
		this.cseClient = cseClient;
		this.subscriber = subscriber;
		this.cseBaseName = cseBaseName;
		this.userMap = userMap;
		outStream = new OutStream(name);
		errStream = new ErrStream(name);
	}
	
	void insert(String id, String serial) {
		serialMap.put(id,serial);
	}
	
	void insert(String id) {
		// TODO
	}
	
	void track(String id) {
		// TODO
	}
	
	@Override
	public void run() {
		// TODO
	}

}
