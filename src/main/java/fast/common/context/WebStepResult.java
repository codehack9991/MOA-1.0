package fast.common.context;

import fast.common.logging.FastLogger;

import java.util.ArrayList;
import java.util.HashMap;

public class WebStepResult extends StepResult {
	private ArrayList<String> _list;
	private HashMap<String, String> _values;

	static FastLogger logger = FastLogger.getLogger("WebStepResult");

	public WebStepResult() {
		setStatus(Status.Passed);
		_values = new HashMap<String, String>();
		_list  = new ArrayList<>();
	}

	@Override
	public String toString() { return ""; }

	@Override
	public String getFieldValue(String field) {
		return _values.get(field);
	}

	public void setFieldValue(String field, String value) {
		_values.put(field, value);
	}

	@Override
	public ArrayList<String> getFieldsValues(String field) {
		return _list;
	}

	public void setFieldValues(ArrayList<String> values) {
		_list.addAll(values);
	}

}
