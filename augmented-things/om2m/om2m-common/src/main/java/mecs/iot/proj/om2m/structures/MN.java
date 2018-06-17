package mecs.iot.proj.om2m.structures;

public class MN {
	
	public String id;
	public String address;
	
	public MN(String id, String address) {
		this.id = id;
		this.address = address;
	}
	
	@Override
	
	public String toString() {
		return "id=" + id + ", address=" + address;
	}
	
	@Override
	
	public boolean equals(Object obj) {
		MN mn = (MN)obj;
		return id.equals(mn.id);
	}
	
}
