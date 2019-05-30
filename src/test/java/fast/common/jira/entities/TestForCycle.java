package fast.common.jira.entities;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class TestForCycle {

	private Cycle cycle;

	@Before
	public void Before() {
		cycle = new Cycle();
	}

	@Test
	public void setId_theGivenValueIsSet() {
		cycle.setId("id");
		assertEquals("id", cycle.getId());
	}

	@Test
	public void setTotalExecutions_theGivenValueIsSet() {
		cycle.setTotalExecutions(12345);
		assertEquals(12345, cycle.getTotalExecutions());
	}

	@Test
	public void setEndDate_theGivenValueIsSet() {	
		Date date = new Date();
		cycle.setEndDate(date);
		assertEquals(date, cycle.getEndDate());
	}

	@Test
	public void setDescription_theGivenStringIsSet() {
		cycle.setDescription("description");
		assertEquals("description", cycle.getDescription());
	}

	@Test
	public void setTotalExecuted_theGivenNumberIsSet() {
		cycle.setTotalExecuted(199);
		assertEquals(199, cycle.getTotalExecuted());
	}

	@Test
	public void setStarted_theGivenValueIsSet() {		
		cycle.setStarted("started");
		assertEquals("started", cycle.getStarted());
	}

	@Test
	public void setExpand_theGivenValueIsSet() {		
		cycle.setExpand("expand");
		assertEquals("expand", cycle.getExpand());
	}

	@Test
	public void setProjectKey_theGivenValueIsSet() {
		cycle.setProjectKey("projectkey");
		assertEquals("projectkey", cycle.getProjectKey());
	}

	@Test
	public void setVersionId_theGivenValueIsSet() {		
		cycle.setVersionId(2018);

		assertEquals(2018, cycle.getVersionId());
	}

	@Test
	public void setEnvironment_theGivenValueIsSet() {
		cycle.setEnvironment("environment");
		assertEquals("environment", cycle.getEnvironment());
	}

	@Test
	public void setBuild_theGivenValueIsSet() {
		cycle.setBuild("build");
		assertEquals("build", cycle.getBuild());
	}

	@Test
	public void setEnded_theGivenValueIsSet() {
		cycle.setEnded("ended");
		assertEquals("ended", cycle.getEnded());
	}

	@Test
	public void setName_theGivenValueIsSet() {
		cycle.setName("name");
		assertEquals("name", cycle.getName());
	}

	@Test
	public void setModifiedBy_theGivenValueIsSet() {
		cycle.setModifiedBy("modifiedBy");
		assertEquals("modifiedBy", cycle.getModifiedBy());
	}

	@Test
	public void setProjectId_theGivenValueIsSet() {
		cycle.setProjectId(167813);
		assertEquals(167813, cycle.getProjectId());
	}

	@Test
	public void setStartDate_theGivenValueIsSet() {
		cycle.setStartDate("startDate");
		assertEquals("startDate", cycle.getStartDate());
	}

	@Test
	public void setExecutionSummaries_theGivenValueIsSet() {		
		ExecutionSummaries obj= new ExecutionSummaries();		
		cycle.setExecutionSummaries(obj);		
		assertEquals(obj, cycle.getExecutionSummaries());
	}

}
