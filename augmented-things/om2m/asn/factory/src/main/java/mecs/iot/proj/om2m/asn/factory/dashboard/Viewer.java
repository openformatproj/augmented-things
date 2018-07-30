package mecs.iot.proj.om2m.asn.factory.dashboard;

import mecs.iot.proj.om2m.dashboard.FactoryInterface;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Viewer implements FactoryInterface {
	
	private static ArrayList<ASN> nodes;
	private static Grid grid;
	private final static int maxActions = 5;
	
	public Viewer() {
		nodes = new ArrayList<ASN>();
		grid = new Grid("AT Viewer",500,500,4,4); 																		// Name, width, height, number of slots on X and Y axes
	}
	
	@Override
	public void start() {
		grid.setVisible(true);
	}
	
	@Override
	public void add(String id, String serial, String type, int actions) {
		nodes.add(new ASN(id,serial,type,Math.min(actions,maxActions)));
	}
	
	@Override
	public void show(int n) {
		ASN node = nodes.get(n);
		grid.add(node.id,node.actions);
	}
	
	@Override
	public void touch(int n) {
		grid.nodes.get(n).touch();
	}
	
	@Override
	public void touch(int n, int action) {
		if (action<maxActions)
			grid.nodes.get(n).touch(action);
	}
	
	@Override
	public void terminate() {
		grid.setVisible(false);
		grid.dispose();
	}
	
	public static void main(String[] args) {
	    Viewer viewer = new Viewer();
	    viewer.add("sensor1", "0x0001", "tempC", 0);
	    viewer.add("sensor2", "0x0003", "tempC", 0);
	    viewer.add("actuator1", "0x0002", "act", 6);
	    viewer.add("actuator2", "0x0004", "act", 3);
	    viewer.start();
	    for (int i=0; i<4; i++)
	    	viewer.show(i);
	    while(true) {
		    try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    viewer.touch(0);
		    try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    viewer.touch(1);
		    try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    viewer.touch(2,(int)Math.rint(5*Math.random()));
		    try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    viewer.touch(3,(int)Math.rint(2*Math.random()));
	    }
	}

	private class ASN {
		
		String id;
		String serial;
		String type;
		int actions;
		
		ASN(String id, String serial, String type, int actions) {
			this.id = id;
			this.serial = serial;
			this.type = type;
			this.actions = actions;
		}
		
	}
	
}

