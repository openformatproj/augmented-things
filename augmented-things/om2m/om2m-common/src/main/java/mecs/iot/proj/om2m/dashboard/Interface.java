package mecs.iot.proj.om2m.dashboard;

public interface Interface {
	
	String getSerial();
	void start();
	String in();
	void out(String str, boolean isJSON);
	void outAsync(String str, boolean isJSON);
	void terminate();

}
