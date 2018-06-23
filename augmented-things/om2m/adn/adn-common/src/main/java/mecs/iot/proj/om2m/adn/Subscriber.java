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
	
	public void insert(String resource, String id, String address) {
		Reference ref = new Reference(resource,id,address);
		if (referenceMap.containsKey(resource)) {
			referenceMap.get(resource).add(ref);
		} else {
			ArrayList<Reference> refs = new ArrayList<Reference>();
			refs.add(ref);
			referenceMap.put(resource,refs);
		}
		emptyRefs.remove(resource);
		lastResource = resource;
	}
	
	public void insert(String resource, String event, String id, String address, String action) {
		Reference ref = new Reference(resource,event,id,address,action);
		if (referenceMap.containsKey(resource)) {
			referenceMap.get(resource).add(ref);
		} else {
			ArrayList<Reference> refs = new ArrayList<Reference>();
			refs.add(ref);
			referenceMap.put(resource,refs);
		}
		emptyRefs.remove(resource);
		lastResource = resource;
	}
	
	// TODO: push referenceMap pair (sensor,refs) into CSE for IN-MN synchronization
	
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
	
	public String getName(String pi) {
		return piMap.get(pi);
	}
	
	public ArrayList<String> emptyRefs() {
		return emptyRefs;
	}
	
	public void remove (String receiver) {
		List<String> resources = (List<String>)referenceMap.keySet();
		ArrayList<Reference> refs;
		for (int i=0; i<resources.size(); i++) {
			refs = referenceMap.get(resources.get(i));
			for (int j=0; j<refs.size(); j++) {
				if (refs.get(j).receiver.equals(receiver))
					refs.remove(j);
			}
			if (refs.size()==0)
				emptyRefs.add(resources.get(i));
		}
	}

}
