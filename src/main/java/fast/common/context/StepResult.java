package fast.common.context;

import java.util.ArrayList;

public abstract class StepResult {
	
	public static final String DEFAULT_FIELD_VALUE="Value";
	
	public enum Status{
		Passed,
		Failed,
		Skipped
	}
	
	public static final String STEP_RESULT_PASS = "PASSED";
	public static final String STEP_RESULT_FAIL = "FAILED";
	public static final String STEP_RESULT_SKIP = "SKIPPED";
	
	private Status status;
	private String log;
	private String failedMessage;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public String getFailedMessage() {
		return failedMessage;
	}

	public void setFailedMessage(String failedMessage) {
		this.failedMessage = failedMessage;
	}
	
    public abstract String toString();
    public abstract String getFieldValue(String field) throws Throwable;
    public abstract ArrayList<String> getFieldsValues(String field);
}
