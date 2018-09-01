package mecs.iot.proj.om2m.dashboard;

public interface FactoryInterface {
	
	void add(String id, String serial, String type, String[] attributes);
	void start();
	void show(int n);
	void touch(int n, String event);
	void touch(int n, int action);
	void terminate();

}
