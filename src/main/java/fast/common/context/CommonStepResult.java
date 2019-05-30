package fast.common.context;

import java.util.ArrayList;
import java.util.HashMap;

import fast.common.logging.FastLogger;

public class CommonStepResult extends StepResult {

	public static final String DefaultField = "Value";
	private HashMap<String, String> _values;
	private String errorMessage;
	static FastLogger logger = FastLogger.getLogger("CommonStepResult");

	public CommonStepResult() {
		setStatus(Status.Passed);
		_values = new HashMap<String, String>();
	}

	public String toString() {
		return _values.values().toString();
	}

	public void setErrorMessage(String msg) {
		errorMessage = msg;
		setStatus(Status.Failed);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getFieldValue(String field) {
		return _values.get(field);
	}

	public void setFieldValue(String value) {
		_values.put(DefaultField, value);
	}

	public void setFieldValue(String field, String value) {
		_values.put(field, value);
	}

	public ArrayList<String> getFieldsValues(String field) {
		ArrayList<String> retval = new ArrayList<String>();
		retval.add(_values.get(field));
		return retval;
	}

}
