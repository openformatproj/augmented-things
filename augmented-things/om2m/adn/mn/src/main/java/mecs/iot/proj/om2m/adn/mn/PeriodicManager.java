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
	protected HashMap<String,NotificationRegister> map;
	protected ADN_MN mn;
	protected int i;
	
	private LinkedList<Content> fifo;
	
	PeriodicManager(String name, ADN_MN mn, boolean debug) {
		this.name = name;
		this.mn = mn;
		outStream = new OutStream(name);
		errStream = new ErrStream(name);
		debugStream = new DebugStream(name,debug);
		map = new HashMap<String,NotificationRegister>();
		i=0;
		fifo = new LinkedList<Content>();
	}
	
	protected void insert(String id, Node node) {
		switch (node) {
			case SENSOR:
				map.put(id,new NotificationRegister(this,mn.tagMap.get(id)));
				break;
			case ACTUATOR:
				map.put(id,new NotificationRegister(this,mn.tagMap.get(id)));
				break;
			case USER:
				map.put(id,new NotificationRegister(this,mn.userMap.get(id)));
				break;
		}
	}
	
	protected void remove(String id) {
		map.get(id).terminate();
	}
	
	protected abstract void act(String id, Node node);
	
	void push(String id, Node node) {
		fifo.add(new Content(id,node));
	}
	
	private Content pull() {
		return fifo.poll();
	}
	
	@Override
	public void run() {
		Content content;
		while(true) {
			synchronized(this) {
				content = pull();
				while (content==null) {
					await();
					content = pull();
				}
			}
			act(content.id,content.node);
		}
	}
	
	private void await() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class Content {
		
		Node node;
		String id;
		
		Content(String id, Node node) {
			this.id = id;
			this.node = node;
		}
		
	}

}
