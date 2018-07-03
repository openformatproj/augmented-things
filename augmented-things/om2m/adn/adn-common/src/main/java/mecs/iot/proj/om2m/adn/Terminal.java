package mecs.iot.proj.om2m.adn;

import java.io.Serializable;

import mecs.iot.proj.om2m.structures.Node;

public class Terminal implements Serializable {
	
	public String id;
	public String type;
	public String address;
	public Node node;
	
	private static final long serialVersionUID = 1L;
	
	Terminal(String id, String type, String address, Node node) {
		this.id = id;
		this.type = type;
		this.address = address;
		this.node = node;
	}
	
}
