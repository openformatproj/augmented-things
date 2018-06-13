package mecs.iot.proj.om2m.structures;

import java.util.Map;

public class Constants {

	final public static String inAddress;

	final public static String cseProtocol;
	final public static String adnProtocol;

	final public static int inCSEPort;
	final public static int mnCSEPort;
	final public static int inADNPort;
	final public static int mnADNPort;

	final public static String context;
	final public static String root;
	final public static String inPostfix;
	final public static String mnPostfix;
	final public static String inCSEPostfix;
	final public static String mnCSEPostfix;

	final public static String inId;
	final public static String mnId;

	final public static int streamCharacters;

	final public static String _inCSEPort;
	final public static String _mnCSEPort;
	final public static String _inADNPort;
	final public static String _mnADNPort;

	final public static String inRoot;
	final public static String mnRoot;
	
	private static Configuration conf;

	static {
		conf = null;
		Object str = null;
		try {
			conf = new Configuration ("../../../../../configuration/config.ini");
			System.out.println("Found local configuration file");
		} catch (Exception e0) {
			try {
				conf = new Configuration ("configuration/config.ini","http://thingstalk.altervista.org/augmented-things");
				System.out.println("Found remote configuration file");
			} catch (Exception e1) {
				//e1.printStackTrace();
				try {
					conf = new Configuration ("../../configuration/config.ini");
					System.out.println("Found local configuration file (Maven)");
				} catch (Exception e2) {
					System.out.println("No configuration files found, using default values");
				}
			}
		}
		try {
			str = conf.loadAttribute("inAddress");
		} catch (Exception e) {
			str = "127.0.0.1";
		} finally {
			inAddress = (String) str;
		}
		try {
			str = conf.loadAttribute("cseProtocol");
		} catch (Exception e) {
			str = "coap://";
		} finally {
			cseProtocol = (String) str;
		}
		try {
			str = conf.loadAttribute("adnProtocol");
		} catch (Exception e) {
			str = "coap://";
		} finally {
			adnProtocol = (String) str;
		}
		try {
			str = Integer.parseInt(conf.loadAttribute("inCSEPort"));
		} catch (Exception e) {
			str = 5683;
		} finally {
			inCSEPort = (int) str;
		}
		try {
			str = Integer.parseInt(conf.loadAttribute("mnCSEPort"));
		} catch (Exception e) {
			str = 5684;
		} finally {
			mnCSEPort = (int) str;
		}
		try {
			str = Integer.parseInt(conf.loadAttribute("inADNPort"));
		} catch (Exception e) {
			str = 5685;
		} finally {
			inADNPort = (int) str;
		}
		try {
			str = Integer.parseInt(conf.loadAttribute("mnADNPort"));
		} catch (Exception e) {
			str = 5686;
		} finally {
			mnADNPort = (int) str;
		}
		try {
			str = conf.loadAttribute("context");
		} catch (Exception e) {
			str = "augmented-things";
		} finally {
			context = (String) str;
		}
		try {
			str = conf.loadAttribute("root");
		} catch (Exception e) {
			str = "~/";
		} finally {
			root = (String) str;
		}
		try {
			str = conf.loadAttribute("inPostfix");
		} catch (Exception e) {
			str = "-IN";
		} finally {
			inPostfix = (String) str;
		}
		try {
			str = conf.loadAttribute("mnPostfix");
		} catch (Exception e) {
			str = "-MN";
		} finally {
			mnPostfix = (String) str;
		}
		try {
			str = conf.loadAttribute("inCSEPostfix");
		} catch (Exception e) {
			str = "-IN-cse";
		} finally {
			inCSEPostfix = (String) str;
		}
		try {
			str = conf.loadAttribute("mnCSEPostfix");
		} catch (Exception e) {
			str = "-MN-cse";
		} finally {
			mnCSEPostfix = (String) str;
		}
		try {
			str = conf.loadAttribute("inId");
		} catch (Exception e) {
			str = "IN-ADN";
		} finally {
			inId = (String) str;
		}
		try {
			str = conf.loadAttribute("mnId");
		} catch (Exception e) {
			str = "MN-ADN";
		} finally {
			mnId = (String) str;
		}
		try {
			str = Integer.parseInt(conf.loadAttribute("streamCharacters"));
		} catch (Exception e) {
			str = 120;
		} finally {
			streamCharacters = (int) str;
		}
		_inCSEPort = ":" + Integer.toString(inCSEPort);
		_mnCSEPort = ":" + Integer.toString(mnCSEPort);
		_inADNPort = ":" + Integer.toString(inADNPort);
		_mnADNPort = ":" + Integer.toString(mnADNPort);
		inRoot = _inCSEPort + "/" + root;
		mnRoot = _mnCSEPort + "/" + root;
	}

	public static String getComputerName() {
		Map<String, String> env = System.getenv();
	    if (env.containsKey("COMPUTERNAME"))
	        return env.get("COMPUTERNAME").replaceAll("\\s","_");
	    else if (env.containsKey("HOSTNAME"))
	        return env.get("HOSTNAME").replaceAll("\\s","_");
	    // Ubuntu systems require to "export HOSTNAME" before running the Java code, otherwise env.containsKey("HOSTNAME")==false
	    else if (env.containsKey("USER"))
	    	return env.get("USER").replaceAll("\\s","_");
	    else
	        return "unknown_host";
	}
	
	// TODO: retrieve current machine IP (for subscriptions)
	public static String getIp() {
		String str = null;
		try {
			str = conf.loadAttribute("ip");
		} catch (Exception e) {
			str = "localhost";
		}
		return str;
	}

}
