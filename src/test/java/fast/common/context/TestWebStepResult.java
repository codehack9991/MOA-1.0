package fast.common.context;


import fast.common.context.StepResult.Status;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestWebStepResult {
	
	private WebStepResult webStepResult = null;
	private ArrayList<Map<String, Object>> result = null;
	private Map<String,Object> mapObj = null;

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void setup(){
		webStepResult = new WebStepResult();
		result = new ArrayList<>();
		mapObj = new HashMap<>();
	}
	
	@Test
	public void construct_WebStepResult(){
		assertEquals(Status.Passed, webStepResult.getStatus());
	}

	@Test
	public void test_toString(){
		assertEquals("",webStepResult.toString());
	}
	@Test
	public void test_getFieldsValue(){
		webStepResult.setFieldValue("Value","test");
		assertEquals("test", webStepResult.getFieldValue("Value"));
	}

	@Test
	public void test_getFieldsValues(){
		ArrayList<String> targetList = new ArrayList<>();
		targetList.add("invalidKey");
		webStepResult.setFieldValues(targetList);
		assertEquals(targetList, webStepResult.getFieldsValues("invalidKey"));
	}


}
