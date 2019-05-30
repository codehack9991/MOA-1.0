package fast.common.testng;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import org.testng.ITestResult;
import org.testng.Reporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dashboard.restservice.invoke.ReportingService;
import fast.common.context.ScenarioContextManager;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;
import fast.common.reporting.email.Email;
import fast.common.reporting.email.EmailData;
import fast.common.reporting.result.Element;
import fast.common.reporting.result.Feature;
import fast.common.reporting.result.Result;
import fast.common.reporting.result.Step;

public class FastTestngReporter {
	private static final String CONFIG_REPORTING_CONFIG_YML = "config/reportingConfig.yml";
	public static final String DEFAULT_STEP_NAME = "Default";
	public static final String STATUS_PASSED = "passed";
	public static final String STATUS_FAILED = "failed";
	public static final String STATUS_SKIPPED = "skipped";
	public static final String PROPERTY_REPORT_CONFIG = "reportConfigFile";
	public static final String PROPERTY_UPLOAD_DASHBOARD = "uploadToDashboard";
	public static final String PROPERTY_SEND_EMAIL = "sendEmail";
	public static final String PROPERTY_PROJECT_NAME = "testProjectName";
	public static final String PROPERTY_SUITE_GROUP = "testSuiteGroup";
	public static final String PROPERTY_SUITE_NAME = "testSuiteName";
	public static final String PROPERTY_TEST_TYPE = "testType";
	public static final String PROPERTY_RUNBY = "runby";
	public static final String CONFIG_PARAM_REPORT = "Reporting";

	private static FastLogger logger = FastLogger.getLogger(FastTestngReporter.class.getName());
	private ReportingService restService = null;
	private Email email = null;
	private boolean uploadToDashboard = false;
	private boolean sendEmail = false;
	private String projectName = null;
	private String suiteGroup = null;
	private String suiteName = null;
	private String testType = null;
	private String runBy = null;
	private String reportConfigFile = null;

	public FastTestngReporter() throws Exception {
		loadConfig();
	}

	private void loadConfig() throws Exception {
		Properties properties = System.getProperties();
		reportConfigFile = System.getProperty(PROPERTY_REPORT_CONFIG, CONFIG_REPORTING_CONFIG_YML);
		@SuppressWarnings("unchecked")
		Map<String, String> reportMap = Configurator.getMap(Configurator.readYaml(reportConfigFile),
				CONFIG_PARAM_REPORT);
		uploadToDashboard = properties.containsKey(PROPERTY_UPLOAD_DASHBOARD)
				? Boolean.getBoolean(PROPERTY_UPLOAD_DASHBOARD)
				: Configurator.getBoolean(reportMap, PROPERTY_UPLOAD_DASHBOARD);
		sendEmail = properties.containsKey(PROPERTY_SEND_EMAIL) ? Boolean.getBoolean(PROPERTY_SEND_EMAIL)
				: Configurator.getBoolean(reportMap, PROPERTY_SEND_EMAIL);
		projectName = properties.containsKey(PROPERTY_PROJECT_NAME) ? properties.getProperty(PROPERTY_PROJECT_NAME)
				: Configurator.getString(reportMap, PROPERTY_PROJECT_NAME);
		suiteGroup = properties.containsKey(PROPERTY_SUITE_GROUP)
				? properties.getProperty(PROPERTY_SUITE_GROUP, "Regression")
				: Configurator.getStringOr(reportMap, PROPERTY_SUITE_GROUP, "Regression");
		suiteName = properties.containsKey(PROPERTY_SUITE_NAME) ? properties.getProperty(PROPERTY_SUITE_NAME)
				: Configurator.getString(reportMap, PROPERTY_SUITE_NAME);
		testType = properties.containsKey(PROPERTY_TEST_TYPE) ? properties.getProperty(PROPERTY_TEST_TYPE, "Regression")
				: Configurator.getStringOr(reportMap, PROPERTY_TEST_TYPE, "Regression");
		runBy = properties.containsKey(PROPERTY_RUNBY)
				? properties.getProperty(PROPERTY_RUNBY, System.getProperty("user.name"))
				: Configurator.getStringOr(reportMap, PROPERTY_RUNBY, System.getProperty("user.name"));
	}


	public void setReportingService(ReportingService restService){
		this.restService = restService;
	}

	public void setEmail(Email email){
		this.email = email;
	}

