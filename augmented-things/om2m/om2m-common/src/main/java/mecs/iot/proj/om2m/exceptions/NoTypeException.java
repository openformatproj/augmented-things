package mecs.iot.proj.om2m.exceptions;

public class NoTypeException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public NoTypeException() {
		super("Specified sensor type hasn't been found");
	}
	
	public String getReason() {
        return super.getMessage();
    }
	
}
