package mecs.iot.proj.om2m.adn.mn.exceptions;

public class StateCreationException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public StateCreationException() {
		super("Failed to create ADN state inside CSE");
	}
	
	public String getReason() {
        return super.getMessage();
    }
	
}
