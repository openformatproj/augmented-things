package mecs.iot.proj.om2m.asn.factory;

import mecs.iot.proj.om2m.asn.Action;
import mecs.iot.proj.om2m.asn.Client;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.FactoryInterface;
import mecs.iot.proj.om2m.structures.Configuration;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.Format;
import mecs.iot.proj.om2m.structures.ConfigurationDirectory;
import mecs.iot.proj.om2m.structures.ASN;
import mecs.iot.proj.om2m.structures.ConfigurationType;

import java.net.URISyntaxException;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class Remotes {
	
	static ArrayList<Client> load(String host, String address, String context, boolean debug, String ip, FactoryInterface viewer) throws URISyntaxException {
		
		Configuration conf = null;
		NodeList list = null;
		ArrayList<Client> clients = new ArrayList<Client>();
		
		String id;
		String serial;
		String type;
		NodeList attrList;
		ArrayList<String> attributes;
		int location;
		double value;
		double fluctuation;
		int port;
		long duration;
		long period;
		ASN tag;
		Node node;
		
		int sensors = 0;
		
		DebugStream debugStream = new DebugStream(Format.joinIdHost("configurator/main",host),debug);
		
		try {
			conf = new Configuration ("/configuration/factory.xml",ConfigurationDirectory.JAR,ConfigurationType.XML);
			debugStream.out("Found local factory",0);
		} catch (Exception e0) {
			try {
				conf = new Configuration ("src/main/resources/configuration/factory.xml",ConfigurationDirectory.MAVEN,ConfigurationType.XML);
				debugStream.out("Found local factory",0);
			} catch (Exception e1) {
				try {
					conf = new Configuration (Constants.remotePath+"/factory.xml",ConfigurationDirectory.REMOTE,ConfigurationType.XML);
					debugStream.out("Found remote factory",0);
				} catch (Exception e2) {
					//e1.printStackTrace();
					debugStream.out("No factories found",0);
					return null;
				}
			}
		}
		
		try {
			list = conf.getElements("sensor");
		} catch (Exception e) {
			debugStream.out("No sensors found",0);
		}
		
		for (int i=0; i<list.getLength(); i++) {
			node = list.item(i);
			try {
				id = conf.getAttribute("id",node);
				serial = conf.getTagTextContent("serial",node);
				type = conf.getTagTextContent("type",node);
				attrList = conf.getElements("event",conf.getElements("events",node).item(0));
				attributes = new ArrayList<String>();
				for (int j=0; j<attrList.getLength(); j++) {
					attributes.add(attrList.item(j).getTextContent());
				}
				location = Integer.parseInt(conf.getTagTextContent("location",node));
				value = Double.parseDouble(conf.getTagTextContent("value",node));
				fluctuation = Double.parseDouble(conf.getTagTextContent("fluctuation",node));
				duration = Long.parseLong(conf.getTagTextContent("duration",node));
				period = Long.parseLong(conf.getTagTextContent("period",node));
				tag = new ASN(Format.joinIdHost(id,host),serial,type,attributes.toArray(new String[]{}));
				try {
					mecs.iot.proj.om2m.asn.sensor.RemoteInterface remote = new mecs.iot.proj.om2m.asn.sensor.RemoteInterface(tag,location,address,context,debug,value,fluctuation,duration,period);
					clients.add(remote);
					viewer.add(id,serial,type,0);
					remote.add(viewer,i);
					sensors++;
				} catch (URISyntaxException e) {
					throw e;
				}
			} catch (Exception e) {
				continue;
			}
		}
		
		try {
			list = conf.getElements("actuator");
		} catch (Exception e) {
			debugStream.out("No actuators found",0);
		}
		
		for (int i=0; i<list.getLength(); i++) {
			node = list.item(i);
			try {
				id = conf.getAttribute("id",node);
				serial = conf.getTagTextContent("serial",node);
				attrList = conf.getElements("action",conf.getElements("actions",node).item(0));
				attributes = new ArrayList<String>();
				for (int j=0; j<attrList.getLength(); j++) {
					attributes.add(attrList.item(j).getTextContent());
				}
				location = Integer.parseInt(conf.getTagTextContent("location",node));
				port = Integer.parseInt(conf.getTagTextContent("port",node));
				duration = Integer.parseInt(conf.getTagTextContent("duration",node));
				tag = new ASN(Format.joinIdHost(id,host),serial,attributes.toArray(new String[]{}));
				Action[] callbacks = new Action[attributes.size()];
				for (int j=0; j<callbacks.length; j++) {
					final int n = sensors+i;
					final int action = j;
					callbacks[j] = () -> {viewer.touch(n,action);};
				}
				try {
					mecs.iot.proj.om2m.asn.actuator.RemoteInterface remote = new mecs.iot.proj.om2m.asn.actuator.RemoteInterface(tag,location,address,context,debug,callbacks,ip,port,id,host,duration);
					clients.add(remote);
					viewer.add(id,serial,"act",callbacks.length);
					remote.add(viewer,sensors+i);
				} catch (URISyntaxException e) {
					throw e;
				}
			} catch (Exception e) {
				continue;
			}
		}

		return clients;
		
	}

}

