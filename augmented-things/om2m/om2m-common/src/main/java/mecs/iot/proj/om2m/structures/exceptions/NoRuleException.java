package mecs.iot.proj.om2m.structures.exceptions;

public class NoRuleException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public NoRuleException() {
		super("No rules found, assuming always true");
	}
	
	public String getReason() {
        return super.getMessage();
    }
	
}
