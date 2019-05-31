package fast.common.testng;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFastHelper {
	
	@Mock
	private ITestResult result;
	
	@Before
	public void setup() {
		Reporter.setCurrentTestResult(result);
	}

    @Test
    public void getCurrentScenarioContext() throws Exception {
        assertNotNull(FastHelper.getCurrentScenarioContext());
    }

    @Test
    public void writeOutput() throws Exception {
        FastHelper.writeOutput("test output");
        ArrayList<FastTestngStep> steps = FastHelper.FastSteps.get(Reporter.getCurrentTestResult());
        assertEquals("test output", steps.get(steps.size() - 1).getMessages().get(0));;
    }

    @Test
    public void setCurrentJiraKey(){
        ITestResult result = mock(ITestResult.class);
        Reporter.setCurrentTestResult(result);
        doNothing().when(result).setAttribute(anyString(),anyString());
        when(result.getAttribute(FastHelper.ATTRIBUTE_JIRA_KEY)).thenReturn("test jira key");
        FastHelper.setCurrentJiraKey("test jira key");
        assertEquals("test jira key", Reporter.getCurrentTestResult().getAttribute(FastHelper.ATTRIBUTE_JIRA_KEY));
    }

    @Test
    public void addStep(){
        FastHelper.addStep("test step name");
        FastTestngStep step = FastHelper.FastSteps.get(Reporter.getCurrentTestResult()).get(0);
        assertEquals("test step name", step.getStepName());
    }
}
