package fast.common.context;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fast.common.context.StepResult.Status;

public class TestDateTimeDifferStepResult {
	private DateTimeDifferStepResult dateTimeStepResult = null;
	private Map<String, String> result = null;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void setup(){
		dateTimeStepResult = new DateTimeDifferStepResult();
		result = new HashMap<>();
		result.put("invalidKey", "invalidValue");
		dateTimeStepResult.setResult(result);
	}
	
	@Test
	public void construct_DateTimeStepResult(){
		assertEquals(Status.Passed, dateTimeStepResult.getStatus());
	}
	
	@Test
	public void test_setResult(){
		 dateTimeStepResult.setResult(result);
		 assertEquals(result,dateTimeStepResult.getResult());
	}
	
	@Test
	public void test_getResult(){
		 assertEquals(result,dateTimeStepResult.getResult());
	}
	
	@Test
	public void test_toString(){
		assertEquals("{invalidKey=invalidValue}",dateTimeStepResult.toString());
	}
	
	@Test
	public void test_getFieldValue() throws Throwable{
		assertEquals("invalidValue",dateTimeStepResult.getFieldValue("invalidKey"));
	}
	
	@Test
	public void test_getFieldValue_empty() throws Throwable{
		assertEquals(null,dateTimeStepResult.getFieldValue("emptyKey"));
	}
	
	@Test
	public void test_getFieldsValues(){
		assertEquals(null, dateTimeStepResult.getFieldsValues("invalidKey"));
	}
	
}
