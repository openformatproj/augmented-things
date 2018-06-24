package mecs.iot.proj.om2m.dashboard;

public interface Interface {
	
	String getSerial();
	void start();
	String in();
	void out(String str);
	void outAsync(String str);
	void terminate();

}
