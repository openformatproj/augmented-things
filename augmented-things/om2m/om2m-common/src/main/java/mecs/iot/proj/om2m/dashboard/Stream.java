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
	
	static void register(String thread) {
		agentMap.put(new Agent(thread,Type.OUT),new State(false));
		agentMap.put(new Agent(thread,Type.DEBUG),new State(false));
		agentMap.put(new Agent(thread,Type.ERR),new State(false));
	}
	
	static void print(String msg) {
		
		if (insertNewLine) {
			System.out.print("\n" + msg);
		} else {
			System.out.print(msg);
			// hasBeenInterrupted[owner.ordinal()] = false;
			State state = agentMap.get(owner);
			state.value = false;
		}
		
	}
	
	static void lock(Type type) {
		//l.lock();
		Agent caller = new Agent(Thread.currentThread().getName(),type);
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
	
	static void unlock() {
		owner = null;
		//l.unlock();
	}
	
	static boolean hasBeenInterrupted(Type type) {
		Agent caller = new Agent(Thread.currentThread().getName(),type);
		State state = agentMap.get(caller);
		return state.value;
		//return hasBeenInterrupted[caller.ordinal()];
	}

}

class Agent {
	
	String thread;
	Type type;
	
	Agent(String thread, Type type) {
		this.thread = thread;
		this.type = type;
	}
	
	@Override
	
	public int hashCode() {
		int hash = thread.hashCode()*(type.ordinal()+1);
		return hash;
	}
	
	@Override
	
	public boolean equals(Object obj) {
		Agent agent = (Agent)obj;
		return thread.equals(agent.thread) && type.equals(agent.type);
	}
	
}

class State {
	
	boolean value;
	
	State(boolean value) {
		this.value = value;
	}
	
}
