package fast.common.htmlReport;

public class AssertionFailure extends AssertionError {
	
	private static final long serialVersionUID = 1L;
	private String message;

	public AssertionFailure() {
		super();
	}

	public AssertionFailure(String Message) {
		super(Message);
		this.message = Message;
	}

	public AssertionFailure(String message, Throwable cause) {
		super(message, cause);

	}
	
	@Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
	
	@Override
    public String toString() {
        return message;
    }
 
    @Override
    public String getMessage() {
        return message;
    }

}
