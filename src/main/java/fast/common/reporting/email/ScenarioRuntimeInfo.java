package fast.common.reporting;

import java.util.Date;

public class ScenarioRuntimeInfo {
	public static final String DELIMITER = "@#@";

	private long scenarioStartRuntime;
	private long scenarioEndRuntime;	
	private String scenarioId;

	public Date getScenarioStartRuntime() {
		return new Date(scenarioStartRuntime);
	}

	public void setScenarioStartRuntime(long scenarioStartRuntime) {
		this.scenarioStartRuntime = scenarioStartRuntime;
	}

	public Date getScenarioEndRuntime() {
		return new Date(scenarioEndRuntime);
	}

	public void setScenarioEndRuntime(long scenarioEndRuntime) {
		this.scenarioEndRuntime = scenarioEndRuntime;
	}

	public String getScenarioId() {
		return scenarioId;
	}

	public void setScenarioId(String scenarioName) {
		this.scenarioId = scenarioName;
	}
}
