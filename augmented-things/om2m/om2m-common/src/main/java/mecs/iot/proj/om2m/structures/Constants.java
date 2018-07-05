package mecs.iot.proj.om2m.structures;

import mecs.iot.proj.om2m.structures.Configuration;

//import java.io.IOException;
import java.util.Map;

public class Constants {

	final public static String cseProtocol;
	final public static String adnProtocol;

	final private static int inCSEPort;
	final private static int mnCSEPort;
	final public static int inADNPort;
	final public static int mnADNPort;
	
	final public static String context;
	
	final private static String root;
	final private static String inCSEPostfix;
	final private static String mnCSEPostfix;

	final public static String inCSERoot;
	final public static String mnCSERoot;
	final public static String inADNRoot;
	final public static String mnADNRoot;
	
	final public static int streamCharacters;
	
	private static Configuration asn = null;

	static {
		// TODO: attach System.out to a stream
		Configuration conf = null;
		Object str = null;
		try {
			conf = new Configuration ("/configuration/config.ini",Pack.JAR,Type.INI);
			System.out.println("Found local configuration file");
		} catch (Exception e0) {
			try {
				conf = new Configuration ("../../om2m-common/src/main/resources/configuration/config.ini",Pack.MAVEN,Type.INI);
				System.out.println("Found local configuration file");
			} catch (Exception e1) {
				try {
					conf = new Configuration ("http://thingstalk.altervista.org/augmented-things/configuration/config.ini",Pack.REMOTE,Type.INI);
					System.out.println("Found remote configuration file");
				} catch (Exception e2) {
					//e1.printStackTrace();
					System.out.println("No configuration files found, using default values");
				}
			}
		}
		try {
			str = conf.getAttribute("mecs.iot.proj.om2m.cseProtocol");
		} catch (Exception e) {
			str = "coap://";
		} finally {
			cseProtocol = (String) str;
		}
		try {
			str = conf.getAttribute("mecs.iot.proj.om2m.adnProtocol");
		} catch (Exception e) {
			str = "coap://";
		} finally {
			adnProtocol = (String) str;
		}
		try {
			str = Integer.parseInt(conf.getAttribute("mecs.iot.proj.om2m.inCSEPort"));
		} catch (Exception e) {
			str = 5683;
		} finally {
			inCSEPort = (int) str;
		}
		try {
			str = Integer.parseInt(conf.getAttribute("mecs.iot.proj.om2m.mnCSEPort"));
		} catch (Exception e) {
			str = 5684;
		} finally {
			mnCSEPort = (int) str;
		}
		try {
			str = Integer.parseInt(conf.getAttribute("mecs.iot.proj.om2m.inADNPort"));
		} catch (Exception e) {
			str = 5685;
		} finally {
			inADNPort = (int) str;
		}
		try {
			str = Integer.parseInt(conf.getAttribute("mecs.iot.proj.om2m.mnADNPort"));
		} catch (Exception e) {
			str = 5686;
		} finally {
			mnADNPort = (int) str;
		}
		try {
			str = conf.getAttribute("mecs.iot.proj.om2m.context");
		} catch (Exception e) {
			str = "augmented-things";
		} finally {
			context = (String) str;
		}
		try {
			str = conf.getAttribute("mecs.iot.proj.om2m.root");
		} catch (Exception e) {
			str = "~";
		} finally {
			root = (String) str;
		}
		try {
			str = conf.getAttribute("mecs.iot.proj.om2m.inCSEPostfix");
		} catch (Exception e) {
			str = "-IN-cse";
		} finally {
			inCSEPostfix = (String) str;
		}
		try {
			str = conf.getAttribute("mecs.iot.proj.om2m.mnCSEPostfix");
		} catch (Exception e) {
			str = "-MN-cse";
		} finally {
			mnCSEPostfix = (String) str;
		}
		try {
			str = Integer.parseInt(conf.getAttribute("mecs.iot.proj.om2m.streamCharacters"));
		} catch (Exception e) {
			str = 120;
		} finally {
			streamCharacters = (int) str;
		}
		inCSERoot = ":" + Integer.toString(inCSEPort) + "/" + root + "/" + context + inCSEPostfix;
		mnCSERoot = ":" + Integer.toString(mnCSEPort) + "/" + root + "/" + context + mnCSEPostfix;
		inADNRoot = ":" + Integer.toString(inADNPort) + "/" + context;
		mnADNRoot = ":" + Integer.toString(mnADNPort) + "/" + context;
	}

	public static String getComputerName() {
		Map<String, String> env = System.getenv();
		// Ubuntu systems require to "export HOSTNAME" before running the Java code, otherwise env.containsKey("HOSTNAME")==false
//		try {
//			Process p = Runtime.getRuntime().exec("export HOSTNAME");
//			p.waitFor();
//		} catch (IOException e) {
//			//e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	    if (env.containsKey("COMPUTERNAME"))
	        return env.get("COMPUTERNAME").replaceAll("\\s","_");
	    else if (env.containsKey("HOSTNAME"))
	        return env.get("HOSTNAME").replaceAll("\\s","_");
	    else if (env.containsKey("USER"))
	    	return env.get("USER").replaceAll("\\s","_");
	    else
	        return "unknown_host";
	}
	
	private static void loadASN() {
		if (asn==null) {
			try {
				asn = new Configuration ("/configuration/asn.ini",Pack.JAR,Type.INI);
				System.out.println("Found local configuration file (ASN)");
			} catch (Exception e0) {
				try {
					asn = new Configuration ("../asn-common/src/main/resources/configuration/asn.ini",Pack.MAVEN,Type.INI);
					System.out.println("Found local configuration file (ASN)");
				} catch (Exception e1) {
					try {
						asn = new Configuration ("http://thingstalk.altervista.org/augmented-things/configuration/asn.ini",Pack.REMOTE,Type.INI);
						System.out.println("Found remote configuration file (ASN)");
					} catch (Exception e2) {
						System.out.println("No configuration files found, using default values");
					}
				}
			}
		}
	}
	
	public static String getInAddress() {
		loadASN();
		String str = null;
		try {
			str = asn.getAttribute("mecs.iot.proj.om2m.inAddress");
		} catch (Exception e) {
			str = "127.0.0.1";
		}
		System.out.println("\tinAddress="+str);
		return str;
	}
	
	// TODO: retrieve current machine IP (for subscriptions)
	public static String getIp() {
		loadASN();
		String str = null;
		try {
			str = asn.getAttribute("mecs.iot.proj.om2m.ip");
		} catch (Exception e) {
			str = "localhost";
		}
		System.out.println("\tip="+str);
		return str;
	}

}
