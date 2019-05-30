package fast.common.core;

/*
 * An exception when data validation failed
 */
@SuppressWarnings("serial")
public class ValidationFailed extends Exception {
	public ValidationFailed() {
		super();
	}
	
	public ValidationFailed(String message){
		super(message);
	}
}
