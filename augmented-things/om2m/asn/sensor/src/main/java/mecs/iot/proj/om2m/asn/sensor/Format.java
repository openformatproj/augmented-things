package mecs.iot.proj.om2m.asn.sensor;

import java.util.HashMap;

class Format {
	
	private static HashMap<String,String> formatMap;
	
	static {
		formatMap = new HashMap<String,String>();
		formatMap.put("tempC","°C");
		// TODO: load from ini file
	}
	
	static String get(String str) {
		return formatMap.get(str);
	}
	
}
