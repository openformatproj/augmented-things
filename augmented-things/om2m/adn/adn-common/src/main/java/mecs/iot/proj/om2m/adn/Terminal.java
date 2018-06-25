package mecs.iot.proj.om2m.adn;

import mecs.iot.proj.om2m.structures.Node;

public class Terminal {
	
	public String id;
	public String address;
	public Node node;
	
	Terminal(String id, String address, Node node) {
		this.id = id;
		this.address = address;
		this.node = node;
	}
	
}
