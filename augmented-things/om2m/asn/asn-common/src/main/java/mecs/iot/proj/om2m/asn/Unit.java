package mecs.iot.proj.om2m.asn;

import org.eclipse.californium.core.coap.Response;

public interface Unit {
	
	void sendAck(String str);
	Response sendContent(String str);
	String getName();

}
