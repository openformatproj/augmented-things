package mecs.iot.proj.om2m.dashboard;

import java.util.concurrent.locks.ReentrantLock;

class Stream {
	
	static private ReentrantLock l = new ReentrantLock();
	static Type owner = null;
	private static boolean insertNewLine = false;
	private static boolean[] hasBeenInterrupted = {false,false,false};
	
	static void print(String msg) {
		
		if (insertNewLine) {
			System.out.print("\n" + msg);
		}
		else {
			System.out.print(msg);
			hasBeenInterrupted[owner.ordinal()] = false;
		}
		
	}
	
	static void lock(Type caller) {
		l.lock();
		if (owner==null || caller==owner) {
			insertNewLine = false;
			owner = caller;
		} else {
			insertNewLine = true;
			hasBeenInterrupted[owner.ordinal()] = true;
		}
	}
	
	static void unlock() {
		owner = null;
		l.unlock();
	}
	
	static boolean hasBeenInterrupted(Type caller) {
		return hasBeenInterrupted[caller.ordinal()];
	}

}
