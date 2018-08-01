package mecs.iot.proj.om2m.adn;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.dashboard.Console;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.ASN;

import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

/** An ADN CoAP resource, used to implement a CoAP server.
 * 
 * @author Alessandro Trifoglio
 * @version 0.0.1-SNAPSHOT
 * @since 0.0.1-SNAPSHOT
*/
public class ADN extends CoapResource {
	
	public Client cseClient;
	public String cseBaseName;
	
	protected OutStream outStream;
	protected DebugStream debugStream;
	protected ErrStream errStream;
	
	protected Console console;
	protected int i;

	protected ADN(String cseBaseName, String host, boolean debug, Console console) {
		super(Constants.context);
		setObservable(true);
		this.cseBaseName = cseBaseName;
		String adnServerName = Format.joinIdHost(cseBaseName+"/server",host);
		outStream = new OutStream(adnServerName);
		debugStream = new DebugStream(adnServerName,debug);
		errStream = new ErrStream(adnServerName);
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
	
	protected boolean isRecognizedContent(String content, ASN tag) {
		// TODO
		return true;
	}
	
	protected boolean isValidLabel(String label) {
		// TODO: syntax check
		return !label.isEmpty();
	}
	
	protected boolean isRecognizedLabel(String label, ASN tag) {
		switch(tag.node) {
			case SENSOR:
				return tag.ruleMap.containsKey(label);
			case ACTUATOR:
				String[] attributes = tag.attributes;
				for (int i=0; i<attributes.length; i++) {
					if (attributes[i].equals(label))
						return true;
				}
				return false;
			default:
				return false;
		}
	}

}
