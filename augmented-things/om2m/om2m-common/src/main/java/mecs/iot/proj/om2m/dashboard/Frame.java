package mecs.iot.proj.om2m.dashboard;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

class Frame {
	
	private JFrame frame;
	private JLabel login;
	private JLabel out;
	private JTextField commandLine;
	
	Frame() {
		frame = new JFrame("AT Console");
		login = new JLabel();
		out = new JLabel();
		commandLine = new JTextField();
		JButton submit = new JButton("Submit");
		submit.addActionListener((arg0)->{wake();});
		frame.add(submit);
		frame.add(login);
		frame.add(out);
		frame.add(commandLine);
		frame.setSize(800,300);
		frame.setResizable(false);
		frame.setLayout(null);
		login.setBounds(10, 10, 200, 30);
		out.setBounds(10, 50, 500, 200);
		out.setVerticalAlignment(SwingConstants.TOP);
		commandLine.setBounds(210, 10, 400, 30);
		submit.setBounds(650, 10, 100, 30);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	void start() {
		frame.setVisible(true);
	}
	
	void setLogin(String str) {
		login.setText(str);
	}
	
	synchronized String in() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return commandLine.getText();
	}
	
	private synchronized void wake() {
		notify();
	}
	
	void out(String str) {
		out.setText(str);
	}
	
	public static void main(String[] args) {
	    Frame frame = new Frame();
	    frame.start();
	    frame.setLogin("login");
	    while (true) {
		    String str = frame.in();
		    frame.out(str);
	    }
	}

}
