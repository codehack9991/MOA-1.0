package fast.common.context;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fast.common.context.exception.ColIndexOutofRangeException;
import fast.common.context.exception.ColumnNotExistsException;
import fast.common.context.exception.RowIndexOutofRangeException;
import fast.common.logging.FastLogger;

public class DatabaseStepResult extends StepResult implements ITableResult{

	private DataTable resultTable;
	private int affectedRows;
	
	static FastLogger logger = FastLogger.getLogger("DatabaseStepResult");
	
	@Override
	public String toString() {
		return "Status:" + this.getStatus();
	}

	@Override
	public String getFieldValue(String field) throws Throwable {
		if(this.resultTable.isEmpty()){
	        throw new RuntimeException("There isn't row in the table");
	    }
		return this.resultTable.getCellValue(0, field).toString();
	}

	@Override
	public ArrayList<String> getFieldsValues(String field) {	
		final ArrayList<String> col = new ArrayList<String>();

		try {
			this.resultTable.getColumn(field).forEach(x->{
				col.add(x.toString());
			});
		} catch (Exception e) {}
		return col;
	}
	
	@Override
	public String getCellValue(String rowIndex, String columnName) throws Throwable {	
		int index = Integer.parseInt(rowIndex)-1;
		return this.resultTable.getCellValue(index, columnName).toString();
	}
	
	public void setResult(DataTable result) {
		this.resultTable = result;
	}	

	public int getAffectedRows() {
		return affectedRows;
	}

	public void setAffectedRows(int affectedRows) {
		this.affectedRows = affectedRows;
	}	
	
	public DataTable filterData(Map<String, String> filters) 
			throws RowIndexOutofRangeException, ColIndexOutofRangeException{
		DataTable result = this.resultTable.copyEmptyTable();
		
		if(filters==null || filters.isEmpty()){
			return this.resultTable;
		}
		
		String key = null;
		String value = null;	
		boolean matchFilter=true;
		for(int rowIndex=0;rowIndex<this.resultTable.getRowCount();++rowIndex){
			matchFilter=true;
			
			for(Entry<String, String> entry : filters.entrySet()){
				key=entry.getKey();
				value=entry.getValue();
				try{
					matchFilter= value.equals(this.resultTable.getCellValue(rowIndex, key));
				}catch(ColumnNotExistsException ex){
					matchFilter = false;
				}
				if(!matchFilter) break;
			}					
			if(matchFilter){
				result.addRow(this.resultTable.getRow(rowIndex));
			}
		}		
		return result;
	}

	public void compareData(String condition, String expectedField, String expectedValue)
			throws RowIndexOutofRangeException, ColIndexOutofRangeException, ColumnNotExistsException{
		
		String[] conditions = condition.trim().split("\\|");
		Map<String, String> filters = new HashMap<String, String>();
		for (String oneCondition : conditions) {
			if (!oneCondition.isEmpty()) {
				String[] match = oneCondition.split("=");
				filters.put(match[0].trim(), match[1].trim());
			}
		}
		
		DataTable filterData = this.filterData(filters);
		
		for(int rowIndex=0;rowIndex< filterData.getRowCount();++rowIndex){
			assertEquals(expectedValue, filterData.getCellValue(rowIndex, expectedField));
		}
	}
}
