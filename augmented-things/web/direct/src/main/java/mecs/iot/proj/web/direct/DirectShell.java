package mecs.iot.proj.web.direct;

import mecs.iot.proj.Interface;

public class DirectShell implements Interface {

	private String serial;
	private String fullcommand = "";
	private String out = "", outAsync = "";
	private boolean commandReady = false;	// different lock conditions
	private boolean outReady = false, outAsyncReady = false;
	private final static String SHELL_LOG = "[DSHELL] ";
	private final static String [] ALERTS = { 
			"has a mandatory number of 1 options to specify",
			"Error: 4.00",
			"is not a valid command",
			"Error: 5.00"
	}; 
	
	@Override
	public synchronized String getSerial() {
		// deve essere bloccante (sync) perche' e' qui aspetto il primo seriale dal quale
		// capiamo a quale middle node attaccarsi
//		isRegistering = true;
		try { wait(); } 
		catch (InterruptedException e) { 
			System.out.println(SHELL_LOG+e.getMessage());
			e.printStackTrace();
		}
		return serial;
	}
	
//	@Override
//	public synchronized void setRegistered(boolean success) {
//		isRegistering = false;
//		this.success = success;
//		notify();
//	}
	
	public synchronized void setSerial(String serial) {
		this.serial = serial;
		// by now, let's assume the registration goes well 
//		setRegistered(true);
		notify();
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
		return fullcommand;
	}
	
	public synchronized void setCommand(String command, String[] options) {
		// possible commands: query -<serial>, mn, name -<serial>, read -<serial> etc
		fullcommand = command;		
		if (options != null) {
//			System.out.println("option len: "+options.length);
			for (int i = 0; i < options.length; i++)
				fullcommand = fullcommand.concat(" -"+options[i]);
		}
		System.out.println(SHELL_LOG+"fullcommand: "+fullcommand);
		commandReady = true;
		notify();
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
//		notify();
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
		// there is no block on the layout async. 
		// if there is no new value, just return the old one
		if (!outAsyncReady) {
//			try {wait();}
//			catch (InterruptedException e) {
//				System.out.println(SHELL_LOG+e.getMessage());
//				e.printStackTrace();
//			}
			return null;
		}
		outAsyncReady = false;
		return outAsync;
	}

	@Override
	public void terminate() {
		// brute force awakening
		outReady = outAsyncReady = commandReady = true;
		notifyAll();
	}
	
	
	public boolean isConsistent(String str) {
		for (int i = 0; i < ALERTS.length; i++) {
			if (str.contains(ALERTS[i]))
				return false;
		}
		return true;
	}	
}

