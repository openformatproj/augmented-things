package mecs.iot.proj.om2m.dashboard;

import mecs.iot.proj.om2m.Services;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.Interface;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

class Shell implements Interface {
	
	private JFrame frame;
	private JTextComponent out;
	private JTextComponent outAsync;
	private JTextField commandLine;
	private JLabel login;
	private JButton submit;
	private JLabel asyncLabel;
	
	private final int offsetX = 20;
	private final int offsetY = 10;
	private final int commandLineHeight = 30;
	private final int loginWidth = 350;
	private final int commandLineWidth = 400;
	private final int submitWidth = 100;
	private final int outHeight = 300;
	private final int asyncLabelHeight = 30;
	private final Color bg = new Color(48,10,36);
	private final Color fg = Color.WHITE;
	
	private int i,j;
	
	private boolean fixed;
	
	Shell(Console console) {
		
		login = new JLabel();
		login.setBounds(offsetX, offsetY, loginWidth, commandLineHeight);
		if (console!=null)
			login.setText(console.getName()+">");
		else
			login.setText("login"+">");
		login.setFont(new Font("Ubuntu Mono",Font.BOLD,14));
		login.setForeground(fg);
		
		commandLine = new JTextField();
		commandLine.setBorder(null);
		commandLine.setBounds(loginWidth+2*offsetX, offsetY, commandLineWidth, commandLineHeight);
		commandLine.setBackground(bg);
		commandLine.setFont(new Font("Ubuntu Mono",Font.PLAIN,14));
		commandLine.setForeground(fg);
		commandLine.setCaretColor(fg);
		
		submit = new JButton("Submit");
		submit.addActionListener((arg0)->{wake();});
		submit.setBounds(loginWidth+commandLineWidth+3*offsetX, offsetY, submitWidth, commandLineHeight);
		submit.setBackground(fg);
		submit.setFont(new Font("Ubuntu Mono",Font.BOLD,14));
		submit.setForeground(bg);
		
		final int frameWidth = loginWidth+commandLineWidth+submitWidth+4*offsetX;
		final int frameWidth_ = loginWidth+commandLineWidth+submitWidth+2*offsetX;
		
		out = new JTextArea();
		JScrollPane pane = new JScrollPane(out);
		pane.setBounds(offsetX, commandLineHeight+2*offsetY, frameWidth_, outHeight);
		// out.setBounds(offsetX, commandLineHeight+2*offsetY, frameWidth_, outHeight);
		out.setBackground(bg);
		out.setFont(new Font("Ubuntu Mono",Font.BOLD,12));
		out.setForeground(fg);
		
		asyncLabel = new JLabel();
		asyncLabel.setBounds(offsetX, commandLineHeight+outHeight+3*offsetY, frameWidth_, asyncLabelHeight);
		asyncLabel.setText("Notifications:");
		asyncLabel.setFont(new Font("Ubuntu Mono",Font.BOLD,14));
		asyncLabel.setForeground(fg);
		
		outAsync = new JTextArea();
		JScrollPane paneAsync = new JScrollPane(outAsync);
		paneAsync.setBounds(offsetX, commandLineHeight+outHeight+asyncLabelHeight+4*offsetY, frameWidth_, outHeight);
		// outAsync.setBounds(offsetX, commandLineHeight+outHeight+asyncLabelHeight+4*offsetY, frameWidth_, outHeight);
		outAsync.setBackground(bg);
		outAsync.setFont(new Font("Ubuntu Mono",Font.BOLD,12));
		outAsync.setForeground(fg);
		
		final int frameHeight = commandLineHeight+outHeight+asyncLabelHeight+outHeight+5*offsetY;
		
		frame = new JFrame("AT Shell");
		frame.getContentPane().add(pane);
		frame.getContentPane().add(paneAsync);
		frame.add(commandLine);
		frame.add(login);
		frame.add(submit);
		frame.add(asyncLabel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(frameWidth,frameHeight);
		frame.setResizable(true);
		frame.setLayout(null);
		frame.getContentPane().setBackground(bg);
		
		i = 0;
		j = 0;
		
		fixed = false;
		
	}
	
	private void fix() {
		
		final int frameWidth = loginWidth+commandLineWidth+submitWidth+4*offsetX;
		final int frameWidth_ = loginWidth+commandLineWidth+submitWidth+2*offsetX;
		
		out = new JTextPane();
		out.setBounds(offsetX, commandLineHeight+2*offsetY, frameWidth_, outHeight);
		out.setBackground(bg);
		out.setFont(new Font("Ubuntu Mono",Font.BOLD,12));
		out.setForeground(fg);
		out.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE), 
	            BorderFactory.createEmptyBorder(10,10,10,10)));
		
		outAsync = new JTextPane();
		outAsync.setBounds(offsetX, commandLineHeight+outHeight+asyncLabelHeight+4*offsetY, frameWidth_, outHeight);
		outAsync.setBackground(bg);
		outAsync.setFont(new Font("Ubuntu Mono",Font.BOLD,12));
		outAsync.setForeground(fg);
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
		frame.setResizable(true);
		frame.setLayout(null);
		frame.getContentPane().setBackground(bg);
		
		fixed = true;
		
	}
	
	@Override
	public String getSerial() {
		return "0x0001";
	}
	
	@Override
	public void start() {
		try {
			frame.setVisible(true);
		} catch (NullPointerException e) {
			fix();
			frame.setVisible(true);
		}
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
		i = i%99 + 1;
		if (!fixed) {
			((JTextArea)out).setText("");
			((JTextArea)out).append(i + ") Message from \"" + Thread.currentThread().getName() + "\":\r\n\r\n");
		}
		if (isJSON) {
			if (!fixed) {
				((JTextArea)out).append(Services.formatJSON(str).replace(Constants.newLine,"\n").replace(Constants.tab,"   "));
			} else {
				((JTextPane)out).setText(Services.formatJSON(str).replace(Constants.newLine,"\n").replace(Constants.tab,"   "));
			}
		} else {
			if (!fixed) {
				((JTextArea)out).append(str.replace(Constants.newLine,"\n").replace(Constants.tab,"   "));
			} else {
				((JTextPane)out).setText(str.replace(Constants.newLine,"\n").replace(Constants.tab,"   "));
			}
		}
	}
	
	@Override
	public void outAsync(String str, boolean isJSON) {
		j = j%99 + 1;
		if (!fixed) {
			((JTextArea)outAsync).setText("");
			((JTextArea)outAsync).append(j + ") Message from \"" + Thread.currentThread().getName() + "\":\r\n\r\n");
		}
		if (isJSON) {
			if (!fixed) {
				((JTextArea)outAsync).append(Services.formatJSON(str).replace(Constants.newLine,"\n").replace(Constants.tab,"   "));
			} else {
				((JTextPane)outAsync).setText(Services.formatJSON(str).replace(Constants.newLine,"\n").replace(Constants.tab,"   "));
			}
		} else {
			if (!fixed) {
				((JTextArea)outAsync).append(str.replace(Constants.newLine,"\n").replace(Constants.tab,"   "));
			} else {
				((JTextPane)outAsync).setText(str.replace(Constants.newLine,"\n").replace(Constants.tab,"   "));
			}
		}
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
	    String json = "{" + 
				"   \"mn\":\"augmented-things-MN\"," + 
				"   \"subs\":[" + 
				"      {" + 
				"         \"receiver\":{" + 
				"            \"node\":\"ACTUATOR\"," + 
				"            \"address\":\"coap://127.0.0.1:5690/augmented-things\"," + 
				"            \"id\":\"actuator.alessandro\"" + 
				"         }," + 
				"         \"sender\":{" + 
				"            \"node\":\"SENSOR\"," + 
				"            \"id\":\"sensor.alessandro\"," + 
				"            \"type\":\"tempC\"" + 
				"         }," + 
				"         \"action\":\"action1\"," + 
				"         \"event\":\"event\"" + 
				"      }," + 
				"      {" + 
				"         \"receiver\":{" + 
				"            \"node\":\"USER\"," + 
				"            \"address\":\"coap://192.168.0.107:5691/augmented-things\"," + 
				"            \"id\":\"user.ALESSANDRO-K7NR\"" + 
				"         }," + 
				"         \"sender\":{" + 
				"            \"node\":\"SENSOR\"," + 
				"            \"id\":\"sensor.alessandro\"," + 
				"            \"type\":\"tempC\"" + 
				"         }" + 
				"      }" + 
				"   ]," + 
				"   \"id\":\"sensor.alessandro\"" + 
				"}";
	    shell.start();
	    shell.outAsync(json,true);
	    while (true) {
		    String str = shell.in();
		    shell.out(str,false);
	    }
	}

}
