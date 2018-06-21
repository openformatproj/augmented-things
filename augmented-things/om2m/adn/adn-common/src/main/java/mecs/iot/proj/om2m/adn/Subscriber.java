package mecs.iot.proj.om2m.adn;

import java.util.HashMap;
import java.util.ArrayList;

public class Subscriber {
	
	private HashMap<String,ArrayList<Reference>> referenceMap;
	private HashMap<String,String> piMap;
	private String lastResource;
	
	public Subscriber() {
		referenceMap = new HashMap<String,ArrayList<Reference>>();
		piMap = new HashMap<String,String>();
	}
	
	public void insert(String sensor, String id, String address) {
		Reference ref = new Reference(sensor,id,address);
		if (referenceMap.containsKey(sensor)) {
			referenceMap.get(sensor).add(ref);
		} else {
			ArrayList<Reference> refs = new ArrayList<Reference>();
			refs.add(ref);
			referenceMap.put(sensor,refs);
		}
		lastResource = sensor;
	}
	
	public void insert(String sensor, String event, String id, String address, String action) {
		Reference ref = new Reference(sensor,event,id,address,action);
		if (referenceMap.containsKey(sensor)) {
			referenceMap.get(sensor).add(ref);
		} else {
			ArrayList<Reference> refs = new ArrayList<Reference>();
			refs.add(ref);
			referenceMap.put(sensor,refs);
		}
		lastResource = sensor;
	}
	
	public boolean containsResource(String resource) {
		return piMap.containsValue(resource);
	}
	
	public boolean containsKey(String pi) {
		return piMap.containsKey(pi);
	}
	
	public void bindToLastResource(String pi) {
		piMap.put(pi,lastResource);
	}
	
	public ArrayList<Reference> get(String pi) {
		return referenceMap.get(piMap.get(pi));
	}

}
