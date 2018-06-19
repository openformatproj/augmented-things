package mecs.iot.proj.om2m.asn.actuator.exceptions;

public class ActionNumberMismatchException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public ActionNumberMismatchException() {
		super("Actions and callbacks are not in the same number");
	}
	
	public String getReason() {
        return super.getMessage();
    }

}
