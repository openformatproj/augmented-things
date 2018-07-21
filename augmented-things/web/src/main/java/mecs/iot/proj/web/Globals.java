package mecs.iot.proj.web;


public class Globals {
	final public static IndirectShell is = new IndirectShell();
	final public static DirectShell ds = new DirectShell(); 
	
	// this database should be removed and put into the DirectShell (or updated here by the IN too):
	// when a sensor/actuator subscribes, its serial should be added to the list, so that the 
	// servlet can check its presence before leaving the serial into the shell for getSerial() 
	final public static String[] knownCodes = new String[]  {"0x0001", "0x0002"};
};
