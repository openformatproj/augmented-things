package mecs.iot.proj.om2m.dashboard;

import mecs.iot.proj.om2m.structures.Severity;

public class ErrStream {
	
	String name;
	
	public ErrStream(String name) {
		Stream.register(name);
		this.name = name;
	}

	public synchronized void out(Exception e, int i, Severity severity) {
		System.err.println(name + "	[" + severity + "]	" + Integer.toString(i) + ") " + e.getMessage());
	}
	
	public synchronized void out(String msg, int i, Severity severity) {
		System.err.println(name + "	[" + severity + "]	" + Integer.toString(i) + ") " + msg);
	}

}
