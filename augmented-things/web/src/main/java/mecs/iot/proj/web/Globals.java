package mecs.iot.proj.web;


public class Globals {
	final public static IndirectShell is = new IndirectShell();
	final public static DirectShell ds = new DirectShell(); // una sola per una user_direct per ogni JVM
	
	final public String[] knownCodes = new String[]  {"0x17","0x28","0xAC","0xB7"};
};
