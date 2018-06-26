package mecs.iot.proj.om2m.structures.exceptions;

public class InvalidRuleException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InvalidRuleException() {
		super("Invalid syntax for the given rule");
	}
	
	public String getReason() {
        return super.getMessage();
    }
	
}
