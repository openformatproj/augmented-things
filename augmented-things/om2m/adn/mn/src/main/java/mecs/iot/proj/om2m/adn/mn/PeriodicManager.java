package mecs.iot.proj.om2m.adn.mn;

import mecs.iot.proj.om2m.Client;
import mecs.iot.proj.om2m.dashboard.DebugStream;
import mecs.iot.proj.om2m.dashboard.ErrStream;
import mecs.iot.proj.om2m.dashboard.OutStream;
import mecs.iot.proj.om2m.structures.Node;

import java.util.HashMap;
import java.util.LinkedList;

abstract class PeriodicManager extends Thread {
	
	public String name;
	protected Client cseClient;
	protected OutStream outStream;
	protected ErrStream errStream;
	protected DebugStream debugStream;
	protected HashMap<String,NotificationRegister> reg;
	protected ADN_MN mn;
	protected int i;
	
	private LinkedList<NotificationRegister> fifo;
	
	PeriodicManager(String name, ADN_MN mn, boolean debug) {
		this.name = name;
		this.mn = mn;
		outStream = new OutStream(name);
		errStream = new ErrStream(name);
		debugStream = new DebugStream(name,debug);
		reg = new HashMap<String,NotificationRegister>();
		i = 0;
		fifo = new LinkedList<NotificationRegister>();
	}
	
	protected void insert(String id, String key, Node node) {
		switch (node) {
			case SENSOR:
				reg.put(id,new NotificationRegister(this,mn.tagMap.get(key)));
				break;
			case ACTUATOR:
				reg.put(id,new NotificationRegister(this,mn.tagMap.get(key)));
				break;
			case USER:
				reg.put(id,new NotificationRegister(this,mn.userMap.get(key)));
				break;
		}
	}
	
	protected void remove(String id) {
		reg.get(id).terminate();
	}
	
	protected abstract void act(NotificationRegister nr);
	
	void push(NotificationRegister nr) {
		fifo.add(nr);
	}
	
	private NotificationRegister pull() {
		return fifo.poll();
	}
	
	@Override
	public void run() {
		NotificationRegister nr;
		while(true) {
			synchronized(this) {
				nr = pull();
				while (nr==null) {
					await();
					nr = pull();
				}
			}
			act(nr);
		}
	}
	
	private void await() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
