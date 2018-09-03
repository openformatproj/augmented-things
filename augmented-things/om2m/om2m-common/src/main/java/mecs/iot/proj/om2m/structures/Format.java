package mecs.iot.proj.om2m.structures;

import mecs.iot.proj.om2m.exceptions.NoTypeException;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;

/** A set of services dealing with formatting tasks.
 * 
 * @author Alessandro Trifoglio
 * @version 0.0.1-SNAPSHOT
 * @since 0.0.1-SNAPSHOT
 */
public class Format {
	
	private static HashMap<String,Metadata> formatMap;
	
	static {
		formatMap = new HashMap<String,Metadata>();
		formatMap.put("tempC",new Metadata("°C",Double.class));
		formatMap.put("pressTorr",new Metadata("mmHg",Double.class));
		formatMap.put("lumLux",new Metadata("lx",Double.class));
		formatMap.put("humPerc",new Metadata("\\%",Double.class));
	}
	
	/** Retrieve the class associated to a sensor's type.
	 * 
	 * @param type the sensor's type
	 * @return the sensors' class ('Double.class', 'String.class'...)
	 * @throws NoTypeException type is not registered
	 */
	public static Class<?> getClass(String type) throws NoTypeException {
		Metadata md = formatMap.get(type);
		if (md!=null) {
			return md.cl;
		} else {
			throw new NoTypeException();
		}
	}
	
	/** Retrieve the class name associated to a sensor's type.
	 * 
	 * @param type the sensor's type
	 * @return the sensors' class simple name ('Double', 'String'...)
	 * @throws NoTypeException type is not registered
	 */
	public static String getClassName(String type) throws NoTypeException {
		Metadata md = formatMap.get(type);
		if (md!=null) {
			return md.cl.getSimpleName();
		} else {
			throw new NoTypeException();
		}
	}
	
	/** Pack a value in a properly formatted string.
	 * 
	 * @param value the value to pack. It must be of the right class: for primitive classes such as 'Double', use the native counterpart (such as 'double')
	 * @param type the sensor's type the given value refers to
	 * @return the formatted string
	 * @throws NoTypeException type is not registered
	 */
	public static String pack(Object value, String type) throws NoTypeException {
		Metadata md = formatMap.get(type);
		String ans = null;
		if (md!=null) {
			switch (md.cl.getSimpleName()) {
				case "Double":
					ans = String.format("%.3f",(double)value) + " " + md.content;
					break;
				default:
					break;
			}
			return ans;
		} else {
			throw new NoTypeException();
		}
	}
	
	/** Unpack a string by retrieving the original value.
	 * 
	 * @param content the string to unpack
	 * @param type the sensor's type the given content refers to
	 * @return the original value: for primitive classes such as 'Double', the native counterpart (such as 'double') is used as return type
	 * @throws ParseException the content is incompatible with the given type
	 * @throws NoTypeException type is not registered
	 */
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
	
	/** Retrieve a random sensors' value. Especially useful for simulations.
	 * 
	 * @param type the sensor's type
	 * @return a random value in its packed form
	 * @throws NoTypeException type is not registered
	 */
	public static String getRandomValue(String type) throws NoTypeException {
		boolean hasType = formatMap.containsKey(type);
		String ans = null;
		if (hasType) {
			switch (type) {
				// TODO: use more realistic distributions
				case "tempC":
					ans = pack(Physics.threshold(36.0*Physics.randomGaussianFluctuation(0.05),Double.valueOf(-273.15),null),"tempC");
					break;
				case "pressTorr":
					ans = pack(Physics.threshold(760.0*Physics.randomGaussianFluctuation(1.0),Double.valueOf(0.0),null),"pressTorr");
					break;
				case "lumLux":
					ans = pack(Physics.threshold(400.0*Physics.randomGaussianFluctuation(0.5),Double.valueOf(0.0),null),"lumLux");
					break;
				case "humPerc":
					ans = pack(Physics.threshold(75.0*Physics.randomGaussianFluctuation(0.1),Double.valueOf(0.0),Double.valueOf(100.0)),"humPerc");
					break;
				default:
					break;
			}
			return ans;
		} else {
			throw new NoTypeException();
		}
	}
	
	/** Answer whether a sensor's type is registered or not.
	 * 
	 * @param type the sensor's type
	 * @return true if the given type is registered, false otherwise
	 */
	public static boolean contains(String type) {
		return formatMap.containsKey(type);
	}
	
	/** Make a name suited to be used as OM2M value.
	 * 
	 * @param name the name to convert
	 * @return the normalized name
	 */
	public static String normalizeName(String name) {
		return name.replace('@','.');
	}
	
	/** Retrieve a formatted string containing the identifier of a task and its host.
	 * 
	 * @param id the task's name
	 * @param host the host's name
	 * @return the formatted string
	 */
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
