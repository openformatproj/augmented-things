package mecs.iot.proj.om2m.structures;

public class Tag_ {
	
	public Node node;
	public String id;
	public String type;
	public String address;
	public String[] attributes;
	
	public Tag_ (Node node, String id, String description, String[] attributes) {
		this.node = node;
		this.id = id;
		switch (node) {
			case SENSOR:
				type = description;
				break;
			case ACTUATOR:
				address = description;
				break;
		}
		this.attributes = attributes;
	}
	
	public String[] labels() {
		if (node==Node.ACTUATOR) {
			return attributes;
		} else {
			int l = attributes.length;
			String[] labels = new String[l];
			String[] splits;
			for (int i=0; i<l; i++) {
				splits = attributes[i].split(": ");
				if (splits.length>1)
					labels[i] = splits[1];
				else
					labels[i] = splits[0];
			}
			return labels;
		}
	}

}
