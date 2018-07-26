package mecs.iot.proj.om2m.dashboard;

public class ErrStream {
	
	private Agent me;
	private String name;
	
	public ErrStream(String name) {
		me = new Agent(name,Type.ERR);
		Stream.register(me);
		this.name = name;
	}

	public synchronized void out(Exception e, int i, Severity severity) {
		System.err.println(name + "	[" + severity + "]	" + Integer.toString(i) + ") " + e.getMessage());
	}
	
	public synchronized void out(String msg, int i, Severity severity) {
		System.err.println(name + "	[" + severity + "]	" + Integer.toString(i) + ") " + msg);
	}

}
