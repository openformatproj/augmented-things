package mecs.iot.proj.om2m.exceptions;

public class AttributeSyntaxException extends Exception {

	private static final long serialVersionUID = 1L;

	public AttributeSyntaxException() {
		super("Syntax for attribute query is not correct");
	}
	
	public String getReason() {
        return super.getMessage();
    }

}
