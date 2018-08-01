package mecs.iot.proj.om2m.adn.mn;

import mecs.iot.proj.om2m.adn.mn.exceptions.StateCreationException;
import mecs.iot.proj.om2m.dashboard.Severity;
import mecs.iot.proj.om2m.structures.ASN;
import mecs.iot.proj.om2m.structures.Node;

import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

class PeriodicNotifier extends PeriodicManager {
	
	PeriodicNotifier(String name, ADN_MN mn, boolean debug) {
		super(name,mn,debug);
	}
	
	void reset(String id) {
		reg.get(id).update();
	}
	
	protected void act(NotificationRegister nr) {
		debugStream.out("Listener \"" + nr.asn.id + "\" is going to be solicitated", i);
		CoapResponse response_ = null;
		try {
			response_ = forwardNotification(nr.asn.id,nr.asn.address);
		} catch (URISyntaxException e) {
			errStream.out(e,i,Severity.MEDIUM);
		}
		if (response_==null) {
			errStream.out("Unable to send data to \"" + nr.asn.id + "\", timeout expired", i, Severity.LOW);
			delete(nr.asn.id,nr.asn.node);
		}
		i++;
	}
	
	private CoapResponse forwardNotification(String id, String address) throws URISyntaxException {
		mn.notificationClient.stepCount();
		mn.notificationClient.connect(address,false);
		Request request = new Request(Code.GET);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		return mn.notificationClient.send(request, Code.GET);
	}
	
	private void delete(String id, Node node) {
		String[] uri_;
		CoapResponse response_;
		switch (node) {
			case SENSOR:
				break;
			case ACTUATOR:
				ASN tag = mn.tagMap.get(id);
				outStream.out1("Handling removal of node with serial \"" + tag.serial + "\"", i);
				tag.active = false;
				remove(id);
				try {
					mn.subscriber.remove(tag.id,Node.ACTUATOR,i);
				} catch (URISyntaxException | StateCreationException e) {
					errStream.out(e,i,Severity.HIGH);
					outStream.out2("failed");
					i++;
					return;
				}
				uri_ = new String[] {mn.cseBaseName, "state", "tagMap", tag.serial};
				response_ = null;
				cseClient.stepCount();
				try {
					response_ = cseClient.services.oM2Mput(tag.serial,tag,uri_,false,cseClient.getCount());
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
				break;
			case USER:
				ASN user = mn.userMap.get(id);
				outStream.out1("Handling removal of user \"" + id + "\"", i);
				user.active = false;
				remove(id);
				try {
					mn.subscriber.remove(id,Node.USER,i);
				} catch (URISyntaxException | StateCreationException e) {
					errStream.out(e,i,Severity.HIGH);
					outStream.out2("failed");
					i++;
					return;
				}
				uri_ = new String[] {mn.cseBaseName, "state", "userMap", id};
				response_ = null;
				cseClient.stepCount();
				try {
					response_ = cseClient.services.oM2Mput(id,user,uri_,false,cseClient.getCount());
				} catch (URISyntaxException e) {
					errStream.out(e,i,Severity.HIGH);
					outStream.out2("failed");
					i++;
					return;
				}
				if (response_==null) {
					errStream.out("Unable to remove user from CSE, timeout expired", i, Severity.HIGH);
					outStream.out2("failed");
					i++;
					return;
				} else if (response_.getCode()!=ResponseCode.CREATED) {
					errStream.out("Unable to remove user from CSE, response: " + response_.getCode(),
							i, Severity.HIGH);
					outStream.out2("failed");
					i++;
					return;
				}
				break;
		}
		outStream.out2("done");
	}

}
