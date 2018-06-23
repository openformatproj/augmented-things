package mecs.iot.proj.om2m.adn;

import java.util.HashMap;

import mecs.iot.proj.om2m.structures.Node;

import java.util.ArrayList;

public class Subscriber {
	
	private HashMap<String,ArrayList<Reference>> referenceMap;										// resource -> list of references
	private HashMap<String,String> piMap;
	private ArrayList<String> orphanRefs;															// It contains all resources pointing to an empty list of references: they correspond to subscriptions nobody is really subscribed to, and that must therefore be removed
	private String lastResource;
	
	public Subscriber() {
		referenceMap = new HashMap<String,ArrayList<Reference>>();
		piMap = new HashMap<String,String>();
		orphanRefs = new ArrayList<String>();
	}
	
	public void insert(String sender, String receiver, String address) {
		Reference ref = new Reference(sender,receiver,address);
		if (referenceMap.containsKey(sender)) {
			referenceMap.get(sender).add(ref);
		} else {
			ArrayList<Reference> refs = new ArrayList<Reference>();
			refs.add(ref);
			referenceMap.put(sender,refs);
		}
		orphanRefs.remove(sender);
		lastResource = sender;
	}
	
	public void insert(String sender, String event, String receiver, String address, String action) {
		Reference ref = new Reference(sender,event,receiver,address,action);
		if (referenceMap.containsKey(sender)) {
			referenceMap.get(sender).add(ref);
		} else {
			ArrayList<Reference> refs = new ArrayList<Reference>();
			refs.add(ref);
			referenceMap.put(sender,refs);
		}
		orphanRefs.remove(sender);
		lastResource = sender;
	}
	
	// TODO: push referenceMap pair (sensor,refs) into CSE for IN-MN synchronization
	
	public boolean containsResource(String sender) {
		return piMap.containsValue(sender);
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
	
	public String getName(String pi) {
		return piMap.get(pi);
	}
	
	public ArrayList<String> orphanRefs() {
		return orphanRefs;
	}
	
	public void remove(String id, Node node) {
		switch(node) {
			case SENSOR:
				referenceMap.remove(id);
				break;
			case ACTUATOR:
			case USER:
				String[] resources = referenceMap.keySet().toArray(new String[]{});
				ArrayList<Reference> refs;
				for (int i=0; i<resources.length; i++) {
					refs = referenceMap.get(resources[i]);
					for (int j=0; j<refs.size(); j++) {
						if (refs.get(j).receiver.equals(id))
							refs.remove(j);
					}
					if (refs.size()==0)
						orphanRefs.add(resources[i]);
				}
				break;
		}
	}
	
	public void removeOrphanRef(int i) {
		orphanRefs.remove(i);															// TODO: seems not to work
	}

}
