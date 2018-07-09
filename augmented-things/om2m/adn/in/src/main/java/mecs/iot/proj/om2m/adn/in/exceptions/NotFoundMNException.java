package mecs.iot.proj.om2m.adn.in.exceptions;

public class NotFoundMNException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public NotFoundMNException() {
		super("MN not found");
	}
	
	public String getReason() {
        return super.getMessage();
    }
	
}
