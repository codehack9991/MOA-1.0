package fast.common.testng;

import java.util.Date;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.Reporter;

import fast.common.context.ScenarioContextManager;
import fast.common.jira.JiraUploader;
import fast.common.jira.JiraUploader.JiraExecutionStatus;
import fast.common.logging.FastLogger;

public class FastTestListner extends TestListenerAdapter  {
	private static FastLogger logger = FastLogger.getLogger(FastTestListner.class.getName());	
	
	@Override
	public void onStart(ITestContext context){
		super.onStart(context);		
		Date startTime = context.getStartDate();
		logger.info(String.format("FAST Test Suite [%s] based on TestNG is started at %s", context.getSuite().getName(), startTime));
	}
	
	@Override
	public void onFinish(ITestContext context){				
		Date endTime = context.getEndDate();		
		logger.info(String.format("FAST Test Suite [%s] based on TestNG is finished at %s", context.getSuite().getName(), endTime));
		try{
			FastTestngReporter reporter = new FastTestngReporter();
			String suiteName = context.getSuite().getName();
			reporter.uploadDashboard(suiteName);
			reporter.sendEmail(suiteName);
		}
		catch(Exception ex){
			logger.error(String.format("Failed to handle report due to %s", ex.getMessage()));
		}
		
		super.onFinish(context);
	}
	
	@Override
	public void onTestStart(ITestResult result){
		Reporter.setCurrentTestResult(result);
		try {
			ScenarioContextManager.getInstance().getOrCreateScenarioContext(result);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	private void updateJiraStatus(ITestResult result, JiraExecutionStatus status){
		if(!JiraUploader.getInstance().getEnabled()){
			return;
		}
		
		Object jiraKey = result.getAttribute(FastHelper.ATTRIBUTE_JIRA_KEY);
		if(jiraKey == null){
			jiraKey = JiraUploader.getIssueKeyFromScenarioName(result.getMethod().getDescription());
			result.setAttribute(FastHelper.ATTRIBUTE_JIRA_KEY, jiraKey);
		}		
		
		if(jiraKey != null){
			try{
	        	JiraUploader.getInstance().uploadNewExecutionInfo((String)jiraKey, status.getValue(),
	                    JiraUploader.getInstance().getUser(), null);
	        	logger.debug(String.format("Successfully updated execution status of JIRA issue %s to %s", jiraKey, status));
	        }catch(Exception exception){
	        	logger.error(String.format("Class:'%s' Failed to upload execution status for test '%s', error: %s", 
	        			result.getClass().getName(), jiraKey, exception.getMessage()));
	        }
		}
	}
	
	@Override
	public void onTestFailure(ITestResult result){
		super.onTestFailure(result);
		updateJiraStatus(result, JiraExecutionStatus.FAIL);
	}
	
	@Override
	public void onTestSuccess(ITestResult result){
		super.onTestSuccess(result);
		updateJiraStatus(result, JiraExecutionStatus.PASS);
	}
	
	@Override
	public void onTestSkipped(ITestResult result){
		super.onTestSkipped(result);
		updateJiraStatus(result, JiraExecutionStatus.UNEXECUTED);
	}
}
