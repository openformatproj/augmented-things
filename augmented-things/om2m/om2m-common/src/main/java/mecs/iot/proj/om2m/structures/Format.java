package mecs.iot.proj.om2m.structures;

import mecs.iot.proj.om2m.exceptions.NoTypeException;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;

public class Format {
	
	private static HashMap<String,Metadata> formatMap;
	
	static {
		formatMap = new HashMap<String,Metadata>();
		formatMap.put("tempC",new Metadata("°C",Double.class));
	}
	
	public static Class<?> getClass(String type) throws NoTypeException {
		Metadata md = formatMap.get(type);
		if (md!=null) {
			return md.cl;
		} else {
			throw new NoTypeException();
		}
	}
	
	public static String getClassName(String type) throws NoTypeException {
		Metadata md = formatMap.get(type);
		if (md!=null) {
			return md.cl.getSimpleName();
		} else {
			throw new NoTypeException();
		}
	}
	
	public static String pack(double value, String type) throws NoTypeException {
		Metadata md = formatMap.get(type);
		String ans = null;
		if (md!=null) {
			switch (md.cl.getSimpleName()) {
				case "Double":
					ans = String.format("%.3f",value) + " " + md.content;
					break;
				default:
					break;
			}
			return ans;
		} else {
			throw new NoTypeException();
		}
	}
	
	public static Object unpack(String content, String type) throws ParseException, NoTypeException {
		Metadata md = formatMap.get(type);
		Object ans = null;
		if (md!=null) {
			switch (md.cl.getSimpleName()) {
				case "Double":
					String[] splits = content.split(" ");
					NumberFormat format = NumberFormat.getInstance();
					ans = format.parse(splits[0]).doubleValue();
					break;
				default:
					break;
			}
			return ans;
		} else {
			throw new NoTypeException();
		}
	}
	
	public static String getRandomValue(String type) throws NoTypeException {
		boolean hasType = formatMap.containsKey(type);
		String ans = null;
		if (hasType) {
			switch (type) {
				case "tempC":
					ans = pack(36.0*Physics.randomGaussianFluctuation(0.05),"tempC");
					break;
				default:
					break;
			}
			return ans;
		} else {
			throw new NoTypeException();
		}
	}
	
	public static boolean contains(String type) {
		return formatMap.containsKey(type);
	}
	
	public static String normalizeName(String name) {
		return name.replace('@','.');
	}
	
	public static String joinIdHost(String id, String host) {
		return id + "@" + host;
	}
	
	public static String getKeyFromAttribute(String attr) {
		return attr.split("cnt-")[1];
	}
	
	public static String getPathFromKey(String key) {
		return "cnt-" + key;
	}
	
}

class Metadata {
	
	String content;
	Class<?> cl;
	
	Metadata(String content, Class<?> cl) {
		this.content = content;
		this.cl = cl;
	}
	
}
