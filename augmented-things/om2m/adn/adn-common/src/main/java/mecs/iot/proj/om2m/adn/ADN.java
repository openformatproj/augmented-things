package mecs.iot.proj.om2m.adn;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.structures.Tag;

//import java.util.Arrays;
//import java.util.HashSet;
import java.util.List;
//import java.util.Set;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class ADN extends CoapResource {
	
	public Client cseClient;
	public Client notificationClient;
	
	protected String name;
	protected String context;
	
	protected Subscriber subscriber;
	
	protected OutStream outStream;
	protected DebugStream debugStream;
	protected ErrStream errStream;
	
	protected Console console;
	
	protected int i;

	protected ADN(String name, String uri, String context, boolean debug, Console console) {
		super(uri);
		setObservable(true);
		this.name = name;
		this.context = context;
		outStream = new OutStream(name);
		debugStream = new DebugStream(name,debug);
		errStream = new ErrStream(name);
		i = 1;
		this.console = console;
	}

	protected String getUriValue(CoapExchange exchange, String attribute, int index) {
		List<String> query = exchange.getRequestOptions().getUriQuery();
		if (index<query.size()) {
			String q = query.get(index);
			if (attribute.equals(q.substring(0, attribute.length())))
				return q.substring(attribute.length()+1);
			else
				return null;
		} else {
			return null;
		}
	}
	
	protected boolean isValidSerial(String serial) {
		// TODO
		return serial!=null && !serial.isEmpty();
	}
	
	protected boolean isValidId(String id) {
		// TODO (really needed?)
		return id!=null && !id.isEmpty();
	}
	
	protected boolean isValidType(String type) {
		// TODO
		return type!=null && !type.isEmpty();
	}
	
	protected boolean areValidAttributes(String[] attributes, Integer k) {
		for (int i=0; i<attributes.length; i++) {
			if (attributes[i]==null || attributes[i].isEmpty()) {
				k = new Integer(i);
				return false;
			}
		}
		return true;
	}
	
	protected boolean isValidLocation(String location) {
		if (location==null || location.isEmpty())
			return false;
		try {
			Integer.parseInt(location);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	protected boolean isValidAddress(String address) {
		// TODO
		return address!=null && !address.isEmpty();
	}
	
	protected boolean isValidKey(String key) {
		// TODO
		return key!=null && !key.isEmpty();
	}
	
	protected boolean isValidLabel(String label, Tag tag) {
//		Set<String> s = new HashSet<String>(Arrays.asList(tag.labels()));
//		if (s.contains(label))
//			return true;
//		else
//			return false;
		return tag.labelMap.containsKey(label);
	}

}
