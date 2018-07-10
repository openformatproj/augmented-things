package mecs.iot.proj.om2m.structures;

import mecs.iot.proj.om2m.dashboard.DebugStream;
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
	final private static String csePostfix;

	final public static String inADNRoot;
	final public static String mnADNRoot;
	
	final public static String inId;
	
	final public static int streamCharacters;
	
	final public static String remotePath = "http://thingstalk.altervista.org/augmented-things/configuration";
	
	final public static String newLine = "//";
	final public static String tab = "/.";
	
	private static Configuration asn = null;
	private static Configuration adn = null;

	static {
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
					conf = new Configuration (remotePath+"/config.ini",Pack.REMOTE,Type.INI);
					System.out.println("Found remote configuration file");
				} catch (Exception e2) {
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
			str = conf.getAttribute("mecs.iot.proj.om2m.csePostfix");
		} catch (Exception e) {
			str = "-cse";
		} finally {
			csePostfix = (String) str;
		}
		try {
			str = conf.getAttribute("mecs.iot.proj.om2m.inId");
		} catch (Exception e) {
			str = "augmented-things-IN";
		} finally {
			inId = (String) str;
		}
		try {
			str = Integer.parseInt(conf.getAttribute("mecs.iot.proj.om2m.streamCharacters"));
		} catch (Exception e) {
			str = 120;
		} finally {
			streamCharacters = (int) str;
		}
		//inCSERoot = ":" + Integer.toString(inCSEPort) + "/" + root + "/" + context + "-IN" + csePostfix;
		//mnCSERoot = ":" + Integer.toString(mnCSEPort) + "/" + root + "/" + context + "-MN" + csePostfix;
		inADNRoot = ":" + Integer.toString(inADNPort) + "/" + context;
		mnADNRoot = ":" + Integer.toString(mnADNPort) + "/" + context;
	}
	
	public static String inCSERoot(String id) {
		return ":" + Integer.toString(inCSEPort) + "/" + root + "/" + id + csePostfix;
	}
	
	public static String mnCSERoot(String id) {
		return ":" + Integer.toString(mnCSEPort) + "/" + root + "/" + id + csePostfix;
	}
	
	public static String inCSERoot() {
		return ":" + Integer.toString(inCSEPort) + "/" + root;
	}
	
	public static String mnCSERoot() {
		return ":" + Integer.toString(mnCSEPort) + "/" + root;
	}

	public static String computerName() {
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
	
	private static void loadASN(DebugStream debugStream, int i) {
		if (asn==null) {
			try {
				asn = new Configuration ("/configuration/asn.ini",Pack.JAR,Type.INI);
				debugStream.out("Found local configuration file (ASN)",i);
			} catch (Exception e0) {
				try {
					asn = new Configuration ("../asn-common/src/main/resources/configuration/asn.ini",Pack.MAVEN,Type.INI);
					debugStream.out("Found local configuration file (ASN)",i);
				} catch (Exception e1) {
					try {
						asn = new Configuration (remotePath+"/asn.ini",Pack.REMOTE,Type.INI);
						debugStream.out("Found remote configuration file (ASN)",i);
					} catch (Exception e2) {
						debugStream.out("No configuration files (ASN) found, using default values",i);
					}
				}
			}
		}
	}
	
	private static void loadADN(DebugStream debugStream, int i) {
		if (adn==null) {
			try {
				adn = new Configuration ("/configuration/adn.ini",Pack.JAR,Type.INI);
				debugStream.out("Found local configuration file (ADN)",i);
			} catch (Exception e0) {
				try {
					adn = new Configuration ("../adn-common/src/main/resources/configuration/adn.ini",Pack.MAVEN,Type.INI);
					debugStream.out("Found local configuration file (ADN)",i);
				} catch (Exception e1) {
					try {
						adn = new Configuration (remotePath+"/adn.ini",Pack.REMOTE,Type.INI);
						debugStream.out("Found remote configuration file (ADN)",i);
					} catch (Exception e2) {
						debugStream.out("No configuration files (ADN) found, using default values",i);
					}
				}
			}
		}
	}
	
	public static String inAddressASN(DebugStream debugStream, int i) {
		loadASN(debugStream,i);
		String str = null;
		try {
			str = asn.getAttribute("mecs.iot.proj.om2m.inAddress");
		} catch (Exception e) {
			str = "127.0.0.1";
		}
		// debugStream.out("\tinAddress="+str,i);
		return str;
	}
	
	public static String inAddressADN(DebugStream debugStream, int i) {
		loadADN(debugStream,i);
		String str = null;
		try {
			str = adn.getAttribute("mecs.iot.proj.om2m.inAddress");
		} catch (Exception e) {
			str = "127.0.0.1";
		}
		// debugStream.out("\tinAddress="+str,i);
		return str;
	}
	
	// TODO: retrieve current machine IP (for subscriptions)
	public static String ip(DebugStream debugStream, int i) {
		loadASN(debugStream,i);
		String str = null;
		try {
			str = asn.getAttribute("mecs.iot.proj.om2m.ip");
		} catch (Exception e) {
			str = "127.0.0.1";
		}
		debugStream.out("\tip="+str,i);
		return str;
	}

}
