package fast.common.testng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.testng.ITestResult;
import org.testng.Reporter;

import fast.common.context.ScenarioContext;
import fast.common.context.ScenarioContextManager;

public class FastHelper {
	public static final String ATTRIBUTE_JIRA_KEY = "jira_key";
	protected static final Map<ITestResult, ArrayList<FastTestngStep>> FastSteps = new HashMap<>();
	
	private FastHelper(){		
	}

	public static ScenarioContext getCurrentScenarioContext() throws Exception {
		return ScenarioContextManager.getInstance().getOrCreateScenarioContext(Reporter.getCurrentTestResult());
	}
	
	public static void writeOutput(String message){

		ITestResult result = Reporter.getCurrentTestResult();
		
		if(!FastSteps.containsKey(result)){
			
			addStep(FastTestngReporter.DEFAULT_STEP_NAME);
		
		}
		
		ArrayList<FastTestngStep> steps = FastSteps.get(result);
		steps.get(steps.size()-1).addMessage(message);
		
	}
	
	public static void setCurrentJiraKey(String jiraKey){
		Reporter.getCurrentTestResult().setAttribute(ATTRIBUTE_JIRA_KEY, jiraKey);
	}
	
	public static void addStep(String name){
		
		FastTestngStep step = new FastTestngStep();
		
		step.setStepName(name);
		step.setStartTime(System.currentTimeMillis());
		
		ITestResult result = Reporter.getCurrentTestResult();
		
		if(FastSteps.containsKey(result)){
			
			FastSteps.get(result).add(step);
			
		}else{
			
			ArrayList<FastTestngStep> steps = new ArrayList<>();
			steps.add(step);
			FastSteps.put(result, steps);
			
		}
	}
}
