package mecs.iot.proj.om2m.asn;

import org.eclipse.californium.core.coap.Response;

public interface Unit {
	
	Response send(String str);
	String getName();

}