class Grid extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private int xSlots;
	private int ySlots;
	private Stroke solidStroke;
	private Graphics2D g2d;
	
	private Point[][] centers;
	private final double edge;
	
	final ArrayList<Node> nodes;
	
	Grid(String name, int panelWidth, int panelHeight, int xSlots, int ySlots) {
		super(name);
		this.xSlots = xSlots;
		this.ySlots = ySlots;
		BufferedImage graphicsContext = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_RGB);
		JLabel contextRender = new JLabel(new ImageIcon(graphicsContext));
		JPanel contentPanel = new JPanel();
		contentPanel.add(contextRender);
		contentPanel.setSize(panelWidth, panelHeight);
		g2d = graphicsContext.createGraphics();
		RenderingHints antialiasing = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHints(antialiasing);
		// Set up the font to print on the circles
		g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 14f));
		// Clear the background
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, graphicsContext.getWidth(), graphicsContext.getHeight());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setContentPane(contentPanel);
		pack();
		setLocationRelativeTo(null);
		centers = new Point[xSlots][ySlots];
		for (int i=0; i<xSlots; i++)
			for (int j=0; j<ySlots; j++)
				centers[i][j] = new Point((i+0.5)*(panelWidth/(double)xSlots),(j+0.5)*(panelHeight/(double)ySlots));
		edge = Math.min(panelWidth/(double)xSlots,panelHeight/(double)ySlots)/2.0;
		solidStroke = new BasicStroke((float)(3.0*(edge/125.0)));
		nodes = new ArrayList<Node>();
	}
	
	void add(String name, int actions) {
		Point p;
		int explored = 0;
		do {
			int x = (int)Math.rint((xSlots-1)*Math.random());
			int y = (int)Math.rint((ySlots-1)*Math.random());
			p = centers[x][y];
			if (!p.explored) {
				p.explored = true;
				explored++;
			} else {
				continue;
			}
		} while (p.assigned && explored<xSlots*ySlots);
		for (int i=0; i<xSlots; i++)
			for (int j=0; j<ySlots; j++)
				centers[i][j].explored = false;
		if (p.assigned==false) {
			p.assigned = true;
			Node node = new Node(this,actions);
			double x = p.center[0];
			double y = p.center[1];
			Point2D center = new Point2D.Double(x,y);
			double radius = 0.5*edge;
			node.circle = circle(center,radius);
			// TODO: show node info and actions
			double actionRadius = 0.1*edge;
			double angle = 2*Math.PI*Math.random();
			for (int i=0; i<actions; i++) {
				center = new Point2D.Double(x+(radius+0.15*edge+actionRadius)*Math.cos(angle),y+(radius+0.15*edge+actionRadius)*Math.sin(angle));
				angle += Math.PI/5.0;
				node.actionCircle[i] = circle(center,actionRadius);
			}
			node.draw();
			nodes.add(node);
		}
	}

	static Ellipse2D.Double circle(Point2D center, double radius) {
		Ellipse2D.Double circle = new Ellipse2D.Double(center.getX()-radius, center.getY()-radius, 2*radius, 2*radius);
		return circle;
	}

	class Node {
		
		Grid parent;
		Ellipse2D.Double circle;
		Ellipse2D.Double[] actionCircle;
		int actions;
		Color color = new Color(221,221,119);
		Color actionColor = new Color(119,221,119);
		
		Node(Grid parent, int actions) {
			this.parent = parent;
			actionCircle = new Ellipse2D.Double[actions];
			this.actions = actions;
		}
		
		void draw() {
			g2d.setStroke(solidStroke);
			g2d.setColor(Color.BLACK);
			g2d.draw(circle);
			for (int i=0; i<actions; i++) {
				g2d.setStroke(solidStroke);
				g2d.setColor(Color.BLACK);
				g2d.draw(actionCircle[i]);
			}
			parent.getContentPane().validate();
			parent.getContentPane().repaint();
		}
		
		void touch() {
			double x = circle.getCenterX();
			double y = circle.getCenterY();
			Point2D center = new Point2D.Double(x,y);
			double radius = circle.getWidth()/2.0;
			Ellipse2D.Double c = circle(center,0.8*radius);
			g2d.setStroke(new BasicStroke(0.0f));
			g2d.setPaint(color);
			g2d.fill(c);
			parent.getContentPane().validate();
			parent.getContentPane().repaint();
			Fader fader = new Fader(c,color);
			fader.start();
		}
		
		void touch(int n) {
			double x = actionCircle[n].getCenterX();
			double y = actionCircle[n].getCenterY();
			Point2D center = new Point2D.Double(x,y);
			double radius = actionCircle[n].getWidth()/2.0;
			Ellipse2D.Double c = circle(center,0.8*radius);
			g2d.setStroke(new BasicStroke(0.0f));
			g2d.setPaint(actionColor);
			g2d.fill(c);
			parent.getContentPane().validate();
			parent.getContentPane().repaint();
			Fader fader = new Fader(c,actionColor);
			fader.start();
		}
		
		private class Fader extends Thread {
			
			Ellipse2D.Double c;
			int r;
			int g;
			int b;
			
			Fader(Ellipse2D.Double c, Color color) {
				this.c = c;
				r = color.getRed();
				g = color.getGreen();
				b = color.getBlue();
			}
			
			@Override
			public void run() {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				while(r<255 || g<255 || b<255) {
					if (r<255)
						r = Math.min((int)Math.floor(1.05*r),255);
					if (g<255)
						g = Math.min((int)Math.floor(1.05*g),255);
					if (b<255)
						b = Math.min((int)Math.floor(1.05*b),255);
					g2d.setStroke(new BasicStroke(0.0f));
					g2d.setPaint(new Color(r,g,b));
					g2d.fill(c);
					parent.getContentPane().validate();
					parent.getContentPane().repaint();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		
	}

	private class Point {
	
		double[] center = new double[2];
		boolean assigned = false;
		boolean explored = false;
		
		Point(double x, double y) {
			this.center[0] = x;
			this.center[1] = y;
		}
		
	}
	
}