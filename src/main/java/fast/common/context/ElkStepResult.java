package fast.common.context;

import java.util.ArrayList;
import java.util.Map;

import fast.common.logging.FastLogger;

public class ElkStepResult extends StepResult implements ITableResult{
	private ArrayList<Map<String, Object>> result;

	static FastLogger logger = FastLogger.getLogger("ElkStepResult");

	public ElkStepResult() {
		setStatus(Status.Passed);
		result = new ArrayList<Map<String, Object>>();
	}

	public void setResult(ArrayList<Map<String, Object>> result) {
		this.result = result;
	}

	public ArrayList<Map<String, Object>> getResult() {
		return result;
	}



	@Override
	public String toString() {
		
		return "";
	}

	@Override
	public String getFieldValue(String field) throws Throwable {
	    String fieldValue = null;
        if(result.size() >= 1 ){
        	fieldValue = result.get(0).get(field).toString();
        }
		return fieldValue;
	}

	@Override
	public ArrayList<String> getFieldsValues(String field) {
		ArrayList<String> fieldsValue = new ArrayList<String>();
		for (Map<String, Object> s : result) {
			fieldsValue.add(s.get(field).toString());
		}
		return fieldsValue;
	}

	@Override
	public String getCellValue(String rowIndex, String columnName) throws Throwable {
		int index = Integer.parseInt(rowIndex) - 1;
		Map<String, Object> s = null;
		if (index < result.size()) {
			s = result.get(index);
		}
		if(s==null) return null;
		if(!s.containsKey(columnName)){
			throw new Exception("Invalid column name");
		}
		Object cell =  s.get(columnName);
		return cell == null ? null : cell.toString();
	}
}
