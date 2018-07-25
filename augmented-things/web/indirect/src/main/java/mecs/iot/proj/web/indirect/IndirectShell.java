package mecs.iot.proj.web.indirect;


import mecs.iot.proj.Interface;

public class IndirectShell implements Interface {

	
	// questa roba deve cambiare e deve diventare qualcosa di comprensibile PER ME
	// out deve essere: restituisco al servlet un JSON da parsare per esempio
	private String outString;
	private String command;
	
	public IndirectShell() {
		
			
	}
	
	@Override
	// still don't understand why such that 
	public String getSerial() {
		return null;
	}
	
	@Override
	
	public void start() {
		
	}
	
	@Override
	
	public synchronized String in() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return command;
	}
	
	@Override
	
	public void out(String str, boolean isJSON) {
		if (isJSON)
			outString = "{}"; //Services.formatJSON(str).replace(Constants.newLine,"\n").replace(Constants.tab,"   ");
		else
			outString = ""; //str.replace(Constants.newLine,"\n").replace(Constants.tab,"   ");
	}
	
	@Override
	
	public void outAsync(String str, boolean isJSON) {
		if (isJSON)
			outString = "{}"; //Services.formatJSON(str).replace(Constants.newLine,"\n").replace(Constants.tab,"   ");
		else
			outString = ""; //str.replace(Constants.newLine,"\n").replace(Constants.tab,"   ");
	}
	
	@Override
	
	public void terminate() {
		// to do
	}
	
	// queryshell puo' essere usato in concorrenza dal servlet e dalla console
	// cosi' che la wake arriva quando il comando e' pronto
	private synchronized void wake() {
		notify();
	}
	
	// Specific functions to call the commands and they are used ONLY
	// by the servlet in order to notify the Console
	public synchronized void callMNS() {
		command = "mns";
		wake(); // meglio solo notify? e' gia' synchronized!!!
	}
	
	public synchronized void callNODES(String mn_name) {
		if (!mn_name.isEmpty())
			command = "nodes -"+mn_name;
		else
			command = "nodes";
		wake();
	}
	
	public synchronized void callUSERS(String users) {
		if (!users.isEmpty())
			command = "users -"+users;
		else
			command = "users";
		wake();
	}
	
	public String getOutString() {
		return outString;
	}
}
