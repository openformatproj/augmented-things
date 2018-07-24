package mecs.iot.proj.om2m.adn;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.Tag;

import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class ADN extends CoapResource {
	
	protected String name;
	protected String cseBaseName;
	
	protected OutStream outStream;
	protected DebugStream debugStream;
	protected ErrStream errStream;
	
	protected Console console;
	
	protected int i;
	
	public Client cseClient;

	protected ADN(String cseBaseName, String host, boolean debug, Console console) {
		super(Constants.context);
		setObservable(true);
		this.name = Services.joinIdHost(cseBaseName+"/server",host);
		this.cseBaseName = cseBaseName;
		outStream = new OutStream(name);
		debugStream = new DebugStream(name,debug);
		errStream = new ErrStream(name);
		i = 0;
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
		return !serial.isEmpty();
	}
	
	protected boolean isValidId(String id) {
		// TODO (really needed?)
		return !id.isEmpty();
	}
	
	protected boolean isValidType(String type) {
		return !type.isEmpty() && (Format.contains(type) || type.equals("act"));
	}
	
	protected boolean areValidAttributes(String[] attributes, Integer k) {
		for (int i=0; i<attributes.length; i++) {
			if (attributes[i].isEmpty()) {
				k = new Integer(i);
				return false;
			}
		}
		return true;
	}
	
	protected boolean isValidLocation(String location) {
		if (location.isEmpty())
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
		return !address.isEmpty();
	}
	
	protected boolean isValidContent(String content) {
		// TODO
		return !content.isEmpty();
	}
	
	protected boolean isRecognizedContent(String content, Tag tag) {
		// TODO
		return true;
	}
	
	protected boolean isValidLabel(String label) {
		// TODO: syntax check
		return !label.isEmpty();
	}
	
	protected boolean isRecognizedLabel(String label, Tag tag) {
		return tag.ruleMap.containsKey(label);
	}

}
