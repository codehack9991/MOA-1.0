package fast.common.reporting.email;

public class EmailData{
	String featureName;
	int scenarioNum;
	int passedScenario;
	int faildScenario;
	String scenarioResult;
	
	public String getScenarioResult() {
		return scenarioResult;
	}

	public void setScenarioResult(String scenarioResult) {
		this.scenarioResult = scenarioResult;
	}

	public EmailData(String featureName){
		this.featureName = featureName;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public int getScenarioNum() {
		return scenarioNum;
	}

	public void setScenarioNum(int scenarioNum) {
		this.scenarioNum = scenarioNum;
	}

	public int getPassedScenario() {
		return passedScenario;
	}

	public void setPassedScenario(int passedScenario) {
		this.passedScenario = passedScenario;
	}

	public int getFaildScenario() {
		return faildScenario;
	}

	public void setFaildScenario(int faildScenario) {
		this.faildScenario = faildScenario;
	}
}
