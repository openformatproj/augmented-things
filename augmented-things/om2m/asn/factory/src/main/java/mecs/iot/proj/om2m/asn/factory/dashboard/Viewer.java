package mecs.iot.proj.om2m.asn.factory.dashboard;

import mecs.iot.proj.om2m.dashboard.FactoryInterface;

import java.util.ArrayList;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ToolTipManager;

public class Viewer implements FactoryInterface {
	
	private static ArrayList<ASN> nodes;
	private static Grid grid;
	private final static int maxAttributes = 5;
	
	public Viewer() {
		nodes = new ArrayList<ASN>();
		grid = new Grid("AT Viewer",500,500,4,4); 																		// Name, width, height, number of slots on X and Y axes
	}
	
	@Override
	public void start() {
		grid.setVisible(true);
		grid.logger.setVisible(true);
	}
	
	@Override
	public void add(String id, String serial, String type, String[] attributes) {
		nodes.add(new ASN(id,serial,type,attributes));
	}
	
	@Override
	public void show(int n) {
		ASN node = nodes.get(n);
		grid.add(node.id,node.serial,node.attributes);
	}
	
	@Override
	public void touch(int n, String event) {
		grid.nodes.get(n).touch(event);
	}
	
	@Override
	public void touch(int n, int action) {
		if (action<maxAttributes)
			grid.nodes.get(n).touch(action);
	}
	
	@Override
	public void terminate() {
		grid.setVisible(false);
		grid.dispose();
		grid.logger.setVisible(false);
		grid.logger.dispose();
	}
	
	public static void main(String[] args) {
	    Viewer viewer = new Viewer();
	    viewer.add("sensor1", "0x0001", "tempC", new String[] {"event1"});
	    viewer.add("sensor2", "0x0003", "tempC", new String[] {});
	    viewer.add("actuator1", "0x0002", "act", new String[] {"action1","action2","action3","action4","action5","action6"});
	    viewer.add("actuator2", "0x0004", "act", new String[] {"action1","action2","action3"});
	    viewer.start();
	    for (int i=0; i<4; i++)
	    	viewer.show(i);
	    while(true) {
		    try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    viewer.touch(0,"Published: 36.0 °C");
		    try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    viewer.touch(1,"Published: 37.0 °C");
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
		String[] attributes;
		
		ASN(String id, String serial, String type, String[] attributes) {
			this.id = id;
			this.serial = serial;
			this.type = type;
			if (attributes.length<=maxAttributes) {
				this.attributes = attributes;
			} else {
				this.attributes = new String[maxAttributes];
				for (int i=0; i<maxAttributes; i++)
					this.attributes[i] = attributes[i];
			}
		}
		
	}
	
}

class Grid extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private int xSlots;
	private int ySlots;
	private Stroke solidStroke;
	private Graphics2D g2d;
	private JPanel contentPanel;
	
	private Point[][] centers;
	private final double edge;
	
	final ArrayList<Node> nodes;
	
	Logger logger;
	
	Grid(String name, int panelWidth, int panelHeight, int xSlots, int ySlots) {
		super(name);
		this.xSlots = xSlots;
		this.ySlots = ySlots;
		BufferedImage graphicsContext = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_RGB);
		JLabel contextRender = new JLabel(new ImageIcon(graphicsContext));
		contentPanel = new JPanel();
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
		logger = new Logger("AT Event Logger",300,200);
	}
	
	void add(String id, String serial, String[] attributes) {
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
			Node node = new Node(this,id,serial,attributes);
			double x = p.center[0];
			double y = p.center[1];
			Point2D center = new Point2D.Double(x,y);
			double radius = 0.5*edge;
			node.circle = circle(center,radius);																										// Create the main circle for the node
			double attributeRadius = 0.1*edge;
			double angle = 2*Math.PI*Math.random();
			for (int i=0; i<attributes.length; i++) {
				center = new Point2D.Double(x+(radius+0.15*edge+attributeRadius)*Math.cos(angle),y+(radius+0.15*edge+attributeRadius)*Math.sin(angle));
				angle += Math.PI/5.0;
				node.attributeCircle[i] = circle(center,attributeRadius);																				// Add a circle for the i-th attribute (event or action)
			}
			node.draw();
			nodes.add(node);
		}
	}
	
	void log(String event) {
		logger.print(event);
	}

	static Ellipse2D.Double circle(Point2D center, double radius) {
		Ellipse2D.Double circle = new Ellipse2D.Double(center.getX()-radius, center.getY()-radius, 2*radius, 2*radius);
		return circle;
	}
	
	class Logger extends JFrame {
		
		private static final long serialVersionUID = 1L;
		
		private JTextArea textArea;
		private JScrollPane pane;
		
		Logger(String title, int width, int height) {
			super(title);
			setSize(width, height);
			textArea = new JTextArea();
			pane = new JScrollPane(textArea);
			getContentPane().add(pane);
		}

		void print(String event) {
			textArea.append(event);
			this.getContentPane().validate();
		}
		
	}

	class Node {
		
		Grid parent;
		String id;
		String serial;
		Ellipse2D.Double circle;
		Ellipse2D.Double[] attributeCircle;
		String[] attributes;
		Color color = new Color(221,221,119);
		Color attributeColor = new Color(119,221,119);
		
		Node(Grid parent, String id, String serial, String[] attributes) {
			this.parent = parent;
			this.id = id;
			this.serial = serial;
			attributeCircle = new Ellipse2D.Double[attributes.length];
			this.attributes = attributes;
		}
		
		void draw() {
			g2d.setStroke(solidStroke);
			g2d.setColor(Color.BLACK);
			g2d.draw(circle);
			parent.contentPanel.addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseMoved(MouseEvent e) {
					if (circle.contains(e.getPoint()))
						parent.contentPanel.setToolTipText("<html><p>Id: " + id + "<br/>Serial: " + serial + "</p></html>");
					ToolTipManager.sharedInstance().mouseMoved(e);
				}
				@Override
				public void mouseDragged(MouseEvent e) {
					// TODO Auto-generated method stub 
				}
			});
			for (int i=0; i<attributes.length; i++) {
				g2d.setStroke(solidStroke);
				g2d.setColor(Color.BLACK);
				g2d.draw(attributeCircle[i]);
			}
			parent.getContentPane().validate();
			parent.getContentPane().repaint();
		}
		
		void touch(String event) {
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
			parent.log(id + " has sent data. " + event + "\r\n");
		}
		
		void touch(int n) {
			double x = attributeCircle[n].getCenterX();
			double y = attributeCircle[n].getCenterY();
			Point2D center = new Point2D.Double(x,y);
			double radius = attributeCircle[n].getWidth()/2.0;
			Ellipse2D.Double c = circle(center,0.8*radius);
			g2d.setStroke(new BasicStroke(0.0f));
			g2d.setPaint(attributeColor);
			g2d.fill(c);
			parent.getContentPane().validate();
			parent.getContentPane().repaint();
			Fader fader = new Fader(c,attributeColor);
			fader.start();
			parent.log(id + " has triggered an action: " + attributes[n] + "\r\n");
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