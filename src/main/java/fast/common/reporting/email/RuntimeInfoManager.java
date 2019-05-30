package fast.common.reporting;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.Scenario;
import fast.common.context.SuiteContext;
import fast.common.jira.JiraUploader;
import fast.common.jira.JiraUploader.JiraExecutionStatus;
import fast.common.logging.FastLogger;

public enum RuntimeInfoManager {

	INSTANCE;
	
	int scenarioRuntimeInfoFileNum = 1;
	private FastLogger logger = FastLogger.getLogger("RuntimeInfoManager");
	private HashMap<Scenario, ScenarioRuntimeInfo> scenarioToRuntimeInfo = new HashMap<>();
	private HashMap<Scenario, HashMap<Object, Boolean>> scenarioToGlueObjects = new HashMap<>();

	private RuntimeInfoManager() {
		
		try {
			SuiteContext.getInstance().verifyFrameworkVersion();
		} catch (Exception e) {
			logger.warn("Exception is thrown when verifying framework version");
		}
	}

	public int getScenarioRuntimeInfoNum() {
		return scenarioRuntimeInfoFileNum++;
	}

	
	public void saveReportToFile(Scenario scenario) throws Exception {

		if (scenarioToRuntimeInfo.containsKey(scenario)) {
			ObjectMapper mapper = new ObjectMapper();
			String processName = ManagementFactory.getRuntimeMXBean().getName();
			String fileName = processName + "-" + getScenarioRuntimeInfoNum() + ".json";
			ScenarioRuntimeInfo runtimeInfo = scenarioToRuntimeInfo.get(scenario);

			runtimeInfo.setScenarioId(scenario.getId());

			String runtimeInfoDir = "./target/debugInfo/";
			File dir = new File("./target/debugInfo/");
			if (!dir.exists()) {
				try {
					dir.mkdirs();
				} catch (SecurityException se) {
					logger.error("Failed to make directory. Exception: " + se.getMessage());
				}
			}
			String pathName = runtimeInfoDir + fileName;
			logger.info("Save runtime info into " + pathName);
			mapper.writeValue(new File(pathName), runtimeInfo);
		}
	}


	public void notifyScenarioStart(Scenario scenario, Object glueObj) {
		if (!scenarioToRuntimeInfo.containsKey(scenario)) {
			ScenarioRuntimeInfo runtimeInfo = new ScenarioRuntimeInfo();
			runtimeInfo.setScenarioStartRuntime(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
			scenarioToRuntimeInfo.put(scenario, runtimeInfo);
			logger.debug(String.format("Scenario %s start time is set as %s", scenario.getName(),
					runtimeInfo.getScenarioStartRuntime()));
		}

		if (!scenarioToGlueObjects.containsKey(scenario)) {
			scenarioToGlueObjects.put(scenario, new HashMap<Object, Boolean>());

		}

		if (!scenarioToGlueObjects.get(scenario).containsKey(glueObj)) {
			scenarioToGlueObjects.get(scenario).put(glueObj, true);
		}
	}

	public void notifyScenarioEnd(Scenario scenario, Object glueObj) {
		if (scenarioToRuntimeInfo.containsKey(scenario)) {
			scenarioToRuntimeInfo.get(scenario)
					.setScenarioEndRuntime(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
			logger.debug(String.format("Scenario %s end time is set as %s", scenario.getName(),
					scenarioToRuntimeInfo.get(scenario).getScenarioEndRuntime()));			
		}

		if (scenarioToGlueObjects.containsKey(scenario) && scenarioToGlueObjects.get(scenario).containsKey(glueObj)) {
			scenarioToGlueObjects.get(scenario).put(glueObj, false);
		}
		
        if(isScenarioEnded(scenario) && JiraUploader.getInstance().getEnabled()) {
            try{
            	JiraUploader.getInstance().uploadNewExecutionInfo(JiraUploader.getIssueKeyFromScenarioName(scenario.getName()),
                        JiraExecutionStatus.convertToJiraExecutionStatus(scenario.getStatus()).getValue(),
                        JiraUploader.getInstance().getUser(), null);
            }catch(Exception exception){
                logger.error(String.format("Scenario ID:'%s' Failed to upload execution status for scenario '%s', error: %s", 
                        scenario.getId(),scenario.getName(), exception.toString()));
            }
        }
	}
	
	public boolean isScenarioStarted(Scenario scenario) {
		if (scenarioToGlueObjects.containsKey(scenario)) {
			HashMap<Object, Boolean> glueObjects = scenarioToGlueObjects.get(scenario);
			if (glueObjects.size() > 0) {
				return true;
			}
		}

		return false;
	}

	public boolean isScenarioEnded(Scenario scenario) {
		if (scenarioToGlueObjects.containsKey(scenario)) {
			HashMap<Object, Boolean> glueObjects = scenarioToGlueObjects.get(scenario);
			if (glueObjects.size() > 0) {
				Set<Object> objs = glueObjects.keySet();
				for (Object obj : objs) {
					if (glueObjects.get(obj)) {
						return false;
					}
				}
			}
		}

		return true;
	}
}
