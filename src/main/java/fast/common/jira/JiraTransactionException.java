package fast.common.jira;

public class JiraTransactionException extends Exception{
	private static final long serialVersionUID =-9187847261739322307L;
	
	public JiraTransactionException() {
	}

	public JiraTransactionException(String message) {
		super(message);
	}

	public JiraTransactionException(Throwable cause) {
		super(cause);
	}

	public JiraTransactionException(String message, Throwable cause) {
		super(message, cause);
	}

	public JiraTransactionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
