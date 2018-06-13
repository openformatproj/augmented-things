package mecs.iot.proj.om2m.exceptions;

public class AttributeMissException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public AttributeMissException() {
		super("Requested configuration attribute doesn't exist");
	}
	
	public String getReason() {
        return super.getMessage();
    }
	
}
