package mecs.iot.proj.om2m.asn.factory.dashboard;

public interface Interface {
	
	void add(String id, String serial, String type, int actions);
	void start();
	void show(int n);
	void touch(int n, int action);
	void terminate();

}
