package fast.common.jira.entities;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TestForExecutionSummary {

	private ExecutionSummaries summaries;

	@Before
	public void Before() {
		summaries = new ExecutionSummaries();
	}
	
	@Test
	public void setExecutionSummaries_givenValueSet() {
		ArrayList<Object> objList= new ArrayList<Object>();	
		objList.add("obj");
		summaries.setExecutionSummary(objList);		
		assertEquals("obj", summaries.getExecutionSummary().get(0));
	}
}
