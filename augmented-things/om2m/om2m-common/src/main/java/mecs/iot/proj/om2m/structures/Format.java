package mecs.iot.proj.om2m.structures;

import java.text.NumberFormat;
import java.text.ParseException;
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
	
	// Only double types currently supported
	
	public static String pack(double value, String type) {
		return String.format("%.3f",value) + " " + formatMap.get(type);
	}
	
	public static double unpack(String content, String type) throws ParseException {
		String[] splits = content.split(" ");
		NumberFormat format = NumberFormat.getInstance();
		return format.parse(splits[0]).doubleValue();
	}
	
	public static String getRandomValue(String type) {
		if (type.equals("tempC"))
			return pack(36.0*Physics.randomGaussianFluctuation(0.05),type);
		else
			return "";
		// TODO: throw a not existing type exception
	}
	
	public static boolean contains(String type) {
		return formatMap.containsKey(type);
	}
	
}
