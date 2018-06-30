package mecs.iot.proj.om2m.structures;

import java.util.HashMap;

public class Format {
	
	private static HashMap<String,String> formatMap;
	
	static {
		formatMap = new HashMap<String,String>();
		formatMap.put("tempC","°C");
		// TODO: load from ini file
	}
	
	static String get(String type) {
		return formatMap.get(type);
	}
	
	public static String pack(double value, String type) {
		return String.format("%.3f",value) + " " + formatMap.get(type);
	}
	
	public static double unpack(String content, String type) throws NumberFormatException {
		String[] splits = content.split(" ");
		return Double.parseDouble(splits[0]);
	}
	
}
