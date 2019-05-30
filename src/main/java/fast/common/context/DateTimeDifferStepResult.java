package fast.common.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fast.common.logging.FastLogger;

public class DateTimeDifferStepResult extends StepResult{
	private Map<String, String> result;

	static FastLogger logger = FastLogger.getLogger("DateTimeStepResult");

	public DateTimeDifferStepResult() {
		setStatus(Status.Passed);
		result = new HashMap<String, String>();
	}

	public void setResult(Map<String, String> result) {
		this.result = result;
	}

	public Map<String, String> getResult() {
		return result;
	}

	@Override
	public String toString() {
		return result.toString();
	}

	@Override
	public String getFieldValue(String field) throws Throwable {
	    String fieldValue = null;
	    if(!result.containsKey(field)){
	    	logger.info("the "+field+" is not the key of map result!");
	    	return null;
	    }
	    fieldValue = result.get(field).toString();
		return fieldValue;
	}

	@Override
	public ArrayList<String> getFieldsValues(String field) {
		// unused
		return null;
	}
	
}
