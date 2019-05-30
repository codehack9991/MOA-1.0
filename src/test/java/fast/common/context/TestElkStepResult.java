package fast.common.context;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fast.common.context.StepResult.Status;

public class TestElkStepResult {
	
	private ElkStepResult elkStepResult = null;
	private ArrayList<Map<String, Object>> result = null;
	private Map<String,Object> mapObj = null;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void setup(){
		elkStepResult = new ElkStepResult();
		result = new ArrayList<>();
		mapObj = new HashMap<>();
		mapObj.put("invalidKey", "invalidValue");
		result.add(mapObj);
		elkStepResult.setResult(result);
	}
	
	@Test
	public void construct_ElkStepResult(){
		assertEquals(Status.Passed, elkStepResult.getStatus());
	}
	
	@Test
	public void test_setResult(){
		 elkStepResult.setResult(result);
		 assertEquals(result,elkStepResult.getResult());
	}
	
	@Test
	public void test_getResult(){
		 assertEquals(result,elkStepResult.getResult());
	}
	
	@Test
	public void test_toString(){
		assertEquals("",elkStepResult.toString());
	}
	
	@Test
	public void test_getFieldValue() throws Throwable{
		assertEquals("invalidValue",elkStepResult.getFieldValue("invalidKey"));
	}
	
	@Test
	public void test_getFieldValue_empty() throws Throwable{
		ArrayList<Map<String, Object>> result = new ArrayList<>();
		elkStepResult.setResult(result);
		assertEquals(null,elkStepResult.getFieldValue("invalidKey"));
	}
	
	@Test
	public void test_getFieldsValues(){
		ArrayList<String> targetList = new ArrayList<>();
		targetList.add("invalidValue");
		assertEquals(targetList, elkStepResult.getFieldsValues("invalidKey"));
	}
	
	@Test
	public void test_getCellValue() throws Throwable{
		assertEquals("invalidValue", elkStepResult.getCellValue("1", "invalidKey"));
	}
	
	@Test
	public void test_getCellValue_bigIndex() throws Throwable{
		assertEquals(null, elkStepResult.getCellValue("2", "invalidKey"));
	}
	
	@Test(expected=Exception.class)
	public void test_getCellValue_noContainKey() throws Throwable{
		assertEquals("invalidValue", elkStepResult.getCellValue("1", "invalidColumnName"));
	}
	
	@Test
	public void testException_anotherWay() throws Throwable{
	        thrown.expect(Exception.class);
	        thrown.expectMessage("Invalid column name");
	        elkStepResult.getCellValue("1", "invalidColumnName");
	}
	
	@Test
	public void test_getCellValue_nullValue() throws Throwable{
		mapObj.put("secondKey", null);
		result.add(mapObj);
		assertEquals(null, elkStepResult.getCellValue("2", "secondKey"));
	}

}
