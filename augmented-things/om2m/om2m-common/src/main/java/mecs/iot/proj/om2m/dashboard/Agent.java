package mecs.iot.proj.om2m.dashboard;

class Agent {
	
	String name;
	Type type;
	
	Agent(String name, Type type) {
		this.name = name;
		this.type = type;
	}
	
	@Override
	
	public int hashCode() {
		int hash = name.hashCode()*(type.ordinal()+1);
		return hash;
	}
	
	@Override
	
	public boolean equals(Object obj) {
		Agent agent = (Agent)obj;
		return name.equals(agent.name) && type.equals(agent.type);
	}
	
}
