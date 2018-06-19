package mecs.iot.proj.om2m.dashboard;

import mecs.iot.proj.om2m.structures.Constants;

import java.util.ArrayList;
import java.text.MessageFormat;

public class DebugStream {
	
	private Agent me;
	private String name;
	private boolean debug;
	
	final private String pad;
	
	public DebugStream(String name, boolean debug) {
		me = new Agent(name,Type.DEBUG);
		Stream.register(me);
		this.name = name;
		this.debug = debug;
		String str = "";
		for (int i=0; i<name.length(); i++)
			str += " ";
		str += "\t";
		for (int i=0; i<(new String("[DEBUG]")).length(); i++)
			str += " ";
		str += "\t";
		pad = str;
	}
	
	private String pad(int num) {
		String str = pad + "   ";
		if (num>=10) {
			for (int i=0; i<Math.floor(Math.log10(num)); i++)
				str += " ";
		}
		return str;
	}
	
	private boolean isBreakingChar (char c) {
		return c==' ' || c==',' || c=='/';
	}
	
	public void out(String msg, int i) {
		
		if (!debug || msg==null || msg=="")
			return;
		
		String preamble = name + "\t[DEBUG]\t" + Integer.toString(i) + ") ";
		int chunkLength = Constants.streamCharacters - preamble.length();
		ArrayList<String> chunk = new ArrayList<String>();
		int startIndex = 0;
		int endIndex;
		
		while(startIndex<msg.length()) {
			endIndex = Math.min(startIndex+chunkLength,msg.length());
			while (endIndex<msg.length() && !isBreakingChar(msg.charAt(endIndex))) {
				endIndex++;
			}
			if (endIndex<msg.length()) endIndex++;
			chunk.add(msg.substring(startIndex,endIndex));
			startIndex = endIndex;
		}
		
		String str = "";
		
		for (int j=0; j<chunk.size(); j++) {
			if (j==0) {
				str += chunk.get(0);
			} else {
				str += pad(i);
				str += chunk.get(j);
			}
			if (j<chunk.size()-1)
				str += "\n";
		}
		
		Stream.print(preamble+str+"\n",me);
		
	}
	
	public void setDebugState (boolean debug) {
		this.debug = debug;
	}
	
	public boolean getDebugState () {
		return debug;
	}
	
	private String showLocation() {
		StackTraceElement element = Thread.currentThread().getStackTrace()[3];
		return MessageFormat.format("({0}:{1, number,#}) : ",element.getFileName(),element.getLineNumber());
	}

}
