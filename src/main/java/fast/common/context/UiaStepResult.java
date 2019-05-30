package fast.common.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import fast.common.logging.FastLogger;

public class UiaStepResult extends StepResult implements ITableResult {
	public static final String DefaultField = "Value";
	private String _rawstr;
    private HashMap<String, String> _values;
    private LinkedHashMap<String, List<String>> table;
    private String errorMessage;    
    static FastLogger logger = FastLogger.getLogger("UiaStepResult");
    
    public UiaStepResult(String rawstr) {
    	_rawstr = rawstr;
        _values = new HashMap<String, String>();   
        if(rawstr == null){
        	return;
        }
        
        String upperCaseStr = rawstr.toUpperCase();
        if(upperCaseStr.startsWith("ERROR:")){
        	setStatus(Status.Failed);
        	if(_rawstr.length() > 8){
        		errorMessage =  _rawstr.substring(6);
        	}
        }
        else if (upperCaseStr.startsWith("SUCCESS")){
        	setStatus(Status.Passed);
        	if(upperCaseStr.startsWith("SUCCESS:") && _rawstr.length() > 8){
        		String value=_rawstr.substring(8);
        		_values.put(DefaultField, value);
        		if (value.matches("<table>.*?</table>")) {
					this.convertValueToTable(value);
				}
        	}
        }
    }    

    public String toString() {
        return _rawstr;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getFieldValue(String field) {
        return _values.get(field);
    }
    
    public void setFieldValue(String field, String value) {
        _values.put(field,  value);
    }

    public ArrayList<String> getFieldsValues(String field) { 
    	ArrayList<String> retval = new ArrayList<String>();
    	retval.add(_values.get(field));
    	return retval;
    }

    public void contains(String userstr) throws XPathExpressionException {
        _values.containsKey(userstr);
    }
	
	public int getTableRowCount(){
		if(this.table!=null && this.table.entrySet().size()>0){
			for(String key : this.table.keySet()){
				return this.table.get(key).size();
			}
		}
		return 0;
	}
	
	public int getTableColumnCount(){
		if(this.table!=null){
			return this.table.size();
		}
		return 0;
	}
	
	public List<String> getColumnCells(String colName){
		if(isTableEmpty()||
			!this.table.containsKey(colName)){
			return null;
		}			
		
		return this.table.get(colName);
	}
	
	public List<String> getRowCells(int rowIndex){
		if(isTableEmpty()){
			return null;
		}
		
		List<String> result=new ArrayList<>();
		
		for(List<String> col : this.table.values() ){
			if(col.size()>0 && rowIndex<=col.size()){
				result.add(col.get(rowIndex-1));
			}else{
				return null;
			}
		}						
		
		return result;
	}
	
	public List<String> getHeaders(){
		if(this.table==null || 
				this.table.keySet().size()==0){
				return null;
			}
		List<String> headers=new ArrayList<>();
		for(String header : this.table.keySet()){
			headers.add(header);
		}
		return headers;
	}
	
	public String getHeader(int index){
		List<String> headers=this.getHeaders();
		if(headers==null || index<1 || index>headers.size()){
			return null;
		}
		return headers.get(index-1);
	}
	
	private boolean isTableEmpty(){
		return this.table==null || 
				this.table.keySet().size()==0;
	}

	private void convertValueToTable(String value){
		String theadPattern="<thead>(.*?)</thead>";
		String thPattern="<th>(.*?)</th>";
		String tbodyPattern="<tbody>(.*?)</tbody>";
		String trPattern="<tr>(.*?)</tr>";
		String tdPattern="<td>(.*?)</td>";
		table=new LinkedHashMap<>();
		Pattern pattern = Pattern.compile(theadPattern);
		Matcher matcher=pattern.matcher(value);
		if(!matcher.find()){
			return;
		}
		String theadStr=matcher.group(1);
		pattern=Pattern.compile(thPattern);
		matcher=pattern.matcher(theadStr);
		while(matcher.find()){
			table.put(matcher.group(1),new ArrayList<>());
		}
		
		pattern=Pattern.compile(tbodyPattern);
		matcher=pattern.matcher(value);
		if(!matcher.find()){
			return;
		}
		String tbodyStr=matcher.group(1);
		List<String> trs=new ArrayList<>();
		pattern=Pattern.compile(trPattern);
		matcher=pattern.matcher(tbodyStr);
		while(matcher.find()){
			trs.add(matcher.group(1));
		}
		
		pattern=Pattern.compile(tdPattern);
		for(String tr: trs){
			matcher=pattern.matcher(tr);
			for(String key: table.keySet()){
				if(matcher.find()){
					table.get(key).add(matcher.group(1));
				}
			}
		}
	}

	@Override
	public String getCellValue(String rowIndex, String columnName) throws Throwable {
		int index = Integer.parseInt(rowIndex)-1;
		return table.get(columnName).get(index);
	}
	
}
