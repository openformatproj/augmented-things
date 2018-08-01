package mecs.iot.proj.om2m.adn.mn;

import mecs.iot.proj.om2m.adn.mn.exceptions.StateCreationException;
import mecs.iot.proj.om2m.dashboard.Severity;
import mecs.iot.proj.om2m.structures.Node;
import mecs.iot.proj.om2m.structures.ASN;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

class PeriodicityTracker extends PeriodicManager {
	
	PeriodicityTracker(String name, ADN_MN mn, boolean debug) {
		super(name,mn,debug);
	}
	
	void track(String id) {
		reg.get(id).update();
	}
	
	protected void act(NotificationRegister nr) {
		debugStream.out("Resource \"" + nr.asn.id + "\" with period \"" + nr.asn.period + " ms\" has been detected to be inactive", i);
		delete(nr.asn);
		i++;
	}
	
	private void delete(ASN tag) {
		outStream.out1("Handling removal of node with serial \"" + tag.serial + "\"", i);
		tag.active = false;
		remove(tag.id);
		try {
			mn.subscriber.remove(tag.id,Node.SENSOR,i);
		} catch (URISyntaxException | StateCreationException e) {
			errStream.out(e,i,Severity.HIGH);
			outStream.out2("failed");
			i++;
			return;
		}
		String[] uri_ = new String[] {mn.cseBaseName, "state", "tagMap", tag.serial};
		CoapResponse response_ = null;
		mn.cseClient.stepCount();
		try {
			response_ = mn.cseClient.services.oM2Mput(tag.serial,tag,uri_,false,mn.cseClient.getCount());
		} catch (URISyntaxException e) {
			errStream.out(e,i,Severity.HIGH);
			outStream.out2("failed");
			i++;
			return;
		}
		if (response_==null) {
			errStream.out("Unable to remove node from CSE, timeout expired", i, Severity.HIGH);
			outStream.out2("failed");
			i++;
			return;
		} else if (response_.getCode()!=ResponseCode.CREATED) {
			errStream.out("Unable to remove node from CSE, response: " + response_.getCode(),
					i, Severity.HIGH);
			outStream.out2("failed");
			i++;
			return;
		}
		outStream.out2("done");
	}

}
