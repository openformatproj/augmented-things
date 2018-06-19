package mecs.iot.proj.om2m.dashboard;

import mecs.iot.proj.om2m.structures.Constants;

import java.util.ArrayList;

public class OutStream {
	
	private Agent me;
	private String name;
	
	final private String pad;
	
	private int preambleLength;
	private int offset;
	private int num;
	
	public OutStream(String name) {
		me = new Agent(name,Type.OUT);
		Stream.register(me);
		this.name = name;
		String str = "";
		for (int i=0; i<name.length(); i++)
			str += " ";
		str += "\t";
		for (int i=0; i<(new String("[INFO]")).length(); i++)
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
		
		if (msg==null || msg=="")
			return;
		
		String preamble = name + "\t[INFO]\t" + Integer.toString(i) + ") ";
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
	
	public void out1(String msg, int i) {
		
		if (msg==null || msg=="")
			return;
		
		msg += "...";
		
		String preamble = name + "\t[INFO]\t" + Integer.toString(i) + ") ";
		preambleLength = preamble.length();
		int chunkLength = Constants.streamCharacters - preambleLength;
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
		
		offset = Math.min(chunk.get(chunk.size()-1).length(),chunkLength);
		num = i;
		
		Stream.lock(me);
		Stream.print(preamble+str,me);
		
	}
	
	public void out1_2(String msg) {
		
		if (msg==null || msg=="")
			return;
		
		if (!Stream.hasBeenInterrupted(me)) {
		
			msg = " " + msg + "...";
			
			int chunkLength = Constants.streamCharacters - preambleLength;
			ArrayList<String> chunk = new ArrayList<String>();
			int startIndex = 0;
			int endIndex;
			int offset = this.offset;
			
			while(startIndex<msg.length()) {
				endIndex = Math.min(startIndex+chunkLength-offset,msg.length());
				while (endIndex>0 && endIndex<msg.length() && !isBreakingChar(msg.charAt(endIndex))) {
					endIndex++;
				}
				if (endIndex>0 && endIndex<msg.length()) endIndex++;
				chunk.add(msg.substring(startIndex,endIndex));
				startIndex = endIndex;
				offset = 0;
			}
			
			String str = "";
			
			for (int j=0; j<chunk.size(); j++) {
				if (j==0) {
					str += chunk.get(0);
				} else {
					str += pad(num);
					str += chunk.get(j);
				}
				if (j<chunk.size()-1)
					str += "\n";
			}
			
			if (chunk.size()==1)
				this.offset = Math.min(this.offset + chunk.get(0).length(),chunkLength);
			else
				this.offset = Math.min(chunk.get(chunk.size()-1).length(),chunkLength);
			
			Stream.print(str,me);
		
		} else {
			
			msg = "..." + msg;
			out1(msg,num);
			
		}
		
	}
	
	public void out2(String msg) {
		
		if (msg==null || msg=="")
			return;
		
		if (!Stream.hasBeenInterrupted(me)) {
		
			msg = " " + msg;
			
			int chunkLength = Constants.streamCharacters - preambleLength;
			ArrayList<String> chunk = new ArrayList<String>();
			int startIndex = 0;
			int endIndex;
			int offset = this.offset;
			
			while(startIndex<msg.length()) {
				endIndex = Math.min(startIndex+chunkLength-offset,msg.length());
				while (endIndex>0 && endIndex<msg.length() && !isBreakingChar(msg.charAt(endIndex))) {
					endIndex++;
				}
				if (endIndex>0 && endIndex<msg.length()) endIndex++;
				chunk.add(msg.substring(startIndex,endIndex));
				startIndex = endIndex;
				offset = 0;
			}
			
			String str = "";
			
			for (int j=0; j<chunk.size(); j++) {
				if (j==0) {
					str += chunk.get(0);
				} else {
					str += pad(num);
					str += chunk.get(j);
				}
				if (j<chunk.size()-1)
					str += "\n";
			}
			
			Stream.print(str+"\n",me);
		
		} else {
			
			msg = "..." + msg;
			out(msg,num);
			
		}
		
		Stream.unlock(me);
		
	}

}
