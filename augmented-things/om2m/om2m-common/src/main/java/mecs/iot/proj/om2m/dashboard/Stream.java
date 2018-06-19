package mecs.iot.proj.om2m.dashboard;

import java.util.HashMap;
import java.util.Set;

class Stream {
	
	static private HashMap<Agent,State> agentMap = new HashMap<Agent,State>();
	
	static synchronized void register(Agent agent) {
		agentMap.put(agent,new State(false,false));
	}
	
	static synchronized void print(String msg, Agent caller) {
		Set<Agent> agents = agentMap.keySet();
		boolean hasInterruptedSomeone = false;
		for (Agent a: agents) {
			State s = agentMap.get(a);
			if (!a.equals(caller) && s.isOwner) {
				if (!s.hasBeenInterrupted) {
					hasInterruptedSomeone = true;
					s.hasBeenInterrupted = true;
				}
			}
		}
		if (hasInterruptedSomeone) {
			System.out.print("\n"+msg);
			//agentMap.get(caller).hasInterruptedSomeone = false;
		}
		else
			System.out.print(msg);
		agentMap.get(caller).hasBeenInterrupted = false;
	}
	
	static synchronized void lock(Agent caller) {
		agentMap.get(caller).isOwner = true;
		agentMap.get(caller).hasBeenInterrupted = false;
	}
	
	static synchronized void unlock(Agent caller) {
		agentMap.get(caller).hasBeenInterrupted = false;
		agentMap.get(caller).isOwner = false;
	}
	
	static synchronized boolean hasBeenInterrupted(Agent caller) {
		return agentMap.get(caller).hasBeenInterrupted;
	}
	
	public static void main( String[] args )
    {
		OutStream outStream1 = new OutStream("sensor@ALESSANDRO-K7NR");
		OutStream outStream2 = new OutStream("actuator@ALESSANDRO-K7NR");
		DebugStream debugStream1 = new DebugStream("sensor@ALESSANDRO-K7NR",true);
		DebugStream debugStream2 = new DebugStream("actuator@ALESSANDRO-K7NR",true);
		for (int i=0; i<11; i++) {
			outStream1.out1("Received 192.168.0.0 as MN address, connecting to CSE", i);
			outStream2.out1("Received 192.168.0.0 as MN address, connecting to CSE", i);
			debugStream1.out("Connected to CSE",i);
			debugStream2.out("Connected to CSE",i);
			outStream1.out2("done");
			outStream2.out2("done");
		}
    }

}

class State {
	
	boolean isOwner;
	boolean hasBeenInterrupted;
	
	State(boolean isOwner, boolean hasBeenInterrupted) {
		this.isOwner = isOwner;
		this.hasBeenInterrupted = hasBeenInterrupted;
	}
	
}
