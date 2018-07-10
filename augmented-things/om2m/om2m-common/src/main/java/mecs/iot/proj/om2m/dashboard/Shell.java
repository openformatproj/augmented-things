package mecs.iot.proj.om2m.dashboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.structures.Constants;

class Shell implements Interface {
	
	private JFrame frame;
	private JTextPane out;
	private JTextPane outAsync;
	private JTextField commandLine;
	
	private final int offsetX = 20;
	private final int offsetY = 10;
	private final int commandLineHeight = 30;
	private final int loginWidth = 350;
	private final int commandLineWidth = 400;
	private final int submitWidth = 100;
	private final int outHeight = 300;
	private final int asyncLabelHeight = 30;
	
	Shell(Console console) {
		
		JLabel login = new JLabel();
		login.setBounds(offsetX, offsetY, loginWidth, commandLineHeight);
		if (console!=null)
			login.setText(console.getName()+">");
		else
			login.setText("augmented-things-IN/console@ALESSANDRO-K7NR"+">");
		login.setFont(new Font("Ubuntu Mono",Font.BOLD,14));
		login.setForeground(Color.WHITE);
		
		commandLine = new JTextField();
		commandLine.setBorder(null);
		commandLine.setBounds(loginWidth+2*offsetX, offsetY, commandLineWidth, commandLineHeight);
		commandLine.setFont(new Font("Ubuntu Mono",Font.PLAIN,14));
		commandLine.setForeground(Color.WHITE);
		commandLine.setCaretColor(Color.WHITE);
		
		JButton submit = new JButton("Submit");
		submit.addActionListener((arg0)->{wake();});
		submit.setBounds(loginWidth+commandLineWidth+3*offsetX, offsetY, submitWidth, commandLineHeight);
		submit.setBackground(Color.WHITE);
		submit.setFont(new Font("Ubuntu Mono",Font.BOLD,14));
		
		final int frameWidth = loginWidth+commandLineWidth+submitWidth+4*offsetX;
		final int frameWidth_ = loginWidth+commandLineWidth+submitWidth+2*offsetX;
		
		out = new JTextPane();
		out.setBounds(offsetX, commandLineHeight+2*offsetY, frameWidth_, outHeight);
		out.setFont(new Font("Ubuntu Mono",Font.BOLD,12));
		out.setForeground(Color.WHITE);
		out.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE), 
	            BorderFactory.createEmptyBorder(10,10,10,10)));
		
		JLabel asyncLabel = new JLabel();
		asyncLabel.setBounds(offsetX, commandLineHeight+outHeight+3*offsetY, frameWidth_, asyncLabelHeight);
		asyncLabel.setText("Notifications:");
		asyncLabel.setFont(new Font("Ubuntu Mono",Font.BOLD,14));
		asyncLabel.setForeground(Color.WHITE);
		
		outAsync = new JTextPane();
		outAsync.setBounds(offsetX, commandLineHeight+outHeight+asyncLabelHeight+4*offsetY, frameWidth_, outHeight);
		outAsync.setFont(new Font("Ubuntu Mono",Font.BOLD,12));
		outAsync.setForeground(Color.WHITE);
		outAsync.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE), 
	            BorderFactory.createEmptyBorder(10,10,10,10)));
		
		final int frameHeight = commandLineHeight+outHeight+asyncLabelHeight+outHeight+5*offsetY;
		
		frame = new JFrame("AT Shell");
		frame.add(out);
		frame.add(outAsync);
		frame.add(commandLine);
		frame.add(login);
		frame.add(submit);
		frame.add(asyncLabel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(frameWidth,frameHeight);
		frame.setResizable(false);
		frame.setLayout(null);
		frame.getContentPane().setBackground(new Color(48,10,36));
		
		out.setBackground(new Color(48,10,36));
		outAsync.setBackground(new Color(48,10,36));
		commandLine.setBackground(new Color(48,10,36));
		
	}
	
	@Override
	
	public String getSerial() {
		return "0x0001";
	}
	
	@Override
	
	public void start() {
		frame.setVisible(true);
	}
	
	@Override
	
	public synchronized String in() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return commandLine.getText();
	}
	
	@Override
	
	public void out(String str, boolean isJSON) {
		if (isJSON)
			out.setText(Services.formatJSON(str).replace(Constants.newLine,"\n").replace(Constants.tab,"\t"));
		else
			out.setText(str.replace(Constants.newLine,"\n").replace(Constants.tab,"\t"));
	}
	
	@Override
	
	public void outAsync(String str, boolean isJSON) {
		if (isJSON)
			outAsync.setText(Services.formatJSON(str).replace(Constants.newLine,"\n").replace(Constants.tab,"\t"));
		else
			outAsync.setText(str.replace(Constants.newLine,"\n").replace(Constants.tab,"\t"));
	}
	
	@Override
	
	public void terminate() {
		frame.setVisible(false);
		frame.dispose();
	}
	
	private synchronized void wake() {
		notify();
	}
	
	public static void main(String[] args) {
	    Shell shell = new Shell(null);
	    shell.outAsync("Async content",false);
	    shell.start();
	    while (true) {
		    String str = shell.in();
		    shell.out(str,false);
	    }
	}

}
