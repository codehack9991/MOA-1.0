package fast.common.context;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCommonStepResult {
	
	private CommonStepResult commonStepResult;
	
	@Before
	public void setup(){
		commonStepResult = new CommonStepResult();
	}
	
	@After
	public void tearDown(){
		
	}
	
	@Test
	public void testSetErrorMessage(){
		commonStepResult.setErrorMessage("test set error message");
		assertEquals("test set error message",commonStepResult.getErrorMessage());
	}
	
	@Test
	public void testGetErrorMessage(){
		commonStepResult.setErrorMessage("test get error message");
		assertEquals("test get error message",commonStepResult.getErrorMessage());
	}
	
	@Test
	public void testSetFieldValueWithValue(){
		commonStepResult.setFieldValue("defaultFieldValue");
		assertEquals("defaultFieldValue", commonStepResult.getFieldValue(commonStepResult.DefaultField));
	}
	
	@Test
	public void testGetFieldValue(){
		commonStepResult.setFieldValue("defaultFieldValue");
		assertEquals("defaultFieldValue", commonStepResult.getFieldValue(commonStepResult.DefaultField));
	}
	
	@Test
	public void testSetFieldValueWithFieldAndValue(){
		commonStepResult.setFieldValue("testField", "testValue");
		assertEquals("testValue",commonStepResult.getFieldValue("testField"));
	}
	
	@Test
	public void testGetFieldsValues(){
		commonStepResult.setFieldValue("testField", "testValue");
		ArrayList<String> list = new ArrayList<>();
		list.add("testValue");
		assertEquals(list,commonStepResult.getFieldsValues("testField"));
	}
	
	@Test
	public void testToString(){
		commonStepResult.setFieldValue("defaultFieldValue");
		assertEquals("[defaultFieldValue]",commonStepResult.toString());
	}
	
}
