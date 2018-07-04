package mecs.iot.proj.om2m.adn.mn.exceptions;

public class RegistrationException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public RegistrationException() {
		super("Failed to register to IN");
	}
	
	public String getReason() {
        return super.getMessage();
    }
	
}
