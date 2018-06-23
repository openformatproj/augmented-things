package mecs.iot.proj.om2m.adn;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;

public class Subscriber {
	
	private HashMap<String,ArrayList<Reference>> referenceMap;
	private HashMap<String,String> piMap;
	private ArrayList<String> emptyRefs;
	private String lastResource;
	
	public Subscriber() {
		referenceMap = new HashMap<String,ArrayList<Reference>>();
		piMap = new HashMap<String,String>();
		emptyRefs = new ArrayList<String>();
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
		emptyRefs.remove(sender);
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
		emptyRefs.remove(sender);
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
	
	public ArrayList<String> emptyRefs() {
		return emptyRefs;
	}
	
	public void remove (String receiver) {
		String[] resources = referenceMap.keySet().toArray(new String[]{});
		ArrayList<Reference> refs;
		for (int i=0; i<resources.length; i++) {
			refs = referenceMap.get(resources[i]);
			for (int j=0; j<refs.size(); j++) {
				if (refs.get(j).receiver.equals(receiver))
					refs.remove(j);
			}
			if (refs.size()==0)
				emptyRefs.add(resources[i]);
		}
	}

}
