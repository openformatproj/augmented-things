package mecs.iot.proj.om2m.dashboard;

import java.util.HashMap;
//import java.util.concurrent.locks.ReentrantLock;

class Stream {
	
	//static private ReentrantLock l = new ReentrantLock();
	static private HashMap<Agent,State> agentMap = new HashMap<Agent,State>();
	//static Type owner = null;
	static Agent owner = null;
	private static boolean insertNewLine = false;
	//private static boolean[] hasBeenInterrupted = {false,false,false};
	
	static synchronized void register(String name) {
		agentMap.put(new Agent(name,Type.OUT),new State(false));
		agentMap.put(new Agent(name,Type.DEBUG),new State(false));
		agentMap.put(new Agent(name,Type.ERR),new State(false));
	}
	
	static synchronized void print(String msg) {
		
		if (insertNewLine) {
			System.out.print("\n" + msg);
		} else {
			System.out.print(msg);
			// hasBeenInterrupted[owner.ordinal()] = false;
			State state = agentMap.get(owner);
			state.value = false;
		}
		
	}
	
	static synchronized void lock(String name, Type type) {
		//l.lock();
		Agent caller = new Agent(name,type);
		if (owner==null || owner.equals(caller)) {
			insertNewLine = false;
			owner = caller;
		} else {
			insertNewLine = true;
			//hasBeenInterrupted[owner.ordinal()] = true;
			State state = agentMap.get(owner);
			state.value = true;
		}
	}
	
	static synchronized void unlock() {
		owner = null;
		//l.unlock();
	}
	
	static synchronized boolean hasBeenInterrupted(String name, Type type) {
		Agent caller = new Agent(name,type);
		State state = agentMap.get(caller);
		return state.value;
		//return hasBeenInterrupted[caller.ordinal()];
	}

}

class Agent {
	
	String name;
	Type type;
	
	Agent(String name, Type type) {
		this.name = name;
		this.type = type;
	}
	
	@Override
	
	public int hashCode() {
		int hash = name.hashCode()*(type.ordinal()+1);
		return hash;
	}
	
	@Override
	
	public boolean equals(Object obj) {
		Agent agent = (Agent)obj;
		return name.equals(agent.name) && type.equals(agent.type);
	}
	
}

class State {
	
	boolean value;
	
	State(boolean value) {
		this.value = value;
	}
	
}
