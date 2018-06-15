package mecs.iot.proj.om2m.dashboard;

import mecs.iot.proj.om2m.structures.Constants;

import java.util.ArrayList;
import java.util.List;
import java.text.MessageFormat;

public class DebugStream {
	
	private String name;
	private boolean debug;
//	private int preambleLength;
//	private int offset;
	private String pad;
//	private int num;
	
	public DebugStream(String name, boolean debug) {
		Stream.register(Thread.currentThread().getName());
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
		List<String> chunk = new ArrayList<String>();
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
				str += chunk.get(0) + "\n";
			} else {
				str += pad(i);
				str += chunk.get(j) + "\n";
			}
		}
		
		Stream.lock(Type.DEBUG);
		Stream.print(preamble + str);
		Stream.unlock();
		
	}
	
//	public void out1(String msg, int i) {
//		
//		if (!debug || msg==null || msg=="")
//			return;
//		
//		msg += "...";
//		
//		String preamble = name + "\t[DEBUG]\t" + Integer.toString(i) + ") ";
//		preambleLength = preamble.length();
//		int chunkLength = Constants.streamCharacters - preambleLength;
//		List<String> chunk = new ArrayList<String>();
//		int startIndex = 0;
//		int endIndex;
//		
//		while(startIndex<msg.length()) {
//			endIndex = Math.min(startIndex+chunkLength,msg.length());
//			while (endIndex<msg.length() && !isBreakingChar(msg.charAt(endIndex))) {
//				endIndex++;
//			}
//			if (endIndex<msg.length()) endIndex++;
//			chunk.add(msg.substring(startIndex,endIndex));
//			startIndex = endIndex;
//		}
//		
//		String str = "";
//		
//		for (int j=0; j<chunk.size(); j++) {
//			if (j==0) {
//				str += chunk.get(0);
//			} else {
//				str += pad(i);
//				str += chunk.get(j);
//			}
//			if (j<chunk.size()-1)
//				str += "\n";
//		}
//		
//		offset = Math.min(chunk.get(chunk.size()-1).length(),chunkLength);
//		num = i;
//		
//		Stream.lock(Type.DEBUG);
//		Stream.print(preamble + str);
//		
//	}
//	
//	public void out1_2(String msg) {
//		
//		if (!debug || msg==null || msg=="")
//			return;
//		
//		if (!Stream.hasBeenInterrupted(Type.DEBUG)) {
//		
//			msg = " " + msg + "...";
//			
//			int chunkLength = Constants.streamCharacters - preambleLength;
//			List<String> chunk = new ArrayList<String>();
//			int startIndex = 0;
//			int endIndex;
//			int offset = this.offset;
//			
//			while(startIndex<msg.length()) {
//				endIndex = Math.min(startIndex+chunkLength-offset,msg.length());
//				while (endIndex>0 && endIndex<msg.length() && !isBreakingChar(msg.charAt(endIndex))) {
//					endIndex++;
//				}
//				if (endIndex>0 && endIndex<msg.length()) endIndex++;
//				chunk.add(msg.substring(startIndex,endIndex));
//				startIndex = endIndex;
//				offset = 0;
//			}
//			
//			String str = "";
//			
//			for (int j=0; j<chunk.size(); j++) {
//				if (j==0) {
//					str += chunk.get(0);
//				} else {
//					str += pad(num);
//					str += chunk.get(j);
//				}
//				if (j<chunk.size()-1)
//					str += "\n";
//			}
//			
//			if (chunk.size()==1)
//				this.offset = Math.min(this.offset + chunk.get(0).length(),chunkLength);
//			else
//				this.offset = Math.min(chunk.get(chunk.size()-1).length(),chunkLength);
//			
//			Stream.print(str);
//		
//		} else {
//			
//			msg = "..." + msg;
//			out1(msg,num);
//			
//		}
//		
//	}
//	
//	public void out2(String msg) {
//		
//		if (!debug || msg==null || msg=="")
//			return;
//		
//		if (!Stream.hasBeenInterrupted(Type.DEBUG)) {
//		
//			msg = " " + msg;
//			
//			int chunkLength = Constants.streamCharacters - preambleLength;
//			List<String> chunk = new ArrayList<String>();
//			int startIndex = 0;
//			int endIndex;
//			int offset = this.offset;
//			
//			while(startIndex<msg.length()) {
//				endIndex = Math.min(startIndex+chunkLength-offset,msg.length());
//				while (endIndex>0 && endIndex<msg.length() && !isBreakingChar(msg.charAt(endIndex))) {
//					endIndex++;
//				}
//				if (endIndex>0 && endIndex<msg.length()) endIndex++;
//				chunk.add(msg.substring(startIndex,endIndex));
//				startIndex = endIndex;
//				offset = 0;
//			}
//			
//			String str = "";
//			
//			for (int j=0; j<chunk.size(); j++) {
//				if (j==0) {
//					str += chunk.get(0) + "\n";
//				} else {
//					str += pad(num);
//					str += chunk.get(j) + "\n";
//				}
//			}
//			
//			Stream.print(str);
//			Stream.unlock();
//		
//		} else {
//			
//			msg = "..." + msg;
//			out(msg,num);
//			
//		}
//		
//	}
	
	private String showLocation() {
		StackTraceElement element = Thread.currentThread().getStackTrace()[3];
		return MessageFormat.format("({0}:{1, number,#}) : ",element.getFileName(),element.getLineNumber());
	}
	
	public static void main( String[] args )
    {
		OutStream outStream = new OutStream("user@ALESSANDRO-K7NR");
		DebugStream debugStream = new DebugStream("user@ALESSANDRO-K7NR",true);
		for (int i=0; i<11; i++) {
			outStream.out1("Received 192.168.0.103 as MN address, connecting to CSE", i);
			debugStream.out("Connected to CSE",i);
			outStream.out2("done");
		}
    }

}
