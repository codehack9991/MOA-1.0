package fast.common.core;

// Use this instead of RuntimeException
public class FastException extends RuntimeException {
	
	public FastException() {
		super();
	}
	
	public FastException(String message) {
		super(message);
	}
}

