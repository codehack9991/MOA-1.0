package fast.common.context;

import com.citi.dna.data.*;
import cucumber.api.DataTable;
import fast.common.logging.FastLogger;
import org.apache.commons.collections.CollectionUtils;
import java.util.*;

/**
 */
public class DNAStepResult extends StepResult implements ITableResult{

    static FastLogger logger = FastLogger.getLogger("DNAStepResult");

    private Table actualTable=null;
    private String actualString=null;

    public DNAStepResult(Table actualTable) {
        this.actualTable = actualTable;
    }
    
	public DNAStepResult(Object data) {
		if (data == null)
			actualString = "";
		else
			actualString = data.toString();
	}    
    
    public DNAStepResult() {

    }

    public void check(DataTable expectedTable)  {

        List<Map<String, String>> maps = expectedTable.asMaps(String.class, String.class);

        if(maps.size() != 1)
            throw new RuntimeException("Method check. Invalid table format.");

        Map<String, String> expectedMap = maps.get(0);

        Collection missingColumns = CollectionUtils.subtract(expectedMap.keySet(), Arrays.asList(actualTable.getColumnNames()));

        if(actualTable.getSize()< 1)
            throw new RuntimeException("Method check. There are no rows in the actualTable ");

        if(!missingColumns.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Missing the following expected columns: |");
            for(Object col: missingColumns) {
                String missingCol = col.toString();
                sb.append(missingCol);
                sb.append("|");
            }

            String errStr = "Missing columns in the actualTable " + sb.toString();
            logger.error(errStr);
            throw new RuntimeException(errStr);
        }

        List<String> misMatches = new LinkedList<>();

        for (int i = 0; i < actualTable.getSize(); i++) { //expectedTable.raw().size()
            for (String columnName: expectedMap.keySet()){ //expectedTable.topCells()

                String expectedValue= expectedMap.get(columnName);
                String actualValue= actualTable.get(columnName, i).toString();

                if(!expectedValue.equals(actualValue)){
                    misMatches.add("Method check. ExpectedValue [" + expectedValue +"] " + "ActualValue [" + actualValue + "] . Mismatch of cell's value in column [" + columnName+"] " + ", row [" + i + "]" );
                }
            }
        }

        for(String misMatch: misMatches){
            logger.info(misMatch);
        }

        if(!misMatches.isEmpty()){
            throw new RuntimeException("Method check. Mismatch of cell's value in the actual table"  + misMatches);
        }
    }

    private boolean rowEquals(Map<String, String> expectedMap, int i) {
        for (String columnName: expectedMap.keySet()){

            String expectedValue= expectedMap.get(columnName);
            String actualValue= actualTable.get(columnName, i).toString();

            if(!expectedValue.equals(actualValue)){
                return false;
            }
        }
        return true;
    }

    public void checkAll(DataTable expectedTable) throws Exception {

        List<Map<String, String>> expectedMaps = expectedTable.asMaps(String.class, String.class);

        Collection missingColumns = CollectionUtils.subtract(expectedMaps.get(0).keySet(), Arrays.asList(actualTable.getColumnNames()));

        if(actualTable.getSize()< 1)
            throw new RuntimeException("Method checkAll. There are no rows in the actualTable ");

        if(!missingColumns.isEmpty()) {
            logger.debug("Method checkAll. Missing columns in the actualTable " + missingColumns);
            throw new RuntimeException("Method checkAll. Missing columns in the actualTable");
        }

        int expectedTableRows = expectedMaps.size();
        int actualTableRows = actualTable.getSize();

        if(expectedTableRows!= actualTableRows){
            throw new RuntimeException("Method checkAll. Number of rows in the actualTable [" +actualTableRows + "], expectedTableRows [" + expectedTableRows + "]");
        }

        List<String> misMatches = new LinkedList<>();

        int row=0;
        for(Map<String, String> expectedMap: expectedMaps){
            boolean matchFound = false;
            for (int i = 0; i < actualTable.getSize(); i++) {
                boolean equals = rowEquals(expectedMap, i);
                if(equals) {
                    matchFound = true;
                }
            }
            if(!matchFound)
                misMatches.add("Method checkAll. Row not found in the actual table: " + expectedMap + ", in the row [" + row + "]");
            row++;
        }

        for(String misMatch: misMatches){
            logger.info(misMatch);
        }

        if(!misMatches.isEmpty()) {
            throw new RuntimeException("Method checkAll. Some rows is not found in the actual table"  + misMatches);
        }
    }

    public DataTable convertTableToFulltable(DataTable table, ScenarioContext _scenarioContext) {
        // overwriting of all cells for the method %dateToday()% in feature DNA
        List<List<String>> tableRaw = table.raw();
        List<List<String>> tableRez = new LinkedList<>(); //creation of new List because tableRaw is UnmodifiableList

        for( int i=0; i < tableRaw.size(); i++ ){
            tableRez.add( new LinkedList<>() );
            for( int j = 0; j < tableRaw.get(0).size(); j++ ){
                String changeRow = tableRaw.get(i).get(j);
                String processedText = _scenarioContext.processString( changeRow );
//                tableRaw.get(i).set(j, processedText); //tableRaw is UnmodifiableList
                tableRez.get(tableRez.size()-1).add( processedText );
            }
        }
        table = DataTable.create( tableRez, Locale.ENGLISH, table.topCells().toArray(new String[0]) ); //from List to DataTable

        return table;
    }

	@Override
	public String toString() {
		if (actualTable != null)
			return actualTable.toString();
		else
			return actualString;
	}
	
	public Table getActualTable() {
		if (actualTable != null)
			return actualTable;
		else
            throw new RuntimeException("There is no table from DNA result!");
	}
	
	/*
	 * Convert a DNA table cell value to String
	 * @param value DNA cell value
	 * @return String value
	 * @since 1.9 
	 */
	public String stringValueOf(Object value){	
		//For Character[] and char[], we cannot convert it to string value using toString() function
		if(value instanceof Character[]){
			Character[] characters = (Character[]) value; 
			StringBuilder builder = new StringBuilder();
			for(int i=0; i < characters.length; i++){
				builder.append(characters[i].charValue());
			}
			return builder.toString();
		}else if(value instanceof char[]){
			char[] chars = (char[])value;
			StringBuilder builder = new StringBuilder();
			for(int i=0; i < chars.length; i++){
				builder.append(chars[i]);
			}
			return builder.toString();
    	}else {
			return value.toString();
    	}
	}
	
    @Override
    public String getFieldValue(String field) throws Throwable {                
        String fieldValue = null;
        if(actualTable.getSize() >= 1 ){
            Object value = actualTable.get(field, 0);
			fieldValue = stringValueOf(value);
        }
        return fieldValue;
    }

    @Override
    public ArrayList<String> getFieldsValues(String field) {
    	ArrayList<String> fieldValues = new ArrayList<String>();
		for (int i = 0; i < actualTable.getSize(); i++) {
			Object value = actualTable.get(field, i);			
			fieldValues.add(stringValueOf(value));
		}
		logger.info("Get filed values of " + field + "!");
		return fieldValues;
    }
    
    @Override
	public String getCellValue(String rowIndex, String columnName) throws Throwable {
		 int index = Integer.parseInt(rowIndex)-1;    	 
         if(actualTable.getSize() >= 1 ){
        	Object value = actualTable.get(columnName, index);
        	return stringValueOf(value);
         }
         return null;
	}    
}