	private Element generateElementFromTestResult(ITestResult result) {
		Element element = new Element();
		Object jiraKey = result.getAttribute(FastHelper.ATTRIBUTE_JIRA_KEY);
		String description = result.getMethod().getDescription();
		String name = description != null && !description.isEmpty() ? description : result.getName();
		element.setName(
				String.format("%s %s", jiraKey != null && !name.startsWith(jiraKey.toString()) ? jiraKey : "", name));
		element.setType("scenario");
		element.setId(String.format("%d", result.hashCode()));
		
		String status;
		switch (result.getStatus()) {
		case ITestResult.FAILURE:
			status = STATUS_FAILED;
			break;
		case ITestResult.SUCCESS:
		case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
			status = STATUS_PASSED;
			break;
		default:
			status = STATUS_SKIPPED;
			break;
		}
		
		String errorMessage = "";
		if (result.getThrowable() != null) {
			StringBuilder stackTraceBuilder = new StringBuilder(String.format("Exception:%s", result.getThrowable()));
			stackTraceBuilder.append(System.lineSeparator());
			stackTraceBuilder.append("Stack Trace:");
			for (StackTraceElement line : result.getThrowable().getStackTrace()) {
				stackTraceBuilder.append(line.toString());
				stackTraceBuilder.append(System.lineSeparator());
			}

			errorMessage = stackTraceBuilder.toString();
		}
		
		String[] output = new String[Reporter.getOutput(result).size()];
		Reporter.getOutput(result).toArray(output);
		
		int stepNumber = 1;
		List<Step> steps = new ArrayList<>();
		
		if(!FastHelper.FastSteps.containsKey(result)){

			long duration = (result.getEndMillis() - result.getStartMillis()) * 1000000;
			Result stepResult = getStepResult(duration, status, errorMessage);
			Step step = getStep(DEFAULT_STEP_NAME, stepNumber, output, stepResult);
			steps.add(step);
		
		} else {
			
			ArrayList<FastTestngStep> fastSteps = FastHelper.FastSteps.get(result);
	
			for(int item = 0; item < fastSteps.size(); item++){
				FastTestngStep fastStep = fastSteps.get(item);
				String[] messages = fastStep.getMessages().toArray(new String[fastStep.getMessages().size()]);
				
				Result stepResult;
				if(item != fastSteps.size() - 1){
					long duration = (fastSteps.get(item + 1).getStartTime() - fastStep.getStartTime()) * 1000000;
					stepResult = getStepResult(duration, STATUS_PASSED, "");
				}else{
					long duration = (result.getEndMillis() - fastStep.getStartTime()) * 1000000;
					stepResult = getStepResult(duration, status, errorMessage);

				}
				Step step = getStep(fastStep.getStepName(), stepNumber, messages, stepResult);
				steps.add(step);
				stepNumber++;
			}
			
		}
		
		int passedSteps = 0;
		switch(status){
		case STATUS_FAILED:
			passedSteps = steps.size() - 1;
			break;
		case STATUS_PASSED:
			passedSteps = steps.size();
			break;
		case STATUS_SKIPPED:
		default:
			break;
		}

		long duration = (result.getEndMillis() - result.getStartMillis()) * 1000000;
		element.setSteps(steps.toArray(new Step[steps.size()]));
		element.setStartRuntime(result.getStartMillis());
		element.setEndRuntime(result.getEndMillis());
		element.setPassedSteps(passedSteps);
		element.setDuration(duration);
		
		return element;
	}
	
	private Result getStepResult(long duration, String status, String errorMessage){
		
		Result stepResult = new Result();
		
		stepResult.setDuration(duration);	
		stepResult.setStatus(status);
		stepResult.setError_message(errorMessage);

		return stepResult;
		
	}
	
	private Step getStep(String name, int sepNumber, String[] outputs, Result stepResult){
		
		Step step = new Step();
		
		step.setName(name);
		step.setSeqNumber(sepNumber);
		step.setOutput(outputs);
		step.setResult(stepResult);	
		
		return step;
		
	}

	public String generateDashboardJson() throws JsonProcessingException {
		Feature[] features = fetchFeatures();
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(features);
	}

