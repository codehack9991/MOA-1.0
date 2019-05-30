package fast.common.glue;

import static fast.common.glue.BaseCommonStepDefs.getScenarioContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import fast.common.agents.messaging.SolaceAgent;
import fast.common.context.MessagingStepResult;
import fast.common.context.ScenarioContext;
import fast.common.context.DNAStepResult;
import fast.common.context.DateTimeDifferStepResult;
import fast.common.utilities.DateTimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;

import cucumber.api.DataTable;
import cucumber.api.Scenario;
import fast.common.agents.AgentDNA;
import fast.common.agents.AgentsManager;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AgentsManager.class,BaseCommonStepDefs.class,DateTimeUtils.class})
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestCommonStepDefs {
	
	@Mock
	AgentsManager agentsManager;
	
	private CommonStepDefs stepDefs = new CommonStepDefs();

	private Scenario scenario;
	
	CommonStepDefs commonStepDefs = new CommonStepDefs();
	
	@Mock
	private ScenarioContext scenarioContext;
	
	@Mock
	private DateTimeDifferStepResult dateTimeStepResult;

	@Before
	public void setUp() throws Exception {
		scenario = new Scenario() {

			@Override
			public void write(String text) {

			}

			@Override
			public boolean isFailed() {
				return false;
			}

			@Override
			public String getStatus() {
				return null;
			}

			@Override
			public Collection<String> getSourceTagNames() {
				return null;
			}

			@Override
			public String getName() {
				return "scenario";
			}

			@Override
			public String getId() {
				return "123";
			}

			@Override
			public void embed(byte[] data, String mimeType) {

			}
		};
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBeforeScenario() throws Exception {
		stepDefs.beforeScenario(scenario);
		assertNotNull(stepDefs);
	}

	@Test
	public void testAfterScenario() throws Exception {
		stepDefs.beforeScenario(scenario);
		stepDefs.afterScenario(scenario);
		assertNotNull(stepDefs);
	}
	
	@Test
	public void testReceiveTextMessage() throws Throwable {
		stepDefs.beforeScenario(scenario);
		SolaceAgent agent = mock(SolaceAgent.class);
		MessagingStepResult messagingStepResult = new MessagingStepResult();
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.receive()).thenReturn(messagingStepResult);
		stepDefs.receiveTextMessage("SolaceAgent","@receiveTextNew");
		assertEquals(messagingStepResult,getScenarioContext().getVariable("@receiveTextNew"));
	}
	
	@Test
	public void testGetDateTimeDiffer() throws Throwable{
		PowerMockito.mockStatic(BaseCommonStepDefs.class);
		when(BaseCommonStepDefs.getScenarioContext()).thenReturn(scenarioContext);
		when(scenarioContext.processString("endTime")).thenReturn("10:10:00");
		when(scenarioContext.processString("startTime")).thenReturn("10:00:00");
		PowerMockito.mockStatic(DateTimeUtils.class);
		when(DateTimeUtils.getDifferDateTimeAttributes("10:10:00", "10:00:00", "HH:mm:ss")).thenReturn(dateTimeStepResult);
		doNothing().when(scenarioContext).saveLastStepResult(dateTimeStepResult,"invalidVarName");
		CommonStepDefs commonStepDefs = new CommonStepDefs();
		commonStepDefs.getDateTimeDifferAttributes("endTime", "startTime", "HH:mm:ss", "invalidVarName");
	}

	@Test
	public void testdnaExecuteAndCheck() throws Throwable {
		stepDefs.beforeScenario(scenario);
		DNAStepResult result = new DNAStepResult("result");

		// Setup Mocks
		AgentDNA agent = mock(AgentDNA.class);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.sendQuery(eq("ServiceName"), any(String.class))).thenReturn(result);

		CommonStepDefs commonStepDefs = new CommonStepDefs();
		commonStepDefs.dnaExecuteAndCheck("DNAAgent", "ServiceName", "@saveResult",
				"select from arequest where parentOrderID like \"1867gdfy3\", msgType =\"D\"");

		assertEquals(result, getScenarioContext().getVariable("@saveResult"));
	}
	
	@Test(expected=RuntimeException.class)
	public void testdnaExecuteAndCheck_Exception() throws Throwable {
		stepDefs.beforeScenario(scenario);

		// Setup Mocks
		AgentDNA agent = mock(AgentDNA.class);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.sendQuery(eq("ServiceName"), any(String.class))).thenReturn(null);

		CommonStepDefs commonStepDefs = new CommonStepDefs();
		commonStepDefs.dnaExecuteAndCheck("DNAAgent", "ServiceName", "@saveResult",
				"select from arequest where parentOrderID like \"1867gdfy3\", msgType =\"D\"");

	}

	@Test
	public void testdnaExecuteAndCheckValue() throws Throwable {
		stepDefs.beforeScenario(scenario);

		DNAStepResult result = mock(DNAStepResult.class);
		when(result.getFieldValue("dnContract")).thenReturn("1");
		
		List<List<String>> inputData = (List)Arrays.asList(Arrays.asList("dnContract", "true"), Arrays.asList("1", "2"));
		DataTable inputTable = DataTable.create(inputData);

		// Setup Mocks
		AgentDNA agent = mock(AgentDNA.class);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.sendQuery(eq("ServiceName"), any(String.class))).thenReturn(result);

		CommonStepDefs commonStepDefs = new CommonStepDefs();
		commonStepDefs.dnaExecuteAndCheckValue("DNAAgent", "ServiceName", "@saveResult", "select from output",
				inputTable);

		assertEquals(result, getScenarioContext().getVariable("@saveResult"));
	}
	
	@Test
	public void testdnaExecuteAndCheckValueExceptionDontMatch() throws Throwable {
		stepDefs.beforeScenario(scenario);

		DNAStepResult result = mock(DNAStepResult.class);
		when(result.getFieldValue("dnContract")).thenReturn("0");

		List<List<String>> inputData = (List)Arrays.asList(Arrays.asList("dnContract", "true"), Arrays.asList("1", "2"));
		DataTable inputTable = DataTable.create(inputData);

		// Setup Mocks
		AgentDNA agent = mock(AgentDNA.class);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.sendQuery(eq("ServiceName"), any(String.class))).thenReturn(result);
		
		boolean raisedException = false;
		
		try {

		CommonStepDefs commonStepDefs = new CommonStepDefs();
		commonStepDefs.dnaExecuteAndCheckValue("DNAAgent", "ServiceName", "@saveResult", "select from output",
				inputTable);
		} catch (RuntimeException ex) {
			if(ex.getMessage().contains("Value of cells doesn't match")) {
				raisedException = true;
			}
		}
		assertEquals(true, raisedException);
		assertEquals(null, getScenarioContext().getVariable("@saveResult"));
	}
	
	@Test
	public void testdnaExecuteAndCheckMultipleValueSuccess() throws Throwable {
		stepDefs.beforeScenario(scenario);

		List<List<String>> inputData = (List)Arrays.asList(Arrays.asList("dnContract", "true"), Arrays.asList("1", "2"));
		DataTable inputTable = DataTable.create(inputData);
		
		DNAStepResult result = mock(DNAStepResult.class);
		doNothing().when(result).check(inputTable);

		// Setup Mocks
		AgentDNA agent = mock(AgentDNA.class);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.sendQuery(eq("ServiceName"), any(String.class))).thenReturn(result);
		
		CommonStepDefs commonStepDefs = new CommonStepDefs();
		commonStepDefs.dnaExecuteAndCheck("DNAAgent", "ServiceName", "@saveResult", "select from output",
				inputTable);

		assertEquals(result, getScenarioContext().getVariable("@saveResult"));
	}
	
	@Test
	public void testdnaExecuteAndCheckMultipleValueExceptionNoRows() throws Throwable {
		stepDefs.beforeScenario(scenario);

		List<List<String>> inputData = (List)Arrays.asList(Arrays.asList("dnContract", "true"), Arrays.asList("1", "2"));
		DataTable inputTable = DataTable.create(inputData);
		
		DNAStepResult result = mock(DNAStepResult.class);
		doThrow(new RuntimeException("Method check. There are no rows in the actualTable ")).when(result).check(inputTable);

		// Setup Mocks
		AgentDNA agent = mock(AgentDNA.class);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.sendQuery(eq("ServiceName"), any(String.class))).thenReturn(result);
		
		boolean raisedException = false;
		CommonStepDefs commonStepDefs = new CommonStepDefs();
		try {
		commonStepDefs.dnaExecuteAndCheck("DNAAgent", "ServiceName", "@saveResult", "select from output",
				inputTable);
		} catch(RuntimeException ex) {
			if(ex.getMessage().contains("There are no rows in the actualTable")) {
				raisedException = true;
			}
		}

		assertEquals(true, raisedException);
		assertEquals(null, getScenarioContext().getVariable("@saveResult"));
	}
	
	@Test
	public void testdnaExecuteAndPollAndCheckSuccess() throws Throwable {
		stepDefs.beforeScenario(scenario);
		DNAStepResult result = new DNAStepResult("result");

		// Setup Mocks
		AgentDNA agent = mock(AgentDNA.class);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.executeQueryWithPoll(eq("ServiceName"), any(String.class))).thenReturn(result);

		CommonStepDefs commonStepDefs = new CommonStepDefs();
		commonStepDefs.dnaExecuteAndPollAndCheck("DNAAgent", "ServiceName", "@saveResult",
				"select from arequest where parentOrderID like \"1867gdfy3\", msgType =\"D\"");

		assertEquals(result, getScenarioContext().getVariable("@saveResult"));
	}
	
	@Test
	public void testdnaExecuteAndPollAndCheckException() throws Throwable {
		stepDefs.beforeScenario(scenario);
		boolean raisedException = false;
		DNAStepResult result = new DNAStepResult("result");

		// Setup Mocks
		AgentDNA agent = mock(AgentDNA.class);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.executeQueryWithPoll(eq("ServiceName"), any(String.class))).thenThrow(new RuntimeException("Result of query : select from arequest where parentOrderID like 1867gdfy3, msgType =D is blank."));

		CommonStepDefs commonStepDefs = new CommonStepDefs();
		try {
		commonStepDefs.dnaExecuteAndPollAndCheck("DNAAgent", "ServiceName", "@saveResult",
				"select from arequest where parentOrderID like \"1867gdfy3\", msgType =\"D\"");
		} catch(RuntimeException ex) {
			if(ex.getMessage().contains("Result of query")) {
				raisedException = true;
			}
		}

		assertEquals(true, raisedException);
	}
	
	@Test
	public void testdnaExecuteAndCheckWithPoll() throws Throwable {
		stepDefs.beforeScenario(scenario);
		List<List<String>> inputData = (List)Arrays.asList(Arrays.asList("dnContract", "true"), Arrays.asList("1", "2"));
		DataTable inputTable = DataTable.create(inputData);
		
		DNAStepResult result = mock(DNAStepResult.class);
		doNothing().when(result).check(inputTable);

		// Setup Mocks
		AgentDNA agent = mock(AgentDNA.class);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.executeQueryWithPoll(eq("ServiceName"), any(String.class))).thenReturn(result);

		CommonStepDefs commonStepDefs = new CommonStepDefs();
		commonStepDefs.dnaExecuteAndCheckWithPoll("DNAAgent", "ServiceName", "@saveResult",
				"select from arequest where parentOrderID like \"1867gdfy3\", msgType =\"D\"",inputTable);

		
		assertEquals(result, getScenarioContext().getVariable("@saveResult"));
	}
	
	@Test
	public void testdnaExecuteAndCheckAll_Success() throws Throwable {
		stepDefs.beforeScenario(scenario);

		List<List<String>> inputData = (List)Arrays.asList(Arrays.asList("dnContract", "true"), Arrays.asList("1", "2"));
		DataTable inputTable = DataTable.create(inputData);
		
		DNAStepResult result = mock(DNAStepResult.class);
		doNothing().when(result).checkAll(inputTable);

		// Setup Mocks
		AgentDNA agent = mock(AgentDNA.class);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getOrCreateAgent(any(String.class))).thenReturn(agent);
		when(agent.sendQuery(eq("ServiceName"), any(String.class))).thenReturn(result);
		
		CommonStepDefs commonStepDefs = new CommonStepDefs();
		commonStepDefs.dnaExecuteAndCheckAll("DNAAgent", "ServiceName", "@saveResult", "select from output",
				inputTable);

		assertEquals(result, getScenarioContext().getVariable("@saveResult"));
	}
}


