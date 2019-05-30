package fast.common.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fast.common.context.exception.ColIndexOutofRangeException;
import fast.common.context.exception.ColumnNotExistsException;
import fast.common.context.exception.RowIndexOutofRangeException;

public class TestDbStepResult {
	DatabaseStepResult stepResult;
	
	@Rule
	public ExpectedException throwns = ExpectedException.none();
	
	@SuppressWarnings("serial")
	public TestDbStepResult() {
		stepResult = new DatabaseStepResult();	
		
		List<String> theader = new ArrayList<String>(){{add("name");add("age");add("phone");}};
		List<List<Object>> tdata = new ArrayList<List<Object>>();
		tdata.add( new ArrayList<Object>(){{add("LiYi"); add("10"); add("4567891");}});
		tdata.add( new ArrayList<Object>(){{add("LiuEr"); add("11"); add("234567");}});
		tdata.add( new ArrayList<Object>(){{add("ZhangSan"); add("10"); add("123456");}});
		tdata.add( new ArrayList<Object>(){{add("LiSi"); add("11"); add("234567");}});
		tdata.add( new ArrayList<Object>(){{add("WangWu"); add("15"); add("345678");}});
		stepResult.setResult(new DataTable(theader,tdata));
	}
	
	@Test
	public void testGetFieldValue() throws Throwable {
		String fieldValue1 = stepResult.getFieldValue("age");
		String fieldValue2 = stepResult.getFieldValue("name");
		String fieldValue3 = stepResult.getFieldValue("phone");
		assertEquals("10", fieldValue1);
		assertEquals("LiYi", fieldValue2);
		assertEquals("4567891", fieldValue3);
	}
	
	@Test
	public void getFieldValueWithException() throws Throwable{
		stepResult = new DatabaseStepResult();
		stepResult.setResult(new DataTable());
		throwns.expect(RuntimeException.class);
		throwns.expectMessage("There isn't row in the table");
		stepResult.getFieldValue("test");
	}
	
	@Test
	public void testGetFieldsValues() throws Throwable{
		String actual = stepResult.getFieldsValues("age").get(0);
		String actual2 = stepResult.getFieldsValues("age").get(1);
		assertEquals("10", actual);
		assertEquals("11", actual2);
	}
	
	@Test
	public void testCompareData() throws RowIndexOutofRangeException, ColIndexOutofRangeException, ColumnNotExistsException{
		stepResult.compareData("age = 15 ", "name", "WangWu");
		stepResult.compareData("phone = 123456 | name = ZhangSan", "age", "10");
	}
	

	@Test
	public void testGetAffectedRows(){
		DatabaseStepResult stepResult = new DatabaseStepResult();
		stepResult.setAffectedRows(2);
		int actual = stepResult.getAffectedRows();
		assertEquals(2, actual);
	}
	
	@Test
	public void testToStringMethod(){
		DatabaseStepResult stepResult = new DatabaseStepResult();
		String actual = stepResult.toString();
		assertNotNull(actual);
	}
	
	@Test
	public void testGetCellValue() throws Throwable{
		String actual = stepResult.getCellValue("1", "age");
		assertEquals("10",actual);
	}
	
	@Test
	public void filterData_getMatchedRows() 
			throws RowIndexOutofRangeException, ColIndexOutofRangeException, ColumnNotExistsException{
		Map<String,String> filters=new HashMap<>();
		filters.put("age", "10");
		DataTable result = stepResult.filterData(filters);
		assertEquals(result.getRowCount(),2);
		filters.put("phone", "4567891");
		result=stepResult.filterData(filters);
		assertEquals(result.getRowCount(),1);
		result=stepResult.filterData(null);
		assertEquals(result.getRowCount(),5);
	}
	
	@Test
	public void filterData_wrongColNameGiven() 
			throws RowIndexOutofRangeException, ColIndexOutofRangeException, ColumnNotExistsException{
		Map<String,String> filters=new HashMap<>();
		filters.put("time", "10");
		DataTable result = stepResult.filterData(filters);
		assertEquals(result.getRowCount(),0);		
	}
}
