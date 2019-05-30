package fast.common.context;

import java.util.ArrayList;
import java.util.HashMap;

import fast.common.logging.FastLogger;

public class SshStepResult extends StepResult implements IStringResult {	
	public static final String DEFAULT_FIELD = "Value";
	private HashMap<String, String> values;
	static FastLogger logger = FastLogger.getLogger(SshStepResult.class.getName());
	
	public SshStepResult() {
		values = new HashMap<String, String>();
	}

	@Override
	public void contains(String userstr){
		if(!getLog().contains(userstr)){
			throw new AssertionError("[" + getLog() + "] doesn't contain " + userstr);
		}
	}
	
	@Override
	public void not_contains(String userstr){
		if(getLog().contains(userstr)){
			throw new AssertionError("[" + getLog() + "] contains " + userstr);
		}
	}

	public void setFieldValue(String value) {
		values.put(DEFAULT_FIELD, value);
	}

	public void setFieldValue(String field, String value) {
		values.put(field, value);
	}

	@Override
	public String toString() {
		return values.values().toString();
	}

	@Override
	public String getFieldValue(String field) throws Throwable {
		return values.get(field);
	}

	@Override
	public ArrayList<String> getFieldsValues(String field) {
		ArrayList<String> retval = new ArrayList<String>();
		retval.add(values.get(field));
		return retval;
	}

}
