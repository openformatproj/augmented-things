package mecs.iot.proj.om2m.dashboard;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

class Shell implements Interface {
	
	private JFrame frame;
	private JLabel login;
	private JLabel out;
	private JTextField commandLine;
	
	Shell(Console console) {
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
		if (console!=null)
			login.setText(console.getName()+">");
		else
			login.setText("login");
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
	
	public void out(String str) {
		out.setText(str);
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
	    shell.start();
	    while (true) {
		    String str = shell.in();
		    shell.out(str);
	    }
	}

}
