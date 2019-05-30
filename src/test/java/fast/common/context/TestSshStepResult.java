package fast.common.context;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestSshStepResult {
	private SshStepResult stepResult;

	@Before
	public void setUp() throws Exception {
		stepResult = new SshStepResult();
		stepResult.setLog("I'm a SSH step result");
		stepResult.setFieldValue("Field1", "Value1");
		stepResult.setFieldValue("Value");
	}

	@Test
	public void contains_pass() {
		stepResult.contains("SSH");
	}
	
	@Test(expected = AssertionError.class)	
	public void contains_fail() {
		stepResult.contains("HSS");
	}
	
	@Test
	public void not_contains_pass() {
		stepResult.not_contains("HSS");
	}
	
	@Test(expected = AssertionError.class)	
	public void not_contains_fail() {
		stepResult.not_contains("SSH");
	}
	
	@Test
	public void getFieldValue() throws Throwable{		
		assertEquals("Value1", stepResult.getFieldValue("Field1"));
	}
	
	@Test
	public void getFieldsValues(){
		assertEquals("Value1", stepResult.getFieldsValues("Field1").get(0));
	}
}
