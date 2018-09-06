package mecs.iot.proj.web.indirect;


import mecs.iot.proj.Interface;

public class IndirectShell implements Interface {

	private String serial;
	private String command;
	private String out , outAsync;
	private boolean commandReady = false;	// different lock conditions
	private boolean outReady = false, outAsyncReady = false;
	private final static String SHELL_LOG = "[ISHELL] ";
	private final static String [] ALERTS = { 
			"has a mandatory number of",
			"Error: 4.00",
			"is not a valid command",
			"Error: 5.00"
	};
	
	public IndirectShell() {		
	}
	
	/*
	 */
	@Override
	public String getSerial() {
//		try { wait(); } 
//		catch (InterruptedException e) { 
//			System.out.println(SHELL_LOG+e.getMessage());
//			e.printStackTrace();
//		}
		return serial;
	}
	
	@Override
	
	public void start() {
		
	}
	
	@Override
	// this is used by the app to retrieve the command
	public synchronized String in() {
		while(!commandReady) {
			try { wait(); }
			catch (InterruptedException e) { 
				System.out.println(SHELL_LOG+e.getMessage());
				e.printStackTrace(); 
			}
		}
		commandReady = false;
		return command;
	}
	
	
	@Override
	// the string 'str' is what om2m sets!
	public synchronized void out(String str, boolean isJSON) {
		if (isConsistent(str)) 
			out = str;
		else 
			out = null;
		outReady = true;
		notify();
	}

	@Override
	// these are the responses from om2m!! do not use them to write
	public synchronized void outAsync(String str, boolean isJSON) {
		if (isConsistent(str))
			outAsync = str;
		else
			outAsync = null;
		outAsyncReady = true;
		notify();
	}
	
	// these are called ONLY by the servlet. The shell always returns a "valid" string message
	// The problem is: we have to check if the response is consistent or not. We do it into
	// another function
	public synchronized String getOut() {
		if (!outReady) {
			try {wait();}
			catch (InterruptedException e) {
				System.out.println(SHELL_LOG+e.getMessage());
				e.printStackTrace();
			}
		}
		outReady = false;
		return out;
	}
	
	public synchronized String getOutAsync() {
		if (!outAsyncReady) {
			try {wait();}
			catch (InterruptedException e) {
				System.out.println(SHELL_LOG+e.getMessage());
				e.printStackTrace();
			}
		}
		outAsyncReady = false;
		return outAsync;
	}

	@Override
	public void terminate() {
		// brute force submitting
		outReady = outAsyncReady = commandReady = true;
		notifyAll();
	}
	
	public synchronized void submit() {
		commandReady = true;
		notify();
	}
	
	// Specific functions to call the commands and they are used ONLY
	// by the servlet in order to notify the Console
	public synchronized void callMNS() {
		command = "mns";
		submit(); 
	}
	
	public synchronized void callNODES(String nodecommand) {
		command = nodecommand;
		submit();
	}
	
	public synchronized void callUSERS(String usercommand) {
		command = usercommand;
		submit();
	}

	public boolean isConsistent(String str) {
		for (int i = 0; i < ALERTS.length; i++) {
			if (str.contains(ALERTS[i]))
				return false;
		}
		return true;
	}
}