	private Feature[] fetchFeatures() {
		HashMap<String, ArrayList<Element>> featureNameToElementList = new HashMap<>();
		HashMap<String, Integer> featureNameToPassedCases = new HashMap<>();

		Object[] scenarios = ScenarioContextManager.getInstance().getAllScenarios();

		for (Object scenario : scenarios) {
			if(!(scenario instanceof ITestResult)){
				logger.warn("Found non TestNG result scenario");
				continue;
			}
			ITestResult result = (ITestResult) scenario;
			Element element = generateElementFromTestResult(result);
			String featureName = result.getTestClass().getName();
			if (!featureNameToElementList.containsKey(featureName)) {
				featureNameToElementList.put(featureName, new ArrayList<Element>());
			}
			featureNameToElementList.get(featureName).add(element);
			
			int passedCases = 0;
			switch (result.getStatus()) {
			case ITestResult.SUCCESS:
			case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
				passedCases = 1;
				break;
			default:
				break;
			}
			if (featureNameToPassedCases.containsKey(featureName)) {				
				passedCases += featureNameToPassedCases.get(featureName);				
			}
			featureNameToPassedCases.put(featureName, passedCases);
		}

		Feature[] features = new Feature[featureNameToElementList.size()];
		int i = 0;
		for (Map.Entry<String,ArrayList<Element>> entry : featureNameToElementList.entrySet()) {
			String name = entry.getKey();
			ArrayList<Element> elementList = entry.getValue();
			Feature feature = new Feature();
			feature.setName(name);
			Element[] elements = new Element[elementList.size()];
			feature.setElements(elementList.toArray(elements));
			feature.setPassedCases(featureNameToPassedCases.get(name));
			feature.setTotalCases(elementList.size());
			features[i] = feature;
			i++;
		}
		
		return features;
	}

	public void uploadDashboard(String suite) {
		suiteName = suiteName == null ? suite : suiteName;
		if (!uploadToDashboard) {
			logger.info("Dashboard upload is disabled");
			return;
		}

		if(restService == null){
			setReportingService(new ReportingService());
		}

		logger.info("Validate Project info with name: " + projectName);

		int id = restService.getProjectId(projectName);
		if (id == 0) {
			logger.info("No record for Project '" + projectName
					+ "'!!! Uploading operation has been canceled. Please contact FAST Dashboard TEAM(*GT CN CSTC CET QA Automation)");
			return;
		}

		String json = null;
		try {
			json = generateDashboardJson();
		} catch (JsonProcessingException e) {
			logger.error("Failed to convert suite results to json string, cause: " + e.getMessage());
			return;
		}

		logger.info(String.format(
				"Upload report to dashboard with parameters: %nprojectName: %s%nsuiteGroup: %s%nsuiteName: %s%ntestType: %s%nuploadToDashboard: %s",
				projectName, suiteGroup, suiteName, testType, uploadToDashboard));

		restService.uploadJsonReport(projectName, suiteGroup, suiteName, testType, runBy, json);
		logger.info("Upload dashboard done !");
	}

	public void sendEmail(String suite) {
		suiteName = suiteName == null ? suite : suiteName;
		if (!sendEmail) {
			logger.info("Send email result option is disabled.");
			return;
		}

		logger.info("Send email result option is enabled.");

		if(email == null){
			setEmail(new Email(reportConfigFile));
		}

		List<EmailData> emailResult = fecthData();
		String emailContent = email.generateEmail(emailResult);
		try {
			email.sendEmail(emailContent, suiteName, null);
			logger.info("Now sending email result ...");
		} catch (MessagingException | UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		}
	}

	private List<EmailData> fecthData() {
		logger.info("Start to fetch execution results. ");
		Feature[] suiteResults = fetchFeatures();
		return getEmailResult(suiteResults);
	}

	private List<EmailData> getEmailResult(Feature[] suiteResults) {
		ArrayList<EmailData> result = new ArrayList<>();
		if (suiteResults == null)
			return Collections.emptyList();

		for (int i = 0; i < suiteResults.length; i++) {
			int scenarioNum = 0;

			Feature feature = suiteResults[i];
			EmailData e = new EmailData(feature.getName());

			if (feature.getElements() == null)
				continue;
			int passedScenario = 0;
			for (int j = 0; j < feature.getElements().length; j++) {
				boolean scenarioStatus = true;
				Element element = feature.getElements()[j];
				if (element == null)
					continue;
				String elementType = element.getType();
				if (elementType.equals("scenario")) {
					scenarioNum++;
				}
				for (int k = 0; k < element.getSteps().length; k++) {

					Step step = element.getSteps()[k];
					if (!step.getResult().getStatus().equals(STATUS_PASSED)) {
						scenarioStatus = false;
						break;
					}
				}

				if (scenarioStatus && elementType.equals("scenario")) {
					passedScenario++;
				}
			}
			e.setScenarioNum(scenarioNum);
			e.setPassedScenario(passedScenario);
			e.setFaildScenario(scenarioNum - passedScenario);
			result.add(e);
		}
		return result;
	}
}
