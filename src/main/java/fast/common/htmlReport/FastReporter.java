package fast.common.htmlReport;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.TakesScreenshot;

import com.cucumber.listener.ExtentCucumberFormatter;
import com.cucumber.listener.Reporter;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import cucumber.api.Scenario;
import cucumber.runtime.model.CucumberScenario;
import fast.common.agents.AgentsManager;
import fast.common.agents.WebBrowserAgent;
import fast.common.context.ScenarioContext;
import fast.common.context.ScenarioContextManager;
import fast.common.logging.FastLogger;
import fast.common.reporting.RuntimeInfoManager;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Result;

public class FastReporter extends ExtentCucumberFormatter {

	public FastReporter(File file) {
		super(file);
		ReportFile = file;
	}

	static Reporter reporter;
	private static FastLogger logger = FastLogger.getLogger("FastReporter");
	private static ExtentCucumberFormatter cucumberFormatter;
	private static Scenario _globalScenario;
	private static ScenarioContext _globalScenarioContext;
	
	static File ReportFile;
	
	public enum MessageType {

		INFO, PASS, FAIL, UNKNOWN, SKIPPED;
	}
	
	private static ThreadLocal<ExtentReports> thlExtReport = new ThreadLocal<ExtentReports>() {
		@Override
		protected ExtentReports initialValue() {
			return null;
		}
	};

	public static void setExtReport(ExtentReports extTreport) {
		thlExtReport.set(extTreport);
	}

	public static ExtentReports getExtReport() {
		return thlExtReport.get();
	}
	
	private static ThreadLocal<ExtentTest> thlTest = new ThreadLocal<ExtentTest>() {
		@Override
		protected ExtentTest initialValue() {
			return new ExtentTest("Test", "Test");
		}
	};

	public static void setExtTest(ExtentTest extTreport) {
		thlTest.set(extTreport);
	}

	public static ExtentTest getExtTest() {
		return thlTest.get();
	}

	/**
	 * Capture logs and capture screenshot if true
	 */
	@SuppressWarnings("static-access")
	public static void log(String Message, MessageType messageType,
			boolean captureShot) {
		String capturedShot = null;
		if (messageType == MessageType.PASS) {
			cucumberFormatter.result(new Result(Result.PASSED, 0L, Message));
			reporter.addStepLog(Message);
		} else if (messageType == MessageType.FAIL) {
			cucumberFormatter.result(new Result(Result.FAILED, 0L, new AssertionFailure(Message), null));
		} else if (messageType == MessageType.SKIPPED) {
			FastReporter.cucumberFormatter.result(new Result("skipped", null, null, Message));
			/**
			 * RunTests is the junit class from where tests are initiated
			 * _globalScenarioContext is accessible to all classes as scenario
			 */
			try {
				cucumberFormatter.endOfScenarioLifeCycle((gherkin.formatter.model.Scenario) _globalScenarioContext.getScenario());
				_globalScenarioContext.close();
			} catch (Exception e) {
				logFile(e.getMessage());			
				}
		} else if (messageType == MessageType.INFO) {
			reporter.addStepLog("INFO: " + Message);
		} else if (messageType == MessageType.UNKNOWN) {
		}
		
		logger.info(Message);
		
		/*if (captureShot) {
			capturedShot= _captureScreenshot(Thread.currentThread().getStackTrace()[2].getMethodName());
			try {
				reporter.addScreenCaptureFromPath(capturedShot, "screenImage");
			} catch (IOException e) {
				logFile(e.getMessage());		
			}
		}*/
	}
	
	
	
	public static void loadXMLConfig(File file) {
		reporter.loadXMLConfig(file);

	}

	public static void setSystemInfo(String string, String property) {
		reporter.setSystemInfo(string, property);

	}

	public static void setTestRunnerOutput(String string) {
		reporter.setTestRunnerOutput(string);

	}
	
	public static void startTest(Scenario scenario) {
		getCucumberFormatter();
		reporter.addScenarioLog(scenario.getName());
	}
	
	
	/**
	 * Capture logs to log file
	 */
	public static void logFile(String message) {
		logger.info(message.toString());
	}
	
	public static void endTest() {
		 cucumberFormatter.done();
		 cucumberFormatter.close();
	}
	
	public static void setCucumberReport(String fileString) {
		ExtentCucumberFormatter extentCucumberFormatter = new ExtentCucumberFormatter(new File(fileString));
		cucumberFormatter = extentCucumberFormatter;
	}

	public static ExtentCucumberFormatter getCucumberFormatter() {
		return cucumberFormatter;
	}
	
	public void setFeature(Feature feature) {
		cucumberFormatter.feature(feature);
		
		}
	}
