package mecs.iot.proj.web;

import mecs.iot.proj.Interface;

public class DirectShell implements Interface {

	private String serial;
	private String fullcommand;
	private String out, outAsync;
	private boolean outready;
	
	@Override
	public synchronized String getSerial() {
		// deve essere bloccante (sync) perche' e' qui aspetto il primo seriale dal quale
		// capiamo a quale middle node attaccarsi
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return serial;
	}
	
	public synchronized void setSerial(String serial) {
		this.serial = serial;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	// this is used by the app to retrieve the command
	public synchronized String in() {
		try { wait(); }
		catch (InterruptedException e) { e.printStackTrace(); }
		
		return fullcommand;
	}
	
	public synchronized void setCommand(String command, String[] options) {
		// possible commands: query -<serial>, mn, name -<serial>, read -<serial> eccetera
		fullcommand = command;
		if (options != null) {
			for (int i = 0; i < options.length; i++)
				fullcommand.concat(" -"+options[i]);
		}
		notify();
	}

	@Override
	// the string 'str' is what om2m sets!
	public synchronized void out(String str, boolean isJSON) {
		if (isJSON) {
			// capire come Ale traduce la risposta
		}
		else {
			
		}
		
//		if (str.contains("mn_name")) {
//			
//		}
		// per adesso, facciamo un esempio
		out = "{\"mn\": \"greenhouse-MN\"}";
		outready = true;
		notify();
	}

	@Override
	// these are the responses from om2m!! do not use them to write
	public synchronized void outAsync(String str, boolean isJSON) {
		if (isJSON) {
			
		}
		else {
			
		}
		// per adesso, facciamo un esempio
		out = "{\n" + 
				"\"mn\":\"augmented-things-MN\",\n" + 
				"\"subs\":[\n" + 
				"{\n" + 
				"\"receiver\":{\n" + 
				"\"node\":\"ACTUATOR\",\n" + 
				"\"address\":\"coap://127.0.0.1:5690/augmented-things\",\n" + 
				"\"id\":\"actuator.alessandro\"\n" + 
				"},\n" + 
				"\"sender\":{\n" + 
				"\"node\":\"SENSOR\",\n" + 
				"\"id\":\"sensor.alessandro\",\n" + 
				"\"type\":\"tempC\"\n" + 
				"},\n" + 
				"\"action\":\"action1\",\n" + 
				"\"event\":\"event\"\n" + 
				"}\n" + 
				"],\n" + 
				"\"id\":\"sensor.alessandro\"\n" + 
				"}";
		outready = true;
		notify();
	}
	
	public synchronized String getOut() {
//		if (!outready) {
//			try {wait();}
//			catch (InterruptedException e) {e.printStackTrace();}
//		}
		outready = false;
		return out;
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		
	}
	
}
