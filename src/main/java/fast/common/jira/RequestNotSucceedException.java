package fast.common.jira;

public class RequestNotSucceedException extends Exception {

	public static final String EXCEPTION_MESSAGE_PATTERN="StatusCode:%s.ErrorMessage:%s";
	
	private static final long serialVersionUID = 1799460054976758636L;

	public RequestNotSucceedException() {
	}

	public RequestNotSucceedException(String message) {
		super(message);
	}

	public RequestNotSucceedException(Throwable cause) {
		super(cause);
	}

	public RequestNotSucceedException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestNotSucceedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
