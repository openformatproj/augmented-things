package mecs.iot.proj.om2m.asn.factory.dashboard;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Viewer implements Interface {
	
	private static ArrayList<EP> nodes;
	
	private JFrame frame;
	private JLabel out;
	
	public Viewer() {
		nodes = new ArrayList<EP>();
		frame = new JFrame("AT Viewer");
		out = new JLabel();
		frame.add(out);
		frame.setSize(800,300);
		frame.setResizable(false);
		frame.setLayout(null);
		out.setBounds(10, 50, 500, 200);
		out.setVerticalAlignment(SwingConstants.TOP);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	@Override
	
	public void start() {
		frame.setVisible(true);
	}
	
	@Override
	
	public void add(String id, String serial, String type, int actions) {
		nodes.add(new EP(id,serial,type,actions));
	}
	
	@Override
	
	public void show(int n) {
		EP node = nodes.get(n);
		out.setText(out.getText() + "Id: " + node.id + "\n" + "Serial: " + node.serial + "\n" + "Type: " + node.type + "\n");
	}
	
	@Override
	
	public void touch(int n, int action) {
		
	}
	
	@Override
	
	public void terminate() {
		frame.setVisible(false);
		frame.dispose();
	}
	
	public static void main(String[] args) {
	    Viewer viewer = new Viewer();
	    viewer.add("id1", "serial1", "type1", 0);
	    viewer.add("id2", "serial2", "act", 2);
	    viewer.start();
	    for (int i=0; i<2; i++)
	    	viewer.show(i);
	}

}

class EP {
	
	String id;
	String serial;
	String type;
	int actions;
	
	EP (String id, String serial, String type, int actions) {
		this.id = id;
		this.serial = serial;
		this.type = type;
		this.actions = actions;
	}
	
}