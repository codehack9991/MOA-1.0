package fast.common.testng;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;

import dashboard.restservice.invoke.ReportingService;
import fast.common.reporting.email.Email;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.fasterxml.jackson.core.JsonProcessingException;

import fast.common.context.ScenarioContextManager;

@RunWith(MockitoJUnitRunner.class)
public class TestFastTestngReporter {
	@Mock
	private ITestResult result;

	@Mock
	private ReportingService reportingService;

	@Mock
	private ITestNGMethod imethod;

	@Mock
	private IClass iclass;

	@Mock
	private Email email;

	private FastTestngReporter reporter;
	
	@Before
	public void setup() throws Exception{

		when(imethod.getDescription()).thenReturn("TestMethod");

		when(iclass.getName()).thenReturn("TestClass");

		when(result.getAttribute(FastHelper.ATTRIBUTE_JIRA_KEY)).thenReturn("C167813-332");
		when(result.getMethod()).thenReturn(imethod);
		when(result.getTestClass()).thenReturn(iclass);
		when(result.getStatus()).thenReturn(ITestResult.SUCCESS);
		when(result.getThrowable()).thenReturn(null);
		when(result.getStartMillis()).thenReturn(0L);
		when(result.getEndMillis()).thenReturn(0L);


		Reporter.setCurrentTestResult(result);
		ScenarioContextManager.getInstance().close();
		FastHelper.getCurrentScenarioContext();

		FastTestngStep step = new FastTestngStep();
		step.setStepName("Test Step 1");
		step.setStartTime(0L);
		step.addMessage("Test Log 1");

		ArrayList<FastTestngStep> steps = new ArrayList<>();
		steps.add(step);
		FastHelper.FastSteps.put(result, steps);

	}

	@Test
	public void generateDashboardJson_jsonReturned() throws JsonProcessingException, Exception{
		reporter = new FastTestngReporter();
		String acturalJson = reporter.generateDashboardJson();
		String expectedJson = "[{\"line\":0,\"elements\":[{\"line\":0,\"name\":\"C167813-332 TestMethod\",\"description\":null,\"type\":\"scenario\"," +
				"\"keyword\":null,\"steps\":[{\"result\":{\"duration\":0,\"status\":\"passed\",\"error_message\":\"\"},\"line\":0," +
				"\"name\":\"Test Step 1\",\"match\":null,\"keyword\":null,\"matchedColumns\":null,\"seqNumber\":1," +
				"\"debugInfo\":\"Test Log 1\",\"output\":[\"Test Log 1\"]}],\"before\":null,\"id\":\"" + result.hashCode() + 
				"\",\"after\":null,\"tags\":null,\"startRuntime\":0,\"endRuntime\":0,\"passedSteps\":1,\"duration\":0,\"totalSteps\":1}]" +
				",\"name\":\"TestClass\",\"description\":null,\"id\":null,\"keyword\":null,\"uri\":null,\"passedCases\":1,\"totalCases\":1}]";
		assertEquals(expectedJson, acturalJson);
	}

	@Test
	public void uploadDashboard_success() throws Exception {

		when(reportingService.getProjectId(anyString())).thenReturn(167813);
		doNothing().when(reportingService).uploadJsonReport(anyString(),anyString(),anyString(),anyString(),anyString(),anyString());

		System.setProperty("uploadToDashboard","true");
		reporter = new FastTestngReporter();
		reporter.setReportingService(reportingService);

		String suiteName = "TestNG Example";
		reporter.uploadDashboard(suiteName);

	}

	@Test
	public void uploadDashboard_failed() throws Exception {

		when(reportingService.getProjectId(anyString())).thenReturn(167813);

		System.setProperty("testProjectName","ForNoRecord");
		System.setProperty("uploadToDashboard","true");
		reporter = new FastTestngReporter();
		reporter.setReportingService(reportingService);

		String suiteName = "TestNG Example";
		reporter.uploadDashboard(suiteName);

	}

	@Test
	public void sendEmail() throws Exception {

		when(email.generateEmail(anyList())).thenReturn("test email");

		System.setProperty("sendEmail","true");
		reporter = new FastTestngReporter();
		reporter.setEmail(email);

		String suiteName = "TestNG Example";
		reporter.sendEmail(suiteName);

	}
}
